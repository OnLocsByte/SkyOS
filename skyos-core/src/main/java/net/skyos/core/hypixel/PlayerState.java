package net.skyos.core.hypixel;

import net.minecraft.client.MinecraftClient;

public final class PlayerState {

    private static PlayerState instance;

    private int health;
    private int maxHealth;
    private int defense;
    private int mana;
    private int maxMana;
    private float speed;
    private String skyBlockProfile = "";
    private String area = "";

    public PlayerState() {
        instance = this;
    }

    public static PlayerState getInstance() {
        return instance;
    }

    public void update(MinecraftClient client) {
        if (client.player == null) return;
        // Health and absorption from vanilla (base stats)
        this.health = (int) client.player.getHealth();
        this.maxHealth = (int) client.player.getMaxHealth();
        this.speed = client.player.getSpeed();
        // SkyBlock-specific stats are parsed from the action bar
    }

    public void parseFromActionBar(String actionBar) {
        // Action bar format: "§c❤ 14,230/14,230❤  §a§l✈ 100% §b✎ 1,380 Mana §3Ⓞ 100"
        parseHealth(actionBar);
        parseMana(actionBar);
        parseDefense(actionBar);
    }

    private void parseHealth(String bar) {
        try {
            int heartIdx = bar.indexOf("❤");
            if (heartIdx == -1) return;
            // Find numbers before ❤
            int start = bar.lastIndexOf(' ', heartIdx) + 1;
            String segment = bar.substring(start, heartIdx).replaceAll("[^0-9/,]", "");
            String[] parts = segment.split("/");
            if (parts.length == 2) {
                this.health = Integer.parseInt(parts[0].replace(",", ""));
                this.maxHealth = Integer.parseInt(parts[1].replace(",", ""));
            }
        } catch (Exception ignored) {}
    }

    private void parseMana(String bar) {
        try {
            int manaIdx = bar.indexOf("Mana");
            if (manaIdx == -1) return;
            int spaceIdx = bar.lastIndexOf(' ', manaIdx - 2);
            String segment = bar.substring(spaceIdx, manaIdx).replaceAll("[^0-9,]", "");
            this.mana = Integer.parseInt(segment.replace(",", ""));
        } catch (Exception ignored) {}
    }

    private void parseDefense(String bar) {
        try {
            int defIdx = bar.indexOf("❈");
            if (defIdx == -1) return;
            int spaceIdx = bar.lastIndexOf(' ', defIdx) + 1;
            String segment = bar.substring(spaceIdx, defIdx).replaceAll("[^0-9,]", "");
            this.defense = Integer.parseInt(segment.replace(",", ""));
        } catch (Exception ignored) {}
    }

    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public int getDefense() { return defense; }
    public int getMana() { return mana; }
    public int getMaxMana() { return maxMana; }
    public float getSpeed() { return speed; }
    public String getSkyBlockProfile() { return skyBlockProfile; }
    public void setSkyBlockProfile(String profile) { this.skyBlockProfile = profile; }
    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
}
