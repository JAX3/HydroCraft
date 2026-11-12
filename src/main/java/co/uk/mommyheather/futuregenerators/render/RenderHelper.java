package co.uk.mommyheather.futuregenerators.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

/** Draws fluids: as a box inside a block (tank and pump) and as a tiled gauge in a GUI. */
public class RenderHelper {
   private static final float MIN = 0.001F;
   private static final float MAX = 0.999F;
   /** Full-bright, as in the original. */
   private static final int LIGHT = 240;

   private static FluidModel model(Fluid fluid) {
      return Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluid.defaultFluidState());
   }

   /** The fluid's still texture on the block atlas. */
   public static TextureAtlasSprite still(Fluid fluid) {
      return model(fluid).stillMaterial().sprite();
   }

   /** The colour the fluid's texture is tinted with (white for untinted fluids like lava). */
   public static int tint(Fluid fluid) {
      var source = model(fluid).fluidTintSource();
      return source == null ? -1 : source.colorAsStack(new FluidStack(fluid, 1000));
   }

   /** A box of fluid filling the block from the bottom to {@code ylevel} (0..1), drawn with {@code buffer}. */
   public static void renderFluidInWorld(PoseStack.Pose pose, VertexConsumer buffer, TextureAtlasSprite sprite, int color, float ylevel) {
      float top = Math.min(ylevel, MAX);
      float minU = sprite.getU(5 / 16.0F), maxU = sprite.getU(11 / 16.0F);
      float minV = sprite.getV(5 / 16.0F), maxV = sprite.getV(11 / 16.0F);
      for (Direction dir : Direction.values()) {
         float[][] c = switch (dir) {
            case UP -> new float[][]{{MAX, top, MIN}, {MIN, top, MIN}, {MIN, top, MAX}, {MAX, top, MAX}};
            case DOWN -> new float[][]{{MIN, MIN, MIN}, {MAX, MIN, MIN}, {MAX, MIN, MAX}, {MIN, MIN, MAX}};
            case NORTH -> new float[][]{{MAX, MIN, MIN}, {MIN, MIN, MIN}, {MIN, top, MIN}, {MAX, top, MIN}};
            case SOUTH -> new float[][]{{MIN, MIN, MAX}, {MAX, MIN, MAX}, {MAX, top, MAX}, {MIN, top, MAX}};
            case EAST -> new float[][]{{MAX, MIN, MAX}, {MAX, MIN, MIN}, {MAX, top, MIN}, {MAX, top, MAX}};
            case WEST -> new float[][]{{MIN, MIN, MIN}, {MIN, MIN, MAX}, {MIN, top, MAX}, {MIN, top, MIN}};
         };
         float[][] uv = {{minU, minV}, {maxU, minV}, {maxU, maxV}, {minU, maxV}};
         for (int i = 0; i < 4; i++) {
            buffer.addVertex(pose, c[i][0], c[i][1], c[i][2])
               .setColor(color)
               .setUv(uv[i][0], uv[i][1])
               .setOverlay(OverlayTexture.NO_OVERLAY)
               .setLight(LIGHT)
               .setNormal(pose, dir.getStepX(), dir.getStepY(), dir.getStepZ());
         }
      }
   }

   /** Fills a GUI rectangle with the fluid's texture, tiled 16x16 from the bottom up. */
   public static void renderFluidInGui(GuiGraphicsExtractor graphics, int x, int y, int width, int height, Fluid fluid) {
      if (width <= 0 || height <= 0) return;
      TextureAtlasSprite sprite = still(fluid);
      int color = ARGB.opaque(tint(fluid));
      int bottom = y + height;
      graphics.enableScissor(x, y, x + width, bottom);
      for (int dy = 0; dy < height; dy += 16) {
         for (int dx = 0; dx < width; dx += 16) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x + dx, bottom - dy - 16, 16, 16, color);
         }
      }
      graphics.disableScissor();
   }
}
