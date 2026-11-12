package co.uk.mommyheather.futuregenerators.tile;

import co.uk.mommyheather.futuregenerators.config.FutureGeneratorsConfig;
import co.uk.mommyheather.futuregenerators.util.FluidTank;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class TileFluidTank extends BlockEntity {
   public FluidTank tank;
   private boolean ticked = false;

   public TileFluidTank(BlockPos pos, BlockState state) {
      super(Tiles.fluidTank.get(), pos, state);
      this.tank = new FluidTank(0, this::setChanged);
   }

   /** Every change is sent to clients, which draw the fluid and show it in the GUI. */
   @Override
   public void setChanged() {
      super.setChanged();
      if (this.level != null && !this.level.isClientSide()) {
         this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 2);
      }
   }

   public static void tick(Level level, BlockPos pos, BlockState state, BlockEntity be) {
      TileFluidTank tank = (TileFluidTank)be;
      if (!tank.ticked) {
         tank.tank.setCapacity(FutureGeneratorsConfig.SERVER.fluidTankCapacity.get());
         tank.ticked = true;
      }
   }

   @Override
   protected void saveAdditional(ValueOutput output) {
      super.saveAdditional(output);
      this.tank.serialize(output.child("tank"));
   }

   @Override
   protected void loadAdditional(ValueInput input) {
      super.loadAdditional(input);
      input.child("tank").ifPresent(this.tank::deserialize);
   }

   @Override
   public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
      return this.saveCustomOnly(registries);
   }

   @Override
   public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }
}
