package com.chan1.client.feature.misc;

import com.chan1.client.config.InsightConfig;
import com.chan1.client.util.render.TooltipBackgroundRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

//LEGACY CLASS, HAS SOME DUMB STUFF
public final class CropGrowthRenderer {

    private static final int LIGHT_FULL_BRIGHT = 15728880;
    private static final float MIN_READABLE_SCALE = 0.65f;
    private static final float OPTIMAL_SCALE = 0.85f;

    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int FULLY_GROWN_COLOR = 0xFF55FF55;


    public static void renderBillboards(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            Camera camera,
            float partialTick
    ) {
        if (!InsightConfig.isCropGrowthEnabled()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null || minecraft.level == null || minecraft.screen != null) {
            return;
        }

        CropGrowthDetector.CropInfo cropInfo = CropGrowthDetector.getTargetedCropInfo();
        if (cropInfo == null) {
            return;
        }

        Vec3 billboardPos = CropGrowthDetector.getCropBillboardPosition(cropInfo.pos());
        Vec3 cameraPos = camera.getPosition();
        Vec3 toCrop = billboardPos.subtract(cameraPos);
        double distance = toCrop.length();

        Vec3 cameraLookVec = new Vec3(camera.getLookVector());
        if (toCrop.normalize().dot(cameraLookVec) < -0.2) {
            return;
        }

        if (distance > InsightConfig.getMaxRenderDistance()) {
            return;
        }

        if (bufferSource instanceof MultiBufferSource.BufferSource source) {
            source.endBatch();
        }

        renderCropTooltip(poseStack, billboardPos, distance, cameraPos, camera, minecraft.font, cropInfo);
    }


    private static void renderCropTooltip(
            PoseStack poseStack,
            Vec3 billboardPos,
            double distance,
            Vec3 cameraPos,
            Camera camera,
            Font font,
            CropGrowthDetector.CropInfo cropInfo
    ) {
        Minecraft minecraft = Minecraft.getInstance();

        String statusText;
        int textColor;
        if (cropInfo.isFullyGrown()) {
            statusText = cropInfo.cropName() + " - Ready!";
            textColor = FULLY_GROWN_COLOR;
        } else {
            int percent = (int) cropInfo.getGrowthPercent();
            statusText = cropInfo.cropName() + " - " + percent + "%";
            textColor = TEXT_COLOR;
        }

        int textWidth = font.width(statusText);
        float progressBarWidth = Math.max(textWidth, 40);
        float progressBarHeight = 4;

        float tooltipWidth = Math.max(textWidth, progressBarWidth) + 12;
        float tooltipHeight = font.lineHeight + progressBarHeight + 10;

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
        float textY = -halfHeight + 4;

        font.drawInBatch(
                statusText,
                textX,
                textY,
                textColor,
                false,
                matrix,
                immediateSource,
                Font.DisplayMode.SEE_THROUGH,
                0,
                LIGHT_FULL_BRIGHT
        );

        immediateSource.endBatch();

        if (!cropInfo.isFullyGrown()) {
            float barX = -progressBarWidth / 2.0f;
            float barY = textY + font.lineHeight + 3;
            float progress = cropInfo.getGrowthPercent() / 100f;
            renderProgressBar(matrix, barX, barY, progressBarWidth, progressBarHeight, progress);
        }

        // Restore GL state
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();

        poseStack.popPose();
    }


    private static void renderProgressBar(Matrix4f matrix, float x, float y, float width, float height, float progress) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();

        // background
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        float bgR = 0.2f, bgG = 0.2f, bgB = 0.2f, bgA = 1.0f;
        builder.vertex(matrix, x, y + height, 0).color(bgR, bgG, bgB, bgA).endVertex();
        builder.vertex(matrix, x + width, y + height, 0).color(bgR, bgG, bgB, bgA).endVertex();
        builder.vertex(matrix, x + width, y, 0).color(bgR, bgG, bgB, bgA).endVertex();
        builder.vertex(matrix, x, y, 0).color(bgR, bgG, bgB, bgA).endVertex();
        BufferUploader.drawWithShader(builder.end());

        // progress fill
        if (progress > 0) {
            float fillWidth = width * Math.min(1.0f, progress);
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            float fillR = 0.27f, fillG = 0.67f, fillB = 0.27f, fillA = 1.0f;
            builder.vertex(matrix, x, y + height, 0).color(fillR, fillG, fillB, fillA).endVertex();
            builder.vertex(matrix, x + fillWidth, y + height, 0).color(fillR, fillG, fillB, fillA).endVertex();
            builder.vertex(matrix, x + fillWidth, y, 0).color(fillR, fillG, fillB, fillA).endVertex();
            builder.vertex(matrix, x, y, 0).color(fillR, fillG, fillB, fillA).endVertex();
            BufferUploader.drawWithShader(builder.end());
        }
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
