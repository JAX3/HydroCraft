package co.uk.mommyheather.futuregenerators.tile;

import co.uk.mommyheather.futuregenerators.config.FutureGeneratorsConfig;
import co.uk.mommyheather.futuregenerators.fluids.Fluids;
import co.uk.mommyheather.futuregenerators.util.FutureGeneratorsEnergyStorage;
import java.util.HashMap;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class TileWasher extends BlockEntity {
   public FluidTank tank;
   public FutureGeneratorsEnergyStorage battery;
   public ItemStackHandler items;
   private boolean ticked = false;
   private LazyOptional<IFluidHandler> lazyTank;
   private LazyOptional<IEnergyStorage> lazyBattery;
   private LazyOptional<IItemHandler> lazyItems;
   private int ticks = 0;
   private static HashMap<ItemStack, Float> drops = new HashMap<>();

   public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
      if (cap == ForgeCapabilities.ITEM_HANDLER) {
         return this.lazyItems.cast();
      } else if (cap == ForgeCapabilities.FLUID_HANDLER) {
         return this.lazyTank.cast();
      } else {
         return cap == ForgeCapabilities.ENERGY ? this.lazyBattery.cast() : super.getCapability(cap, side);
      }
   }

   public void invalidateCaps() {
      super.invalidateCaps();
      this.lazyTank.invalidate();
      this.lazyBattery.invalidate();
   }

   public TileWasher(BlockPos p_155229_, BlockState p_155230_) {
      super((BlockEntityType)Tiles.washer.get(), p_155229_, p_155230_);
      this.tank = new FluidTank(0, stack -> stack.getFluid().isSame((Fluid)Fluids.HOT_WATER.get())) {
         protected void onContentsChanged() {
            TileWasher.this.setChanged();
         }
      };
      this.battery = new FutureGeneratorsEnergyStorage(0) {
         @Override
         public void onContentsChanged() {
            TileWasher.this.setChanged();
         }
      };
      this.items = new ItemStackHandler(13) {
         public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return stack.is(Items.COBBLESTONE) && slot < 3;
         }

         public void onContentsChanged(int slot) {
            TileWasher.this.setChanged();
         }

         @NotNull
         public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return slot < 3 ? ItemStack.EMPTY : super.extractItem(slot, amount, simulate);
         }
      };
      this.lazyTank = LazyOptional.of(() -> this.tank);
      this.lazyBattery = LazyOptional.of(() -> this.battery);
      this.lazyItems = LazyOptional.of(() -> this.items);
   }

   public static void tick(Level level, BlockPos pos, BlockState state, BlockEntity be) {
      if (!level.isClientSide) {
         TileWasher washer = (TileWasher)be;
         if (!washer.ticked) {
            washer.tank.setCapacity((Integer)FutureGeneratorsConfig.SERVER.washerWaterCapacity.get());
            washer.battery.setCapacity((Integer)FutureGeneratorsConfig.SERVER.washerPowerCapacity.get());
            washer.ticked = true;
         }

         washer.ticks++;
         if (washer.canProcess() && washer.ticks % (Integer)FutureGeneratorsConfig.SERVER.washerProcessTime.get() == 0) {
            for (int i = 0; i < 3; i++) {
               if (!washer.items.getStackInSlot(i).isEmpty()) {
                  if (!washer.canProcess()) {
                     return;
                  }

                  washer.items.getStackInSlot(i).setCount(washer.items.getStackInSlot(i).getCount() - 1);
                  washer.tank.drain((Integer)FutureGeneratorsConfig.SERVER.washerWaterConsumption.get(), FluidAction.EXECUTE);
                  washer.battery.setEnergy(washer.battery.getEnergyStored() - (Integer)FutureGeneratorsConfig.SERVER.washerPowerConsumption.get());

                  for (ItemStack stack : drops.keySet()) {
                     float f = drops.get(stack);
                     float c = level.random.nextFloat();
                     if (f >= c) {
                        ItemStack insert = stack.copy();

                        for (int j = 3; j < washer.items.getSlots(); j++) {
                           ItemStack slot = washer.items.getStackInSlot(j);
                           if (slot.isEmpty()) {
                              washer.items.setStackInSlot(j, insert);
                              break;
                           }

                           if (ItemStack.isSameItem(insert, slot) && slot.getCount() < 64) {
                              slot.setCount(slot.getCount() + 1);
                              break;
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   protected void saveAdditional(CompoundTag tag) {
      super.saveAdditional(tag);
      tag.put("tank", this.tank.writeToNBT(new CompoundTag()));
      tag.put("battery", this.battery.serializeNBT());
      tag.put("items", this.items.serializeNBT());
   }

   public void load(CompoundTag tag) {
      super.load(tag);
      if (tag.contains("tank")) {
         this.tank.readFromNBT(tag.getCompound("tank"));
      }

      if (!this.tank.getFluid().getFluid().isSame((Fluid)Fluids.HOT_WATER.get())) {
         this.tank.setFluid(new FluidStack((Fluid)Fluids.HOT_WATER.get(), this.tank.getFluidAmount()));
      }

      if (tag.contains("battery")) {
         this.battery.deserializeNBT(tag.get("battery"));
      }

      if (tag.contains("items")) {
         this.items.deserializeNBT(tag.getCompound("items"));
      }
   }

   public boolean canProcess() {
      return this.battery.getEnergyStored() >= (Integer)FutureGeneratorsConfig.SERVER.washerPowerConsumption.get()
         && this.tank.getFluidAmount() >= (Integer)FutureGeneratorsConfig.SERVER.washerWaterConsumption.get()
         && (this.items.getStackInSlot(0).getCount() > 0 || this.items.getStackInSlot(1).getCount() > 0 || this.items.getStackInSlot(2).getCount() > 0);
   }

   static {
      drops.put(new ItemStack(Items.IRON_NUGGET), 0.15F);
      drops.put(new ItemStack(Items.GOLD_NUGGET), 0.15F);
      drops.put(new ItemStack(Items.IRON_ORE), 0.1F);
      drops.put(new ItemStack(Items.COPPER_ORE), 0.01F);
      drops.put(new ItemStack(Items.GOLD_ORE), 0.1F);
      drops.put(new ItemStack(Items.QUARTZ), 0.05F);
      drops.put(new ItemStack(Items.LAPIS_ORE), 0.05F);
      drops.put(new ItemStack(Items.REDSTONE_ORE), 0.05F);
      drops.put(new ItemStack(Items.ENDER_PEARL), 0.015F);
      drops.put(new ItemStack(Items.DIAMOND_ORE), 0.01F);
      drops.put(new ItemStack(Items.NETHERITE_SCRAP), 0.001F);
   }
}
