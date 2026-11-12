# HydroCraft

A small Forge mod for Minecraft 1.20.1 that adds a few different generators. It includes:
- a boiler, turbine and multiblock turbine (controller and casing);
- a lightning generator and lightning dynamo;
- a pump, a washer, fluid pipes and a fluid tank;
- a hot water fluid.

Mod ID `futuregenerators`, version 1.5.1. Authors Heather White and clown_95x. Licensed BSD 3-Clause.

## Where this source came from

The original project folder was lost. This source was recovered from the built jar, `HydroCraft-1.5.11.jar`, in four steps:

1. The classes were decompiled with Vineflower 1.12.
2. The obfuscated SRG names (`m_129843_`, `f_...`) were mapped back to Mojang's official names, using MCP config 1.20.1-20230612.114412 and Mojang's 1.20.1 client mappings.
3. Assets, data, `pack.mcmeta` and `META-INF/mods.toml` were copied over unchanged.
4. The Gradle build was recreated. It uses ModDevGradle's legacy Forge plugin with Forge 1.20.1-47.4.29.

The rebuilt jar contains exactly the same files as the original. On build it is converted back to SRG names, so it runs on a normal Forge 1.20.1 install.

**What could not be recovered:**
- comments;
- parameter names on overridden Minecraft methods, which show as `p_12345_`. Minecraft 1.20.1's official mappings don't include them.

Local variable names did survive, because the jar kept its debug info.

**Known issue carried over from the original:** `assets/futuregenerators/models/block/hot_water.json` uses the texture `futuregenerators:block/hot_water_still`, but the only texture is `hot_water.png`. The game logs a missing-texture warning for the hot water block.

## Building

The build needs Java 17.

```bash
./gradlew build
```

The jar is written to `build/libs/`. To test in game:

```bash
./gradlew runClient
```

```bash
./gradlew runServer
```

The server needs `run/eula.txt` to contain `eula=true`.
