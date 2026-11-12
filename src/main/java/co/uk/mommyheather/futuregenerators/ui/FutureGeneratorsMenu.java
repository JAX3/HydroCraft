package co.uk.mommyheather.futuregenerators.ui;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class FutureGeneratorsMenu extends AbstractContainerMenu {
   private final Block verificationBlock;
   private final BlockPos pos;
   protected int SLOT_COUNT;
   protected int SLOT_INPUT;

   public FutureGeneratorsMenu(MenuType<?> type, int windowId, Player player, BlockPos pos, Block verificationBlock) {
      super(type, windowId);
      this.pos = pos;
      this.verificationBlock = verificationBlock;
   }

   private int addSlotRange(Container playerInventory, int index, int x, int y, int amount, int dx) {
      for (int i = 0; i < amount; i++) {
         this.addSlot(new Slot(playerInventory, index, x, y));
         x += dx;
         index++;
      }

      return index;
   }

   private int addSlotBox(Container playerInventory, int index, int x, int y, int horAmount, int dx, int verAmount, int dy) {
      for (int j = 0; j < verAmount; j++) {
         index = this.addSlotRange(playerInventory, index, x, y, horAmount, dx);
         y += dy;
      }

      return index;
   }

   protected void layoutPlayerInventorySlots(Container playerInventory, int leftCol, int topRow) {
      this.addSlotBox(playerInventory, 9, leftCol, topRow, 9, 18, 3, 18);
      topRow += 58;
      this.addSlotRange(playerInventory, 0, leftCol, topRow, 9, 18);
   }

   public ItemStack quickMoveStack(Player player, int index) {
      ItemStack itemstack = ItemStack.EMPTY;
      Slot slot = (Slot)this.slots.get(index);
      if (slot.hasItem()) {
         ItemStack stack = slot.getItem();
         itemstack = stack.copy();
         if (index < this.SLOT_COUNT && !this.moveItemStackTo(stack, this.SLOT_COUNT, 36 + this.SLOT_COUNT, true)) {
            return ItemStack.EMPTY;
         }

         if (!this.moveItemStackTo(stack, 0, this.SLOT_INPUT, false)) {
            if (index < 27 + this.SLOT_COUNT) {
               if (!this.moveItemStackTo(stack, 27 + this.SLOT_COUNT, 36 + this.SLOT_COUNT, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (index < 36 + this.SLOT_COUNT && !this.moveItemStackTo(stack, this.SLOT_COUNT, 27 + this.SLOT_COUNT, false)) {
               return ItemStack.EMPTY;
            }
         }

         if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }

         if (stack.getCount() == itemstack.getCount()) {
            return ItemStack.EMPTY;
         }

         slot.onTake(player, stack);
      }

      return itemstack;
   }

   public boolean stillValid(Player player) {
      return stillValid(ContainerLevelAccess.create(player.level(), this.pos), player, this.verificationBlock);
   }
}
