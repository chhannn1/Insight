package com.chan1.client.feature.items;

import com.chan1.client.config.InsightConfig;
import com.chan1.client.util.detection.CrosshairDetector;
import net.minecraft.world.entity.item.ItemEntity;

import java.util.List;

public final class DroppedItemDetector {

    public static void tick() {
    }

    public static List<ItemEntity> getNearbyItems() {
        List<ItemEntity> items = CrosshairDetector.getNearbyEntities(
                ItemEntity.class,
                InsightConfig.getDetectionRange(),
                DroppedItemDetector::isValidItem
        );

        int limit = InsightConfig.getMaxDisplayedTooltips();
        if (items.size() > limit) {
            return items.subList(0, limit);
        }
        return items;
    }


    public static ItemEntity getCrosshairItem() {
        return CrosshairDetector.getTargetedEntity(ItemEntity.class, DroppedItemDetector::isValidItem);
    }


    public static boolean isValidItem(ItemEntity item) {
        if (!item.isAlive()) {
            return false;
        }
        return !item.getItem().isEmpty();
    }
}
