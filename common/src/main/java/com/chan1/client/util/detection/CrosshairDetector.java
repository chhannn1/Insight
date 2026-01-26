package com.chan1.client.util.detection;

import com.chan1.client.config.InsightConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;


public final class CrosshairDetector {


    private static long lastCacheFrame = -1;
    private static HitResult cachedVanillaHit = null;
    private static BlockHitResult cachedBlockTarget = null;
    private static boolean blockSearchDone = false;


    private static Vec3 cachedEyePos = null;
    private static Vec3 cachedLookVec = null;
    private static double cachedRange = 0;

    private CrosshairDetector() {
    }




    @SuppressWarnings("unchecked")
    public static <T extends Entity> T getTargetedEntity(Class<T> entityClass) {
        refreshCacheIfNeeded();

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return null;
        }

        // Check vanilla hit result first
        if (cachedVanillaHit != null && cachedVanillaHit.getType() == HitResult.Type.ENTITY) {
            Entity entity = ((EntityHitResult) cachedVanillaHit).getEntity();
            if (entityClass.isInstance(entity)) {
                return (T) entity;
            }
        }

        // Manual fallback with type filter
        return findEntityManual(mc, entityClass, null);
    }


    @SuppressWarnings("unchecked")
    public static <T extends Entity> T getTargetedEntity(Class<T> entityClass, Predicate<T> validator) {
        refreshCacheIfNeeded();

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return null;
        }

        // vanila hit first
        if (cachedVanillaHit != null && cachedVanillaHit.getType() == HitResult.Type.ENTITY) {
            Entity entity = ((EntityHitResult) cachedVanillaHit).getEntity();
            if (entityClass.isInstance(entity)) {
                @SuppressWarnings("unchecked")
                T typed = (T) entity;
                if (validator.test(typed)) {
                    return typed;
                }
            }
        }

        return findEntityManual(mc, entityClass, validator);
    }


    public static BlockHitResult getTargetedBlockHit() {
        refreshCacheIfNeeded();

        if (blockSearchDone) {
            return cachedBlockTarget;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            blockSearchDone = true;
            return null;
        }

        // again, vanila hits first
        if (cachedVanillaHit != null && cachedVanillaHit.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) cachedVanillaHit;
            double distance = cachedEyePos.distanceTo(Vec3.atCenterOf(blockHit.getBlockPos()));
            if (distance <= cachedRange) {
                cachedBlockTarget = blockHit;
                blockSearchDone = true;
                return cachedBlockTarget;
            }
        }

        // manual
        cachedBlockTarget = findBlockManual(mc);
        blockSearchDone = true;
        return cachedBlockTarget;
    }


    public static <T extends Block> BlockTarget<T> getTargetedBlockInfo(Class<T> blockClass) {
        BlockHitResult hit = getTargetedBlockHit();
        if (hit == null || hit.getType() != HitResult.Type.BLOCK) {
            return null;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return null;
        }

        BlockPos pos = hit.getBlockPos();
        BlockState state = mc.level.getBlockState(pos);
        if (blockClass.isInstance(state.getBlock())) {
            @SuppressWarnings("unchecked")
            T block = (T) state.getBlock();
            return new BlockTarget<>(pos, state, block, hit);
        }

        return null;
    }




    public static <T extends Entity> List<T> getNearbyEntities(
            Class<T> entityClass,
            double range,
            Predicate<T> validator) {

        refreshCacheIfNeeded();

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return List.of();
        }

        Player player = mc.player;
        Vec3 playerPos = player.position();
        Vec3 eyePos = cachedEyePos;
        Vec3 lookVec = cachedLookVec;
        double rangeSq = range * range;

        AABB searchBox = new AABB(
                playerPos.x - range, playerPos.y - range, playerPos.z - range,
                playerPos.x + range, playerPos.y + range, playerPos.z + range
        );

        List<T> candidates = mc.level.getEntitiesOfClass(
                entityClass,
                searchBox,
                validator != null ? validator : e -> true
        );

        // filtered by range & visibility
        List<T> filtered = new ArrayList<>();
        for (T entity : candidates) {
            Vec3 entityPos = entity.position();
            double distSq = entityPos.distanceToSqr(playerPos);

            if (distSq > rangeSq) {
                continue;
            }

            Vec3 toEntity = entityPos.subtract(eyePos).normalize();
            double viewDot = toEntity.dot(lookVec);
            if (viewDot < -0.3) {
                continue;
            }

            filtered.add(entity);
        }

        // visibility first, then by distance
        filtered.sort((a, b) -> {
            Vec3 toA = a.position().subtract(eyePos).normalize();
            Vec3 toB = b.position().subtract(eyePos).normalize();
            double viewScoreA = toA.dot(lookVec);
            double viewScoreB = toB.dot(lookVec);

            boolean aInFront = viewScoreA > 0.7;
            boolean bInFront = viewScoreB > 0.7;

            if (aInFront && !bInFront) return -1;
            if (bInFront && !aInFront) return 1;

            double distA = a.distanceToSqr(playerPos);
            double distB = b.distanceToSqr(playerPos);
            return Double.compare(distA, distB);
        });

        return filtered;
    }


    @SuppressWarnings("unchecked")
    private static <T extends Entity> T findEntityManual(
            Minecraft mc,
            Class<T> typeFilter,
            Predicate<T> validator) {

        Player player = mc.player;
        Vec3 eyePos = cachedEyePos;
        Vec3 lookVec = cachedLookVec;
        double maxRange = cachedRange;

        Vec3 endPos = eyePos.add(lookVec.scale(maxRange));
        AABB searchBox = new AABB(eyePos, endPos).inflate(2.0);

        double closestDistSq = Double.MAX_VALUE;
        T closest = null;

        Class<? extends Entity> searchClass = typeFilter != null ? typeFilter : Entity.class;

        for (Entity entity : mc.level.getEntitiesOfClass(searchClass, searchBox)) {
            if (entity == player) continue;

            @SuppressWarnings("unchecked")
            T typedEntity = (T) entity;
            if (validator != null && !validator.test(typedEntity)) continue;

            // BBI first
            AABB entityBox = entity.getBoundingBox();
            Optional<Vec3> intersection = entityBox.clip(eyePos, endPos);

            if (intersection.isPresent()) {
                double distSq = eyePos.distanceToSqr(intersection.get());
                if (distSq < closestDistSq) {
                    closestDistSq = distSq;
                    closest = typedEntity;
                }
            } else {
                // falls back to distance from ray to entity center
                Vec3 entityCenter = entity.position().add(0, entity.getBbHeight() / 2, 0);
                Vec3 toEntity = entityCenter.subtract(eyePos);

                double projection = toEntity.dot(lookVec);
                if (projection <= 0 || projection > maxRange) continue;

                Vec3 pointOnRay = eyePos.add(lookVec.scale(projection));
                double deviationSq = entityCenter.distanceToSqr(pointOnRay);

                // tolerance scales with entity size (thanks random forge forum post for idea)
                float entityRadius = Math.max(entity.getBbWidth() / 2.0f, entity.getBbHeight() / 2.0f);
                double tolerance = entityRadius + 0.3;
                double maxDeviationSq = tolerance * tolerance;

                if (deviationSq > maxDeviationSq) continue;

                double distSq = toEntity.lengthSqr();
                if (distSq < closestDistSq) {
                    closestDistSq = distSq;
                    closest = typedEntity;
                }
            }
        }

        return closest;
    }

    private static BlockHitResult findBlockManual(Minecraft mc) {
        Vec3 eyePos = cachedEyePos;
        Vec3 lookVec = cachedLookVec;
        double maxRange = cachedRange;

        Vec3 endPos = eyePos.add(lookVec.scale(maxRange));

        ClipContext clipContext = new ClipContext(
                eyePos,
                endPos,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                mc.player
        );

        BlockHitResult result = mc.level.clip(clipContext);

        if (result.getType() == HitResult.Type.BLOCK) {
            return result;
        }

        return null;
    }

    // cache stuff
    private static void refreshCacheIfNeeded() {
        Minecraft mc = Minecraft.getInstance();

        long currentFrame = mc.level != null ? mc.level.getGameTime() : 0;

        if (currentFrame != lastCacheFrame) {
            lastCacheFrame = currentFrame;
            cachedVanillaHit = mc.hitResult;
            cachedBlockTarget = null;
            blockSearchDone = false;

            if (mc.player != null) {
                cachedEyePos = mc.player.getEyePosition();
                cachedLookVec = mc.player.getLookAngle();
                cachedRange = InsightConfig.getDetectionRange();
            } else {
                cachedEyePos = null;
                cachedLookVec = null;
                cachedRange = 0;
            }
        }
    }

    public record BlockTarget<T extends Block>(
            BlockPos pos,
            BlockState state,
            T block,
            BlockHitResult hitResult
    ) {}
}
