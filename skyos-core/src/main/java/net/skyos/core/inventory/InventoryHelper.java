package net.skyos.core.inventory;

import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;

import java.util.*;

public final class InventoryHelper {

    private InventoryHelper() {}

    public static Optional<ItemStack> getSlot(int slotIndex) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return Optional.empty();
        ScreenHandler handler = client.player.currentScreenHandler;
        if (slotIndex < 0 || slotIndex >= handler.slots.size()) return Optional.empty();
        ItemStack stack = handler.slots.get(slotIndex).getStack();
        return stack.isEmpty() ? Optional.empty() : Optional.of(stack);
    }

    public static List<ItemStack> getAllSlots() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return List.of();
        List<ItemStack> result = new ArrayList<>();
        for (Slot slot : client.player.currentScreenHandler.slots) {
            result.add(slot.getStack());
        }
        return Collections.unmodifiableList(result);
    }

    public static String getDisplayName(ItemStack stack) {
        if (stack.isEmpty()) return "";
        return stack.getName().getString();
    }

    // MC 1.20.5+: ItemStack.getNbt() was removed. Custom NBT is now stored
    // in the CUSTOM_DATA DataComponent (ExtraAttributes for SkyBlock items).
    public static Optional<String> getSkyBlockItemId(ItemStack stack) {
        if (stack.isEmpty()) return Optional.empty();
        NbtComponent customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (customData == null) return Optional.empty();
        NbtCompound nbt = customData.copyNbt();
        if (!nbt.contains("ExtraAttributes")) return Optional.empty();
        NbtCompound ea = nbt.getCompoundOrEmpty("ExtraAttributes");
        if (!ea.contains("id")) return Optional.empty();
        return ea.getString("id");
    }

    // MC 1.20.5+: skull owner data moved to DataComponentTypes.PROFILE.
    public static Optional<String> getSkullTextureUrl(ItemStack stack) {
        if (stack.isEmpty()) return Optional.empty();
        ProfileComponent profile = stack.get(DataComponentTypes.PROFILE);
        if (profile == null) return Optional.empty();
        var textures = profile.getGameProfile().properties().get("textures");
        if (textures.isEmpty()) return Optional.empty();
        return Optional.of(textures.iterator().next().value());
    }

    // MC 1.20.5+: lore moved to DataComponentTypes.LORE.
    public static List<Text> getLore(ItemStack stack) {
        if (stack.isEmpty()) return List.of();
        LoreComponent loreComp = stack.get(DataComponentTypes.LORE);
        if (loreComp == null) return List.of();
        return Collections.unmodifiableList(loreComp.lines());
    }

    public static boolean isChestName(String name, String... patterns) {
        for (String pattern : patterns) {
            if (name.contains(pattern)) return true;
        }
        return false;
    }
}
