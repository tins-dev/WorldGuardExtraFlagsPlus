# WorldGuard ExtraFlags Plus

A Bukkit plugin extension that provides extra flags for [WorldGuard](https://github.com/EngineHub/WorldGuard).
- Support for **Folia** ✅
- New Flag **"permit-completely"** | Blocks all usage of specified items (MACE, FIREWORK_ROCKET, WIND_CHARGE, TOTEM_OF_UNDYING) ✅
- New Flags **"entry-min-level"** & **"entry-max-level"** | Restrict region entry based on **Player (xp) level** or **PlaceholderAPI** values ✅
- New **Configurable Messages** | Customize all plugin messages via `messages.yml` in WorldGuard folder ✅
- New **Message Cooldown System** | Prevents message spam with configurable cooldown (default: 3 seconds) ✅

## About
WorldGuard allows protecting areas of land by the creation of regions which then can be customized further by applying special flags. WorldGuard provides an API that 3th party plugins can use to provide their own flags.

This plugin adds extra flags to allow customizing regions even further.
WorldGuard ExtraFlags Plus is extension to WorldGuard that adds 29 new flags listed below!

    teleport-on-entry & teleport-on-exit | Teleports the player to given location when player enters/exists the region
    command-on-entry & command-on-exit | Executes a command when player enters/exists the region (Use %username% for player username placeholder)!
    console-command-on-entry & console-command-on-exit | Executes a command as console when player enters/exists the region (Use %username% for player username placeholder)!
    walk-speed & fly-speed | Sets the walking speed inside the region
    keep-inventory | Does the player keep their inventory on death inside the region
    keep-exp | Does the player keep their exp on death inside the region
    chat-prefix | Chat prefix shown when inside the region
    chat-suffix | Chat suffix shown when inside the region
    godmode | Does the player the damage inside the region
    blocked-effects | Block effects inside the region
    respawn-location | Sets the players respawn location when inside the region
    worldedit | Is WorldEdit permitted inside the region
    give-effects | Gives effects while inside the region and restores old effects on region leave with correct time left
    fly | Whatever flying is enabled or disabled when entering the region
    play-sounds | Allows you to play sounds once or on repeat. When running 1.9 server or above the sound will stop playing once the player leaves the region
    frostwalker | Is frostwalker permitted inside the region
    nether-portals | Is creation of nether portals permitted inside the region
    glide | Is flying with Elytra allowed inside the region. Can also be used to give the player glide effect without wearing one
    chunk-unload | Is chunk unloading permitted inside the region
    item-durability | Is item durability allowed inside the region
    join-location | Teleports the player to given location when logging in to the region

    **NEW**
    permit-completely | Blocks all usage of specified items (MACE, FIREWORK_ROCKET, WIND_CHARGE, TOTEM_OF_UNDYING) inside the region. Usage includes interactions, damage, projectile launches, and totem activation. Usage: `/rg flag <region> permit-completely <item1,item2,...>` or `/rg flag <region> permit-completely clear` (sets empty set). Supports inheritance - child regions can override parent using `clear` or by setting their own item list.
    entry-min-level & entry-max-level | Restricts region entry based on player level or PlaceholderAPI placeholder value. Format: `<threshold> <source>` where source is either "XP" (Minecraft XP level) or a PlaceholderAPI placeholder (e.g., `%battlepass_tier%`). Examples: `10 XP` or `30 %armor_durability_left_helmet%`.

How to use?
Simply use the WorldGuard region flag command. All of the flags can be interacted that way, just like any other flag.

**Examples:**
```
/rg flag spawn permit-completely MACE
/rg flag spawn permit-completely MACE,FIREWORK_ROCKET,TOTEM_OF_UNDYING
/rg flag spawn permit-completely clear

/rg flag dungeon entry-min-level 20 XP
/rg flag dungeon entry-min-level 40 %battlepass_tier%
```

**Permit-completely Inheritance Example:**
```
# Parent region blocks totem
/rg flag outside permit-completely TOTEM_OF_UNDYING

# Child region overrides parent (allows everything)
/rg flag inside permit-completely clear
# OR set specific items only
/rg flag inside permit-completely MACE
```

Minecraft & WorldGuard version support:
To make sure that the plugin works correctly, you need to have compatible version of the WorldGuard and Minecraft alongside the plugin itself. The following list contains the supported versions.

    Minecraft 1.20 - 1.21.10
        WorldGuard 7.0.13+
        WorldGuard ExtraFlags Plus 4.3.3+ (Latest, Support provided)

    Minecraft 1.7 - 1.19 (Outdated, no support)

### Message Customization
Plugin messages can be customized through `plugins/WorldGuard/messages.yml`. The file is created automatically on first plugin load.

**Features:**
- Edit messages to match your server's style
- Set message to empty string (`""`) to disable it completely
- Supports placeholders: `{required}`, `{current}`, `{item}`
- Use color codes with `&` format (e.g., `&c` for red, `&7` for gray)
- Configurable message cooldown to prevent spam (set `send-message-cooldown` in messages.yml, default: 3 seconds, 0 = disabled)
- Reload messages without server restart using `/wgefp reload` or `/wg reload`

**Available Messages:**
- `entry-min-level-denied` - Message shown when player level is below minimum requirement
- `entry-max-level-denied` - Message shown when player level is above maximum requirement
- `permit-completely-blocked` - Message shown when blocked item is used

## New updates & features developed by (WorldGuard ExtraFlags Plus)
- tins

## Original author (WorldGuard ExtraFlags)
- isokissa3
- https://joniaromaa.fi

