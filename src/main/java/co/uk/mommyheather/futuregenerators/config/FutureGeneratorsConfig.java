package co.uk.mommyheather.futuregenerators.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;
import org.apache.commons.lang3.tuple.Pair;

/**
 * The mod's settings. Registered as a synced config (one per world, in {@code <world>/syncedconfig/}), so the server's
 * values also reach the client, where the screens read them.
 */
public class FutureGeneratorsConfig {
   public static final FutureGeneratorsConfig.Server SERVER;
   public static final ModConfigSpec SERVER_SPEC;

   static {
      Pair<FutureGeneratorsConfig.Server, ModConfigSpec> server = new Builder().configure(FutureGeneratorsConfig.Server::new);
      SERVER = server.getLeft();
      SERVER_SPEC = server.getRight();
   }

   public static class Server {
      public final ConfigValue<Integer> turbineMaxSpeed;
      public final ConfigValue<Integer> turbineFeRatio;
      public final ConfigValue<Integer> turbineSpinupTime;
      public final ConfigValue<Integer> turbineWaterCapacity;
      public final ConfigValue<Integer> turbinePowerCapacity;
      public final ConfigValue<Integer> multiblockTurbineMaxSpeed;
      public final ConfigValue<Integer> multiblockTurbineFeRatio;
      public final ConfigValue<Integer> multiblockTurbineSpinupTime;
      public final ConfigValue<Integer> multiblockTurbineWaterCapacity;
      public final ConfigValue<Integer> multiblockTurbinePowerCapacity;
      public final ConfigValue<Integer> lightningGeneratorConsumption;
      public final ConfigValue<Integer> lightningGeneratorCapacity;
      public final ConfigValue<Integer> lightningDynamoProduction;
      public final ConfigValue<Integer> lightningDynamoCapacity;
      public final ConfigValue<Integer> lightningDynamoMaxGenerators;
      public final ConfigValue<Integer> washerPowerCapacity;
      public final ConfigValue<Integer> washerPowerConsumption;
      public final ConfigValue<Integer> washerWaterCapacity;
      public final ConfigValue<Integer> washerWaterConsumption;
      public final ConfigValue<Integer> washerProcessTime;
      public final ConfigValue<Integer> boilerWaterCapacity;
      public final ConfigValue<Integer> boilerHeatingRate;
      public final ConfigValue<Integer> fluidTankCapacity;
      public final ConfigValue<Integer> fluidPumpCapacity;
      public final ConfigValue<Integer> fluidPumpRange;
      public final ConfigValue<Boolean> drainWater;
      public final ConfigValue<Integer> fluidPumpConsumption;
      public final ConfigValue<Integer> fluidPumpBattery;
      public final ConfigValue<Double> fluidPumpConversion;
      public final ConfigValue<Integer> fluidPumpTime;
      public final ConfigValue<Integer> fluidPipeFrequency;
      public final ConfigValue<Integer> fluidPipeRate;

      public Server(Builder builder) {
         builder.push("Boiler");
         this.boilerWaterCapacity = builder.comment("The water capacity of the boiler. The hot water capacity is 2x this.")
            .defineInRange("boilerWaterCapacity", 8000, Integer.MIN_VALUE, 1073741823);
         this.boilerHeatingRate = builder.comment("How much water the boiler converts into hot water each tick whilst running.")
            .defineInRange("boilerHeatingRate", 100, 1, Integer.MAX_VALUE);
         builder.pop();
         builder.push("Turbine");
         this.turbineMaxSpeed = builder.comment("The max speed (mb/t consumption) the turbine can reach.")
            .defineInRange("maxTurbineSpeed", 200, 1, Integer.MAX_VALUE);
         this.turbineFeRatio = builder.comment("How much FE the turbine makes for every mb of water it consumes.")
            .defineInRange("turbineFERatio", 4, 1, Integer.MAX_VALUE);
         this.turbineSpinupTime = builder.comment(
               "How long the turbine takes to spin up from still to max speed, in seconds.\nMinimum speed gain is 1 per tick / 20 per second!"
            )
            .defineInRange("turbineSpinupTime", 60, 0, Integer.MAX_VALUE);
         this.turbineWaterCapacity = builder.comment("How much water the turbine can hold.")
            .defineInRange("turbineWaterCapacity", 10000, 1000, Integer.MAX_VALUE);
         this.turbinePowerCapacity = builder.comment("How much power the turbine can hold.")
            .defineInRange("turbinePowerCapacity", 10000, 1000, Integer.MAX_VALUE);
         builder.pop();
         builder.push("MultiblockTurbine");
         this.multiblockTurbineMaxSpeed = builder.comment("The max speed (mb/t consumption) the multiblock turbine can reach.")
            .defineInRange("multiblockTurbineMaxSpeed", 1000, 1, Integer.MAX_VALUE);
         this.multiblockTurbineFeRatio = builder.comment("How much FE the turbine makes for every mb of water it consumes.")
            .defineInRange("multiblockTurbineFeRatio", 6, 1, Integer.MAX_VALUE);
         this.multiblockTurbineSpinupTime = builder.comment(
               "How long the turbine takes to spin up from still to max speed, in seconds.\nMinimum speed gain is 1 per tick / 20 per second!"
            )
            .defineInRange("multiblockTurbineSpinupTime", 100, 0, Integer.MAX_VALUE);
         this.multiblockTurbineWaterCapacity = builder.comment("How much water the turbine can hold.")
            .defineInRange("multiblockTurbineWaterCapacity", 50000, 1000, Integer.MAX_VALUE);
         this.multiblockTurbinePowerCapacity = builder.comment("How much power the turbine can hold.")
            .defineInRange("multiblockTurbinePowerCapacity", 50000, 1000, Integer.MAX_VALUE);
         builder.pop();
         builder.push("LightningGenerator");
         this.lightningGeneratorConsumption = builder.comment("The power consumption of the lightning generator.")
            .defineInRange("lightningGeneratorConsumption", 1000, 1, Integer.MAX_VALUE);
         this.lightningGeneratorCapacity = builder.comment("The lightning generator's power capacity.")
            .defineInRange("lightningGeneratorCapacity", 100000, 1, Integer.MAX_VALUE);
         builder.pop();
         builder.push("LightningDynamo");
         this.lightningDynamoProduction = builder.comment("How much power each dynamo produces per connected lightning generator.")
            .defineInRange("lightningDynamoProduction", 10000, 1, Integer.MAX_VALUE);
         this.lightningDynamoCapacity = builder.comment("How much power the lightning dynamo can store.")
            .defineInRange("lightningDynamoCapacity", 10000000, 1, Integer.MAX_VALUE);
         this.lightningDynamoMaxGenerators = builder.comment("How many lightning generators a single dynamo can connect to.")
            .defineInRange("lightningDynamoMaxGenerators", 4, 1, 4);
         builder.pop();
         builder.push("Washer");
         this.washerPowerCapacity = builder.comment("How much power the washer can hold.").defineInRange("washerPowerCapacity", 10000, 1000, Integer.MAX_VALUE);
         this.washerPowerConsumption = builder.comment("How much power is consumed per cobblestone 'washed'.")
            .defineInRange("washerPowerConsumption", 1000, 1, Integer.MAX_VALUE);
         this.washerWaterCapacity = builder.comment("How much water the washer can hold.").defineInRange("washerWaterCapacity", 10000, 1000, Integer.MAX_VALUE);
         this.washerWaterConsumption = builder.comment("How much water is consumed per cobblestone 'washed'.")
            .defineInRange("washerWaterConsumption", 250, 1, Integer.MAX_VALUE);
         this.washerProcessTime = builder.comment("How many ticks it takes to 'wash' a single block of cobblestone.")
            .defineInRange("washerProcessTime", 5, 1, Integer.MAX_VALUE);
         builder.pop();
         builder.push("Tank");
         this.fluidTankCapacity = builder.comment("How much fluid the tank can store.").defineInRange("fluidTankCapacity", 100000, 1000, Integer.MAX_VALUE);
         builder.pop();
         builder.push("Pump");
         this.fluidPumpCapacity = builder.comment("How much fluid the pump can store.").defineInRange("pumpFluidCapacity", 10000, 1000, Integer.MAX_VALUE);
         this.fluidPumpRange = builder.comment("The search range for the pump. Higher values perform worse.").defineInRange("pumpRange", 32, 1, 512);
         this.drainWater = builder.comment("Whether or not the fluid pump should drain water. Pumps infinitely if disabled.").define("pumpDrainsWater", false);
         this.fluidPumpConsumption = builder.comment("The power consumption of the fluid pump per block pumped. Must be lower than the capacity!")
            .defineInRange("pumpPowerConsumption", 500, 0, Integer.MAX_VALUE);
         this.fluidPumpBattery = builder.comment("The power capacity of the fluid pump.").defineInRange("pumpPowerCapacity", 10000, 1000, Integer.MAX_VALUE);
         this.fluidPumpConversion = builder.comment(
               "The power conversion per tick of burn time the inserted fuel has. The resulting power is rounded to the nearest whole number!"
            )
            .defineInRange("pumpPowerConversion", 1.0, 0.001, Double.MAX_VALUE);
         this.fluidPumpTime = builder.comment("How frequently the pump can operate. 1 = every tick!")
            .defineInRange("pumpOperationTime", 5, 1, Integer.MAX_VALUE);
         builder.pop();
         builder.push("Pipe");
         this.fluidPipeFrequency = builder.comment("How often pipes can pull from inventories. 1 = every tick!")
            .defineInRange("fluidPipeFrequency", 3, 1, Integer.MAX_VALUE);
         this.fluidPipeRate = builder.comment("How much fluid a pipe will pull with each operation.")
            .defineInRange("fluidPipeRate", 1000, 1, Integer.MAX_VALUE);
         builder.pop();
      }
   }
}
