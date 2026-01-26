package com.chan1.fabric.client;

import com.chan1.client.InsightClient;
import com.chan1.client.config.InsightConfig;
import net.fabricmc.api.ClientModInitializer;

public final class InsightFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        InsightConfig.load();
        InsightClient.init();
    }
}
