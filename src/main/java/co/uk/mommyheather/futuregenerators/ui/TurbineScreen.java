package co.uk.mommyheather.futuregenerators.ui;

import co.uk.mommyheather.futuregenerators.config.FutureGeneratorsConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
   private final ResourceLocation GUI = new ResourceLocation("futuregenerators", "textures/gui/turbine.png");

   public TurbineScreen(TurbineMenu menu, Inventory inventory, Component title) {
      super(menu, inventory, title);
      this.imageWidth = 176;
      this.inventoryLabelY = this.imageHeight - 105;
   }

   protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
      super.renderBackground(graphics);
      int relX = (this.width - this.imageWidth) / 2;
      int relY = (this.height - this.imageHeight) / 2;
      graphics.blit(this.GUI, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
      int power = ((TurbineMenu)this.menu).power;
      int water = ((TurbineMenu)this.menu).water;
      int p = 37 - (int)((float)power / ((TurbineMenu)this.menu).powerMax * 37.0F);
      int w = 37 - (int)((float)water / ((TurbineMenu)this.menu).waterMax * 37.0F);
      graphics.blit(this.GUI, this.leftPos + 136, this.topPos + 24 + p, 176, p + 14, 12, 37 - p);
      graphics.blit(this.GUI, this.leftPos + 27, this.topPos + 24 + w, 176, w + 51, 14, 37 - w);
      if (((TurbineMenu)this.menu).speed > 0) {
         graphics.blit(this.GUI, this.leftPos + 82, this.topPos + 36, 176, 0, 14, 14);
      }

      Component speed = Component.translatable("futuregenerators.ui.speed", new Object[]{((TurbineMenu)this.menu).speed});
      Component producing = Component.translatable(
         "futuregenerators.ui.production",
         new Object[]{String.format("%,d", ((TurbineMenu)this.menu).speed * (Integer)FutureGeneratorsConfig.SERVER.turbineFeRatio.get())}
      );
      graphics.drawCenteredString(this.font, speed, this.width / 2, this.topPos + 24 + 37, 16777215);
      graphics.drawCenteredString(this.font, producing, this.width / 2, this.topPos + 24 + 37 + 9 + 1, 16777215);
   }

   public void render(GuiGraphics graphics, int mousex, int mousey, float partialTick) {
      super.render(graphics, mousex, mousey, partialTick);
      if (mousex >= this.leftPos + 136 && mousex < this.leftPos + 136 + 12 && mousey >= this.topPos + 24 && mousey < this.topPos + 24 + 37) {
         int power = ((TurbineMenu)this.menu).power;
         graphics.renderTooltip(this.font, Component.literal(power + " FE"), mousex, mousey);
      } else if (mousex >= this.leftPos + 27 && mousex < this.leftPos + 27 + 14 && mousey >= this.topPos + 24 && mousey < this.topPos + 24 + 37) {
         int water = ((TurbineMenu)this.menu).water;
         graphics.renderTooltip(this.font, Component.literal(water + " mb"), mousex, mousey);
      } else {
         this.renderTooltip(graphics, mousex, mousey);
      }
   }

   protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
      graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
   }
}
