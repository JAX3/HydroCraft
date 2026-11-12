package co.uk.mommyheather.futuregenerators.blocks;

import co.uk.mommyheather.futuregenerators.tile.TileLightningGenerator;
import co.uk.mommyheather.futuregenerators.tile.Tiles;
import co.uk.mommyheather.futuregenerators.ui.LightningGeneratorMenu;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class BlockLightningGenerator extends Block implements EntityBlock {
   public BlockLightningGenerator(Properties properties) {
      super(properties);
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TileLightningGenerator(pos, state);
   }

   @Override
   public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      return type == Tiles.lightningGenerator.get() ? TileLightningGenerator::tick : null;
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult trace) {
      if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TileLightningGenerator) {
         player.openMenu(new SimpleMenuProvider((windowId, inventory, p) -> new LightningGeneratorMenu(windowId, p, pos), Component.translatable("futuregenerators.ui.lightning_generator")), pos);
      }

      return InteractionResult.SUCCESS;
   }

   /** Re-checks the lightning rod and the neighbouring dynamos when something next to it changes. */
   @Override
   protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
      if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TileLightningGenerator tile) {
         tile.checkNeighbours();
      }
   }
}
