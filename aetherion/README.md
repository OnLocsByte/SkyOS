# Aetherion

Client-side information and quality-of-life overlays for Hypixel Skyblock, built with Fabric and Kotlin for Minecraft 1.21.1.

Aetherion only reads information the server already sends you (chat, action bar, scoreboard, tooltips) and displays it more clearly. It does **not** automate gameplay, hide which mods you have installed, or give any advantage that isn't purely informational — see [Scope & limits](#scope--limits) below.

## Features

- **Dungeon Timer** — total elapsed time and per-phase splits, read from the sidebar scoreboard during a dungeon run
- **Skill XP Progress** — per-action skill XP gains and progress to the next level, parsed from the action bar
- **Rarity Tooltip Frame** — adds a rarity-colored divider to Skyblock item tooltips
- **Slayer Boss HP Bar** — replaces the action-bar HP text with a HUD progress bar
- **Best Flip Checker** — display-only bazaar spread checker (plus an optional, heuristic auction-house scanner); requires your own [Hypixel API key](https://developer.hypixel.net)
- **Draggable HUD** — every overlay's position can be dragged to a new spot in the config GUI (`/aetherion` → any category → "HUD Position" → Edit, or directly via `/aetherion hud`)

Every feature can be toggled independently, with its own sub-options, in the dark config GUI opened via `/aetherion`.

## Scope & limits

Aetherion deliberately does **not** include: autoclickers, killaura, reach extenders, X-ray, or any mod-channel suppression that hides installed mods from the server. It's built to stay within Hypixel's client modification rules.

The "Best Flip" checker only displays numbers already public on the Hypixel API — it never buys, bids, or auto-acts.

## Configuration

Settings are stored as JSON at `config/aetherion.json` in your Minecraft instance.

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.21.1
2. Install [Fabric API](https://modrinth.com/mod/fabric-api)
3. Install [Fabric Language Kotlin](https://modrinth.com/mod/fabric-language-kotlin)
4. Drop `aetherion-*.jar` into your `mods/` folder

## Building

```bash
./gradlew build
```

The output jar is in `build/libs/`.

## Usage

| Command         | Action                          |
|-----------------|----------------------------------|
| `/aetherion`     | Open the config GUI             |
| `/aetherion hud` | Open the HUD position editor    |

## License

MIT — see [LICENSE](LICENSE)
