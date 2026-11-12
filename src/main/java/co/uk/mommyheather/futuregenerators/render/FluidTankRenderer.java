package co.uk.mommyheather.futuregenerators.render;

import co.uk.mommyheather.futuregenerators.config.FutureGeneratorsConfig;
import co.uk.mommyheather.futuregenerators.tile.TileFluidTank;
import net.neoforged.neoforge.fluids.FluidStack;

public class FluidTankRenderer extends FluidBlockRenderer<TileFluidTank> {
   @Override
   protected FluidStack fluid(TileFluidTank be) {
      return be.tank.getFluid();
   }

   @Override
   protected int capacity(TileFluidTank be) {
      return FutureGeneratorsConfig.SERVER.fluidTankCapacity.get();
   }
}
