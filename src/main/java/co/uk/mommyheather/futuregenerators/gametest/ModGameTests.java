package co.uk.mommyheather.futuregenerators.gametest;

import co.uk.mommyheather.futuregenerators.FutureGenerators;
import co.uk.mommyheather.futuregenerators.blocks.BlockFluidPipe;
import co.uk.mommyheather.futuregenerators.blocks.Blocks;
import co.uk.mommyheather.futuregenerators.fluids.Fluids;
import co.uk.mommyheather.futuregenerators.items.Items;
import co.uk.mommyheather.futuregenerators.tile.TileBoiler;
import co.uk.mommyheather.futuregenerators.tile.TileFluidTank;
import co.uk.mommyheather.futuregenerators.tile.TileLightningDynamo;
import co.uk.mommyheather.futuregenerators.tile.TileLightningGenerator;
import co.uk.mommyheather.futuregenerators.tile.TilePump;
import co.uk.mommyheather.futuregenerators.tile.TileTurbine;
import co.uk.mommyheather.futuregenerators.tile.TileTurbineController;
import co.uk.mommyheather.futuregenerators.tile.TileWasher;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/**
 * In-world tests of every machine, run with {@code ./gradlew runGameTestServer} (a real dedicated server, which also
 * proves no client classes load on the server). Each runs on a 7x5x7 stone platform.
 */
public final class ModGameTests {
   public static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS = DeferredRegister.create(Registries.TEST_FUNCTION, FutureGenerators.MODID);
   private static final Identifier STRUCTURE = FutureGenerators.id("test_platform");
   private static final Map<String, Consumer<GameTestHelper>> TESTS = new LinkedHashMap<>();

   static {
      TESTS.put("turbine_makes_power_from_hot_water", ModGameTests::turbineMakesPower);
      TESTS.put("turbine_refuses_plain_water", ModGameTests::turbineRefusesWater);
      TESTS.put("boiler_heats_water_with_fuel", ModGameTests::boilerHeatsWater);
      TESTS.put("boiler_feeds_turbine_which_powers_washer", ModGameTests::boilerTurbineWasher);
      TESTS.put("washer_washes_cobblestone", ModGameTests::washerWashes);
      TESTS.put("fluid_pipe_pulls_from_one_tank_into_another", ModGameTests::pipeMovesFluid);
      TESTS.put("pump_pumps_water_without_draining_it", ModGameTests::pumpPumpsWater);
      TESTS.put("pump_drains_lava_sources", ModGameTests::pumpDrainsLava);
      TESTS.put("multiblock_turbine_forms_and_makes_power", ModGameTests::multiblockTurbine);
      TESTS.put("lightning_generator_runs_dynamo", ModGameTests::lightningDynamo);
      TESTS.put("multiblock_turbine_casings_push_power", ModGameTests::casingsPushPower);
      TESTS.put("washer_inputs_stay_put_for_hoppers_but_not_players", ModGameTests::washerInputs);
      TESTS.forEach((name, fn) -> FUNCTIONS.register(name, () -> fn));
   }

   private ModGameTests() {}

   public static void registerTests(RegisterGameTestsEvent event) {
      Holder<TestEnvironmentDefinition<?>> env = event.registerEnvironment(FutureGenerators.id("default"), new TestEnvironmentDefinition.AllOf());
      for (String name : TESTS.keySet()) {
         ResourceKey<Consumer<GameTestHelper>> fn = ResourceKey.create(Registries.TEST_FUNCTION, FutureGenerators.id(name));
         event.registerTest(FutureGenerators.id(name), new FunctionGameTestInstance(fn, new TestData<>(env, STRUCTURE, 400, 0, true)));
      }
   }

   private static FluidResource hotWater() {
      return FluidResource.of(Fluids.HOT_WATER.get());
   }

   private static FluidResource water() {
      return FluidResource.of(net.minecraft.world.level.material.Fluids.WATER);
   }

   /** Pours fluid into the block at {@code pos} through its fluid capability, the way a pipe or another mod would. */
   private static int pour(GameTestHelper h, BlockPos pos, FluidResource fluid, int amount) {
      ResourceHandler<FluidResource> handler = h.getLevel().getCapability(Capabilities.Fluid.BLOCK, h.absolutePos(pos), Direction.UP);
      h.assertTrue(handler != null, "block at " + pos + " has a fluid handler");
      try (Transaction transaction = Transaction.openRoot()) {
         int inserted = handler.insert(fluid, amount, transaction);
         transaction.commit();
         return inserted;
      }
   }

   // ------------------------------------------------------------------ turbines

   private static void turbineMakesPower(GameTestHelper h) {
      BlockPos pos = new BlockPos(3, 1, 3);
      h.setBlock(pos, Blocks.turbine.get());
      h.runAfterDelay(1, () -> {
         h.assertTrue(pour(h, pos, hotWater(), 5000) == 5000, "the turbine takes hot water through its capability");
         h.succeedWhen(() -> {
            TileTurbine turbine = h.getBlockEntity(pos, TileTurbine.class);
            h.assertTrue(turbine.speed > 0, "the turbine spins up");
            h.assertTrue(turbine.battery.getEnergyStored() > 0, "the turbine makes FE");
            h.assertTrue(turbine.tank.getFluidAmount() < 5000, "the turbine uses hot water");
         });
      });
   }

   private static void turbineRefusesWater(GameTestHelper h) {
      BlockPos pos = new BlockPos(3, 1, 3);
      h.setBlock(pos, Blocks.turbine.get());
      h.runAfterDelay(1, () -> {
         h.assertTrue(pour(h, pos, water(), 1000) == 0, "plain water is refused");
         h.succeed();
      });
   }

   private static void multiblockTurbine(GameTestHelper h) {
      // Controller with three casings making a 2x2 square to its north-east.
      BlockPos controllerPos = new BlockPos(2, 1, 3);
      h.setBlock(new BlockPos(2, 1, 2), Blocks.turbineCasing.get());
      h.setBlock(new BlockPos(3, 1, 3), Blocks.turbineCasing.get());
      h.setBlock(new BlockPos(3, 1, 2), Blocks.turbineCasing.get());
      h.setBlock(controllerPos, Blocks.turbineController.get());
      h.runAfterDelay(2, () -> {
         TileTurbineController controller = h.getBlockEntity(controllerPos, TileTurbineController.class);
         h.assertTrue(controller.valid, "the turbine forms");
         h.assertTrue(controller.casings.size() == 3, "with three casings");
         // Casings stand in for the controller.
         h.assertTrue(pour(h, new BlockPos(3, 1, 2), hotWater(), 5000) == 5000, "hot water goes in through a casing");
         h.succeedWhen(() -> h.assertTrue(controller.battery.getEnergyStored() > 0 && controller.speed > 0, "the multiblock turbine makes FE"));
      });
   }

   /** A washer touching only a casing (not the controller) still gets power. */
   private static void casingsPushPower(GameTestHelper h) {
      BlockPos controllerPos = new BlockPos(2, 1, 3), washerPos = new BlockPos(4, 1, 2);
      h.setBlock(new BlockPos(2, 1, 2), Blocks.turbineCasing.get());
      h.setBlock(new BlockPos(3, 1, 3), Blocks.turbineCasing.get());
      h.setBlock(new BlockPos(3, 1, 2), Blocks.turbineCasing.get());
      h.setBlock(controllerPos, Blocks.turbineController.get());
      h.setBlock(washerPos, Blocks.washer.get());
      h.runAfterDelay(2, () -> {
         h.assertTrue(h.getBlockEntity(controllerPos, TileTurbineController.class).valid, "the turbine forms");
         pour(h, controllerPos, hotWater(), 20000);
         h.succeedWhen(() -> h.assertTrue(h.getBlockEntity(washerPos, TileWasher.class).battery.getEnergyStored() > 0,
            "the washer beside a casing is powered"));
      });
   }

   // ------------------------------------------------------------------ boiler

   private static void boilerHeatsWater(GameTestHelper h) {
      BlockPos pos = new BlockPos(3, 1, 3);
      h.setBlock(pos, Blocks.boiler.get());
      h.runAfterDelay(1, () -> {
         TileBoiler boiler = h.getBlockEntity(pos, TileBoiler.class);
         h.assertTrue(pour(h, pos, hotWater(), 1000) == 0, "only plain water goes in");
         h.assertTrue(pour(h, pos, water(), 8000) == 8000, "water goes in");
         boiler.items.setStackInSlot(0, new ItemStack(net.minecraft.world.item.Items.COAL));
         h.succeedWhen(() -> {
            h.assertTrue(boiler.fuelRemaining > 0, "the boiler burns the coal");
            h.assertTrue(boiler.tank.outTank.getFluidAmount() > 0, "and makes hot water");
            h.assertTrue(boiler.tank.outTank.getFluidResource().is(Fluids.HOT_WATER.get()), "which is hot water");
         });
      });
   }

   /** Boiler -> turbine -> washer, with no pipes: each machine pushes straight into the next. */
   private static void boilerTurbineWasher(GameTestHelper h) {
      BlockPos boilerPos = new BlockPos(2, 1, 3), turbinePos = new BlockPos(3, 1, 3), washerPos = new BlockPos(4, 1, 3);
      h.setBlock(boilerPos, Blocks.boiler.get());
      h.setBlock(turbinePos, Blocks.turbine.get());
      h.setBlock(washerPos, Blocks.washer.get());
      h.runAfterDelay(1, () -> {
         pour(h, boilerPos, water(), 8000);
         h.getBlockEntity(boilerPos, TileBoiler.class).items.setStackInSlot(0, new ItemStack(net.minecraft.world.item.Items.COAL, 4));
         h.succeedWhen(() -> {
            h.assertTrue(h.getBlockEntity(turbinePos, TileTurbine.class).speed > 0, "the turbine runs on the boiler's hot water");
            h.assertTrue(h.getBlockEntity(washerPos, TileWasher.class).battery.getEnergyStored() > 0, "and powers the washer");
         });
      });
   }

   // ------------------------------------------------------------------ washer

   private static void washerWashes(GameTestHelper h) {
      BlockPos pos = new BlockPos(3, 1, 3);
      h.setBlock(pos, Blocks.washer.get());
      h.runAfterDelay(1, () -> {
         TileWasher washer = h.getBlockEntity(pos, TileWasher.class);
         h.assertTrue(pour(h, pos, hotWater(), 10000) == 10000, "hot water goes in");
         washer.battery.setEnergy(10000);
         washer.items.setStackInSlot(0, new ItemStack(net.minecraft.world.item.Items.COBBLESTONE, 64));
         h.assertTrue(!washer.items.isValid(3, net.neoforged.neoforge.transfer.item.ItemResource.of(net.minecraft.world.item.Items.COBBLESTONE)),
            "output slots don't take items");
         h.succeedWhen(() -> {
            h.assertTrue(washer.items.getStackInSlot(0).getCount() < 64, "cobblestone is used up");
            h.assertTrue(washer.tank.getFluidAmount() < 10000, "hot water is used");
            h.assertTrue(washer.battery.getEnergyStored() < 10000, "power is used");
         });
      });
   }

   private static void washerInputs(GameTestHelper h) {
      BlockPos pos = new BlockPos(3, 1, 3);
      h.setBlock(pos, Blocks.washer.get());
      h.runAfterDelay(1, () -> {
         TileWasher washer = h.getBlockEntity(pos, TileWasher.class);
         var cobble = net.neoforged.neoforge.transfer.item.ItemResource.of(net.minecraft.world.item.Items.COBBLESTONE);
         var nugget = net.neoforged.neoforge.transfer.item.ItemResource.of(net.minecraft.world.item.Items.IRON_NUGGET);
         washer.items.setStackInSlot(0, new ItemStack(net.minecraft.world.item.Items.COBBLESTONE, 10));
         washer.items.setStackInSlot(3, new ItemStack(net.minecraft.world.item.Items.IRON_NUGGET, 5));
         var automation = h.getLevel().getCapability(Capabilities.Item.BLOCK, h.absolutePos(pos), Direction.DOWN);
         try (Transaction simulate = Transaction.openRoot()) {
            h.assertTrue(automation.extract(0, cobble, 10, simulate) == 0, "hoppers can't take cobblestone out of the input");
            h.assertTrue(automation.extract(3, nugget, 5, simulate) == 5, "hoppers can take the results");
            h.assertTrue(washer.items.extract(0, cobble, 10, simulate) == 10, "players can take cobblestone back out in the GUI");
         }
         h.succeed();
      });
   }

   // ------------------------------------------------------------------ pipes and tanks

   private static void pipeMovesFluid(GameTestHelper h) {
      BlockPos from = new BlockPos(1, 1, 3), to = new BlockPos(4, 1, 3);
      h.setBlock(from, Blocks.fluidTank.get());
      h.setBlock(to, Blocks.fluidTank.get());
      BlockFluidPipe pipe = Blocks.fluidPipe.get();
      for (int x = 2; x <= 3; x++) {
         BlockPos pos = new BlockPos(x, 1, 3);
         h.setBlock(pos, pipe.defaultBlockState());
      }
      h.runAfterDelay(1, () -> {
         // Connect the pipes as if placed by a player, with the first one pulling from the tank to its west.
         for (int x = 2; x <= 3; x++) {
            BlockPos pos = new BlockPos(x, 1, 3);
            BlockState state = pipe.getStateForPlacement(h.getLevel(), h.absolutePos(pos));
            if (x == 2) state = state.setValue(BlockFluidPipe.PUMP_WEST, true);
            h.setBlock(pos, state);
         }
         h.assertTrue(pour(h, from, water(), 20000) == 20000, "the first tank fills");
         h.succeedWhen(() -> {
            TileFluidTank target = h.getBlockEntity(to, TileFluidTank.class);
            h.assertTrue(target.tank.getFluidAmount() > 0, "water reaches the second tank");
            h.assertTrue(h.getBlockEntity(from, TileFluidTank.class).tank.getFluidAmount() + target.tank.getFluidAmount() == 20000,
               "no water is lost or made");
         });
      });
   }

   private static void pumpPumpsWater(GameTestHelper h) {
      BlockPos pumpPos = new BlockPos(3, 2, 3), waterPos = new BlockPos(3, 1, 3);
      h.setBlock(waterPos, net.minecraft.world.level.block.Blocks.WATER);
      h.setBlock(pumpPos, Blocks.pump.get());
      h.runAfterDelay(1, () -> {
         TilePump pump = h.getBlockEntity(pumpPos, TilePump.class);
         pump.items.setStackInSlot(0, new ItemStack(net.minecraft.world.item.Items.COAL, 4));
         h.succeedWhen(() -> {
            h.assertTrue(pump.tank.getFluidAmount() >= 2000, "the pump pumps water");
            h.assertTrue(pump.battery.getEnergyStored() > 0 || pump.items.getStackInSlot(0).getCount() < 4, "it burns fuel for power");
            FluidState below = h.getLevel().getFluidState(h.absolutePos(waterPos));
            h.assertTrue(below.isSource(), "water isn't drained by default");
         });
      });
   }

   private static void pumpDrainsLava(GameTestHelper h) {
      BlockPos pumpPos = new BlockPos(3, 2, 3), lavaPos = new BlockPos(3, 1, 3);
      h.setBlock(lavaPos, net.minecraft.world.level.block.Blocks.LAVA);
      h.setBlock(pumpPos, Blocks.pump.get());
      h.runAfterDelay(1, () -> {
         h.getBlockEntity(pumpPos, TilePump.class).items.setStackInSlot(0, new ItemStack(net.minecraft.world.item.Items.COAL, 4));
         h.succeedWhen(() -> {
            TilePump pump = h.getBlockEntity(pumpPos, TilePump.class);
            h.assertTrue(pump.tank.getFluidAmount() == 1000, "the pump takes the lava");
            h.assertTrue(h.getLevel().getFluidState(h.absolutePos(lavaPos)).isEmpty(), "and the source is gone");
         });
      });
   }

   // ------------------------------------------------------------------ lightning

   private static void lightningDynamo(GameTestHelper h) {
      BlockPos generatorPos = new BlockPos(3, 1, 3), dynamoPos = new BlockPos(4, 1, 3);
      h.setBlock(generatorPos, Blocks.lightningGenerator.get());
      h.setBlock(generatorPos.above(), net.minecraft.world.level.block.Blocks.LIGHTNING_ROD.asList().getFirst());
      h.setBlock(dynamoPos, Blocks.lightningDynamo.get());
      h.runAfterDelay(2, () -> {
         TileLightningGenerator generator = h.getBlockEntity(generatorPos, TileLightningGenerator.class);
         generator.checkNeighbours();
         h.getBlockEntity(dynamoPos, TileLightningDynamo.class).checkNeighbours();
         h.assertTrue(generator.hasRod, "the generator sees its lightning rod");
         h.assertTrue(generator.dynamos == 1, "and the dynamo");
         generator.battery.setEnergy(100000);
         generator.items.setStackInSlot(0, new ItemStack(Items.lightningCharge.get()));
         h.succeedWhen(() -> {
            h.assertTrue(generator.isRunning(), "the generator runs");
            h.assertTrue(h.getBlockEntity(dynamoPos, TileLightningDynamo.class).battery.getEnergyStored() > 0, "the dynamo makes FE");
         });
      });
   }
}
