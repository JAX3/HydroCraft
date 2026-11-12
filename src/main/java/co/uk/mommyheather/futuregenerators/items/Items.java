package co.uk.mommyheather.futuregenerators.items;

import co.uk.mommyheather.futuregenerators.FutureGenerators;
import co.uk.mommyheather.futuregenerators.blocks.Blocks;
import co.uk.mommyheather.futuregenerators.fluids.Fluids;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class Items {
   public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(FutureGenerators.MODID);
   public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, FutureGenerators.MODID);
   public static final DeferredItem<BlockItem> turbine = ITEMS.registerSimpleBlockItem(Blocks.turbine);
   public static final DeferredItem<BlockItem> turbineController = ITEMS.registerSimpleBlockItem(Blocks.turbineController);
   public static final DeferredItem<BlockItem> turbineCasing = ITEMS.registerSimpleBlockItem(Blocks.turbineCasing);
   public static final DeferredItem<BlockItem> lightningGenerator = ITEMS.registerSimpleBlockItem(Blocks.lightningGenerator);
   public static final DeferredItem<BlockItem> lightningDynamo = ITEMS.registerSimpleBlockItem(Blocks.lightningDynamo);
   public static final DeferredItem<BlockItem> washer = ITEMS.registerSimpleBlockItem(Blocks.washer);
   public static final DeferredItem<BlockItem> boiler = ITEMS.registerSimpleBlockItem(Blocks.boiler);
   public static final DeferredItem<BlockItem> fluidTank = ITEMS.registerSimpleBlockItem(Blocks.fluidTank);
   public static final DeferredItem<BlockItem> pump = ITEMS.registerSimpleBlockItem(Blocks.pump);
   public static final DeferredItem<BlockItem> fluidPipe = ITEMS.registerSimpleBlockItem(Blocks.fluidPipe);
   public static final DeferredItem<Item> lightningCharge = ITEMS.registerSimpleItem("lightning_charge", p -> p.stacksTo(1).durability(250));

   public static final Supplier<CreativeModeTab> CREATIVE = TABS.register("creative_tab", () -> CreativeModeTab.builder()
      .icon(() -> new ItemStack(turbine.get()))
      .title(Component.translatable("futuregenerators.creativetab"))
      .displayItems((params, output) -> {
         output.accept(turbine.get());
         output.accept(turbineController.get());
         output.accept(turbineCasing.get());
         output.accept(lightningGenerator.get());
         output.accept(lightningDynamo.get());
         output.accept(washer.get());
         output.accept(boiler.get());
         output.accept(fluidTank.get());
         output.accept(pump.get());
         output.accept(fluidPipe.get());
         output.accept(lightningCharge.get());
         output.accept(Fluids.HOT_WATER_BUCKET.get());
      })
      .build());
}
