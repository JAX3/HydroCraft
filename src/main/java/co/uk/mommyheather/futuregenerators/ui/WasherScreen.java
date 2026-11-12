package co.uk.mommyheather.futuregenerators.ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class WasherScreen extends AbstractContainerScreen<WasherMenu> {
   private static final int ENERGY_LEFT = 143;
   private static final int ENERGY_WIDTH = 12;
   private static final int ENERGY_TOP = 24;
   private static final int ENERGY_HEIGHT = 37;
   private static final int WATER_LEFT = 20;
   private static final int WATER_WIDTH = 14;
   private static final int WATER_TOP = 24;
   private static final int WATER_HEIGHT = 37;
   private final ResourceLocation GUI = new ResourceLocation("futuregenerators", "textures/gui/washer.png");

   public WasherScreen(WasherMenu menu, Inventory inventory, Component title) {
      super(menu, inventory, title);
      this.imageWidth = 176;
      this.inventoryLabelY = this.imageHeight - 105;
   }

   protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
      super.renderBackground(graphics);
      int relX = (this.width - this.imageWidth) / 2;
      int relY = (this.height - this.imageHeight) / 2;
      graphics.blit(this.GUI, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
      int power = ((WasherMenu)this.menu).power;
      int water = ((WasherMenu)this.menu).water;
      int p = 37 - (int)((float)power / ((WasherMenu)this.menu).powerMax * 37.0F);
      int w = 37 - (int)((float)water / ((WasherMenu)this.menu).waterMax * 37.0F);
      graphics.blit(this.GUI, this.leftPos + 143, this.topPos + 24 + p, 176, p + 14, 12, 37 - p);
      graphics.blit(this.GUI, this.leftPos + 20, this.topPos + 24 + w, 176, w + 51, 14, 37 - w);
   }

   public void render(GuiGraphics graphics, int mousex, int mousey, float partialTick) {
      super.render(graphics, mousex, mousey, partialTick);
      if (mousex >= this.leftPos + 143 && mousex < this.leftPos + 143 + 12 && mousey >= this.topPos + 24 && mousey < this.topPos + 24 + 37) {
         int power = ((WasherMenu)this.menu).power;
         graphics.renderTooltip(this.font, Component.literal(power + " FE"), mousex, mousey);
      } else if (mousex >= this.leftPos + 20 && mousex < this.leftPos + 20 + 14 && mousey >= this.topPos + 24 && mousey < this.topPos + 24 + 37) {
         int water = ((WasherMenu)this.menu).water;
         graphics.renderTooltip(this.font, Component.literal(water + " mb"), mousex, mousey);
      } else {
         this.renderTooltip(graphics, mousex, mousey);
      }
   }

   protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
      graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
   }
}
