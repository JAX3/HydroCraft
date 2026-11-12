package co.uk.mommyheather.futuregenerators.tile;

import co.uk.mommyheather.futuregenerators.blocks.BlockFluidPipe;
import co.uk.mommyheather.futuregenerators.config.FutureGeneratorsConfig;
import co.uk.mommyheather.futuregenerators.util.BlockSegment;
import co.uk.mommyheather.futuregenerators.util.FluidPipeNetwork;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import org.jetbrains.annotations.NotNull;

public class TileFluidPipe extends BlockEntity {
   public TileFluidPipe.FakeFluidHandler tank;
   private LazyOptional<IFluidHandler> lazyTank;
   private HashMap<Direction, Boolean> pullingModes = new HashMap<>();
   public FluidPipeNetwork network;
   public int priority = 0;
   private int ticks = 0;
   ArrayList<TileFluidPipe> pipes = new ArrayList<>();

   public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
      return this.isConnectedToTank() && cap == ForgeCapabilities.FLUID_HANDLER
         ? LazyOptional.of(() -> this.tank.of(side)).cast()
         : super.getCapability(cap, side);
   }

   public void invalidateCaps() {
      super.invalidateCaps();
      this.lazyTank.invalidate();
   }

   public TileFluidPipe(BlockPos pos, BlockState state) {
      super((BlockEntityType)Tiles.fluidPipe.get(), pos, state);
      this.tank = new TileFluidPipe.FakeFluidHandler(this);
      this.lazyTank = LazyOptional.of(() -> this.tank);

      for (Direction direction : Direction.values()) {
         this.pullingModes.put(direction, (Boolean)state.getValue((Property)BlockFluidPipe.PUMP_BY_DIRECTION.get(direction)));
      }
   }

   public void setChanged() {
      super.setChanged();
      this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 2);
   }

   public void invalidateNetwork() {
      if (this.network != null) {
         this.network.removePipe(this.worldPosition.asLong());
      }
   }

   public static void tick(Level level, BlockPos pos, BlockState state, BlockEntity be) {
      if (!level.isClientSide) {
         TileFluidPipe pipe = (TileFluidPipe)be;
         if (pipe.network == null) {
            HashMap<Long, FluidPipeNetwork> networks = new HashMap<>();

            for (Direction direction : Direction.values()) {
               BlockEntity blockEntity = level.getBlockEntity(pos.offset(direction.getNormal()));
               if (blockEntity != null
                  && blockEntity instanceof TileFluidPipe neighbour
                  && neighbour.network != null
                  && !networks.containsKey(neighbour.network.id)) {
                  networks.put(neighbour.network.id, neighbour.network);
               }
            }

            if (networks.isEmpty()) {
               pipe.network = new FluidPipeNetwork(pos.asLong());
               pipe.network.addPipe(pos.asLong(), pipe);
            } else if (networks.size() == 1) {
               FluidPipeNetwork network = networks.entrySet().iterator().next().getValue();
               pipe.network = network;
               network.addPipe(pos.asLong(), pipe);
            } else {
               Entry<Long, FluidPipeNetwork> entry = networks.entrySet().iterator().next();
               networks.remove(entry.getKey());
               entry.getValue().addPipe(pos.asLong(), pipe);
               pipe.network = entry.getValue();
               entry.getValue().mergeIntoSelf(networks.values().toArray(new FluidPipeNetwork[networks.size()]));
            }
         }

         pipe.ticks++;
         if (pipe.ticks % (Integer)FutureGeneratorsConfig.SERVER.fluidPipeFrequency.get() == 0) {
            for (Direction direction : Direction.values()) {
               if (pipe.pullingModes.get(direction)) {
                  BlockPos pos2 = pos.offset(direction.getNormal());
                  BlockEntity target = level.getBlockEntity(pos2);
                  if (target != null && !(target instanceof TileFluidPipe)) {
                     LazyOptional<IFluidHandler> optional = target.getCapability(ForgeCapabilities.FLUID_HANDLER, direction.getOpposite());
                     if (optional.isPresent()) {
                        IFluidHandler handler = (IFluidHandler)optional.resolve().get();
                        FluidStack toFill = handler.drain((Integer)FutureGeneratorsConfig.SERVER.fluidPipeRate.get(), FluidAction.SIMULATE);
                        int delivered = pipe.distributeFluidIntoNetwork(toFill, FluidAction.SIMULATE, direction);
                        toFill = handler.drain(delivered, FluidAction.EXECUTE);
                        pipe.distributeFluidIntoNetwork(toFill, FluidAction.EXECUTE, direction);
                     }
                  }
               }
            }
         }
      }
   }

   public void updateCache() {
      if (this.network.hasChanged(this.worldPosition)) {
         this.pipes = new ArrayList<>(this.network.pipes.values());
         this.pipes
            .sort(
               new Comparator<TileFluidPipe>() {
                  public int compare(TileFluidPipe o1, TileFluidPipe o2) {
                     int result = o2.priority - o1.priority;
                     return result != 0
                        ? result
                        : (int)((TileFluidPipe.this.worldPosition.distSqr(o1.worldPosition) - TileFluidPipe.this.worldPosition.distSqr(o2.worldPosition)) * 100.0);
                  }
               }
            );
      }
   }

   public int distributeFluidIntoNetwork(FluidStack fluid, FluidAction action, Direction exclude) {
      if (this.network == null) {
         return 0;
      }

      this.updateCache();
      fluid = fluid.copy();
      int delivered = 0;

      for (TileFluidPipe pipe : this.pipes) {
         if (fluid.getAmount() <= 0) {
            return delivered;
         }

         fluid.setAmount(fluid.getAmount() - delivered);
         delivered += pipe.distributeFluidIntoConnections(fluid, action, exclude, this.worldPosition.asLong());
      }

      return delivered;
   }

   public int distributeFluidIntoConnections(FluidStack fluid, FluidAction action, Direction exclude, long source) {
      fluid = fluid.copy();
      int delivered = 0;

      for (Direction direction : Direction.values()) {
         if ((direction != exclude || this.worldPosition.asLong() != source) && !this.pullingModes.get(direction)) {
            if (fluid.getAmount() <= 0) {
               return delivered;
            }

            fluid.setAmount(fluid.getAmount() - delivered);
            BlockPos pos = this.worldPosition.offset(direction.getNormal());
            BlockEntity be = this.level.getBlockEntity(pos);
            if (be != null && !(be instanceof TileFluidPipe)) {
               LazyOptional<IFluidHandler> optional = be.getCapability(ForgeCapabilities.FLUID_HANDLER, direction.getOpposite());
               if (optional.isPresent()) {
                  IFluidHandler handler = (IFluidHandler)optional.resolve().get();
                  delivered += handler.fill(fluid, action);
               }
            }
         }
      }

      return delivered;
   }

   public boolean isConnectedToTank() {
      for (Direction direction : Direction.values()) {
         BlockPos pos = this.worldPosition.offset(direction.getNormal());
         BlockEntity be = this.level.getBlockEntity(pos);
         if (be != null && !(be instanceof TileFluidPipe) && be.getCapability(ForgeCapabilities.FLUID_HANDLER, direction.getOpposite()).isPresent()) {
            return true;
         }
      }

      return false;
   }

   protected void saveAdditional(CompoundTag tag) {
      super.saveAdditional(tag);
      tag.putInt("priority", this.priority);
   }

   public void load(CompoundTag tag) {
      super.load(tag);
      if (tag.contains("priority")) {
         this.priority = tag.getInt("priority");
      }
   }

   public void handleInteraction(BlockSegment blockSegment, boolean crouching, Player player) {
      switch (blockSegment) {
         case CENTRE:
            this.priority += crouching ? -1 : 1;
            player.sendSystemMessage(Component.translatable("futuregenerators.interactions.priority", new Object[]{this.priority}));
            this.network.setChanged();
            return;
         default:
            Boolean mode = !this.pullingModes.get(blockSegment.getDirection());
            this.pullingModes.put(blockSegment.getDirection(), mode);
            BlockState state = (BlockState)this.level
               .getBlockState(this.worldPosition)
               .setValue((Property)BlockFluidPipe.PUMP_BY_DIRECTION.get(blockSegment.getDirection()), mode);
            this.level.setBlockAndUpdate(this.worldPosition, state);
            player.sendSystemMessage(Component.translatable("futuregenerators.interactions.pulling", new Object[]{mode}));
            this.network.setChanged();
      }
   }

   public static class FakeFluidHandler implements IFluidHandler {
      public final TileFluidPipe pipe;
      public Direction direction;

      public FakeFluidHandler(TileFluidPipe pipe) {
         this.pipe = pipe;
      }

      public FakeFluidHandler(TileFluidPipe pipe, Direction side) {
         this.pipe = pipe;
         this.direction = side;
      }

      public TileFluidPipe.FakeFluidHandler of(Direction direction) {
         return new TileFluidPipe.FakeFluidHandler(this.pipe, direction);
      }

      public int getTanks() {
         return 1;
      }

      @NotNull
      public FluidStack getFluidInTank(int tank) {
         return FluidStack.EMPTY;
      }

      public int getTankCapacity(int tank) {
         return Integer.MAX_VALUE;
      }

      public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
         return true;
      }

      public int fill(FluidStack resource, FluidAction action) {
         return this.pipe.distributeFluidIntoNetwork(resource, action, this.direction);
      }

      @NotNull
      public FluidStack drain(FluidStack resource, FluidAction action) {
         return FluidStack.EMPTY;
      }

      @NotNull
      public FluidStack drain(int maxDrain, FluidAction action) {
         return FluidStack.EMPTY;
      }
   }
}
