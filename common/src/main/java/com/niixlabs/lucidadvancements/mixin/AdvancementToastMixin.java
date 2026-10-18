package com.niixlabs.lucidadvancements.mixin;

import com.niixlabs.lucidadvancements.client.gui.overlay.LucidNotificationOverlay;
import com.niixlabs.lucidadvancements.config.LucidConfig;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AdvancementToast.class)
public abstract class AdvancementToastMixin implements LucidNotificationOverlay.Provider {
    @Shadow @Final private AdvancementHolder advancement;

    @Override
    public LucidNotificationOverlay.Notification lucid$notification() {
        DisplayInfo display = advancement.value().display().orElse(null);
        if (display == null) {
            return null;
        }

        int accent = display.getType() == AdvancementType.CHALLENGE
                ? LucidConfig.notificationChallengeAccent
                : LucidConfig.notificationNormalAccent;

        if (display.getType() == AdvancementType.CHALLENGE) {
            Minecraft.getInstance().getSoundManager().play(
                    SimpleSoundInstance.forUI(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F)
            );
        }

        int titleColor = display.getType() == AdvancementType.CHALLENGE
                ? LucidConfig.notificationChallengeTitleColor
                : LucidConfig.notificationTitleColor;

        return LucidNotificationOverlay.Notification.item(
                display.getIcon(),
                display.getTitle(),
                display.getType().getDisplayName(),
                accent,
                titleColor
        );
    }
}
