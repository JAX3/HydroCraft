package co.uk.mommyheather.futuregenerators.ui;

import co.uk.mommyheather.futuregenerators.FutureGenerators;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

public class Menus {
   public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, FutureGenerators.MODID);
   public static final Supplier<MenuType<TurbineMenu>> turbine = MENUS.register(
      "turbine", () -> IMenuTypeExtension.create((window, inv, data) -> new TurbineMenu(window, inv.player, data.readBlockPos()))
   );
   public static final Supplier<MenuType<MultiBlockTurbineMenu>> multiblockTurbine = MENUS.register(
      "multiblock_turbine", () -> IMenuTypeExtension.create((window, inv, data) -> new MultiBlockTurbineMenu(window, inv.player, data.readBlockPos()))
   );
   public static final Supplier<MenuType<LightningGeneratorMenu>> lightningGenerator = MENUS.register(
      "lightning_generator", () -> IMenuTypeExtension.create((window, inv, data) -> new LightningGeneratorMenu(window, inv.player, data.readBlockPos()))
   );
   public static final Supplier<MenuType<LightningDynamoMenu>> lightningDynamo = MENUS.register(
      "lightning_dynamo", () -> IMenuTypeExtension.create((window, inv, data) -> new LightningDynamoMenu(window, inv.player, data.readBlockPos()))
   );
   public static final Supplier<MenuType<WasherMenu>> washer = MENUS.register(
      "washer", () -> IMenuTypeExtension.create((window, inv, data) -> new WasherMenu(window, inv.player, data.readBlockPos()))
   );
   public static final Supplier<MenuType<BoilerMenu>> boiler = MENUS.register(
      "boiler", () -> IMenuTypeExtension.create((window, inv, data) -> new BoilerMenu(window, inv.player, data.readBlockPos()))
   );
   public static final Supplier<MenuType<FluidTankMenu>> fluidTank = MENUS.register(
      "fluid_tank", () -> IMenuTypeExtension.create((window, inv, data) -> new FluidTankMenu(window, inv.player, data.readBlockPos()))
   );
   public static final Supplier<MenuType<PumpMenu>> pump = MENUS.register(
      "pump", () -> IMenuTypeExtension.create((window, inv, data) -> new PumpMenu(window, inv.player, data.readBlockPos()))
   );
}
