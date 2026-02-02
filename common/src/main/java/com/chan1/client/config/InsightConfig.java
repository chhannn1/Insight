package com.chan1.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.architectury.platform.Platform;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class InsightConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = Platform.getConfigFolder().resolve("insight.json");

    public enum DetectionMode {
        AREA,
        CROSSHAIR
    }

    private static ConfigData data = new ConfigData();

    public static class ConfigData {
        public boolean itemTooltipsEnabled = true;
        public boolean targetHudEnabled = true;
        public boolean bedTimerEnabled = true;
        public boolean cropGrowthEnabled = true;

        public boolean targetHudSmartPositioning = true;

        public double tooltipYOffset = 0.5;
        public double maxRenderDistance = 8.0;
        public float billboardScale = 0.010f;
        public boolean animationsEnabled = true;
        public int animationDurationMs = 150;

        public DetectionMode detectionMode = DetectionMode.CROSSHAIR;
        public int maxDisplayedTooltips = 5;

        public boolean filterCommonItems = false;
        public List<String> filteredItems = getDefaultFilteredItems();
    }

    private static List<String> getDefaultFilteredItems() {
        List<String> items = new ArrayList<>();
        items.add("minecraft:cobblestone");
        items.add("minecraft:cobbled_deepslate");
        items.add("minecraft:andesite");
        items.add("minecraft:diorite");
        items.add("minecraft:granite");
        items.add("minecraft:tuff");
        items.add("minecraft:netherrack");
        items.add("minecraft:basalt");
        items.add("minecraft:blackstone");
        items.add("minecraft:dirt");
        items.add("minecraft:coarse_dirt");
        items.add("minecraft:gravel");
        items.add("minecraft:sand");
        items.add("minecraft:red_sand");
        items.add("minecraft:clay_ball");
        items.add("minecraft:oak_log");
        items.add("minecraft:spruce_log");
        items.add("minecraft:birch_log");
        items.add("minecraft:jungle_log");
        items.add("minecraft:acacia_log");
        items.add("minecraft:dark_oak_log");
        items.add("minecraft:mangrove_log");
        items.add("minecraft:cherry_log");
        items.add("minecraft:raw_copper");
        items.add("minecraft:copper_ingot");
        items.add("minecraft:copper_block");
        items.add("minecraft:rotten_flesh");
        items.add("minecraft:poisonous_potato");
        items.add("minecraft:spider_eye");
        items.add("minecraft:bone");
        items.add("minecraft:arrow");
        items.add("minecraft:string");
        items.add("minecraft:gunpowder");
        items.add("minecraft:wheat_seeds");
        items.add("minecraft:stick");
        return items;
    }

    private static Set<Item> resolvedFilteredItems = null;

    public static boolean isFilterCommonItemsEnabled() {
        return data.filterCommonItems;
    }

    public static void setFilterCommonItemsEnabled(boolean enabled) {
        data.filterCommonItems = enabled;
    }

    public static List<String> getFilteredItemIds() {
        return data.filteredItems;
    }

    public static void setFilteredItemIds(List<String> items) {
        data.filteredItems = items;
        resolvedFilteredItems = null;
    }

    public static Set<Item> getResolvedFilteredItems() {
        if (resolvedFilteredItems == null) {
            resolvedFilteredItems = new HashSet<>();
            for (String id : data.filteredItems) {
                Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(id));
                if (item != net.minecraft.world.item.Items.AIR) {
                    resolvedFilteredItems.add(item);
                }
            }
        }
        return resolvedFilteredItems;
    }

    public static boolean isItemFiltered(Item item) {
        return data.filterCommonItems && getResolvedFilteredItems().contains(item);
    }

    public static boolean isItemTooltipsEnabled() {
        return data.itemTooltipsEnabled;
    }

    public static void setItemTooltipsEnabled(boolean enabled) {
        data.itemTooltipsEnabled = enabled;
    }

    public static boolean isTargetHudEnabled() {
        return data.targetHudEnabled;
    }

    public static void setTargetHudEnabled(boolean enabled) {
        data.targetHudEnabled = enabled;
    }

    public static boolean isBedTimerEnabled() {
        return data.bedTimerEnabled;
    }

    public static void setBedTimerEnabled(boolean enabled) {
        data.bedTimerEnabled = enabled;
    }

    public static boolean isCropGrowthEnabled() {
        return data.cropGrowthEnabled;
    }

    public static void setCropGrowthEnabled(boolean enabled) {
        data.cropGrowthEnabled = enabled;
    }

    public static boolean isTargetHudSmartPositioning() {
        return data.targetHudSmartPositioning;
    }

    public static void setTargetHudSmartPositioning(boolean enabled) {
        data.targetHudSmartPositioning = enabled;
    }

    public static DetectionMode getDetectionMode() {
        return data.detectionMode;
    }

    public static void setDetectionMode(DetectionMode mode) {
        data.detectionMode = mode;
    }

    public static int getMaxDisplayedTooltips() {
        return data.maxDisplayedTooltips;
    }

    public static void setMaxDisplayedTooltips(int max) {
        data.maxDisplayedTooltips = max;
    }

    public static double getDetectionRange() {
        return 4.5;
    }

    public static double getTooltipYOffset() {
        return data.tooltipYOffset;
    }

    public static void setTooltipYOffset(double offset) {
        data.tooltipYOffset = offset;
    }

    public static double getMaxRenderDistance() {
        return data.maxRenderDistance;
    }

    public static void setMaxRenderDistance(double distance) {
        data.maxRenderDistance = distance;
    }

    public static float getBillboardScale() {
        return data.billboardScale;
    }

    public static void setBillboardScale(float scale) {
        data.billboardScale = scale;
    }

    public static boolean isAnimationsEnabled() {
        return data.animationsEnabled;
    }

    public static void setAnimationsEnabled(boolean enabled) {
        data.animationsEnabled = enabled;
    }

    public static int getAnimationDurationMs() {
        return data.animationDurationMs;
    }

    public static void setAnimationDurationMs(int durationMs) {
        data.animationDurationMs = durationMs;
    }

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                data = GSON.fromJson(json, ConfigData.class);
                if (data == null) {
                    data = new ConfigData();
                }
            } catch (IOException e) {
                e.printStackTrace();
                data = new ConfigData();
            }
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(data));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private InsightConfig() {
    }
}
