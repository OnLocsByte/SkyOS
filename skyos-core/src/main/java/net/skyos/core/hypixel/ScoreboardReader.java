package net.skyos.core.hypixel;

import net.minecraft.client.MinecraftClient;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardScore;
import net.minecraft.scoreboard.ScoreHolder;

import java.util.*;

public final class ScoreboardReader {

    private ScoreboardReader() {}

    public static String readSidebarTitle(MinecraftClient client) {
        if (client.world == null) return null;
        Scoreboard scoreboard = client.world.getScoreboard();
        ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        if (objective == null) return null;
        return stripFormatting(objective.getDisplayName().getString());
    }

    public static List<String> readSidebarLines(MinecraftClient client) {
        if (client.world == null) return List.of();
        Scoreboard scoreboard = client.world.getScoreboard();
        ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        if (objective == null) return List.of();

        List<String> lines = new ArrayList<>();
        scoreboard.getKnownScoreHolders().stream()
                .filter(holder -> scoreboard.getScore(holder, objective) != null)
                .sorted(Comparator.comparingInt(holder -> {
                    ScoreboardScore score = scoreboard.getScore(holder, objective);
                    return score != null ? -score.getScore() : 0;
                }))
                .forEach(holder -> lines.add(stripFormatting(holder.getNameForScoreboard())));
        return Collections.unmodifiableList(lines);
    }

    public static String readLocation(MinecraftClient client) {
        List<String> lines = readSidebarLines(client);
        // Hypixel SkyBlock shows location on the scoreboard
        // Usually formatted as " ⏣ LocationName" or "⏣ LocationName"
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("⏣") || trimmed.startsWith("ф")) {
                return trimmed.replaceFirst("[⏣ф]\\s*", "").trim();
            }
        }
        return "Unknown";
    }

    public static String stripFormatting(String text) {
        if (text == null) return null;
        return text.replaceAll("§[0-9a-fk-or]", "").trim();
    }
}
