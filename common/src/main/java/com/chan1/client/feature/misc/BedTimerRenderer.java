package com.chan1.client.feature.misc;

import com.chan1.client.config.InsightConfig;
import com.chan1.client.util.MinecraftTimeHelper;
import com.chan1.client.util.render.TooltipBackgroundRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;


public final class BedTimerRenderer {

    private static final int LIGHT_FULL_BRIGHT = 15728880;
    private static final float MIN_READABLE_SCALE = 0.65f;
    private static final float OPTIMAL_SCALE = 0.85f;

    private static final int ZZZ_COLOR = 0xFF9999FF;


    public static void renderBillboards(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            Camera camera,
            float partialTick
    ) {
        if (!InsightConfig.isBedTimerEnabled()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null || minecraft.level == null || minecraft.screen != null) {
            return;
        }

        BlockPos bedPos = BedBlockDetector.getTargetedBedPos();
        if (bedPos == null) {
            return;
        }

        Vec3 billboardPos = BedBlockDetector.getBedBillboardPosition(bedPos);
        Vec3 cameraPos = camera.getPosition();
        Vec3 toBed = billboardPos.subtract(cameraPos);
        double distance = toBed.length();

        // Check if behind camera
        Vec3 cameraLookVec = new Vec3(camera.getLookVector());
        if (toBed.normalize().dot(cameraLookVec) < -0.2) {
            return;
        }

        if (distance > InsightConfig.getMaxRenderDistance()) {
            return;
        }

        if (bufferSource instanceof MultiBufferSource.BufferSource source) {
            source.endBatch();
        }

        boolean isNight = MinecraftTimeHelper.isNightTime(minecraft.level);

        if (isNight) {
            renderNightZzz(poseStack, billboardPos, distance, cameraPos, camera, minecraft.font);
        } else {
            String timeText = MinecraftTimeHelper.formatTimeUntilNight(minecraft.level);
            if (timeText != null) {
                renderDayTooltip(poseStack, billboardPos, distance, cameraPos, camera, minecraft.font, timeText);
            }
        }
    }

    private static void renderDayTooltip(
            PoseStack poseStack,
            Vec3 billboardPos,
            double distance,
            Vec3 cameraPos,
            Camera camera,
            Font font,
            String timeText
    ) {
        Minecraft minecraft = Minecraft.getInstance();

        int textWidth = font.width(timeText);
        int tooltipWidth = textWidth + 12;
        int tooltipHeight = 16;

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
        float scale = baseScale * distanceScale;
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
        TooltipBackgroundRenderer.renderNoStateChange(matrix, left, top, right, bottom, 1.0f);

        // text
        MultiBufferSource.BufferSource immediateSource = minecraft.renderBuffers().bufferSource();

        float textX = -textWidth / 2.0f;
        float textY = -font.lineHeight / 2.0f;

        font.drawInBatch(
                timeText,
                textX,
                textY,
                0xFFFFFFFF,
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


    private static void renderNightZzz(
            PoseStack poseStack,
            Vec3 billboardPos,
            double distance,
            Vec3 cameraPos,
            Camera camera,
            Font font
    ) {
        Minecraft minecraft = Minecraft.getInstance();

        // ugh
        long gameTime = minecraft.level != null ? minecraft.level.getGameTime() : 0;
        float time = (gameTime % 40) / 40.0f * (float) Math.PI * 2.0f;

        int zWidth = font.width("z");
        float charSpacing = zWidth + 2;
        float totalWidth = charSpacing * 3 - 2;

        poseStack.pushPose();

        float floatOffset = (float) Math.sin(time * 0.5f) * 3.0f;

        poseStack.translate(
                billboardPos.x - cameraPos.x,
                billboardPos.y - cameraPos.y,
                billboardPos.z - cameraPos.z
        );

        Quaternionf cameraRotation = camera.rotation();
        poseStack.mulPose(cameraRotation);

        float baseScale = InsightConfig.getBillboardScale();
        float distanceScale = calculateDistanceScale(distance);
        float scale = baseScale * distanceScale;

        float pulseScale = 1.0f + (float) Math.sin(time * 0.7f) * 0.05f;
        scale *= pulseScale;

        poseStack.scale(-scale, -scale, scale);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        MultiBufferSource.BufferSource immediateSource = minecraft.renderBuffers().bufferSource();

        // staggered timing
        float startX = -totalWidth / 2.0f;

        for (int i = 0; i < 3; i++) {
            float phaseOffset = i * 0.8f;
            float waveY = (float) Math.sin(time + phaseOffset) * 4.0f;

            float sizeMultiplier = 1.0f + i * 0.15f;

            int alpha = 255 - (i * 25);
            int color = (alpha << 24) | (ZZZ_COLOR & 0x00FFFFFF);

            poseStack.pushPose();

            float charX = startX + i * charSpacing;
            float charY = -font.lineHeight / 2.0f + waveY + floatOffset;

            if (sizeMultiplier != 1.0f) {
                poseStack.translate(charX + zWidth / 2.0f, charY + font.lineHeight / 2.0f, 0);
                poseStack.scale(sizeMultiplier, sizeMultiplier, 1.0f);
                poseStack.translate(-(charX + zWidth / 2.0f), -(charY + font.lineHeight / 2.0f), 0);
            }

            Matrix4f matrix = poseStack.last().pose();

            font.drawInBatch(
                    "z",
                    charX,
                    charY,
                    color,
                    false,
                    matrix,
                    immediateSource,
                    Font.DisplayMode.SEE_THROUGH,
                    0,
                    LIGHT_FULL_BRIGHT
            );

            poseStack.popPose();
        }

        immediateSource.endBatch();

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();

        poseStack.popPose();
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
}
