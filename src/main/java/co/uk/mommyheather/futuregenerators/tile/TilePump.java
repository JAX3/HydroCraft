package co.uk.mommyheather.futuregenerators.tile;

import co.uk.mommyheather.futuregenerators.config.FutureGeneratorsConfig;
import co.uk.mommyheather.futuregenerators.util.FluidTank;
import co.uk.mommyheather.futuregenerators.util.FutureGeneratorsEnergyStorage;
import co.uk.mommyheather.futuregenerators.util.ItemStorage;
import co.uk.mommyheather.futuregenerators.util.TransferUtils;
import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jspecify.annotations.Nullable;

/** Pumps fluid source blocks from below it, spreading out through the connected fluid, powered by burning fuel. */
public class TilePump extends BlockEntity {
   public FluidTank tank;
   public FutureGeneratorsEnergyStorage battery;
   public ItemStorage items;
   private boolean ticked = false;
   private ArrayList<BlockPos> pumpedBlocks = new ArrayList<>();
   public Fluid lastPumped = Fluids.EMPTY;
   private int ticks = 0;
   private static final Direction[] customDirections = new Direction[]{
      Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN
   };

   public TilePump(BlockPos pos, BlockState state) {
      super(Tiles.pump.get(), pos, state);
      this.tank = new FluidTank(0, this::setChanged);
      this.battery = new FutureGeneratorsEnergyStorage(0, this::setChanged);
      // Fuel that leaves something behind (a lava bucket) isn't accepted.
      this.items = new ItemStorage(1, (slot, stack) -> TransferUtils.isFuel(stack) && stack.getItem().getCraftingRemainder(stack) == null, this::setChanged);
   }

   /** Every change is sent to clients, which draw the fluid and show it in the GUI. */
   @Override
   public void setChanged() {
      super.setChanged();
      if (this.level != null && !this.level.isClientSide()) {
         this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 2);
      }
   }

   public static void tick(Level level, BlockPos pos, BlockState state, BlockEntity be) {
      TilePump pump = (TilePump)be;
      if (!pump.ticked) {
         pump.tank.setCapacity(FutureGeneratorsConfig.SERVER.fluidPumpCapacity.get());
         pump.battery.setCapacity(FutureGeneratorsConfig.SERVER.fluidPumpBattery.get());
         pump.ticked = true;
      }

      if (!level.isClientSide()) {
         pump.ticks++;
         if (pump.ticks % FutureGeneratorsConfig.SERVER.fluidPumpTime.get() == 0) {
            pump.handlePowerConversion();
            FluidState fluidState = level.getBlockState(pos.below()).getFluidState();
            if (!FutureGeneratorsConfig.SERVER.drainWater.get() && fluidState.is(Fluids.WATER)) {
               pump.drain(pos.below(), false);
            } else {
               pump.drain();
            }
         }
      }
   }

   public void drain() {
      if (this.pumpedBlocks.isEmpty()) {
         this.lastPumped = Fluids.EMPTY;
         if (this.isDrainable(this.worldPosition.below()) && this.drain(this.worldPosition.below(), true)) {
            this.pumpedBlocks.add(this.worldPosition.below());
         }
      } else {
         for (int i = this.pumpedBlocks.size() - 1; i >= 0; i--) {
            BlockPos pos = this.pumpedBlocks.get(i);

            for (Direction direction : customDirections) {
               BlockPos pos2 = pos.relative(direction);
               if (this.isDrainable(pos2)) {
                  if (this.drain(pos2, true)) {
                     this.pumpedBlocks.add(pos2);
                  }

                  return;
               }
            }

            this.pumpedBlocks.remove(i);
         }
      }
   }

   public boolean drain(BlockPos pos, boolean consume) {
      if (!this.canDrain(this.level.getFluidState(pos))) {
         return false;
      }

      this.tank.fill(FluidResource.of(this.level.getFluidState(pos).getType()), 1000);
      this.battery.extractEnergy(FutureGeneratorsConfig.SERVER.fluidPumpConsumption.get());
      this.lastPumped = this.level.getFluidState(pos).getType();
      if (consume) {
         this.level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
      }

      return consume;
   }

   public boolean isDrainable(BlockPos pos) {
      FluidState state = this.level.getFluidState(pos);
      if (state.isEmpty()) {
         return false;
      } else if (!state.is(this.lastPumped) && !this.lastPumped.isSame(Fluids.EMPTY)) {
         return false;
      } else if (!state.isSource()) {
         return false;
      } else {
         return this.worldPosition.distSqr(pos) > Math.pow(FutureGeneratorsConfig.SERVER.fluidPumpRange.get(), 2.0)
            ? false
            : pos.getY() > this.worldPosition.getY() - 3;
      }
   }

   public boolean canDrain(FluidState state) {
      if (!this.tank.isEmpty() && !state.is(this.tank.getFluid().getFluid())) {
         return false;
      } else {
         return this.tank.getFluidAmount() + 1000 > this.tank.getCapacity()
            ? false
            : this.battery.getEnergyStored() - FutureGeneratorsConfig.SERVER.fluidPumpConsumption.get() >= 0;
      }
   }

   public void handlePowerConversion() {
      if (!this.items.getStackInSlot(0).isEmpty()) {
         int toInsert = (int)(
            TransferUtils.burnTime(this.level, this, this.items.getStackInSlot(0).copyWithCount(1)) * FutureGeneratorsConfig.SERVER.fluidPumpConversion.get()
         );
         if (this.battery.getMaxEnergyStored() - this.battery.getEnergyStored() >= toInsert) {
            this.battery.receiveEnergy(toInsert);
            this.items.extractItem(0, 1);
         }
      }
   }

   @Override
   protected void saveAdditional(ValueOutput output) {
      super.saveAdditional(output);
      this.tank.serialize(output.child("tank"));
      this.battery.serialize(output.child("battery"));
      this.items.serialize(output.child("items"));
      output.store("pumpedCache", BlockPos.CODEC.listOf(), this.pumpedBlocks);
      output.putString("fluid", BuiltInRegistries.FLUID.getKey(this.lastPumped).toString());
   }

   @Override
   protected void loadAdditional(ValueInput input) {
      super.loadAdditional(input);
      input.child("tank").ifPresent(this.tank::deserialize);
      input.child("battery").ifPresent(this.battery::deserialize);
      input.child("items").ifPresent(this.items::deserialize);
      input.read("pumpedCache", BlockPos.CODEC.listOf()).ifPresent(blocks -> this.pumpedBlocks = new ArrayList<>(blocks));
      input.getString("fluid").ifPresent(id -> this.lastPumped = BuiltInRegistries.FLUID.getValue(Identifier.parse(id)));
   }

   /** Clients get the pump's data, for the fluid drawn inside it and the GUI. */
   @Override
   public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
      return this.saveCustomOnly(registries);
   }

   @Override
   public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   /** Drops the machine's items when it is broken. */
   @Override
   public void preRemoveSideEffects(BlockPos pos, BlockState state) {
      super.preRemoveSideEffects(pos, state);
      if (this.level != null) {
         for (int i = 0; i < this.items.getSlots(); i++) {
            if (!this.items.getStackInSlot(i).isEmpty()) {
               Containers.dropItemStack(this.level, pos.getX(), pos.getY(), pos.getZ(), this.items.getStackInSlot(i));
            }
         }
      }
   }
}
