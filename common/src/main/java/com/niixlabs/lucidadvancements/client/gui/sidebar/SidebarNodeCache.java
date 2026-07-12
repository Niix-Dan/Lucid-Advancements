package com.niixlabs.lucidadvancements.client.gui.sidebar;

import com.niixlabs.lucidadvancements.Constants;
import com.niixlabs.lucidadvancements.config.LucidConfig;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

public final class SidebarNodeCache {
    public final AdvancementNode node;
    public final ItemStack icon;
    public final String displayTitle;

    public SidebarNodeCache(@Nullable AdvancementNode node, Font font, int maxTextWidth) {
        this.node = node;
        if (node == null) {
            this.icon = new ItemStack(Items.LECTERN);
            this.displayTitle = font.plainSubstrByWidth(globalCategoryLabel(), maxTextWidth);
        } else {
            this.icon = node.holder().value().display().map(DisplayInfo::getIcon).orElse(new ItemStack(Items.BOOK));
            this.displayTitle = truncatedTitle(node, font, maxTextWidth);
        }
    }

    private static String globalCategoryLabel() {
        return Component.translatable(Constants.MOD_ID + ".gui.global_category.tab_name").getString();
    }

    private static String truncatedTitle(AdvancementNode node, Font font, int maxTextWidth) {
        String rawTitle = node.holder().value().display()
                .map(DisplayInfo::getTitle)
                .orElse(Component.literal("?"))
                .getString();

        if (font.width(rawTitle) <= maxTextWidth) {
            return rawTitle;
        }
        return font.plainSubstrByWidth(rawTitle, maxTextWidth - font.width(LucidConfig.sidebarTruncationEllipsis)) + LucidConfig.sidebarTruncationEllipsis;
    }
}