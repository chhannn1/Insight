package com.chan1.client;

import com.chan1.client.feature.items.DroppedItemDetector;
import dev.architectury.event.events.client.ClientTickEvent;


public final class InsightClient {

    private static boolean initialized = false;

    private InsightClient() {
    }

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;

        ClientTickEvent.CLIENT_POST.register(minecraft -> {
            if (minecraft.level != null && minecraft.player != null) {
                DroppedItemDetector.tick();
            }
        });
    }
}
