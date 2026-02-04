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
                        Component.translatable("config.insight.itemTooltips.filterItems"),
                        InsightConfig.isFilterItemsEnabled())
                .setDefaultValue(false)
                .setTooltip(Component.translatable("config.insight.itemTooltips.filterItems.tooltip"))
                .setSaveConsumer(InsightConfig::setFilterItemsEnabled)
                .build();
        itemTooltips.addEntry(filterToggle);

        Requirement filterEnabled = Requirement.isTrue(filterToggle);

        itemTooltips.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("config.insight.filter.stone"),
                        InsightConfig.isFilterStone())
                .setDefaultValue(false)
                .setTooltip(Component.translatable("config.insight.filter.stone.tooltip"))
                .setSaveConsumer(InsightConfig::setFilterStone)
                .setDisplayRequirement(filterEnabled)
                .build());

        itemTooltips.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("config.insight.filter.dirtSand"),
                        InsightConfig.isFilterDirtSand())
                .setDefaultValue(false)
                .setTooltip(Component.translatable("config.insight.filter.dirtSand.tooltip"))
                .setSaveConsumer(InsightConfig::setFilterDirtSand)
                .setDisplayRequirement(filterEnabled)
                .build());

        itemTooltips.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("config.insight.filter.logs"),
                        InsightConfig.isFilterLogs())
                .setDefaultValue(false)
                .setTooltip(Component.translatable("config.insight.filter.logs.tooltip"))
                .setSaveConsumer(InsightConfig::setFilterLogs)
                .setDisplayRequirement(filterEnabled)
                .build());

        itemTooltips.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("config.insight.filter.copper"),
                        InsightConfig.isFilterCopper())
                .setDefaultValue(false)
                .setTooltip(Component.translatable("config.insight.filter.copper.tooltip"))
                .setSaveConsumer(InsightConfig::setFilterCopper)
                .setDisplayRequirement(filterEnabled)
                .build());

        itemTooltips.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("config.insight.filter.mobDrops"),
                        InsightConfig.isFilterMobDrops())
                .setDefaultValue(false)
                .setTooltip(Component.translatable("config.insight.filter.mobDrops.tooltip"))
                .setSaveConsumer(InsightConfig::setFilterMobDrops)
                .setDisplayRequirement(filterEnabled)
                .build());

        itemTooltips.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("config.insight.filter.seedsMisc"),
                        InsightConfig.isFilterSeedsMisc())
                .setDefaultValue(false)
                .setTooltip(Component.translatable("config.insight.filter.seedsMisc.tooltip"))
                .setSaveConsumer(InsightConfig::setFilterSeedsMisc)
                .setDisplayRequirement(filterEnabled)
                .build());

        itemTooltips.addEntry(entryBuilder.startStrList(
                        Component.translatable("config.insight.filter.customItems"),
                        new ArrayList<>(InsightConfig.getCustomFilteredItems()))
                .setExpanded(true)
                .setInsertInFront(false)
                .setAddButtonTooltip(Component.translatable("config.insight.filter.customItems.add"))
                .setRemoveButtonTooltip(Component.translatable("config.insight.filter.customItems.remove"))
                .setCellErrorSupplier(value -> {
                    if (value == null || value.isBlank()) {
                        return Optional.of(Component.literal("Item ID cannot be empty"));
                    }
                    if (!value.contains(":")) {
                        return Optional.of(Component.literal("Use format: namespace:item (e.g. minecraft:cobblestone)"));
                    }
                    return Optional.empty();
                })
                .setDefaultValue(List.of())
                .setTooltip(Component.translatable("config.insight.filter.customItems.tooltip"))
                .setSaveConsumer(InsightConfig::setCustomFilteredItems)
                .setDisplayRequirement(filterEnabled)
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
