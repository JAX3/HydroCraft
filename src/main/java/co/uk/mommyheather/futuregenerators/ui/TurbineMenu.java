package co.uk.mommyheather.futuregenerators.ui;

import co.uk.mommyheather.futuregenerators.blocks.Blocks;
import co.uk.mommyheather.futuregenerators.config.FutureGeneratorsConfig;
import co.uk.mommyheather.futuregenerators.tile.TileTurbine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

public class TurbineMenu extends FutureGeneratorsMenu {
   public int water;
   public int waterMax;
   public int power;
   public int powerMax;
   public int speed;
   public int speedMax;

   public TurbineMenu(int windowId, Player player, BlockPos pos) {
      super((MenuType<?>)Menus.turbine.get(), windowId, player, pos, (Block)Blocks.turbine.get());
      BlockEntity be = player.level().getBlockEntity(pos);
      this.layoutPlayerInventorySlots(player.getInventory(), 8, 84);
      if (be instanceof TileTurbine turbine) {
         this.addDataSlot(new DataSlot() {
            public int get() {
               return turbine.battery.getEnergyStored() & 65535;
            }

            public void set(int pValue) {
               TurbineMenu.this.power = TurbineMenu.this.power & -65536 | pValue & 65535;
            }
         });
         this.addDataSlot(new DataSlot() {
            public int get() {
               return turbine.battery.getEnergyStored() >> 16 & 65535;
            }

            public void set(int pValue) {
               TurbineMenu.this.power = TurbineMenu.this.power & 65535 | (pValue & 65535) << 16;
            }
         });
         this.addDataSlot(new DataSlot() {
            public int get() {
               return turbine.tank.getFluidAmount() & 65535;
            }

            public void set(int pValue) {
               TurbineMenu.this.water = TurbineMenu.this.water & -65536 | pValue & 65535;
            }
         });
         this.addDataSlot(new DataSlot() {
            public int get() {
               return turbine.tank.getFluidAmount() >> 16 & 65535;
            }

            public void set(int pValue) {
               TurbineMenu.this.water = TurbineMenu.this.water & 65535 | (pValue & 65535) << 16;
            }
         });
         this.addDataSlot(new DataSlot() {
            public int get() {
               return turbine.battery.getMaxEnergyStored() & 65535;
            }

            public void set(int pValue) {
               TurbineMenu.this.powerMax = TurbineMenu.this.powerMax & -65536 | pValue & 65535;
            }
         });
         this.addDataSlot(new DataSlot() {
            public int get() {
               return turbine.battery.getMaxEnergyStored() >> 16 & 65535;
            }

            public void set(int pValue) {
               TurbineMenu.this.powerMax = TurbineMenu.this.powerMax & 65535 | (pValue & 65535) << 16;
            }
         });
         this.addDataSlot(new DataSlot() {
            public int get() {
               return turbine.tank.getCapacity() & 65535;
            }

            public void set(int pValue) {
               TurbineMenu.this.waterMax = TurbineMenu.this.waterMax & -65536 | pValue & 65535;
            }
         });
         this.addDataSlot(new DataSlot() {
            public int get() {
               return turbine.tank.getCapacity() >> 16 & 65535;
            }

            public void set(int pValue) {
               TurbineMenu.this.waterMax = TurbineMenu.this.waterMax & 65535 | (pValue & 65535) << 16;
            }
         });
         this.addDataSlot(new DataSlot() {
            public int get() {
               return turbine.speed & 65535;
            }

            public void set(int pValue) {
               TurbineMenu.this.speed = TurbineMenu.this.speed & -65536 | pValue & 65535;
            }
         });
         this.addDataSlot(new DataSlot() {
            public int get() {
               return turbine.speed >> 16 & 65535;
            }

            public void set(int pValue) {
               TurbineMenu.this.speed = TurbineMenu.this.speed & 65535 | (pValue & 65535) << 16;
            }
         });
         this.addDataSlot(new DataSlot() {
            public int get() {
               return (Integer)FutureGeneratorsConfig.SERVER.turbineMaxSpeed.get() & 65535;
            }

            public void set(int pValue) {
               TurbineMenu.this.speedMax = TurbineMenu.this.speedMax & -65536 | pValue & 65535;
            }
         });
         this.addDataSlot(new DataSlot() {
            public int get() {
               return (Integer)FutureGeneratorsConfig.SERVER.turbineMaxSpeed.get() >> 16 & 65535;
            }

            public void set(int pValue) {
               TurbineMenu.this.speedMax = TurbineMenu.this.speedMax & 65535 | (pValue & 65535) << 16;
            }
         });
      }
   }
}
