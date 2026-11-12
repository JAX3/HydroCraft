package co.uk.mommyheather.futuregenerators.blocks;

import co.uk.mommyheather.futuregenerators.tile.TileTurbineCasing;
import co.uk.mommyheather.futuregenerators.tile.Tiles;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/** Part of the multiblock turbine; breaking one un-forms the turbine (see {@link TileTurbineCasing#preRemoveSideEffects}). */
public class BlockTurbineCasing extends Block implements EntityBlock {
   public BlockTurbineCasing(Properties properties) {
      super(properties);
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TileTurbineCasing(pos, state);
   }

   @Override
   public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      return type == Tiles.turbineCasing.get() ? TileTurbineCasing::tick : null;
   }
}
