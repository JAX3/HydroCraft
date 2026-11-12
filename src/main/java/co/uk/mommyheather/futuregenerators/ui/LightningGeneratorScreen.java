package co.uk.mommyheather.futuregenerators.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;

public class LightningGeneratorScreen extends AbstractContainerScreen<LightningGeneratorMenu> {
   private static final int ENERGY_LEFT = 136;
   private static final int ENERGY_WIDTH = 12;
   private static final int ENERGY_TOP = 24;
   private static final int ENERGY_HEIGHT = 37;
   private static final Identifier GUI = Identifier.fromNamespaceAndPath("futuregenerators", "textures/gui/lightning_generator.png");

   public LightningGeneratorScreen(LightningGeneratorMenu menu, Inventory inventory, Component title) {
      super(menu, inventory, title, 176, 166);
      this.inventoryLabelY = this.imageHeight - 105;
   }

   @Override
   public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
      super.extractBackground(graphics, mouseX, mouseY, partialTicks);
      int relX = (this.width - this.imageWidth) / 2;
      int relY = (this.height - this.imageHeight) / 2;
      graphics.blit(RenderPipelines.GUI_TEXTURED, GUI, relX, relY, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
      int power = ((LightningGeneratorMenu)this.menu).power;
      int p = 37 - (int)((float)power / ((LightningGeneratorMenu)this.menu).powerMax * 37.0F);
      graphics.blit(RenderPipelines.GUI_TEXTURED, GUI, this.leftPos + 136, this.topPos + 24 + p, 176, p + 14, 12, 37 - p, 256, 256);
      Component rod = ((LightningGeneratorMenu)this.menu).hasRod
         ? Component.translatable("futuregenerators.ui.has_rod")
         : Component.translatable("futuregenerators.ui.no_rod").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(16724539)));
      int height = this.topPos + 24 + 3;

      for (FormattedCharSequence line : this.font.split(rod, 90)) {
         graphics.text(this.font, line, this.leftPos + 5, height, 0xFFFFFFFF);
         height += 9;
      }

      Component dynamos = Component.translatable("futuregenerators.ui.dynamos", new Object[]{((LightningGeneratorMenu)this.menu).dynamos});
      graphics.text(this.font, dynamos, this.leftPos + 5, height + 3, 0xFFFFFFFF);
   }

   @Override
   protected void extractTooltip(GuiGraphicsExtractor graphics, int mousex, int mousey) {
      if (mousex >= this.leftPos + 136 && mousex < this.leftPos + 136 + 12 && mousey >= this.topPos + 24 && mousey < this.topPos + 24 + 37) {
         int power = ((LightningGeneratorMenu)this.menu).power;
         graphics.setTooltipForNextFrame(this.font, Component.literal(power + " FE"), mousex, mousey);
      } else {
         super.extractTooltip(graphics, mousex, mousey);
      }
   }

   @Override
   protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
      graphics.text(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xFF404040, false);
   }
}
