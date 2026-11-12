package co.uk.mommyheather.futuregenerators.tile;

import co.uk.mommyheather.futuregenerators.config.FutureGeneratorsConfig;
import co.uk.mommyheather.futuregenerators.util.FluidTank;
import co.uk.mommyheather.futuregenerators.util.ItemStorage;
import co.uk.mommyheather.futuregenerators.util.TransferUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class TileBoiler extends BlockEntity {
   public TileBoiler.DoubleTank tank;
   public ItemStorage items;
   private boolean ticked = false;
   private int ticks = 0;
   public int fuelRemaining = 0;

   public TileBoiler(BlockPos pos, BlockState state) {
      super(Tiles.boiler.get(), pos, state);
      this.tank = new TileBoiler.DoubleTank(0, this::setChanged);
      this.items = new ItemStorage(1, (slot, stack) -> TransferUtils.isFuel(stack), this::setChanged);
   }

   public static void tick(Level level, BlockPos pos, BlockState state, BlockEntity be) {
      if (!level.isClientSide()) {
         TileBoiler boiler = (TileBoiler)be;
         if (!boiler.ticked) {
            boiler.tank.setCapacity(FutureGeneratorsConfig.SERVER.boilerWaterCapacity.get(), 2);
            boiler.ticked = true;
         }

         boiler.ticks++;
         if (boiler.fuelRemaining <= 0) {
            if (boiler.tank.inTank.getFluidAmount() > 0 && boiler.tank.outTank.getFluidAmount() < boiler.tank.outTank.getCapacity()) {
               ItemStack stack = boiler.items.extractItem(0, 1);
               boiler.fuelRemaining = TransferUtils.burnTime(level, boiler, stack);
            }
         } else {
            boiler.fuelRemaining--;
            int rate = FutureGeneratorsConfig.SERVER.boilerHeatingRate.get();
            if (boiler.tank.inTank.getFluidAmount() >= rate) {
               boiler.tank.inTank.drain(rate);
               boiler.tank.outTank.fill(FluidResource.of(co.uk.mommyheather.futuregenerators.fluids.Fluids.HOT_WATER.get()), rate);
            }
         }

         if (boiler.ticks % 6 == 0 || boiler.fuelRemaining > 0) {
            boiler.eject();
         }
      }
   }

   @Override
   protected void saveAdditional(ValueOutput output) {
      super.saveAdditional(output);
      ValueOutput tanks = output.child("tank");
      this.tank.inTank.serialize(tanks.child("in"));
      this.tank.outTank.serialize(tanks.child("out"));
      this.items.serialize(output.child("items"));
      output.putInt("burnTime", this.fuelRemaining);
   }

   @Override
   protected void loadAdditional(ValueInput input) {
      super.loadAdditional(input);
      input.child("tank").ifPresent(tanks -> {
         tanks.child("in").ifPresent(this.tank.inTank::deserialize);
         tanks.child("out").ifPresent(this.tank.outTank::deserialize);
      });
      input.child("items").ifPresent(this.items::deserialize);
      this.fuelRemaining = input.getIntOr("burnTime", 0);
   }

   /** Pushes hot water out to the neighbours. */
   public void eject() {
      TransferUtils.pushFluid(this.level, this.getBlockPos(), this.tank.outTank);
   }

   /**
    * The boiler's two tanks as one handler: water goes in to slot 0 ({@link #inTank}), hot water comes out of slot 1
    * ({@link #outTank}), which holds twice as much.
    */
   public static class DoubleTank implements ResourceHandler<FluidResource> {
      public final FluidTank inTank;
      public final FluidTank outTank;

      public DoubleTank(int capacity, Runnable onChanged) {
         this.inTank = new FluidTank(capacity, fluid -> fluid.is(Fluids.WATER), onChanged);
         this.outTank = new FluidTank(capacity, onChanged);
      }

      private FluidTank tank(int index) {
         return index == 0 ? this.inTank : this.outTank;
      }

      @Override
      public int size() {
         return 2;
      }

      @Override
      public FluidResource getResource(int index) {
         return tank(index).getResource(0);
      }

      @Override
      public long getAmountAsLong(int index) {
         return tank(index).getAmountAsLong(0);
      }

      @Override
      public long getCapacityAsLong(int index, FluidResource resource) {
         return tank(index).getCapacity();
      }

      @Override
      public boolean isValid(int index, FluidResource resource) {
         return tank(index).isValid(0, resource);
      }

      @Override
      public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
         return index == 0 ? this.inTank.insert(0, resource, amount, transaction) : 0;
      }

      @Override
      public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
         return index == 1 ? this.outTank.extract(0, resource, amount, transaction) : 0;
      }

      public void setCapacity(int capacity, int outMult) {
         this.inTank.setCapacity(capacity);
         this.outTank.setCapacity(capacity * outMult);
      }
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
