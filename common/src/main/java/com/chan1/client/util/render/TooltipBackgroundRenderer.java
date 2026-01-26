package com.chan1.client.util.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;


public final class TooltipBackgroundRenderer {


    public static final int BACKGROUND_COLOR = 0xF0100010;
    public static final int BORDER_START_COLOR = 0xFF4000CC;
    public static final int BORDER_END_COLOR = 0xFF200060;

    private TooltipBackgroundRenderer() {
    }


    public static void render(Matrix4f matrix, float left, float top, float right, float bottom, float alpha) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        RenderSystem.disableDepthTest();

        renderNoStateChange(matrix, left, top, right, bottom, alpha);

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }


    public static void renderNoStateChange(Matrix4f matrix, float left, float top, float right, float bottom, float alpha) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        int bgAlpha = (int) (((BACKGROUND_COLOR >> 24) & 0xFF) * alpha);
        int borderStartAlpha = (int) (((BORDER_START_COLOR >> 24) & 0xFF) * alpha);
        int borderEndAlpha = (int) (((BORDER_END_COLOR >> 24) & 0xFF) * alpha);
        float blackA = alpha;

        float bgR = ((BACKGROUND_COLOR >> 16) & 0xFF) / 255.0f;
        float bgB = (BACKGROUND_COLOR & 0xFF) / 255.0f;
        float bgA = bgAlpha / 255.0f;

        float bsR = ((BORDER_START_COLOR >> 16) & 0xFF) / 255.0f;
        float bsG = ((BORDER_START_COLOR >> 8) & 0xFF) / 255.0f;
        float bsB = (BORDER_START_COLOR & 0xFF) / 255.0f;
        float bsA = borderStartAlpha / 255.0f;

        float beR = ((BORDER_END_COLOR >> 16) & 0xFF) / 255.0f;
        float beG = ((BORDER_END_COLOR >> 8) & 0xFF) / 255.0f;
        float beB = (BORDER_END_COLOR & 0xFF) / 255.0f;
        float beA = borderEndAlpha / 255.0f;

        float z = 0.0f;

        addQuad(buffer, matrix, left - 1, top - 2, right + 1, top - 1, z, 0, 0, 0, blackA);
        addQuad(buffer, matrix, left - 1, bottom + 1, right + 1, bottom + 2, z, 0, 0, 0, blackA);
        addQuad(buffer, matrix, left - 2, top - 1, left - 1, bottom + 1, z, 0, 0, 0, blackA);
        addQuad(buffer, matrix, right + 1, top - 1, right + 2, bottom + 1, z, 0, 0, 0, blackA);

        addQuad(buffer, matrix, left - 1, top - 1, right + 1, top, z, bsR, bsG, bsB, bsA);
        addQuad(buffer, matrix, left - 1, bottom, right + 1, bottom + 1, z, beR, beG, beB, beA);
        addGradientQuad(buffer, matrix, left - 1, top, left, bottom, z, bsR, bsG, bsB, bsA, beR, beG, beB, beA);
        addGradientQuad(buffer, matrix, right, top, right + 1, bottom, z, bsR, bsG, bsB, bsA, beR, beG, beB, beA);

        addQuad(buffer, matrix, left, top, right, bottom, z, bgR, bgG, bgB, bgA);

        tesselator.end();
    }


    public static void addQuad(BufferBuilder buffer, Matrix4f matrix,
                               float x1, float y1, float x2, float y2, float z,
                               float r, float g, float b, float a) {
        buffer.vertex(matrix, x1, y1, z).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x1, y2, z).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x2, y2, z).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x2, y1, z).color(r, g, b, a).endVertex();
    }


    public static void addGradientQuad(BufferBuilder buffer, Matrix4f matrix,
                                       float x1, float y1, float x2, float y2, float z,
                                       float r1, float g1, float b1, float a1,
                                       float r2, float g2, float b2, float a2) {
        buffer.vertex(matrix, x1, y1, z).color(r1, g1, b1, a1).endVertex();
        buffer.vertex(matrix, x1, y2, z).color(r2, g2, b2, a2).endVertex();
        buffer.vertex(matrix, x2, y2, z).color(r2, g2, b2, a2).endVertex();
        buffer.vertex(matrix, x2, y1, z).color(r1, g1, b1, a1).endVertex();
    }
}
