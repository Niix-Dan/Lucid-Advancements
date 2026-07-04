package com.niixlabs.lucidadvancements.client.gui.screen;

import com.niixlabs.lucidadvancements.config.LucidConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

public final class GuiScale {
    private static final double MIN_VIRTUAL_WIDTH = 550.0;
    private static final double MIN_VIRTUAL_HEIGHT = 300.0;

    private GuiScale() {}

    public static double targetScale(Minecraft minecraft) {
        double screenWidth = minecraft.getWindow().getScreenWidth();
        double screenHeight = minecraft.getWindow().getScreenHeight();

        double maxScaleX = screenWidth / MIN_VIRTUAL_WIDTH;
        double maxScaleY = screenHeight / MIN_VIRTUAL_HEIGHT;
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