# SkyOS Module Development Guide

## Overview

SkyOS modules are separate Fabric mods that depend on `skyos-core`. Each module should implement `SkyOSModule` and register with `ModuleRegistry`.

## Minimum Setup

### `build.gradle.kts`

```kotlin
dependencies {
    modImplementation("net.skyos:skyos-core:1.0.0")
    // ... other deps
}
```

### Module Class

```java
public final class MyModule implements SkyOSModule {

    @Override public String getId() { return "my-module"; }
    @Override public String getDisplayName() { return "My Module"; }
    @Override public String getDescription() { return "Does something useful."; }
    @Override public ModuleCategory getCategory() { return ModuleCategory.UTILITY; }

    @Override
    public void onEnable() {
        // Wire events, register overlays, etc.
    }

    @Override
    public void onDisable() {
        // Unregister overlays, clean up listeners
    }
}
```

### Registration

```java
public class MyModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ModuleRegistry.getInstance().register(new MyModule());
    }
}
```

## Using Core APIs

### Subscribing to Events

```java
SkyOSCoreClient.getInstance().getEventBus()
    .subscribe(SkyOSEvents.SKYBLOCK_JOIN, event -> {
        // Triggered when the player enters SkyBlock
        String location = event.location();
    });
```

### Registering an Overlay

```java
OverlayManager overlayManager = SkyOSCoreClient.getInstance().getOverlayManager();
overlayManager.register(new MyOverlay());
```

```java
public class MyOverlay implements Overlay {

    private final RenderEngine re = SkyOSCoreClient.getInstance().getRenderEngine();

    @Override public String getId() { return "my-overlay"; }
    @Override public OverlayAnchor getAnchor() { return OverlayAnchor.TOP_RIGHT; }
    @Override public int getX() { return -8; }
    @Override public int getY() { return 8; }
    @Override public int getWidth() { return 200; }
    @Override public int getHeight() { return 120; }
    @Override public boolean isVisible() { return true; }

    @Override
    public void render(DrawContext ctx, float tickDelta) {
        re.drawPanel(ctx, getX(), getY(), getWidth(), getHeight());
        // Draw content...
    }
}
```

### Notifications

```java
SkyOSCoreClient.getInstance().getNotificationManager()
    .push("SkyOS", "Module enabled!", NotificationType.SUCCESS);
```

### Config

```java
SkyOSConfig config = SkyOSCoreClient.getInstance().getConfig();

// Read
boolean enabled = config.getBoolean("my-module.feature-x", true);

// Write (auto-saved on game stop)
config.setBoolean("my-module.feature-x", false);
```

### Storage

```java
StorageManager storage = SkyOSCoreClient.getInstance().getStorageManager();

// Write JSON
storage.writeJson("my-module-data", myDataObject);

// Read JSON
Optional<MyData> data = storage.readJson("my-module-data", MyData.class);
```

## Rendering Guidelines

Always use `RenderEngine` primitives. Never call `DrawContext.fill()` directly with raw color values.

```java
RenderEngine re = SkyOSCoreClient.getInstance().getRenderEngine();

// Draw a themed card panel
re.drawPanel(ctx, x, y, width, height);

// Draw text on it
ctx.drawTextWithShadow(client.textRenderer, "Hello", x + 8, y + 8, SkyOSPalette.TEXT_PRIMARY);
```

## Animation

```java
AnimationEngine anim = SkyOSCoreClient.getInstance().getRenderEngine().getAnimationEngine();

// Create a tracked animated float
AnimatedFloat opacity = anim.create(0f, Easing.Type.EASE_OUT_QUAD, 200f);

// In onEnable
opacity.animateTo(1f);

// In render
ctx.setShaderColor(1f, 1f, 1f, opacity.get());
// ... draw stuff
ctx.setShaderColor(1f, 1f, 1f, 1f);
```

## Hypixel / SkyBlock State

```java
HypixelDetector detector = SkyOSCoreClient.getInstance().getHypixelDetector();

boolean onSkyBlock = detector.isOnSkyBlock();
String location = detector.getSkyBlockLocation();
```
