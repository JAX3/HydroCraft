package co.uk.mommyheather.futuregenerators.ui;

import co.uk.mommyheather.futuregenerators.config.FutureGeneratorsConfig;
import co.uk.mommyheather.futuregenerators.render.RenderHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.fluids.FluidStack;

public class FluidTankScreen extends AbstractContainerScreen<FluidTankMenu> {
   private static final int FLUID_LEFT = 26;
   private static final int FLUID_WIDTH = 124;
   private static final int FLUID_TOP = 24;
   private static final int FLUID_HEIGHT = 45;
   private final ResourceLocation GUI = new ResourceLocation("futuregenerators", "textures/gui/tank.png");

   public FluidTankScreen(FluidTankMenu menu, Inventory inventory, Component title) {
      super(menu, inventory, title);
      this.imageWidth = 176;
      this.inventoryLabelY = this.imageHeight - 105;
   }

   protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
      super.renderBackground(graphics);
      int relX = (this.width - this.imageWidth) / 2;
      int relY = (this.height - this.imageHeight) / 2;
      graphics.blit(this.GUI, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
      if (!((FluidTankMenu)this.menu).tank.tank.isEmpty()) {
         float percent = (float)((FluidTankMenu)this.menu).tank.tank.getFluidAmount()
            / ((Integer)FutureGeneratorsConfig.SERVER.fluidTankCapacity.get()).intValue();
         int ylevel = (int)(45.0F * percent);
         RenderHelper.renderFluidInGui(
            graphics, this.leftPos + 26, this.topPos + 24 + (45 - ylevel), 124, ylevel, ((FluidTankMenu)this.menu).tank.tank.getFluid().getFluid()
         );
      }

      graphics.blit(this.GUI, this.leftPos + 26, this.topPos + 24, 176, 0, 66, 45);
   }

   public void render(GuiGraphics graphics, int mousex, int mousey, float partialTick) {
      super.render(graphics, mousex, mousey, partialTick);
      FluidStack fluid = ((FluidTankMenu)this.menu).tank.tank.getFluid();
      if (mousex >= this.leftPos + 26 && mousex < this.leftPos + 26 + 124 && mousey >= this.topPos + 24 && mousey < this.topPos + 24 + 45) {
         Component text;
         if (fluid.isEmpty()) {
            text = Component.literal(I18n.get("futuregenerators.ui.empty", new Object[0]));
         } else {
            text = Component.literal(
               I18n.get(fluid.getTranslationKey(), new Object[0])
                  + ": "
                  + fluid.getAmount() / 1000.0F
                  + " / "
                  + ((Integer)FutureGeneratorsConfig.SERVER.fluidTankCapacity.get()).intValue() / 1000.0F
                  + "B"
            );
         }

         graphics.renderTooltip(this.font, text, mousex, mousey);
      } else {
         this.renderTooltip(graphics, mousex, mousey);
      }
   }

   protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
      graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
   }
}
