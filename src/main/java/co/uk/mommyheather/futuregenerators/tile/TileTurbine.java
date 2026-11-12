package co.uk.mommyheather.futuregenerators.tile;

import co.uk.mommyheather.futuregenerators.config.FutureGeneratorsConfig;
import co.uk.mommyheather.futuregenerators.fluids.Fluids;
import co.uk.mommyheather.futuregenerators.util.FluidTank;
import co.uk.mommyheather.futuregenerators.util.FutureGeneratorsEnergyStorage;
import co.uk.mommyheather.futuregenerators.util.TransferUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TileTurbine extends BlockEntity {
   public int speed;
   public FluidTank tank;
   public FutureGeneratorsEnergyStorage battery;
   private boolean ticked = false;
   private int ticks = 0;

   public TileTurbine(BlockPos pos, BlockState state) {
      super(Tiles.turbine.get(), pos, state);
      this.speed = 0;
      this.tank = new FluidTank(0, fluid -> fluid.is(Fluids.HOT_WATER.get()), this::setChanged);
      this.battery = new FutureGeneratorsEnergyStorage(0, this::setChanged);
   }

   public static void tick(Level level, BlockPos pos, BlockState state, BlockEntity be) {
      if (!level.isClientSide()) {
         TileTurbine turbine = (TileTurbine)be;
         if (!turbine.ticked) {
            turbine.tank.setCapacity(FutureGeneratorsConfig.SERVER.turbineWaterCapacity.get());
            turbine.battery.setCapacity(FutureGeneratorsConfig.SERVER.turbinePowerCapacity.get());
            turbine.ticked = true;
         }

         int maxSpeed = FutureGeneratorsConfig.SERVER.turbineMaxSpeed.get();
         int step = Math.max(1, maxSpeed / (FutureGeneratorsConfig.SERVER.turbineSpinupTime.get() * 20));
         if (turbine.tank.getFluidAmount() > 0 && turbine.tank.getFluidAmount() >= turbine.speed) {
            turbine.tank.drain(turbine.speed);
            if (turbine.speed < maxSpeed) {
               turbine.speed = Math.min(maxSpeed, turbine.speed + step);
            }
         } else {
            turbine.speed = Math.max(0, turbine.speed - step);
         }

         turbine.battery.receiveEnergy(turbine.speed * FutureGeneratorsConfig.SERVER.turbineFeRatio.get());
         turbine.ticks++;
         if (turbine.ticks % 6 == 0 || turbine.speed > 0) {
            turbine.eject();
         }
      }
   }

   public void eject() {
      TransferUtils.pushEnergy(this.level, this.getBlockPos(), this.battery, target -> false);
   }

   @Override
   protected void saveAdditional(ValueOutput output) {
      super.saveAdditional(output);
      this.tank.serialize(output.child("tank"));
      this.battery.serialize(output.child("battery"));
      output.putInt("speed", this.speed);
   }

   @Override
   protected void loadAdditional(ValueInput input) {
      super.loadAdditional(input);
      input.child("tank").ifPresent(this.tank::deserialize);
      input.child("battery").ifPresent(this.battery::deserialize);
      this.speed = input.getIntOr("speed", 0);
   }
}
