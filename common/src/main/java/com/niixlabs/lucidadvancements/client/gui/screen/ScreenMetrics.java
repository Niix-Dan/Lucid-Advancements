package com.niixlabs.lucidadvancements.client.gui.screen;

public final class ScreenMetrics {
    public static final int SIDEBAR_WIDTH = 100;
    public static final int TOP_BAR_HEIGHT = 48;
    public static final int SIDEBAR_ROW_HEIGHT = 42;
    public static final int SIDEBAR_ITEM_HEIGHT = 34;
    public static final int SIDEBAR_PROGRESS_HEIGHT = 38;
    public static final int SIDEBAR_TOP_PADDING = 12;
    public static final int CONTENT_MARGIN = 18;
    public static final int CARD_SPACING = 8;
    public static final int VIEWPORT_BOTTOM_MARGIN = 18;
    public static final int HEADER_HEIGHT_WITH_DESCRIPTION = 52;
    public static final int MIN_SCROLL_THUMB_HEIGHT = 24;
    public static final int SCROLLBAR_WIDTH = 3;
    public static final int SCROLLBAR_RIGHT_MARGIN = 12;

    private ScreenMetrics() {}

    public static int contentX() {
        return SIDEBAR_WIDTH + CONTENT_MARGIN;
    }

    public static int contentWidth(int screenWidth) {
        return screenWidth - SIDEBAR_WIDTH - (CONTENT_MARGIN * 2);
    }

    public static int viewportY(boolean searching) {
        return TOP_BAR_HEIGHT + HEADER_HEIGHT_WITH_DESCRIPTION;//TOP_BAR_HEIGHT + (searching ? HEADER_HEIGHT_SEARCH : HEADER_HEIGHT_WITH_DESCRIPTION);
    }
}