package net.skyos.core.network;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket;
import net.skyos.core.SkyOSCoreClient;

public final class NetworkHelper {

    private static final int COMMAND_COMPLETE_TRANSACTION = 1;

    private NetworkHelper() {}

    public static boolean isConnected() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client.getNetworkHandler() != null && client.getNetworkHandler().getConnection().isOpen();
    }

    public static void sendCommandTabComplete(String partial) {
        ClientPlayNetworkHandler handler = MinecraftClient.getInstance().getNetworkHandler();
        if (handler == null) return;
        try {
            handler.sendPacket(new RequestCommandCompletionsC2SPacket(COMMAND_COMPLETE_TRANSACTION, partial));
        } catch (Exception e) {
            SkyOSCoreClient.LOGGER.warn("Failed to send tab complete packet", e);
        }
    }

    public static String getServerAddress() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getCurrentServerEntry() == null) return "";
        return client.getCurrentServerEntry().address;
    }

    public static String getServerBrand() {
        ClientPlayNetworkHandler handler = MinecraftClient.getInstance().getNetworkHandler();
        if (handler == null) return "";
        return handler.getBrand() != null ? handler.getBrand() : "";
    }
}
