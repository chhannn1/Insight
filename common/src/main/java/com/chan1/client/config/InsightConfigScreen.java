package com.chan1.client.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.api.Requirement;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InsightConfigScreen {

    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("config.insight.title"))
                .setSavingRunnable(InsightConfig::save);

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        ConfigCategory itemTooltips = builder.getOrCreateCategory(
                Component.translatable("config.insight.category.item_tooltips"));

        itemTooltips.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("config.insight.itemTooltips.enabled"),
                        InsightConfig.isItemTooltipsEnabled())
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.insight.itemTooltips.enabled.tooltip"))
                .setSaveConsumer(InsightConfig::setItemTooltipsEnabled)
                .build());

        itemTooltips.addEntry(entryBuilder.startEnumSelector(
                        Component.translatable("config.insight.itemTooltips.detectionMode"),
                        InsightConfig.DetectionMode.class,
                        InsightConfig.getDetectionMode())
                .setDefaultValue(InsightConfig.DetectionMode.AREA)
                .setTooltip(Component.translatable("config.insight.itemTooltips.detectionMode.tooltip"))
                .setSaveConsumer(InsightConfig::setDetectionMode)
                .build());

        itemTooltips.addEntry(entryBuilder.startIntSlider(
                        Component.translatable("config.insight.itemTooltips.maxTooltips"),
                        InsightConfig.getMaxDisplayedTooltips(),
                        1, 10)
                .setDefaultValue(5)
                .setTooltip(Component.translatable("config.insight.itemTooltips.maxTooltips.tooltip"))
                .setSaveConsumer(InsightConfig::setMaxDisplayedTooltips)
                .build());

        BooleanListEntry filterToggle = entryBuilder.startBooleanToggle(
                        Component.translatable("config.insight.itemTooltips.filterCommon"),
                        InsightConfig.isFilterCommonItemsEnabled())
                .setDefaultValue(false)
                .setTooltip(Component.translatable("config.insight.itemTooltips.filterCommon.tooltip"))
                .setSaveConsumer(InsightConfig::setFilterCommonItemsEnabled)
                .build();
        itemTooltips.addEntry(filterToggle);

        itemTooltips.addEntry(entryBuilder.startStrList(
                        Component.translatable("config.insight.itemTooltips.filteredItems"),
                        new ArrayList<>(InsightConfig.getFilteredItemIds()))
                .setExpanded(true)
                .setInsertInFront(false)
                .setAddButtonTooltip(Component.translatable("config.insight.itemTooltips.filteredItems.add"))
                .setRemoveButtonTooltip(Component.translatable("config.insight.itemTooltips.filteredItems.remove"))
                .setCellErrorSupplier(value -> {
                    if (value == null || value.isBlank()) {
                        return Optional.of(Component.literal("Item ID cannot be empty"));
                    }
                    if (!value.contains(":")) {
                        return Optional.of(Component.literal("Use format: namespace:item (e.g. minecraft:cobblestone)"));
                    }
                    return Optional.empty();
                })
                .setDefaultValue(List.of(
                        "minecraft:cobblestone", "minecraft:cobbled_deepslate", "minecraft:andesite",
                        "minecraft:diorite", "minecraft:granite", "minecraft:tuff", "minecraft:netherrack",
                        "minecraft:basalt", "minecraft:blackstone",
                        "minecraft:dirt", "minecraft:coarse_dirt", "minecraft:gravel",
                        "minecraft:sand", "minecraft:red_sand", "minecraft:clay_ball",
                        "minecraft:oak_log", "minecraft:spruce_log", "minecraft:birch_log",
                        "minecraft:jungle_log", "minecraft:acacia_log", "minecraft:dark_oak_log",
                        "minecraft:mangrove_log", "minecraft:cherry_log",
                        "minecraft:raw_copper", "minecraft:copper_ingot", "minecraft:copper_block",
                        "minecraft:rotten_flesh", "minecraft:poisonous_potato", "minecraft:spider_eye",
                        "minecraft:bone", "minecraft:arrow", "minecraft:string", "minecraft:gunpowder",
                        "minecraft:wheat_seeds", "minecraft:stick"))
                .setTooltip(Component.translatable("config.insight.itemTooltips.filteredItems.tooltip"))
                .setSaveConsumer(InsightConfig::setFilteredItemIds)
                .setDisplayRequirement(Requirement.isTrue(filterToggle))
                .build());

        ConfigCategory targetHud = builder.getOrCreateCategory(
                Component.translatable("config.insight.category.target_hud"));

        targetHud.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("config.insight.targetHud.enabled"),
                        InsightConfig.isTargetHudEnabled())
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.insight.targetHud.enabled.tooltip"))
                .setSaveConsumer(InsightConfig::setTargetHudEnabled)
                .build());

        targetHud.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("config.insight.targetHud.smartPositioning"),
                        InsightConfig.isTargetHudSmartPositioning())
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.insight.targetHud.smartPositioning.tooltip"))
                .setSaveConsumer(InsightConfig::setTargetHudSmartPositioning)
                .build());

        ConfigCategory misc = builder.getOrCreateCategory(
                Component.translatable("config.insight.category.misc"));

        misc.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("config.insight.misc.bedTimer"),
                        InsightConfig.isBedTimerEnabled())
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.insight.misc.bedTimer.tooltip"))
                .setSaveConsumer(InsightConfig::setBedTimerEnabled)
                .build());

        misc.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("config.insight.misc.cropGrowth"),
                        InsightConfig.isCropGrowthEnabled())
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.insight.misc.cropGrowth.tooltip"))
                .setSaveConsumer(InsightConfig::setCropGrowthEnabled)
                .build());

        ConfigCategory appearance = builder.getOrCreateCategory(
                Component.translatable("config.insight.category.appearance"));

        appearance.addEntry(entryBuilder.startIntSlider(
                        Component.translatable("config.insight.appearance.scale"),
                        (int) (InsightConfig.getBillboardScale() * 1000),
                        10, 30)
                .setDefaultValue(10)
                .setTooltip(Component.translatable("config.insight.appearance.scale.tooltip"))
                .setTextGetter(value -> Component.literal(String.format("%.1f%%", value / 10.0)))
                .setSaveConsumer(value -> InsightConfig.setBillboardScale(value / 1000.0f))
                .build());

        appearance.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("config.insight.appearance.animations"),
                        InsightConfig.isAnimationsEnabled())
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.insight.appearance.animations.tooltip"))
                .setSaveConsumer(InsightConfig::setAnimationsEnabled)
                .build());

        return builder.build();
    }
}
