package com.niixlabs.lucidadvancements.mixin;

import com.niixlabs.lucidadvancements.client.gui.LucidAdvancementsScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AdvancementsScreen.class)
public abstract class AdvancementsScreenMixin {

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void redirect(CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.screen instanceof LucidAdvancementsScreen)) {
            mc.setScreen(new LucidAdvancementsScreen(mc.player.connection.getAdvancements()));
            ci.cancel();
        }
    }
}
