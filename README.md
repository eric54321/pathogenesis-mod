# Pathogenesis

A cooperative wave-based defense mod for Minecraft built on the [Fabric](https://fabricmc.net/) modloader.

Players fight as immune cells defending a human body against escalating waves of pathogens — bacteria, viruses, fungi, and parasites — across multiple organ stages, from the skin surface to the brain.

## Current State

This mod is in active early development. The systems below are implemented and working.

### Wave System

Enemies spawn in waves every 2 minutes. Enemy count scales with the number of online players:

```
enemies = 5 + (players × 3) + (wave - 1) × 2
```

The wave timer pauses when no players are online. Enemies are distributed round-robin across players so no one is left alone.

### Infection Entry Routes

**Stage 1 — Skin** (waves 1–3)
Pathogens breach the body through the skin surface. The default overworld environment.

| Enemy | HP | Behavior |
|---|---|---|
| Staphylococcus | 8 | Spawns in clusters of 3. Swarm pressure — forces AoE play |
| Streptococcus | 14 | Faster, hits harder. Applies Weakness on hit |
| Dermatophyte | 16 | Slow. Drops a Slowness + Nausea spore cloud on death |

**GI Tract — Food Route** (waves 6+)
Parasites ingested through contaminated food. All three grow larger and more dangerous the longer they survive.

| Enemy | Base HP | Behavior |
|---|---|---|
| Ascaris | 30 | Grows 0.5× scale every 10 sec (max 3.5×). Hits harder, gets slower. Kill fast |
| Taenia | 25 | Adds a segment every 15 sec (+12 HP, +0.15 scale). Sheds segments as sub-enemies on death |
| Strongyloides | 20 | Spawns larvae every 15 sec (max 4 nearby). Both adult and larvae grow over time |

### Other Implemented Enemies

| Enemy | Notes |
|---|---|
| Viron | Fast viral swarm, applies Weakness |
| Influenza | Slower than Viron, applies Nausea + Slowness |
| Coronavirus | Applies Blindness + Weakness |
| Bacteriophage | Slow, high damage, applies Poison. Rare precision threat |
| RogueCell | Cancer cell. Splits into 2 weaker copies on death |

### Items

**CAR-T Injector** — One-use item. Permanently increases attack damage by 4. Cannot be stacked. Based on real CAR-T cell immunotherapy used in cancer treatment.

## Planned Stages

| Stage | Location | Entry Route |
|---|---|---|
| 1 | Skin (surface → epidermis → dermis) | Contact / wound |
| 2 | Lungs | Inhaled pathogens |
| 2b | GI Tract | Ingested food / water |
| 3 | Bloodstream | All routes converge |
| 4 | Liver | Organ infiltration |
| 5 | Heart | Critical systems |
| 6 | Brain | Final stand |

Each stage introduces pathogens specific to that environment. Surviving a stage advances the infection deeper into the body.

## Planned Systems

- **Player roles** — distinct immune cell types (T-cell, B-cell, Macrophage, NK cell) with unique abilities, stored via Fabric `AttachmentType`
- **Host health bar** — shared resource that depletes when pathogens breach a stage barrier; shared across all stages
- **Stage transitions** — clearing a wave threshold advances the infection to the next organ
- **Win / loss conditions** — defend the brain or the host dies

## Tech Stack

- **Minecraft** 1.21.1
- **Fabric Loader** 0.16.5 + **Fabric API** 0.116.11
- **Java 21**
- **Gradle 8.10.2** (Fabric Loom 1.7.4)

## Building

```bash
gradlew build
```

The compiled `.jar` will be in `build/libs/`.

## Running in Dev

```bash
gradlew runClient
```

## Contributing

Open issues and PRs are welcome. Please follow the existing code style and include tests for new game logic where practical.

## License

MIT
