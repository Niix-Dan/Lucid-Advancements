package com.niixlabs.lucidadvancements.client.gui.screen;

import com.niixlabs.lucidadvancements.config.LucidConfig;
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
        return options.size() * LucidConfig.dropdownOptionHeight;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!visible) {
            return;
        }

        boolean highlighted = isHovered() || open;
        int backgroundColor = highlighted ? LucidConfig.widgetBackgroundHovered : LucidConfig.widgetBackgroundIdle;
        int borderColor = highlighted ? LucidConfig.widgetBorderHovered : LucidConfig.widgetBorderIdle;
        int textColor = highlighted ? LucidConfig.widgetTextHovered : LucidConfig.widgetTextIdle;

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
        guiGraphics.drawString(font, open ? LucidConfig.dropdownArrowOpen : LucidConfig.dropdownArrowClosed, x2 - 14, y1 + (height - 8) / 2, textColor);
    }

    void renderOptions(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!open) {
            return;
        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 200);

        Font font = Minecraft.getInstance().font;
        int x1 = getX();
        int y1 = getY() + height;
        int x2 = x1 + width;
        int optHeight = optionsHeight();

        guiGraphics.fill(x1, y1, x2, y1 + optHeight, LucidConfig.dropdownOptionBackground);

        int optY = y1;
        for (T option : options) {
            boolean hovered = mouseX >= x1 && mouseX <= x2 && mouseY >= optY && mouseY <= optY + LucidConfig.dropdownOptionHeight;
            if (hovered) {
                guiGraphics.fill(x1, optY, x2, optY + LucidConfig.dropdownOptionHeight, LucidConfig.dropdownOptionHover);
            }

            int textColor = option.equals(selected) ? LucidConfig.dropdownOptionSelectedText : (hovered ? LucidConfig.widgetTextHovered : LucidConfig.widgetTextIdle);
            guiGraphics.drawString(font, labelProvider.apply(option), x1 + 6, optY + (LucidConfig.dropdownOptionHeight - 8) / 2, textColor);
            optY += LucidConfig.dropdownOptionHeight;
        }

        guiGraphics.fill(x1, y1, x1 + 1, y1 + optHeight, LucidConfig.widgetBorderHovered);
        guiGraphics.fill(x2 - 1, y1, x2, y1 + optHeight, LucidConfig.widgetBorderHovered);
        guiGraphics.fill(x1, y1 + optHeight - 1, x2, y1 + optHeight, LucidConfig.widgetBorderHovered);

        guiGraphics.pose().popPose();
    }

    void mouseClickedOptions(double mouseX, double mouseY) {
        int x1 = getX();
        int y1 = getY() + height;
        int x2 = x1 + width;
        int optHeight = optionsHeight();

        if (mouseX >= x1 && mouseX <= x2 && mouseY >= y1 && mouseY <= y1 + optHeight) {
            int index = (int) ((mouseY - y1) / LucidConfig.dropdownOptionHeight);
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