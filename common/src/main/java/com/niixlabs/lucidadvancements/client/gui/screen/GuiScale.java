package com.niixlabs.lucidadvancements.client.gui.screen;

import com.niixlabs.lucidadvancements.config.LucidConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

public final class GuiScale {
    private GuiScale() {}

    public static double targetScale(Minecraft minecraft) {
        double screenWidth = minecraft.getWindow().getScreenWidth();
        double screenHeight = minecraft.getWindow().getScreenHeight();

        double maxScaleX = screenWidth / LucidConfig.scaleMinVirtualWidth;
        double maxScaleY = screenHeight / LucidConfig.scaleMinVirtualHeight;
        double maxSafeScale = Math.max(1.0, Math.floor(Math.min(maxScaleX, maxScaleY)));
        double vanillaScale = minecraft.getWindow().getGuiScale();

        if (LucidConfig.customGuiScale == 0) {
            return Math.min(vanillaScale, maxSafeScale);
        }
        return Mth.clamp((double) LucidConfig.customGuiScale, 1.0, maxSafeScale);
    }

    public static double scaleFactor(Minecraft minecraft) {
        return minecraft.getWindow().getGuiScale() / targetScale(minecraft);
    }

    public static float scaleModifier(Minecraft minecraft) {
        return (float) (targetScale(minecraft) / minecraft.getWindow().getGuiScale());
    }
}