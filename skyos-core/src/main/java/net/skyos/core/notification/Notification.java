package net.skyos.core.notification;

public final class Notification {

    private final String title;
    private final String message;
    private final NotificationType type;
    private final long durationMs;
    private final long createdAt;

    private float alpha = 0f;
    private float slideOffset = 320f;
    private boolean dismissed = false;

    public Notification(String title, String message, NotificationType type, long durationMs) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.durationMs = durationMs;
        this.createdAt = System.currentTimeMillis();
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - createdAt > durationMs;
    }

    public boolean isDismissed() { return dismissed; }
    public void dismiss() { this.dismissed = true; }

    public float getLifeProgress() {
        return Math.min(1f, (float)(System.currentTimeMillis() - createdAt) / durationMs);
    }

    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public NotificationType getType() { return type; }
    public float getAlpha() { return alpha; }
    public float getSlideOffset() { return slideOffset; }
    public void setAlpha(float alpha) { this.alpha = alpha; }
    public void setSlideOffset(float offset) { this.slideOffset = offset; }
}
