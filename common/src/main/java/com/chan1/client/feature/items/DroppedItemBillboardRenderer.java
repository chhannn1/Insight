package com.chan1.client.feature.items;

import com.chan1.client.config.InsightConfig;
import com.chan1.client.util.render.AnimationHelper;
import com.chan1.client.util.render.TooltipBackgroundRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.GlConst;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class DroppedItemBillboardRenderer {

    //TODO: organize and clean up this mess lol

    private static final int LIGHT_FULL_BRIGHT = 15728880;
    private static final float ICON_SIZE = 16.0f;
    private static final float ICON_PADDING = 2.0f;
    private static final int COUNT_COLOR = 0xBBBBBB;
    private static final float MAX_SCALE_MULTIPLIER = 1.0f;
    private static final float SCALE_START_DISTANCE = 3.0f;

    private static final AnimationHelper animationHelper = new AnimationHelper();

    private DroppedItemBillboardRenderer() {
    }

    private record ItemWithDistance(ItemEntity entity, double distance, Vec3 position) {}


    public static void renderBillboards(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            Camera camera,
            float partialTick
    ) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null || minecraft.level == null || minecraft.screen != null) {
            return;
        }

        List<ItemEntity> items;
        if (InsightConfig.getDetectionMode() == InsightConfig.DetectionMode.CROSSHAIR) {
            ItemEntity crosshairItem = DroppedItemDetector.getCrosshairItem();
            items = crosshairItem != null ? List.of(crosshairItem) : List.of();
        } else {
            items = DroppedItemDetector.getNearbyItems();
        }

        Vec3 cameraPos = camera.getPosition();
        Vec3 cameraLookVec = new Vec3(camera.getLookVector());

        List<ItemWithDistance> sortedItems = new ArrayList<>();

        for (ItemEntity itemEntity : items) {
            Vec3 itemPos = getInterpolatedPosition(itemEntity, partialTick);
            Vec3 toItem = itemPos.subtract(cameraPos);
            double distance = toItem.length();

            if (toItem.normalize().dot(cameraLookVec) < -0.2) {
                continue;
            }

            sortedItems.add(new ItemWithDistance(itemEntity, distance, itemPos));
        }


        Set<Integer> currentEntityIds = new HashSet<>();
        for (ItemWithDistance item : sortedItems) {
            currentEntityIds.add(item.entity().getId());
        }

        // almost forgot to clean up stale states :p
        animationHelper.cleanupStaleAnimations(currentEntityIds);

        if (sortedItems.isEmpty()) {
            return;
        }

        sortedItems.sort(Comparator.comparingDouble(ItemWithDistance::distance).reversed());

        if (bufferSource instanceof MultiBufferSource.BufferSource source) {
            source.endBatch();
        }

        if (InsightConfig.isAnimationsEnabled()) {
            for (ItemWithDistance item : sortedItems) {
                animationHelper.getOrCreateAppearAnimation(item.entity().getId());
            }
        }

        for (ItemWithDistance item : sortedItems) {
            float animScale = 1.0f;
            if (InsightConfig.isAnimationsEnabled()) {
                AnimationHelper.AnimationState state = animationHelper.getAnimationState(item.entity().getId());
                if (state != null) {
                    animScale = AnimationHelper.calculateScale(state, InsightConfig.getAnimationDurationMs());
                }
            }

            if (animScale > 0.001f) {
                renderItemBillboard(poseStack, bufferSource, item.entity(), item.position(),
                        item.distance(), cameraPos, camera, minecraft.font, animScale);
            }
        }
    }

    private static void renderItemBillboard(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            ItemEntity itemEntity,
            Vec3 itemPos,
            double distance,
            Vec3 cameraPos,
            Camera camera,
            Font font,
            float animScale
    ) {
        Minecraft minecraft = Minecraft.getInstance();

        if (distance > InsightConfig.getMaxRenderDistance()) {
            return;
        }

        float alpha = 1.0f;

        ItemStack itemStack = itemEntity.getItem();
        int stackCount = itemStack.getCount();

        Component titleLine = buildTitleWithCount(itemStack, stackCount);

        List<Component> originalTooltip = itemStack.getTooltipLines(minecraft.player,
                minecraft.options.advancedItemTooltips ? net.minecraft.world.item.TooltipFlag.Default.ADVANCED : net.minecraft.world.item.TooltipFlag.Default.NORMAL);

        if (originalTooltip.isEmpty()) {
            return;
        }

        Vec3 billboardPos = itemPos.add(0, InsightConfig.getTooltipYOffset(), 0);

        float iconTotalWidth = ICON_SIZE + ICON_PADDING * 2;

        int maxTextWidth = font.width(titleLine);
        for (int i = 1; i < originalTooltip.size(); i++) {
            int width = font.width(originalTooltip.get(i));
            if (width > maxTextWidth) {
                maxTextWidth = width;
            }
        }

        int textAreaWidth = maxTextWidth + 6;
        int tooltipWidth = (int) iconTotalWidth + textAreaWidth;
        int tooltipHeight = 4 + originalTooltip.size() * 10;
        if (originalTooltip.size() > 1) {
            tooltipHeight += 1;
        }

        tooltipHeight = Math.max(tooltipHeight, (int) (ICON_SIZE + ICON_PADDING));

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
        RenderSystem.depthFunc(com.mojang.blaze3d.platform.GlConst.GL_ALWAYS);
        RenderSystem.depthMask(false);

        // tooltip background
        TooltipBackgroundRenderer.renderNoStateChange(matrix, left, top, right, bottom, alpha);

        // item icon
        renderItemIconNoStateChange(poseStack, bufferSource, itemStack, -halfWidth + ICON_PADDING, -ICON_SIZE / 2, alpha);

        int textAlpha = (int) (255 * alpha);
        float textStartX = -halfWidth + iconTotalWidth + 2;
        float y = -halfHeight + 2;

        MultiBufferSource.BufferSource immediateSource = minecraft.renderBuffers().bufferSource();

        font.drawInBatch(
                titleLine,
                textStartX,
                y,
                (textAlpha << 24) | 0x00FFFFFF,
                false,
                matrix,
                immediateSource,
                Font.DisplayMode.SEE_THROUGH,
                0,
                LIGHT_FULL_BRIGHT
        );

        y += 10;
        if (originalTooltip.size() > 1) {
            y += 1;
        }

        for (int i = 1; i < originalTooltip.size(); i++) {
            Component line = originalTooltip.get(i);

            font.drawInBatch(
                    line,
                    textStartX,
                    y,
                    (textAlpha << 24) | 0x00AAAAAA,
                    false,
                    matrix,
                    immediateSource,
                    Font.DisplayMode.SEE_THROUGH,
                    0,
                    LIGHT_FULL_BRIGHT
            );

            y += 10;
        }

        immediateSource.endBatch();


        RenderSystem.depthMask(true);
        RenderSystem.depthFunc(com.mojang.blaze3d.platform.GlConst.GL_LEQUAL);
        RenderSystem.disableBlend();

        poseStack.popPose();
    }

    private static Component buildTitleWithCount(ItemStack itemStack, int count) {
        MutableComponent name = itemStack.getHoverName().copy();

        if (count > 1) {
            MutableComponent countText = Component.literal(" x" + count)
                    .withStyle(Style.EMPTY.withColor(COUNT_COLOR));
            return name.append(countText);
        }

        return name;
    }

    private static void renderItemIcon(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            ItemStack itemStack,
            float x, float y,
            float alpha
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        ItemRenderer itemRenderer = minecraft.getItemRenderer();

        poseStack.pushPose();

        poseStack.translate(x + ICON_SIZE / 2, y + ICON_SIZE / 2, -0.01f);

        float iconScale = 14.0f;
        poseStack.scale(iconScale, -iconScale, 0.001f);

       // forgor to do this
        poseStack.last().normal().identity();

        RenderSystem.depthMask(false);
        RenderSystem.disableDepthTest();

        MultiBufferSource.BufferSource immediateSource = minecraft.renderBuffers().bufferSource();

        itemRenderer.renderStatic(
                itemStack,
                ItemDisplayContext.GUI,
                LIGHT_FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                immediateSource,
                minecraft.level,
                0
        );

        immediateSource.endBatch();

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);

        poseStack.popPose();
    }


    private static void renderItemIconNoStateChange(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            ItemStack itemStack,
            float x, float y,
            float alpha
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        ItemRenderer itemRenderer = minecraft.getItemRenderer();

        poseStack.pushPose();

        poseStack.translate(x + ICON_SIZE / 2, y + ICON_SIZE / 2, -0.01f);

        float iconScale = 14.0f;
        poseStack.scale(iconScale, -iconScale, 0.001f);

        // forgor part 2
        poseStack.last().normal().identity();

        MultiBufferSource.BufferSource immediateSource = minecraft.renderBuffers().bufferSource();

        itemRenderer.renderStatic(
                itemStack,
                ItemDisplayContext.GUI,
                LIGHT_FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                immediateSource,
                minecraft.level,
                0
        );

        RenderSystem.depthMask(true);
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
        RenderSystem.depthMask(false);
        immediateSource.endBatch();

        poseStack.popPose();
    }

    private static Vec3 getInterpolatedPosition(ItemEntity entity, float partialTick) {
        double x = entity.xo + (entity.getX() - entity.xo) * partialTick;
        double y = entity.yo + (entity.getY() - entity.yo) * partialTick;
        double z = entity.zo + (entity.getZ() - entity.zo) * partialTick;
        return new Vec3(x, y, z);
    }

    private static float calculateDistanceScale(double distance) {
        if (distance <= SCALE_START_DISTANCE) {
            return MAX_SCALE_MULTIPLIER;
        }

        float maxDist = (float) InsightConfig.getMaxRenderDistance();
        float t = (float) (distance - SCALE_START_DISTANCE) / (maxDist - SCALE_START_DISTANCE);
        t = Math.min(1.0f, Math.max(0.0f, t));

        return MAX_SCALE_MULTIPLIER + t * (1.5f - MAX_SCALE_MULTIPLIER);
    }

    public static void clearAnimations() {
        animationHelper.clear();
    }
}
