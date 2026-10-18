package com.niixlabs.lucidadvancements.mixin;

import com.niixlabs.lucidadvancements.config.LucidConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EffectRenderingInventoryScreen.class)
public class EffectRenderingInventoryScreenMixin {
    @Inject(method = "renderEffects", at = @At("HEAD"), cancellable = true)
    private void lucid$cancelVanillaEffects(GuiGraphics guiGraphics, int mouseX, int mouseY, CallbackInfo ci) {
        if (LucidConfig.effectEnabled) {
            ci.cancel();
        }
    }
}
