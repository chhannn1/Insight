package com.chan1.client.util.math;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public final class WorldToScreenProjector {

    private static Matrix4f cachedModelViewMatrix = new Matrix4f();
    private static Matrix4f cachedProjectionMatrix = new Matrix4f();
    private static boolean matricesValid = false;

    private WorldToScreenProjector() {
    }


    public static class ScreenPosition {
        public final int x;
        public final int y;
        public final double distance;

        public ScreenPosition(int x, int y, double distance) {
            this.x = x;
            this.y = y;
            this.distance = distance;
        }
    }


    public static void captureMatrices(Matrix4f modelView, Matrix4f projection) {
        cachedModelViewMatrix = new Matrix4f(modelView);
        cachedProjectionMatrix = new Matrix4f(projection);
        matricesValid = true;
    }

    // cache
    public static void invalidateMatrices() {
        matricesValid = false;
    }


    public static boolean areMatricesValid() {
        return matricesValid;
    }


    @Nullable
    public static ScreenPosition projectToScreen(Vec3 worldPos, int screenWidth, int screenHeight) {
        if (!matricesValid) {
            return null;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Vec3 cameraPos = minecraft.gameRenderer.getMainCamera().getPosition();

        Vector3f relativePos = new Vector3f(
                (float) (worldPos.x - cameraPos.x),
                (float) (worldPos.y - cameraPos.y),
                (float) (worldPos.z - cameraPos.z)
        );

        double distance = Math.sqrt(
                relativePos.x * relativePos.x +
                relativePos.y * relativePos.y +
                relativePos.z * relativePos.z
        );

        Vector4f clipCoords = new Vector4f(relativePos.x, relativePos.y, relativePos.z, 1.0f);

        clipCoords.mul(cachedModelViewMatrix);
        clipCoords.mul(cachedProjectionMatrix);

        if (clipCoords.w <= 0.0f) {
            return null;
        }

        float ndcX = clipCoords.x / clipCoords.w;
        float ndcY = clipCoords.y / clipCoords.w;
        float ndcZ = clipCoords.z / clipCoords.w;

        if (ndcX < -1.0f || ndcX > 1.0f || ndcY < -1.0f || ndcY > 1.0f || ndcZ < -1.0f || ndcZ > 1.0f) {
            return null;
        }

        int screenX = (int) ((ndcX + 1.0f) * 0.5f * screenWidth);
        int screenY = (int) ((1.0f - ndcY) * 0.5f * screenHeight);

        return new ScreenPosition(screenX, screenY, distance);
    }


    public static float calculateDistanceAlpha(double distance, double fadeStart, double maxDistance) {
        if (distance <= fadeStart) {
            return 1.0f;
        }
        if (distance >= maxDistance) {
            return 0.0f;
        }
        return (float) ((maxDistance - distance) / (maxDistance - fadeStart));
    }
}
