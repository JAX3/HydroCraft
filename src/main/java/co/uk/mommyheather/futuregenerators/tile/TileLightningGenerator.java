package co.uk.mommyheather.futuregenerators.tile;

import co.uk.mommyheather.futuregenerators.config.FutureGeneratorsConfig;
import co.uk.mommyheather.futuregenerators.items.Items;
import co.uk.mommyheather.futuregenerators.util.FutureGeneratorsEnergyStorage;
import co.uk.mommyheather.futuregenerators.util.ItemStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TileLightningGenerator extends BlockEntity {
   public FutureGeneratorsEnergyStorage battery;
   public ItemStorage items;
   private boolean ticked = false;
   private int ticks = 0;
   public boolean hasRod = false;
   public int dynamos = 0;

   public TileLightningGenerator(BlockPos pos, BlockState state) {
      super(Tiles.lightningGenerator.get(), pos, state);
      this.items = new ItemStorage(6, (slot, stack) -> stack.is(Items.lightningCharge.get()), this::setChanged);
      this.battery = new FutureGeneratorsEnergyStorage(9, Integer.MAX_VALUE, 0, this::setChanged);
   }

   public static void tick(Level level, BlockPos pos, BlockState state, BlockEntity be) {
      if (!level.isClientSide()) {
         TileLightningGenerator generator = (TileLightningGenerator)be;
         if (!generator.ticked) {
            generator.battery.setCapacity(FutureGeneratorsConfig.SERVER.lightningGeneratorCapacity.get());
            generator.checkNeighbours();
            generator.ticked = true;
         }

         int fuelSlot = generator.fuelSlot();
         if (fuelSlot >= 0 && generator.hasRod && generator.battery.getEnergyStored() >= FutureGeneratorsConfig.SERVER.lightningGeneratorConsumption.get()) {
            generator.ticks++;
            generator.battery.extractEnergy(FutureGeneratorsConfig.SERVER.lightningGeneratorConsumption.get());
            if (generator.ticks % 20 == 0) {
               // The charge wears down by one point a second and is used up when it breaks.
               ItemStack fuel = generator.items.getStackInSlot(fuelSlot).copy();
               fuel.setDamageValue(fuel.getDamageValue() + 1);
               generator.items.setStackInSlot(fuelSlot, fuel.getDamageValue() >= fuel.getMaxDamage() ? ItemStack.EMPTY : fuel);
            }

            ((ServerLevel)level)
               .sendParticles(
                  ParticleTypes.FIREWORK,
                  pos.getX() + level.getRandom().nextDouble(),
                  pos.getY() + 1,
                  pos.getZ() + level.getRandom().nextDouble(),
                  2,
                  0.3,
                  0.0,
                  0.3,
                  0.1
               );
            generator.setChanged();
         }
      }
   }

   /** The first slot holding a charge, or -1. */
   private int fuelSlot() {
      for (int i = 0; i < 6; i++) {
         if (!this.items.getStackInSlot(i).isEmpty()) {
            return i;
         }
      }

      return -1;
   }

   @Override
   protected void saveAdditional(ValueOutput output) {
      super.saveAdditional(output);
      this.items.serialize(output.child("items"));
      this.battery.serialize(output.child("battery"));
   }

   @Override
   protected void loadAdditional(ValueInput input) {
      super.loadAdditional(input);
      input.child("items").ifPresent(this.items::deserialize);
      input.child("battery").ifPresent(this.battery::deserialize);
   }

   public void checkNeighbours() {
      this.dynamos = 0;
      BlockState rod = this.level.getBlockState(this.worldPosition.above());
      this.hasRod = rod.is(BlockTags.LIGHTNING_RODS);

      for (Direction direction : Direction.values()) {
         if (direction.getAxis() != Axis.Y) {
            BlockState dynamo = this.level.getBlockState(this.worldPosition.relative(direction));
            if (dynamo.is((Block)co.uk.mommyheather.futuregenerators.blocks.Blocks.lightningDynamo.get())) {
               this.dynamos++;
            }
         }
      }
   }

   public boolean isRunning() {
      return this.fuelSlot() >= 0 && this.hasRod && this.battery.getEnergyStored() >= FutureGeneratorsConfig.SERVER.lightningGeneratorConsumption.get();
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
