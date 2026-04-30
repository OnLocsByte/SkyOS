# Getting Started with SkyOS Development

## Prerequisites

- JDK 21 (Temurin recommended)
- IntelliJ IDEA (recommended) or any Java IDE
- Git

## Setup

```bash
git clone https://github.com/onlocsbyte/SkyOS.git
cd SkyOS/skyos-core

# Generate IDE run configurations
./gradlew genSources idea   # IntelliJ
./gradlew genSources eclipse # Eclipse
```

## Building

```bash
cd skyos-core
./gradlew build
```

Output: `skyos-core/build/libs/skyos-core-1.0.0.jar`

## Running in Development

```bash
./gradlew runClient
```

This starts a Fabric development client. Connect to Hypixel to test SkyBlock-specific features.

## Project Layout

```
SkyOS/
├── skyos-core/          # The main Fabric mod
│   └── src/main/java/net/skyos/core/
│       ├── SkyOSCoreClient.java   # Entry point
│       ├── api/                    # Public API interfaces
│       ├── render/                 # Rendering system
│       ├── overlay/                # Overlay management
│       ├── config/                 # Configuration
│       ├── hypixel/                # Server detection
│       ├── network/                # Network helpers
│       ├── inventory/              # Inventory utilities
│       ├── storage/                # Data persistence
│       ├── notification/           # Notification system
│       ├── util/                   # Shared utilities
│       └── mixin/                  # Minecraft injections
├── modules/             # Planned module stubs
├── docs/                # Documentation
└── .github/             # CI and issue templates
```

## Code Style

- Java 21, modern features preferred (records, sealed, switch expressions)
- 4-space indentation
- Final fields and classes where possible
- No `null` returns from public APIs — use `Optional<T>`
- All public APIs must compile without errors or warnings

## Testing

Test all features in-game on Hypixel SkyBlock. Key scenarios to validate:

1. Connect to Hypixel — verify detection fires
2. Enter SkyBlock — verify `SKYBLOCK_JOIN` event fires
3. Open chest — verify `InventoryHelper` reads items correctly
4. Walk around — verify scoreboard location updates
5. Trigger a notification — verify slide-in animation plays
