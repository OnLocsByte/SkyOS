package net.skyos.core.inventory;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
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
        Slot slot = handler.slots.get(slotIndex);
        ItemStack stack = slot.getStack();
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

    public static Optional<String> getSkyBlockItemId(ItemStack stack) {
        if (stack.isEmpty()) return Optional.empty();
        NbtCompound nbt = stack.getNbt();
        if (nbt == null) return Optional.empty();
        NbtCompound extraAttributes = nbt.getCompound("ExtraAttributes");
        if (extraAttributes == null || !extraAttributes.contains("id")) return Optional.empty();
        return Optional.of(extraAttributes.getString("id"));
    }

    public static Optional<String> getSkullTextureUrl(ItemStack stack) {
        if (stack.isEmpty()) return Optional.empty();
        NbtCompound nbt = stack.getNbt();
        if (nbt == null) return Optional.empty();
        try {
            NbtCompound skullOwner = nbt.getCompound("SkullOwner");
            NbtCompound properties = skullOwner.getCompound("Properties");
            NbtList textures = properties.getList("textures", 10);
            if (textures.isEmpty()) return Optional.empty();
            NbtCompound texture = textures.getCompound(0);
            return Optional.of(texture.getString("Value"));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static List<Text> getLore(ItemStack stack) {
        if (stack.isEmpty()) return List.of();
        NbtCompound nbt = stack.getNbt();
        if (nbt == null) return List.of();
        NbtCompound display = nbt.getCompound("display");
        if (!display.contains("Lore")) return List.of();
        NbtList loreNbt = display.getList("Lore", 8);
        List<Text> lore = new ArrayList<>();
        for (int i = 0; i < loreNbt.size(); i++) {
            lore.add(Text.Serialization.fromJson(loreNbt.getString(i), MinecraftClient.getInstance().world.getRegistryManager()));
        }
        return Collections.unmodifiableList(lore);
    }

    public static boolean isChestName(String name, String... patterns) {
        for (String pattern : patterns) {
            if (name.contains(pattern)) return true;
        }
        return false;
    }
}
