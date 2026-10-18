package com.niixlabs.lucidadvancements.mixin;

import com.niixlabs.lucidadvancements.client.gui.overlay.LucidNotificationOverlay;
import com.niixlabs.lucidadvancements.config.LucidConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiEffectsMixin {
    @Inject(method = "renderEffects", at = @At("HEAD"), cancellable = true)
    private void lucid$renderEffects(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui || !LucidConfig.effectEnabled) {
            return;
        }

        LucidNotificationOverlay.renderEffects(guiGraphics);
        ci.cancel();
    }
}
