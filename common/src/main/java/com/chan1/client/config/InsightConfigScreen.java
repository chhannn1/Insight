package com.chan1.client.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

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
