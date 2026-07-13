package com.niixlabs.lucidadvancements.client.gui.screen;

import com.niixlabs.lucidadvancements.config.LucidConfig;

public final class ScreenMetrics {
    private ScreenMetrics() {}

    public static int sidebarWidth() {
        return LucidConfig.screenSidebarWidth;
    }

    public static int topBarHeight() {
        return LucidConfig.screenTopBarHeight;
    }

    public static int sidebarRowHeight() {
        return LucidConfig.screenSidebarRowHeight;
    }

    public static int sidebarItemHeight() {
        return LucidConfig.screenSidebarItemHeight;
    }

    public static int sidebarProgressHeight() {
        return LucidConfig.screenSidebarProgressHeight;
    }

    public static int sidebarTopPadding() {
        return LucidConfig.screenSidebarTopPadding;
    }

    public static int contentMargin() {
        return LucidConfig.screenContentMargin;
    }

    public static int cardSpacing() {
        return LucidConfig.screenCardSpacing;
    }

    public static int viewportBottomMargin() {
        return LucidConfig.screenViewportBottomMargin;
    }

    public static int headerHeightWithDescription() {
        return LucidConfig.screenHeaderHeight;
    }

    public static int minScrollThumbHeight() {
        return LucidConfig.screenMinScrollThumbHeight;
    }

    public static int scrollbarWidth() {
        return LucidConfig.screenScrollbarWidth;
    }

    public static int scrollbarRightMargin() {
        return LucidConfig.screenScrollbarRightMargin;
    }

    public static int contentX() {
        return sidebarWidth() + contentMargin();
    }

    public static int contentWidth(int screenWidth) {
        return screenWidth - sidebarWidth() - (contentMargin() * 2);
    }

    public static int viewportY(boolean searching) {
        return topBarHeight() + headerHeightWithDescription();
    }
}