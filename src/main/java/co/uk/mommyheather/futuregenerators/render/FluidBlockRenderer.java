package co.uk.mommyheather.futuregenerators.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jspecify.annotations.Nullable;

/** Draws the fluid a block holds as a box filling it from the bottom, in proportion to how full it is. */
public abstract class FluidBlockRenderer<T extends BlockEntity> implements BlockEntityRenderer<T, FluidBlockRenderer.State> {
   public static final class State extends BlockEntityRenderState {
      @Nullable TextureAtlasSprite sprite;
      int color;
      float ylevel;
   }

   /** The fluid held. */
   protected abstract FluidStack fluid(T be);

   /** How much it can hold. */
   protected abstract int capacity(T be);

   @Override
   public State createRenderState() {
      return new State();
   }

   @Override
   public void extractRenderState(T be, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
      BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPosition, breakProgress);
      FluidStack fluid = this.fluid(be);
      int capacity = this.capacity(be);
      state.sprite = null;
      if (!fluid.isEmpty() && fluid.getAmount() > 0 && capacity > 0) {
         state.sprite = RenderHelper.still(fluid.getFluid());
         state.color = RenderHelper.tint(fluid.getFluid());
         state.ylevel = (float)fluid.getAmount() / capacity;
      }
   }

   @Override
   public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
      TextureAtlasSprite sprite = state.sprite;
      if (sprite == null) return;
      collector.submitCustomGeometry(poseStack, Sheets.translucentBlockItemSheet(),
         (pose, buffer) -> RenderHelper.renderFluidInWorld(pose, buffer, sprite, state.color, state.ylevel));
   }
}
