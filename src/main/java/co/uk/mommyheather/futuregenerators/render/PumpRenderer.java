package co.uk.mommyheather.futuregenerators.render;

import co.uk.mommyheather.futuregenerators.config.FutureGeneratorsConfig;
import co.uk.mommyheather.futuregenerators.tile.TilePump;
import net.neoforged.neoforge.fluids.FluidStack;

public class PumpRenderer extends FluidBlockRenderer<TilePump> {
   @Override
   protected FluidStack fluid(TilePump be) {
      return be.tank.getFluid();
   }

   @Override
   protected int capacity(TilePump be) {
      return FutureGeneratorsConfig.SERVER.fluidPumpCapacity.get();
   }
}
