package com.niixlabs.lucidadvancements.client.gui.util;

import com.niixlabs.lucidadvancements.config.LucidConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

public class LucidScrollHandler {
    private double scrollOffset = 0;
    private double maxScroll = 0;
    private boolean dragging = false;
    private double dragClickOffset = 0;

    public double getScrollOffset() {
        return scrollOffset;
    }

    public void setScrollOffset(double offset) {
        this.scrollOffset = Mth.clamp(offset, 0, maxScroll);
    }

    public double getMaxScroll() {
        return maxScroll;
    }

    public void updateMaxScroll(double maxScroll) {
        this.maxScroll = Math.max(0, maxScroll);
        this.scrollOffset = Mth.clamp(this.scrollOffset, 0, this.maxScroll);
    }

    public boolean isDragging() {
        return dragging;
    }

    public void setDragging(boolean dragging) {
        this.dragging = dragging;
    }

    public int getThumbHeight(int viewportHeight) {
        if (maxScroll <= 0) {
            return 0;
        }
        return Math.max(LucidConfig.screenMinScrollThumbHeight,
                (int) ((viewportHeight / (float) (viewportHeight + maxScroll)) * viewportHeight));
    }

    public int getThumbY(int viewportY, int viewportHeight) {
        if (maxScroll <= 0) {
            return viewportY;
        }
        return viewportY + (int) ((scrollOffset / maxScroll) * (viewportHeight - getThumbHeight(viewportHeight)));
    }

    public void renderScrollbar(GuiGraphics guiGraphics, int width, int viewportY, int viewportHeight) {
        if (maxScroll <= 0) {
            return;
        }

        int scrollbarX = width - LucidConfig.screenScrollbarRightMargin;
        int thumbHeight = getThumbHeight(viewportHeight);
        int thumbY = getThumbY(viewportY, viewportHeight);

        guiGraphics.fill(scrollbarX, viewportY, scrollbarX + LucidConfig.screenScrollbarWidth, viewportY + viewportHeight, LucidConfig.screenScrollbarTrackColor);
        int thumbColor = dragging ? LucidConfig.screenScrollbarThumbActive : LucidConfig.screenScrollbarThumbIdle;
        guiGraphics.fill(scrollbarX, thumbY, scrollbarX + LucidConfig.screenScrollbarWidth, thumbY + thumbHeight, thumbColor);
    }

    public boolean handleMouseDown(double mouseX, double mouseY, int width, int viewportY, int viewportHeight) {
        if (maxScroll <= 0) {
            return false;
        }

        int scrollbarX = width - LucidConfig.screenScrollbarRightMargin;
        if (mouseX < scrollbarX - 2 || mouseX > scrollbarX + 5) {
            return false;
        }

        int thumbHeight = getThumbHeight(viewportHeight);
        int thumbY = getThumbY(viewportY, viewportHeight);

        if (mouseY >= thumbY && mouseY <= thumbY + thumbHeight) {
            dragging = true;
            dragClickOffset = mouseY - thumbY;
            return true;
        }
        return false;
    }

    public boolean handleMouseDragged(double mouseY, int viewportY, int viewportHeight) {
        if (dragging && maxScroll > 0) {
            int thumbHeight = getThumbHeight(viewportHeight);
            int trackHeight = viewportHeight - thumbHeight;

            if (trackHeight > 0) {
                double targetThumbY = mouseY - dragClickOffset;
                double scrollPercentage = Mth.clamp((targetThumbY - viewportY) / trackHeight, 0.0, 1.0);
                scrollOffset = scrollPercentage * maxScroll;
            }
            return true;
        }
        return false;
    }

    public boolean handleMouseScrolled(double scrollY, double scrollSpeed) {
        if (maxScroll > 0) {
            setScrollOffset(scrollOffset - (scrollY * scrollSpeed));
            return true;
        }
        return false;
    }
}