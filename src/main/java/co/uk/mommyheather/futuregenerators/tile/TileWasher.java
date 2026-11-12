package co.uk.mommyheather.futuregenerators.tile;

import co.uk.mommyheather.futuregenerators.config.FutureGeneratorsConfig;
import co.uk.mommyheather.futuregenerators.fluids.Fluids;
import co.uk.mommyheather.futuregenerators.util.FluidTank;
import co.uk.mommyheather.futuregenerators.util.FutureGeneratorsEnergyStorage;
import co.uk.mommyheather.futuregenerators.util.ItemStorage;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

/** Washes cobblestone (slots 0-2) with hot water and power into a chance of ores and nuggets (slots 3-12). */
public class TileWasher extends BlockEntity {
   public FluidTank tank;
   public FutureGeneratorsEnergyStorage battery;
   public ItemStorage items;
   private boolean ticked = false;
   private int ticks = 0;
   private static final Map<Item, Float> drops = new LinkedHashMap<>();

   public TileWasher(BlockPos pos, BlockState state) {
      super(Tiles.washer.get(), pos, state);
      this.tank = new FluidTank(0, fluid -> fluid.is(Fluids.HOT_WATER.get()), this::setChanged);
      this.battery = new FutureGeneratorsEnergyStorage(0, this::setChanged);
      this.items = new ItemStorage(13, (slot, stack) -> stack.is(Items.COBBLESTONE) && slot < 3, slot -> slot >= 3, this::setChanged);
   }

   public static void tick(Level level, BlockPos pos, BlockState state, BlockEntity be) {
      if (!level.isClientSide()) {
         TileWasher washer = (TileWasher)be;
         if (!washer.ticked) {
            washer.tank.setCapacity(FutureGeneratorsConfig.SERVER.washerWaterCapacity.get());
            washer.battery.setCapacity(FutureGeneratorsConfig.SERVER.washerPowerCapacity.get());
            washer.ticked = true;
         }

         washer.ticks++;
         if (washer.canProcess() && washer.ticks % FutureGeneratorsConfig.SERVER.washerProcessTime.get() == 0) {
            for (int i = 0; i < 3; i++) {
               if (!washer.items.getStackInSlot(i).isEmpty()) {
                  if (!washer.canProcess()) {
                     return;
                  }

                  washer.items.extractItem(i, 1);
                  washer.tank.drain(FutureGeneratorsConfig.SERVER.washerWaterConsumption.get());
                  washer.battery.extractEnergy(FutureGeneratorsConfig.SERVER.washerPowerConsumption.get());

                  for (Map.Entry<Item, Float> drop : drops.entrySet()) {
                     if (drop.getValue() >= level.getRandom().nextFloat()) {
                        washer.addOutput(new ItemStack(drop.getKey()));
                     }
                  }
               }
            }
         }
      }
   }

   /** Puts one item in the first output slot with room for it (the first empty one, or a stack of the same item). */
   private void addOutput(ItemStack insert) {
      for (int j = 3; j < this.items.getSlots(); j++) {
         ItemStack slot = this.items.getStackInSlot(j);
         if (slot.isEmpty()) {
            this.items.setStackInSlot(j, insert);
            return;
         }

         if (ItemStack.isSameItem(insert, slot) && slot.getCount() < 64) {
            this.items.setStackInSlot(j, slot.copyWithCount(slot.getCount() + 1));
            return;
         }
      }
   }

   @Override
   protected void saveAdditional(ValueOutput output) {
      super.saveAdditional(output);
      this.tank.serialize(output.child("tank"));
      this.battery.serialize(output.child("battery"));
      this.items.serialize(output.child("items"));
   }

   @Override
   protected void loadAdditional(ValueInput input) {
      super.loadAdditional(input);
      input.child("tank").ifPresent(this.tank::deserialize);
      if (!this.tank.isEmpty() && !this.tank.getFluidResource().is(Fluids.HOT_WATER.get())) {
         this.tank.setFluid(FluidResource.of(Fluids.HOT_WATER.get()), this.tank.getFluidAmount());
      }

      input.child("battery").ifPresent(this.battery::deserialize);
      input.child("items").ifPresent(this.items::deserialize);
   }

   public boolean canProcess() {
      return this.battery.getEnergyStored() >= FutureGeneratorsConfig.SERVER.washerPowerConsumption.get()
         && this.tank.getFluidAmount() >= FutureGeneratorsConfig.SERVER.washerWaterConsumption.get()
         && (this.items.getStackInSlot(0).getCount() > 0 || this.items.getStackInSlot(1).getCount() > 0 || this.items.getStackInSlot(2).getCount() > 0);
   }

   static {
      drops.put(Items.IRON_NUGGET, 0.15F);
      drops.put(Items.GOLD_NUGGET, 0.15F);
      drops.put(Items.IRON_ORE, 0.1F);
      drops.put(Items.COPPER_ORE, 0.01F);
      drops.put(Items.GOLD_ORE, 0.1F);
      drops.put(Items.QUARTZ, 0.05F);
      drops.put(Items.LAPIS_ORE, 0.05F);
      drops.put(Items.REDSTONE_ORE, 0.05F);
      drops.put(Items.ENDER_PEARL, 0.015F);
      drops.put(Items.DIAMOND_ORE, 0.01F);
      drops.put(Items.NETHERITE_SCRAP, 0.001F);
   }

   /** Drops the machine's items when it is broken. */
   @Override
   public void preRemoveSideEffects(BlockPos pos, BlockState state) {
      super.preRemoveSideEffects(pos, state);
      if (this.level != null) {
         for (int i = 0; i < this.items.getSlots(); i++) {
            if (!this.items.getStackInSlot(i).isEmpty()) {
               Containers.dropItemStack(this.level, pos.getX(), pos.getY(), pos.getZ(), this.items.getStackInSlot(i));
            }
         }
      }
   }
}
