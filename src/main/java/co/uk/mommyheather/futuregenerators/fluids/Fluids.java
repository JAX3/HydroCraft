package co.uk.mommyheather.futuregenerators.fluids;

import co.uk.mommyheather.futuregenerators.blocks.Blocks;
import co.uk.mommyheather.futuregenerators.items.Items;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.function.Consumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer.FogMode;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.FluidType.Properties;
import net.minecraftforge.fluids.ForgeFlowingFluid.Flowing;
import net.minecraftforge.fluids.ForgeFlowingFluid.Source;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries.Keys;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class Fluids {
   public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(Keys.FLUID_TYPES, "futuregenerators");
   public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, "futuregenerators");
   public static final RegistryObject<FluidType> HOT_WATER_TYPE = FLUID_TYPES.register(
      "hot_water",
      () -> new FluidType(Properties.create().supportsBoating(true).canHydrate(false)) {
         public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
            consumer.accept(
               new IClientFluidTypeExtensions() {
                  private static final ResourceLocation STILL = new ResourceLocation("futuregenerators:block/hot_water");
                  private static final ResourceLocation FLOW = new ResourceLocation("futuregenerators:block/hot_water");
                  private static final ResourceLocation OVERLAY = new ResourceLocation("futuregenerators:block/hot_water");
                  private static final ResourceLocation VIEW_OVERLAY = new ResourceLocation("futuregenerators:textures/block/hot_water.png");

                  public ResourceLocation getStillTexture() {
                     return STILL;
                  }

                  public ResourceLocation getFlowingTexture() {
                     return FLOW;
                  }

                  public ResourceLocation getOverlayTexture() {
                     return OVERLAY;
                  }

                  public ResourceLocation getRenderOverlayTexture(Minecraft mc) {
                     return VIEW_OVERLAY;
                  }

                  public int getTintColor() {
                     return -1350565932;
                  }

                  @NotNull
                  public Vector3f modifyFogColor(
                     Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector3f fluidFogColor
                  ) {
                     int color = this.getTintColor();
                     return new Vector3f((color >> 16 & 0xFF) / 255.0F, (color >> 8 & 0xFF) / 255.0F, (color & 0xFF) / 255.0F);
                  }

                  public void modifyFogRender(
                     Camera camera, FogMode mode, float renderDistance, float partialTick, float nearDistance, float farDistance, FogShape shape
                  ) {
                     nearDistance = -8.0F;
                     farDistance = 24.0F;
                     if (farDistance > renderDistance) {
                        farDistance = renderDistance;
                        shape = FogShape.CYLINDER;
                     }

                     RenderSystem.setShaderFogStart(nearDistance);
                     RenderSystem.setShaderFogEnd(farDistance);
                     RenderSystem.setShaderFogShape(shape);
                  }
               }
            );
         }
      }
   );
   public static final RegistryObject<FlowingFluid> HOT_WATER = FLUIDS.register("hot_water", () -> new Source(fluidProperties()));
   public static final RegistryObject<Fluid> HOT_WATER_FLOWING = FLUIDS.register("hot_water_flowing", () -> new Flowing(fluidProperties()));
   public static final RegistryObject<LiquidBlock> HOT_WATER_BLOCK = Blocks.BLOCKS
      .register(
         "hot_water",
         () -> new LiquidBlock(
            HOT_WATER, net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().noCollission().replaceable().strength(100.0F).noLootTable()
         )
      );
   public static final RegistryObject<Item> HOT_WATER_BUCKET = Items.ITEMS
      .register(
         "hot_water_bucket",
         () -> new BucketItem(HOT_WATER, new net.minecraft.world.item.Item.Properties().craftRemainder(net.minecraft.world.item.Items.BUCKET).stacksTo(1))
      );

   private static net.minecraftforge.fluids.ForgeFlowingFluid.Properties fluidProperties() {
      return new net.minecraftforge.fluids.ForgeFlowingFluid.Properties(HOT_WATER_TYPE, HOT_WATER, HOT_WATER_FLOWING)
         .block(HOT_WATER_BLOCK)
         .bucket(HOT_WATER_BUCKET);
   }
}
