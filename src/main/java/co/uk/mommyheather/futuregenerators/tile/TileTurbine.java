package co.uk.mommyheather.futuregenerators.tile;

import co.uk.mommyheather.futuregenerators.config.FutureGeneratorsConfig;
import co.uk.mommyheather.futuregenerators.fluids.Fluids;
import co.uk.mommyheather.futuregenerators.util.FutureGeneratorsEnergyStorage;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class TileTurbine extends BlockEntity {
   public int speed;
   public FluidTank tank;
   public FutureGeneratorsEnergyStorage battery;
   private boolean ticked = false;
   private LazyOptional<IFluidHandler> lazyTank;
   private LazyOptional<IEnergyStorage> lazyBattery;
   private int ticks = 0;

   public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
      if (cap == ForgeCapabilities.FLUID_HANDLER) {
         return this.lazyTank.cast();
      } else {
         return cap == ForgeCapabilities.ENERGY ? this.lazyBattery.cast() : super.getCapability(cap, side);
      }
   }

   public void invalidateCaps() {
      super.invalidateCaps();
      this.lazyTank.invalidate();
      this.lazyBattery.invalidate();
   }

   public TileTurbine(BlockPos p_155229_, BlockState p_155230_) {
      super((BlockEntityType)Tiles.turbine.get(), p_155229_, p_155230_);
      this.speed = 0;
      this.tank = new FluidTank(0, stack -> stack.getFluid().isSame((Fluid)Fluids.HOT_WATER.get())) {
         protected void onContentsChanged() {
            TileTurbine.this.setChanged();
         }
      };
      this.battery = new FutureGeneratorsEnergyStorage(0) {
         @Override
         public void onContentsChanged() {
            TileTurbine.this.setChanged();
         }
      };
      this.lazyTank = LazyOptional.of(() -> this.tank);
      this.lazyBattery = LazyOptional.of(() -> this.battery);
   }

   public static void tick(Level level, BlockPos pos, BlockState state, BlockEntity be) {
      if (!level.isClientSide) {
         TileTurbine turbine = (TileTurbine)be;
         if (!turbine.ticked) {
            turbine.tank.setCapacity((Integer)FutureGeneratorsConfig.SERVER.turbineWaterCapacity.get());
            turbine.battery.setCapacity((Integer)FutureGeneratorsConfig.SERVER.turbinePowerCapacity.get());
            turbine.ticked = true;
         }

         if (turbine.tank.getFluidAmount() > 0 && turbine.tank.getFluidAmount() >= turbine.speed) {
            turbine.tank.drain(turbine.speed, FluidAction.EXECUTE);
            if (turbine.speed < (Integer)FutureGeneratorsConfig.SERVER.turbineMaxSpeed.get()) {
               turbine.speed = Math.min(
                  (Integer)FutureGeneratorsConfig.SERVER.turbineMaxSpeed.get(),
                  turbine.speed
                     + Math.max(
                        1, (Integer)FutureGeneratorsConfig.SERVER.turbineMaxSpeed.get() / ((Integer)FutureGeneratorsConfig.SERVER.turbineSpinupTime.get() * 20)
                     )
               );
            }
         } else {
            turbine.speed = Math.max(
               0,
               turbine.speed
                  - Math.max(
                     1, (Integer)FutureGeneratorsConfig.SERVER.turbineMaxSpeed.get() / ((Integer)FutureGeneratorsConfig.SERVER.turbineSpinupTime.get() * 20)
                  )
            );
         }

         turbine.battery.receiveEnergy(turbine.speed * (Integer)FutureGeneratorsConfig.SERVER.turbineFeRatio.get(), false);
         turbine.ticks++;
         if (turbine.ticks % 6 == 0 || turbine.speed > 0) {
            turbine.eject();
         }
      }
   }

   public void eject() {
      for (Direction direction : new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN}) {
         if (this.battery.getEnergyStored() <= 0) {
            return;
         }

         BlockEntity target = this.level.getBlockEntity(this.getBlockPos().relative(direction));
         if (target != null) {
            LazyOptional<IEnergyStorage> energyOptional = target.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite());
            if (energyOptional != null && energyOptional.isPresent()) {
               IEnergyStorage storage = (IEnergyStorage)energyOptional.resolve().get();
               int energy = storage.receiveEnergy(this.battery.getEnergyStored(), true);
               if (energy != 0) {
                  storage.receiveEnergy(energy, false);
                  this.battery.setEnergy(this.battery.getEnergyStored() - energy);
               }
            }
         }
      }
   }

   protected void saveAdditional(CompoundTag tag) {
      super.saveAdditional(tag);
      tag.put("tank", this.tank.writeToNBT(new CompoundTag()));
      tag.put("battery", this.battery.serializeNBT());
      tag.putInt("speed", this.speed);
   }

   public void load(CompoundTag tag) {
      super.load(tag);
      if (tag.contains("tank")) {
         this.tank.readFromNBT(tag.getCompound("tank"));
      }

      if (tag.contains("battery")) {
         this.battery.deserializeNBT(tag.get("battery"));
      }

      this.speed = tag.getInt("speed");
   }
}
