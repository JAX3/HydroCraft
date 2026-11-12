package co.uk.mommyheather.futuregenerators.tile;

import co.uk.mommyheather.futuregenerators.blocks.Blocks;
import co.uk.mommyheather.futuregenerators.config.FutureGeneratorsConfig;
import co.uk.mommyheather.futuregenerators.util.FutureGeneratorsEnergyStorage;
import co.uk.mommyheather.futuregenerators.util.TransferUtils;
import java.util.HashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TileLightningDynamo extends BlockEntity {
   public FutureGeneratorsEnergyStorage battery;
   private boolean ticked = false;
   private int ticks = 0;
   public HashMap<Direction, TileLightningGenerator> generators = new HashMap<>();

   public TileLightningDynamo(BlockPos pos, BlockState state) {
      super(Tiles.lightningDynamo.get(), pos, state);
      this.battery = new FutureGeneratorsEnergyStorage(0, 0, Integer.MAX_VALUE, this::setChanged);
   }

   public static void tick(Level level, BlockPos pos, BlockState state, BlockEntity be) {
      if (!level.isClientSide()) {
         TileLightningDynamo dynamo = (TileLightningDynamo)be;
         if (!dynamo.ticked) {
            dynamo.ticked = true;
            dynamo.battery.setCapacity(FutureGeneratorsConfig.SERVER.lightningDynamoCapacity.get());
            dynamo.checkNeighbours();
         }

         int i = 0;

         for (Direction direction : Direction.values()) {
            TileLightningGenerator generator = dynamo.generators.get(direction);
            if (generator != null && generator.isRunning()) {
               if (++i > FutureGeneratorsConfig.SERVER.lightningDynamoMaxGenerators.get()) {
                  break;
               }

               dynamo.battery.receiveEnergy(FutureGeneratorsConfig.SERVER.lightningDynamoProduction.get());
            }
         }

         dynamo.ticks++;
         if (dynamo.ticks % 6 == 0) {
            dynamo.eject();
         }
      }
   }

   @Override
   protected void saveAdditional(ValueOutput output) {
      super.saveAdditional(output);
      this.battery.serialize(output.child("battery"));
   }

   @Override
   protected void loadAdditional(ValueInput input) {
      super.loadAdditional(input);
      input.child("battery").ifPresent(this.battery::deserialize);
   }

   public void eject() {
      TransferUtils.pushEnergy(this.level, this.getBlockPos(), this.battery, target -> false);
   }

   public void checkNeighbours() {
      for (Direction direction : Direction.values()) {
         if (direction.getAxis() != Axis.Y) {
            BlockState generator = this.level.getBlockState(this.worldPosition.relative(direction));
            if (generator.is(Blocks.lightningGenerator.get())
               && this.level.getBlockEntity(this.worldPosition.relative(direction)) instanceof TileLightningGenerator tile) {
               this.generators.put(direction, tile);
            } else {
               this.generators.put(direction, null);
            }
         }
      }
   }
}
