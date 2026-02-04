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

    public enum FilterCategory {
        STONE("cobblestone", "cobbled_deepslate", "andesite", "diorite", "granite", "tuff", "netherrack", "basalt", "blackstone"),
        DIRT_SAND("dirt", "coarse_dirt", "gravel", "sand", "red_sand", "clay_ball"),
        LOGS("oak_log", "spruce_log", "birch_log", "jungle_log", "acacia_log", "dark_oak_log", "mangrove_log", "cherry_log"),
        COPPER("raw_copper", "copper_ingot", "copper_block"),
        MOB_DROPS("rotten_flesh", "poisonous_potato", "spider_eye", "bone", "arrow", "string", "gunpowder"),
        SEEDS_MISC("wheat_seeds", "stick");

        private final List<String> itemIds;

        FilterCategory(String... items) {
            List<String> ids = new ArrayList<>();
            for (String item : items) {
                ids.add("minecraft:" + item);
            }
            this.itemIds = List.copyOf(ids);
        }

        public List<String> getItemIds() {
            return itemIds;
        }
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

        public boolean filterItems = false;
        public boolean filterStone = false;
        public boolean filterDirtSand = false;
        public boolean filterLogs = false;
        public boolean filterCopper = false;
        public boolean filterMobDrops = false;
        public boolean filterSeedsMisc = false;
        public List<String> customFilteredItems = new ArrayList<>();
    }

    private static Set<Item> resolvedFilteredItems = null;

    public static boolean isFilterItemsEnabled() {
        return data.filterItems;
    }

    public static void setFilterItemsEnabled(boolean enabled) {
        data.filterItems = enabled;
        resolvedFilteredItems = null;
    }

    public static boolean isFilterStone() { return data.filterStone; }
    public static void setFilterStone(boolean v) { data.filterStone = v; resolvedFilteredItems = null; }

    public static boolean isFilterDirtSand() { return data.filterDirtSand; }
    public static void setFilterDirtSand(boolean v) { data.filterDirtSand = v; resolvedFilteredItems = null; }

    public static boolean isFilterLogs() { return data.filterLogs; }
    public static void setFilterLogs(boolean v) { data.filterLogs = v; resolvedFilteredItems = null; }

    public static boolean isFilterCopper() { return data.filterCopper; }
    public static void setFilterCopper(boolean v) { data.filterCopper = v; resolvedFilteredItems = null; }

    public static boolean isFilterMobDrops() { return data.filterMobDrops; }
    public static void setFilterMobDrops(boolean v) { data.filterMobDrops = v; resolvedFilteredItems = null; }

    public static boolean isFilterSeedsMisc() { return data.filterSeedsMisc; }
    public static void setFilterSeedsMisc(boolean v) { data.filterSeedsMisc = v; resolvedFilteredItems = null; }

    public static List<String> getCustomFilteredItems() { return data.customFilteredItems; }
    public static void setCustomFilteredItems(List<String> items) {
        data.customFilteredItems = items;
        resolvedFilteredItems = null;
    }

    public static Set<Item> getResolvedFilteredItems() {
        if (resolvedFilteredItems == null) {
            resolvedFilteredItems = new HashSet<>();
            List<String> allIds = new ArrayList<>();

            if (data.filterStone) allIds.addAll(FilterCategory.STONE.getItemIds());
            if (data.filterDirtSand) allIds.addAll(FilterCategory.DIRT_SAND.getItemIds());
            if (data.filterLogs) allIds.addAll(FilterCategory.LOGS.getItemIds());
            if (data.filterCopper) allIds.addAll(FilterCategory.COPPER.getItemIds());
            if (data.filterMobDrops) allIds.addAll(FilterCategory.MOB_DROPS.getItemIds());
            if (data.filterSeedsMisc) allIds.addAll(FilterCategory.SEEDS_MISC.getItemIds());
            allIds.addAll(data.customFilteredItems);

            for (String id : allIds) {
                Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(id));
                if (item != net.minecraft.world.item.Items.AIR) {
                    resolvedFilteredItems.add(item);
                }
            }
        }
        return resolvedFilteredItems;
    }

    public static boolean isItemFiltered(Item item) {
        return data.filterItems && getResolvedFilteredItems().contains(item);
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
