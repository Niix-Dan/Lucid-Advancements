package com.niixlabs.lucidadvancements.client.gui;

import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AdvancementCard implements Comparable<AdvancementCard> {

    private final AdvancementNode node;
    private final DisplayInfo display;
    private final boolean done;
    private final boolean hidden;
    private final boolean rare;
    private final AdvancementState state;
    private final List<FormattedCharSequence> wrappedDesc;
    private final int height;
    private final Component title;

    public AdvancementCard(AdvancementNode node, DisplayInfo display, boolean done, Font font, int maxWidth) {
        this.node = node;
        this.display = display;
        this.done = done;
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
        this.height = Math.max(46, 28 + (this.wrappedDesc.size() * 10));
    }

    public int getHeight() {
        return this.height;
    }

    public void render(GuiGraphics guiGraphics, Font font, int x, int y, int width) {
        int bgColor1 = 0xAA1A1A1A;
        int bgColor2 = 0xAA121212;
        int borderColor = 0x882A2A2A;
        int titleColor = 0xFFFFFFFF;
        String statusIcon = "🔒";
        int statusColor = 0xFF555555;

        if (this.done) {
            statusIcon = "✔";
            if (this.rare) {
                bgColor1 = 0xBB2D1438;
                bgColor2 = 0xBB1E0C26;
                borderColor = 0xAAAA00FF;
                titleColor = 0xFFFF77FF;
                statusColor = 0xFFAA00FF;
            } else {
                bgColor1 = 0xBB142E1F;
                bgColor2 = 0xBB0C1E14;
                borderColor = 0xAA00FFAA;
                titleColor = 0xFF77FF77;
                statusColor = 0xFF00FFAA;
            }
        } else if (this.rare) {
            borderColor = 0xAA42245A;
            titleColor = 0xFFB388CC;
        }

        guiGraphics.fillGradient(x, y, x + width, y + this.height, bgColor1, bgColor2);
        guiGraphics.fill(x, y, x + 3, y + this.height, borderColor);
        guiGraphics.fill(x, y, x + width, y + 1, borderColor);
        guiGraphics.fill(x, y + this.height - 1, x + width, y + this.height, borderColor);
        guiGraphics.fill(x + width - 1, y, x + width, y + this.height, borderColor);

        int iconY = y + (this.height / 2) - 8;
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 50);

        if (!this.hidden) {
            guiGraphics.renderItem(this.display.getIcon(), x + 12, iconY);
        } else {
            guiGraphics.drawCenteredString(font, "?", x + 20, iconY + 4, 0xFF666666);
        }

        guiGraphics.drawString(font, this.title, x + 40, y + 8, titleColor, true);

        int descY = y + 22;
        for (FormattedCharSequence line : this.wrappedDesc) {
            guiGraphics.drawString(font, line, x + 40, descY, 0xFFAAAAAA, true);
            descY += 10;
        }

        guiGraphics.drawString(font, statusIcon, x + width - 20, y + (this.height / 2) - 4, statusColor, true);

        guiGraphics.pose().popPose();
    }

    @Nullable
    public ItemStack getHoveredIcon(int mouseX, int mouseY, int x, int y, int viewportY, int viewportHeight) {
        if (this.hidden) {
            return null;
        }
        int iconY = y + (this.height / 2) - 8;
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
}