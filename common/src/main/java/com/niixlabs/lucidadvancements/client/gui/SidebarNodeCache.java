package com.niixlabs.lucidadvancements.client.gui;

import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class SidebarNodeCache {

    public final AdvancementNode node;
    public final ItemStack icon;
    public final String displayTitle;

    public SidebarNodeCache(AdvancementNode node, Font font, int maxTextWidth) {
        this.node = node;
        this.icon = node.holder().value().display().map(DisplayInfo::getIcon).orElse(new ItemStack(Items.BOOK));

        Component categoryTitle = node.holder().value().display().map(DisplayInfo::getTitle).orElse(Component.literal("?"));
        String rawTitle = categoryTitle.getString();

        if (font.width(rawTitle) > maxTextWidth) {
            this.displayTitle = font.plainSubstrByWidth(rawTitle, maxTextWidth - font.width("...")) + "...";
        } else {
            this.displayTitle = rawTitle;
        }
    }
}