package com.niixlabs.lucidadvancements.mixin;

import com.niixlabs.lucidadvancements.client.gui.overlay.LucidAdvancementsOverlay;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {
    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void lucid$handleOverlayShortcuts(long window, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
        boolean control = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
        boolean alt = (modifiers & GLFW.GLFW_MOD_ALT) != 0;

        if (!control && !alt) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null || mc.player == null) {
            return;
        }

        if (!mc.options.keyAdvancements.matches(key, scanCode)) {
            return;
        }

        if (action == GLFW.GLFW_PRESS) {
            if (alt) {
                LucidAdvancementsOverlay.toggleVisible();
            } else {
                LucidAdvancementsOverlay.nextPage();
            }
        }

        if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT) {
            ci.cancel();
        }
    }
}
