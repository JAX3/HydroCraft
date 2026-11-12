package co.uk.mommyheather.futuregenerators.ui;

import co.uk.mommyheather.futuregenerators.blocks.Blocks;
import co.uk.mommyheather.futuregenerators.tile.TileLightningDynamo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;

public class LightningDynamoMenu extends FutureGeneratorsMenu {
   public int power;
   public int powerMax;
   public int generators;

   public LightningDynamoMenu(int windowId, Player player, BlockPos pos) {
      super((MenuType<?>)Menus.lightningDynamo.get(), windowId, player, pos, (Block)Blocks.lightningDynamo.get());
      if (player.level().getBlockEntity(pos) instanceof TileLightningDynamo dynamo) {
         this.layoutPlayerInventorySlots(player.getInventory(), 8, 84);
         this.addDataSlot(new DataSlot() {
            public int get() {
               return dynamo.battery.getEnergyStored() & 65535;
            }

            public void set(int pValue) {
               LightningDynamoMenu.this.power = LightningDynamoMenu.this.power & -65536 | pValue & 65535;
            }
         });
         this.addDataSlot(new DataSlot() {
            public int get() {
               return dynamo.battery.getEnergyStored() >> 16 & 65535;
            }

            public void set(int pValue) {
               LightningDynamoMenu.this.power = LightningDynamoMenu.this.power & 65535 | (pValue & 65535) << 16;
            }
         });
         this.addDataSlot(new DataSlot() {
            public int get() {
               return dynamo.battery.getMaxEnergyStored() & 65535;
            }

            public void set(int pValue) {
               LightningDynamoMenu.this.powerMax = LightningDynamoMenu.this.powerMax & -65536 | pValue & 65535;
            }
         });
         this.addDataSlot(new DataSlot() {
            public int get() {
               return dynamo.battery.getMaxEnergyStored() >> 16 & 65535;
            }

            public void set(int pValue) {
               LightningDynamoMenu.this.powerMax = LightningDynamoMenu.this.powerMax & 65535 | (pValue & 65535) << 16;
            }
         });
         this.addDataSlot(new DataSlot() {
            public int get() {
               int i = 0;

               for (Direction direction : Direction.values()) {
                  if (dynamo.generators.get(direction) != null) {
                     i++;
                  }
               }

               return i;
            }

            public void set(int pValue) {
               LightningDynamoMenu.this.generators = pValue;
            }
         });
      }
   }
}
