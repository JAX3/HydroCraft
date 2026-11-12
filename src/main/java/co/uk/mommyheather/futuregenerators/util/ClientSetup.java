package co.uk.mommyheather.futuregenerators.util;

import co.uk.mommyheather.futuregenerators.ui.BoilerScreen;
import co.uk.mommyheather.futuregenerators.ui.FluidTankScreen;
import co.uk.mommyheather.futuregenerators.ui.LightningDynamoScreen;
import co.uk.mommyheather.futuregenerators.ui.LightningGeneratorScreen;
import co.uk.mommyheather.futuregenerators.ui.Menus;
import co.uk.mommyheather.futuregenerators.ui.MultiblockTurbineScreen;
import co.uk.mommyheather.futuregenerators.ui.PumpScreen;
import co.uk.mommyheather.futuregenerators.ui.TurbineScreen;
import co.uk.mommyheather.futuregenerators.ui.WasherScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = "futuregenerators", bus = Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
   @SubscribeEvent
   public static void init(FMLClientSetupEvent event) {
      event.enqueueWork(() -> {
         MenuScreens.register((MenuType)Menus.turbine.get(), TurbineScreen::new);
         MenuScreens.register((MenuType)Menus.multiblockTurbine.get(), MultiblockTurbineScreen::new);
         MenuScreens.register((MenuType)Menus.lightningGenerator.get(), LightningGeneratorScreen::new);
         MenuScreens.register((MenuType)Menus.lightningDynamo.get(), LightningDynamoScreen::new);
         MenuScreens.register((MenuType)Menus.washer.get(), WasherScreen::new);
         MenuScreens.register((MenuType)Menus.boiler.get(), BoilerScreen::new);
         MenuScreens.register((MenuType)Menus.fluidTank.get(), FluidTankScreen::new);
         MenuScreens.register((MenuType)Menus.pump.get(), PumpScreen::new);
      });
   }
}
