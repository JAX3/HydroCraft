package co.uk.mommyheather.futuregenerators.ui;

import co.uk.mommyheather.futuregenerators.config.FutureGeneratorsConfig;
import co.uk.mommyheather.futuregenerators.render.RenderHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

public class PumpScreen extends AbstractContainerScreen<PumpMenu> {
   private static final int FLUID_LEFT = 67;
   private static final int FLUID_WIDTH = 83;
   private static final int FLUID_TOP = 24;
   private static final int FLUID_HEIGHT = 45;
   private static final int ENERGY_LEFT = 10;
   private static final int ENERGY_WIDTH = 12;
   private static final int ENERGY_TOP = 28;
   private static final int ENERGY_HEIGHT = 37;
   private static final Identifier GUI = Identifier.fromNamespaceAndPath("futuregenerators", "textures/gui/pump.png");

   public PumpScreen(PumpMenu menu, Inventory inventory, Component title) {
      super(menu, inventory, title, 176, 166);
      this.inventoryLabelY = this.imageHeight - 105;
   }

   @Override
   public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
      super.extractBackground(graphics, mouseX, mouseY, partialTicks);
      int relX = (this.width - this.imageWidth) / 2;
      int relY = (this.height - this.imageHeight) / 2;
      graphics.blit(RenderPipelines.GUI_TEXTURED, GUI, relX, relY, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
      if (!((PumpMenu)this.menu).pump.tank.isEmpty()) {
         float percent = (float)((PumpMenu)this.menu).pump.tank.getFluidAmount()
            / ((Integer)FutureGeneratorsConfig.SERVER.fluidPumpCapacity.get()).intValue();
         int ylevel = (int)(45.0F * percent);
         RenderHelper.renderFluidInGui(
            graphics, this.leftPos + 67, this.topPos + 24 + (45 - ylevel), 83, ylevel, ((PumpMenu)this.menu).pump.tank.getFluid().getFluid()
         );
      }

      graphics.blit(RenderPipelines.GUI_TEXTURED, GUI, this.leftPos + 67, this.topPos + 24, 176, 0, 66, 45, 256, 256);
      int p = 37
         - (int)(
            (float)((PumpMenu)this.menu).pump.battery.getEnergyStored()
               / ((Integer)FutureGeneratorsConfig.SERVER.fluidPumpBattery.get()).intValue()
               * 37.0F
         );
      graphics.blit(RenderPipelines.GUI_TEXTURED, GUI, this.leftPos + 10, this.topPos + 28 + p, 176, p + 46, 12, 37 - p, 256, 256);
   }

   @Override
   protected void extractTooltip(GuiGraphicsExtractor graphics, int mousex, int mousey) {
      FluidStack fluid = ((PumpMenu)this.menu).pump.tank.getFluid();
      if (mousex >= this.leftPos + 67 && mousex < this.leftPos + 67 + 83 && mousey >= this.topPos + 24 && mousey < this.topPos + 24 + 45) {
         Component text;
         if (fluid.isEmpty()) {
            text = Component.literal(Component.translatable("futuregenerators.ui.empty").getString());
         } else {
            text = Component.literal(
               fluid.getHoverName().getString()
                  + ": "
                  + fluid.getAmount() / 1000.0F
                  + " / "
                  + ((Integer)FutureGeneratorsConfig.SERVER.fluidPumpCapacity.get()).intValue() / 1000.0F
                  + "B"
            );
         }

         graphics.setTooltipForNextFrame(this.font, text, mousex, mousey);
      } else if (mousex >= this.leftPos + 10 && mousex < this.leftPos + 10 + 12 && mousey >= this.topPos + 28 && mousey < this.topPos + 28 + 37) {
         int power = ((PumpMenu)this.menu).pump.battery.getEnergyStored();
         graphics.setTooltipForNextFrame(this.font, Component.literal(power + " FE"), mousex, mousey);
      } else {
         super.extractTooltip(graphics, mousex, mousey);
      }
   }

   @Override
   protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
      graphics.text(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xFF404040, false);
      if (!((PumpMenu)this.menu).pump.lastPumped.equals(Fluids.EMPTY)) {
         FluidStack fluid = new FluidStack(((PumpMenu)this.menu).pump.lastPumped, 1);
         Component pumping = Component.translatable("futuregenerators.ui.pumping", new Object[]{fluid.getHoverName().getString()});
         graphics.text(this.font, pumping, this.imageWidth - this.font.width(pumping) - 5, this.titleLabelY, 0xFF404040, false);
      } else {
         Component pumping = Component.translatable("futuregenerators.ui.pumping_nothing");
         graphics.text(this.font, pumping, this.imageWidth - this.font.width(pumping) - this.inventoryLabelX, this.titleLabelY, 0xFF404040, false);
      }
   }
}
