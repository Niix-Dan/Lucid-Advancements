package com.niixlabs.lucidadvancements;

import com.niixlabs.lucidadvancements.client.gui.LucidAdvancementsOverlay;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class LucidAdvancementsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        HudRenderCallback.EVENT.register((guiGraphics, deltaTracker) -> {
            float partialTick = deltaTracker.getGameTimeDeltaTicks();
            LucidAdvancementsOverlay.render(guiGraphics, partialTick);
        });
    }
}
