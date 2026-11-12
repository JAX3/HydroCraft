package co.uk.mommyheather.futuregenerators.tile;

import co.uk.mommyheather.futuregenerators.blocks.Blocks;
import co.uk.mommyheather.futuregenerators.config.FutureGeneratorsConfig;
import co.uk.mommyheather.futuregenerators.util.FutureGeneratorsEnergyStorage;
import java.util.HashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

public class TileLightningDynamo extends BlockEntity {
   public FutureGeneratorsEnergyStorage battery;
   private boolean ticked = false;
   private LazyOptional<IEnergyStorage> lazyBattery;
   private int ticks = 0;
   public HashMap<Direction, TileLightningGenerator> generators = new HashMap<>();

   public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
      return cap == ForgeCapabilities.ENERGY ? this.lazyBattery.cast() : super.getCapability(cap, side);
   }

   public void invalidateCaps() {
      super.invalidateCaps();
      this.lazyBattery.invalidate();
   }

   public TileLightningDynamo(BlockPos p_155229_, BlockState p_155230_) {
      super((BlockEntityType)Tiles.lightningDynamo.get(), p_155229_, p_155230_);
      this.battery = new FutureGeneratorsEnergyStorage(0, 0, Integer.MAX_VALUE) {
         @Override
         public void onContentsChanged() {
            TileLightningDynamo.this.setChanged();
         }
      };
      this.lazyBattery = LazyOptional.of(() -> this.battery);
   }

   public static void tick(Level level, BlockPos pos, BlockState state, BlockEntity be) {
      if (!level.isClientSide) {
         TileLightningDynamo dynamo = (TileLightningDynamo)be;
         if (!dynamo.ticked) {
            dynamo.ticked = true;
            dynamo.battery.setCapacity((Integer)FutureGeneratorsConfig.SERVER.lightningDynamoCapacity.get());
            dynamo.checkNeighbours();
         }

         int i = 0;

         for (Direction direction : Direction.values()) {
            if (dynamo.generators.get(direction) != null) {
               TileLightningGenerator generator = dynamo.generators.get(direction);
               if (generator.isRunning()) {
                  if (++i > (Integer)FutureGeneratorsConfig.SERVER.lightningDynamoMaxGenerators.get()) {
                     break;
                  }

                  dynamo.battery
                     .setEnergy(
                        Math.min(
                           (Integer)FutureGeneratorsConfig.SERVER.lightningDynamoCapacity.get(),
                           dynamo.battery.getEnergyStored() + (Integer)FutureGeneratorsConfig.SERVER.lightningDynamoProduction.get()
                        )
                     );
               }
            }
         }

         dynamo.ticks++;
         if (dynamo.ticks % 6 == 0) {
            dynamo.eject();
         }
      }
   }

   protected void saveAdditional(CompoundTag tag) {
      super.saveAdditional(tag);
      tag.put("battery", this.battery.serializeNBT());
   }

   public void load(CompoundTag tag) {
      super.load(tag);
      if (tag.contains("battery")) {
         this.battery.deserializeNBT(tag.get("battery"));
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

   public void checkNeighbours() {
      for (Direction direction : Direction.values()) {
         if (direction.getAxis() != Axis.Y) {
            BlockState generator = this.level.getBlockState(this.worldPosition.relative(direction));
            if (generator.is((Block)Blocks.lightningGenerator.get())) {
               this.generators.put(direction, (TileLightningGenerator)this.level.getBlockEntity(this.worldPosition.relative(direction)));
            } else {
               this.generators.put(direction, null);
            }
         }
      }
   }
}
