package co.uk.mommyheather.futuregenerators.ui;

import co.uk.mommyheather.futuregenerators.blocks.Blocks;
import co.uk.mommyheather.futuregenerators.tile.TileWasher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

public class WasherMenu extends FutureGeneratorsMenu {
   public int water;
   public int waterMax;
   public int power;
   public int powerMax;

   public WasherMenu(int windowId, Player player, BlockPos pos) {
      super((MenuType<?>)Menus.washer.get(), windowId, player, pos, (Block)Blocks.washer.get());
      BlockEntity be = player.level().getBlockEntity(pos);
      this.SLOT_COUNT = 13;
      this.SLOT_INPUT = 3;
      if (be instanceof TileWasher washer) {
         this.addSlot(new SlotItemHandler(washer.items, 0, 62, 15));
         this.addSlot(new SlotItemHandler(washer.items, 1, 80, 15));
         this.addSlot(new SlotItemHandler(washer.items, 2, 98, 15));
         this.addSlot(new SlotItemHandler(washer.items, 3, 44, 44));
         this.addSlot(new SlotItemHandler(washer.items, 4, 62, 44));
         this.addSlot(new SlotItemHandler(washer.items, 5, 80, 44));
         this.addSlot(new SlotItemHandler(washer.items, 6, 98, 44));
         this.addSlot(new SlotItemHandler(washer.items, 7, 116, 44));
         this.addSlot(new SlotItemHandler(washer.items, 8, 44, 62));
         this.addSlot(new SlotItemHandler(washer.items, 9, 62, 62));
         this.addSlot(new SlotItemHandler(washer.items, 10, 80, 62));
         this.addSlot(new SlotItemHandler(washer.items, 11, 98, 62));
         this.addSlot(new SlotItemHandler(washer.items, 12, 116, 62));
         this.layoutPlayerInventorySlots(player.getInventory(), 8, 84);
         this.addDataSlot(new DataSlot() {
            public int get() {
               return washer.battery.getEnergyStored() & 65535;
            }

            public void set(int pValue) {
               WasherMenu.this.power = WasherMenu.this.power & -65536 | pValue & 65535;
            }
         });
         this.addDataSlot(new DataSlot() {
            public int get() {
               return washer.battery.getEnergyStored() >> 16 & 65535;
            }

            public void set(int pValue) {
               WasherMenu.this.power = WasherMenu.this.power & 65535 | (pValue & 65535) << 16;
            }
         });
         this.addDataSlot(new DataSlot() {
            public int get() {
               return washer.tank.getFluidAmount() & 65535;
            }

            public void set(int pValue) {
               WasherMenu.this.water = WasherMenu.this.water & -65536 | pValue & 65535;
            }
         });
         this.addDataSlot(new DataSlot() {
            public int get() {
               return washer.tank.getFluidAmount() >> 16 & 65535;
            }

            public void set(int pValue) {
               WasherMenu.this.water = WasherMenu.this.water & 65535 | (pValue & 65535) << 16;
            }
         });
         this.addDataSlot(new DataSlot() {
            public int get() {
               return washer.battery.getMaxEnergyStored() & 65535;
            }

            public void set(int pValue) {
               WasherMenu.this.powerMax = WasherMenu.this.powerMax & -65536 | pValue & 65535;
            }
         });
         this.addDataSlot(new DataSlot() {
            public int get() {
               return washer.battery.getMaxEnergyStored() >> 16 & 65535;
            }

            public void set(int pValue) {
               WasherMenu.this.powerMax = WasherMenu.this.powerMax & 65535 | (pValue & 65535) << 16;
            }
         });
         this.addDataSlot(new DataSlot() {
            public int get() {
               return washer.tank.getCapacity() & 65535;
            }

            public void set(int pValue) {
               WasherMenu.this.waterMax = WasherMenu.this.waterMax & -65536 | pValue & 65535;
            }
         });
         this.addDataSlot(new DataSlot() {
            public int get() {
               return washer.tank.getCapacity() >> 16 & 65535;
            }

            public void set(int pValue) {
               WasherMenu.this.waterMax = WasherMenu.this.waterMax & 65535 | (pValue & 65535) << 16;
            }
         });
      }
   }
}
