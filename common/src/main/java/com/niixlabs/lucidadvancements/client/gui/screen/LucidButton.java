package com.niixlabs.lucidadvancements.client.gui.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

final class LucidButton extends Button {
    private static final int COLOR_BACKGROUND_HOVERED = 0xEE1E1E1E;
    private static final int COLOR_BACKGROUND_IDLE = 0xEE121212;
    private static final int COLOR_BORDER_HOVERED = 0xFF00FFAA;
    private static final int COLOR_BORDER_IDLE = 0x33FFFFFF;
    private static final int COLOR_TEXT_HOVERED = 0xFF00FFAA;
    private static final int COLOR_TEXT_IDLE = 0xFFE0E0E0;

    LucidButton(int x, int y, int width, int height, Component message, OnPress onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!visible) {
            return;
        }

        boolean hovered = isHovered();
        int backgroundColor = hovered ? COLOR_BACKGROUND_HOVERED : COLOR_BACKGROUND_IDLE;
        int borderColor = hovered ? COLOR_BORDER_HOVERED : COLOR_BORDER_IDLE;
        int textColor = hovered ? COLOR_TEXT_HOVERED : COLOR_TEXT_IDLE;

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
        guiGraphics.drawCenteredString(font, getMessage(), x1 + width / 2, y1 + (height - 8) / 2, textColor);
    }
}