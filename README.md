# Pathogenesis

A cooperative wave-based defense mod for Minecraft built on the [Fabric](https://fabricmc.net/) modloader.

## Concept

Four players take on the roles of immune cells — working together inside a stylized human body to fend off waves of pathogens and cancer cells before they overwhelm the host's defenses.

## Gameplay

- **4-player co-op** — each player controls a distinct immune cell type (e.g., T-cell, B-cell, Macrophage, Natural Killer cell), each with unique abilities and stat profiles.
- **Wave-based defense** — enemies spawn in escalating waves. Early waves feature simple bacteria and viruses; later waves introduce mutated cancer cells with special behaviors.
- **Roles & synergies** — players must coordinate their abilities (phagocytosis, antibody release, cytokine signaling) to counter specific threat types.
- **Body arenas** — fights take place inside procedurally themed arenas representing different organs and tissues (bloodstream, lymph node, lung tissue, tumor microenvironment).
- **Resource management** — players collect ATP and signal molecules dropped by defeated pathogens to upgrade abilities between waves.

## Enemy Types

| Enemy | Threat Level | Behavior |
|---|---|---|
| Bacterium | Low | Swarms in large numbers, melee |
| Virus | Medium | Infects and converts nearby entities |
| Fungal Spore | Medium | Spreads AoE debuff clouds |
| Cancer Cell | High | High HP, self-replicates if not killed quickly |
| Boss Pathogen | Extreme | Wave boss with phase transitions |

## Tech Stack

- **Minecraft** 1.21.x
- **Fabric Loader** + **Fabric API**
- **Java 21**
- **Gradle** (Fabric Loom)

## Building

```bash
./gradlew build
```

The compiled `.jar` will be in `build/libs/`.

## Running in Dev

```bash
./gradlew runClient
```

## Contributing

Open issues and PRs are welcome. Please follow the existing code style and include tests for new game logic where practical.

## License

MIT
