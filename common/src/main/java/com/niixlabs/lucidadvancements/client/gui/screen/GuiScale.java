package com.niixlabs.lucidadvancements.client.gui.screen;

import com.niixlabs.lucidadvancements.config.LucidConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public final class GuiScale {
    public static final int MAX_CUSTOM_SCALE = 8;

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

    public static int maxSupportedScale(Minecraft minecraft) {
        double screenWidth = minecraft.getWindow().getScreenWidth();
        double screenHeight = minecraft.getWindow().getScreenHeight();

        double maxScaleX = screenWidth / LucidConfig.scaleMinVirtualWidth;
        double maxScaleY = screenHeight / LucidConfig.scaleMinVirtualHeight;
        int maxSafeScale = (int) Math.max(1.0, Math.floor(Math.min(maxScaleX, maxScaleY)));

        return Math.min(maxSafeScale, MAX_CUSTOM_SCALE);
    }

    public static List<Integer> supportedScaleOptions(Minecraft minecraft) {
        List<Integer> options = new ArrayList<>();
        options.add(0);

        int maxScale = maxSupportedScale(minecraft);
        for (int scale = 1; scale <= maxScale; scale++) {
            options.add(scale);
        }

        return options;
    }
}