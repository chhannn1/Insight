package com.chan1.fabric;

import net.fabricmc.api.ModInitializer;

import com.chan1.Insight;

public final class InsightFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Insight.init();
    }
}
