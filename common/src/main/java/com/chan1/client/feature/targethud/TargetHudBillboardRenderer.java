package com.chan1.client.feature.targethud;

import com.chan1.client.config.InsightConfig;
import com.chan1.client.util.render.AnimationHelper;
import com.chan1.client.util.render.TooltipBackgroundRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public final class TargetHudBillboardRenderer {

    private static final int LIGHT_FULL_BRIGHT = 15728880;
    private static final float MIN_READABLE_SCALE = 0.65f;
    private static final float OPTIMAL_SCALE = 0.85f;


    private static final int HEALTH_BAR_BG_COLOR = 0xFF1A1A1A;
    private static final int HEALTH_BAR_BORDER_COLOR = 0xFF000000;
    private static final int HEALTH_COLOR_FULL = 0xFF55FF55;
    private static final int HEALTH_COLOR_HALF = 0xFFFFFF55;
    private static final int HEALTH_COLOR_LOW = 0xFFFF5555;

    private static final AnimationHelper animationHelper = new AnimationHelper();
    private static final Map<Integer, Float> displayedHealthPercent = new HashMap<>();
    private static final float HEALTH_BAR_LERP_SPEED = 0.3f;

    private TargetHudBillboardRenderer() {
    }


    public static void renderBillboards(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            Camera camera,
            float partialTick
    ) {
        if (!InsightConfig.isTargetHudEnabled()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null || minecraft.level == null || minecraft.screen != null) {
            return;
        }

        LivingEntity target = TargetEntityDetector.getHoveredEntity();

        // for cleanup *
        Set<Integer> currentEntityIds = new HashSet<>();
        if (target != null) {
            currentEntityIds.add(target.getId());
        } else {
            // cleanup
            TargetHudPositioner.clear();
        }

        animationHelper.cleanupStaleAnimations(currentEntityIds);
        displayedHealthPercent.keySet().retainAll(currentEntityIds);

        if (target == null) {
            return;
        }

        Vec3 cameraPos = camera.getPosition();
        Vec3 entityPos = getInterpolatedPosition(target, partialTick);
        Vec3 toEntity = entityPos.subtract(cameraPos);
        double distance = toEntity.length();

        Vec3 cameraLookVec = new Vec3(camera.getLookVector());
        if (toEntity.normalize().dot(cameraLookVec) < -0.2) {
            return;
        }

        if (distance > InsightConfig.getMaxRenderDistance()) {
            return;
        }

        if (bufferSource instanceof MultiBufferSource.BufferSource source) {
            source.endBatch();
        }

        float animScale = 1.0f;
        if (InsightConfig.isAnimationsEnabled()) {
            animationHelper.getOrCreateAppearAnimation(target.getId());
            AnimationHelper.AnimationState state = animationHelper.getAnimationState(target.getId());
            if (state != null) {
                animScale = AnimationHelper.calculateScale(state, InsightConfig.getAnimationDurationMs());
            }
        }

        if (animScale > 0.001f) {
            renderTargetHud(poseStack, bufferSource, target, entityPos, distance, cameraPos, camera, minecraft.font, animScale);
        }
    }

    private static void renderTargetHud(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            LivingEntity entity,
            Vec3 entityPos,
            double distance,
            Vec3 cameraPos,
            Camera camera,
            Font font,
            float animScale
    ) {
        Minecraft minecraft = Minecraft.getInstance();

        float alpha = 1.0f;

        Component entityName = entity.getDisplayName();
        float health = entity.getHealth();
        float maxHealth = entity.getMaxHealth();
        float healthPercent = Math.min(1.0f, health / maxHealth);

        int entityId = entity.getId();
        float displayed = displayedHealthPercent.getOrDefault(entityId, healthPercent);
        displayed = displayed + (healthPercent - displayed) * HEALTH_BAR_LERP_SPEED;
        if (Math.abs(displayed - healthPercent) < 0.001f) {
            displayed = healthPercent;
        }
        displayedHealthPercent.put(entityId, displayed);

        String healthText = String.format("%.1f / %.1f", health, maxHealth);

        int nameWidth = font.width(entityName);
        int healthTextWidth = font.width(healthText);
        int healthBarWidth = Math.max(nameWidth, healthTextWidth) + 10;
        healthBarWidth = Math.max(healthBarWidth, 60);

        int tooltipWidth = healthBarWidth + 12;
        int healthBarHeight = 6;
        int tooltipHeight = 10 + 2 + healthBarHeight + 2 + 10 + 4; // name + gap + bar + gap + health text + padding

        Vec3 billboardPos = TargetHudPositioner.calculateBillboardPosition(
                entity,
                entityPos,
                camera,
                InsightConfig.getTooltipYOffset(),
                InsightConfig.isTargetHudSmartPositioning()
        );

        poseStack.pushPose();

        poseStack.translate(
                billboardPos.x - cameraPos.x,
                billboardPos.y - cameraPos.y,
                billboardPos.z - cameraPos.z
        );

        Quaternionf cameraRotation = camera.rotation();
        poseStack.mulPose(cameraRotation);

        float baseScale = InsightConfig.getBillboardScale();
        float distanceScale = calculateDistanceScale(distance);
        float scale = baseScale * distanceScale * animScale;
        poseStack.scale(-scale, -scale, scale);

        float halfWidth = tooltipWidth / 2.0f;
        float halfHeight = tooltipHeight / 2.0f;

        Matrix4f matrix = poseStack.last().pose();

        float left = -halfWidth - 3;
        float top = -halfHeight - 3;
        float right = halfWidth + 3;
        float bottom = halfHeight + 3;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        // tooltip background
        TooltipBackgroundRenderer.renderNoStateChange(matrix, left, top, right, bottom, alpha);

        // entity name
        MultiBufferSource.BufferSource immediateSource = minecraft.renderBuffers().bufferSource();
        int textAlpha = (int) (255 * alpha);

        float nameX = -nameWidth / 2.0f;
        float nameY = -halfHeight + 4;

        font.drawInBatch(
                entityName,
                nameX,
                nameY,
                (textAlpha << 24) | 0x00FFFFFF,
                false,
                matrix,
                immediateSource,
                Font.DisplayMode.SEE_THROUGH,
                0,
                LIGHT_FULL_BRIGHT
        );

        immediateSource.endBatch();

        //health bar
        float barLeft = -healthBarWidth / 2.0f;
        float barTop = nameY + 12;
        float barRight = healthBarWidth / 2.0f;
        float barBottom = barTop + healthBarHeight;

        renderHealthBar(matrix, barLeft, barTop, barRight, barBottom, displayed, alpha);

        immediateSource = minecraft.renderBuffers().bufferSource();
        float healthTextX = -healthTextWidth / 2.0f;
        float healthTextY = barBottom + 3;

        int healthColor = getHealthColor(healthPercent);

        font.drawInBatch(
                healthText,
                healthTextX,
                healthTextY,
                (textAlpha << 24) | (healthColor & 0x00FFFFFF),
                false,
                matrix,
                immediateSource,
                Font.DisplayMode.SEE_THROUGH,
                0,
                LIGHT_FULL_BRIGHT
        );

        immediateSource.endBatch();

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();

        poseStack.popPose();
    }

    private static void renderHealthBar(Matrix4f matrix, float left, float top, float right, float bottom, float healthPercent, float alpha) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        float z = 0.0f;

        addQuad(buffer, matrix, left - 1, top - 1, right + 1, top, z, HEALTH_BAR_BORDER_COLOR, alpha);
        addQuad(buffer, matrix, left - 1, bottom, right + 1, bottom + 1, z, HEALTH_BAR_BORDER_COLOR, alpha);
        addQuad(buffer, matrix, left - 1, top, left, bottom, z, HEALTH_BAR_BORDER_COLOR, alpha);
        addQuad(buffer, matrix, right, top, right + 1, bottom, z, HEALTH_BAR_BORDER_COLOR, alpha);


        addQuad(buffer, matrix, left, top, right, bottom, z, HEALTH_BAR_BG_COLOR, alpha);

        float fillWidth = (right - left) * healthPercent;
        int healthColor = getHealthColor(healthPercent);
        addQuad(buffer, matrix, left, top, left + fillWidth, bottom, z, healthColor, alpha);

        tesselator.end();
    }

    private static void addQuad(BufferBuilder buffer, Matrix4f matrix,
                                float x1, float y1, float x2, float y2, float z,
                                int color, float alpha) {
        float a = ((color >> 24) & 0xFF) / 255.0f * alpha;
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;

        buffer.vertex(matrix, x1, y1, z).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x1, y2, z).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x2, y2, z).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x2, y1, z).color(r, g, b, a).endVertex();
    }

    private static int getHealthColor(float healthPercent) {
        if (healthPercent > 0.5f) {
            float t = (healthPercent - 0.5f) * 2.0f;
            return interpolateColor(HEALTH_COLOR_HALF, HEALTH_COLOR_FULL, t);
        } else {
            float t = healthPercent * 2.0f;
            return interpolateColor(HEALTH_COLOR_LOW, HEALTH_COLOR_HALF, t);
        }
    }

    private static int interpolateColor(int color1, int color2, float t) {
        int a1 = (color1 >> 24) & 0xFF;
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int a2 = (color2 >> 24) & 0xFF;
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int a = (int) (a1 + (a2 - a1) * t);
        int r = (int) (r1 + (r2 - r1) * t);
        int g = (int) (g1 + (g2 - g1) * t);
        int b = (int) (b1 + (b2 - b1) * t);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static Vec3 getInterpolatedPosition(LivingEntity entity, float partialTick) {
        double x = entity.xo + (entity.getX() - entity.xo) * partialTick;
        double y = entity.yo + (entity.getY() - entity.yo) * partialTick;
        double z = entity.zo + (entity.getZ() - entity.zo) * partialTick;
        return new Vec3(x, y, z);
    }


    private static float calculateDistanceScale(double distance) {
        float maxDist = (float) InsightConfig.getMaxRenderDistance();

        if (distance <= 1.5) {
            float t = (float) (distance / 1.5);
            return MIN_READABLE_SCALE + t * (OPTIMAL_SCALE - MIN_READABLE_SCALE) * 0.5f;
        } else if (distance <= 4.0) {
            float t = (float) ((distance - 1.5) / 2.5);
            float startScale = MIN_READABLE_SCALE + (OPTIMAL_SCALE - MIN_READABLE_SCALE) * 0.5f;
            return startScale + t * (OPTIMAL_SCALE - startScale);
        } else {
            float t = (float) ((distance - 4.0) / (maxDist - 4.0));
            t = Math.min(1.0f, Math.max(0.0f, t));
            return OPTIMAL_SCALE + t * (1.1f - OPTIMAL_SCALE);
        }
    }


    public static void clearAnimations() {
        animationHelper.clear();
        displayedHealthPercent.clear();
        TargetHudPositioner.clear();
    }
}
