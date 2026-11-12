package co.uk.mommyheather.futuregenerators.ui;

import co.uk.mommyheather.futuregenerators.config.FutureGeneratorsConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class LightningDynamoScreen extends AbstractContainerScreen<LightningDynamoMenu> {
   private static final int ENERGY_LEFT = 136;
   private static final int ENERGY_WIDTH = 12;
   private static final int ENERGY_TOP = 24;
   private static final int ENERGY_HEIGHT = 37;
   private static final Identifier GUI = Identifier.fromNamespaceAndPath("futuregenerators", "textures/gui/lightning_dynamo.png");

   public LightningDynamoScreen(LightningDynamoMenu menu, Inventory inventory, Component title) {
      super(menu, inventory, title, 176, 190);
      this.inventoryLabelY = this.imageHeight - 105;
   }

   @Override
   public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
      super.extractBackground(graphics, mouseX, mouseY, partialTicks);
      int relX = (this.width - this.imageWidth) / 2;
      int relY = (this.height - this.imageHeight) / 2;
      graphics.blit(RenderPipelines.GUI_TEXTURED, GUI, relX, relY, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
      int power = ((LightningDynamoMenu)this.menu).power;
      int p = 37 - (int)((float)power / ((LightningDynamoMenu)this.menu).powerMax * 37.0F);
      graphics.blit(RenderPipelines.GUI_TEXTURED, GUI, this.leftPos + 136, this.topPos + 24 + p, 176, p + 14, 12, 37 - p, 256, 256);
      int height = this.topPos + 24 + 3;
      Component dynamos = Component.translatable("futuregenerators.ui.generators", new Object[]{((LightningDynamoMenu)this.menu).generators});
      Component producing = Component.translatable(
         "futuregenerators.ui.production",
         new Object[]{
            String.format("%,d", ((LightningDynamoMenu)this.menu).generators * (Integer)FutureGeneratorsConfig.SERVER.lightningDynamoProduction.get())
         }
      );
      graphics.text(this.font, dynamos, this.leftPos + 5, height + 3, 0xFFFFFFFF);
      height += 9;
      graphics.text(this.font, producing, this.leftPos + 5, height + 3, 0xFFFFFFFF);
   }

   @Override
   protected void extractTooltip(GuiGraphicsExtractor graphics, int mousex, int mousey) {
      if (mousex >= this.leftPos + 136 && mousex < this.leftPos + 136 + 12 && mousey >= this.topPos + 24 && mousey < this.topPos + 24 + 37) {
         int power = ((LightningDynamoMenu)this.menu).power;
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
