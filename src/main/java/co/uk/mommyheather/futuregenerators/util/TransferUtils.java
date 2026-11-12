package co.uk.mommyheather.futuregenerators.util;

import java.util.Optional;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class TransferUtils {
   public static boolean handleFluidInteraction(Player player, InteractionHand hand, ItemStack itemStack, IFluidTank tank) {
      ItemStack stack = itemStack.copyWithCount(1);
      Optional<IFluidHandlerItem> handlerOptional = FluidUtil.getFluidHandler(stack).resolve();
      if (!stack.isEmpty() && handlerOptional.isPresent()) {
         IFluidHandlerItem handler = handlerOptional.get();
         if (!handler.getFluidInTank(0).isEmpty() && tank.getFluidAmount() < tank.getCapacity()) {
            FluidStack fluidStack = handler.drain(tank.getCapacity(), FluidAction.SIMULATE);
            int drained = tank.fill(fluidStack, FluidAction.EXECUTE);
            handler.drain(drained, FluidAction.EXECUTE);
         } else {
            FluidStack fluidStack = tank.drain(handler.getTankCapacity(0), FluidAction.SIMULATE);
            int drained = handler.fill(fluidStack, FluidAction.EXECUTE);
            tank.drain(drained, FluidAction.EXECUTE);
         }

         ItemStack container = handler.getContainer().copy();
         itemStack.shrink(1);
         if (itemStack.isEmpty()) {
            player.setItemInHand(hand, container);
         } else if (!player.getInventory().add(container)) {
            player.drop(container, false, true);
         }

         return true;
      } else {
         return false;
      }
   }
}
