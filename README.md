# SkyOS

**Premium modular Minecraft Fabric ecosystem for Hypixel SkyBlock.**

[![Build](https://github.com/onlocsbyte/SkyOS/actions/workflows/build.yml/badge.svg)](https://github.com/onlocsbyte/SkyOS/actions/workflows/build.yml)

---

## Supported Versions

| Minecraft | Loader | Status |
|-----------|--------|--------|
| 1.21.10   | Fabric | Active |
| 1.21.11   | Fabric | Active |

No Forge. No NeoForge. No older versions. Hypixel SkyBlock only.

---

## Modules

### SkyOS Core (`skyos-core`)
The foundation. All other modules depend on this.

**Provides:**
- Rendering framework with SkyOS visual identity
- Animation system (easing curves, animated values)
- Overlay manager (anchor-based HUD elements)
- Notification system (slide-in toasts with progress)
- Event bus (typed, lightweight pub/sub)
- Module registry (lifecycle management)
- Config framework (JSON-backed, typed accessors)
- Storage manager (persistent data, write-through cache)
- Hypixel & SkyBlock detection (scoreboard-based)
- Scoreboard reader & action bar parser
- Player state tracker (health, mana, defense, speed)
- Network helpers
- Inventory utilities (SkyBlock item NBT parsing)
- Color, math, and string utilities

### Planned Modules

| Module | Description | Status |
|--------|-------------|--------|
| SkyOS Storage Overlay | Modern NEU-style storage grid | Planned |
| SkyOS HUD | Modular SkyBlock HUD | Planned |
| SkyOS Utilities | QoL features | Planned |
| SkyOS Dungeons | Dungeon overlays | Planned |
| SkyOS Bazaar | Economy utilities | Planned |

---

## Design

SkyOS uses a premium dark futuristic visual identity:
- Deep dark backgrounds (`#0A0E1A`)
- Blue/purple accent system (`#4A9EFF` / `#7B4FFF`)
- Glow effects, gradients, rounded surfaces
- Smooth easing animations throughout
- Zero vanilla UI aesthetics

See [Design Language](docs/branding/design-language.md).

---

## Building

```bash
git clone https://github.com/onlocsbyte/SkyOS.git
cd SkyOS/skyos-core
./gradlew build
```

Requires JDK 21.

---

## Module Development

See [Module Development Guide](docs/modules/module-development.md).

---

## References

- [NotEnoughUpdates](https://github.com/Moulberry/NotEnoughUpdates)
- [SkyHanni](https://github.com/hannibal002/SkyHanni)
- [Skyblocker](https://github.com/SkyblockerMod/Skyblocker)
- [Firmament](https://github.com/FirmamentMC/Firmament)

---

## License

GNU GPL v3. See [LICENSE](LICENSE).
