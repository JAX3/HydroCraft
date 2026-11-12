package co.uk.mommyheather.futuregenerators.tile;

import co.uk.mommyheather.futuregenerators.config.FutureGeneratorsConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class TileFluidTank extends BlockEntity {
   public FluidTank tank;
   private LazyOptional<IFluidHandler> lazyTank;
   private boolean ticked = false;

   public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
      return cap == ForgeCapabilities.FLUID_HANDLER ? this.lazyTank.cast() : super.getCapability(cap, side);
   }

   public void invalidateCaps() {
      super.invalidateCaps();
      this.lazyTank.invalidate();
   }

   public TileFluidTank(BlockPos p_155229_, BlockState p_155230_) {
      super((BlockEntityType)Tiles.fluidTank.get(), p_155229_, p_155230_);
      this.tank = new FluidTank(0) {
         protected void onContentsChanged() {
            TileFluidTank.this.setChanged();
         }
      };
      this.lazyTank = LazyOptional.of(() -> this.tank);
   }

   public void setChanged() {
      super.setChanged();
      this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 2);
   }

   public static void tick(Level level, BlockPos pos, BlockState state, BlockEntity be) {
      TileFluidTank tank = (TileFluidTank)be;
      if (!tank.ticked) {
         tank.tank.setCapacity((Integer)FutureGeneratorsConfig.SERVER.fluidTankCapacity.get());
         tank.ticked = true;
      }
   }

   protected void saveAdditional(CompoundTag tag) {
      super.saveAdditional(tag);
      tag.put("tank", this.tank.writeToNBT(new CompoundTag()));
   }

   public void load(CompoundTag tag) {
      super.load(tag);
      if (tag.contains("tank")) {
         this.tank.readFromNBT(tag.getCompound("tank"));
      }
   }

   public CompoundTag getUpdateTag() {
      CompoundTag tag = new CompoundTag();
      tag.put("tank", this.tank.writeToNBT(new CompoundTag()));
      return tag;
   }

   public void handleUpdateTag(CompoundTag tag) {
      if (tag.contains("tank")) {
         this.tank.readFromNBT(tag.getCompound("tank"));
      }
   }

   public Packet<ClientGamePacketListener> getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }
}
