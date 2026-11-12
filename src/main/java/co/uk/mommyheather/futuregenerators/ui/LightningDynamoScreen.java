package co.uk.mommyheather.futuregenerators.ui;

import co.uk.mommyheather.futuregenerators.config.FutureGeneratorsConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class LightningDynamoScreen extends AbstractContainerScreen<LightningDynamoMenu> {
   private static final int ENERGY_LEFT = 136;
   private static final int ENERGY_WIDTH = 12;
   private static final int ENERGY_TOP = 24;
   private static final int ENERGY_HEIGHT = 37;
   private final ResourceLocation GUI = new ResourceLocation("futuregenerators", "textures/gui/lightning_dynamo.png");

   public LightningDynamoScreen(LightningDynamoMenu menu, Inventory inventory, Component title) {
      super(menu, inventory, title);
      this.imageWidth = 176;
      this.imageHeight = 190;
      this.inventoryLabelY = this.imageHeight - 105;
   }

   protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
      super.renderBackground(graphics);
      int relX = (this.width - this.imageWidth) / 2;
      int relY = (this.height - this.imageHeight) / 2;
      graphics.blit(this.GUI, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
      int power = ((LightningDynamoMenu)this.menu).power;
      int p = 37 - (int)((float)power / ((LightningDynamoMenu)this.menu).powerMax * 37.0F);
      graphics.blit(this.GUI, this.leftPos + 136, this.topPos + 24 + p, 176, p + 14, 12, 37 - p);
      int height = this.topPos + 24 + 3;
      Component dynamos = Component.translatable("futuregenerators.ui.generators", new Object[]{((LightningDynamoMenu)this.menu).generators});
      Component producing = Component.translatable(
         "futuregenerators.ui.production",
         new Object[]{
            String.format("%,d", ((LightningDynamoMenu)this.menu).generators * (Integer)FutureGeneratorsConfig.SERVER.lightningDynamoProduction.get())
         }
      );
      graphics.drawString(this.font, dynamos, this.leftPos + 5, height + 3, 16777215);
      height += 9;
      graphics.drawString(this.font, producing, this.leftPos + 5, height + 3, 16777215);
   }

   public void render(GuiGraphics graphics, int mousex, int mousey, float partialTick) {
      super.render(graphics, mousex, mousey, partialTick);
      if (mousex >= this.leftPos + 136 && mousex < this.leftPos + 136 + 12 && mousey >= this.topPos + 24 && mousey < this.topPos + 24 + 37) {
         int power = ((LightningDynamoMenu)this.menu).power;
         graphics.renderTooltip(this.font, Component.literal(power + " FE"), mousex, mousey);
      } else {
         this.renderTooltip(graphics, mousex, mousey);
      }
   }

   protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
      graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
   }
}
