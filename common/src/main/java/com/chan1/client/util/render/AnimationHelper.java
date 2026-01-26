package com.chan1.client.util.render;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public final class AnimationHelper {


    public static class AnimationState {
        public final long startTime;
        public final boolean appearing;
        public final float startScale;
        public final float targetScale;

        public AnimationState(boolean appearing) {
            this.startTime = System.currentTimeMillis();
            this.appearing = appearing;
            this.startScale = appearing ? 0.0f : 1.0f;
            this.targetScale = appearing ? 1.0f : 0.0f;
        }
    }

    private final Map<Integer, AnimationState> animationStates = new HashMap<>();


    public AnimationState getOrCreateAppearAnimation(int entityId) {
        return animationStates.computeIfAbsent(entityId, id -> new AnimationState(true));
    }


    public AnimationState getAnimationState(int entityId) {
        return animationStates.get(entityId);
    }


    public void cleanupStaleAnimations(Set<Integer> visibleEntityIds) {
        if (!animationStates.isEmpty()) {
            animationStates.keySet().removeIf(entityId -> !visibleEntityIds.contains(entityId));
        }
    }


    public void clear() {
        animationStates.clear();
    }

    public boolean hasAnimations() {
        return !animationStates.isEmpty();
    }


    public static float calculateScale(AnimationState state, int durationMs) {
        long elapsed = System.currentTimeMillis() - state.startTime;
        float progress = Math.min(1.0f, (float) elapsed / durationMs);

        float easedProgress = easeOutCubic(progress);

        return state.startScale + (state.targetScale - state.startScale) * easedProgress;
    }


    public static float easeOutCubic(float t) {
        float inv = 1.0f - t;
        return 1.0f - inv * inv * inv;
    }


    public static float easeInCubic(float t) {
        return t * t * t;
    }


    public static float easeInOutCubic(float t) {
        if (t < 0.5f) {
            return 4.0f * t * t * t;
        } else {
            float f = 2.0f * t - 2.0f;
            return 0.5f * f * f * f + 1.0f;
        }
    }

    public static float lerp(float start, float end, float t) {
        return start + (end - start) * t;
    }
}
