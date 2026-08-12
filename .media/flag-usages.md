# WorldGuard ExtraFlags Plus - Flag Usage Examples

**Plugin release:** 4.4.2

This is the complete flag reference for **WorldGuard ExtraFlags Plus (WGEFP)**. Every flag is set with the standard WorldGuard command syntax — `/rg flag <region> <flag> <value>` — and removed with `/rg flag <region> <flag> clear`.

## Usage

Use the WorldGuard region flag commands as usual — all ExtraFlagsPlus flags integrate natively.

### Example Commands

#### Item Blocking
```bash
/rg flag spawn disable-completely MACE
/rg flag spawn disable-completely MACE,FIREWORK_ROCKET,GOAT_HORN
/rg flag spawn disable-completely SPEAR
/rg flag spawn disable-completely IRON_SPEAR
/rg flag spawn disable-completely clear (especially for inherited child regions)
```

Blocking spear **Lunge** (including **Lunge** enchant) requires **PacketEvents** or **ProtocolLib**. Install **PacketEvents** for primary support; **ProtocolLib** is used as a fallback when PacketEvents is unavailable.

#### Throwable blocking (`disable-throw`)

Blocks **throwing** only: **`EGG`**, **`SNOWBALL`**, **`ENDER_PEARL`**, **`EXPERIENCE_BOTTLE`**. Tridents and wind charges belong on **`disable-completely`**, not this flag.

Ensure **`all-flags-control.disable-throw`** is enabled in `plugins/WorldGuard/config-wgefp.yml` (default on).

Customize **`disable-throw-blocked`** in `messages-wgefp.yml` (placeholder `{item}`).

```bash
/rg flag arena disable-throw EGG,SNOWBALL
/rg flag arena disable-throw ENDER_PEARL
/rg flag arena disable-throw EXPERIENCE_BOTTLE
/rg flag arena disable-throw clear
```

#### Chambered ender pearl *(experimental)*
**Experimental** — behavior may change in future releases.
```bash
/rg flag spawn chambered-enderpearl deny
/rg flag spawn chambered-enderpearl allow
/rg flag spawn chambered-enderpearl clear
```

#### Level Restrictions
```bash
/rg flag dungeon entry-min-level 20 XP
/rg flag dungeon entry-min-level 40 %battlepass_tier%
```

#### Entry Permissions
```bash
/rg flag vip entry-permission myPlugin.myPermission.1
/rg flag vip entry-deny-permission myPlugin.myPermission.1
```

**`entry-permission`** requires players to have the given permission node to enter the region. **`entry-deny-permission`** denies entry to players holding the given permission node. If both flags are set, `entry-permission` takes priority.

#### Trading & Interaction Control
```bash
/rg flag spawn villager-trade deny
```

#### Player Count Limits
```bash
/rg flag arena player-count-limit 10
```

#### Collision Control
```bash
/rg flag spawn disable-collision true
```

#### Block Control
```bash
/rg flag spawn allow-block-place STONE,COBBLESTONE,GRASS_BLOCK
/rg flag spawn deny-block-place TNT,LAVA_BUCKET
/rg flag spawn allow-block-break STONE,COBBLESTONE
/rg flag spawn deny-block-break BEDROCK,SPAWNER
```

#### Item Control
```bash
/rg flag spawn deny-item-drops diamond,emerald,netherite_ingot
/rg flag spawn deny-item-pickup apple,redstone,iron_ingot
```

#### Workbench Control
```bash
/rg flag spawn permit-workbenches ALL
/rg flag spawn permit-workbenches ALL,ender
/rg flag spawn permit-workbenches craft,anvil,ender
/rg flag spawn permit-workbenches clear
```

#### Crafting Control
```bash
/rg flag spawn inventory-craft deny
```

#### Protection & Movement
```bash
/rg flag spawn godmode deny (also disables EssentialsX godmode if enabled)
```

#### Chat Formatting
```bash
/rg flag spawn chat-prefix "&7[%vault_rank%] "
/rg flag spawn chat-suffix " &7[%player_level%]"
```

#### Repeating Console Commands
```bash
# Quote the entire "<seconds> <command>" as one string:
/rg flag arena console-command-repeat "20 give %player% diamond 1"
/rg flag arena console-command-repeat "5 eco give %player% 10"
/rg flag arena console-command-repeat clear
```

#### Play Sounds
```bash
/rg flag spawn play-sounds minecraft:block.note_block.pling
```

#### PlaceholderAPI placeholder — players in region

Requires **PlaceholderAPI** on the server. Toggle with `all-flags-control.papi-placeholders` in `config-wgefp.yml` (default on).

```bash
%wgefp_players_in_region_spawn%     # players currently inside the "spawn" region
```

- Region id is **case-insensitive** and may contain underscores (they are kept, not split).
- Sums players across **all worlds** that have a region with the same id.
- Returns `0` when the region doesn't exist or is empty.
- Values are cached and refreshed once per second; each player is counted on their own region thread (Folia-safe).

## Flag Categories

### Location & Teleportation
- `teleport-on-entry` / `teleport-on-exit`
- `join-location` (not available on Folia)
- `respawn-location`

### Command Execution
- `command-on-entry` / `command-on-exit`
- `console-command-on-entry` / `console-command-on-exit`
- `console-command-repeat`

### Movement & Speed Control
- `walk-speed` / `fly-speed`
- `fly` / `glide`
- `frostwalker`

### Protection & Survival
- `godmode` / `keep-inventory` / `keep-exp`
- `item-durability`

### Chat Modification
- `chat-prefix` / `chat-suffix`

### Effect Control
- `blocked-effects` / `give-effects`
- `play-sounds`

### World Interaction
- `worldedit`
- `nether-portals` / `chunk-unload`
- `villager-trade` / `inventory-craft`

### Block & Item Control
- `allow-block-place` / `deny-block-place`
- `allow-block-break` / `deny-block-break`
- `deny-item-drops` / `deny-item-pickup`
- `disable-completely` / `disable-throw` / `permit-workbenches`

### Entry Control
- `entry-min-level` / `entry-max-level`
- `entry-permission` / `entry-deny-permission`
- `player-count-limit`

### Special Features
- `disable-collision`
- `hide-players` *(experimental)*
- `lightning-damage`
- `chambered-enderpearl` *(experimental)*

## Notes

- All flags use standard WorldGuard syntax: `/rg flag <region> <flag> <value>`
- Use `clear` to remove flag values: `/rg flag <region> <flag> clear`
- Some flags require specific plugins (PlaceholderAPI for placeholder support)
- Check individual flag documentation for advanced usage
- **`disable-throw`** must be enabled in **`config-wgefp.yml`** (`all-flags-control.disable-throw`) alongside other toggled flags
