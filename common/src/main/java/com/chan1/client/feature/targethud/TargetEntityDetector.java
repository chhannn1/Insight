package com.chan1.client.feature.targethud;

import com.chan1.client.util.detection.CrosshairDetector;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;


public final class TargetEntityDetector {

    private static LivingEntity cachedTarget = null;



    public static LivingEntity getHoveredEntity() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            cachedTarget = null;
            return null;
        }

        Player player = minecraft.player;
        LivingEntity target = CrosshairDetector.getTargetedEntity(
                LivingEntity.class,
                entity -> isValidTarget(entity, player)
        );

        cachedTarget = target;
        return target;
    }


    public static boolean isValidTarget(LivingEntity entity, Player player) {
        if (!entity.isAlive()) {
            return false;
        }
        if (entity == player) {
            return false;
        }
        if (entity.isInvisible()) {
            return false;
        }
        return true;
    }


    public static LivingEntity getCachedTarget() {
        return cachedTarget;
    }


    public static void clearCache() {
        cachedTarget = null;
    }
}
