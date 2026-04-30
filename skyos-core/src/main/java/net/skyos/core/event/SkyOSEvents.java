package net.skyos.core.event;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SkyOSEvents {

    static final Logger LOGGER = LoggerFactory.getLogger("SkyOSEvents");

    public static final EventType<MinecraftClient> CLIENT_TICK = EventType.create("client_tick");
    public static final EventType<ClientPlayNetworkHandler> JOIN_SERVER = EventType.create("join_server");
    public static final EventType<ClientPlayNetworkHandler> LEAVE_SERVER = EventType.create("leave_server");
    public static final EventType<SkyBlockStateEvent> SKYBLOCK_JOIN = EventType.create("skyblock_join");
    public static final EventType<SkyBlockStateEvent> SKYBLOCK_LEAVE = EventType.create("skyblock_leave");
    public static final EventType<String> CHAT_MESSAGE = EventType.create("chat_message");
    public static final EventType<String> ACTION_BAR = EventType.create("action_bar");

    public record SkyBlockStateEvent(boolean joined, String location) {}

    private SkyOSEvents() {}
}
