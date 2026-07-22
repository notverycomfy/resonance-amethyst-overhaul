# Resonance

**Minecraft:** 26.1.2 | **Loader:** NeoForge 26.1.2.81

---

## Items & Blocks

### Material
- **Amethyst Ingot** — 4 amethyst shards + 4 iron ingots (shapeless). Repairs all Resonant gear.

### Tools & Weapons
| Item | Mechanic |
|------|----------|
| Resonant Sword | Applies Resonance on hit (amethyst chime + note particles) |
| Resonant Spear | Consumes Resonance to deal 7 burst damage (crit + end rod particles) |
| Resonant Pickaxe | 15% chain mine (up to 3 blocks) on block break |
| Resonant Axe | 40% shockwave on Resonance targets: applies Resonance + knockback in 3 blocks |
| Resonant Shovel | Creates resonant paths: stacking speed boost up to +20% + END_ROD particles while walking |
| Resonant Hoe | Till to create harmonized farmland: crops grow 20% faster (SavedData) |
| Resonant Arrow | AOE Resonance on impact (3 block radius) |

Diamond mining tier, 780 durability, 20 enchantability, 7.0 mining speed, 2.5 attack damage bonus.

### Player Armor
| Item | Defense | Mechanic |
|------|---------|----------|
| Helmet | 3 | Resonance-afflicted mobs glow within 16 blocks |
| Chestplate | 7 | Melee attackers receive Resonance |
| Leggings | 6 | +10% speed on stone/deepslate/amethyst |
| Boots | 3 | Negate fall damage up to 4 blocks |

+1 toughness, 20 enchantability. Supports armor trims (with icon overlay).

**Set Bonus — Harmonic Shield:** Full set absorbs one hit every 30s, bursting Resonance + knockback to nearby mobs. Tooltip displays on all armor pieces.

### Companion Armor
| Item | Defense | Toughness | Mechanic |
|------|---------|-----------|----------|
| Horse Armor | 15 | 2 | Pulses Resonance to hostiles within 8 blocks every 2s |
| Nautilus Armor | 15 | 2 | Conduit Power to rider + Resonance to underwater hostiles within 6 blocks |
| Wolf Armor | 15 | 2 | Pulses Resonance to hostiles within 5 blocks; absorbs all damage (durability) |

All crafted from 7 Amethyst Ingots.

### Special Items
| Item | Mechanic |
|------|----------|
| Resonant Totem | Totem of Undying behavior; on use applies Resonance III (2x duration) in 8 blocks |

### Blocks
| Block | Mechanic |
|-------|----------|
| Resonant Lantern | Light level 15, hanging/waterloggable. Applies Resonance to monsters within 12 blocks every 3s |
| Frequency Relay | Linking system (right-click to pair). Detects Resonance mobs in 8 blocks, emits redstone 15 via linked partner |

---

## Mechanics

### Resonance Effect
- +20% damage from all sources per amplifier level (configurable)
- 5 second duration (configurable), resets on reapplication
- END_ROD particles on afflicted entities (configurable)

### Potions
| Potion | Effect | Duration | Amplifier | Recipe |
|--------|--------|----------|-----------|--------|
| Resonance | Resonance | 5s | 0 | Awkward + Amethyst Ingot |
| Long Resonance | Resonance | 10s | 0 | Resonance + Redstone |
| Strong Resonance | Resonance | 5s | 1 | Resonance + Glowstone Dust |
| Strongest Resonance | Resonance | 5s | 2 | Strong + Amethyst Ingot |

### Sculk Interaction
- **Dampening** — full armor suppresses all vibrations from the player
- **Weaponization** — right-click a sculk sensor with a Resonant weapon to pulse Resonance II to hostiles within 8 blocks

### Data Persistence
- Harmonized farmland and resonant paths survive world reloads (SavedData + Codec)

### Loot Table Integration
- **Ancient City:** Amethyst Ingots (17.5%) + Resonant Armor pieces (4% each, enchanted)
- **End City Treasure:** Amethyst Ingots (15%, 2-4) + Resonant Armor pieces (6.5% each, enchanted)

---

## Advancements

Good Vibrations (root)
├── Sing, Blade, Sing
│   ├── Frequency Spike
│   └── Echoing Shot
├── Cover Your Frequencies [goal]
│   ├── Crystal Cavalry
│   ├── Depths of Harmony
│   └── Pack Resonance
└── Tuned Toolkit
    └── Full Resonance [challenge] (all 14 items)

---

## Config

| Option | Default | Range |
|--------|---------|-------|
| `resonanceDamageBonus` | 0.2 | 0.0–2.0 |
| `resonanceDuration` | 100 ticks | 20–600 |
| `harmonicShieldCooldown` | 600 ticks | 100–2400 |
| `showParticles` | true | true/false |

---

## What's Next

- [ ] Resonant Shield
- [ ] Custom enchantment (Harmonic)
- [ ] Amethyst geode interaction
- [x] Custom sounds (ModSounds registry, subtitled, vanilla-backed)
- [x] Loot table integration (Ancient City + End City)
- [x] Resonant path particles (END_ROD on walk) + Shovel stacking speed boost
- [x] Harmonic Shield set bonus tooltip on armor
- [ ] Frequency Relay resonant beam visuals
