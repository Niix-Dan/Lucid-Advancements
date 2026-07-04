package com.niixlabs.lucidadvancements.client.gui.overlay;

import com.niixlabs.lucidadvancements.Constants;
import com.niixlabs.lucidadvancements.client.gui.access.AdvancementProgressAccess;
import com.niixlabs.lucidadvancements.client.gui.screen.GuiScale;
import com.niixlabs.lucidadvancements.client.gui.screen.LucidAdvancementsScreen;
import com.niixlabs.lucidadvancements.config.LucidConfig;
import com.niixlabs.lucidadvancements.translation.CriterionTranslator;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class LucidAdvancementsOverlay {
    private static final String ICON_LOCKED = "🔒";

    private static final int BOX_WIDTH = 135;
    private static final int RIGHT_MARGIN = 8;
    private static final int START_Y = 55;
    private static final int BOX_SPACING = 5;
    private static final int BOTTOM_SCREEN_MARGIN = 35;

    private static final int BASE_BOX_HEIGHT = 24;
    private static final int LINE_HEIGHT = 10;
    private static final int OVERFLOW_LINE_PADDING = 2;
    private static final int MAX_VISIBLE_CRITERIA = 3;

    private static final int ACCENT_WIDTH = 2;
    private static final int ICON_X_OFFSET = 6;
    private static final int ICON_Y_OFFSET = 4;
    private static final int TITLE_X_OFFSET = 26;
    private static final int TITLE_Y_OFFSET = 5;
    private static final int TITLE_MAX_WIDTH_PADDING = 32;
    private static final int CRITERIA_START_Y_OFFSET = 22;
    private static final int CRITERION_X_OFFSET = 8;
    private static final int CRITERION_MAX_WIDTH_PADDING = 10;

    private static final int COLOR_CRITERION_TEXT = 0xFFAAAAAA;
    private static final int COLOR_HIDDEN_COUNT_TEXT = 0xFF555555;

    private static final Map<String, CachedOverlayBox> OVERLAY_CACHE = new HashMap<>();
    private static String lastLang = "";

    private LucidAdvancementsOverlay() {}

    public static void render(GuiGraphics guiGraphics, float partialTick) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.options.hideGui || mc.player == null || LucidAdvancementsScreen.TRACKED_ADVANCEMENTS.isEmpty()) return;
        if (mc.getConnection() == null || mc.getConnection().getAdvancements() == null) return;

        String currentLang = mc.getLanguageManager().getSelected();
        if (!currentLang.equals(lastLang)) {
            OVERLAY_CACHE.clear();
            lastLang = currentLang;
        }

        double targetScale = GuiScale.targetScale(mc);
        float scaleMod = GuiScale.scaleModifier(mc);

        int screenWidth = (int) (mc.getWindow().getScreenWidth() / targetScale);
        int screenHeight = (int) (mc.getWindow().getScreenHeight() / targetScale);

        Font font = mc.font;
        int startX = screenWidth - BOX_WIDTH - RIGHT_MARGIN;
        int startY = START_Y;

        Map<AdvancementHolder, AdvancementProgress> vanillaProgressMap = ((AdvancementProgressAccess) mc.getConnection().getAdvancements()).lucid$getProgressMap();

        guiGraphics.pose().pushPose();

        if (scaleMod != 1.0f) {
            guiGraphics.pose().scale(scaleMod, scaleMod, 1.0f);
        }

        for (String idStr : LucidAdvancementsScreen.TRACKED_ADVANCEMENTS) {
            ResourceLocation loc = ResourceLocation.tryParse(idStr);
            if (loc == null) continue;

            AdvancementNode node = mc.getConnection().getAdvancements().getTree().get(loc);
            if (node == null || node.holder().value().display().isEmpty()) continue;

            DisplayInfo display = node.holder().value().display().get();
            AdvancementProgress progress = vanillaProgressMap.get(node.holder());

            if (progress == null || progress.isDone()) {
                continue;
            }

            List<String> rawRemaining = (List<String>) progress.getRemainingCriteria();

            CachedOverlayBox cachedBox = OVERLAY_CACHE.get(idStr);
            if (cachedBox == null || !cachedBox.rawRemaining().equals(rawRemaining)) {
                cachedBox = buildOverlayBox(font, loc, display, rawRemaining);
                OVERLAY_CACHE.put(idStr, cachedBox);
            }

            renderBox(guiGraphics, font, display, cachedBox, rawRemaining, startX, startY);

            startY += cachedBox.boxHeight() + BOX_SPACING;

            if (startY > screenHeight - BOTTOM_SCREEN_MARGIN) {
                break;
            }
        }

        guiGraphics.pose().popPose();
    }

    private static CachedOverlayBox buildOverlayBox(Font font, ResourceLocation loc, DisplayInfo display, List<String> rawRemaining) {
        boolean isChallenge = display.getType() == AdvancementType.CHALLENGE;
        int titleColor = isChallenge ? LucidConfig.overlayChallengeTitleColor : LucidConfig.overlayTitleColor;
        String titleText = font.plainSubstrByWidth(display.getTitle().getString(), BOX_WIDTH - TITLE_MAX_WIDTH_PADDING);

        List<String> critLines = new ArrayList<>();
        int maxVisibleCriteria = Math.min(MAX_VISIBLE_CRITERIA, rawRemaining.size());

        for (int i = 0; i < maxVisibleCriteria; i++) {
            String resolved = CriterionTranslator.resolve(loc, rawRemaining.get(i));
            String critLine = font.plainSubstrByWidth(ICON_LOCKED + " " + resolved, BOX_WIDTH - CRITERION_MAX_WIDTH_PADDING);
            critLines.add(critLine);
        }

        int boxHeight = BASE_BOX_HEIGHT + ((rawRemaining.size() > MAX_VISIBLE_CRITERIA ? maxVisibleCriteria + 1 : maxVisibleCriteria) * LINE_HEIGHT);
        if (maxVisibleCriteria > 0) boxHeight += OVERFLOW_LINE_PADDING;

        return new CachedOverlayBox(new ArrayList<>(rawRemaining), titleText, critLines, boxHeight, titleColor);
    }

    private static void renderBox(GuiGraphics guiGraphics, Font font, DisplayInfo display, CachedOverlayBox cachedBox, List<String> rawRemaining, int startX, int startY) {
        guiGraphics.fill(startX, startY, startX + BOX_WIDTH, startY + cachedBox.boxHeight(), LucidConfig.overlayBgColor);

        boolean isChallenge = display.getType() == AdvancementType.CHALLENGE;
        int accentColor = isChallenge ? LucidConfig.overlayChallengeAccent : LucidConfig.overlayNormalAccent;
        guiGraphics.fill(startX, startY, startX + ACCENT_WIDTH, startY + cachedBox.boxHeight(), accentColor);

        ItemStack iconStack = display.getIcon();
        guiGraphics.pose().pushPose();
        guiGraphics.renderItem(iconStack, startX + ICON_X_OFFSET, startY + ICON_Y_OFFSET);
        guiGraphics.pose().popPose();

        guiGraphics.drawString(font, cachedBox.titleText(), startX + TITLE_X_OFFSET, startY + TITLE_Y_OFFSET, cachedBox.titleColor(), true);

        int textY = startY + CRITERIA_START_Y_OFFSET;
        for (String line : cachedBox.critLines()) {
            guiGraphics.drawString(font, line, startX + CRITERION_X_OFFSET, textY, COLOR_CRITERION_TEXT, true);
            textY += LINE_HEIGHT;
        }

        if (rawRemaining.size() > MAX_VISIBLE_CRITERIA) {
            int hiddenCount = rawRemaining.size() - MAX_VISIBLE_CRITERIA;
            guiGraphics.drawString(font, Component.translatable(Constants.MOD_ID + ".overlay.hidden_count", hiddenCount), startX + CRITERION_X_OFFSET, textY, COLOR_HIDDEN_COUNT_TEXT, true);
        }
    }
}