package co.uk.mommyheather.futuregenerators.ui;

import co.uk.mommyheather.futuregenerators.config.FutureGeneratorsConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class TurbineScreen extends AbstractContainerScreen<TurbineMenu> {
   private static final int ENERGY_LEFT = 136;
   private static final int ENERGY_WIDTH = 12;
   private static final int ENERGY_TOP = 24;
   private static final int ENERGY_HEIGHT = 37;
   private static final int WATER_LEFT = 27;
   private static final int WATER_WIDTH = 14;
   private static final int WATER_TOP = 24;
   private static final int WATER_HEIGHT = 37;
   private static final Identifier GUI = Identifier.fromNamespaceAndPath("futuregenerators", "textures/gui/turbine.png");

   public TurbineScreen(TurbineMenu menu, Inventory inventory, Component title) {
      super(menu, inventory, title, 176, 166);
      this.inventoryLabelY = this.imageHeight - 105;
   }

   @Override
   public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
      super.extractBackground(graphics, mouseX, mouseY, partialTicks);
      int relX = (this.width - this.imageWidth) / 2;
      int relY = (this.height - this.imageHeight) / 2;
      graphics.blit(RenderPipelines.GUI_TEXTURED, GUI, relX, relY, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
      int power = ((TurbineMenu)this.menu).power;
      int water = ((TurbineMenu)this.menu).water;
      int p = 37 - (int)((float)power / ((TurbineMenu)this.menu).powerMax * 37.0F);
      int w = 37 - (int)((float)water / ((TurbineMenu)this.menu).waterMax * 37.0F);
      graphics.blit(RenderPipelines.GUI_TEXTURED, GUI, this.leftPos + 136, this.topPos + 24 + p, 176, p + 14, 12, 37 - p, 256, 256);
      graphics.blit(RenderPipelines.GUI_TEXTURED, GUI, this.leftPos + 27, this.topPos + 24 + w, 176, w + 51, 14, 37 - w, 256, 256);
      if (((TurbineMenu)this.menu).speed > 0) {
         graphics.blit(RenderPipelines.GUI_TEXTURED, GUI, this.leftPos + 82, this.topPos + 36, 176, 0, 14, 14, 256, 256);
      }

      Component speed = Component.translatable("futuregenerators.ui.speed", new Object[]{((TurbineMenu)this.menu).speed});
      Component producing = Component.translatable(
         "futuregenerators.ui.production",
         new Object[]{String.format("%,d", ((TurbineMenu)this.menu).speed * (Integer)FutureGeneratorsConfig.SERVER.turbineFeRatio.get())}
      );
      graphics.centeredText(this.font, speed, this.width / 2, this.topPos + 24 + 37, 0xFFFFFFFF);
      graphics.centeredText(this.font, producing, this.width / 2, this.topPos + 24 + 37 + 9 + 1, 0xFFFFFFFF);
   }

   @Override
   protected void extractTooltip(GuiGraphicsExtractor graphics, int mousex, int mousey) {
      if (mousex >= this.leftPos + 136 && mousex < this.leftPos + 136 + 12 && mousey >= this.topPos + 24 && mousey < this.topPos + 24 + 37) {
         int power = ((TurbineMenu)this.menu).power;
         graphics.setTooltipForNextFrame(this.font, Component.literal(power + " FE"), mousex, mousey);
      } else if (mousex >= this.leftPos + 27 && mousex < this.leftPos + 27 + 14 && mousey >= this.topPos + 24 && mousey < this.topPos + 24 + 37) {
         int water = ((TurbineMenu)this.menu).water;
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
