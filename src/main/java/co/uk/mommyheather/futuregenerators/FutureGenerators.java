package co.uk.mommyheather.futuregenerators;

import co.uk.mommyheather.futuregenerators.blocks.Blocks;
import co.uk.mommyheather.futuregenerators.config.FutureGeneratorsConfig;
import co.uk.mommyheather.futuregenerators.fluids.Fluids;
import co.uk.mommyheather.futuregenerators.gametest.ModGameTests;
import co.uk.mommyheather.futuregenerators.items.Items;
import co.uk.mommyheather.futuregenerators.tile.Tiles;
import co.uk.mommyheather.futuregenerators.ui.Menus;
import co.uk.mommyheather.futuregenerators.util.Capabilities;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(FutureGenerators.MODID)
public class FutureGenerators {
   public static final Logger LOGGER = LogUtils.getLogger();
   public static final String MODID = "futuregenerators";

   public FutureGenerators(IEventBus modBus, ModContainer container) {
      Fluids.FLUID_TYPES.register(modBus);
      Fluids.FLUIDS.register(modBus);
      Blocks.BLOCKS.register(modBus);
      Items.ITEMS.register(modBus);
      Items.TABS.register(modBus);
      Tiles.TILES.register(modBus);
      Menus.MENUS.register(modBus);
      modBus.addListener(Capabilities::register);
      ModGameTests.FUNCTIONS.register(modBus);
      modBus.addListener(ModGameTests::registerTests);
      container.registerConfig(ModConfig.Type.SYNCED, FutureGeneratorsConfig.SERVER_SPEC, MODID + ".toml");
   }

   public static Identifier id(String path) {
      return Identifier.fromNamespaceAndPath(MODID, path);
   }
}
