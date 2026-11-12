package co.uk.mommyheather.futuregenerators.items;

import co.uk.mommyheather.futuregenerators.blocks.Blocks;
import co.uk.mommyheather.futuregenerators.fluids.Fluids;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;

public class Items {
   public static final ResourceKey<CreativeModeTab> CREATIVE = ResourceKey.create(
      Registries.CREATIVE_MODE_TAB, new ResourceLocation("futuregenerators", "creative_tab")
   );
   public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "futuregenerators");
   public static final RegistryObject<BlockItem> turbine = ITEMS.register(
      "turbine", () -> new BlockItem((Block)Blocks.turbine.get(), new Properties().stacksTo(64))
   );
   public static final RegistryObject<BlockItem> turbineController = ITEMS.register(
      "turbine_controller", () -> new BlockItem((Block)Blocks.turbineController.get(), new Properties().stacksTo(64))
   );
   public static final RegistryObject<BlockItem> turbineCasing = ITEMS.register(
      "turbine_casing", () -> new BlockItem((Block)Blocks.turbineCasing.get(), new Properties().stacksTo(64))
   );
   public static final RegistryObject<BlockItem> lightningGenerator = ITEMS.register(
      "lightning_generator", () -> new BlockItem((Block)Blocks.lightningGenerator.get(), new Properties().stacksTo(64))
   );
   public static final RegistryObject<BlockItem> lightningDynamo = ITEMS.register(
      "lightning_dynamo", () -> new BlockItem((Block)Blocks.lightningDynamo.get(), new Properties().stacksTo(64))
   );
   public static final RegistryObject<BlockItem> washer = ITEMS.register(
      "washer", () -> new BlockItem((Block)Blocks.washer.get(), new Properties().stacksTo(64))
   );
   public static final RegistryObject<BlockItem> boiler = ITEMS.register(
      "boiler", () -> new BlockItem((Block)Blocks.boiler.get(), new Properties().stacksTo(64))
   );
   public static final RegistryObject<BlockItem> fluidTank = ITEMS.register(
      "tank", () -> new BlockItem((Block)Blocks.fluidTank.get(), new Properties().stacksTo(64))
   );
   public static final RegistryObject<BlockItem> pump = ITEMS.register("pump", () -> new BlockItem((Block)Blocks.pump.get(), new Properties().stacksTo(64)));
   public static final RegistryObject<BlockItem> fluidPipe = ITEMS.register(
      "fluid_pipe", () -> new BlockItem((Block)Blocks.fluidPipe.get(), new Properties().stacksTo(64))
   );
   public static final RegistryObject<Item> lightningCharge = ITEMS.register("lightning_charge", () -> new Item(new Properties().stacksTo(1).durability(250)));

   public static void onCreativeModeTabRegister(RegisterEvent event) {
      event.register(
         Registries.CREATIVE_MODE_TAB,
         helper -> helper.register(
            CREATIVE,
            CreativeModeTab.builder()
               .icon(() -> new ItemStack((ItemLike)turbine.get()))
               .title(Component.translatable("futuregenerators.creativetab"))
               .displayItems((params, output) -> {
                  output.accept(new ItemStack((ItemLike)turbine.get()));
                  output.accept(new ItemStack((ItemLike)turbineController.get()));
                  output.accept(new ItemStack((ItemLike)turbineCasing.get()));
                  output.accept(new ItemStack((ItemLike)lightningGenerator.get()));
                  output.accept(new ItemStack((ItemLike)lightningDynamo.get()));
                  output.accept(new ItemStack((ItemLike)washer.get()));
                  output.accept(new ItemStack((ItemLike)boiler.get()));
                  output.accept(new ItemStack((ItemLike)fluidTank.get()));
                  output.accept(new ItemStack((ItemLike)pump.get()));
                  output.accept(new ItemStack((ItemLike)fluidPipe.get()));
                  output.accept(new ItemStack((ItemLike)lightningCharge.get()));
                  output.accept(new ItemStack((ItemLike)Fluids.HOT_WATER_BUCKET.get()));
               })
               .build()
         )
      );
   }
}
