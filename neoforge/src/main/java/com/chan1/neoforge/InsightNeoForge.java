package com.chan1.neoforge;

import com.chan1.Insight;
import com.chan1.client.InsightClient;
import com.chan1.client.config.InsightConfig;
import com.chan1.client.config.InsightConfigScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.ConfigScreenHandler;

@Mod(Insight.MOD_ID)
public final class InsightNeoForge {
    public InsightNeoForge(IEventBus modEventBus) {
        Insight.init();

        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(this::onClientSetup);

            ModLoadingContext.get().registerExtensionPoint(
                    ConfigScreenHandler.ConfigScreenFactory.class,
                    () -> new ConfigScreenHandler.ConfigScreenFactory(
                            (mc, parent) -> InsightConfigScreen.create(parent)
                    )
            );
        }
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            InsightConfig.load();
            InsightClient.init();
        });
    }
}
