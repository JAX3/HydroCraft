package co.uk.mommyheather.futuregenerators.tile;

import co.uk.mommyheather.futuregenerators.blocks.BlockFluidPipe;
import co.uk.mommyheather.futuregenerators.config.FutureGeneratorsConfig;
import co.uk.mommyheather.futuregenerators.util.BlockSegment;
import co.uk.mommyheather.futuregenerators.util.FluidPipeNetwork;
import co.uk.mommyheather.futuregenerators.util.TransferUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

/**
 * A fluid pipe. Pipes join into a {@link FluidPipeNetwork}; fluid pumped into any pipe goes straight out to the
 * containers touching the network, highest priority first, then nearest first. A side set to pull draws fluid from the
 * container on that side every {@code fluidPipeFrequency} ticks.
 */
public class TileFluidPipe extends BlockEntity {
   public TileFluidPipe.FakeFluidHandler tank;
   public @Nullable FluidPipeNetwork network;
   public int priority = 0;
   private int ticks = 0;
   ArrayList<TileFluidPipe> pipes = new ArrayList<>();

   public TileFluidPipe(BlockPos pos, BlockState state) {
      super(Tiles.fluidPipe.get(), pos, state);
      this.tank = new TileFluidPipe.FakeFluidHandler(this);
   }

   /** Whether the side towards {@code direction} pulls fluid in (rather than pushing it out). Kept in the block state. */
   public boolean isPulling(Direction direction) {
      return this.getBlockState().getValue(BlockFluidPipe.PUMP_BY_DIRECTION.get(direction));
   }

   @Override
   public void setChanged() {
      super.setChanged();
      if (this.level != null && !this.level.isClientSide()) {
         this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 2);
      }
   }

   public void invalidateNetwork() {
      if (this.network != null) {
         this.network.removePipe(this.worldPosition.asLong());
      }
   }

   public static void tick(Level level, BlockPos pos, BlockState state, BlockEntity be) {
      if (!level.isClientSide()) {
         TileFluidPipe pipe = (TileFluidPipe)be;
         if (pipe.network == null) {
            HashMap<Long, FluidPipeNetwork> networks = new HashMap<>();

            for (Direction direction : Direction.values()) {
               if (level.getBlockEntity(pos.relative(direction)) instanceof TileFluidPipe neighbour
                  && neighbour.network != null
                  && !networks.containsKey(neighbour.network.id)) {
                  networks.put(neighbour.network.id, neighbour.network);
               }
            }

            if (networks.isEmpty()) {
               pipe.network = new FluidPipeNetwork(pos.asLong());
               pipe.network.addPipe(pos.asLong(), pipe);
            } else if (networks.size() == 1) {
               FluidPipeNetwork network = networks.values().iterator().next();
               pipe.network = network;
               network.addPipe(pos.asLong(), pipe);
            } else {
               Entry<Long, FluidPipeNetwork> entry = networks.entrySet().iterator().next();
               networks.remove(entry.getKey());
               entry.getValue().addPipe(pos.asLong(), pipe);
               pipe.network = entry.getValue();
               entry.getValue().mergeIntoSelf(networks.values().toArray(new FluidPipeNetwork[0]));
            }
         }

         pipe.ticks++;
         if (pipe.ticks % FutureGeneratorsConfig.SERVER.fluidPipeFrequency.get() == 0) {
            for (Direction direction : Direction.values()) {
               if (pipe.isPulling(direction)) {
                  pipe.pull(direction);
               }
            }
         }
      }
   }

   /** Draws up to {@code fluidPipeRate} mB from the container on {@code direction} and sends it into the network. */
   private void pull(Direction direction) {
      BlockPos target = this.worldPosition.relative(direction);
      if (this.level.getBlockEntity(target) instanceof TileFluidPipe) return;
      ResourceHandler<FluidResource> handler = this.level.getCapability(Capabilities.Fluid.BLOCK, target, direction.getOpposite());
      if (handler == null) return;
      FluidResource fluid = TransferUtils.firstFluid(handler);
      if (fluid.isEmpty()) return;
      // First find out how much the network can take, then move exactly that much.
      int deliverable;
      try (Transaction simulate = Transaction.openRoot()) {
         int available = handler.extract(fluid, FutureGeneratorsConfig.SERVER.fluidPipeRate.get(), simulate);
         deliverable = this.distributeFluidIntoNetwork(fluid, available, direction, simulate);
      }
      if (deliverable <= 0) return;
      try (Transaction transaction = Transaction.openRoot()) {
         int extracted = handler.extract(fluid, deliverable, transaction);
         if (extracted > 0 && this.distributeFluidIntoNetwork(fluid, extracted, direction, transaction) == extracted) {
            transaction.commit();
         }
      }
   }

   public void updateCache() {
      if (this.network.hasChanged(this.worldPosition)) {
         this.pipes = new ArrayList<>(this.network.pipes.values());
         this.pipes.sort(Comparator.<TileFluidPipe>comparingInt(pipe -> -pipe.priority)
            .thenComparingDouble(pipe -> this.worldPosition.distSqr(pipe.worldPosition)));
      }
   }

   /** Offers {@code amount} of {@code fluid} to every container on the network. Returns how much was taken. */
   public int distributeFluidIntoNetwork(FluidResource fluid, int amount, @Nullable Direction exclude, TransactionContext transaction) {
      if (this.network == null || fluid.isEmpty() || amount <= 0) {
         return 0;
      }

      this.updateCache();
      int delivered = 0;

      for (TileFluidPipe pipe : this.pipes) {
         if (delivered >= amount) {
            break;
         }

         delivered += pipe.distributeFluidIntoConnections(fluid, amount - delivered, exclude, this.worldPosition.asLong(), transaction);
      }

      return delivered;
   }

   /**
    * Offers fluid to the containers around this pipe, except on pulling sides and the side the fluid came in through.
    * Returns how much was taken.
    */
   public int distributeFluidIntoConnections(FluidResource fluid, int amount, @Nullable Direction exclude, long source, TransactionContext transaction) {
      int delivered = 0;

      for (Direction direction : Direction.values()) {
         if ((direction != exclude || this.worldPosition.asLong() != source) && !this.isPulling(direction)) {
            if (delivered >= amount) {
               return delivered;
            }

            BlockPos pos = this.worldPosition.relative(direction);
            if (!(this.level.getBlockEntity(pos) instanceof TileFluidPipe)) {
               ResourceHandler<FluidResource> handler = this.level.getCapability(Capabilities.Fluid.BLOCK, pos, direction.getOpposite());
               if (handler != null) {
                  delivered += handler.insert(fluid, amount - delivered, transaction);
               }
            }
         }
      }

      return delivered;
   }

   /** Whether this pipe touches anything that holds fluid (other than pipes). */
   public boolean isConnectedToTank() {
      if (this.level == null) return false;
      for (Direction direction : Direction.values()) {
         BlockPos pos = this.worldPosition.relative(direction);
         if (!(this.level.getBlockEntity(pos) instanceof TileFluidPipe)
            && this.level.getCapability(Capabilities.Fluid.BLOCK, pos, direction.getOpposite()) != null) {
            return true;
         }
      }

      return false;
   }

   @Override
   protected void saveAdditional(ValueOutput output) {
      super.saveAdditional(output);
      output.putInt("priority", this.priority);
   }

   @Override
   protected void loadAdditional(ValueInput input) {
      super.loadAdditional(input);
      this.priority = input.getIntOr("priority", 0);
   }

   public void handleInteraction(BlockSegment blockSegment, boolean crouching, Player player) {
      switch (blockSegment) {
         case CENTRE:
            this.priority += crouching ? -1 : 1;
            player.sendSystemMessage(Component.translatable("futuregenerators.interactions.priority", this.priority));
            this.setChanged();
            if (this.network != null) this.network.setChanged();
            return;
         default:
            boolean mode = !this.isPulling(blockSegment.getDirection());
            BlockState state = this.level
               .getBlockState(this.worldPosition)
               .setValue(BlockFluidPipe.PUMP_BY_DIRECTION.get(blockSegment.getDirection()), mode);
            this.level.setBlockAndUpdate(this.worldPosition, state);
            player.sendSystemMessage(Component.translatable("futuregenerators.interactions.pulling", mode));
            if (this.network != null) this.network.setChanged();
      }
   }

   /** What a pipe offers on one side: an always-empty tank that passes whatever goes in straight on to the network. */
   public static class FakeFluidHandler implements ResourceHandler<FluidResource> {
      public final TileFluidPipe pipe;
      public final @Nullable Direction direction;

      public FakeFluidHandler(TileFluidPipe pipe) {
         this(pipe, null);
      }

      public FakeFluidHandler(TileFluidPipe pipe, @Nullable Direction side) {
         this.pipe = pipe;
         this.direction = side;
      }

      public TileFluidPipe.FakeFluidHandler of(@Nullable Direction direction) {
         return new TileFluidPipe.FakeFluidHandler(this.pipe, direction);
      }

      @Override
      public int size() {
         return 1;
      }

      @Override
      public FluidResource getResource(int index) {
         return FluidResource.EMPTY;
      }

      @Override
      public long getAmountAsLong(int index) {
         return 0;
      }

      @Override
      public long getCapacityAsLong(int index, FluidResource resource) {
         return Integer.MAX_VALUE;
      }

      @Override
      public boolean isValid(int index, FluidResource resource) {
         return true;
      }

      @Override
      public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
         return this.pipe.distributeFluidIntoNetwork(resource, amount, this.direction, transaction);
      }

      @Override
      public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
         return 0;
      }
   }

   /** A broken pipe leaves its network, which splits up if it has to. */
   @Override
   public void preRemoveSideEffects(BlockPos pos, BlockState state) {
      super.preRemoveSideEffects(pos, state);
      this.invalidateNetwork();
   }
}
