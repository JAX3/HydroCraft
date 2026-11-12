package co.uk.mommyheather.futuregenerators.blocks;

import co.uk.mommyheather.futuregenerators.tile.TilePump;
import co.uk.mommyheather.futuregenerators.tile.Tiles;
import co.uk.mommyheather.futuregenerators.ui.PumpMenu;
import co.uk.mommyheather.futuregenerators.util.TransferUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class BlockPump extends Block implements EntityBlock {
   public BlockPump(Properties properties) {
      super(properties);
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TilePump(pos, state);
   }

   @Override
   public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      return type == Tiles.pump.get() ? TilePump::tick : null;
   }

   /** Buckets and other fluid containers fill or empty the tank (unless sneaking); anything else opens the GUI. */
   @Override
   protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult trace) {
      if (player.isShiftKeyDown() || !(level.getBlockEntity(pos) instanceof TilePump tile)) {
         return InteractionResult.TRY_WITH_EMPTY_HAND;
      }

      if (level.isClientSide()) {
         return TransferUtils.isFluidContainer(stack) ? InteractionResult.SUCCESS : InteractionResult.TRY_WITH_EMPTY_HAND;
      }

      if (TransferUtils.handleFluidInteraction(player, hand, stack, pos, tile.tank)) {
         player.getInventory().setChanged();
         return InteractionResult.SUCCESS;
      }

      return InteractionResult.TRY_WITH_EMPTY_HAND;
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult trace) {
      if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TilePump) {
         player.openMenu(new SimpleMenuProvider((windowId, inventory, p) -> new PumpMenu(windowId, p, pos), Component.translatable("futuregenerators.ui.pump")), pos);
      }

      return InteractionResult.SUCCESS;
   }
}
