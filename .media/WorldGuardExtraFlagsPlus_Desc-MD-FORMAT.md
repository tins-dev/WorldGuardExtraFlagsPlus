# WorldGuard ExtraFlags Plus

An advanced WorldGuard extension that adds over 29 extra region flags for full control of player behavior, teleportation, and region rules — featuring Folia support, item blocking (Mace, Firework, Wind Charge, Totem), and fully customizable messages.


> 🧱 **Folia Ready** | ⚙️ **Custom Messages** | 🪓 **Permit Mace, Totem & More**

> 🎚️ **XP-Based or PlaceholderAPI (integer output) based region entry limits**

---

## Key Features
- ✅ **Folia support** – fully compatible with async region handling  
- 🛡️ **New flag:** `permit-completely` – blocks all usage of specified items *(MACE, FIREWORK_ROCKET, WIND_CHARGE, TOTEM_OF_UNDYING)*  
- 🎚️ **New flags:** `entry-min-level` / `entry-max-level` – restrict entry by **XP level** or **PlaceholderAPI values**  
- 💬 **Customizable messages** via `messages.yml` (disable, recolor, or use placeholders)  
- 🔁 **Message cooldown system** to prevent spam (default 3 seconds)  

---

## About
WorldGuard protects land by defining regions.  
**WorldGuard ExtraFlags Plus** extends it with even more customization — adding powerful flags that modify gameplay, teleportation, commands, and behavior within regions.

---

## Included Flags (29+)
> Here’s a quick overview — all managed with standard WorldGuard flag commands.

```
teleport-on-entry / teleport-on-exit  
command-on-entry / command-on-exit  
console-command-on-entry / console-command-on-exit  
walk-speed / fly-speed  
keep-inventory / keep-exp  
chat-prefix / chat-suffix  
godmode / blocked-effects  
respawn-location / worldedit / give-effects  
fly / play-sounds / frostwalker / nether-portals / glide (elytra-blocker)
chunk-unload / item-durability / join-location
```

**New in Plus:**
```
permit-completely
entry-min-level
entry-max-level
```

---

## Usage
Use the WorldGuard region flag commands as usual —  
all ExtraFlagsPlus flags integrate natively.

Example:
```
/rg flag spawn permit-completely MACE
/rg flag spawn permit-completely MACE,FIREWORK_ROCKET

/rg flag spawn permit-completely clear (especially for inherited child regions)

/rg flag dungeon entry-min-level 20 XP
/rg flag dungeon entry-min-level 40 %battlepass_tier%
```

---

## Version Compatibility
| Minecraft | WorldGuard | ExtraFlagsPlus | Support |
|------------|-------------|----------------|----------|
| 1.20 – 1.21.10 | 7.0.13+ | 4.3.4+ | ✅ Active |
| 1.7 – 1.19 | Older | ❌ No support |

---

## Message Customization
All plugin messages live in `plugins/WorldGuard/messages.yml`.

- Edit freely to match your style  
- Use `{required}`, `{current}`, `{item}` placeholders  
- Color codes supported (`&c`, `&7`, etc.)  
- Disable messages with `""`  
- Reload instantly using `/wgefp reload` or `/wg reload`

---

## Authors
- **ExtraFlags Plus Developer:** [tins](https://github.com/tins-dev)  
- **Original ExtraFlags Author:** [isokissa3](https://joniaromaa.fi)

---

## Image Section

---


⭐ If you like this project, give it a star on [Github](https://github.com/tins-dev/WorldGuardExtraFlagsPlus)