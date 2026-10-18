package com.niixlabs.lucidadvancements.mixin;

import com.niixlabs.lucidadvancements.client.gui.overlay.LucidNotificationOverlay;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ToastComponent.class)
public class ToastComponentMixin {
    @Inject(method = "clear", at = @At("HEAD"))
    private void lucid$clearNotifications(CallbackInfo ci) {
        LucidNotificationOverlay.clear();
    }

    @Inject(method = "addToast", at = @At("HEAD"), cancellable = true)
    private void lucid$replaceToast(Toast toast, CallbackInfo ci) {
        if (!(toast instanceof LucidNotificationOverlay.Provider provider)) {
            return;
        }

        LucidNotificationOverlay.Notification notification = provider.lucid$notification();
        if (notification != null) {
            LucidNotificationOverlay.add(notification);
            ci.cancel();
        }
    }
}
