package co.uk.mommyheather.futuregenerators.blocks;

import co.uk.mommyheather.futuregenerators.tile.TileLightningDynamo;
import co.uk.mommyheather.futuregenerators.tile.Tiles;
import co.uk.mommyheather.futuregenerators.ui.LightningDynamoMenu;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

public class BlockLightningDynamo extends Block implements EntityBlock {
   public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

   public BlockLightningDynamo(Properties p_49795_) {
      super(p_49795_);
      this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH));
   }

   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TileLightningDynamo(pos, state);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      return type == Tiles.lightningDynamo.get() ? TileLightningDynamo::tick : null;
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> state) {
      state.add(new Property[]{FACING});
   }

   public BlockState getStateForPlacement(BlockPlaceContext p_196258_1_) {
      return (BlockState)this.defaultBlockState().setValue(FACING, p_196258_1_.getHorizontalDirection().getOpposite());
   }

   public InteractionResult use(BlockState state, Level level, final BlockPos pos, Player player, InteractionHand hand, BlockHitResult trace) {
      if (!level.isClientSide) {
         BlockEntity be = level.getBlockEntity(pos);
         if (be instanceof TileLightningDynamo) {
            MenuProvider containerProvider = new MenuProvider() {
               public Component getDisplayName() {
                  return Component.translatable("futuregenerators.ui.lightning_dynamo");
               }

               public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity) {
                  return new LightningDynamoMenu(windowId, playerEntity, pos);
               }
            };
            NetworkHooks.openScreen((ServerPlayer)player, containerProvider, be.getBlockPos());
         }
      }

      return InteractionResult.SUCCESS;
   }

   @Deprecated
   public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos p_60513_, boolean p_60514_) {
      if (!level.isClientSide) {
         Optional<TileLightningDynamo> dynamoOptional = level.getBlockEntity(pos, (BlockEntityType)Tiles.lightningDynamo.get());
         if (dynamoOptional.isPresent()) {
            TileLightningDynamo dynamo = dynamoOptional.get();
            dynamo.checkNeighbours();
         }
      }
   }
}
