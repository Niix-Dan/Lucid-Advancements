package com.niixlabs.lucidadvancements.client.gui;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LucidAdvancementsOverlay {

    public static void render(GuiGraphics guiGraphics, float partialTick) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.options.hideGui || mc.player == null || LucidAdvancementsScreen.TRACKED_ADVANCEMENTS.isEmpty()) return;
        if (mc.getConnection() == null || mc.getConnection().getAdvancements() == null) return;

        Font font = mc.font;
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int boxWidth = 135;
        int startX = screenWidth - boxWidth - 8;
        int startY = 55;
        int spacing = 5;

        Map<AdvancementHolder, AdvancementProgress> vanillaProgressMap = ((AdvancementProgressAccess) mc.getConnection().getAdvancements()).lucid$getProgressMap();

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

            List<String> remainingCriteria = new ArrayList<>();
            for (String criterion : progress.getRemainingCriteria()) remainingCriteria.add(cleanCriteriaName(criterion));

            int maxVisibleCriteria = Math.min(3, remainingCriteria.size());
            int boxHeight = 24 + ((remainingCriteria.size() > 3 ? maxVisibleCriteria + 1 : maxVisibleCriteria) * 10);
            if (maxVisibleCriteria > 0) boxHeight += 2;

            guiGraphics.fill(startX, startY, startX + boxWidth, startY + boxHeight, 0x880A0A0A);

            boolean isChallenge = display.getType() == net.minecraft.advancements.AdvancementType.CHALLENGE;
            int accentColor = isChallenge ? 0xFFAA00FF : 0xFF00FFAA;
            guiGraphics.fill(startX, startY, startX + 2, startY + boxHeight, accentColor);

            ItemStack iconStack = display.getIcon();
            guiGraphics.pose().pushPose();
            guiGraphics.renderItem(iconStack, startX + 6, startY + 4);
            guiGraphics.pose().popPose();

            String titleText = font.plainSubstrByWidth(display.getTitle().getString(), boxWidth - 32);
            int titleColor = isChallenge ? 0xFFFF77FF : 0xFFFFFFFF;
            guiGraphics.drawString(font, titleText, startX + 26, startY + 5, titleColor, true);

            int textY = startY + 22;
            for (int i = 0; i < maxVisibleCriteria; i++) {
                String cleanName = formatCaps(remainingCriteria.get(i));
                String critLine = font.plainSubstrByWidth("🔒 " + cleanName, boxWidth - 10);
                guiGraphics.drawString(font, critLine, startX + 8, textY, 0xFFAAAAAA, true);
                textY += 10;
            }

            if (remainingCriteria.size() > 3) {
                int hiddenCount = remainingCriteria.size() - 3;
                guiGraphics.drawString(font, "  + " + hiddenCount + " more...", startX + 8, textY, 0xFF555555, true);
            }

            startY += boxHeight + spacing;

            if (startY > screenHeight - 35) {
                break;
            }
        }
    }

    private static String cleanCriteriaName(String raw) {
        if (raw.contains(":")) {
            raw = raw.substring(raw.indexOf(":") + 1);
        }
        return raw.replace("_", " ").toLowerCase();
    }

    private static String formatCaps(String c) {
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
}