package com.niixlabs.lucidadvancements.config;

public final class LucidConfig {
    private LucidConfig() {}

    @ConfigOption(comment = "GUI scale. 0 = Auto, 1-8 = Fixed Scale")
    public static int customGuiScale = 0;

    @ConfigOption(comment = "Min virtual width for auto-scaling")
    public static double scaleMinVirtualWidth = 550.0;

    @ConfigOption(comment = "Min virtual height for auto-scaling")
    public static double scaleMinVirtualHeight = 300.0;

    @ConfigOption(comment = "Keep search query across menu reopens")
    public static boolean keepSearchQuery = true;

    @ConfigOption(comment = "Config watcher (Needs Restart)")
    public static boolean useConfigWatcher = false;

    @ConfigSection("HUD OVERLAY")
    @ConfigOption(comment = "Background color for pinned boxes", hex = true)
    public static int overlayBgColor = 0x880A0A0A;

    @ConfigOption(comment = "Accent color for normal pinned advancements", hex = true)
    public static int overlayNormalAccent = 0xFF00FFAA;

    @ConfigOption(comment = "Accent color for challenge pinned advancements", hex = true)
    public static int overlayChallengeAccent = 0xFFAA00FF;

    @ConfigOption(comment = "Title color for normal advancements", hex = true)
    public static int overlayTitleColor = 0xFFFFFFFF;

    @ConfigOption(comment = "Title color for challenge advancements", hex = true)
    public static int overlayChallengeTitleColor = 0xFFFF77FF;

    @ConfigOption(comment = "Text color for remaining criteria", hex = true)
    public static int overlayCriterionTextColor = 0xFFAAAAAA;

    @ConfigOption(comment = "Text color for the hidden criteria counter", hex = true)
    public static int overlayHiddenCountTextColor = 0xFF555555;

    @ConfigOption(comment = "Icon for locked criteria")
    public static String overlayIconLocked = "\uD83D\uDD12";

    @ConfigOption(comment = "Width of pinned boxes")
    public static int overlayBoxWidth = 135;

    @ConfigOption(comment = "Right screen margin for pinned boxes")
    public static int overlayRightMargin = 8;

    @ConfigOption(comment = "Starting Y position for the first box")
    public static int overlayStartY = 55;

    @ConfigOption(comment = "Vertical spacing between boxes")
    public static int overlayBoxSpacing = 5;

    @ConfigOption(comment = "Bottom screen margin (stops rendering below this)")
    public static int overlayBottomScreenMargin = 35;

    @ConfigOption(comment = "Base height for boxes without criteria")
    public static int overlayBaseBoxHeight = 24;

    @ConfigOption(comment = "Line height for criteria text")
    public static int overlayLineHeight = 10;

    @ConfigOption(comment = "Extra padding when criteria are shown")
    public static int overlayOverflowLinePadding = 2;

    @ConfigOption(comment = "Max visible criteria per box")
    public static int overlayMaxVisibleCriteria = 3;

    @ConfigOption(comment = "Width of the colored accent strip")
    public static int overlayAccentWidth = 2;

    @ConfigOption(comment = "Icon X offset")
    public static int overlayIconXOffset = 6;

    @ConfigOption(comment = "Icon Y offset")
    public static int overlayIconYOffset = 4;

    @ConfigOption(comment = "Title X offset")
    public static int overlayTitleXOffset = 26;

    @ConfigOption(comment = "Title Y offset")
    public static int overlayTitleYOffset = 5;

    @ConfigOption(comment = "Padding subtracted from width to truncate titles")
    public static int overlayTitleMaxWidthPadding = 32;

    @ConfigOption(comment = "Y offset for the first criteria line")
    public static int overlayCriteriaStartYOffset = 22;

    @ConfigOption(comment = "Criteria X offset")
    public static int overlayCriterionXOffset = 8;

    @ConfigOption(comment = "Padding subtracted from width to truncate criteria")
    public static int overlayCriterionMaxWidthPadding = 10;

    @ConfigSection("LOCKED NORMAL")
    @ConfigOption(comment = "Background gradient start", hex = true)
    public static int cardNormalBg1 = 0xAA1A1A1A;

    @ConfigOption(comment = "Background gradient end", hex = true)
    public static int cardNormalBg2 = 0xAA121212;

    @ConfigOption(comment = "Border color", hex = true)
    public static int cardNormalBorder = 0x882A2A2A;

    @ConfigOption(comment = "Title text color", hex = true)
    public static int cardNormalTitle = 0xFFFFFFFF;

    @ConfigSection("LOCKED CHALLENGE")
    @ConfigOption(comment = "Border color", hex = true)
    public static int cardRareBorder = 0xAA440077;

    @ConfigOption(comment = "Title text color", hex = true)
    public static int cardRareTitle = 0xFFCC77FF;

    @ConfigSection("COMPLETED NORMAL")
    @ConfigOption(comment = "Background gradient start", hex = true)
    public static int cardObtainedBg1 = 0xBB142E1F;

    @ConfigOption(comment = "Background gradient end", hex = true)
    public static int cardObtainedBg2 = 0xBB0C1E14;

    @ConfigOption(comment = "Border color", hex = true)
    public static int cardObtainedBorder = 0xAA00FFAA;

    @ConfigOption(comment = "Title text color", hex = true)
    public static int cardObtainedTitle = 0xFF77FFAA;

    @ConfigSection("COMPLETED CHALLENGE")
    @ConfigOption(comment = "Background gradient start", hex = true)
    public static int cardObtainedRareBg1 = 0xBB2D1438;

    @ConfigOption(comment = "Background gradient end", hex = true)
    public static int cardObtainedRareBg2 = 0xBB1E0C26;

    @ConfigOption(comment = "Border color", hex = true)
    public static int cardObtainedRareBorder = 0xAAAA00FF;

    @ConfigOption(comment = "Title text color", hex = true)
    public static int cardObtainedRareTitle = 0xFFFF77FF;

    @ConfigSection("CARD COLORS")
    @ConfigOption(comment = "Accent color for tracked cards", hex = true)
    public static int cardTrackedActiveColor = 0xFF00FFAA;

    @ConfigOption(comment = "Color for inactive icons and text", hex = true)
    public static int cardInactiveColor = 0xFF555555;

    @ConfigOption(comment = "Color for hidden advancement '?' marks", hex = true)
    public static int cardHiddenMarkColor = 0xFF666666;

    @ConfigOption(comment = "Description text color", hex = true)
    public static int cardDescriptionColor = 0xFFAAAAAA;

    @ConfigOption(comment = "Requirements label color", hex = true)
    public static int cardRequirementsLabelColor = 0xFF888888;

    @ConfigOption(comment = "Completed criteria text color", hex = true)
    public static int cardCriterionDoneColor = 0xFF00CC88;

    @ConfigOption(comment = "Pending criteria text color", hex = true)
    public static int cardCriterionPendingColor = 0xFF777777;

    @ConfigOption(comment = "Divider line color", hex = true)
    public static int cardDividerColor = 0x44FFFFFF;

    @ConfigOption(comment = "Hover overlay color", hex = true)
    public static int cardHoverOverlayColor = 0x20FFFFFF;

    @ConfigOption(comment = "Icon for locked criteria")
    public static String cardIconLocked = "\uD83D\uDD12";

    @ConfigOption(comment = "Icon for completed criteria/advancements")
    public static String cardIconDone = "\u2714";

    @ConfigOption(comment = "Icon for the track toggle")
    public static String cardIconTracked = "\u2726";

    @ConfigOption(comment = "Text replacing hidden titles/descriptions")
    public static String cardHiddenLabel = "???";

    @ConfigSection("CARD LAYOUT")
    @ConfigOption(comment = "Minimum card height")
    public static int cardBaseHeightMin = 46;

    @ConfigOption(comment = "Vertical padding per description line")
    public static int cardBaseHeightPadding = 28;

    @ConfigOption(comment = "Line height for description and criteria")
    public static int cardLineHeight = 10;

    @ConfigOption(comment = "Extra height for expanded headers")
    public static int cardExpandedHeaderHeight = 20;

    @ConfigOption(comment = "Padding around the criteria section")
    public static int cardCriteriaSectionPadding = 12;

    @ConfigOption(comment = "Text X offset (title and description)")
    public static int cardTextXOffset = 40;

    @ConfigOption(comment = "Item icon X offset")
    public static int cardIconXOffset = 12;

    @ConfigOption(comment = "Track icon X offset (from right edge)")
    public static int cardTrackIconXOffset = 20;

    @ConfigOption(comment = "Status icon X offset (from right edge)")
    public static int cardStatusIconXOffset = 30;

    @ConfigSection("SCREEN LAYOUT")
    @ConfigOption(comment = "Sidebar width")
    public static int screenSidebarWidth = 100;

    @ConfigOption(comment = "Top bar height")
    public static int screenTopBarHeight = 48;

    @ConfigOption(comment = "Sidebar row height")
    public static int screenSidebarRowHeight = 32;

    @ConfigOption(comment = "Sidebar item highlight height")
    public static int screenSidebarItemHeight = 28;

    @ConfigOption(comment = "Sidebar progress bar height")
    public static int screenSidebarProgressHeight = 38;

    @ConfigOption(comment = "Top padding before the first sidebar row")
    public static int screenSidebarTopPadding = 12;

    @ConfigOption(comment = "Margin around main content area")
    public static int screenContentMargin = 18;

    @ConfigOption(comment = "Vertical spacing between cards")
    public static int screenCardSpacing = 8;

    @ConfigOption(comment = "Bottom margin of the card viewport")
    public static int screenViewportBottomMargin = 18;

    @ConfigOption(comment = "Content header height (including description)")
    public static int screenHeaderHeight = 52;

    @ConfigOption(comment = "Minimum scroll thumb height")
    public static int screenMinScrollThumbHeight = 24;

    @ConfigOption(comment = "Scrollbar width")
    public static int screenScrollbarWidth = 3;

    @ConfigOption(comment = "Scrollbar right margin")
    public static int screenScrollbarRightMargin = 12;

    @ConfigSection("SCREEN COLORS")
    @ConfigOption(comment = "Screen backdrop color", hex = true)
    public static int screenBackdropColor = 0xD0101010;

    @ConfigOption(comment = "Top bar gradient start", hex = true)
    public static int screenTopBarGradientStart = 0xCC161616;

    @ConfigOption(comment = "Top bar gradient end", hex = true)
    public static int screenTopBarGradientEnd = 0xCC121212;

    @ConfigOption(comment = "Top bar border color", hex = true)
    public static int screenTopBarBorder = 0xAA2A2A2A;

    @ConfigOption(comment = "Sidebar gradient start", hex = true)
    public static int screenSidebarGradientStart = 0xCC111111;

    @ConfigOption(comment = "Sidebar gradient end", hex = true)
    public static int screenSidebarGradientEnd = 0xCC0A0A0A;

    @ConfigOption(comment = "Sidebar border color", hex = true)
    public static int screenSidebarBorder = 0xAA2A2A2A;

    @ConfigOption(comment = "Selected sidebar row fill color", hex = true)
    public static int screenSidebarSelectedFill = 0xAA252525;

    @ConfigOption(comment = "Selected sidebar row accent color", hex = true)
    public static int screenSidebarSelectedAccent = 0xFF00FFAA;

    @ConfigOption(comment = "Hovered sidebar row fill color", hex = true)
    public static int screenSidebarHoverFill = 0x881C1C1C;

    @ConfigOption(comment = "Selected sidebar text color", hex = true)
    public static int screenSidebarTextSelected = 0xFF00FFAA;

    @ConfigOption(comment = "Idle sidebar text color", hex = true)
    public static int screenSidebarTextIdle = 0xFFAAAAAA;

    @ConfigOption(comment = "Sidebar progress bar gradient start", hex = true)
    public static int screenProgressBarGradientStart = 0xFF141414;

    @ConfigOption(comment = "Sidebar progress bar gradient end", hex = true)
    public static int screenProgressBarGradientEnd = 0xFF0D0D0D;

    @ConfigOption(comment = "Sidebar progress bar top border", hex = true)
    public static int screenProgressBarTopBorder = 0x22FFFFFF;

    @ConfigOption(comment = "Progress track border color", hex = true)
    public static int screenProgressTrackBorder = 0xAA333333;

    @ConfigOption(comment = "Progress track fill color", hex = true)
    public static int screenProgressTrackFill = 0xAA1A1A1A;

    @ConfigOption(comment = "Progress fill gradient start", hex = true)
    public static int screenProgressFillStart = 0xAA00FFAA;

    @ConfigOption(comment = "Progress fill gradient end", hex = true)
    public static int screenProgressFillEnd = 0xAA00CC88;

    @ConfigOption(comment = "Progress text color", hex = true)
    public static int screenProgressTextColor = 0xFFE0E0E0;

    @ConfigOption(comment = "Tracked progress fill gradient start", hex = true)
    public static int screenTrackedFillStart = 0xAAAA00FF;

    @ConfigOption(comment = "Tracked progress fill gradient end", hex = true)
    public static int screenTrackedFillEnd = 0xAA7700CC;

    @ConfigOption(comment = "Tracked progress percentage color", hex = true)
    public static int screenTrackedPercentageColor = 0xFFAA00FF;

    @ConfigOption(comment = "Header title color", hex = true)
    public static int screenHeaderTitleColor = 0xFFFFFFFF;

    @ConfigOption(comment = "Header description color", hex = true)
    public static int screenHeaderDescriptionColor = 0xFFAAAAAA;

    @ConfigOption(comment = "Header divider color", hex = true)
    public static int screenHeaderDividerColor = 0x88303030;

    @ConfigOption(comment = "Header progress percentage color", hex = true)
    public static int screenHeaderPercentageColor = 0xFF00FFAA;

    @ConfigOption(comment = "Scrollbar track color", hex = true)
    public static int screenScrollbarTrackColor = 0xAA1A1A1A;

    @ConfigOption(comment = "Active scrollbar thumb color", hex = true)
    public static int screenScrollbarThumbActive = 0xFF00FFAA;

    @ConfigOption(comment = "Idle scrollbar thumb color", hex = true)
    public static int screenScrollbarThumbIdle = 0xAA00FFAA;

    @ConfigSection("WIDGETS")
    @ConfigOption(comment = "Hovered widget background", hex = true)
    public static int widgetBackgroundHovered = 0xEE1E1E1E;

    @ConfigOption(comment = "Idle widget background", hex = true)
    public static int widgetBackgroundIdle = 0xEE121212;

    @ConfigOption(comment = "Hovered widget border", hex = true)
    public static int widgetBorderHovered = 0xFF00FFAA;

    @ConfigOption(comment = "Idle widget border", hex = true)
    public static int widgetBorderIdle = 0x33FFFFFF;

    @ConfigOption(comment = "Hovered widget text", hex = true)
    public static int widgetTextHovered = 0xFF00FFAA;

    @ConfigOption(comment = "Idle widget text", hex = true)
    public static int widgetTextIdle = 0xFFE0E0E0;

    @ConfigOption(comment = "Dropdown list background", hex = true)
    public static int dropdownOptionBackground = 0xF2161616;

    @ConfigOption(comment = "Dropdown option hover fill", hex = true)
    public static int dropdownOptionHover = 0xFF1E1E1E;

    @ConfigOption(comment = "Selected dropdown option text", hex = true)
    public static int dropdownOptionSelectedText = 0xFF00FFAA;

    @ConfigOption(comment = "Dropdown option row height")
    public static int dropdownOptionHeight = 16;

    @ConfigOption(comment = "Closed dropdown arrow glyph")
    public static String dropdownArrowClosed = "\u25BE";

    @ConfigOption(comment = "Open dropdown arrow glyph")
    public static String dropdownArrowOpen = "\u25B4";

    @ConfigSection("SIDEBAR")
    @ConfigOption(comment = "Suffix for truncated sidebar titles")
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