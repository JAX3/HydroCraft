package co.uk.mommyheather.futuregenerators.tile;

import co.uk.mommyheather.futuregenerators.config.FutureGeneratorsConfig;
import co.uk.mommyheather.futuregenerators.items.Items;
import co.uk.mommyheather.futuregenerators.util.FutureGeneratorsEnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class TileLightningGenerator extends BlockEntity {
   public FutureGeneratorsEnergyStorage battery;
   public ItemStackHandler items;
   private boolean ticked = false;
   private LazyOptional<IEnergyStorage> lazyBattery;
   private LazyOptional<IItemHandler> lazyItems;
   private int ticks = 0;
   public boolean hasRod = false;
   public int dynamos = 0;

   public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
      if (cap == ForgeCapabilities.ITEM_HANDLER) {
         return this.lazyItems.cast();
      } else {
         return cap == ForgeCapabilities.ENERGY ? this.lazyBattery.cast() : super.getCapability(cap, side);
      }
   }

   public void invalidateCaps() {
      super.invalidateCaps();
      this.lazyItems.invalidate();
      this.lazyBattery.invalidate();
   }

   public TileLightningGenerator(BlockPos p_155229_, BlockState p_155230_) {
      super((BlockEntityType)Tiles.lightningGenerator.get(), p_155229_, p_155230_);
      this.items = new ItemStackHandler(6) {
         public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return stack.is((Item)Items.lightningCharge.get());
         }

         public void onContentsChanged(int slot) {
            TileLightningGenerator.this.setChanged();
         }
      };
      this.battery = new FutureGeneratorsEnergyStorage(9, Integer.MAX_VALUE, 0) {
         @Override
         public void onContentsChanged() {
            TileLightningGenerator.this.setChanged();
         }
      };
      this.lazyBattery = LazyOptional.of(() -> this.battery);
      this.lazyItems = LazyOptional.of(() -> this.items);
   }

   public static void tick(Level level, BlockPos pos, BlockState state, BlockEntity be) {
      if (!level.isClientSide) {
         TileLightningGenerator generator = (TileLightningGenerator)be;
         ItemStack fuel = ItemStack.EMPTY;
         if (!generator.ticked) {
            generator.battery.setCapacity((Integer)FutureGeneratorsConfig.SERVER.lightningGeneratorCapacity.get());
            generator.checkNeighbours();
            generator.ticked = true;
         }

         for (int i = 0; i < 6; i++) {
            fuel = generator.items.getStackInSlot(i);
            if (!fuel.isEmpty()) {
               break;
            }
         }

         if (!fuel.isEmpty()
            && generator.hasRod
            && generator.battery.getEnergyStored() >= (Integer)FutureGeneratorsConfig.SERVER.lightningGeneratorConsumption.get()) {
            generator.ticks++;
            generator.battery.setEnergy(generator.battery.getEnergyStored() - (Integer)FutureGeneratorsConfig.SERVER.lightningGeneratorConsumption.get());
            if (generator.ticks % 20 == 0 && fuel.hurt(1, level.random, null)) {
               fuel.setCount(0);
            }

            ((ServerLevel)level)
               .sendParticles(
                  ParticleTypes.FIREWORK,
                  pos.getX() + level.random.nextDouble(),
                  pos.getY() + 1,
                  pos.getZ() + level.random.nextDouble(),
                  2,
                  0.3,
                  0.0,
                  0.3,
                  0.1
               );
            generator.setChanged();
         }
      }
   }

   protected void saveAdditional(CompoundTag tag) {
      super.saveAdditional(tag);
      tag.put("items", this.items.serializeNBT());
      tag.put("battery", this.battery.serializeNBT());
   }

   public void load(CompoundTag tag) {
      super.load(tag);
      if (tag.contains("items")) {
         this.items.deserializeNBT(tag.getCompound("items"));
      }

      if (tag.contains("battery")) {
         this.battery.deserializeNBT(tag.get("battery"));
      }
   }

   public void checkNeighbours() {
      this.dynamos = 0;
      BlockState rod = this.level.getBlockState(this.worldPosition.above());
      this.hasRod = rod.is(Blocks.LIGHTNING_ROD);

      for (Direction direction : Direction.values()) {
         if (direction.getAxis() != Axis.Y) {
            BlockState dynamo = this.level.getBlockState(this.worldPosition.relative(direction));
            if (dynamo.is((Block)co.uk.mommyheather.futuregenerators.blocks.Blocks.lightningDynamo.get())) {
               this.dynamos++;
            }
         }
      }
   }

   public boolean isRunning() {
      ItemStack fuel = ItemStack.EMPTY;

      for (int i = 0; i < 6; i++) {
         fuel = this.items.getStackInSlot(i);
         if (!fuel.isEmpty()) {
            break;
         }
      }

      return !fuel.isEmpty() && this.hasRod && this.battery.getEnergyStored() >= (Integer)FutureGeneratorsConfig.SERVER.lightningGeneratorConsumption.get();
   }
}
