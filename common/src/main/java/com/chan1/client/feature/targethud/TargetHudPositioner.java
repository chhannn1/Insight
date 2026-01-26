package com.chan1.client.feature.targethud;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

// thanks to 0xjen for the help with this

public final class TargetHudPositioner {

    private static final float POSITION_LERP_SPEED = 0.035f;
    private static final double HUD_OFFSET_DISTANCE = 0.85;

    private static final Map<Integer, Vec3> currentPositions = new HashMap<>();

    private TargetHudPositioner() {
    }

    public static Vec3 calculateBillboardPosition(
            LivingEntity entity,
            Vec3 entityPos,
            Camera camera,
            double yOffset,
            boolean smartEnabled
    ) {
        float height = entity.getBbHeight();
        Vec3 fallback = entityPos.add(0, height + yOffset, 0);

        if (!smartEnabled) {
            return fallback;
        }

        int entityId = entity.getId();
        Vec3 targetPosition = calculateOptimalPosition(entity, entityPos, camera, yOffset);

        Vec3 currentPosition = currentPositions.get(entityId);
        if (currentPosition == null) {
            currentPosition = targetPosition;
        }

        Vec3 newPosition = lerpVec3(currentPosition, targetPosition, POSITION_LERP_SPEED);
        currentPositions.put(entityId, newPosition);

        return newPosition;
    }

    private static Vec3 calculateOptimalPosition(
            LivingEntity entity,
            Vec3 entityPos,
            Camera camera,
            double yOffset
    ) {
        Vec3 cameraPos = camera.getPosition();
        float height = entity.getBbHeight();
        float halfWidth = entity.getBbWidth() / 2.0f;

        Vec3 entityCenter = entityPos.add(0, height * 0.5, 0);
        double entityTopY = entityPos.y + height + yOffset;

        // setup
        Vec3 lookDir = new Vec3(camera.getLookVector());
        Vec3 upDir = new Vec3(camera.getUpVector());
        Vec3 rightDir = lookDir.cross(upDir).normalize();

        Vec3 toEntity = entityCenter.subtract(cameraPos);
        double dist = toEntity.length();

        if (dist < 0.1) {
            return entityPos.add(0, height + yOffset, 0);
        }


        Vec3 toCamera = toEntity.normalize().scale(-1);

        Vec3 crosshairPoint = cameraPos.add(lookDir.scale(dist));
        Vec3 entityToCrosshair = crosshairPoint.subtract(entityCenter);
        double crosshairDist = entityToCrosshair.length();

        // viewing situation

        double cameraPitch = Math.toRadians(camera.getXRot());
        double topAboveCamera = entityTopY - cameraPos.y;

        // dead zone
        double deadZone = halfWidth + 1.2;
        boolean crosshairOnEntity = crosshairDist < deadZone;

        // if looking at a entity
        boolean lookingDownAtEntity = cameraPos.y > entityCenter.y && cameraPitch > 0.25;


        double topPositionBadness = 0;

        // entity top is above camera
        if (topAboveCamera > 0.4) {
            topPositionBadness = Math.min(1.0, (topAboveCamera - 0.4) / 2.0);
        }

        // tall entities need side positioning
        if (height > 1.8) {
            topPositionBadness = Math.max(topPositionBadness, Math.min(1.0, (height - 1.8) / 1.8));
        }

        // looking down pushes top off-screen
        if (cameraPitch > 0.2) {
            double downBadness = Math.min(1.0, (cameraPitch - 0.2) / 0.6);
            topPositionBadness = Math.max(topPositionBadness, downBadness * 0.6);
        }




        double offsetDist = HUD_OFFSET_DISTANCE + halfWidth + 0.25;

        double baseX, baseZ, baseY;

        // crosshair is on/near entity or looking down at entity
        if (crosshairOnEntity || (lookingDownAtEntity && cameraPitch > 0.4)) {
            double sideDir = 1.0;
            double dotRight = entityToCrosshair.x * rightDir.x + entityToCrosshair.z * rightDir.z;
            if (Math.abs(dotRight) > 0.3) {
                sideDir = Math.signum(dotRight);
            }

            double sideStrength = 0.7;
            if (lookingDownAtEntity && cameraPitch > 0.4) {
                sideStrength = 0.75 + 0.15 * Math.min(1.0, (cameraPitch - 0.4) / 0.4);
            }

            baseX = rightDir.x * sideDir * offsetDist * sideStrength;
            baseZ = rightDir.z * sideDir * offsetDist * sideStrength;

            double blend = easeOutCubic(topPositionBadness);
            double topY = height + yOffset;
            double sideY = height * 0.5;
            baseY = topY + (sideY - topY) * Math.max(blend, lookingDownAtEntity ? 0.4 : 0);
        }
        // entity is significantly off to the side of crosshair
        else {
            Vec3 towardsCrosshair = entityToCrosshair.normalize();

            double horizShiftX = towardsCrosshair.x;
            double horizShiftZ = towardsCrosshair.z;
            double horizLen = Math.sqrt(horizShiftX * horizShiftX + horizShiftZ * horizShiftZ);

            if (horizLen > 0.01) {
                horizShiftX /= horizLen;
                horizShiftZ /= horizLen;
            } else {
                horizShiftX = rightDir.x;
                horizShiftZ = rightDir.z;
            }

            double effectiveOffset = Math.max(0, crosshairDist - deadZone);
            double horizStrength = Math.min(1.0, effectiveOffset / 3.5);
            horizStrength = easeOutQuad(horizStrength) * 0.6;

            baseX = horizShiftX * offsetDist * (0.5 + 0.3 * horizStrength);
            baseZ = horizShiftZ * offsetDist * (0.5 + 0.3 * horizStrength);

            double blend = easeOutCubic(topPositionBadness);
            double topY = height + yOffset;
            double sideY = height * 0.55;
            baseY = topY + (sideY - topY) * blend;
        }

        // bias

        double camerawardBias = 0.15;
        baseX += toCamera.x * camerawardBias;
        baseZ += toCamera.z * camerawardBias;

        if (dist < 2.0) {
            double closeFactor = Math.min(1.0, (2.0 - dist) / 1.5);
            closeFactor = easeOutQuad(closeFactor);
            baseX *= (1.0 + 0.25 * closeFactor);
            baseZ *= (1.0 + 0.25 * closeFactor);
        }

        // min
        double currentHorizOffset = Math.sqrt(baseX * baseX + baseZ * baseZ);
        double minOffset = halfWidth + 0.4;
        if (currentHorizOffset < minOffset && currentHorizOffset > 0.01) {
            double scale = minOffset / currentHorizOffset;
            baseX *= scale;
            baseZ *= scale;
        }

        return entityPos.add(baseX, baseY, baseZ);
    }

    private static double easeOutQuad(double t) {
        return 1 - (1 - t) * (1 - t);
    }

    private static double easeOutCubic(double t) {
        return 1 - Math.pow(1 - t, 3);
    }

    private static Vec3 lerpVec3(Vec3 from, Vec3 to, float t) {
        return new Vec3(
                from.x + (to.x - from.x) * t,
                from.y + (to.y - from.y) * t,
                from.z + (to.z - from.z) * t
        );
    }

    /* public static void cleanupEntity(int entityId) {
        currentPositions.remove(entityId);
    }
     */

    public static void clear() {
        currentPositions.clear();
    }
}
