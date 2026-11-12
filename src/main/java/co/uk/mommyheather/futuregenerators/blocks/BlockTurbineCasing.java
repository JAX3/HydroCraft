package co.uk.mommyheather.futuregenerators.blocks;

import co.uk.mommyheather.futuregenerators.tile.TileTurbineCasing;
import co.uk.mommyheather.futuregenerators.tile.Tiles;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class BlockTurbineCasing extends Block implements EntityBlock {
   public BlockTurbineCasing(Properties p_49795_) {
      super(p_49795_);
   }

   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TileTurbineCasing(pos, state);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      return type == Tiles.turbineCasing.get() ? TileTurbineCasing::tick : null;
   }

   public void onRemove(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean p_51542_) {
      if (!state.is(oldState.getBlock())) {
         TileTurbineCasing casing = (TileTurbineCasing)level.getBlockEntity(pos);
         if (casing.controller != null) {
            casing.controller.valid = false;
         }
      }

      super.onRemove(state, level, pos, oldState, p_51542_);
   }
}
