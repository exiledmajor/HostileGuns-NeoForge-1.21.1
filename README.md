# Hostile Guns for NeoForge 1.21.1

Adds a configurable chance for eligible hostile mobs to spawn with and use Vic's Point Blank firearms.

## Combat behavior
- Default 12% chance for eligible hostile mobs to spawn armed.
- Works with vanilla and modded hostile entities registered in the Monster mob category.
- Excludes bosses and unusual mobs by default (Ender Dragon, Wither, Warden, etc.).
- NPCs use actual moving projectile entities rather than instant, guaranteed hits.
- Visible muzzle flash, moving tracer trail, and impact particles are generated independently of the player weapon renderer.
- NPCs take a short aim delay when acquiring a target.
- Accuracy varies by weapon category, distance, and each mob's individual aim skill.
- Weapon choice is weighted by estimated rarity, so common pistols and SMGs are much more likely than sniper, heavy, launcher, and exotic weapons.
- Shot cadence varies per shot and by weapon category. If a gun exposes RPM through an accessor, that value is used; otherwise category-specific intervals are used.
- Shotguns fire a small multi-pellet spread.

## Weapon compatibility
The mod scans the item registry for Vic's Point Blank GunItem instances. It does not bundle gun assets and does not hardcode a fixed weapon list. Compatible third-party Point Blank content packs can join the pool automatically when their guns register as GunItem instances.

## Configuration
After first launch, edit config/hostileguns.properties.

Important settings:
- armedHostileChance=0.12
- accuracyMultiplier=1.0 (higher = less accurate)
- minAimDelayTicks=5 and maxAimDelayTicks=12
- muzzleFlashEffects=true
- tracerEffects=true
- impactEffects=true
- rarityWeightCommon=9.0
- rarityWeightUncommon=3.0
- rarityWeightRare=0.75
- rarityWeightExotic=0.15

Rarity weights are relative, not fixed percentages. Each candidate gun also receives a small random selection variance. Gun category and rarity are inferred from registry identifiers, so unusual content-pack naming may be classified as common/unknown until its name is recognized.

## Excluded by default
Ender Dragon, Wither, Warden, Elder Guardian, Guardian, Ravager, Creeper, Ghast, Phantom, Slime, Magma Cube, Shulker, Spider, Cave Spider, Silverfish, Endermite, Blaze, Breeze, Hoglin, Zoglin and Vex.

## Requirements
- Minecraft 1.21.1
- NeoForge 21.1.x
- Vic's Point Blank 1.11.1 or newer
- Java 21

## Build
GitHub Actions builds the mod on every push to the default branch and uploads the JAR as a workflow artifact.
