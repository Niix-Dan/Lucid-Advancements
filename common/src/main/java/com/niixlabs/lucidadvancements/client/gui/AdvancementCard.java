package com.niixlabs.lucidadvancements.client.gui;

import com.niixlabs.lucidadvancements.client.LucidConfig;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AdvancementCard implements Comparable<AdvancementCard> {

    private final AdvancementNode node;
    private final DisplayInfo display;
    private final AdvancementProgress progress;
    private final boolean done;
    private final boolean hidden;
    private final boolean rare;
    private final AdvancementState state;
    private final List<FormattedCharSequence> wrappedDesc;

    private final boolean isExpanded;
    private final boolean isTracked;
    private final int baseHeight;
    private final int totalHeight;
    private final Component title;

    private final List<CriteriaEntry> criteriaList = new ArrayList<>();

    public AdvancementCard(AdvancementNode node, DisplayInfo display, @Nullable AdvancementProgress progress, boolean isExpanded, boolean isTracked, Font font, int maxWidth) {
        this.node = node;
        this.display = display;
        this.progress = progress;
        this.isExpanded = isExpanded;
        this.done = progress != null && progress.isDone();
        this.isTracked = isTracked && !this.done;
        this.hidden = display.isHidden() && !this.done;
        this.rare = display.getType() == AdvancementType.CHALLENGE;

        if (this.done) {
            this.state = this.rare ? AdvancementState.OBTAINED_RARE : AdvancementState.OBTAINED_NORMAL;
        } else {
            this.state = this.rare ? AdvancementState.UNOBTAINED_RARE : AdvancementState.UNOBTAINED_NORMAL;
        }

        this.title = this.hidden ? Component.literal("???") : display.getTitle();
        Component descRaw = this.hidden ? Component.literal("???") : display.getDescription();

        this.wrappedDesc = font.split(descRaw, maxWidth - 60);
        this.baseHeight = Math.max(46, 28 + (this.wrappedDesc.size() * 10));

        if (isExpanded && progress != null && !this.hidden) {
            for (String c : progress.getCompletedCriteria()) {
                String clean = formatCaps(cleanCriteriaName(c));
                criteriaList.add(new CriteriaEntry(c, "✔ " + clean, true));
            }
            for (String c : progress.getRemainingCriteria()) {
                String clean = formatCaps(cleanCriteriaName(c));
                criteriaList.add(new CriteriaEntry(c, "🔒 " + clean, false));
            }
        }

        if (this.isExpanded && !this.hidden) {
            int criteriaCount = criteriaList.size();
            this.totalHeight = this.baseHeight + 20 + (criteriaCount > 0 ? 12 + (criteriaCount * 10) : 12);
        } else {
            this.totalHeight = this.baseHeight;
        }
    }

    private String cleanCriteriaName(String raw) {
        if (raw.contains(":")) {
            raw = raw.substring(raw.indexOf(":") + 1);
        }
        return raw.replace("_", " ").toLowerCase();
    }

    public int getHeight() {
        return this.totalHeight;
    }

    public int getBaseHeight() {
        return this.baseHeight;
    }

    public boolean isExpanded() {
        return this.isExpanded;
    }

    public AdvancementNode getNode() {
        return this.node;
    }

    public void renderBackgroundAndText(GuiGraphics guiGraphics, Font font, int x, int y, int width, int mouseX, int mouseY) {
        int bgColor1 = LucidConfig.cardNormalBg1;
        int bgColor2 = LucidConfig.cardNormalBg2;
        int borderColor = LucidConfig.cardNormalBorder;
        int titleColor = LucidConfig.cardNormalTitle;
        int trackedColor = this.isTracked ? 0xFF00FFAA : 0xFF555555;
        String trackedIcon = "✦";
        String statusIcon = "🔒";
        int statusColor = 0xFF555555;

        if (this.done) {
            statusIcon = "✔";
            if (this.rare) {
                bgColor1 = LucidConfig.cardObtainedRareBg1;
                bgColor2 = LucidConfig.cardObtainedRareBg2;
                borderColor = LucidConfig.cardObtainedRareBorder;
                titleColor = LucidConfig.cardObtainedRareTitle;
                statusColor = LucidConfig.cardObtainedRareBorder;
                trackedColor = this.isTracked ? LucidConfig.cardObtainedRareBorder : 0xFF555555;
            } else {
                bgColor1 = LucidConfig.cardObtainedBg1;
                bgColor2 = LucidConfig.cardObtainedBg2;
                borderColor = LucidConfig.cardObtainedBorder;
                titleColor = LucidConfig.cardObtainedTitle;
                statusColor = LucidConfig.cardObtainedBorder;
            }
        } else if (this.rare) {
            borderColor = LucidConfig.cardRareBorder;
            titleColor = LucidConfig.cardRareTitle;
            trackedColor = this.isTracked ? LucidConfig.cardRareBorder : 0xFF555555;
        }

        guiGraphics.fillGradient(x, y, x + width, y + this.totalHeight, bgColor1, bgColor2);
        guiGraphics.fill(x, y, x + 3, y + this.totalHeight, borderColor);
        guiGraphics.fill(x, y, x + width, y + 1, borderColor);
        guiGraphics.fill(x, y + this.totalHeight - 1, x + width, y + this.totalHeight, borderColor);
        guiGraphics.fill(x + width - 1, y, x + width, y + this.totalHeight, borderColor);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 50);

        if (this.hidden) {
            int iconY = y + (this.baseHeight / 2) - 8;
            guiGraphics.drawCenteredString(font, "?", x + 20, iconY + 4, 0xFF666666);
        }

        guiGraphics.drawString(font, this.title, x + 40, y + 8, titleColor, true);

        int descY = y + 22;
        for (FormattedCharSequence line : this.wrappedDesc) {
            guiGraphics.drawString(font, line, x + 40, descY, 0xFFAAAAAA, true);
            descY += 10;
        }

        guiGraphics.drawString(font, statusIcon, x + width - 30, y + (this.baseHeight / 2) - 4, statusColor, true);

        if (!this.done) {
            guiGraphics.drawString(font, trackedIcon, x + width - 20, y + (this.baseHeight / 2) - 4, trackedColor, true);
        }

        if (this.isExpanded && !this.hidden) {
            guiGraphics.fill(x + 12, y + this.baseHeight - 5, x + width - 12, y + this.baseHeight - 4, 0x44FFFFFF);

            int triggerY = y + this.baseHeight + 3;
            guiGraphics.drawString(font, "Requirements:", x + 40, triggerY, 0xFF888888, true);
            triggerY += 11;

            if (criteriaList.isEmpty()) {
                guiGraphics.drawString(font, "• No visible requirements...", x + 48, triggerY, 0xFF555555, true);
            } else {
                for (CriteriaEntry entry : criteriaList) {
                    int color = entry.done ? 0xFF00CC88 : 0xFF777777;
                    guiGraphics.drawString(font, entry.formatted, x + 48, triggerY, color, true);
                    triggerY += 10;
                }
            }
        }

        guiGraphics.pose().popPose();
    }

    @Nullable
    public String getHoveredCriterionTag(Font font, int mouseX, int mouseY, int x, int y, int viewportY, int viewportHeight) {
        if (!this.isExpanded || this.hidden || this.criteriaList.isEmpty()) {
            return null;
        }
        if (mouseY < viewportY || mouseY > viewportY + viewportHeight) {
            return null;
        }

        int triggerY = y + this.baseHeight + 14;

        for (CriteriaEntry entry : criteriaList) {
            int textWidth = font.width(entry.formatted);
            if (mouseX >= x + 48 && mouseX <= x + 48 + textWidth && mouseY >= triggerY && mouseY <= triggerY + 9) {
                return entry.original;
            }
            triggerY += 10;
        }
        return null;
    }

    public String formatCaps(String c) {
        if (c == null || c.isEmpty()) return "";
        String[] words = c.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }

    public void renderIcon(GuiGraphics guiGraphics, int x, int y) {
        if (!this.hidden) {
            int iconY = y + (this.baseHeight / 2) - 8;
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, 50);
            guiGraphics.renderItem(this.display.getIcon(), x + 12, iconY);
            guiGraphics.pose().popPose();
        }
    }

    @Nullable
    public boolean isTrackIconHovered(double mouseX, double mouseY, int x, int y, int width, int viewportY, int viewportHeight) {
        if (this.hidden || this.done) {
            return false;
        }

        if (mouseY < viewportY || mouseY > viewportY + viewportHeight) {
            return false;
        }

        int iconX = x + width - 20;
        int iconY = y + (this.baseHeight / 2) - 4;

        return mouseX >= iconX - 4 && mouseX <= iconX + 12 &&
                mouseY >= iconY - 4 && mouseY <= iconY + 10;
    }

    @Nullable
    public ItemStack getHoveredIcon(int mouseX, int mouseY, int x, int y, int viewportY, int viewportHeight) {
        if (this.hidden) {
            return null;
        }
        int iconY = y + (this.baseHeight / 2) - 8;
        if (mouseX >= x + 12 && mouseX <= x + 28 && mouseY >= iconY && mouseY <= iconY + 16) {
            if (mouseY >= viewportY && mouseY <= viewportY + viewportHeight) {
                return this.display.getIcon();
            }
        }
        return null;
    }

    @Override
    public int compareTo(AdvancementCard other) {
        int stateCmp = this.state.compareTo(other.state);
        if (stateCmp != 0) {
            return stateCmp;
        }
        return this.title.getString().compareTo(other.title.getString());
    }

    private static class CriteriaEntry {
        final String original;
        final String formatted;
        final boolean done;

        CriteriaEntry(String original, String formatted, boolean done) {
            this.original = original;
            this.formatted = formatted;
            this.done = done;
        }
    }
}