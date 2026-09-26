# Muddy Farmland

Muddy Farmland is a Fabric mod that adds a water-storing, crop-supporting
farmland block made from mud. The block ID is
`muddy-farmland:muddy_farmland`.

## Features

- Adds Muddy Farmland to the Natural Blocks creative tab. It uses the supplied
  muddy side and farmland-top textures, farmland's one-pixel-short collision
  shape, and vanilla mud sounds.
- Supports vanilla Farmland moisture states and the
  `minecraft:supports_crops` block tag. The block can be placed as farmland
  only when its position is valid; if it loses support because of a solid
  block above, it becomes regular mud rather than dirt.
- Prevents player and mob trampling from turning the block into dirt. Normal
  fall damage still applies. Empty fields do not turn into dirt just because
  they have no crop; their vanilla-style moisture level can still decrease.
- Crops extending Minecraft's `CropBlock` class can grow on the block without
  light. Each crop random tick guarantees one age stage, then has an 80% chance
  for each of up to four additional stages. Every stage that actually grows
  costs 300 water units.
- Each block stores up to 20,000 water units. A mature `CropBlock` consumes
  one unit per game tick (0.005% of full capacity). When the reservoir reaches
  zero, the block becomes vanilla Farmland and keeps its moisture state.
- Muddy Farmland blocks connected face-to-face in any of the six directions
  share a water network. If any block in the network detects water fluid in
  the nearby search area (horizontal offsets -4 to +4, vertical offset 0 or
  +1), the network is refilled to full capacity. Water changes and Muddy
  Farmland changes invalidate the cached network so it can be recalculated.
  The search checks loaded chunks only and does not force chunks to load.
- Using a water potion on vanilla Farmland converts it to Muddy Farmland,
  preserves the crop above and Farmland moisture, and returns a glass bottle.
  Using a hoe on mud converts it to Muddy Farmland when the space above is
  clear; the hoe takes one durability point.
- Other mods can extend the conversion targets through the block tags
  `muddy-farmland:water_bottle_convertible` and
  `muddy-farmland:hoe_convertible`.
- Includes block names for German, English, and additional languages.

### Modded crops

The crop-support tag allows compatible mods to plant crops on Muddy Farmland.
The special light-independent growth boost and water-per-growth-stage cost
apply to crops that extend Minecraft's `CropBlock`; other crop implementations
may use their own growth and light rules.

## Supported Minecraft versions

The mod metadata allows Minecraft `26.1` through `26.2`, inclusive. The
project's compatibility build matrix checks these Minecraft/Fabric API pairs:

| Minecraft | Fabric API |
| --- | --- |
| 26.1 | `0.145.1+26.1` |
| 26.1.1 | `0.145.4+26.1.1` |
| 26.1.2 | `0.155.3+26.1.2` |
| 26.2 | `0.161.0+26.2` |

Each matrix job compiles the mod against its matching Minecraft and Fabric API
version. Use Fabric Loader `0.19.5` or newer and Java 25 or newer.

## Build from source

Clone the repository and run the Gradle wrapper:

```shell
./gradlew build
```

On Windows:

```powershell
.\gradlew.bat build
```

The current default build targets Minecraft 26.2. The distributable mod JAR
is written to `dist/`; generated binaries are intentionally not committed.
For a different compatibility-matrix entry, pass matching Gradle properties,
for example:

```shell
./gradlew build -Pminecraft_version=26.1.2 -Pfabric_api_version=0.155.3+26.1.2
```

## Install

Install Fabric Loader for a supported Minecraft version, place the built
`muddy-farmland-1.0.0.jar` and a matching Fabric API JAR in that instance's
`mods` folder, then launch the Fabric profile.

## Source layout

- `src/main/java/` — block registration, interactions, water storage/network,
  and crop/network mixins.
- `src/main/resources/assets/` — block/item models, blockstates, textures,
  and translations.
- `src/main/resources/data/` — crop support and conversion block tags.
- `src/client/` — client entry point and client mixin configuration.
- `.github/workflows/build.yml` — build checks for the supported version
  matrix.

## Lizenz / License

This project is released under **CC0 1.0 Universal**; see [LICENSE](LICENSE).
Contributions are understood to be offered under the same license.

Dieses Projekt steht unter **CC0 1.0 Universal**. Die Lizenzbedingungen
stehen in [LICENSE](LICENSE); Beiträge werden unter derselben Lizenz
entgegengenommen.
