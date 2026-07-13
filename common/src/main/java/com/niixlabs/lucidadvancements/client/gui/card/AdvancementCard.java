package com.niixlabs.lucidadvancements.client.gui.card;

import com.niixlabs.lucidadvancements.Constants;
import com.niixlabs.lucidadvancements.config.LucidConfig;
import com.niixlabs.lucidadvancements.translation.CriterionTranslator;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class AdvancementCard implements Comparable<AdvancementCard> {
    private final AdvancementNode node;
    private final DisplayInfo display;
    private final boolean done;
    private final boolean hidden;
    private final boolean rare;
    private final AdvancementState state;
    private final float progressRatio;
    private final List<FormattedCharSequence> wrappedDescription;
    private final List<CriterionEntry> criteria = new ArrayList<>();

    private final boolean expanded;
    private final boolean tracked;
    private final int baseHeight;
    private final int totalHeight;
    private final Component title;

    public final String cachedSearchTitle;
    public final String cachedSearchDesc;
    public final String cachedSearchCategory;

    public AdvancementCard(AdvancementNode node, DisplayInfo display, @Nullable AdvancementProgress progress,
                           boolean expanded, boolean tracked, Font font, int maxWidth) {
        this.node = node;
        this.display = display;
        this.expanded = expanded;
        this.done = progress != null && progress.isDone();
        this.tracked = tracked;
        this.hidden = display.isHidden() && !this.done;
        this.rare = display.getType() == AdvancementType.CHALLENGE;
        this.state = resolveState(this.done, this.rare);
        this.progressRatio = progress != null ? progress.getPercent() : 0f;

        this.title = this.hidden ? Component.literal(LucidConfig.cardHiddenLabel) : display.getTitle();
        Component description = this.hidden ? Component.literal(LucidConfig.cardHiddenLabel) : display.getDescription();

        this.cachedSearchTitle = this.hidden ? "" : display.getTitle().getString().toLowerCase();
        this.cachedSearchDesc = this.hidden ? "" : display.getDescription().getString().toLowerCase();

        String catStr = "";
        if (node.root() != null && node.root().holder().value().display().isPresent()) {
            catStr = node.root().holder().value().display().get().getTitle().getString().toLowerCase();
        }
        this.cachedSearchCategory = catStr;

        this.wrappedDescription = font.split(description, maxWidth - 60);
        this.baseHeight = Math.max(LucidConfig.cardBaseHeightMin, LucidConfig.cardBaseHeightPadding + this.wrappedDescription.size() * LucidConfig.cardLineHeight);

        if (expanded && progress != null && !this.hidden) {
            populateCriteria(node.holder().id(), progress);
        }

        this.totalHeight = computeTotalHeight();
    }

    private static AdvancementState resolveState(boolean done, boolean rare) {
        if (done) {
            return rare ? AdvancementState.OBTAINED_RARE : AdvancementState.OBTAINED_NORMAL;
        }
        return rare ? AdvancementState.UNOBTAINED_RARE : AdvancementState.UNOBTAINED_NORMAL;
    }

    private void populateCriteria(ResourceLocation advancementId, AdvancementProgress progress) {
        for (String criterion : progress.getCompletedCriteria()) {
            String label = CriterionTranslator.resolve(advancementId, criterion);
            criteria.add(new CriterionEntry(criterion, LucidConfig.cardIconDone + " " + label, true));
        }
        for (String criterion : progress.getRemainingCriteria()) {
            String label = CriterionTranslator.resolve(advancementId, criterion);
            criteria.add(new CriterionEntry(criterion, LucidConfig.cardIconLocked + " " + label, false));
        }
    }

    private int computeTotalHeight() {
        if (!expanded || hidden) {
            return baseHeight;
        }
        int criteriaHeight = criteria.isEmpty()
                ? LucidConfig.cardCriteriaSectionPadding
                : LucidConfig.cardCriteriaSectionPadding + criteria.size() * LucidConfig.cardLineHeight;
        return baseHeight + LucidConfig.cardExpandedHeaderHeight + criteriaHeight;
    }

    public int getHeight() {
        return totalHeight;
    }

    public int getBaseHeight() {
        return baseHeight;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public AdvancementNode getNode() {
        return node;
    }

    public void renderBackgroundAndText(GuiGraphics guiGraphics, Font font, int x, int y, int width, int mouseX, int mouseY,
                                        int viewportY, int viewportHeight, boolean isBlocked) {
        CardPalette palette = resolvePalette();

        guiGraphics.fillGradient(x, y, x + width, y + totalHeight, palette.background1(), palette.background2());
        guiGraphics.fill(x, y, x + 3, y + totalHeight, palette.border());
        guiGraphics.fill(x, y, x + width, y + 1, palette.border());
        guiGraphics.fill(x, y + totalHeight - 1, x + width, y + totalHeight, palette.border());
        guiGraphics.fill(x + width - 1, y, x + width, y + totalHeight, palette.border());

        if (!isBlocked && isHovered(mouseX, mouseY, x, y, width, viewportY, viewportHeight)) {
            guiGraphics.fill(x, y, x + width, y + totalHeight, LucidConfig.cardHoverOverlayColor);
        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 50);

        if (hidden) {
            int iconY = y + (baseHeight / 2) - 8;
            guiGraphics.drawCenteredString(font, "?", x + 20, iconY + 4, LucidConfig.cardHiddenMarkColor);
        }

        guiGraphics.drawString(font, title, x + LucidConfig.cardTextXOffset, y + 8, palette.title(), true);

        int descriptionY = y + 22;
        for (FormattedCharSequence line : wrappedDescription) {
            guiGraphics.drawString(font, line, x + LucidConfig.cardTextXOffset, descriptionY, LucidConfig.cardDescriptionColor, true);
            descriptionY += LucidConfig.cardLineHeight;
        }

        guiGraphics.drawString(font, done ? LucidConfig.cardIconDone : LucidConfig.cardIconLocked, x + width - LucidConfig.cardStatusIconXOffset,
                y + (baseHeight / 2) - 4, palette.status(), true);

        if (!hidden) {
            guiGraphics.drawString(font, LucidConfig.cardIconTracked, x + width - LucidConfig.cardTrackIconXOffset,
                    y + (baseHeight / 2) - 4, palette.tracked(), true);
        }

        if (expanded && !hidden) {
            renderCriteriaSection(guiGraphics, font, x, y, width);
        }

        guiGraphics.pose().popPose();
    }

    private void renderCriteriaSection(GuiGraphics guiGraphics, Font font, int x, int y, int width) {
        guiGraphics.fill(x + 12, y + baseHeight - 5, x + width - 12, y + baseHeight - 4, LucidConfig.cardDividerColor);

        int lineY = y + baseHeight + 3;
        guiGraphics.drawString(font, Component.translatable(Constants.MOD_ID + ".gui.card.requirements"),
                x + LucidConfig.cardTextXOffset, lineY, LucidConfig.cardRequirementsLabelColor, true);
        lineY += 11;

        if (criteria.isEmpty()) {
            guiGraphics.drawString(font, Component.translatable(Constants.MOD_ID + ".gui.card.no_requirements"),
                    x + 48, lineY, LucidConfig.cardInactiveColor, true);
            return;
        }

        for (CriterionEntry entry : criteria) {
            int color = entry.completed() ? LucidConfig.cardCriterionDoneColor : LucidConfig.cardCriterionPendingColor;
            guiGraphics.drawString(font, entry.formattedLabel(), x + 48, lineY, color, true);
            lineY += LucidConfig.cardLineHeight;
        }
    }

    private CardPalette resolvePalette() {
        if (done) {
            return rare
                    ? new CardPalette(LucidConfig.cardObtainedRareBg1, LucidConfig.cardObtainedRareBg2,
                    LucidConfig.cardObtainedRareBorder, LucidConfig.cardObtainedRareTitle,
                    LucidConfig.cardObtainedRareBorder, tracked ? LucidConfig.cardTrackedActiveColor : LucidConfig.cardInactiveColor)
                    : new CardPalette(LucidConfig.cardObtainedBg1, LucidConfig.cardObtainedBg2,
                    LucidConfig.cardObtainedBorder, LucidConfig.cardObtainedTitle,
                    LucidConfig.cardObtainedBorder, tracked ? LucidConfig.cardTrackedActiveColor : LucidConfig.cardInactiveColor);
        }
        if (rare) {
            return new CardPalette(LucidConfig.cardNormalBg1, LucidConfig.cardNormalBg2,
                    LucidConfig.cardRareBorder, LucidConfig.cardRareTitle,
                    LucidConfig.cardInactiveColor, tracked ? LucidConfig.cardTrackedActiveColor : LucidConfig.cardInactiveColor);
        }
        return new CardPalette(LucidConfig.cardNormalBg1, LucidConfig.cardNormalBg2,
                LucidConfig.cardNormalBorder, LucidConfig.cardNormalTitle,
                LucidConfig.cardInactiveColor, tracked ? LucidConfig.cardTrackedActiveColor : LucidConfig.cardInactiveColor);
    }

    private boolean isHovered(int mouseX, int mouseY, int x, int y, int width, int viewportY, int viewportHeight) {
        if (mouseY < viewportY || mouseY > viewportY + viewportHeight) {
            return false;
        }
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + totalHeight;
    }

    @Nullable
    public String getHoveredCriterionTag(Font font, int mouseX, int mouseY, int x, int y, int viewportY, int viewportHeight, boolean isBlocked) {
        if (isBlocked || !expanded || hidden || criteria.isEmpty()) {
            return null;
        }
        if (mouseY < viewportY || mouseY > viewportY + viewportHeight) {
            return null;
        }

        int lineY = y + baseHeight + 14;
        for (CriterionEntry entry : criteria) {
            int textWidth = font.width(entry.formattedLabel());
            if (mouseX >= x + 48 && mouseX <= x + 48 + textWidth && mouseY >= lineY && mouseY <= lineY + 9) {
                return entry.rawId();
            }
            lineY += LucidConfig.cardLineHeight;
        }
        return null;
    }

    public float getProgressRatio() {
        return progressRatio;
    }

    public void renderIcon(GuiGraphics guiGraphics, int x, int y) {
        if (hidden) {
            return;
        }
        int iconY = y + (baseHeight / 2) - 8;
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 50);
        guiGraphics.renderItem(display.getIcon(), x + LucidConfig.cardIconXOffset, iconY);
        guiGraphics.pose().popPose();
    }

    public boolean isTrackIconHovered(double mouseX, double mouseY, int x, int y, int width, int viewportY, int viewportHeight, boolean isBlocked) {
        if (isBlocked || hidden) {
            return false;
        }
        if (mouseY < viewportY || mouseY > viewportY + viewportHeight) {
            return false;
        }

        int iconX = x + width - LucidConfig.cardTrackIconXOffset;
        int iconY = y + (baseHeight / 2) - 4;

        return mouseX >= iconX - 4 && mouseX <= iconX + 12 && mouseY >= iconY - 4 && mouseY <= iconY + 10;
    }

    @Nullable
    public ItemStack getHoveredIcon(int mouseX, int mouseY, int x, int y, int viewportY, int viewportHeight, boolean isBlocked) {
        if (isBlocked || hidden) {
            return null;
        }
        int iconY = y + (baseHeight / 2) - 8;
        boolean withinIcon = mouseX >= x + LucidConfig.cardIconXOffset && mouseX <= x + 28 && mouseY >= iconY && mouseY <= iconY + 16;
        boolean withinViewport = mouseY >= viewportY && mouseY <= viewportY + viewportHeight;
        return (withinIcon && withinViewport) ? display.getIcon() : null;
    }

    @Override
    public int compareTo(AdvancementCard other) {
        int stateComparison = state.compareTo(other.state);
        return stateComparison != 0 ? stateComparison : title.getString().compareTo(other.title.getString());
    }

    private record CardPalette(int background1, int background2, int border, int title, int status, int tracked) {
    }
}