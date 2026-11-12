package co.uk.mommyheather.futuregenerators.fluids;

import co.uk.mommyheather.futuregenerators.FutureGenerators;
import co.uk.mommyheather.futuregenerators.blocks.Blocks;
import co.uk.mommyheather.futuregenerators.items.Items;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidType.Properties;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Hot water: what the boiler makes and the turbines and washer run on. Its look (textures, tint and fog) is set up on
 * the client in {@code ClientSetup}.
 */
public class Fluids {
   /** The hot water tint, ARGB. */
   public static final int HOT_WATER_TINT = 0xAF7FFFD4;
   public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, FutureGenerators.MODID);
   public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, FutureGenerators.MODID);
   public static final DeferredHolder<FluidType, FluidType> HOT_WATER_TYPE = FLUID_TYPES.register(
      "hot_water", () -> new FluidType(Properties.create().supportsBoating(true).canHydrate(false))
   );
   public static final DeferredHolder<Fluid, FlowingFluid> HOT_WATER = FLUIDS.register("hot_water", () -> new BaseFlowingFluid.Source(fluidProperties()));
   public static final DeferredHolder<Fluid, FlowingFluid> HOT_WATER_FLOWING = FLUIDS.register(
      "hot_water_flowing", () -> new BaseFlowingFluid.Flowing(fluidProperties())
   );
   public static final DeferredBlock<LiquidBlock> HOT_WATER_BLOCK = Blocks.BLOCKS.registerBlock(
      "hot_water", p -> new LiquidBlock(HOT_WATER.get(), p), p -> p.noCollision().replaceable().strength(100.0F).noLootTable().liquid()
   );
   public static final DeferredItem<BucketItem> HOT_WATER_BUCKET = Items.ITEMS.registerItem(
      "hot_water_bucket", p -> new BucketItem(HOT_WATER.get(), p), p -> p.craftRemainder(net.minecraft.world.item.Items.BUCKET).stacksTo(1)
   );

   private static BaseFlowingFluid.Properties fluidProperties() {
      return new BaseFlowingFluid.Properties(HOT_WATER_TYPE, HOT_WATER, HOT_WATER_FLOWING).block(HOT_WATER_BLOCK).bucket(HOT_WATER_BUCKET);
   }
}
