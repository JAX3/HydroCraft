package co.uk.mommyheather.futuregenerators.tile;

import co.uk.mommyheather.futuregenerators.FutureGenerators;
import co.uk.mommyheather.futuregenerators.blocks.Blocks;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

public class Tiles {
   public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, FutureGenerators.MODID);
   public static final Supplier<BlockEntityType<TileTurbine>> turbine = TILES.register(
      "turbine", () -> new BlockEntityType<>(TileTurbine::new, Blocks.turbine.get())
   );
   public static final Supplier<BlockEntityType<TileTurbineCasing>> turbineCasing = TILES.register(
      "turbine_casing", () -> new BlockEntityType<>(TileTurbineCasing::new, Blocks.turbineCasing.get())
   );
   public static final Supplier<BlockEntityType<TileTurbineController>> turbineController = TILES.register(
      "turbine_controller", () -> new BlockEntityType<>(TileTurbineController::new, Blocks.turbineController.get())
   );
   public static final Supplier<BlockEntityType<TileLightningGenerator>> lightningGenerator = TILES.register(
      "lightning_generator", () -> new BlockEntityType<>(TileLightningGenerator::new, Blocks.lightningGenerator.get())
   );
   public static final Supplier<BlockEntityType<TileLightningDynamo>> lightningDynamo = TILES.register(
      "lightning_dynamo", () -> new BlockEntityType<>(TileLightningDynamo::new, Blocks.lightningDynamo.get())
   );
   public static final Supplier<BlockEntityType<TileWasher>> washer = TILES.register(
      "washer", () -> new BlockEntityType<>(TileWasher::new, Blocks.washer.get())
   );
   public static final Supplier<BlockEntityType<TileBoiler>> boiler = TILES.register(
      "boiler", () -> new BlockEntityType<>(TileBoiler::new, Blocks.boiler.get())
   );
   public static final Supplier<BlockEntityType<TileFluidTank>> fluidTank = TILES.register(
      "tank", () -> new BlockEntityType<>(TileFluidTank::new, Blocks.fluidTank.get())
   );
   public static final Supplier<BlockEntityType<TilePump>> pump = TILES.register(
      "pump", () -> new BlockEntityType<>(TilePump::new, Blocks.pump.get())
   );
   public static final Supplier<BlockEntityType<TileFluidPipe>> fluidPipe = TILES.register(
      "fluid_pipe", () -> new BlockEntityType<>(TileFluidPipe::new, Blocks.fluidPipe.get())
   );
}
