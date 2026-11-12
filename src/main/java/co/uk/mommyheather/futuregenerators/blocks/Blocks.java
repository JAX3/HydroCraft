package co.uk.mommyheather.futuregenerators.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class Blocks {
   public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, "futuregenerators");
   public static final RegistryObject<Block> turbine = BLOCKS.register(
      "turbine", () -> new BlockTurbine(Properties.of().mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(2.5F))
   );
   public static final RegistryObject<Block> turbineCasing = BLOCKS.register(
      "turbine_casing", () -> new BlockTurbineCasing(Properties.of().mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(2.5F))
   );
   public static final RegistryObject<Block> turbineController = BLOCKS.register(
      "turbine_controller", () -> new BlockTurbineController(Properties.of().mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(2.5F))
   );
   public static final RegistryObject<Block> lightningGenerator = BLOCKS.register(
      "lightning_generator", () -> new BlockLightningGenerator(Properties.of().mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(2.5F))
   );
   public static final RegistryObject<Block> lightningDynamo = BLOCKS.register(
      "lightning_dynamo", () -> new BlockLightningDynamo(Properties.of().mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(2.5F))
   );
   public static final RegistryObject<Block> washer = BLOCKS.register(
      "washer", () -> new BlockWasher(Properties.of().mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(2.5F))
   );
   public static final RegistryObject<Block> boiler = BLOCKS.register(
      "boiler", () -> new BlockBoiler(Properties.of().mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(2.5F))
   );
   public static final RegistryObject<Block> fluidTank = BLOCKS.register(
      "tank", () -> new BlockFluidTank(Properties.of().mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(2.5F).noOcclusion())
   );
   public static final RegistryObject<Block> pump = BLOCKS.register(
      "pump", () -> new BlockPump(Properties.of().mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(2.5F).noOcclusion())
   );
   public static final RegistryObject<Block> fluidPipe = BLOCKS.register(
      "fluid_pipe", () -> new BlockFluidPipe(Properties.of().mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(2.5F).noOcclusion())
   );
}
