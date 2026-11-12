package co.uk.mommyheather.futuregenerators.ui;

import co.uk.mommyheather.futuregenerators.config.FutureGeneratorsConfig;
import co.uk.mommyheather.futuregenerators.render.RenderHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

public class PumpScreen extends AbstractContainerScreen<PumpMenu> {
   private static final int FLUID_LEFT = 67;
   private static final int FLUID_WIDTH = 83;
   private static final int FLUID_TOP = 24;
   private static final int FLUID_HEIGHT = 45;
   private static final int ENERGY_LEFT = 10;
   private static final int ENERGY_WIDTH = 12;
   private static final int ENERGY_TOP = 28;
   private static final int ENERGY_HEIGHT = 37;
   private final ResourceLocation GUI = new ResourceLocation("futuregenerators", "textures/gui/pump.png");

   public PumpScreen(PumpMenu menu, Inventory inventory, Component title) {
      super(menu, inventory, title);
      this.imageWidth = 176;
      this.inventoryLabelY = this.imageHeight - 105;
   }

   protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
      super.renderBackground(graphics);
      int relX = (this.width - this.imageWidth) / 2;
      int relY = (this.height - this.imageHeight) / 2;
      graphics.blit(this.GUI, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
      if (!((PumpMenu)this.menu).pump.tank.isEmpty()) {
         float percent = (float)((PumpMenu)this.menu).pump.tank.getFluidAmount()
            / ((Integer)FutureGeneratorsConfig.SERVER.fluidPumpCapacity.get()).intValue();
         int ylevel = (int)(45.0F * percent);
         RenderHelper.renderFluidInGui(
            graphics, this.leftPos + 67, this.topPos + 24 + (45 - ylevel), 83, ylevel, ((PumpMenu)this.menu).pump.tank.getFluid().getFluid()
         );
      }

      graphics.blit(this.GUI, this.leftPos + 67, this.topPos + 24, 176, 0, 66, 45);
      int p = 37
         - (int)(
            (float)((PumpMenu)this.menu).pump.battery.getEnergyStored()
               / ((Integer)FutureGeneratorsConfig.SERVER.fluidPumpBattery.get()).intValue()
               * 37.0F
         );
      graphics.blit(this.GUI, this.leftPos + 10, this.topPos + 28 + p, 176, p + 46, 12, 37 - p);
   }

   public void render(GuiGraphics graphics, int mousex, int mousey, float partialTick) {
      super.render(graphics, mousex, mousey, partialTick);
      FluidStack fluid = ((PumpMenu)this.menu).pump.tank.getFluid();
      if (mousex >= this.leftPos + 67 && mousex < this.leftPos + 67 + 83 && mousey >= this.topPos + 24 && mousey < this.topPos + 24 + 45) {
         Component text;
         if (fluid.isEmpty()) {
            text = Component.literal(I18n.get("futuregenerators.ui.empty", new Object[0]));
         } else {
            text = Component.literal(
               I18n.get(fluid.getTranslationKey(), new Object[0])
                  + ": "
                  + fluid.getAmount() / 1000.0F
                  + " / "
                  + ((Integer)FutureGeneratorsConfig.SERVER.fluidPumpCapacity.get()).intValue() / 1000.0F
                  + "B"
            );
         }

         graphics.renderTooltip(this.font, text, mousex, mousey);
      } else if (mousex >= this.leftPos + 10 && mousex < this.leftPos + 10 + 12 && mousey >= this.topPos + 28 && mousey < this.topPos + 28 + 37) {
         int power = ((PumpMenu)this.menu).pump.battery.getEnergyStored();
         graphics.renderTooltip(this.font, Component.literal(power + " FE"), mousex, mousey);
      } else {
         this.renderTooltip(graphics, mousex, mousey);
      }
   }

   protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
      graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
      if (!((PumpMenu)this.menu).pump.lastPumped.equals(Fluids.EMPTY)) {
         FluidStack fluid = new FluidStack(((PumpMenu)this.menu).pump.lastPumped, 1);
         Component pumping = Component.translatable("futuregenerators.ui.pumping", new Object[]{I18n.get(fluid.getTranslationKey(), new Object[0])});
         graphics.drawString(this.font, pumping, this.imageWidth - this.font.width(pumping) - 5, this.titleLabelY, 4210752, false);
      } else {
         Component pumping = Component.translatable("futuregenerators.ui.pumping_nothing");
         graphics.drawString(this.font, pumping, this.imageWidth - this.font.width(pumping) - this.inventoryLabelX, this.titleLabelY, 4210752, false);
      }
   }
}
