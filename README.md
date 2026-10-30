# Hostile Guns for NeoForge 1.21.1

Adds a configurable chance for eligible hostile mobs to spawn with and use Vic's Point Blank firearms.

## Defaults
- 12% of eligible hostile mobs are armed.
- 32 block maximum firing range.
- 5 base damage.
- 10-18 tick normal firing cadence.
- Distance reduces accuracy.
- Gun selection is dynamic: the live item registry is scanned for Point Blank GunItem instances.
- This is intended to include compatible Point Blank weapon/content packs automatically, including Cyberpunk 2077 Guns for Vic's Point Blank when its weapons register as GunItem instances.
- Modded hostile mobs are supported when they are normal Monster entities or otherwise have an active hostile target.
- Bosses and unusual entities are excluded by default.

## Excluded by default
Ender Dragon, Wither, Warden, Elder Guardian, Guardian, Ravager, Creeper, Ghast, Phantom, Slime, Magma Cube, Shulker, Spider, Cave Spider, Silverfish, Endermite, Blaze, Breeze, Hoglin, Zoglin and Vex.

Edit the generated `config/hostileguns.properties` file after first launch to change the chance, range, damage, cadence, modded-hostile behavior, or exclusion list.

## Requirements
- Minecraft 1.21.1
- NeoForge 21.1.x
- Vic's Point Blank 1.11.1 or newer
- Java 21

## Building
The repository includes a GitHub Actions workflow. Pushing to `main` builds the JAR automatically and uploads it as a workflow artifact.

## Important behavior
The mod equips real Point Blank gun items but uses lightweight server-side hostile-mob firing logic instead of player-input logic. This keeps the hostile weapon pool compatible with third-party Point Blank weapon packs without requiring a custom AI implementation for every gun.
