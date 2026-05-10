# SkyOS

A feature-rich Hypixel Skyblock client mod for Minecraft 1.21.1, built with Fabric and Kotlin.

## Features

- **Config GUI** — Clean, dark-themed settings screen (open with `/skyos`)
- **Firmament Announcer Remover** — Filters Firmament update/announcement messages from chat
- **Auto Updater** — Automatically checks for new releases on server join and downloads them silently; installs on next game restart
- **Mod Hider** — Suppresses mod channel registration packets so servers cannot detect which Fabric mods are active

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.21.1
2. Install [Fabric API](https://modrinth.com/mod/fabric-api)
3. Install [Fabric Language Kotlin](https://modrinth.com/mod/fabric-language-kotlin)
4. Drop `skyos-*.jar` into your `mods/` folder

## Building

```bash
./gradlew build
```

The output jar is in `build/libs/`.

## Usage

| Command   | Action                |
|-----------|-----------------------|
| `/skyos`  | Open the config GUI   |

## License

MIT — see [LICENSE](LICENSE)
