package com.niixlabs.lucidadvancements.client.gui;


import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LucidAdvancementsScreen extends Screen implements ClientAdvancements.Listener {

    private final ClientAdvancements clientAdvancements;
    private final List<AdvancementNode> rootNodes = new ArrayList<>();
    private final Map<AdvancementNode, AdvancementProgress> progressMap = new HashMap<>();
    private final List<SidebarNodeCache> cachedSidebarNodes = new ArrayList<>();
    private final List<Renderable> customRenderables = new ArrayList<>();

    private EditBox searchBox;
    private AdvancementNode selectedRoot = null;
    private double scrollOffset = 0;
    private double maxScroll = 0;
    private double sidebarScroll = 0;
    private double maxSidebarScroll = 0;
    private double dragClickOffset = 0;

    private int totalAdvancements = 0;
    private int completedAdvancements = 0;

    private boolean draggingMainScrollbar = false;
    private boolean needsRecalculation = true;
    private final List<AdvancementCard> cachedCards = new ArrayList<>();

    public LucidAdvancementsScreen(ClientAdvancements clientAdvancements) {
        super(Component.literal("Lucid Advancements"));
        this.clientAdvancements = clientAdvancements;
    }

    @Override
    protected <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T widget) {
        this.customRenderables.add(widget);
        return super.addRenderableWidget(widget);
    }

    @Override
    protected void init() {
        super.init();
        this.customRenderables.clear();
        this.rootNodes.clear();
        this.progressMap.clear();

        int sidebarWidth = 100;
        int contentX = sidebarWidth + 18;

        int barWidth = 120;
        int barX = this.width - barWidth - 18;
        int availableSpaceForSearch = barX - (contentX + 100) - 10;
        int searchWidth = Math.min(140, Math.max(80, availableSpaceForSearch));

        this.searchBox = new EditBox(this.font, barX - searchWidth - 15, 16, searchWidth, 16, Component.literal("Buscar..."));
        this.searchBox.setResponder(text -> {
            this.scrollOffset = 0;
            this.needsRecalculation = true;
        });
        this.addRenderableWidget(this.searchBox);

        this.clientAdvancements.setListener(this);
        this.needsRecalculation = true;
        this.rebuildSidebarCache();
    }

    private void rebuildSidebarCache() {
        this.cachedSidebarNodes.clear();
        if (this.font == null) return;
        int maxTextWidth = (int) ((100 - 32) / 0.85f);
        for (AdvancementNode root : this.rootNodes) {
            this.cachedSidebarNodes.add(new SidebarNodeCache(root, this.font, maxTextWidth));
        }
    }

    @Override
    public void removed() {
        super.removed();
        this.clientAdvancements.setListener(null);
    }

    @Override
    public void onAddAdvancementRoot(AdvancementNode node) {
        if (node.root() == node && node.holder().value().display().isPresent() && !this.rootNodes.contains(node)) {
            this.rootNodes.add(node);
            if (this.selectedRoot == null) {
                this.selectedRoot = node;
            }
            this.recalculateGlobalStats();
            this.rebuildSidebarCache();
        }
    }

    @Override
    public void onRemoveAdvancementRoot(AdvancementNode node) {
        this.rootNodes.remove(node);
        if (this.selectedRoot == node) {
            this.selectedRoot = this.rootNodes.isEmpty() ? null : this.rootNodes.get(0);
        }
        this.recalculateGlobalStats();
        this.rebuildSidebarCache();
    }

    @Override
    public void onAddAdvancementTask(AdvancementNode node) {
        this.recalculateGlobalStats();
    }

    @Override
    public void onRemoveAdvancementTask(AdvancementNode node) {
        this.recalculateGlobalStats();
    }

    @Override
    public void onAdvancementsCleared() {
        this.rootNodes.clear();
        this.progressMap.clear();
        this.selectedRoot = null;
        this.totalAdvancements = 0;
        this.completedAdvancements = 0;
        this.needsRecalculation = true;
        this.rebuildSidebarCache();
    }

    @Override
    public void onUpdateAdvancementProgress(AdvancementNode node, AdvancementProgress progress) {
        this.progressMap.put(node, progress);
        this.recalculateGlobalStats();
    }

    @Override
    public void onSelectedTabChanged(@Nullable AdvancementHolder holder) {
        if (holder == null) {
            this.selectedRoot = null;
        } else {
            for (AdvancementNode node : this.rootNodes) {
                if (node.holder().equals(holder)) {
                    this.selectedRoot = node;
                    break;
                }
            }
        }
        this.scrollOffset = 0;
        this.needsRecalculation = true;
    }

    private void recalculateCards() {
        this.cachedCards.clear();

        if (this.selectedRoot == null && this.searchBox.getValue().isEmpty()) {
            this.maxScroll = 0;
            this.needsRecalculation = false;
            return;
        }

        int sidebarWidth = 100;
        int contentWidth = this.width - sidebarWidth - 36;
        String query = this.searchBox.getValue().toLowerCase();
        boolean isSearching = !query.isEmpty();
        boolean isModIdSearch = query.startsWith("@");
        String searchTarget = isModIdSearch ? query.substring(1) : query;

        List<AdvancementNode> nodesToDisplay = new ArrayList<>();

        if (isSearching) {
            for (AdvancementNode root : this.rootNodes) {
                nodesToDisplay.addAll(this.collectTasks(root));
            }
        } else {
            nodesToDisplay = this.collectTasks(this.selectedRoot);
        }

        for (AdvancementNode child : nodesToDisplay) {
            if (child.holder().value().display().isEmpty()) continue;
            DisplayInfo display = child.holder().value().display().get();

            if (isSearching) {
                String title = display.getTitle().getString().toLowerCase();
                String desc = display.getDescription().getString().toLowerCase();
                String modid = child.holder().id().getNamespace().toLowerCase();

                String catRaw = "";
                if (child.root() != null && child.root().holder().value().display().isPresent()) {
                    catRaw = child.root().holder().value().display().get().getTitle().getString().toLowerCase();
                }

                if (isModIdSearch) {
                    if (!modid.contains(searchTarget)) continue;
                } else {
                    if (!title.contains(searchTarget) && !desc.contains(searchTarget) && !catRaw.contains(searchTarget)) {
                        continue;
                    }
                }
            }

            AdvancementProgress prog = this.progressMap.get(child);
            boolean done = prog != null && prog.isDone();

            this.cachedCards.add(new AdvancementCard(child, display, done, this.font, contentWidth));
        }

        Collections.sort(this.cachedCards);

        int totalCardsHeightCalc = 0;
        for (AdvancementCard card : this.cachedCards) {
            totalCardsHeightCalc += card.getHeight() + 8;
        }

        int topBarHeight = 48;
        int viewportY = isSearching ? topBarHeight + 10 : topBarHeight + 52;
        int viewportHeight = this.height - viewportY - 18;

        this.maxScroll = Math.max(0, totalCardsHeightCalc - viewportHeight);
        this.scrollOffset = Mth.clamp(this.scrollOffset, 0, this.maxScroll);
        this.needsRecalculation = false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, this.width, this.height, 0xD0101010);

        if (this.needsRecalculation) {
            this.recalculateCards();
        }

        for (Renderable renderable : this.customRenderables) {
            renderable.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        int sidebarWidth = 100;
        int topBarHeight = 48;
        int contentX = sidebarWidth + 18;
        int contentWidth = this.width - sidebarWidth - 36;

        guiGraphics.fillGradient(sidebarWidth, 0, this.width, topBarHeight, 0xCC161616, 0xCC121212);
        guiGraphics.fill(sidebarWidth, topBarHeight - 1, this.width, topBarHeight, 0xAA2A2A2A);

        guiGraphics.drawString(this.font, Component.translatable("key.advancements"), contentX, 20, 0xFF00FFAA, true);

        if (this.totalAdvancements > 0) {
            float pct = (float) this.completedAdvancements / this.totalAdvancements;
            int barWidth = 120;
            int barX = this.width - barWidth - 18;
            int barY = 20;

            guiGraphics.fill(barX - 1, barY - 1, barX + barWidth + 1, barY + 9, 0xAA333333);
            guiGraphics.fill(barX, barY, barX + barWidth, barY + 8, 0xAA1A1A1A);
            guiGraphics.fillGradient(barX, barY, barX + (int) (barWidth * pct), barY + 8, 0xAA00FFAA, 0xAA00CC88);

            String progressText = this.completedAdvancements + " / " + this.totalAdvancements;
            guiGraphics.drawCenteredString(this.font, Component.literal(progressText), barX + (barWidth / 2), barY - 12, 0xFFFFFFFF);
        }

        guiGraphics.fillGradient(0, 0, sidebarWidth, this.height, 0xCC111111, 0xCC0A0A0A);
        guiGraphics.fill(sidebarWidth - 1, 0, sidebarWidth, this.height, 0xAA2A2A2A);

        int rootItemHeight = 42;
        this.maxSidebarScroll = Math.max(0, (this.cachedSidebarNodes.size() * rootItemHeight) - (this.height - 24));
        this.sidebarScroll = Mth.clamp(this.sidebarScroll, 0, this.maxSidebarScroll);

        guiGraphics.enableScissor(0, 0, sidebarWidth, this.height);
        int sY = 12 - (int) this.sidebarScroll;

        for (SidebarNodeCache cache : this.cachedSidebarNodes) {
            boolean isSelected = (cache.node == this.selectedRoot);

            if (isSelected) {
                guiGraphics.fill(4, sY, sidebarWidth - 4, sY + 34, 0xAA252525);
                guiGraphics.fill(4, sY, 6, sY + 34, 0xFF00FFAA);
            } else if (mouseX >= 4 && mouseX <= sidebarWidth - 4 && mouseY >= sY && mouseY <= sY + 34) {
                guiGraphics.fill(4, sY, sidebarWidth - 4, sY + 34, 0x881C1C1C);
            }

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(28, sY + 14, 0);
            guiGraphics.pose().scale(0.85f, 0.85f, 1.0f);
            int textColor = isSelected ? 0xFF00FFAA : 0xFFAAAAAA;
            guiGraphics.drawString(this.font, cache.displayTitle, 0, 0, textColor, true);
            guiGraphics.pose().popPose();

            sY += rootItemHeight;
        }

        sY = 12 - (int) this.sidebarScroll;
        for (SidebarNodeCache cache : this.cachedSidebarNodes) {
            guiGraphics.renderItem(cache.icon, 8, sY + 9);
            sY += rootItemHeight;
        }
        guiGraphics.disableScissor();

        ItemStack hoveredTooltipIcon = null;
        boolean isSearching = !this.searchBox.getValue().isEmpty();

        if (this.selectedRoot != null || isSearching) {
            int viewportY = isSearching ? topBarHeight + 10 : topBarHeight + 52;
            int viewportHeight = this.height - viewportY - 18;

            if (!isSearching && this.selectedRoot != null && this.selectedRoot.holder().value().display().isPresent()) {
                DisplayInfo rootDisplay = this.selectedRoot.holder().value().display().get();
                guiGraphics.pose().pushPose();
                guiGraphics.pose().scale(1.2f, 1.2f, 1.2f);
                guiGraphics.drawString(this.font, rootDisplay.getTitle(), (int) (contentX / 1.2f), (int) ((topBarHeight + 10) / 1.2f), 0xFFFFFFFF, true);
                guiGraphics.pose().popPose();

                guiGraphics.drawString(this.font, rootDisplay.getDescription(), contentX, topBarHeight + 28, 0xFFAAAAAA, true);
                guiGraphics.fill(contentX, topBarHeight + 44, this.width - 18, topBarHeight + 45, 0x88303030);
            }

            guiGraphics.enableScissor(contentX, viewportY, this.width - 18, viewportY + viewportHeight);

            int cardYOffset = viewportY - (int) this.scrollOffset;
            for (AdvancementCard card : this.cachedCards) {
                if (cardYOffset + card.getHeight() > viewportY && cardYOffset < viewportY + viewportHeight) {
                    card.renderBackgroundAndText(guiGraphics, this.font, contentX, cardYOffset, contentWidth);
                }
                cardYOffset += card.getHeight() + 8;
            }

            cardYOffset = viewportY - (int) this.scrollOffset;
            for (AdvancementCard card : this.cachedCards) {
                if (cardYOffset + card.getHeight() > viewportY && cardYOffset < viewportY + viewportHeight) {
                    card.renderIcon(guiGraphics, contentX, cardYOffset);

                    ItemStack possibleHover = card.getHoveredIcon(mouseX, mouseY, contentX, cardYOffset, viewportY, viewportHeight);
                    if (possibleHover != null) {
                        hoveredTooltipIcon = possibleHover;
                    }
                }
                cardYOffset += card.getHeight() + 8;
            }
            guiGraphics.disableScissor();

            if (this.maxScroll > 0) {
                int scrollbarX = this.width - 12;
                int thumbHeight = Math.max(24, (int) ((viewportHeight / (float) (viewportHeight + this.maxScroll)) * viewportHeight));
                int thumbY = viewportY + (int) ((this.scrollOffset / this.maxScroll) * (viewportHeight - thumbHeight));

                guiGraphics.fill(scrollbarX, viewportY, scrollbarX + 3, viewportY + viewportHeight, 0xAA1A1A1A);

                int thumbColor = this.draggingMainScrollbar ? 0xFF00FFAA : 0xAA00FFAA;
                guiGraphics.fill(scrollbarX, thumbY, scrollbarX + 3, thumbY + thumbHeight, thumbColor);
            }
        }

        if (hoveredTooltipIcon != null) {
            guiGraphics.renderTooltip(this.font, hoveredTooltipIcon, mouseX, mouseY);
        }
    }

    private void recalculateGlobalStats() {
        this.totalAdvancements = 0;
        this.completedAdvancements = 0;
        for (AdvancementNode root : this.rootNodes) {
            for (AdvancementNode node : this.collectTasks(root)) {
                if (node.holder().value().display().isPresent()) {
                    AdvancementProgress p = this.progressMap.get(node);
                    this.totalAdvancements++;
                    if (p != null && p.isDone()) {
                        this.completedAdvancements++;
                    }
                }
            }
        }
        this.needsRecalculation = true;
    }

    private List<AdvancementNode> collectTasks(AdvancementNode root) {
        List<AdvancementNode> list = new ArrayList<>();
        this.collect(root, list);
        list.remove(root);
        return list;
    }

    private void collect(AdvancementNode node, List<AdvancementNode> list) {
        list.add(node);
        for (AdvancementNode child : node.children()) {
            this.collect(child, list);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (mouseX <= 100) {
                int sY = 12 - (int) this.sidebarScroll;
                for (SidebarNodeCache cache : this.cachedSidebarNodes) {
                    if (mouseY >= sY && mouseY <= sY + 34) {
                        this.clientAdvancements.setSelectedTab(cache.node.holder(), true);
                        return true;
                    }
                    sY += 42;
                }
            }

            else if (this.maxScroll > 0) {
                int topBarHeight = 48;
                boolean isSearching = !this.searchBox.getValue().isEmpty();
                int viewportY = isSearching ? topBarHeight + 10 : topBarHeight + 52;
                int viewportHeight = this.height - viewportY - 18;
                int scrollbarX = this.width - 12;

                if (mouseX >= scrollbarX - 2 && mouseX <= scrollbarX + 5) {
                    int thumbHeight = Math.max(24, (int) ((viewportHeight / (float) (viewportHeight + this.maxScroll)) * viewportHeight));
                    int thumbY = viewportY + (int) ((this.scrollOffset / this.maxScroll) * (viewportHeight - thumbHeight));

                    if (mouseY >= thumbY && mouseY <= thumbY + thumbHeight) {
                        this.draggingMainScrollbar = true;
                        this.dragClickOffset = mouseY - thumbY;
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.draggingMainScrollbar && this.maxScroll > 0) {
            int topBarHeight = 48;
            boolean isSearching = !this.searchBox.getValue().isEmpty();
            int viewportY = isSearching ? topBarHeight + 10 : topBarHeight + 52;
            int viewportHeight = this.height - viewportY - 18;

            int thumbHeight = Math.max(24, (int) ((viewportHeight / (float) (viewportHeight + this.maxScroll)) * viewportHeight));
            int trackHeight = viewportHeight - thumbHeight;

            if (trackHeight > 0) {
                double targetThumbY = mouseY - this.dragClickOffset;

                double scrollPercentage = (targetThumbY - viewportY) / trackHeight;
                scrollPercentage = Mth.clamp(scrollPercentage, 0.0, 1.0);
                this.scrollOffset = scrollPercentage * this.maxScroll;
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            this.draggingMainScrollbar = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseX <= 100) {
            this.sidebarScroll = Mth.clamp(this.sidebarScroll - (scrollY * 20), 0.0, this.maxSidebarScroll);
            return true;
        } else if (this.maxScroll > 0) {
            this.scrollOffset = Mth.clamp(this.scrollOffset - (scrollY * 30), 0.0, this.maxScroll);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}