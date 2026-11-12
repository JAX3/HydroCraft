package co.uk.mommyheather.futuregenerators.blocks;

import co.uk.mommyheather.futuregenerators.FutureGenerators;
import java.util.function.Function;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class Blocks {
   public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(FutureGenerators.MODID);
   public static final DeferredBlock<BlockTurbine> turbine = machine("turbine", BlockTurbine::new, false);
   public static final DeferredBlock<BlockTurbineCasing> turbineCasing = machine("turbine_casing", BlockTurbineCasing::new, false);
   public static final DeferredBlock<BlockTurbineController> turbineController = machine("turbine_controller", BlockTurbineController::new, false);
   public static final DeferredBlock<BlockLightningGenerator> lightningGenerator = machine("lightning_generator", BlockLightningGenerator::new, false);
   public static final DeferredBlock<BlockLightningDynamo> lightningDynamo = machine("lightning_dynamo", BlockLightningDynamo::new, false);
   public static final DeferredBlock<BlockWasher> washer = machine("washer", BlockWasher::new, false);
   public static final DeferredBlock<BlockBoiler> boiler = machine("boiler", BlockBoiler::new, false);
   public static final DeferredBlock<BlockFluidTank> fluidTank = machine("tank", BlockFluidTank::new, true);
   public static final DeferredBlock<BlockPump> pump = machine("pump", BlockPump::new, true);
   public static final DeferredBlock<BlockFluidPipe> fluidPipe = machine("fluid_pipe", BlockFluidPipe::new, true);

   private static <B extends Block> DeferredBlock<B> machine(String name, Function<Properties, B> factory, boolean noOcclusion) {
      return BLOCKS.registerBlock(name, factory, p -> {
         p = p.mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(2.5F);
         return noOcclusion ? p.noOcclusion() : p;
      });
   }
}
