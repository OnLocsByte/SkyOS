package net.skyos.core.event;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class EventBus {

    private final Map<EventType<?>, List<Consumer<Object>>> listeners = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <T> void subscribe(EventType<T> type, Consumer<T> listener) {
        listeners.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>())
                 .add((Consumer<Object>) listener);
    }

    public <T> void unsubscribe(EventType<T> type, Consumer<T> listener) {
        List<Consumer<Object>> list = listeners.get(type);
        if (list != null) list.remove(listener);
    }

    @SuppressWarnings("unchecked")
    public <T> void post(EventType<T> type, T event) {
        List<Consumer<Object>> list = listeners.get(type);
        if (list == null || list.isEmpty()) return;
        for (Consumer<Object> listener : list) {
            try {
                listener.accept(event);
            } catch (Exception e) {
                SkyOSEvents.LOGGER.error("Exception in event listener for {}", type.name(), e);
            }
        }
    }
}
