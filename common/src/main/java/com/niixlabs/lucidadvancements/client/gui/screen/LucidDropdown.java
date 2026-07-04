package com.niixlabs.lucidadvancements.client.gui.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

final class LucidDropdown<T> extends AbstractWidget {
    private static final int COLOR_BACKGROUND_HOVERED = 0xEE1E1E1E;
    private static final int COLOR_BACKGROUND_IDLE = 0xEE121212;
    private static final int COLOR_BORDER_HOVERED = 0xFF00FFAA;
    private static final int COLOR_BORDER_IDLE = 0x33FFFFFF;
    private static final int COLOR_TEXT_HOVERED = 0xFF00FFAA;
    private static final int COLOR_TEXT_IDLE = 0xFFE0E0E0;
    private static final int COLOR_OPTION_BACKGROUND = 0xF2161616;
    private static final int COLOR_OPTION_HOVER = 0xFF1E1E1E;
    private static final int COLOR_OPTION_SELECTED_TEXT = 0xFF00FFAA;
    private static final int OPTION_HEIGHT = 16;
    private static final String ARROW_CLOSED = "\u25BE";
    private static final String ARROW_OPEN = "\u25B4";

    private final List<T> options;
    private final Function<T, String> labelProvider;
    private final Consumer<T> onSelect;

    private T selected;
    private boolean open = false;

    LucidDropdown(int x, int y, int minWidth, int height, T initial, List<T> options,
                  Function<T, String> labelProvider, Consumer<T> onSelect) {
        super(x, y, minWidth, height, Component.empty());
        this.options = options;
        this.labelProvider = labelProvider;
        this.onSelect = onSelect;
        this.selected = initial;

        Font font = Minecraft.getInstance().font;
        int maxWidth = 0;
        for (T option : options) {
            int textWidth = font.width(labelProvider.apply(option));
            if (textWidth > maxWidth) {
                maxWidth = textWidth;
            }
        }

        int idealWidth = maxWidth + 24;
        this.width = Math.max(minWidth, idealWidth);
    }

    boolean isOpen() {
        return open;
    }

    void close() {
        open = false;
    }

    private int optionsHeight() {
        return options.size() * OPTION_HEIGHT;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!visible) {
            return;
        }

        boolean highlighted = isHovered() || open;
        int backgroundColor = highlighted ? COLOR_BACKGROUND_HOVERED : COLOR_BACKGROUND_IDLE;
        int borderColor = highlighted ? COLOR_BORDER_HOVERED : COLOR_BORDER_IDLE;
        int textColor = highlighted ? COLOR_TEXT_HOVERED : COLOR_TEXT_IDLE;

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

        guiGraphics.drawString(font, labelProvider.apply(selected), x1 + 6, y1 + (height - 8) / 2, textColor);
        guiGraphics.drawString(font, open ? ARROW_OPEN : ARROW_CLOSED, x2 - 14, y1 + (height - 8) / 2, textColor);
    }

    void renderOptions(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!open) {
            return;
        }

        Font font = Minecraft.getInstance().font;
        int x1 = getX();
        int y1 = getY() + height;
        int x2 = x1 + width;
        int optHeight = optionsHeight();

        guiGraphics.fill(x1, y1, x2, y1 + optHeight, COLOR_OPTION_BACKGROUND);

        int optY = y1;
        for (T option : options) {
            boolean hovered = mouseX >= x1 && mouseX <= x2 && mouseY >= optY && mouseY <= optY + OPTION_HEIGHT;
            if (hovered) {
                guiGraphics.fill(x1, optY, x2, optY + OPTION_HEIGHT, COLOR_OPTION_HOVER);
            }

            int textColor = option.equals(selected) ? COLOR_OPTION_SELECTED_TEXT : (hovered ? COLOR_TEXT_HOVERED : COLOR_TEXT_IDLE);
            guiGraphics.drawString(font, labelProvider.apply(option), x1 + 6, optY + (OPTION_HEIGHT - 8) / 2, textColor);
            optY += OPTION_HEIGHT;
        }

        guiGraphics.fill(x1, y1, x1 + 1, y1 + optHeight, COLOR_BORDER_HOVERED);
        guiGraphics.fill(x2 - 1, y1, x2, y1 + optHeight, COLOR_BORDER_HOVERED);
        guiGraphics.fill(x1, y1 + optHeight - 1, x2, y1 + optHeight, COLOR_BORDER_HOVERED);
    }

    void mouseClickedOptions(double mouseX, double mouseY) {
        int x1 = getX();
        int y1 = getY() + height;
        int x2 = x1 + width;
        int optHeight = optionsHeight();

        if (mouseX >= x1 && mouseX <= x2 && mouseY >= y1 && mouseY <= y1 + optHeight) {
            int index = (int) ((mouseY - y1) / OPTION_HEIGHT);
            if (index >= 0 && index < options.size()) {
                selected = options.get(index);
                onSelect.accept(selected);
            }
        }
        open = false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || !visible || !active) {
            return false;
        }
        if (isMouseOver(mouseX, mouseY)) {
            open = !open;
            return true;
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }
}