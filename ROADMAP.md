# Resonance Development Roadmap

**Current version:** 1.0.0  
**Minecraft:** 26.1.2  
**Loader:** NeoForge 26.1.2.81  
**Status:** Feature-complete release candidate; final packaged-client smoke test and publishing remain.

This roadmap records the actual state of the mod as of July 22, 2026. Completed sections describe systems already implemented and validated. Future sections are intentionally separated so unreleased ideas are not mistaken for current features.

---

## 1.0.0 — Completed Content

### Core Resonance System

- [x] Stackable Resonance status effect with configurable incoming-damage amplification and duration.
- [x] Resonance particles, application sounds, fractures, vibration scars, and crystal-growth interactions.
- [x] Killing blows correctly roll resonant fracture effects when the attack applies Resonance and kills simultaneously.
- [x] Resonant Sword sweeping attacks apply Resonance to every target struck by the sweep.
- [x] Multiplayer-safe effect, particle, combat, and boss-event synchronization.
- [x] Common configuration for damage bonus, duration, Harmonic Shield cooldown, and particle visibility.

### Materials, Tools, and Weapons

- [x] Amethyst Ingot crafting material and repair ingredient.
- [x] Resonant Sword.
- [x] Resonant Spear with bonuses against resonating targets.
- [x] Resonant Pickaxe with chain-fracture mining behavior.
- [x] Resonant Axe with a crystalline combat shockwave.
- [x] Resonant Shovel with Resonant Path creation and stacking movement bonuses.
- [x] Resonant Hoe with Harmonized Farmland creation.
- [x] Resonant Arrow with area Resonance application and vanilla bow/crossbow compatibility.
- [x] Correct mining tags, tool actions, durability behavior, and creative-tab placement.
- [x] Recipe-book unlocks for every craftable Resonance item.

### Armor and Protective Equipment

- [x] Complete Resonant player armor set with individual equipment abilities.
- [x] Harmonic Shield full-set bonus, cooldown, recharge feedback, particles, and tooltip.
- [x] Resonant Totem with Totem of Undying behavior and an area Resonance burst.
- [x] Resonant Wolf Armor crafted with Crystal Scutes.
- [x] Resonant Horse Armor as rare exploration loot above diamond rarity.
- [x] Resonant Nautilus Armor as rare exploration loot above diamond rarity.
- [x] Armor trim support and matching equipment textures.

### Potions and Food

- [x] Resonance, Long Resonance, Strong Resonance, and highest-strength Resonance potion progression.
- [x] Splash, lingering, and tipped-arrow compatibility through vanilla potion systems.
- [x] Raw Crystal Rabbit.
- [x] Cooked Crystal Rabbit with improved food values over vanilla rabbit.
- [x] Crystal Rabbit Stew with improved food values and a correctly preserved brown bowl.
- [x] Furnace, smoker, campfire, and stew recipe integration.

### Crystal Terrain and Farming

- [x] Crystal Dirt.
- [x] Coarse Crystal Dirt.
- [x] Rooted Crystal Dirt.
- [x] Crystal Grass Block and Crystal Grass.
- [x] Crystal Dirt Path.
- [x] Crystal Farmland with vanilla seed and crop support.
- [x] Vanilla-like acquisition and transformations for dirt variants.
- [x] Shovel and hoe special abilities work across crystal dirt, grass, path, and farmland variants.
- [x] Harmonized Farmland crop acceleration.
- [x] Persistent Resonant Paths and Harmonized Farmland with stale-position cleanup after blocks change.
- [x] Crystal-colored path-running particles instead of vanilla dirt particles.

### Crystal Flora and Wood Family

- [x] Crystal Bloom and Shard Blossom based on recognizable vanilla flower silhouettes.
- [x] Crystal Logs, Wood, Stripped Logs, Stripped Wood, Planks, Stairs, Slabs, Doors, Trapdoors, Fences, Fence Gates, Buttons, and Pressure Plates.
- [x] Cherry-derived wood and leaf textures recolored into the amethyst/calcite palette.
- [x] Cherry-style trunk and foliage variants during natural tree generation.
- [x] Consistent crystal sound type across the entire wood family.
- [x] Crystal Leaves decay like vanilla leaves and do not drop their block naturally.
- [x] Animated Crystal Leaves metadata retained where required.

### Crystal Forest and World Generation

- [x] Crystal Forest biome and controlled biome-spreading system.
- [x] Calcite-rich ground where Crystal Grass does not claim the surface.
- [x] Crystal tree variants, vegetation, flowers, scatter growths, and ambient biome effects.
- [x] Amethyst Spires and Massive Spires.
- [x] Cracked and Massive Geodes.
- [x] Arena-safe geode, spire, and scatter placement margins.
- [x] Harmonic Arena structure generation and protection from overlapping features.
- [x] Crystallized End biome/world-generation support.
- [x] Dense-biome recurring work and vibration-scar processing optimized.

### Passive Crystal Wildlife

- [x] Crystal Rabbit with faceted amethyst-block patterning and baby texture.
- [x] Crystal Armadillo with faceted amethyst-block patterning and baby texture.
- [x] Custom spawn eggs matching their crystal designs.
- [x] Crystal Rabbit breeding and spawn-egg offspring remain Crystal Rabbits.
- [x] Crystal Rabbits drop only Raw/Cooked Crystal Rabbit as appropriate.
- [x] Crystal Armadillos do not drop scutes on death.
- [x] Adult Crystal Armadillos shed Crystal Scutes over time and when brushed.
- [x] Crystal Scute texture and Crystal Scute-based Wolf Armor recipe.

### Hostile Creatures

- [x] Shattered Echo with unique Crystal Fragment drop.
- [x] Resonant Stalker with unique Whisper Fragment drop and custom laugh.
- [x] Crystal Sentinel with beam combat and unique Harmonic Fragment drop.
- [x] Crystal Wraith with a redesigned frightening model, corrected hitbox, shortened arms, claws, rib cage, recessed heart, and z-fighting cleanup.
- [x] Crystal Wraith ground-crawling spawn animation.
- [x] Crystal Wraith breakable crystal armor and enraged second phase.
- [x] Crystal Wraith multiplayer-synced emergence and broken-armor state.
- [x] Crystal Wraith save/reload persistence for emergence, armor, attributes, and slam cooldown.
- [x] Fully custom Crystal Wraith ambient, hurt, death, emergence, armor-break, and attack sounds.
- [x] Loud, rising, menacing emergence cue clearly separated from the fading death cue.
- [x] Wraith, Echo, Sentinel, Rabbit, and Armadillo palettes normalized around amethyst/calcite shading.

### The Harmonic Boss Encounter

- [x] The Harmonic multi-phase boss.
- [x] Harmonic Arena encounter space.
- [x] Harmonic Shield phases and regeneration timing.
- [x] Crystal Sentinel minions and Harmonic Anchors.
- [x] Beam attacks, ground spikes, shockwaves, crystal rain, and phase transitions.
- [x] Boss bar and multiplayer-safe attack state.
- [x] Save/reload persistence for long-lived encounter cooldowns and timers.
- [x] Safe cancellation of transient attacks after reload to prevent duplicate damage.
- [x] Boss loot, summoning progression, and completion advancement.
- [x] Harmonic model cleanup, appropriately sized and positioned hitbox, and texture polish.

### Utility Blocks

- [x] Resonant Lantern with hostile-mob Resonance pulses.
- [x] Frequency Relay linking, detection, redstone output, and active textures.
- [x] Chorus Resonator encounter utility.
- [x] Chorus Resonator icon restored while keeping the block out of creative tabs because it depends on generated crystal formations.

### Items, Art, and Presentation

- [x] Crystal Fragment rename and redesign.
- [x] Unique Crystal, Whisper, and Harmonic Fragment silhouettes tied to their source mobs.
- [x] Cohesive amethyst, calcite-white, deep-violet, and restrained pink palette.
- [x] Creative-tab ordering audited and corrected.
- [x] Recipe-book duplicate stew rendering corrected.
- [x] Spawn eggs, food, scutes, tools, armor, blocks, flowers, terrain, and wood textures completed.
- [x] 400 × 400 CurseForge project icon prepared.
- [x] In-mod icon, metadata, subtitles, and description packaged.

### Loot, Recipes, and Advancements

- [x] Thirty-two crafting/cooking recipes with recipe-book integration.
- [x] Rare Horse Armor and Nautilus Armor moved from crafting to exploration loot.
- [x] Ancient City, End City, shipwreck, and buried-treasure loot integration.
- [x] Mob loot tables for every custom creature.
- [x] Block loot tables and vanilla-like drop behavior for all crystal blocks.
- [x] Fourteen progression, equipment, exploration, summoning, and boss advancements.

### Stability and Release Engineering

- [x] Dedicated-server registry, recipe, advancement, and dimension loading validated.
- [x] Multiplayer synchronization audit completed.
- [x] Persistent-data cleanup and reload safety completed.
- [x] Arena/world-generation overlap audit completed.
- [x] Dense Crystal Forest performance pass completed.
- [x] Clean Gradle build produces `resonance-1.0.0.jar`.
- [x] MIT license, README, changelog, mod icon, metadata, and configuration defaults prepared.
- [x] Private GitHub development repository created and initial project snapshot pushed.

---

## Remaining Before Public Release

- [ ] Run a packaged-client smoke test using the exact release JAR rather than the development run configuration.
- [ ] Confirm client startup and create one completely new test world.
- [ ] Confirm Crystal Forest terrain, calcite coverage, tree variants, geodes, spires, arena protection, and natural mob spawning in that world.
- [ ] Confirm every custom Wraith sound in-game at normal hostile-mob volume settings.
- [ ] Confirm save/reload behavior during a Wraith fight and Harmonic encounter in the packaged build.
- [ ] Confirm a short multiplayer session with Wraith emergence, Sentinel beams, Resonance sweeps, particles, and the Harmonic boss bar.
- [ ] Save the final “Echoes of the Past” CurseForge description with spoiler sections.
- [ ] Upload the release JAR to CurseForge as a NeoForge 26.1.2 release.
- [ ] Add the 1.0.0 changelog and verify the processed download.
- [ ] Verify the public listing icon, license, description, categories, dependencies, file metadata, and download.
- [ ] Create a signed or annotated `v1.0.0` Git tag only after the exact public artifact is confirmed.

---

## Documentation Roadmap

- [x] Updated project README.
- [x] Updated 1.0.0 changelog.
- [x] Comprehensive development roadmap.
- [ ] Submit and receive approval for `resonance.wiki.gg`.
- [ ] Create a spoiler-light Getting Started guide.
- [ ] Document Resonance combat, fractures, vibration scars, and damage scaling.
- [ ] Document every tool, weapon, armor ability, potion, food, and utility block.
- [ ] Document Crystal Forest generation, dirt transformations, farming, flora, and tree variants.
- [ ] Create individual bestiary pages with spawning, behavior, drops, and combat guidance.
- [ ] Create spoiler-marked Wraith, Harmonic Arena, and The Harmonic encounter guides.
- [ ] Add recipe, loot-source, advancement, configuration, installation, compatibility, and troubleshooting pages.
- [ ] Add versioned change-history pages beginning with 1.0.0.

---

## Release Discipline

- Keep `main` buildable.
- Commit focused changes with descriptive messages.
- Update `CHANGELOG.md` for every user-visible change.
- Update this roadmap whenever a feature changes status.
- Never tag a release until the uploaded artifact has passed packaged-client and dedicated-server smoke tests.
- Preserve spoiler labels for major discoveries in public documentation.
