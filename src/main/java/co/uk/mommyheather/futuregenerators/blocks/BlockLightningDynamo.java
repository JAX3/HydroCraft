package co.uk.mommyheather.futuregenerators.blocks;

import co.uk.mommyheather.futuregenerators.tile.TileLightningDynamo;
import co.uk.mommyheather.futuregenerators.tile.Tiles;
import co.uk.mommyheather.futuregenerators.ui.LightningDynamoMenu;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class BlockLightningDynamo extends Block implements EntityBlock {
   public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;

   public BlockLightningDynamo(Properties properties) {
      super(properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TileLightningDynamo(pos, state);
   }

   @Override
   public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      return type == Tiles.lightningDynamo.get() ? TileLightningDynamo::tick : null;
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(FACING);
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult trace) {
      if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TileLightningDynamo) {
         player.openMenu(new SimpleMenuProvider((windowId, inventory, p) -> new LightningDynamoMenu(windowId, p, pos), Component.translatable("futuregenerators.ui.lightning_dynamo")), pos);
      }

      return InteractionResult.SUCCESS;
   }

   /** Re-checks the neighbouring generators when something next to it changes. */
   @Override
   protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
      if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TileLightningDynamo tile) {
         tile.checkNeighbours();
      }
   }
}
