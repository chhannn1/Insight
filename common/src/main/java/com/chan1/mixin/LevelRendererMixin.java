package com.chan1.mixin;

import com.chan1.client.config.InsightConfig;
import com.chan1.client.feature.items.DroppedItemBillboardRenderer;
import com.chan1.client.feature.misc.BedTimerRenderer;
import com.chan1.client.feature.misc.CropGrowthRenderer;
import com.chan1.client.feature.targethud.TargetHudBillboardRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Shadow
    @Final
    private RenderBuffers renderBuffers;

    @Shadow
    @Final
    private Minecraft minecraft;


    @Inject(
            method = "renderLevel",
            at = @At("TAIL")
    )
    private void insight$renderBillboards(
            PoseStack poseStack,
            float partialTick,
            long finishNanoTime,
            boolean renderBlockOutline,
            Camera camera,
            GameRenderer gameRenderer,
            LightTexture lightTexture,
            Matrix4f projectionMatrix,
            CallbackInfo ci
    ) {
        MultiBufferSource.BufferSource bufferSource = this.renderBuffers.bufferSource();

        if (InsightConfig.isItemTooltipsEnabled()) {
            poseStack.pushPose();
            DroppedItemBillboardRenderer.renderBillboards(poseStack, bufferSource, camera, partialTick);
            bufferSource.endBatch();
            poseStack.popPose();
        }

        if (InsightConfig.isTargetHudEnabled()) {
            poseStack.pushPose();
            TargetHudBillboardRenderer.renderBillboards(poseStack, bufferSource, camera, partialTick);
            bufferSource.endBatch();
            poseStack.popPose();
        }

        if (InsightConfig.isBedTimerEnabled()) {
            poseStack.pushPose();
            BedTimerRenderer.renderBillboards(poseStack, bufferSource, camera, partialTick);
            bufferSource.endBatch();
            poseStack.popPose();
        }

        if (InsightConfig.isCropGrowthEnabled()) {
            poseStack.pushPose();
            CropGrowthRenderer.renderBillboards(poseStack, bufferSource, camera, partialTick);
            bufferSource.endBatch();
            poseStack.popPose();
        }
    }
}
