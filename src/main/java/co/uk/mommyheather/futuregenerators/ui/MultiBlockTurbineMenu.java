package co.uk.mommyheather.futuregenerators.ui;

import co.uk.mommyheather.futuregenerators.blocks.Blocks;
import co.uk.mommyheather.futuregenerators.config.FutureGeneratorsConfig;
import co.uk.mommyheather.futuregenerators.tile.TileTurbineController;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

public class MultiBlockTurbineMenu extends FutureGeneratorsMenu {
   public int water;
   public int waterMax;
   public int power;
   public int powerMax;
   public int speed;
   public int speedMax;

   public MultiBlockTurbineMenu(int windowId, Player player, BlockPos pos) {
      super((MenuType<?>)Menus.multiblockTurbine.get(), windowId, player, pos, (Block)Blocks.turbineController.get());
      BlockEntity be = player.level().getBlockEntity(pos);
      this.layoutPlayerInventorySlots(player.getInventory(), 8, 84);
      if (be instanceof TileTurbineController turbine) {
         this.addDataSlot(new DataSlot() {
            public int get() {
               return turbine.battery.getEnergyStored() & 65535;
            }

            public void set(int pValue) {
               MultiBlockTurbineMenu.this.power = MultiBlockTurbineMenu.this.power & -65536 | pValue & 65535;
            }
         });
         this.addDataSlot(new DataSlot() {
            public int get() {
               return turbine.battery.getEnergyStored() >> 16 & 65535;
            }

            public void set(int pValue) {
               MultiBlockTurbineMenu.this.power = MultiBlockTurbineMenu.this.power & 65535 | (pValue & 65535) << 16;
            }
         });
         this.addDataSlot(new DataSlot() {
            public int get() {
               return turbine.tank.getFluidAmount() & 65535;
            }

            public void set(int pValue) {
               MultiBlockTurbineMenu.this.water = MultiBlockTurbineMenu.this.water & -65536 | pValue & 65535;
            }
         });
         this.addDataSlot(new DataSlot() {
            public int get() {
               return turbine.tank.getFluidAmount() >> 16 & 65535;
            }

            public void set(int pValue) {
               MultiBlockTurbineMenu.this.water = MultiBlockTurbineMenu.this.water & 65535 | (pValue & 65535) << 16;
            }
         });
         this.addDataSlot(new DataSlot() {
            public int get() {
               return turbine.battery.getMaxEnergyStored() & 65535;
            }

            public void set(int pValue) {
               MultiBlockTurbineMenu.this.powerMax = MultiBlockTurbineMenu.this.powerMax & -65536 | pValue & 65535;
            }
         });
         this.addDataSlot(new DataSlot() {
            public int get() {
               return turbine.battery.getMaxEnergyStored() >> 16 & 65535;
            }

            public void set(int pValue) {
               MultiBlockTurbineMenu.this.powerMax = MultiBlockTurbineMenu.this.powerMax & 65535 | (pValue & 65535) << 16;
            }
         });
         this.addDataSlot(new DataSlot() {
            public int get() {
               return turbine.tank.getCapacity() & 65535;
            }

            public void set(int pValue) {
               MultiBlockTurbineMenu.this.waterMax = MultiBlockTurbineMenu.this.waterMax & -65536 | pValue & 65535;
            }
         });
         this.addDataSlot(new DataSlot() {
            public int get() {
               return turbine.tank.getCapacity() >> 16 & 65535;
            }

            public void set(int pValue) {
               MultiBlockTurbineMenu.this.waterMax = MultiBlockTurbineMenu.this.waterMax & 65535 | (pValue & 65535) << 16;
            }
         });
         this.addDataSlot(new DataSlot() {
            public int get() {
               return turbine.speed & 65535;
            }

            public void set(int pValue) {
               MultiBlockTurbineMenu.this.speed = MultiBlockTurbineMenu.this.speed & -65536 | pValue & 65535;
            }
         });
         this.addDataSlot(new DataSlot() {
            public int get() {
               return turbine.speed >> 16 & 65535;
            }

            public void set(int pValue) {
               MultiBlockTurbineMenu.this.speed = MultiBlockTurbineMenu.this.speed & 65535 | (pValue & 65535) << 16;
            }
         });
         this.addDataSlot(new DataSlot() {
            public int get() {
               return (Integer)FutureGeneratorsConfig.SERVER.multiblockTurbineMaxSpeed.get() & 65535;
            }

            public void set(int pValue) {
               MultiBlockTurbineMenu.this.speedMax = MultiBlockTurbineMenu.this.speedMax & -65536 | pValue & 65535;
            }
         });
         this.addDataSlot(new DataSlot() {
            public int get() {
               return (Integer)FutureGeneratorsConfig.SERVER.multiblockTurbineMaxSpeed.get() >> 16 & 65535;
            }

            public void set(int pValue) {
               MultiBlockTurbineMenu.this.speedMax = MultiBlockTurbineMenu.this.speedMax & 65535 | (pValue & 65535) << 16;
            }
         });
      }
   }
}
