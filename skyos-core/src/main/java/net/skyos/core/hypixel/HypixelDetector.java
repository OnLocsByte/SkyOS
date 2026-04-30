package net.skyos.core.hypixel;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ServerInfo;
import net.skyos.core.event.EventBus;
import net.skyos.core.event.SkyOSEvents;
import net.skyos.core.SkyOSCoreClient;

public final class HypixelDetector {

    private static final String HYPIXEL_HOST = "hypixel.net";

    private final EventBus eventBus;

    private boolean onHypixel = false;
    private boolean onSkyBlock = false;
    private String skyBlockLocation = "";
    private String scoreboard = "";

    public HypixelDetector(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void tick(MinecraftClient client) {
        boolean wasHypixel = onHypixel;
        boolean wasSkyBlock = onSkyBlock;

        onHypixel = detectHypixel(client);

        if (onHypixel) {
            detectSkyBlock(client);
        } else {
            onSkyBlock = false;
            skyBlockLocation = "";
        }

        if (!wasHypixel && onHypixel) {
            SkyOSCoreClient.LOGGER.info("Detected Hypixel connection.");
        }

        if (!wasSkyBlock && onSkyBlock) {
            eventBus.post(SkyOSEvents.SKYBLOCK_JOIN, new SkyOSEvents.SkyBlockStateEvent(true, skyBlockLocation));
            SkyOSCoreClient.LOGGER.info("Detected SkyBlock location: {}", skyBlockLocation);
        } else if (wasSkyBlock && !onSkyBlock) {
            eventBus.post(SkyOSEvents.SKYBLOCK_LEAVE, new SkyOSEvents.SkyBlockStateEvent(false, skyBlockLocation));
        }
    }

    private boolean detectHypixel(MinecraftClient client) {
        ServerInfo serverInfo = client.getCurrentServerEntry();
        if (serverInfo == null) return false;
        String address = serverInfo.address.toLowerCase();
        return address.contains(HYPIXEL_HOST);
    }

    private void detectSkyBlock(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            onSkyBlock = false;
            return;
        }

        // Parse scoreboard for SkyBlock indicator
        // SkyBlock scoreboards always contain "SKYBLOCK" in the title or sidebar
        scoreboard = ScoreboardReader.readSidebarTitle(client);

        boolean detected = scoreboard != null &&
                (scoreboard.contains("SKYBLOCK") || scoreboard.contains("SkyBlock"));

        if (detected) {
            onSkyBlock = true;
            skyBlockLocation = ScoreboardReader.readLocation(client);
        } else {
            onSkyBlock = false;
            skyBlockLocation = "";
        }
    }

    public boolean isOnHypixel() { return onHypixel; }
    public boolean isOnSkyBlock() { return onSkyBlock; }
    public String getSkyBlockLocation() { return skyBlockLocation; }
    public String getScoreboard() { return scoreboard; }

    public void onServerJoin(ClientPlayNetworkHandler handler) {
        onHypixel = false;
        onSkyBlock = false;
        skyBlockLocation = "";
        scoreboard = "";
    }

    public void onServerLeave() {
        boolean wasSkyBlock = onSkyBlock;
        onHypixel = false;
        onSkyBlock = false;
        skyBlockLocation = "";
        scoreboard = "";
        if (wasSkyBlock) {
            eventBus.post(SkyOSEvents.SKYBLOCK_LEAVE, new SkyOSEvents.SkyBlockStateEvent(false, ""));
        }
    }
}
