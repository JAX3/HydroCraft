package co.uk.mommyheather.futuregenerators.tile;

import co.uk.mommyheather.futuregenerators.config.FutureGeneratorsConfig;
import co.uk.mommyheather.futuregenerators.fluids.Fluids;
import co.uk.mommyheather.futuregenerators.util.FluidTank;
import co.uk.mommyheather.futuregenerators.util.FutureGeneratorsEnergyStorage;
import co.uk.mommyheather.futuregenerators.util.TransferUtils;
import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TileTurbineController extends BlockEntity {
   public int speed;
   public FluidTank tank;
   public FutureGeneratorsEnergyStorage battery;
   private boolean ticked = false;
   private int ticks = 0;
   public boolean valid = false;
   public ArrayList<TileTurbineCasing> casings = new ArrayList<>();

   public TileTurbineController(BlockPos pos, BlockState state) {
      super(Tiles.turbineController.get(), pos, state);
      this.speed = 0;
      this.tank = new FluidTank(0, fluid -> fluid.is(Fluids.HOT_WATER.get()), this::setChanged);
      this.battery = new FutureGeneratorsEnergyStorage(0, this::setChanged);
   }

   @Override
   public void setRemoved() {
      super.setRemoved();
      this.valid = false;
      this.casings.forEach(casing -> casing.setController(null));
   }

   public static void tick(Level level, BlockPos pos, BlockState state, BlockEntity be) {
      if (!level.isClientSide()) {
         TileTurbineController turbine = (TileTurbineController)be;
         if (!turbine.ticked) {
            turbine.tank.setCapacity(FutureGeneratorsConfig.SERVER.multiblockTurbineWaterCapacity.get());
            turbine.battery.setCapacity(FutureGeneratorsConfig.SERVER.multiblockTurbinePowerCapacity.get());
            if (!turbine.valid) {
               turbine.validate();
            }

            turbine.ticked = true;
         }

         int maxSpeed = FutureGeneratorsConfig.SERVER.multiblockTurbineMaxSpeed.get();
         int step = Math.max(1, maxSpeed / (FutureGeneratorsConfig.SERVER.multiblockTurbineSpinupTime.get() * 20));
         if (turbine.tank.getFluidAmount() > 0 && turbine.tank.getFluidAmount() >= turbine.speed) {
            turbine.tank.drain(turbine.speed);
            if (turbine.speed < maxSpeed) {
               turbine.speed = Math.min(maxSpeed, turbine.speed + step);
            }
         } else {
            turbine.speed = Math.max(0, turbine.speed - step);
         }

         turbine.battery.receiveEnergy(turbine.speed * FutureGeneratorsConfig.SERVER.multiblockTurbineFeRatio.get());
         turbine.ticks++;
         if (turbine.ticks % 6 == 0 || turbine.speed > 0) {
            turbine.eject();
         }
      }
   }

   public void validate() {
      this.valid = false;
      this.casings.forEach(casing -> casing.setController(null));
      this.casings.clear();

      for (Direction direction : Direction.values()) {
         if (direction != Direction.UP && direction != Direction.DOWN) {
            BlockPos first = this.worldPosition.relative(direction);
            BlockEntity be = this.level.getBlockEntity(first);
            if (be != null && be instanceof TileTurbineCasing firstCasing && firstCasing.controller == null) {
               BlockPos second = this.worldPosition.relative(direction.getClockWise());
               be = this.level.getBlockEntity(second);
               if (be != null && be instanceof TileTurbineCasing secondCasing && secondCasing.controller == null) {
                  BlockPos third = second.relative(direction);
                  be = this.level.getBlockEntity(third);
                  if (be != null && be instanceof TileTurbineCasing thirdCasing && thirdCasing.controller == null) {
                     ((ServerLevel)this.level)
                        .sendParticles(
                           ParticleTypes.FIREWORK,
                           this.worldPosition.getX() + this.level.getRandom().nextDouble(),
                           this.worldPosition.getY() + 1,
                           this.worldPosition.getZ() + this.level.getRandom().nextDouble(),
                           2,
                           0.3,
                           0.0,
                           0.3,
                           0.1
                        );
                     this.valid = true;
                     this.casings.add(firstCasing);
                     ((ServerLevel)this.level)
                        .sendParticles(
                           ParticleTypes.FIREWORK,
                           firstCasing.getBlockPos().getX() + this.level.getRandom().nextDouble(),
                           firstCasing.getBlockPos().getY() + 1,
                           firstCasing.getBlockPos().getZ() + this.level.getRandom().nextDouble(),
                           2,
                           0.3,
                           0.0,
                           0.3,
                           0.1
                        );
                     firstCasing.setController(this);
                     this.casings.add(secondCasing);
                     ((ServerLevel)this.level)
                        .sendParticles(
                           ParticleTypes.FIREWORK,
                           secondCasing.getBlockPos().getX() + this.level.getRandom().nextDouble(),
                           secondCasing.getBlockPos().getY() + 1,
                           secondCasing.getBlockPos().getZ() + this.level.getRandom().nextDouble(),
                           2,
                           0.3,
                           0.0,
                           0.3,
                           0.1
                        );
                     secondCasing.setController(this);
                     this.casings.add(thirdCasing);
                     ((ServerLevel)this.level)
                        .sendParticles(
                           ParticleTypes.FIREWORK,
                           thirdCasing.getBlockPos().getX() + this.level.getRandom().nextDouble(),
                           thirdCasing.getBlockPos().getY() + 1,
                           thirdCasing.getBlockPos().getZ() + this.level.getRandom().nextDouble(),
                           2,
                           0.3,
                           0.0,
                           0.3,
                           0.1
                        );
                     thirdCasing.setController(this);
                     return;
                  }
               }
            }
         }
      }
   }

   /** Pushes power out of the controller and its casings into the neighbours that aren't part of a turbine. */
   public void eject() {
      java.util.function.Predicate<BlockPos> turbinePart = target -> this.level.getBlockEntity(target) instanceof TileTurbineController
         || this.level.getBlockEntity(target) instanceof TileTurbineCasing;
      TransferUtils.pushEnergy(this.level, this.getBlockPos(), this.battery, turbinePart);
      // Once formed, the casings push power out of their own sides as well.
      if (this.valid) {
         for (TileTurbineCasing casing : this.casings) {
            TransferUtils.pushEnergy(this.level, casing.getBlockPos(), this.battery, turbinePart);
         }
      }
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

   /** Breaking the controller releases its casings. */
   @Override
   public void preRemoveSideEffects(BlockPos pos, BlockState state) {
      super.preRemoveSideEffects(pos, state);
      this.casings.forEach(casing -> casing.setController(null));
      this.casings.clear();
   }
}
