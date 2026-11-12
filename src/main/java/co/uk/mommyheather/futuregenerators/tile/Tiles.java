package co.uk.mommyheather.futuregenerators.tile;

import co.uk.mommyheather.futuregenerators.blocks.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.Builder;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class Tiles {
   public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, "futuregenerators");
   public static final RegistryObject<BlockEntityType<TileTurbine>> turbine = TILES.register(
      "turbine", () -> Builder.of(TileTurbine::new, new Block[]{(Block)Blocks.turbine.get()}).build(null)
   );
   public static final RegistryObject<BlockEntityType<TileTurbineCasing>> turbineCasing = TILES.register(
      "turbine_casing", () -> Builder.of(TileTurbineCasing::new, new Block[]{(Block)Blocks.turbineCasing.get()}).build(null)
   );
   public static final RegistryObject<BlockEntityType<TileTurbineController>> turbineController = TILES.register(
      "turbine_controller", () -> Builder.of(TileTurbineController::new, new Block[]{(Block)Blocks.turbineController.get()}).build(null)
   );
   public static final RegistryObject<BlockEntityType<TileLightningGenerator>> lightningGenerator = TILES.register(
      "lightning_generator", () -> Builder.of(TileLightningGenerator::new, new Block[]{(Block)Blocks.lightningGenerator.get()}).build(null)
   );
   public static final RegistryObject<BlockEntityType<TileLightningDynamo>> lightningDynamo = TILES.register(
      "lightning_dynamo", () -> Builder.of(TileLightningDynamo::new, new Block[]{(Block)Blocks.lightningDynamo.get()}).build(null)
   );
   public static final RegistryObject<BlockEntityType<TileWasher>> washer = TILES.register(
      "washer", () -> Builder.of(TileWasher::new, new Block[]{(Block)Blocks.washer.get()}).build(null)
   );
   public static final RegistryObject<BlockEntityType<TileBoiler>> boiler = TILES.register(
      "boiler", () -> Builder.of(TileBoiler::new, new Block[]{(Block)Blocks.boiler.get()}).build(null)
   );
   public static final RegistryObject<BlockEntityType<TileFluidTank>> fluidTank = TILES.register(
      "tank", () -> Builder.of(TileFluidTank::new, new Block[]{(Block)Blocks.fluidTank.get()}).build(null)
   );
   public static final RegistryObject<BlockEntityType<TilePump>> pump = TILES.register(
      "pump", () -> Builder.of(TilePump::new, new Block[]{(Block)Blocks.pump.get()}).build(null)
   );
   public static final RegistryObject<BlockEntityType<TileFluidPipe>> fluidPipe = TILES.register(
      "fluid_pipe", () -> Builder.of(TileFluidPipe::new, new Block[]{(Block)Blocks.fluidPipe.get()}).build(null)
   );
}
