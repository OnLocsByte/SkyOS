# SkyOS Core Architecture

## Overview

SkyOS Core is the foundation of the SkyOS ecosystem. It provides all shared infrastructure that module authors build on top of.

## Entry Point

`SkyOSCoreClient` (implements `ClientModInitializer`) is the root of the entire system. It instantiates and wires all subsystems together during `onInitializeClient`.

## Subsystem Dependency Graph

```
SkyOSCoreClient
├── EventBus
├── StorageManager
├── SkyOSConfig ──────────── StorageManager
├── RenderEngine ──────────── AnimationEngine
├── OverlayManager ─────────── RenderEngine
├── NotificationManager ────── OverlayManager
├── HypixelDetector ────────── EventBus
└── ModuleRegistry ─────────── EventBus, SkyOSConfig
```

## Event System

`EventBus` is a lightweight, type-safe publish-subscribe bus.

- `EventType<T>` is a typed token used as the key for subscriptions and publications
- All built-in event types are defined in `SkyOSEvents`
- Events are posted synchronously on the calling thread
- Listener exceptions are caught and logged to prevent cascading failures

## Module System

`ModuleRegistry` manages the lifecycle of `SkyOSModule` implementations.

- Modules register themselves during their `ClientModInitializer`
- Registration reads enabled state from `SkyOSConfig`
- `onEnable` / `onDisable` are called on state change
- Modules are organized by `ModuleCategory` for GUI presentation

## Rendering

`RenderEngine` is the single source for all drawing operations. It wraps Minecraft's `DrawContext` and provides:

- Solid and gradient fills
- Rounded rectangles (approximated via fill operations)
- Glow effects (stacked translucent layers)
- Glowing borders
- Progress bars with gradient fill
- Separators
- Panel / card primitives using the SkyOS palette

All colors are sourced from `SkyOSPalette`. No hardcoded color values appear in rendering code.

## Animation

`AnimationEngine` ticks `AnimatedFloat` instances on every rendered frame via the `GameRendererMixin`.

- `AnimatedFloat` holds a current and target value, animating between them over a configurable duration
- `Easing` provides a set of standard easing curves (quad, cubic, back, elastic, expo)
- Overlays and components create `AnimatedFloat` instances and poll `.get()` during render

## Overlay System

`OverlayManager` maintains a sorted list of `Overlay` implementations.

- Each `Overlay` declares its anchor (9-point grid or custom), dimensions, and z-index
- During HUD render, overlays are resolved to screen coordinates based on anchor and current window size
- Render exceptions per overlay are isolated — one broken overlay cannot crash the HUD

## Hypixel / SkyBlock Detection

`HypixelDetector` runs on every client tick.

- Hypixel is detected by matching the server address against `hypixel.net`
- SkyBlock is detected by reading the scoreboard sidebar title via `ScoreboardReader`
- Location is parsed from the sidebar lines (⏣ symbol prefix)
- `SKYBLOCK_JOIN` / `SKYBLOCK_LEAVE` events are posted on state change

## Config

`SkyOSConfig` is a JSON document stored via `StorageManager`. It provides typed accessors for primitive values and module enabled states. It is loaded at startup and saved on `CLIENT_STOPPING`.

## Storage

`StorageManager` persists data to `config/skyos/` in the game directory. It maintains an in-memory write-through cache. All file names are derived by sanitizing the storage key.

## Notifications

`NotificationManager` shows toast-style notifications in the bottom-right corner of the screen. Notifications slide in and fade out with smooth animation. A queue handles overflow when more than 5 are active simultaneously.
