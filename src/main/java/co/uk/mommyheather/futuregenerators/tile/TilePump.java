package co.uk.mommyheather.futuregenerators.tile;

import co.uk.mommyheather.futuregenerators.config.FutureGeneratorsConfig;
import co.uk.mommyheather.futuregenerators.util.FutureGeneratorsEnergyStorage;
import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.ForgeHooks;
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
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

public class TilePump extends BlockEntity {
   public FluidTank tank;
   public FutureGeneratorsEnergyStorage battery;
   public ItemStackHandler items;
   private LazyOptional<IFluidHandler> lazyTank;
   private LazyOptional<IEnergyStorage> lazyBattery;
   private LazyOptional<IItemHandler> lazyItems;
   private boolean ticked = false;
   private ArrayList<BlockPos> pumpedBlocks = new ArrayList<>();
   public Fluid lastPumped = Fluids.EMPTY;
   private int ticks = 0;
   private static final Direction[] customDirections = new Direction[]{
      Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN
   };

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
      this.lazyItems.invalidate();
   }

   public TilePump(BlockPos p_155229_, BlockState p_155230_) {
      super((BlockEntityType)Tiles.pump.get(), p_155229_, p_155230_);
      this.tank = new FluidTank(0) {
         protected void onContentsChanged() {
            TilePump.this.setChanged();
         }
      };
      this.battery = new FutureGeneratorsEnergyStorage(0) {
         @Override
         public void onContentsChanged() {
            TilePump.this.setChanged();
         }
      };
      this.items = new ItemStackHandler(1) {
         public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) > 0 && !stack.hasCraftingRemainingItem();
         }

         public void onContentsChanged(int slot) {
            TilePump.this.setChanged();
         }
      };
      this.lazyTank = LazyOptional.of(() -> this.tank);
      this.lazyBattery = LazyOptional.of(() -> this.battery);
      this.lazyItems = LazyOptional.of(() -> this.items);
   }

   public void setChanged() {
      super.setChanged();
      this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 2);
   }

   public static void tick(Level level, BlockPos pos, BlockState state, BlockEntity be) {
      TilePump pump = (TilePump)be;
      if (!pump.ticked) {
         pump.tank.setCapacity((Integer)FutureGeneratorsConfig.SERVER.fluidPumpCapacity.get());
         pump.battery.setCapacity((Integer)FutureGeneratorsConfig.SERVER.fluidPumpBattery.get());
         pump.ticked = true;
      }

      if (!level.isClientSide) {
         pump.ticks++;
         if (pump.ticks % (Integer)FutureGeneratorsConfig.SERVER.fluidPumpTime.get() == 0) {
            pump.handlePowerConversion();
            BlockState below = level.getBlockState(pos.below());
            FluidState fluidState = below.getFluidState();
            if (!(Boolean)FutureGeneratorsConfig.SERVER.drainWater.get() && fluidState.is(Fluids.WATER)) {
               pump.drain(pos.below(), false);
            } else {
               pump.drain();
            }
         }
      }
   }

   public void drain() {
      if (this.pumpedBlocks.isEmpty()) {
         this.lastPumped = Fluids.EMPTY;
         if (this.isDrainable(this.worldPosition.below()) && this.drain(this.worldPosition.below(), true)) {
            this.pumpedBlocks.add(this.worldPosition.below());
         }
      } else {
         for (int i = this.pumpedBlocks.size() - 1; i >= 0; i--) {
            BlockPos pos = this.pumpedBlocks.get(i);

            for (Direction direction : customDirections) {
               BlockPos pos2 = pos.relative(direction);
               if (this.isDrainable(pos2)) {
                  if (this.drain(pos2, true)) {
                     this.pumpedBlocks.add(pos2);
                  }

                  return;
               }
            }

            this.pumpedBlocks.remove(i);
         }
      }
   }

   public boolean drain(BlockPos pos, boolean consume) {
      if (!this.canDrain(this.level.getFluidState(pos))) {
         return false;
      }

      this.tank.fill(new FluidStack(this.level.getFluidState(pos).getType(), 1000), FluidAction.EXECUTE);
      this.battery.extractEnergy((Integer)FutureGeneratorsConfig.SERVER.fluidPumpConsumption.get(), false);
      this.lastPumped = this.level.getFluidState(pos).getType();
      if (consume) {
         this.level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
      }

      return consume;
   }

   public boolean isDrainable(BlockPos pos) {
      FluidState state = this.level.getFluidState(pos);
      if (state.isEmpty()) {
         return false;
      } else if (!state.is(this.lastPumped) && !this.lastPumped.isSame(Fluids.EMPTY)) {
         return false;
      } else if (!state.isSource()) {
         return false;
      } else {
         return this.worldPosition.distSqr(pos) > Math.pow(((Integer)FutureGeneratorsConfig.SERVER.fluidPumpRange.get()).intValue(), 2.0)
            ? false
            : pos.getY() > this.worldPosition.getY() - 3;
      }
   }

   public boolean canDrain(FluidState state) {
      if (!this.tank.isEmpty() && !state.is(this.tank.getFluid().getFluid())) {
         return false;
      } else {
         return this.tank.getFluidAmount() + 1000 > this.tank.getCapacity()
            ? false
            : this.battery.getEnergyStored() - (Integer)FutureGeneratorsConfig.SERVER.fluidPumpConsumption.get() >= 0;
      }
   }

   public void handlePowerConversion() {
      if (!this.items.getStackInSlot(0).isEmpty()) {
         int toInsert = (int)(
            ForgeHooks.getBurnTime(this.items.getStackInSlot(0).copyWithCount(1), RecipeType.SMELTING)
               * (Double)FutureGeneratorsConfig.SERVER.fluidPumpConversion.get()
         );
         if (this.battery.getMaxEnergyStored() - this.battery.getEnergyStored() >= toInsert) {
            this.battery.receiveEnergy(toInsert, false);
            this.items.extractItem(0, 1, false);
         }
      }
   }

   protected void saveAdditional(CompoundTag tag) {
      super.saveAdditional(tag);
      tag.put("tank", this.tank.writeToNBT(new CompoundTag()));
      tag.put("battery", this.battery.serializeNBT());
      tag.put("items", this.items.serializeNBT());
      ListTag blocks = new ListTag();

      for (BlockPos blockPos : this.pumpedBlocks) {
         blocks.add(NbtUtils.writeBlockPos(blockPos));
      }

      tag.put("pumpedCache", blocks);
      tag.putString("fluid", ForgeRegistries.FLUIDS.getKey(this.lastPumped).toString());
   }

   public void load(CompoundTag tag) {
      super.load(tag);
      if (tag.contains("tank")) {
         this.tank.readFromNBT(tag.getCompound("tank"));
      }

      if (tag.contains("battery")) {
         this.battery.deserializeNBT(tag.get("battery"));
      }

      if (tag.contains("items")) {
         this.items.deserializeNBT(tag.getCompound("items"));
      }

      if (tag.contains("pumpedCache")) {
         ListTag blocks = tag.getList("pumpedCache", 10);

         for (int i = 0; i < blocks.size(); i++) {
            this.pumpedBlocks.add(NbtUtils.readBlockPos(blocks.getCompound(i)));
         }
      }

      if (tag.contains("fluid")) {
         this.lastPumped = (Fluid)ForgeRegistries.FLUIDS.getValue(new ResourceLocation(tag.getString("fluid")));
      }
   }

   public CompoundTag getUpdateTag() {
      CompoundTag tag = new CompoundTag();
      tag.put("tank", this.tank.writeToNBT(new CompoundTag()));
      tag.put("battery", this.battery.serializeNBT());
      tag.putString("fluid", ForgeRegistries.FLUIDS.getKey(this.lastPumped).toString());
      return tag;
   }

   public void handleUpdateTag(CompoundTag tag) {
      if (tag.contains("tank")) {
         this.tank.readFromNBT(tag.getCompound("tank"));
      }

      if (tag.contains("battery")) {
         this.battery.deserializeNBT(tag.get("battery"));
      }

      if (tag.contains("fluid")) {
         this.lastPumped = (Fluid)ForgeRegistries.FLUIDS.getValue(new ResourceLocation(tag.getString("fluid")));
      }
   }

   public Packet<ClientGamePacketListener> getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }
}
