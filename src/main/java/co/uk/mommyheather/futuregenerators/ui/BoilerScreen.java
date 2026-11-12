package co.uk.mommyheather.futuregenerators.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class BoilerScreen extends AbstractContainerScreen<BoilerMenu> {
   private static final int HOT_WATER_LEFT = 136;
   private static final int HOT_WATER_WIDTH = 14;
   private static final int HOT_WATER_TOP = 24;
   private static final int HOT_WATER_HEIGHT = 37;
   private static final int WATER_LEFT = 26;
   private static final int WATER_WIDTH = 14;
   private static final int WATER_TOP = 24;
   private static final int WATER_HEIGHT = 37;
   private static final Identifier GUI = Identifier.fromNamespaceAndPath("futuregenerators", "textures/gui/boiler.png");

   public BoilerScreen(BoilerMenu menu, Inventory inventory, Component title) {
      super(menu, inventory, title, 176, 166);
      this.inventoryLabelY = this.imageHeight - 105;
   }

   @Override
   public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
      super.extractBackground(graphics, mouseX, mouseY, partialTicks);
      int relX = (this.width - this.imageWidth) / 2;
      int relY = (this.height - this.imageHeight) / 2;
      graphics.blit(RenderPipelines.GUI_TEXTURED, GUI, relX, relY, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
      int water = ((BoilerMenu)this.menu).water;
      int hotWater = ((BoilerMenu)this.menu).hotWater;
      int w = 37 - (int)((float)water / ((BoilerMenu)this.menu).waterMax * 37.0F);
      int h = 37 - (int)((float)hotWater / (((BoilerMenu)this.menu).waterMax * 2) * 37.0F);
      graphics.blit(RenderPipelines.GUI_TEXTURED, GUI, this.leftPos + 136, this.topPos + 24 + h, 176, h + 14, 14, 37 - h, 256, 256);
      graphics.blit(RenderPipelines.GUI_TEXTURED, GUI, this.leftPos + 26, this.topPos + 24 + w, 176, w + 51, 14, 37 - w, 256, 256);
      if (((BoilerMenu)this.menu).timeRemaining > 0) {
         graphics.blit(RenderPipelines.GUI_TEXTURED, GUI, this.leftPos + 81, this.topPos + 36, 176, 0, 14, 14, 256, 256);
      }

      Component burning = Component.translatable("futuregenerators.ui.burn_time", new Object[]{String.format("%,d", ((BoilerMenu)this.menu).timeRemaining)});
      graphics.centeredText(this.font, burning, this.width / 2, this.topPos + 24 + 37 + 9 + 1, 0xFFFFFFFF);
   }

   @Override
   protected void extractTooltip(GuiGraphicsExtractor graphics, int mousex, int mousey) {
      if (mousex >= this.leftPos + 136 && mousex < this.leftPos + 136 + 14 && mousey >= this.topPos + 24 && mousey < this.topPos + 24 + 37) {
         int hotWater = ((BoilerMenu)this.menu).hotWater;
         graphics.setTooltipForNextFrame(this.font, Component.literal(hotWater + " mb"), mousex, mousey);
      } else if (mousex >= this.leftPos + 26 && mousex < this.leftPos + 26 + 14 && mousey >= this.topPos + 24 && mousey < this.topPos + 24 + 37) {
         int water = ((BoilerMenu)this.menu).water;
         graphics.setTooltipForNextFrame(this.font, Component.literal(water + " mb"), mousex, mousey);
      } else {
         super.extractTooltip(graphics, mousex, mousey);
      }
   }

   @Override
   protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
      graphics.text(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xFF404040, false);
   }
}
