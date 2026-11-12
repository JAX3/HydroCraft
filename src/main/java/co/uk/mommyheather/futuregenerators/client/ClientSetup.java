package co.uk.mommyheather.futuregenerators.client;

import co.uk.mommyheather.futuregenerators.FutureGenerators;
import co.uk.mommyheather.futuregenerators.fluids.Fluids;
import co.uk.mommyheather.futuregenerators.render.FluidTankRenderer;
import co.uk.mommyheather.futuregenerators.render.PumpRenderer;
import co.uk.mommyheather.futuregenerators.tile.Tiles;
import co.uk.mommyheather.futuregenerators.ui.BoilerScreen;
import co.uk.mommyheather.futuregenerators.ui.FluidTankScreen;
import co.uk.mommyheather.futuregenerators.ui.LightningDynamoScreen;
import co.uk.mommyheather.futuregenerators.ui.LightningGeneratorScreen;
import co.uk.mommyheather.futuregenerators.ui.Menus;
import co.uk.mommyheather.futuregenerators.ui.MultiblockTurbineScreen;
import co.uk.mommyheather.futuregenerators.ui.PumpScreen;
import co.uk.mommyheather.futuregenerators.ui.TurbineScreen;
import co.uk.mommyheather.futuregenerators.ui.WasherScreen;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.fluid.FluidTintSources;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;

/** Client-only entry point: screens, the fluid renderers and how hot water looks. Never loaded on a dedicated server. */
@Mod(value = FutureGenerators.MODID, dist = Dist.CLIENT)
public class ClientSetup {
   private static final Identifier HOT_WATER = FutureGenerators.id("block/hot_water");
   private static final Identifier HOT_WATER_VIEW = FutureGenerators.id("textures/block/hot_water.png");

   public ClientSetup(IEventBus modBus, ModContainer container) {
      modBus.addListener(ClientSetup::registerScreens);
      modBus.addListener(ClientSetup::registerRenderers);
      modBus.addListener(ClientSetup::registerFluidModels);
      modBus.addListener(ClientSetup::registerClientExtensions);
   }

   private static void registerScreens(RegisterMenuScreensEvent event) {
      event.register(Menus.turbine.get(), TurbineScreen::new);
      event.register(Menus.multiblockTurbine.get(), MultiblockTurbineScreen::new);
      event.register(Menus.lightningGenerator.get(), LightningGeneratorScreen::new);
      event.register(Menus.lightningDynamo.get(), LightningDynamoScreen::new);
      event.register(Menus.washer.get(), WasherScreen::new);
      event.register(Menus.boiler.get(), BoilerScreen::new);
      event.register(Menus.fluidTank.get(), FluidTankScreen::new);
      event.register(Menus.pump.get(), PumpScreen::new);
   }

   private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
      event.registerBlockEntityRenderer(Tiles.fluidTank.get(), context -> new FluidTankRenderer());
      event.registerBlockEntityRenderer(Tiles.pump.get(), context -> new PumpRenderer());
   }

   /** Hot water uses one animated texture for still, flowing and overlay, tinted aquamarine. */
   private static void registerFluidModels(RegisterFluidModelsEvent event) {
      Material texture = new Material(HOT_WATER);
      event.register(new FluidModel.Unbaked(texture, texture, texture, FluidTintSources.constant(Fluids.HOT_WATER_TINT)),
         Fluids.HOT_WATER, Fluids.HOT_WATER_FLOWING);
   }

   private static void registerClientExtensions(RegisterClientExtensionsEvent event) {
      event.registerFluidType(new IClientFluidTypeExtensions() {
         @Override
         public Identifier getRenderOverlayTexture(Minecraft mc) {
            return HOT_WATER_VIEW;
         }

         /** Underwater fog takes the hot water's colour. */
         @Override
         public void modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector4f fluidFogColor) {
            int color = Fluids.HOT_WATER_TINT;
            fluidFogColor.set((color >> 16 & 0xFF) / 255.0F, (color >> 8 & 0xFF) / 255.0F, (color & 0xFF) / 255.0F, fluidFogColor.w);
         }

         /** And it is murky: you see about 24 blocks, as in the original. */
         @Override
         public void modifyFogRender(Camera camera, @Nullable FogEnvironment environment, float renderDistance, float partialTick, FogData fogData) {
            fogData.environmentalStart = -8.0F;
            fogData.environmentalEnd = Math.min(24.0F, renderDistance);
         }
      }, Fluids.HOT_WATER_TYPE.get());
   }
}
