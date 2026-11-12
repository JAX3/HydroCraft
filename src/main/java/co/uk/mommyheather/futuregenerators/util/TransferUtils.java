package co.uk.mommyheather.futuregenerators.util;

import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.loot.NeoForgeLootContextParams;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.Nullable;

public class TransferUtils {
   /** The order machines push to their neighbours in, as in the original. */
   public static final Direction[] EJECT_ORDER = new Direction[]{
      Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN
   };

   /**
    * Fills or empties a bucket (or any fluid container) in the player's hand from a machine's tank. Returns false when
    * the held item isn't a fluid container.
    */
   public static boolean handleFluidInteraction(Player player, InteractionHand hand, ItemStack itemStack, BlockPos pos, ResourceHandler<FluidResource> tank) {
      if (!isFluidContainer(itemStack)) {
         return false;
      }
      FluidUtil.interactWithFluidHandler(player, hand, pos, tank, null);
      return true;
   }

   /** Whether an item holds fluid (a bucket, for example). */
   public static boolean isFluidContainer(ItemStack stack) {
      return !stack.isEmpty() && ItemAccess.forStack(stack.copyWithCount(1)).getCapability(Capabilities.Fluid.ITEM) != null;
   }

   /** Pushes as much of {@code battery}'s energy as the neighbours take, skipping the positions {@code skip} matches. */
   public static void pushEnergy(Level level, BlockPos pos, FutureGeneratorsEnergyStorage battery, Predicate<BlockPos> skip) {
      for (Direction direction : EJECT_ORDER) {
         if (battery.getEnergyStored() <= 0) return;
         BlockPos target = pos.relative(direction);
         if (skip.test(target)) continue;
         EnergyHandler storage = level.getCapability(Capabilities.Energy.BLOCK, target, direction.getOpposite());
         if (storage == null) continue;
         try (Transaction transaction = Transaction.openRoot()) {
            int energy = storage.insert(battery.getEnergyStored(), transaction);
            if (energy > 0) {
               transaction.commit();
               battery.extractEnergy(energy);
            }
         }
      }
   }

   /** Pushes as much of {@code tank}'s fluid as the neighbours take. */
   public static void pushFluid(Level level, BlockPos pos, FluidTank tank) {
      for (Direction direction : EJECT_ORDER) {
         if (tank.isEmpty()) return;
         ResourceHandler<FluidResource> storage = level.getCapability(Capabilities.Fluid.BLOCK, pos.relative(direction), direction.getOpposite());
         if (storage == null) continue;
         try (Transaction transaction = Transaction.openRoot()) {
            int amount = storage.insert(tank.getFluidResource(), tank.getFluidAmount(), transaction);
            if (amount > 0) {
               transaction.commit();
               tank.drain(amount);
            }
         }
      }
   }

   /** The first fluid {@code handler} holds, or empty. */
   public static FluidResource firstFluid(ResourceHandler<FluidResource> handler) {
      for (int i = 0; i < handler.size(); i++) {
         FluidResource resource = handler.getResource(i);
         if (!resource.isEmpty() && handler.getAmountAsLong(i) > 0) return resource;
      }
      return FluidResource.EMPTY;
   }

   /** Whether an item burns in a furnace. */
   public static boolean isFuel(ItemStack stack) {
      return stack.has(DataComponents.COOKING_FUEL);
   }

   /** How many ticks an item burns for in a furnace (0 if it isn't fuel), worked out the way the furnace does. */
   public static int burnTime(@Nullable Level level, BlockEntity be, ItemStack stack) {
      if (stack.isEmpty() || !isFuel(stack) || !(level instanceof ServerLevel serverLevel)) return 0;
      LootContext context = new LootContext.Builder(
            new LootParams.Builder(serverLevel)
               .withParameter(LootContextParams.BLOCK_STATE, be.getBlockState())
               .withParameter(LootContextParams.BLOCK_ENTITY, be)
               .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(be.getBlockPos()))
               .withParameter(LootContextParams.CONTAINER, new SimpleContainer(stack.copy()))
               .withOptionalParameter(NeoForgeLootContextParams.QUERIED_STACK, stack)
               .create(LootContextParamSets.CONTAINER_PROCESS)
         )
         .create(Optional.empty());
      return ResolvableInt.getFromItem(stack, DataComponents.COOKING_FUEL, CookingFuel::burnTime, context, 0);
   }
}
