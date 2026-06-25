package com.niixlabs.lucidadvancements;

import com.niixlabs.lucidadvancements.client.gui.LucidAdvancementsOverlay;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class LucidAdvancementsEvents {
    @SubscribeEvent
    public static void onRenderGuiPost(RenderGuiEvent.Post event) {
        float partialTick = event.getPartialTick().getGameTimeDeltaTicks();
        LucidAdvancementsOverlay.render(event.getGuiGraphics(), partialTick);
    }
}
