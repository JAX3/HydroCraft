package co.uk.mommyheather.futuregenerators.ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;

public class LightningGeneratorScreen extends AbstractContainerScreen<LightningGeneratorMenu> {
   private static final int ENERGY_LEFT = 136;
   private static final int ENERGY_WIDTH = 12;
   private static final int ENERGY_TOP = 24;
   private static final int ENERGY_HEIGHT = 37;
   private final ResourceLocation GUI = new ResourceLocation("futuregenerators", "textures/gui/lightning_generator.png");

   public LightningGeneratorScreen(LightningGeneratorMenu menu, Inventory inventory, Component title) {
      super(menu, inventory, title);
      this.imageWidth = 176;
      this.inventoryLabelY = this.imageHeight - 105;
   }

   protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
      super.renderBackground(graphics);
      int relX = (this.width - this.imageWidth) / 2;
      int relY = (this.height - this.imageHeight) / 2;
      graphics.blit(this.GUI, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
      int power = ((LightningGeneratorMenu)this.menu).power;
      int p = 37 - (int)((float)power / ((LightningGeneratorMenu)this.menu).powerMax * 37.0F);
      graphics.blit(this.GUI, this.leftPos + 136, this.topPos + 24 + p, 176, p + 14, 12, 37 - p);
      Component rod = ((LightningGeneratorMenu)this.menu).hasRod
         ? Component.translatable("futuregenerators.ui.has_rod")
         : Component.translatable("futuregenerators.ui.no_rod").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(16724539)));
      int height = this.topPos + 24 + 3;

      for (FormattedCharSequence line : this.font.split(rod, 90)) {
         graphics.drawString(this.font, line, this.leftPos + 5, height, 16777215);
         height += 9;
      }

      Component dynamos = Component.translatable("futuregenerators.ui.dynamos", new Object[]{((LightningGeneratorMenu)this.menu).dynamos});
      graphics.drawString(this.font, dynamos, this.leftPos + 5, height + 3, 16777215);
   }

   public void render(GuiGraphics graphics, int mousex, int mousey, float partialTick) {
      super.render(graphics, mousex, mousey, partialTick);
      if (mousex >= this.leftPos + 136 && mousex < this.leftPos + 136 + 12 && mousey >= this.topPos + 24 && mousey < this.topPos + 24 + 37) {
         int power = ((LightningGeneratorMenu)this.menu).power;
         graphics.renderTooltip(this.font, Component.literal(power + " FE"), mousex, mousey);
      } else {
         this.renderTooltip(graphics, mousex, mousey);
      }
   }

   protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
      graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
   }
}
