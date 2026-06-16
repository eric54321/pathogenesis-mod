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

The wave timer pauses when no players are online. Enemies are distributed round-robin across players so no one is left alone. A countdown timer is shown in the action bar above the hotbar.

### Host Health

A shared boss bar at the top of the screen shows the host body's health (100 HP max). Every time a pathogen hits a player it drains 5 HP from the host. The bar turns yellow below 60 HP and red below 30 HP.

- Host HP reaches 0 → game over, waves reset
- Survive all 10 waves → victory

### Stage Atmosphere

Each stage has distinct ambient particle effects and a title card announcement:

| Waves | Stage | Atmosphere |
|---|---|---|
| 1–3 | Epidermis | Fleshy pink dust particles |
| 4–6 | Dermis | Deeper rose-pink particles |
| 7–9 | Bloodstream | Blood-red particles |
| 10+ | Deep Tissue | Dark crimson particles |

### Infection Entry Routes

**Stage 1 — Skin** (waves 1–3)
Pathogens breach the body through the skin surface.

| Enemy | HP | Behavior |
|---|---|---|
| Staphylococcus | 8 | Spawns in clusters of 3. Forces AoE play |
| Streptococcus | 14 | Faster, hits harder. Applies Weakness on hit |
| Dermatophyte | 16 | Slow. Drops a Slowness + Nausea spore cloud on death |

**GI Tract — Food Route** (waves 6+)
Parasites ingested through contaminated food. All three grow larger the longer they survive.

| Enemy | Base HP | Behavior |
|---|---|---|
| Ascaris | 30 | Grows 0.5× scale every 10 sec (max 3.5×). Hits harder, gets slower |
| Taenia | 25 | Adds a segment every 15 sec (+12 HP, +0.15 scale). Sheds segments as sub-enemies on death |
| Strongyloides | 20 | Spawns larvae every 15 sec (max 4 nearby). Both adult and larvae grow over time |

**Viral / Other Enemies**

| Enemy | Notes |
|---|---|
| Viron | Fast viral swarm, applies Weakness |
| Influenza | Slower Viron variant, applies Nausea + Slowness |
| Coronavirus | Applies Blindness + Weakness |
| Bacteriophage | Slow, high damage, applies Poison. Rare precision threat |
| RogueCell | Cancer cell. Splits into 2 weaker copies on death |

### Skin Terrain

On first world load, the game generates a 600×600 block fleshy skin surface centered on spawn. All normal Minecraft terrain in a 1000×1000 ring around it is wiped to void so the skin sits alone.

The terrain is layered like real skin:

| Layer | Blocks | Depth |
|---|---|---|
| Epidermis (surface) | Pink terracotta + pink concrete powder | Top |
| Dermis | Red terracotta | −1 to −2 |
| Subcutaneous fat | Yellow terracotta + honey blocks | −3 to −6 |

Surface features scattered across the terrain:

- **Hair follicles** — brown terracotta pillars 3–7 blocks tall, some topped with dead bush tufts
- **Pores** — small 1×1 and 2×2 holes punched through to the dermis below
- **Infection wounds** — crimson nylium patches with crimson roots and magma cores, marking where pathogens entered
- **Sebaceous glands** — hollow slime block chambers just below the surface
- **Surface veins** — red concrete lines running N–S and E–W across the terrain
- **Sweat gland openings** — cyan terracotta holes in the surface
- **Cell nuclei** — sea lanterns glowing deep below the surface, visible through pores at night

### Pathogen Lair & Boss

At wave 5, players receive a **Pathogen Tracker** item showing the coordinates of the Pathogen Lair, built 600 blocks north of spawn. When a player approaches within 35 blocks, **Bacillus Anthracis** spawns — a massive two-phase bacterial boss with 400 HP and a green boss bar.

- **Phase 1 (green):** Toxin Cloud, Spore Burst, Ground Slam, Charge attack
- **Phase 2 (blue, triggers at 50% HP):** Heals to full, switches to Lightning Storm, Frost Nova, Radiant Pulse, Plasma Volley
- **On death:** drops the **Anthrax Blade** — a unique weapon with 11 base attack, Poison III + Wither II + Slowness III + Weakness II on hit, and +15 bonus magic damage per strike

The boss does not respawn after being defeated. Its defeated state is saved across server restarts.

### Items

**CAR-T Injector** — One-use item. Permanently increases attack damage by 4. Based on real CAR-T cell immunotherapy used in cancer treatment.

**Pathogen Tracker** — Given to all players at wave 5. Shows the X/Y/Z coordinates of the Pathogen Lair and the full `/tp` command to reach it.

**Anthrax Blade** — Unique drop from Bacillus Anthracis. 11 base attack damage. On every hit: Poison III (8s), Wither II (6s), Slowness III (4s), Weakness II (5s), +15 bonus magic damage bypassing armor. Has an enchantment glint.

---

## Roadmap

### Near Term

- **Custom dimensions per stage** — each stage plays in a custom Minecraft dimension with matching sky color, fog, and terrain (pink flesh for skin, red-fog cave for bloodstream, etc.). Players teleport between them as the infection advances.

- **Stage 2 — Lungs** — new enemy set for waves 11+. Candidates: *Mycobacterium tuberculosis* (slow, tanky, infects air around it), *RSV virus* (fast swarm, spreads Blindness), *Aspergillus* fungus (plants stationary spore clouds on the ground).

- **Antibiotic / antiviral items** — targeted weapons that deal bonus damage to specific pathogen classes. An antibiotic grenade would be effective vs bacteria; an antiviral injection vs viruses. Gives players more strategic options beyond swords.

- **Player roles** — distinct immune cell types with unique passive abilities:
  - **Macrophage** — high HP, slower, engulfs (stuns) nearby enemies on right-click
  - **T-Cell** — standard fighter, bonus damage after the CAR-T Injector
  - **B-Cell** — ranged attacker, can mark enemies so the whole team deals +20% damage to them
  - **NK Cell** — fast, targets cancer cells (RogueCell) specifically

### Medium Term

- **Stage 3 — Bloodstream** — pathogens travel through a flowing "blood" river environment. Enemies include red blood cell mimics, platelets that block player movement, and fast viral particles in large swarms.

- **Stage 4+ — Liver, Heart, Brain** — escalating organ stages. Heart stage introduces a pulsing mechanic (wave of knockback every few seconds). Brain stage is the final stand with the most dangerous enemies.

- **Host health events** — if the host drops below 30 HP, trigger a cytokine storm (all players briefly get Slowness + Weakness as the immune system overreacts), giving the team a reason to not let things get critical.

- **Sound design** — heartbeat ambient sound that speeds up as host HP drops, breathing sounds for the lung stage, a flatline sound on game over.

### Long Term

- **Win screen / death recap** — on game over, show which wave was reached and how many pathogens were killed. On victory, show total enemies defeated across all waves.

- **Mutation system** — after wave 5, some enemies occasionally spawn as mutant variants (larger, faster, or resistant to a specific damage type), reflecting real-world pathogen evolution.

- **Vaccine mechanic** — players can craft vaccines between waves that reduce the spawn count of a specific enemy type for the next 3 waves.

- **Multiplayer roles UI** — a choose-your-immune-cell screen when joining, showing each role's stats and abilities before the first wave.

---

## Tech Stack

- **Minecraft** 1.21.1
- **Fabric Loader** 0.16.5 + **Fabric API** 0.116.11
- **Java 21**
- **Gradle 8.10.2** (Fabric Loom 1.7.4)

## Building

```
gradlew build
```

The compiled `.jar` will be in `build/libs/`.

## Running in Dev

```
gradlew runClient
```

## License

MIT
