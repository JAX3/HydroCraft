package co.uk.mommyheather.futuregenerators.tile;

import co.uk.mommyheather.futuregenerators.config.FutureGeneratorsConfig;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class TileBoiler extends BlockEntity {
   public TileBoiler.DoubleTank tank;
   public ItemStackHandler items;
   private boolean ticked = false;
   private LazyOptional<IFluidHandler> lazyTank;
   private LazyOptional<IItemHandler> lazyItems;
   private int ticks = 0;
   public int fuelRemaining = 0;

   public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
      if (cap == ForgeCapabilities.ITEM_HANDLER) {
         return this.lazyItems.cast();
      } else {
         return cap == ForgeCapabilities.FLUID_HANDLER ? this.lazyTank.cast() : super.getCapability(cap, side);
      }
   }

   public void invalidateCaps() {
      super.invalidateCaps();
      this.lazyTank.invalidate();
      this.lazyItems.invalidate();
   }

   public TileBoiler(BlockPos p_155229_, BlockState p_155230_) {
      super((BlockEntityType)Tiles.boiler.get(), p_155229_, p_155230_);
      this.tank = new TileBoiler.DoubleTank(0, stack -> stack.getFluid().isSame(Fluids.WATER)) {
         @Override
         protected void onContentsChanged() {
            TileBoiler.this.setChanged();
         }
      };
      this.items = new ItemStackHandler(1) {
         public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) > 0;
         }

         public void onContentsChanged(int slot) {
            TileBoiler.this.setChanged();
         }
      };
      this.lazyTank = LazyOptional.of(() -> this.tank);
      this.lazyItems = LazyOptional.of(() -> this.items);
   }

   public static void tick(Level level, BlockPos pos, BlockState state, BlockEntity be) {
      if (!level.isClientSide) {
         TileBoiler boiler = (TileBoiler)be;
         if (!boiler.ticked) {
            boiler.tank.setCapacity((Integer)FutureGeneratorsConfig.SERVER.boilerWaterCapacity.get(), 2);
            boiler.ticked = true;
         }

         boiler.ticks++;
         if (boiler.fuelRemaining <= 0) {
            if (boiler.tank.inTank.getFluidAmount() > 0 && boiler.tank.outTank.getFluidAmount() < boiler.tank.outTank.getCapacity()) {
               ItemStack stack = boiler.items.extractItem(0, 1, false);
               boiler.fuelRemaining = ForgeHooks.getBurnTime(stack, RecipeType.SMELTING);
            }
         } else {
            boiler.fuelRemaining--;
            if (boiler.tank.inTank.getFluidAmount() >= (Integer)FutureGeneratorsConfig.SERVER.boilerHeatingRate.get()) {
               boiler.tank.drainIntake((Integer)FutureGeneratorsConfig.SERVER.boilerHeatingRate.get());
               boiler.tank
                  .fillOutput(
                     new FluidStack(
                        (Fluid)co.uk.mommyheather.futuregenerators.fluids.Fluids.HOT_WATER.get(),
                        (Integer)FutureGeneratorsConfig.SERVER.boilerHeatingRate.get()
                     )
                  );
            }
         }

         if (boiler.ticks % 6 == 0 || boiler.fuelRemaining > 0) {
            boiler.eject();
         }
      }
   }

   protected void saveAdditional(CompoundTag tag) {
      super.saveAdditional(tag);
      tag.put("tank", this.tank.writeToNBT(new CompoundTag()));
      tag.put("items", this.items.serializeNBT());
      tag.putInt("burnTime", this.fuelRemaining);
   }

   public void load(CompoundTag tag) {
      super.load(tag);
      if (tag.contains("tank")) {
         this.tank.readFromNBT(tag.getCompound("tank"));
      }

      if (tag.contains("items")) {
         this.items.deserializeNBT(tag.getCompound("items"));
      }

      if (tag.contains("burnTime")) {
         this.fuelRemaining = tag.getInt("burnTime");
      }
   }

   public void eject() {
      for (Direction direction : new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN}) {
         if (this.tank.outTank.getFluidAmount() <= 0) {
            return;
         }

         BlockEntity target = this.level.getBlockEntity(this.getBlockPos().relative(direction));
         if (target != null) {
            LazyOptional<IFluidHandler> fluidOptional = target.getCapability(ForgeCapabilities.FLUID_HANDLER, direction.getOpposite());
            if (fluidOptional != null && fluidOptional.isPresent()) {
               IFluidHandler storage = (IFluidHandler)fluidOptional.resolve().get();
               int amount = storage.fill(this.tank.outTank.getFluid(), FluidAction.SIMULATE);
               if (amount != 0) {
                  amount = storage.fill(this.tank.outTank.getFluid(), FluidAction.EXECUTE);
                  this.tank.drain(amount, FluidAction.EXECUTE);
               }
            }
         }
      }
   }

   public static class DoubleTank implements IFluidHandler {
      private FluidTank inTank;
      private FluidTank outTank;

      public DoubleTank(int capacity, Predicate<FluidStack> validatorInsert) {
         this.inTank = new FluidTank(capacity, validatorInsert);
         this.outTank = new FluidTank(capacity);
      }

      public int getTanks() {
         return 2;
      }

      @NotNull
      public FluidStack getFluidInTank(int tank) {
         return tank == 0 ? this.inTank.getFluid() : this.outTank.getFluid();
      }

      public int getTankCapacity(int tank) {
         return this.inTank.getCapacity();
      }

      public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
         return tank == 0 ? this.inTank.isFluidValid(stack) : this.outTank.isFluidValid(stack);
      }

      public int fill(FluidStack resource, FluidAction action) {
         this.onContentsChanged();
         return this.inTank.fill(resource, action);
      }

      @NotNull
      public FluidStack drain(FluidStack resource, FluidAction action) {
         this.onContentsChanged();
         return this.outTank.drain(resource, action);
      }

      @NotNull
      public FluidStack drain(int maxDrain, FluidAction action) {
         this.onContentsChanged();
         return this.outTank.drain(maxDrain, action);
      }

      public void drainIntake(int toDrain) {
         this.onContentsChanged();
         this.inTank.drain(toDrain, FluidAction.EXECUTE);
      }

      public void fillOutput(FluidStack resource) {
         this.onContentsChanged();
         this.outTank.fill(resource, FluidAction.EXECUTE);
      }

      public void setCapacity(int capacity, int outMult) {
         this.inTank.setCapacity(capacity);
         this.outTank.setCapacity(capacity * outMult);
      }

      protected void onContentsChanged() {
      }

      public void readFromNBT(CompoundTag compound) {
         if (compound.contains("in")) {
            this.inTank.readFromNBT(compound.getCompound("in"));
         }

         if (compound.contains("out")) {
            this.outTank.readFromNBT(compound.getCompound("out"));
         }
      }

      public Tag writeToNBT(CompoundTag tag) {
         CompoundTag in = new CompoundTag();
         this.inTank.writeToNBT(in);
         tag.put("in", in);
         CompoundTag out = new CompoundTag();
         this.outTank.writeToNBT(out);
         tag.put("out", out);
         return tag;
      }
   }
}
