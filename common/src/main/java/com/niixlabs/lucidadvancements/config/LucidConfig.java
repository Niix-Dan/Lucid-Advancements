package com.niixlabs.lucidadvancements.config;

public final class LucidConfig {
    private LucidConfig() {}

    @ConfigOption(comment = "Interface scale behavior. 0 = Auto/Vanilla, 1-4 = Fixed Custom Scale Factor")
    public static int customGuiScale = 0;

    @ConfigOption(comment = "Minimum virtual width used to compute the maximum safe auto scale")
    public static double scaleMinVirtualWidth = 550.0;

    @ConfigOption(comment = "Minimum virtual height used to compute the maximum safe auto scale")
    public static double scaleMinVirtualHeight = 300.0;

    @ConfigSection("HUD OVERLAY CONFIGURATION")
    @ConfigOption(comment = "Background fill color for the pinned advancement boxes", hex = true)
    public static int overlayBgColor = 0x880A0A0A;

    @ConfigOption(comment = "Vertical strip color for normal advancements pinned to HUD", hex = true)
    public static int overlayNormalAccent = 0xFF00FFAA;

    @ConfigOption(comment = "Vertical strip color for challenge advancements pinned to HUD", hex = true)
    public static int overlayChallengeAccent = 0xFFAA00FF;

    @ConfigOption(comment = "Text color for normal advancement titles on HUD", hex = true)
    public static int overlayTitleColor = 0xFFFFFFFF;

    @ConfigOption(comment = "Text color for challenge advancement titles on HUD", hex = true)
    public static int overlayChallengeTitleColor = 0xFFFF77FF;

    @ConfigOption(comment = "Text color for remaining criteria lines on HUD boxes", hex = true)
    public static int overlayCriterionTextColor = 0xFFAAAAAA;

    @ConfigOption(comment = "Text color for the hidden criteria counter on HUD boxes", hex = true)
    public static int overlayHiddenCountTextColor = 0xFF555555;

    @ConfigOption(comment = "Icon shown before each locked criterion on HUD boxes")
    public static String overlayIconLocked = "\uD83D\uDD12";

    @ConfigOption(comment = "Width of each pinned advancement box")
    public static int overlayBoxWidth = 135;

    @ConfigOption(comment = "Right screen margin for pinned advancement boxes")
    public static int overlayRightMargin = 8;

    @ConfigOption(comment = "Starting Y position for the first pinned advancement box")
    public static int overlayStartY = 55;

    @ConfigOption(comment = "Vertical spacing between pinned advancement boxes")
    public static int overlayBoxSpacing = 5;

    @ConfigOption(comment = "Bottom screen margin before pinned boxes stop rendering")
    public static int overlayBottomScreenMargin = 35;

    @ConfigOption(comment = "Base height for a pinned advancement box with no criteria lines")
    public static int overlayBaseBoxHeight = 24;

    @ConfigOption(comment = "Line height for each criterion line on a pinned box")
    public static int overlayLineHeight = 10;

    @ConfigOption(comment = "Extra padding added when at least one criterion line is shown")
    public static int overlayOverflowLinePadding = 2;

    @ConfigOption(comment = "Maximum number of remaining criteria shown per pinned box")
    public static int overlayMaxVisibleCriteria = 3;

    @ConfigOption(comment = "Width of the colored accent strip on pinned boxes")
    public static int overlayAccentWidth = 2;

    @ConfigOption(comment = "Horizontal offset of the icon inside a pinned box")
    public static int overlayIconXOffset = 6;

    @ConfigOption(comment = "Vertical offset of the icon inside a pinned box")
    public static int overlayIconYOffset = 4;

    @ConfigOption(comment = "Horizontal offset of the title inside a pinned box")
    public static int overlayTitleXOffset = 26;

    @ConfigOption(comment = "Vertical offset of the title inside a pinned box")
    public static int overlayTitleYOffset = 5;

    @ConfigOption(comment = "Padding subtracted from box width when truncating the title")
    public static int overlayTitleMaxWidthPadding = 32;

    @ConfigOption(comment = "Vertical offset where criteria lines start inside a pinned box")
    public static int overlayCriteriaStartYOffset = 22;

    @ConfigOption(comment = "Horizontal offset of criteria lines inside a pinned box")
    public static int overlayCriterionXOffset = 8;

    @ConfigOption(comment = "Padding subtracted from box width when truncating criteria lines")
    public static int overlayCriterionMaxWidthPadding = 10;

    @ConfigSection("UNOBTAINED NORMAL ADVANCEMENTS")
    @ConfigOption(comment = "Background color gradient start", hex = true)
    public static int cardNormalBg1 = 0xAA1A1A1A;

    @ConfigOption(comment = "Background color gradient end", hex = true)
    public static int cardNormalBg2 = 0xAA121212;

    @ConfigOption(comment = "Border color", hex = true)
    public static int cardNormalBorder = 0x882A2A2A;

    @ConfigOption(comment = "Title text color", hex = true)
    public static int cardNormalTitle = 0xFFFFFFFF;

    @ConfigSection("UNOBTAINED CHALLENGE/RARE ADVANCEMENTS")
    @ConfigOption(comment = "Border color", hex = true)
    public static int cardRareBorder = 0xAA440077;

    @ConfigOption(comment = "Title text color", hex = true)
    public static int cardRareTitle = 0xFFCC77FF;

    @ConfigSection("COMPLETED NORMAL ADVANCEMENTS")
    @ConfigOption(comment = "Background color gradient start", hex = true)
    public static int cardObtainedBg1 = 0xBB142E1F;

    @ConfigOption(comment = "Background color gradient end", hex = true)
    public static int cardObtainedBg2 = 0xBB0C1E14;

    @ConfigOption(comment = "Border color", hex = true)
    public static int cardObtainedBorder = 0xAA00FFAA;

    @ConfigOption(comment = "Title text color", hex = true)
    public static int cardObtainedTitle = 0xFF77FFAA;

    @ConfigSection("COMPLETED CHALLENGE/RARE ADVANCEMENTS")
    @ConfigOption(comment = "Background color gradient start", hex = true)
    public static int cardObtainedRareBg1 = 0xBB2D1438;

    @ConfigOption(comment = "Background color gradient end", hex = true)
    public static int cardObtainedRareBg2 = 0xBB1E0C26;

    @ConfigOption(comment = "Border color", hex = true)
    public static int cardObtainedRareBorder = 0xAAAA00FF;

    @ConfigOption(comment = "Title text color", hex = true)
    public static int cardObtainedRareTitle = 0xFFFF77FF;

    @ConfigSection("ADVANCEMENT CARD COLORS")
    @ConfigOption(comment = "Accent color used when a card is tracked", hex = true)
    public static int cardTrackedActiveColor = 0xFF00FFAA;

    @ConfigOption(comment = "Color used for inactive icons and text", hex = true)
    public static int cardInactiveColor = 0xFF555555;

    @ConfigOption(comment = "Color of the '?' mark on hidden advancements", hex = true)
    public static int cardHiddenMarkColor = 0xFF666666;

    @ConfigOption(comment = "Color of the description text", hex = true)
    public static int cardDescriptionColor = 0xFFAAAAAA;

    @ConfigOption(comment = "Color of the requirements label", hex = true)
    public static int cardRequirementsLabelColor = 0xFF888888;

    @ConfigOption(comment = "Color of completed criteria text", hex = true)
    public static int cardCriterionDoneColor = 0xFF00CC88;

    @ConfigOption(comment = "Color of pending criteria text", hex = true)
    public static int cardCriterionPendingColor = 0xFF777777;

    @ConfigOption(comment = "Color of the divider line between description and criteria", hex = true)
    public static int cardDividerColor = 0x44FFFFFF;

    @ConfigOption(comment = "Color of the overlay shown when hovering a card", hex = true)
    public static int cardHoverOverlayColor = 0x20FFFFFF;

    @ConfigOption(comment = "Icon shown before locked criteria")
    public static String cardIconLocked = "\uD83D\uDD12";

    @ConfigOption(comment = "Icon shown for completed advancements/criteria")
    public static String cardIconDone = "\u2714";

    @ConfigOption(comment = "Icon shown for the tracked toggle")
    public static String cardIconTracked = "\u2726";

    @ConfigOption(comment = "Label shown in place of hidden advancement title/description")
    public static String cardHiddenLabel = "???";

    @ConfigSection("ADVANCEMENT CARD LAYOUT")
    @ConfigOption(comment = "Minimum height of a card")
    public static int cardBaseHeightMin = 46;

    @ConfigOption(comment = "Padding added per description line to compute base height")
    public static int cardBaseHeightPadding = 28;

    @ConfigOption(comment = "Line height for description and criteria text")
    public static int cardLineHeight = 10;

    @ConfigOption(comment = "Extra header height added when a card is expanded")
    public static int cardExpandedHeaderHeight = 20;

    @ConfigOption(comment = "Padding added around the criteria section")
    public static int cardCriteriaSectionPadding = 12;

    @ConfigOption(comment = "Horizontal offset of the title and description text")
    public static int cardTextXOffset = 40;

    @ConfigOption(comment = "Horizontal offset of the item icon")
    public static int cardIconXOffset = 12;

    @ConfigOption(comment = "Horizontal offset of the track icon from the right edge")
    public static int cardTrackIconXOffset = 20;

    @ConfigOption(comment = "Horizontal offset of the status icon from the right edge")
    public static int cardStatusIconXOffset = 30;

    @ConfigSection("SCREEN LAYOUT")
    @ConfigOption(comment = "Width of the sidebar")
    public static int screenSidebarWidth = 100;

    @ConfigOption(comment = "Height of the top bar")
    public static int screenTopBarHeight = 48;

    @ConfigOption(comment = "Height of each sidebar row")
    public static int screenSidebarRowHeight = 42;

    @ConfigOption(comment = "Height of each sidebar item highlight")
    public static int screenSidebarItemHeight = 34;

    @ConfigOption(comment = "Height reserved for the sidebar progress bar")
    public static int screenSidebarProgressHeight = 38;

    @ConfigOption(comment = "Top padding before the first sidebar row")
    public static int screenSidebarTopPadding = 12;

    @ConfigOption(comment = "Margin around the main content area")
    public static int screenContentMargin = 18;

    @ConfigOption(comment = "Vertical spacing between advancement cards")
    public static int screenCardSpacing = 8;

    @ConfigOption(comment = "Bottom margin of the card viewport")
    public static int screenViewportBottomMargin = 18;

    @ConfigOption(comment = "Height of the content header including description")
    public static int screenHeaderHeight = 52;

    @ConfigOption(comment = "Minimum height of the scroll thumb")
    public static int screenMinScrollThumbHeight = 24;

    @ConfigOption(comment = "Width of the scrollbar")
    public static int screenScrollbarWidth = 3;

    @ConfigOption(comment = "Right margin of the scrollbar")
    public static int screenScrollbarRightMargin = 12;

    @ConfigSection("SCREEN COLORS")
    @ConfigOption(comment = "Backdrop fill color behind the whole screen", hex = true)
    public static int screenBackdropColor = 0xD0101010;

    @ConfigOption(comment = "Top bar gradient start color", hex = true)
    public static int screenTopBarGradientStart = 0xCC161616;

    @ConfigOption(comment = "Top bar gradient end color", hex = true)
    public static int screenTopBarGradientEnd = 0xCC121212;

    @ConfigOption(comment = "Top bar border color", hex = true)
    public static int screenTopBarBorder = 0xAA2A2A2A;

    @ConfigOption(comment = "Sidebar gradient start color", hex = true)
    public static int screenSidebarGradientStart = 0xCC111111;

    @ConfigOption(comment = "Sidebar gradient end color", hex = true)
    public static int screenSidebarGradientEnd = 0xCC0A0A0A;

    @ConfigOption(comment = "Sidebar border color", hex = true)
    public static int screenSidebarBorder = 0xAA2A2A2A;

    @ConfigOption(comment = "Fill color for the selected sidebar row", hex = true)
    public static int screenSidebarSelectedFill = 0xAA252525;

    @ConfigOption(comment = "Accent color for the selected sidebar row", hex = true)
    public static int screenSidebarSelectedAccent = 0xFF00FFAA;

    @ConfigOption(comment = "Fill color for a hovered sidebar row", hex = true)
    public static int screenSidebarHoverFill = 0x881C1C1C;

    @ConfigOption(comment = "Text color for the selected sidebar row", hex = true)
    public static int screenSidebarTextSelected = 0xFF00FFAA;

    @ConfigOption(comment = "Text color for idle sidebar rows", hex = true)
    public static int screenSidebarTextIdle = 0xFFAAAAAA;

    @ConfigOption(comment = "Sidebar progress bar gradient start color", hex = true)
    public static int screenProgressBarGradientStart = 0xFF141414;

    @ConfigOption(comment = "Sidebar progress bar gradient end color", hex = true)
    public static int screenProgressBarGradientEnd = 0xFF0D0D0D;

    @ConfigOption(comment = "Sidebar progress bar top border color", hex = true)
    public static int screenProgressBarTopBorder = 0x22FFFFFF;

    @ConfigOption(comment = "Progress track border color", hex = true)
    public static int screenProgressTrackBorder = 0xAA333333;

    @ConfigOption(comment = "Progress track fill color", hex = true)
    public static int screenProgressTrackFill = 0xAA1A1A1A;

    @ConfigOption(comment = "Progress fill gradient start color", hex = true)
    public static int screenProgressFillStart = 0xAA00FFAA;

    @ConfigOption(comment = "Progress fill gradient end color", hex = true)
    public static int screenProgressFillEnd = 0xAA00CC88;

    @ConfigOption(comment = "Progress text color", hex = true)
    public static int screenProgressTextColor = 0xFFE0E0E0;

    @ConfigOption(comment = "Tracked progress fill gradient start color", hex = true)
    public static int screenTrackedFillStart = 0xAAAA00FF;

    @ConfigOption(comment = "Tracked progress fill gradient end color", hex = true)
    public static int screenTrackedFillEnd = 0xAA7700CC;

    @ConfigOption(comment = "Tracked progress percentage text color", hex = true)
    public static int screenTrackedPercentageColor = 0xFFAA00FF;

    @ConfigOption(comment = "Header title text color", hex = true)
    public static int screenHeaderTitleColor = 0xFFFFFFFF;

    @ConfigOption(comment = "Header description text color", hex = true)
    public static int screenHeaderDescriptionColor = 0xFFAAAAAA;

    @ConfigOption(comment = "Header divider line color", hex = true)
    public static int screenHeaderDividerColor = 0x88303030;

    @ConfigOption(comment = "Header progress percentage text color", hex = true)
    public static int screenHeaderPercentageColor = 0xFF00FFAA;

    @ConfigOption(comment = "Scrollbar track color", hex = true)
    public static int screenScrollbarTrackColor = 0xAA1A1A1A;

    @ConfigOption(comment = "Scrollbar thumb color while dragging", hex = true)
    public static int screenScrollbarThumbActive = 0xFF00FFAA;

    @ConfigOption(comment = "Scrollbar thumb color while idle", hex = true)
    public static int screenScrollbarThumbIdle = 0xAA00FFAA;

    @ConfigSection("WIDGETS")
    @ConfigOption(comment = "Widget background color while hovered", hex = true)
    public static int widgetBackgroundHovered = 0xEE1E1E1E;

    @ConfigOption(comment = "Widget background color while idle", hex = true)
    public static int widgetBackgroundIdle = 0xEE121212;

    @ConfigOption(comment = "Widget border color while hovered", hex = true)
    public static int widgetBorderHovered = 0xFF00FFAA;

    @ConfigOption(comment = "Widget border color while idle", hex = true)
    public static int widgetBorderIdle = 0x33FFFFFF;

    @ConfigOption(comment = "Widget text color while hovered", hex = true)
    public static int widgetTextHovered = 0xFF00FFAA;

    @ConfigOption(comment = "Widget text color while idle", hex = true)
    public static int widgetTextIdle = 0xFFE0E0E0;

    @ConfigOption(comment = "Dropdown option list background color", hex = true)
    public static int dropdownOptionBackground = 0xF2161616;

    @ConfigOption(comment = "Dropdown option hover fill color", hex = true)
    public static int dropdownOptionHover = 0xFF1E1E1E;

    @ConfigOption(comment = "Dropdown selected option text color", hex = true)
    public static int dropdownOptionSelectedText = 0xFF00FFAA;

    @ConfigOption(comment = "Height of each dropdown option row")
    public static int dropdownOptionHeight = 16;

    @ConfigOption(comment = "Arrow glyph shown when the dropdown is closed")
    public static String dropdownArrowClosed = "\u25BE";

    @ConfigOption(comment = "Arrow glyph shown when the dropdown is open")
    public static String dropdownArrowOpen = "\u25B4";

    @ConfigSection("SIDEBAR")
    @ConfigOption(comment = "Suffix appended when a sidebar title is truncated")
    public static String sidebarTruncationEllipsis = "...";

    public static void load() {
        ConfigManager.load(LucidConfig.class);
    }

    public static void save() {
        ConfigManager.save(LucidConfig.class);
    }

    public static void updateAndSave(String fieldName, Object value) {
        ConfigManager.updateAndSave(LucidConfig.class, fieldName, value);
    }

    public static void startWatcher() {
        ConfigManager.startWatcher(LucidConfig.class);
    }
}