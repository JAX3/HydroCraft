package co.uk.mommyheather.futuregenerators.util;

import co.uk.mommyheather.futuregenerators.tile.Tiles;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/** What each machine exposes to pipes, cables, hoppers and other mods. Every face is the same, as in the original. */
public final class Capabilities {
   private Capabilities() {}

   public static void register(RegisterCapabilitiesEvent event) {
      var fluid = net.neoforged.neoforge.capabilities.Capabilities.Fluid.BLOCK;
      var energy = net.neoforged.neoforge.capabilities.Capabilities.Energy.BLOCK;
      var item = net.neoforged.neoforge.capabilities.Capabilities.Item.BLOCK;

      event.registerBlockEntity(fluid, Tiles.turbine.get(), (be, side) -> be.tank);
      event.registerBlockEntity(energy, Tiles.turbine.get(), (be, side) -> be.battery);

      event.registerBlockEntity(fluid, Tiles.turbineController.get(), (be, side) -> be.tank);
      event.registerBlockEntity(energy, Tiles.turbineController.get(), (be, side) -> be.battery);
      // Casings stand in for their controller once the turbine has formed.
      event.registerBlockEntity(fluid, Tiles.turbineCasing.get(), (be, side) -> be.controller == null ? null : be.controller.tank);
      event.registerBlockEntity(energy, Tiles.turbineCasing.get(), (be, side) -> be.controller == null ? null : be.controller.battery);

      event.registerBlockEntity(fluid, Tiles.boiler.get(), (be, side) -> be.tank);
      event.registerBlockEntity(item, Tiles.boiler.get(), (be, side) -> be.items);

      event.registerBlockEntity(fluid, Tiles.washer.get(), (be, side) -> be.tank);
      event.registerBlockEntity(energy, Tiles.washer.get(), (be, side) -> be.battery);
      // Hoppers and pipes can only take the washed results out, not the cobblestone waiting to be washed.
      event.registerBlockEntity(item, Tiles.washer.get(), (be, side) -> be.items.automation());

      event.registerBlockEntity(energy, Tiles.lightningGenerator.get(), (be, side) -> be.battery);
      event.registerBlockEntity(item, Tiles.lightningGenerator.get(), (be, side) -> be.items);
      event.registerBlockEntity(energy, Tiles.lightningDynamo.get(), (be, side) -> be.battery);

      event.registerBlockEntity(fluid, Tiles.fluidTank.get(), (be, side) -> be.tank);

      event.registerBlockEntity(fluid, Tiles.pump.get(), (be, side) -> be.tank);
      event.registerBlockEntity(energy, Tiles.pump.get(), (be, side) -> be.battery);
      event.registerBlockEntity(item, Tiles.pump.get(), (be, side) -> be.items);

      // Pipes only accept fluid where they touch something that holds fluid; they pass it straight on to the network.
      event.registerBlockEntity(fluid, Tiles.fluidPipe.get(), (be, side) -> be.isConnectedToTank() ? be.tank.of(side) : null);
   }
}
