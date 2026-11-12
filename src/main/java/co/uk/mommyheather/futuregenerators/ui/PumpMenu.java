package co.uk.mommyheather.futuregenerators.ui;

import co.uk.mommyheather.futuregenerators.blocks.Blocks;
import co.uk.mommyheather.futuregenerators.tile.TilePump;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

public class PumpMenu extends FutureGeneratorsMenu {
   public TilePump pump;

   public PumpMenu(int windowId, Player player, BlockPos pos) {
      super((MenuType<?>)Menus.pump.get(), windowId, player, pos, (Block)Blocks.pump.get());
      BlockEntity be = player.level().getBlockEntity(pos);
      if (be instanceof TilePump) {
         this.pump = (TilePump)be;
         this.addSlot(new ResourceHandlerSlot(this.pump.items, this.pump.items::set, 0, 35, 38));
      }

      this.layoutPlayerInventorySlots(player.getInventory(), 8, 84);
   }
}
