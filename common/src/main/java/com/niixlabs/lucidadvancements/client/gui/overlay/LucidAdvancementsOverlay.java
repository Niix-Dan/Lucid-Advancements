package com.niixlabs.lucidadvancements.client.gui.overlay;

import com.niixlabs.lucidadvancements.Constants;
import com.niixlabs.lucidadvancements.client.gui.access.AdvancementProgressAccess;
import com.niixlabs.lucidadvancements.client.gui.util.GuiScale;
import com.niixlabs.lucidadvancements.config.LucidConfig;
import com.niixlabs.lucidadvancements.config.category.CategoryConfigManager;
import com.niixlabs.lucidadvancements.translation.CriterionTranslator;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class LucidAdvancementsOverlay {
    private static final long REFRESH_INTERVAL_MS = 500L;
    private static final int PAGE_SIZE = 4;

    private static final Map<String, CachedOverlayBox> OVERLAY_CACHE = new ConcurrentHashMap<>();
    private static final List<AdvancementNode> TASK_CACHE = new ArrayList<>();
    private static List<Integer> pageStarts = List.of();
    private static Language lastLangInstance = null;
    private static ClientAdvancements lastAdvancements = null;
    private static long lastRefresh = 0L;
    private static int page = 0;
    private static int pageCount = 0;
    private static int layoutScreenHeight = -1;
    private static volatile boolean layoutDirty = true;
    private static boolean visible = true;

    private LucidAdvancementsOverlay() {}

    public static void invalidateCache() {
        OVERLAY_CACHE.clear();
        layoutDirty = true;
    }

    public static void toggleVisible() {
        visible = !visible;
        layoutDirty = true;

        if (!visible) {
            page = 0;
            pageCount = 0;
        }
    }

    public static void nextPage() {
        if (pageCount <= 1) {
            return;
        }
        page = (page + 1) % pageCount;
    }

    public static void render(GuiGraphics guiGraphics, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.player == null) return;
        if (mc.screen == null) {
            LucidNotificationOverlay.render(guiGraphics);
        }

        if (!visible) return;
        if (mc.getConnection() == null || mc.getConnection().getAdvancements() == null) return;

        ClientAdvancements advancements = mc.getConnection().getAdvancements();
        Language currentLangInstance = Language.getInstance();
        boolean languageChanged = currentLangInstance != lastLangInstance;
        boolean advancementsChanged = advancements != lastAdvancements;

        if (languageChanged) {
            OVERLAY_CACHE.clear();
            lastLangInstance = currentLangInstance;
        }

        if (advancementsChanged) {
            page = 0;
            OVERLAY_CACHE.clear();
        }

        long now = System.currentTimeMillis();
        if (advancementsChanged || languageChanged || now - lastRefresh >= REFRESH_INTERVAL_MS) {
            rebuildTaskCache(advancements);
            lastAdvancements = advancements;
            lastRefresh = now;
        }

        if (TASK_CACHE.isEmpty()) {
            page = 0;
            pageCount = 0;
            return;
        }

        double targetScale = GuiScale.targetScale(mc);
        float scaleMod = GuiScale.scaleModifier(mc);

        int screenWidth = (int) Math.ceil(mc.getWindow().getScreenWidth() / targetScale);
        int screenHeight = (int) Math.ceil(mc.getWindow().getScreenHeight() / targetScale);

        Map<AdvancementHolder, AdvancementProgress> vanillaProgressMap =
                ((AdvancementProgressAccess) advancements).lucid$getProgressMap();

        if (layoutDirty || screenHeight != layoutScreenHeight) {
            pageStarts = computePageStarts(vanillaProgressMap, screenHeight);
            pageCount = pageStarts.size();
            layoutDirty = false;
            layoutScreenHeight = screenHeight;

            if (pageCount == 0) {
                page = 0;
            } else if (page >= pageCount) {
                page = pageCount - 1;
            }
        }

        if (pageCount == 0) {
            return;
        }

        Font font = mc.font;
        int startX = screenWidth - LucidConfig.overlayBoxWidth - LucidConfig.overlayRightMargin;
        int startY = LucidConfig.overlayStartY;

        guiGraphics.pose().pushPose();

        if (scaleMod != 1.0f) {
            guiGraphics.pose().scale(scaleMod, scaleMod, 1.0f);
        }

        String pageText = (page + 1) + "/" + pageCount;
        int pageTextY = Math.max(2, startY - 10);
        guiGraphics.drawString(font, pageText, startX + LucidConfig.overlayBoxWidth - font.width(pageText), pageTextY, LucidConfig.overlayCriterionTextColor, true);

        int startIndex = pageStarts.get(page);
        int endIndex = page + 1 < pageCount ? pageStarts.get(page + 1) : TASK_CACHE.size();
        int maxBoxHeight = screenHeight - LucidConfig.overlayBottomScreenMargin - LucidConfig.overlayStartY;

        for (int index = startIndex; index < endIndex; index++) {
            AdvancementNode node = TASK_CACHE.get(index);
            DisplayInfo display = node.holder().value().display().orElse(null);
            if (display == null) continue;

            AdvancementProgress progress = vanillaProgressMap.get(node.holder());
            if (progress == null || progress.isDone()) continue;

            ResourceLocation loc = node.holder().id();
            String idStr = loc.toString();
            List<String> rawRemaining = remainingCriteria(progress);

            CachedOverlayBox cachedBox = OVERLAY_CACHE.get(idStr);
            if (cachedBox == null || !cachedBox.rawRemaining().equals(rawRemaining)) {
                cachedBox = buildOverlayBox(font, loc, display, rawRemaining);
                OVERLAY_CACHE.put(idStr, cachedBox);
            }

            if (cachedBox.boxHeight() > maxBoxHeight) {
                continue;
            }

            renderBox(guiGraphics, font, display, cachedBox, rawRemaining, startX, startY);

            startY += cachedBox.boxHeight() + LucidConfig.overlayBoxSpacing;
        }

        guiGraphics.pose().popPose();
    }

    private static void rebuildTaskCache(ClientAdvancements advancements) {
        TASK_CACHE.clear();

        Map<AdvancementHolder, AdvancementProgress> progressMap =
                ((AdvancementProgressAccess) advancements).lucid$getProgressMap();
        Map<ResourceLocation, Boolean> rootEnabled = new HashMap<>();
        Set<String> validIds = new HashSet<>();

        for (AdvancementNode node : advancements.getTree().nodes()) {
            if (node.parent() == null) continue;

            DisplayInfo display = node.holder().value().display().orElse(null);
            if (display == null || display.isHidden()) continue;

            ResourceLocation rootId = node.root().holder().id();
            if (!rootEnabled.computeIfAbsent(rootId, CategoryConfigManager::isEnabled)) continue;

            AdvancementProgress progress = progressMap.get(node.holder());
            if (progress == null || progress.isDone()) continue;

            TASK_CACHE.add(node);
            validIds.add(node.holder().id().toString());
        }

        if (TASK_CACHE.isEmpty()) {
            page = 0;
            pageCount = 0;
        }

        layoutDirty = true;
        OVERLAY_CACHE.keySet().retainAll(validIds);
    }

    private static List<Integer> computePageStarts(Map<AdvancementHolder, AdvancementProgress> progressMap,
                                                   int screenHeight) {
        List<Integer> starts = new ArrayList<>();
        int limit = screenHeight - LucidConfig.overlayBottomScreenMargin;
        int maxBoxHeight = limit - LucidConfig.overlayStartY;
        int startY = LucidConfig.overlayStartY;
        int pageStart = 0;
        int itemsOnPage = 0;
        boolean hasEntry = false;

        for (int i = 0; i < TASK_CACHE.size(); i++) {
            AdvancementProgress progress = progressMap.get(TASK_CACHE.get(i).holder());
            if (progress == null || progress.isDone()) {
                continue;
            }

            int remainingCount = 0;
            for (String ignored : progress.getRemainingCriteria()) {
                remainingCount++;
            }

            int boxHeight = boxHeightForRemaining(remainingCount);
            if (boxHeight > maxBoxHeight) {
                continue;
            }

            boolean pageFull = itemsOnPage >= PAGE_SIZE;
            boolean doesNotFit = itemsOnPage > 0 && startY + boxHeight > limit;

            if (i > pageStart && (pageFull || doesNotFit)) {
                starts.add(pageStart);
                pageStart = i;
                startY = LucidConfig.overlayStartY;
                itemsOnPage = 0;
            }

            hasEntry = true;
            startY += boxHeight + LucidConfig.overlayBoxSpacing;
            itemsOnPage++;
        }

        if (hasEntry) {
            starts.add(pageStart);
        }

        return starts;
    }

    private static List<String> remainingCriteria(AdvancementProgress progress) {
        List<String> criteria = new ArrayList<>();
        for (String criterion : progress.getRemainingCriteria()) {
            criteria.add(criterion);
        }
        return criteria;
    }

    private static int boxHeightForRemaining(int remainingCount) {
        int maxVisibleCriteria = Math.min(LucidConfig.overlayMaxVisibleCriteria, remainingCount);
        int boxHeight = LucidConfig.overlayBaseBoxHeight
                + ((remainingCount > LucidConfig.overlayMaxVisibleCriteria ? maxVisibleCriteria + 1 : maxVisibleCriteria) * LucidConfig.overlayLineHeight);
        if (maxVisibleCriteria > 0) {
            boxHeight += LucidConfig.overlayOverflowLinePadding;
        }
        return boxHeight;
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

        int boxHeight = boxHeightForRemaining(rawRemaining.size());

        return new CachedOverlayBox(rawRemaining, titleText, critLines, boxHeight, titleColor);
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
