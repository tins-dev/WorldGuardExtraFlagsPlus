# `deny-mobspawn` — Mob Spawn Categories

The `deny-mobspawn` region flag blocks spawning of mobs in the listed categories. When a mob tries to spawn inside a region where the flag is set, the spawn is cancelled.

Set it with the standard WorldGuard syntax:

```bash
/rg flag <region> deny-mobspawn ALL_HOSTILE
```

Values are comma-separated and combine freely — the mob spawn is cancelled if it matches **any** listed value:

```bash
/rg flag <region> deny-mobspawn ALL_HOSTILE,ALL_NEUTRAL
/rg flag <region> deny-mobspawn ALL
/rg flag <region> deny-mobspawn ZOMBIE,CREEPER
```

Use `clear` to remove the flag again:

```bash
/rg flag <region> deny-mobspawn clear
```

## Categories

### ALL_HOSTILE

Every hostile mob — anything classified as an `Enemy` mob by the server API, **including bosses** (`WITHER`, `ENDER_DRAGON`). Because the set is derived from the `Enemy` interface at plugin load, newly added hostile mobs are covered automatically.

Complete list (41 mobs, MC 26.x / 1.21.4+):

`BLAZE`, `BOGGED`, `BREEZE`, `CAVE_SPIDER`, `CREAKING`, `CREEPER`, `DROWNED`, `ELDER_GUARDIAN`, `ENDER_DRAGON`, `ENDERMAN`, `ENDERMITE`, `EVOKER`, `GHAST`, `GIANT`, `GUARDIAN`, `HOGLIN`, `HUSK`, `ILLUSIONER`, `MAGMA_CUBE`, `PARCHED`, `PHANTOM`, `PIGLIN`, `PIGLIN_BRUTE`, `PILLAGER`, `RAVAGER`, `SHULKER`, `SILVERFISH`, `SKELETON`, `SLIME`, `SPIDER`, `STRAY`, `VEX`, `VINDICATOR`, `WARDEN`, `WITCH`, `WITHER`, `WITHER_SKELETON`, `ZOGLIN`, `ZOMBIE`, `ZOMBIE_VILLAGER`, `ZOMBIFIED_PIGLIN`

> **Notes**
> - The list above reflects MC 26.x / 1.21.4+. Older MC versions have a subset — `BOGGED`, `BREEZE`, `CREAKING` and `PARCHED` are newer mobs and only exist on newer servers.
> - `WITHER` and `ENDER_DRAGON` are included deliberately — they are `Enemy` mobs, so `ALL_HOSTILE` blocks their spawning.
> - `ENDERMAN`, `PIGLIN`, `ZOMBIFIED_PIGLIN` and `HOGLIN` are classified as `Enemy` by the API, so they are part of `ALL_HOSTILE`, not `ALL_NEUTRAL`, even though they only attack in some situations.

### ALL_NEUTRAL

Mobs that only attack when provoked. These are **not** `Enemy` mobs, so they are not part of `ALL_HOSTILE`:

`WOLF`, `BEE`, `IRON_GOLEM`, `SNOW_GOLEM`, `POLAR_BEAR`, `GOAT`, `PANDA`, `LLAMA`, `TRADER_LLAMA`, `DOLPHIN`, `FOX`

### ALL_PEACEFUL

Every other living mob — anything that is alive and is neither hostile nor neutral. This is an **inclusive** definition: villagers, wandering traders, bats, allays, armor stands, all passive animals (pigs, cows, sheep, chickens, rabbits, turtles, axolotls, cats, horses, donkeys, mules, striders, ...), fish and squid (cod, salmon, pufferfish, tropical fish, squid, glow squid), frogs, tadpoles, and every other non-hostile living creature. Armor stands and allays are part of this category.

The set is derived automatically at plugin load (every alive entity type that is not hostile and not neutral), so new peaceful mobs are covered automatically.

### ALL

Shorthand for `ALL_HOSTILE,ALL_NEUTRAL,ALL_PEACEFUL` — blocks spawning of every mob.

### Per-entity

Instead of (or in addition to) the categories, you can list specific `EntityType` names for per-mob granularity:

```bash
/rg flag <region> deny-mobspawn ZOMBIE,CREEPER
```

Any valid `EntityType` name is accepted, e.g. `VILLAGER`, `WARDEN`, `ENDER_DRAGON`.

## Notes

- `ALL_PEACEFUL` and per-mob names do not affect hostile mobs unless `ALL_HOSTILE` (or `ALL`) is also set.
- The flag works alongside vanilla WorldGuard `deny-spawn` / `mob-spawning` — it is an additional, independent filter.
- Spawn cancellation applies to every spawn source that fires a `CreatureSpawnEvent` (natural spawns, spawners, spawn eggs, structure spawns, summoning, ...).
