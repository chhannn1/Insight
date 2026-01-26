package com.chan1.client.util;

import net.minecraft.client.multiplayer.ClientLevel;


public final class MinecraftTimeHelper {


    public static final long DAY_LENGTH = 24000L;

    public static final long NIGHT_START = 12542L;

    public static final long NIGHT_END = 23460L;

    private MinecraftTimeHelper() {
    }


    public static boolean isNightTime(ClientLevel level) {
        if (level == null) {
            return false;
        }
        long dayTime = level.getDayTime() % DAY_LENGTH;
        return dayTime >= NIGHT_START || dayTime < 0;
    }


    public static long getTicksUntilNight(ClientLevel level) {
        if (level == null) {
            return 0;
        }
        long dayTime = level.getDayTime() % DAY_LENGTH;

        if (dayTime >= NIGHT_START) {
            // Already night
            return 0;
        }

        return NIGHT_START - dayTime;
    }


    public static String formatTimeUntilNight(ClientLevel level) {
        long ticksUntilNight = getTicksUntilNight(level);

        if (ticksUntilNight <= 0) {
            return null;
        }

        long totalSeconds = ticksUntilNight / 20;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;

        return String.format("(%d:%02d) minutes until night", minutes, seconds);
    }
}
