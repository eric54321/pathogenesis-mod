# Pathogenesis Mod — Claude Code Context

## What This Is
A Minecraft Fabric mod (Java 21, MC 1.21.1) — a wave-based cooperative defense game
set inside a human body. Players are immune cells fighting pathogens and cancer cells.
Educational biology is embedded in gameplay mechanics, never in pop-ups.

## Team
- **Dad (Eric)** — scaffolding, wave spawner, build system, new entity registration
- **Son 1** — RogueCell mob (entity, model, renderer)
- **Son 2** — therapy weapons (items)
- **Son 3** — wave spawner logic, new enemy types

## Build & Deploy
```powershell
.\deploy.ps1        # builds and copies JAR to Minecraft mods folder
```
Minecraft mods folder: `%APPDATA%\.minecraft\mods`
Launch profile: **Fabric 1.21.1**

## Project Structure
```
src/main/java/com/pathogenesis/
├── PathogenesisMod.java          — server entrypoint (registration)
├── PathogenesisModClient.java    — client entrypoint (renderers)
├── entity/
│   ├── VirusEntity.java          — abstract base for all viruses
│   ├── VironEntity.java          — fast swarm virus, Weakness debuff on hit
│   └── RogueCellEntity.java      — cancer cell, splits into 2 on death
│   ├── model/                    — Java model classes (generated from Blockbench)
│   └── renderer/                 — entity renderers
├── init/
│   ├── ModEntities.java          — register all entity types HERE
│   ├── ModItems.java             — register all items HERE
│   └── ModModelLayers.java       — register all model layers HERE
├── item/
│   └── CARTInjectorItem.java     — permanent attack damage boost
└── system/
    └── WaveSpawner.java          — 2-min wave timer, scales with player count

src/main/resources/assets/pathogenesis/
├── models/mob/                   — Blockbench .bbmodel source files
├── textures/entity/              — exported PNG textures (one per mob)
└── lang/en_us.json               — display names for all entities and items
```

## Adding a New Enemy
1. Create `entity/YourEntity.java` extending `VirusEntity` (for viruses) or `HostileEntity`
2. Add `ModEntities.YOUR_ENTITY` registration in `ModEntities.java`
3. Add `FabricDefaultAttributeRegistry.register()` call in `ModEntities.register()`
4. Create `entity/model/YourModel.java` and `entity/renderer/YourRenderer.java`
5. Add `ModModelLayers.YOUR_ENTITY` in `ModModelLayers.java`
6. Register model layer and renderer in `PathogenesisModClient.java`
7. Add display name in `lang/en_us.json`
8. Add to `WaveSpawner.spawnEnemies()` so it appears in waves

## Adding a New Virus (Fastest Path)
Extend `VirusEntity` and override only `applyInfectionEffect()`:
```java
@Override
protected void applyInfectionEffect(LivingEntity target) {
    target.addStatusEffect(new StatusEffectInstance(StatusEffects.YOUR_EFFECT, duration, amplifier, false, true));
}
```
Everything else (flying navigation, infection-on-hit, no gravity) is inherited.

## Wave System
- Interval: 2 minutes (2400 ticks)
- Enemy count: `5 + (playerCount × 3) + ((waveNumber - 1) × 2)`
- Enemies spawn 10–20 blocks from each player, distributed round-robin
- `WaveSpawner.getWaveNumber()` — other systems can read the current wave
- `WaveSpawner.reset()` — call when players win

## Blockbench Workflow
1. Open `src/main/resources/assets/pathogenesis/models/mob/NAME.bbmodel` in blockbench.net
2. Edit the model and texture visually
3. Export texture PNG → save to `src/main/resources/assets/pathogenesis/textures/entity/NAME.png`
4. Save updated `.bbmodel` back to the same `models/mob/` folder

## Git Workflow
```powershell
git pull                          # always pull first
# make your changes
git add .
git commit -m "what you changed"
git push
```
**File ownership — edit only your files to avoid conflicts:**
- Dad: `WaveSpawner.java`, `ModEntities.java`, `ModItems.java`, build files
- Son 1: `RogueCellEntity.java`, `RogueCellModel.java`, `RogueCellRenderer.java`
- Son 2: `CARTInjectorItem.java`, new items
- Son 3: new enemy entity files

## Key Versions
- Minecraft: 1.21.1
- Fabric Loader: 0.16.5
- Fabric API: 0.116.11+1.21.1
- Fabric Loom: 1.7.4
- Gradle: 8.10.2
- Java: 21 (Adoptium Temurin — must be Java 21 specifically, not 25)
- `org.gradle.java.home` is pinned in `gradle.properties` to the Java 21 install path

## Biology Reference
Each mechanic maps to real biology — keep this alignment when adding enemies:
| Enemy | Real Biology | In-Game Mechanic |
|---|---|---|
| Viron | Viral particle traveling between cells | Fast swarm, Weakness debuff on hit |
| RogueCell | Cancer cell division | Splits into 2 copies on death |
| (future) Bacterium | Bacterial infection | Slow, tanky, drops antibiotic items |
| (future) Mutagen | Mutagenic agent | Buffs nearby enemies |
