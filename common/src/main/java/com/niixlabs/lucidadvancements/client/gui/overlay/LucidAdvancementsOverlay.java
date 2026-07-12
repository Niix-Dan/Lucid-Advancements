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
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class LucidAdvancementsOverlay {
    private static final Map<String, CachedOverlayBox> OVERLAY_CACHE = new HashMap<>();
    private static Language lastLangInstance = null;

    private LucidAdvancementsOverlay() {}

    public static void render(GuiGraphics guiGraphics, float partialTick) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.options.hideGui || mc.player == null || LucidAdvancementsScreen.TRACKED_ADVANCEMENTS.isEmpty()) return;
        if (mc.getConnection() == null || mc.getConnection().getAdvancements() == null) return;

        Language currentLangInstance = Language.getInstance();
        if (currentLangInstance != lastLangInstance) {
            OVERLAY_CACHE.clear();
            lastLangInstance = currentLangInstance;
        }

        double targetScale = GuiScale.targetScale(mc);
        float scaleMod = GuiScale.scaleModifier(mc);

        int screenWidth = (int) (mc.getWindow().getScreenWidth() / targetScale);
        int screenHeight = (int) (mc.getWindow().getScreenHeight() / targetScale);

        Font font = mc.font;
        int startX = screenWidth - LucidConfig.overlayBoxWidth - LucidConfig.overlayRightMargin;
        int startY = LucidConfig.overlayStartY;

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

            startY += cachedBox.boxHeight() + LucidConfig.overlayBoxSpacing;

            if (startY > screenHeight - LucidConfig.overlayBottomScreenMargin) {
                break;
            }
        }

        guiGraphics.pose().popPose();
    }

    private static CachedOverlayBox buildOverlayBox(Font font, ResourceLocation loc, DisplayInfo display, List<String> rawRemaining) {
        boolean isChallenge = display.getType() == AdvancementType.CHALLENGE;
        int titleColor = isChallenge ? LucidConfig.overlayChallengeTitleColor : LucidConfig.overlayTitleColor;
        String titleText = font.plainSubstrByWidth(display.getTitle().getString(), LucidConfig.overlayBoxWidth - LucidConfig.overlayTitleMaxWidthPadding);

        List<String> critLines = new ArrayList<>();
        int maxVisibleCriteria = Math.min(LucidConfig.overlayMaxVisibleCriteria, rawRemaining.size());

        for (int i = 0; i < maxVisibleCriteria; i++) {
            String resolved = CriterionTranslator.resolve(loc, rawRemaining.get(i));
            String critLine = font.plainSubstrByWidth(LucidConfig.overlayIconLocked + " " + resolved, LucidConfig.overlayBoxWidth - LucidConfig.overlayCriterionMaxWidthPadding);
            critLines.add(critLine);
        }

        int boxHeight = LucidConfig.overlayBaseBoxHeight + ((rawRemaining.size() > LucidConfig.overlayMaxVisibleCriteria ? maxVisibleCriteria + 1 : maxVisibleCriteria) * LucidConfig.overlayLineHeight);
        if (maxVisibleCriteria > 0) boxHeight += LucidConfig.overlayOverflowLinePadding;

        return new CachedOverlayBox(new ArrayList<>(rawRemaining), titleText, critLines, boxHeight, titleColor);
    }

    private static void renderBox(GuiGraphics guiGraphics, Font font, DisplayInfo display, CachedOverlayBox cachedBox, List<String> rawRemaining, int startX, int startY) {
        guiGraphics.fill(startX, startY, startX + LucidConfig.overlayBoxWidth, startY + cachedBox.boxHeight(), LucidConfig.overlayBgColor);

        boolean isChallenge = display.getType() == AdvancementType.CHALLENGE;
        int accentColor = isChallenge ? LucidConfig.overlayChallengeAccent : LucidConfig.overlayNormalAccent;
        guiGraphics.fill(startX, startY, startX + LucidConfig.overlayAccentWidth, startY + cachedBox.boxHeight(), accentColor);

        ItemStack iconStack = display.getIcon();
        guiGraphics.pose().pushPose();
        guiGraphics.renderItem(iconStack, startX + LucidConfig.overlayIconXOffset, startY + LucidConfig.overlayIconYOffset);
        guiGraphics.pose().popPose();

        guiGraphics.drawString(font, cachedBox.titleText(), startX + LucidConfig.overlayTitleXOffset, startY + LucidConfig.overlayTitleYOffset, cachedBox.titleColor(), true);

        int textY = startY + LucidConfig.overlayCriteriaStartYOffset;
        for (String line : cachedBox.critLines()) {
            guiGraphics.drawString(font, line, startX + LucidConfig.overlayCriterionXOffset, textY, LucidConfig.overlayCriterionTextColor, true);
            textY += LucidConfig.overlayLineHeight;
        }

        if (rawRemaining.size() > LucidConfig.overlayMaxVisibleCriteria) {
            int hiddenCount = rawRemaining.size() - LucidConfig.overlayMaxVisibleCriteria;
            guiGraphics.drawString(font, Component.translatable(Constants.MOD_ID + ".overlay.hidden_count", hiddenCount), startX + LucidConfig.overlayCriterionXOffset, textY, LucidConfig.overlayHiddenCountTextColor, true);
        }
    }
}