package co.uk.mommyheather.futuregenerators.ui;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class Menus {
   public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, "futuregenerators");
   public static final RegistryObject<MenuType<TurbineMenu>> turbine = MENUS.register(
      "turbine", () -> IForgeMenuType.create((window, inv, data) -> new TurbineMenu(window, inv.player, data.readBlockPos()))
   );
   public static final RegistryObject<MenuType<MultiBlockTurbineMenu>> multiblockTurbine = MENUS.register(
      "multiblock_turbine", () -> IForgeMenuType.create((window, inv, data) -> new MultiBlockTurbineMenu(window, inv.player, data.readBlockPos()))
   );
   public static final RegistryObject<MenuType<LightningGeneratorMenu>> lightningGenerator = MENUS.register(
      "lightning_generator", () -> IForgeMenuType.create((window, inv, data) -> new LightningGeneratorMenu(window, inv.player, data.readBlockPos()))
   );
   public static final RegistryObject<MenuType<LightningDynamoMenu>> lightningDynamo = MENUS.register(
      "lightning_dynamo", () -> IForgeMenuType.create((window, inv, data) -> new LightningDynamoMenu(window, inv.player, data.readBlockPos()))
   );
   public static final RegistryObject<MenuType<WasherMenu>> washer = MENUS.register(
      "washer", () -> IForgeMenuType.create((window, inv, data) -> new WasherMenu(window, inv.player, data.readBlockPos()))
   );
   public static final RegistryObject<MenuType<BoilerMenu>> boiler = MENUS.register(
      "boiler", () -> IForgeMenuType.create((window, inv, data) -> new BoilerMenu(window, inv.player, data.readBlockPos()))
   );
   public static final RegistryObject<MenuType<FluidTankMenu>> fluidTank = MENUS.register(
      "fluid_tank", () -> IForgeMenuType.create((window, inv, data) -> new FluidTankMenu(window, inv.player, data.readBlockPos()))
   );
   public static final RegistryObject<MenuType<PumpMenu>> pump = MENUS.register(
      "pump", () -> IForgeMenuType.create((window, inv, data) -> new PumpMenu(window, inv.player, data.readBlockPos()))
   );
}
