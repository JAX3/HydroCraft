package co.uk.mommyheather.futuregenerators.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/** Part of the multiblock turbine. Once formed, it hands out its controller's tank and battery (see {@code Capabilities}). */
public class TileTurbineCasing extends BlockEntity {
   public @Nullable TileTurbineController controller;
   private boolean ticked = false;

   public TileTurbineCasing(BlockPos pos, BlockState state) {
      super(Tiles.turbineCasing.get(), pos, state);
   }

   /** The casing's capabilities change when it joins or leaves a turbine. */
   public void setController(@Nullable TileTurbineController controller) {
      this.controller = controller;
      if (this.level != null) this.level.invalidateCapabilities(this.worldPosition);
   }

   public static void tick(Level level, BlockPos pos, BlockState state, BlockEntity be) {
      if (!level.isClientSide()) {
         TileTurbineCasing casing = (TileTurbineCasing)be;
         if (!casing.ticked) {
            casing.ticked = true;

            for (int x = pos.getX() - 1; x < pos.getX() + 2; x++) {
               for (int z = pos.getZ() - 1; z < pos.getZ() + 2; z++) {
                  if (x != pos.getX() || z != pos.getZ()) {
                     if (casing.controller != null) {
                        return;
                     }

                     BlockPos pos2 = new BlockPos(x, pos.getY(), z);
                     if (level.getBlockEntity(pos2) instanceof TileTurbineController controller && !controller.valid) {
                        controller.validate();
                     }
                  }
               }
            }
         }
      }
   }

   /** Breaking a casing un-forms its turbine. */
   @Override
   public void preRemoveSideEffects(BlockPos pos, BlockState state) {
      super.preRemoveSideEffects(pos, state);
      if (this.controller != null) {
         this.controller.valid = false;
      }
   }
}
