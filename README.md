# YSM Equipment Render Patch

**English** | [日本語](README_ja.md)

YSM Equipment Render Patch is a client-side compatibility mod that renders supported equipment correctly on players using [Yes Steve Model](https://www.curseforge.com/minecraft/mc-mods/yes-steve-model).

## Features

- Renders Elytra-compatible chest equipment on YSM players.
- Supports the Draconic Evolution flight module's Elytra setting when that mod is installed.
- Renders supported lanterns equipped in Curios slots, including swing animation.
- Renders Kaleidoscope Doll items equipped in Curios head slots.
- Respects Curios slot visibility settings.
- Uses YSM Mapping API for runtime YSM compatibility instead of bundling private mappings.

Equipment registration and Curios slot availability remain the responsibility of the corresponding equipment mod. This project provides the rendering integration.

## Supported versions

| Minecraft | Loader | Yes Steve Model | Curios | Draconic Evolution | Kaleidoscope Doll | Java |
| --- | --- | --- | --- | --- | --- | --- |
| 1.20.1 | Forge 47.4.20+ | 2.6.0-forge+mc1.20.1+ | 5.14.1+1.20.1+ | 3.1.2.621+ | 1.4.1-forge+mc1.20.1+ | 17 |
| 1.21.1 | NeoForge 21.1.228+ | 2.6.0-neoforge+mc1.21.1+ | 9.5.1+1.21.1+ | 3.1.4.632+ | 1.4.1-neoforge+mc1.21.1+ | 21 |

Source branches are `mc/1.20.1` and `mc/1.21.1`, respectively.

The `main` branch contains shared repository documentation and maintenance policy. Buildable mod sources are maintained on the matching `mc/<minecraft-version>` branch.

## Requirements

Always required:

- Yes Steve Model
- YSM Mapping API

Optional integrations:

- Curios API
- Draconic Evolution
- Kaleidoscope Doll

YSM Mapping API 0.1.0 or newer is required. Curios, Draconic Evolution, and Kaleidoscope Doll are optional; their minimum versions are listed above.

## Installation

1. Download the jar that matches your Minecraft version and mod loader.
2. Install Yes Steve Model and YSM Mapping API.
3. Place all required jars in the client-side `mods/` directory.
4. Install the optional integration mods only for the features you want to use.

This mod is client-side only.

## Building

Check out the branch matching the target Minecraft version, install the Java version listed above, and run:

```bash
./gradlew build
```

On Windows, use `gradlew.bat build`. The generated jar is written to `build/libs/`.

## Credits

The lantern swing behavior is based on [Toni's Immersive Lanterns](https://modrinth.com/mod/immersive-lanterns).

## License

This project is licensed under the MIT License.
