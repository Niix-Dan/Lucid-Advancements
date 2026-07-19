package com.niixlabs.lucidadvancements.client.gui.screen;

import com.niixlabs.lucidadvancements.config.LucidConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

final class LucidButton extends Button {
    LucidButton(int x, int y, int width, int height, Component message, OnPress onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!visible) {
            return;
        }

        boolean hovered = isHovered();
        int backgroundColor = hovered ? LucidConfig.widgetBackgroundHovered : LucidConfig.widgetBackgroundIdle;
        int borderColor = hovered ? LucidConfig.widgetBorderHovered : LucidConfig.widgetBorderIdle;
        int textColor = hovered ? LucidConfig.widgetTextHovered : LucidConfig.widgetTextIdle;

        int x1 = getX();
        int y1 = getY();
        int x2 = x1 + width;
        int y2 = y1 + height;

        guiGraphics.fill(x1, y1, x2, y2, backgroundColor);
        guiGraphics.fill(x1, y1, x2, y1 + 1, borderColor);
        guiGraphics.fill(x1, y2 - 1, x2, y2, borderColor);
        guiGraphics.fill(x1, y1, x1 + 1, y2, borderColor);
        guiGraphics.fill(x2 - 1, y1, x2, y2, borderColor);

        Font font = Minecraft.getInstance().font;
        guiGraphics.centeredText(font, getMessage(), x1 + width / 2, y1 + (height - 8) / 2, textColor);
    }
}