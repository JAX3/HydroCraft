package co.uk.mommyheather.futuregenerators.ui;

import co.uk.mommyheather.futuregenerators.blocks.Blocks;
import co.uk.mommyheather.futuregenerators.tile.TileLightningGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.items.SlotItemHandler;

public class LightningGeneratorMenu extends FutureGeneratorsMenu {
   public int power;
   public int powerMax;
   public int dynamos;
   public boolean hasRod;

   public LightningGeneratorMenu(int windowId, Player player, BlockPos pos) {
      super((MenuType<?>)Menus.lightningGenerator.get(), windowId, player, pos, (Block)Blocks.lightningGenerator.get());
      if (player.level().getBlockEntity(pos) instanceof TileLightningGenerator generator) {
         this.SLOT_COUNT = 6;
         this.SLOT_INPUT = 6;
         this.addSlot(new SlotItemHandler(generator.items, 0, 97, 17));
         this.addSlot(new SlotItemHandler(generator.items, 1, 115, 17));
         this.addSlot(new SlotItemHandler(generator.items, 2, 97, 35));
         this.addSlot(new SlotItemHandler(generator.items, 3, 115, 35));
         this.addSlot(new SlotItemHandler(generator.items, 4, 97, 53));
         this.addSlot(new SlotItemHandler(generator.items, 5, 115, 53));
         this.layoutPlayerInventorySlots(player.getInventory(), 8, 84);
         this.addDataSlot(new DataSlot() {
            public int get() {
               return generator.battery.getEnergyStored() & 65535;
            }

            public void set(int pValue) {
               LightningGeneratorMenu.this.power = LightningGeneratorMenu.this.power & -65536 | pValue & 65535;
            }
         });
         this.addDataSlot(new DataSlot() {
            public int get() {
               return generator.battery.getEnergyStored() >> 16 & 65535;
            }

            public void set(int pValue) {
               LightningGeneratorMenu.this.power = LightningGeneratorMenu.this.power & 65535 | (pValue & 65535) << 16;
            }
         });
         this.addDataSlot(new DataSlot() {
            public int get() {
               return generator.battery.getMaxEnergyStored() & 65535;
            }

            public void set(int pValue) {
               LightningGeneratorMenu.this.powerMax = LightningGeneratorMenu.this.powerMax & -65536 | pValue & 65535;
            }
         });
         this.addDataSlot(new DataSlot() {
            public int get() {
               return generator.battery.getMaxEnergyStored() >> 16 & 65535;
            }

            public void set(int pValue) {
               LightningGeneratorMenu.this.powerMax = LightningGeneratorMenu.this.powerMax & 65535 | (pValue & 65535) << 16;
            }
         });
         this.addDataSlot(new DataSlot() {
            public int get() {
               return generator.hasRod ? 1 : 0;
            }

            public void set(int pValue) {
               LightningGeneratorMenu.this.hasRod = pValue == 1;
            }
         });
         this.addDataSlot(new DataSlot() {
            public int get() {
               return generator.dynamos;
            }

            public void set(int pValue) {
               LightningGeneratorMenu.this.dynamos = pValue;
            }
         });
      }
   }
}
