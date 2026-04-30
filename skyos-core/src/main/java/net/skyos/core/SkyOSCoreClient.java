package net.skyos.core;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.skyos.core.config.SkyOSConfig;
import net.skyos.core.event.EventBus;
import net.skyos.core.event.SkyOSEvents;
import net.skyos.core.hypixel.HypixelDetector;
import net.skyos.core.notification.NotificationManager;
import net.skyos.core.overlay.OverlayManager;
import net.skyos.core.render.RenderEngine;
import net.skyos.core.api.module.ModuleRegistry;
import net.skyos.core.storage.StorageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class SkyOSCoreClient implements ClientModInitializer {

    public static final String MOD_ID = "skyos-core";
    public static final String MOD_NAME = "SkyOS Core";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    private static SkyOSCoreClient instance;

    private EventBus eventBus;
    private SkyOSConfig config;
    private RenderEngine renderEngine;
    private OverlayManager overlayManager;
    private NotificationManager notificationManager;
    private HypixelDetector hypixelDetector;
    private ModuleRegistry moduleRegistry;
    private StorageManager storageManager;

    @Override
    public void onInitializeClient() {
        instance = this;
        LOGGER.info("[{}] Initializing SkyOS Core...", MOD_NAME);

        eventBus = new EventBus();
        storageManager = new StorageManager();
        config = new SkyOSConfig(storageManager);
        config.load();

        renderEngine = new RenderEngine();
        overlayManager = new OverlayManager(renderEngine);
        notificationManager = new NotificationManager(overlayManager);
        hypixelDetector = new HypixelDetector(eventBus);
        moduleRegistry = new ModuleRegistry(eventBus, config);

        registerFabricEvents();

        LOGGER.info("[{}] SkyOS Core initialized successfully.", MOD_NAME);
    }

    private void registerFabricEvents() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            overlayManager.tick(client);
            notificationManager.tick();
            hypixelDetector.tick(client);
            eventBus.post(SkyOSEvents.CLIENT_TICK, client);
        });

        HudRenderCallback.EVENT.register((drawContext, tickDeltaManager) -> {
            float tickDelta = tickDeltaManager.getTickDelta(true);
            if (hypixelDetector.isOnSkyBlock()) {
                overlayManager.render(drawContext, tickDelta);
                notificationManager.render(drawContext, tickDelta);
            }
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            config.save();
            storageManager.flush();
            LOGGER.info("[{}] SkyOS Core stopped.", MOD_NAME);
        });
    }

    public static SkyOSCoreClient getInstance() {
        return instance;
    }

    public EventBus getEventBus() { return eventBus; }
    public SkyOSConfig getConfig() { return config; }
    public RenderEngine getRenderEngine() { return renderEngine; }
    public OverlayManager getOverlayManager() { return overlayManager; }
    public NotificationManager getNotificationManager() { return notificationManager; }
    public HypixelDetector getHypixelDetector() { return hypixelDetector; }
    public ModuleRegistry getModuleRegistry() { return moduleRegistry; }
    public StorageManager getStorageManager() { return storageManager; }
}
