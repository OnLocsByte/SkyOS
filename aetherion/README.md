# Aetherion

A client-side, informational/QoL Fabric mod for Hypixel Skyblock (Minecraft 1.21.1), built with
Fabric API and Fabric Language Kotlin. Dark-themed config GUI, open with `/aetherion`.

Aetherion only reads information the server/client already exposes (chat, action bar, scoreboard,
tooltips, public Hypixel API data) and renders it more conveniently. It does **not**:

- suppress or fake mod-channel registration packets, or otherwise hide which mods you run,
- auto-click, auto-fight, extend reach, reveal hidden blocks/entities, or otherwise automate or
  alter gameplay,
- auto-buy or auto-bid on the Bazaar/Auction House — the price checker is display-only.

## Features

- **Dungeon Timer** — elapsed-time overlay for Catacombs runs, with best-effort phase splits and
  a death counter, derived from chat messages.
- **Skill Progress** — a small progress bar for the skill XP gained per action (Farming, Mining,
  Combat, ...), with an optional ETA to the next level.
- **Item Tooltips** — adds a rarity-colored rule and an optional "Reforge:" line to Skyblock item
  tooltips, without touching the original tooltip text.
- **Slayer HP Bar** — replaces the raw slayer boss action-bar text with a health bar.
- **Best Flip** — periodically checks Hypixel's public Bazaar/Auction House data for the largest
  buy/sell margins and lists them (own screen + optional compact HUD panel). Display only —
  nothing is ever bought or bid on automatically.
- **HUD editor** — every overlay above can be dragged to a custom screen position from
  `/aetherion` → any feature → "Move overlay", or via the dedicated "HUD" category.

All features are individually toggleable with their own sub-options, and every setting is saved
as JSON in `config/aetherion.json`.

## Architecture

- One package per feature under `feature/`, each implementing a small `Feature` interface
  (`isEnabled()` + `init()`), registered once in `AetherionMod`.
- HUD overlays additionally implement `HudElement` and register with `HudManager`, which both the
  live renderer and the drag-and-drop `HudEditScreen` iterate over.
- All game-state reads use Fabric API events (`ClientTickEvents`, `ClientReceiveMessageEvents`,
  `ItemTooltipCallback`, `HudRenderCallback`, `ClientCommandRegistrationCallback`) — no mixins
  touch network/packet handling.
- The **only** mixin in the project (`ActionBarAccessorMixin`) is a non-cancelling, read-only tap
  on `Gui.setOverlayMessage` used to feed the dungeon/skill/slayer parsers with the action-bar
  text Minecraft is already about to render. It does not alter, delay, or cancel anything.
- The Bazaar/AH price checker only calls Hypixel's public `/v2/skyblock/bazaar` and
  `/v2/skyblock/auctions` endpoints, which don't require an API key.

Hypixel's exact chat/action-bar wording can change over time; the regexes that drive the dungeon
timer, skill progress and slayer HP bar are isolated at the top of their respective feature files
so they're easy to update if that happens.

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

| Command      | Action              |
|--------------|----------------------|
| `/aetherion` | Open the config GUI |

## License

MIT — see [LICENSE](LICENSE)
