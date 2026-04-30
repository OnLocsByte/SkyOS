# Contributing to SkyOS

SkyOS is a premium modular Hypixel SkyBlock ecosystem for Minecraft Fabric. Contributions are welcome if they align with the project's goals and design standards.

## Before You Start

Read the full documentation in `docs/` before contributing. SkyOS has strict standards for code quality, architecture, and visual design.

## Scope

SkyOS targets **only**:
- Minecraft 1.21.10 / 1.21.11
- Fabric Loader
- Hypixel SkyBlock gameplay

Do not submit PRs that add:
- Forge/NeoForge support
- Vanilla Minecraft features
- General multiplayer compatibility
- Older Minecraft version support

## Code Standards

- Java 21 with modern language features (records, sealed classes, pattern matching)
- No placeholder/TODO implementations — all code must be production-ready
- No dead code, commented-out blocks, or mock systems
- Follow the existing package and naming conventions
- All rendering must use `RenderEngine` primitives and `SkyOSPalette` colors
- No hardcoded color values outside `SkyOSPalette`

## Visual Standards

All UI must match the SkyOS design language:
- Dark surfaces (`BG_PRIMARY`, `BG_CARD`)
- Blue/purple accent colors (`ACCENT_PRIMARY`, `ACCENT_SECONDARY`)
- Glow effects on active elements
- Rounded corners (radius 4–8px)
- Smooth animations via `AnimatedFloat` / `AnimationEngine`

## Pull Request Process

1. Fork the repo and create a feature branch from `develop`
2. Write real, working code (no stubs)
3. Test in-game on Hypixel SkyBlock
4. Open a PR against `develop` with a clear description
5. Reference any related issues

## Module Development

When building a new SkyOS module:
1. Implement `SkyOSModule` interface
2. Register with `ModuleRegistry` during your `ClientModInitializer`
3. Depend on `skyos-core` in your `build.gradle.kts`
4. Use `OverlayManager` for any HUD rendering
5. Use `NotificationManager.push()` for user-facing notifications
6. Store data via `StorageManager`

## License

By contributing, you agree your code will be licensed under GPL-3.0.
