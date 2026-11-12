package co.uk.mommyheather.futuregenerators.ui;

import co.uk.mommyheather.futuregenerators.blocks.Blocks;
import co.uk.mommyheather.futuregenerators.config.FutureGeneratorsConfig;
import co.uk.mommyheather.futuregenerators.tile.TileBoiler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

public class BoilerMenu extends FutureGeneratorsMenu {
   public int timeRemaining;
   public int water;
   public int hotWater;
   public int waterMax;

   public BoilerMenu(int windowId, Player player, BlockPos pos) {
      super((MenuType<?>)Menus.boiler.get(), windowId, player, pos, (Block)Blocks.boiler.get());
      BlockEntity be = player.level().getBlockEntity(pos);
      this.SLOT_COUNT = 1;
      this.SLOT_INPUT = 1;
      if (be instanceof TileBoiler boiler) {
         this.addSlot(new ResourceHandlerSlot(boiler.items, boiler.items::set, 0, 80, 53));
         this.layoutPlayerInventorySlots(player.getInventory(), 8, 84);
         this.addDataSlot(new DataSlot() {
            public int get() {
               return boiler.fuelRemaining / 20 & 65535;
            }

            public void set(int pValue) {
               BoilerMenu.this.timeRemaining = BoilerMenu.this.timeRemaining & -65536 | pValue & 65535;
            }
         });
         this.addDataSlot(new DataSlot() {
            public int get() {
               return boiler.fuelRemaining / 20 >> 16 & 65535;
            }

            public void set(int pValue) {
               BoilerMenu.this.timeRemaining = BoilerMenu.this.timeRemaining & 65535 | (pValue & 65535) << 16;
            }
         });
         this.addDataSlot(new DataSlot() {
            public int get() {
               return boiler.tank.inTank.getFluidAmount() & 65535;
            }

            public void set(int pValue) {
               BoilerMenu.this.water = BoilerMenu.this.water & -65536 | pValue & 65535;
            }
         });
         this.addDataSlot(new DataSlot() {
            public int get() {
               return boiler.tank.inTank.getFluidAmount() >> 16 & 65535;
            }

            public void set(int pValue) {
               BoilerMenu.this.water = BoilerMenu.this.water & 65535 | (pValue & 65535) << 16;
            }
         });
         this.addDataSlot(new DataSlot() {
            public int get() {
               return boiler.tank.outTank.getFluidAmount() & 65535;
            }

            public void set(int pValue) {
               BoilerMenu.this.hotWater = BoilerMenu.this.hotWater & -65536 | pValue & 65535;
            }
         });
         this.addDataSlot(new DataSlot() {
            public int get() {
               return boiler.tank.outTank.getFluidAmount() >> 16 & 65535;
            }

            public void set(int pValue) {
               BoilerMenu.this.hotWater = BoilerMenu.this.hotWater & 65535 | (pValue & 65535) << 16;
            }
         });
         this.addDataSlot(new DataSlot() {
            public int get() {
               return (Integer)FutureGeneratorsConfig.SERVER.boilerWaterCapacity.get() & 65535;
            }

            public void set(int pValue) {
               BoilerMenu.this.waterMax = BoilerMenu.this.waterMax & -65536 | pValue & 65535;
            }
         });
         this.addDataSlot(new DataSlot() {
            public int get() {
               return (Integer)FutureGeneratorsConfig.SERVER.boilerWaterCapacity.get() >> 16 & 65535;
            }

            public void set(int pValue) {
               BoilerMenu.this.waterMax = BoilerMenu.this.waterMax & 65535 | (pValue & 65535) << 16;
            }
         });
      }
   }
}
