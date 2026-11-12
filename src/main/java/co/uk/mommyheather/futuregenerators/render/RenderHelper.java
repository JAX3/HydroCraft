package co.uk.mommyheather.futuregenerators.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class RenderHelper {
   private static final float MIN = 0.001F;
   private static final float MAX = 0.999F;

   public static void renderFluidInWorld(
      BlockPos blockPos, float partialTicks, PoseStack stack, MultiBufferSource bufferSource, int packedLightIn, int overlay, Fluid fluid, float ylevel
   ) {
      stack.pushPose();
      IClientFluidTypeExtensions properties = IClientFluidTypeExtensions.of(fluid);
      ResourceLocation texture = properties.getStillTexture();
      TextureAtlasSprite sprite = (TextureAtlasSprite)Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(texture);
      int[] col = splitRGBA(properties.getTintColor());
      VertexConsumer builder = bufferSource.getBuffer(Sheets.translucentCullBlockSheet());
      Matrix4f matrix = stack.last().pose();
      Matrix3f normal = stack.last().normal();
      float minU = sprite.getU(5.0);
      float maxU = sprite.getU(11.0);
      float minV = sprite.getV(5.0);
      float maxV = sprite.getV(11.0);
      ylevel = Math.min(ylevel, 0.999F);
      builder.vertex(matrix, 0.999F, ylevel, 0.001F)
         .color(col[0], col[1], col[2], col[3])
         .uv(minU, minV)
         .overlayCoords(overlay)
         .uv2(240)
         .normal(normal, 0.0F, 1.0F, 0.0F)
         .endVertex();
      builder.vertex(matrix, 0.001F, ylevel, 0.001F)
         .color(col[0], col[1], col[2], col[3])
         .uv(maxU, minV)
         .overlayCoords(overlay)
         .uv2(240)
         .normal(normal, 0.0F, 1.0F, 0.0F)
         .endVertex();
      builder.vertex(matrix, 0.001F, ylevel, 0.999F)
         .color(col[0], col[1], col[2], col[3])
         .uv(maxU, maxV)
         .overlayCoords(overlay)
         .uv2(240)
         .normal(normal, 0.0F, 1.0F, 0.0F)
         .endVertex();
      builder.vertex(matrix, 0.999F, ylevel, 0.999F)
         .color(col[0], col[1], col[2], col[3])
         .uv(minU, maxV)
         .overlayCoords(overlay)
         .uv2(240)
         .normal(normal, 0.0F, 1.0F, 0.0F)
         .endVertex();
      builder.vertex(matrix, 0.001F, 0.001F, 0.001F)
         .color(col[0], col[1], col[2], col[3])
         .uv(minU, minV)
         .overlayCoords(overlay)
         .uv2(240)
         .normal(normal, 0.0F, -1.0F, 0.0F)
         .endVertex();
      builder.vertex(matrix, 0.999F, 0.001F, 0.001F)
         .color(col[0], col[1], col[2], col[3])
         .uv(maxU, minV)
         .overlayCoords(overlay)
         .uv2(240)
         .normal(normal, 0.0F, -1.0F, 0.0F)
         .endVertex();
      builder.vertex(matrix, 0.999F, 0.001F, 0.999F)
         .color(col[0], col[1], col[2], col[3])
         .uv(maxU, maxV)
         .overlayCoords(overlay)
         .uv2(240)
         .normal(normal, 0.0F, -1.0F, 0.0F)
         .endVertex();
      builder.vertex(matrix, 0.001F, 0.001F, 0.999F)
         .color(col[0], col[1], col[2], col[3])
         .uv(minU, maxV)
         .overlayCoords(overlay)
         .uv2(240)
         .normal(normal, 0.0F, -1.0F, 0.0F)
         .endVertex();
      builder.vertex(matrix, 0.999F, 0.001F, 0.001F)
         .color(col[0], col[1], col[2], col[3])
         .uv(minU, minV)
         .overlayCoords(overlay)
         .uv2(240)
         .normal(normal, 0.0F, 0.0F, -1.0F)
         .endVertex();
      builder.vertex(matrix, 0.001F, 0.001F, 0.001F)
         .color(col[0], col[1], col[2], col[3])
         .uv(maxU, minV)
         .overlayCoords(overlay)
         .uv2(240)
         .normal(normal, 0.0F, 0.0F, -1.0F)
         .endVertex();
      builder.vertex(matrix, 0.001F, ylevel, 0.001F)
         .color(col[0], col[1], col[2], col[3])
         .uv(maxU, maxV)
         .overlayCoords(overlay)
         .uv2(240)
         .normal(normal, 0.0F, 0.0F, -1.0F)
         .endVertex();
      builder.vertex(matrix, 0.999F, ylevel, 0.001F)
         .color(col[0], col[1], col[2], col[3])
         .uv(minU, maxV)
         .overlayCoords(overlay)
         .uv2(240)
         .normal(normal, 0.0F, 0.0F, -1.0F)
         .endVertex();
      builder.vertex(matrix, 0.001F, 0.001F, 0.999F)
         .color(col[0], col[1], col[2], col[3])
         .uv(minU, minV)
         .overlayCoords(overlay)
         .uv2(240)
         .normal(normal, 0.0F, 0.0F, 1.0F)
         .endVertex();
      builder.vertex(matrix, 0.999F, 0.001F, 0.999F)
         .color(col[0], col[1], col[2], col[3])
         .uv(maxU, minV)
         .overlayCoords(overlay)
         .uv2(240)
         .normal(normal, 0.0F, 0.0F, 1.0F)
         .endVertex();
      builder.vertex(matrix, 0.999F, ylevel, 0.999F)
         .color(col[0], col[1], col[2], col[3])
         .uv(maxU, maxV)
         .overlayCoords(overlay)
         .uv2(240)
         .normal(normal, 0.0F, 0.0F, 1.0F)
         .endVertex();
      builder.vertex(matrix, 0.001F, ylevel, 0.999F)
         .color(col[0], col[1], col[2], col[3])
         .uv(minU, maxV)
         .overlayCoords(overlay)
         .uv2(240)
         .normal(normal, 0.0F, 0.0F, 1.0F)
         .endVertex();
      builder.vertex(matrix, 0.999F, 0.001F, 0.999F)
         .color(col[0], col[1], col[2], col[3])
         .uv(minU, minV)
         .overlayCoords(overlay)
         .uv2(240)
         .normal(normal, 1.0F, 0.0F, 0.0F)
         .endVertex();
      builder.vertex(matrix, 0.999F, 0.001F, 0.001F)
         .color(col[0], col[1], col[2], col[3])
         .uv(maxU, minV)
         .overlayCoords(overlay)
         .uv2(240)
         .normal(normal, 1.0F, 0.0F, 0.0F)
         .endVertex();
      builder.vertex(matrix, 0.999F, ylevel, 0.001F)
         .color(col[0], col[1], col[2], col[3])
         .uv(maxU, maxV)
         .overlayCoords(overlay)
         .uv2(240)
         .normal(normal, 1.0F, 0.0F, 0.0F)
         .endVertex();
      builder.vertex(matrix, 0.999F, ylevel, 0.999F)
         .color(col[0], col[1], col[2], col[3])
         .uv(minU, maxV)
         .overlayCoords(overlay)
         .uv2(240)
         .normal(normal, 1.0F, 0.0F, 0.0F)
         .endVertex();
      builder.vertex(matrix, 0.001F, 0.001F, 0.001F)
         .color(col[0], col[1], col[2], col[3])
         .uv(minU, minV)
         .overlayCoords(overlay)
         .uv2(240)
         .normal(normal, -1.0F, 0.0F, 0.0F)
         .endVertex();
      builder.vertex(matrix, 0.001F, 0.001F, 0.999F)
         .color(col[0], col[1], col[2], col[3])
         .uv(maxU, minV)
         .overlayCoords(overlay)
         .uv2(240)
         .normal(normal, -1.0F, 0.0F, 0.0F)
         .endVertex();
      builder.vertex(matrix, 0.001F, ylevel, 0.999F)
         .color(col[0], col[1], col[2], col[3])
         .uv(maxU, maxV)
         .overlayCoords(overlay)
         .uv2(240)
         .normal(normal, -1.0F, 0.0F, 0.0F)
         .endVertex();
      builder.vertex(matrix, 0.001F, ylevel, 0.001F)
         .color(col[0], col[1], col[2], col[3])
         .uv(minU, maxV)
         .overlayCoords(overlay)
         .uv2(240)
         .normal(normal, -1.0F, 0.0F, 0.0F)
         .endVertex();
      stack.popPose();
   }

   public static void renderFluidInGui(GuiGraphics guiGraphics, int x, int y, int width, int height, Fluid fluid) {
      IClientFluidTypeExtensions properties = IClientFluidTypeExtensions.of(fluid);
      Matrix4f matrix = guiGraphics.pose().last().pose();
      int[] tint = splitRGBA(properties.getTintColor());
      guiGraphics.setColor(tint[0] / 255.0F, tint[1] / 255.0F, tint[2] / 255.0F, tint[3] / 255.0F);
      TextureAtlasSprite sprite = (TextureAtlasSprite)Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(properties.getStillTexture());
      RenderSystem.setShader(GameRenderer::getPositionTexShader);
      RenderSystem.setShaderTexture(0, sprite.atlasLocation());

      for (int i = 0; i < width; i += 16) {
         for (int j = 0; j < height; j += 16) {
            int drawWidth = Math.min(width - i, 16);
            int drawHeight = (int)Math.min(height - j, 16.0F);
            int drawX = x + i;
            int drawY = y + j;
            float minU = sprite.getU1();
            float maxU = sprite.getU0();
            float minV = sprite.getV1();
            float maxV = sprite.getV0();
            Tesselator tessellator = Tesselator.getInstance();
            BufferBuilder buffer = tessellator.getBuilder();
            buffer.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            buffer.vertex(matrix, drawX, drawY + drawHeight, 0.0F).uv(minU, minV + (maxV - minV) * drawHeight / 16.0F).endVertex();
            buffer.vertex(matrix, drawX + drawWidth, drawY + drawHeight, 0.0F).uv(minU, minV + (maxV - minV) * drawHeight / 16.0F).endVertex();
            buffer.vertex(matrix, drawX + drawWidth, drawY, 0.0F).uv(minU, minV + (maxV - minV) * drawHeight / 16.0F).endVertex();
            buffer.vertex(matrix, drawX, drawY, 0.0F).uv(minU, minV + (maxV - minV) * drawHeight / 16.0F).endVertex();
            buffer.vertex(matrix, drawX, drawY + drawHeight, 0.0F).uv(minU, minV + (maxV - minV) * drawHeight / 16.0F).endVertex();
            buffer.vertex(matrix, drawX + drawWidth, drawY + drawHeight, 0.0F)
               .uv(minU + (maxU - minU) * drawWidth / 16.0F, minV + (maxV - minV) * drawHeight / 16.0F)
               .endVertex();
            buffer.vertex(matrix, drawX + drawWidth, drawY, 0.0F).uv(minU + (maxU - minU) * drawWidth / 16.0F, minV).endVertex();
            buffer.vertex(matrix, drawX, drawY, 0.0F).uv(minU, minV).endVertex();
            BufferUploader.drawWithShader(buffer.end());
         }
      }

      guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   public static int[] splitRGBA(int color) {
      int r = color >> 16 & 0xFF;
      int g = color >> 8 & 0xFF;
      int b = color & 0xFF;
      int a = color >> 24 & 0xFF;
      return new int[]{r, g, b, a};
   }
}
