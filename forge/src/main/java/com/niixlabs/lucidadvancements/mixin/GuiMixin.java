package com.niixlabs.lucidadvancements.mixin;

import com.niixlabs.lucidadvancements.client.gui.overlay.LucidAdvancementsOverlay;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.DeltaTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void renderCustomHUD(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        LucidAdvancementsOverlay.render(guiGraphics, deltaTracker.getGameTimeDeltaTicks());
    }
}
