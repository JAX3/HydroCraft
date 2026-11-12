package co.uk.mommyheather.futuregenerators.ui;

import co.uk.mommyheather.futuregenerators.config.FutureGeneratorsConfig;
import co.uk.mommyheather.futuregenerators.render.RenderHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.fluids.FluidStack;

public class FluidTankScreen extends AbstractContainerScreen<FluidTankMenu> {
   private static final int FLUID_LEFT = 26;
   private static final int FLUID_WIDTH = 124;
   private static final int FLUID_TOP = 24;
   private static final int FLUID_HEIGHT = 45;
   private static final Identifier GUI = Identifier.fromNamespaceAndPath("futuregenerators", "textures/gui/tank.png");

   public FluidTankScreen(FluidTankMenu menu, Inventory inventory, Component title) {
      super(menu, inventory, title, 176, 166);
      this.inventoryLabelY = this.imageHeight - 105;
   }

   @Override
   public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
      super.extractBackground(graphics, mouseX, mouseY, partialTicks);
      int relX = (this.width - this.imageWidth) / 2;
      int relY = (this.height - this.imageHeight) / 2;
      graphics.blit(RenderPipelines.GUI_TEXTURED, GUI, relX, relY, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
      if (!((FluidTankMenu)this.menu).tank.tank.isEmpty()) {
         float percent = (float)((FluidTankMenu)this.menu).tank.tank.getFluidAmount()
            / ((Integer)FutureGeneratorsConfig.SERVER.fluidTankCapacity.get()).intValue();
         int ylevel = (int)(45.0F * percent);
         RenderHelper.renderFluidInGui(
            graphics, this.leftPos + 26, this.topPos + 24 + (45 - ylevel), 124, ylevel, ((FluidTankMenu)this.menu).tank.tank.getFluid().getFluid()
         );
      }

      graphics.blit(RenderPipelines.GUI_TEXTURED, GUI, this.leftPos + 26, this.topPos + 24, 176, 0, 66, 45, 256, 256);
   }

   @Override
   protected void extractTooltip(GuiGraphicsExtractor graphics, int mousex, int mousey) {
      FluidStack fluid = ((FluidTankMenu)this.menu).tank.tank.getFluid();
      if (mousex >= this.leftPos + 26 && mousex < this.leftPos + 26 + 124 && mousey >= this.topPos + 24 && mousey < this.topPos + 24 + 45) {
         Component text;
         if (fluid.isEmpty()) {
            text = Component.literal(Component.translatable("futuregenerators.ui.empty").getString());
         } else {
            text = Component.literal(
               fluid.getHoverName().getString()
                  + ": "
                  + fluid.getAmount() / 1000.0F
                  + " / "
                  + ((Integer)FutureGeneratorsConfig.SERVER.fluidTankCapacity.get()).intValue() / 1000.0F
                  + "B"
            );
         }

         graphics.setTooltipForNextFrame(this.font, text, mousex, mousey);
      } else {
         super.extractTooltip(graphics, mousex, mousey);
      }
   }

   @Override
   protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
      graphics.text(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xFF404040, false);
   }
}
