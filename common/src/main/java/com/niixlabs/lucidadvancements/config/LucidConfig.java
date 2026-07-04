package com.niixlabs.lucidadvancements.config;

public final class LucidConfig {
    private LucidConfig() {}

    @ConfigOption(comment = "Interface scale behavior. 0 = Auto/Vanilla, 1-4 = Fixed Custom Scale Factor")
    public static int customGuiScale = 0;

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

    @ConfigSection("ADVANCEMENT CARDS MENU CONFIGURATION\n# Unobtained Normal Advancements")
    @ConfigOption(comment = "Background color gradient start", hex = true)
    public static int cardNormalBg1 = 0xAA1A1A1A;

    @ConfigOption(comment = "Background color gradient end", hex = true)
    public static int cardNormalBg2 = 0xAA121212;

    @ConfigOption(comment = "Border color", hex = true)
    public static int cardNormalBorder = 0x882A2A2A;

    @ConfigOption(comment = "Title text color", hex = true)
    public static int cardNormalTitle = 0xFFFFFFFF;

    @ConfigSection("Unobtained Challenge/Rare Advancements")
    @ConfigOption(comment = "Border color", hex = true)
    public static int cardRareBorder = 0xAA42245A;

    @ConfigOption(comment = "Title text color", hex = true)
    public static int cardRareTitle = 0xFFB388CC;

    @ConfigSection("Completed Normal Advancements")
    @ConfigOption(comment = "Background color gradient start", hex = true)
    public static int cardObtainedBg1 = 0xBB142E1F;

    @ConfigOption(comment = "Background color gradient end", hex = true)
    public static int cardObtainedBg2 = 0xBB0C1E14;

    @ConfigOption(comment = "Border color", hex = true)
    public static int cardObtainedBorder = 0xAA00FFAA;

    @ConfigOption(comment = "Title text color", hex = true)
    public static int cardObtainedTitle = 0xFF77FF77;

    @ConfigSection("Completed Challenge/Rare Advancements")
    @ConfigOption(comment = "Background color gradient start", hex = true)
    public static int cardObtainedRareBg1 = 0xBB2D1438;

    @ConfigOption(comment = "Background color gradient end", hex = true)
    public static int cardObtainedRareBg2 = 0xBB1E0C26;

    @ConfigOption(comment = "Border color", hex = true)
    public static int cardObtainedRareBorder = 0xAAAA00FF;

    @ConfigOption(comment = "Title text color", hex = true)
    public static int cardObtainedRareTitle = 0xFFFF77FF;

    public static void load() {
        ConfigManager.load(LucidConfig.class);
    }

    public static void save() {
        ConfigManager.save(LucidConfig.class);
    }

    public static void updateAndSave(String fieldName, Object value) {
        ConfigManager.updateAndSave(LucidConfig.class, fieldName, value);
    }
}