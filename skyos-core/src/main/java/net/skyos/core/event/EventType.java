package net.skyos.core.event;

public final class EventType<T> {

    private final String name;

    private EventType(String name) {
        this.name = name;
    }

    public static <T> EventType<T> create(String name) {
        return new EventType<>(name);
    }

    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return "EventType[" + name + "]";
    }
}
