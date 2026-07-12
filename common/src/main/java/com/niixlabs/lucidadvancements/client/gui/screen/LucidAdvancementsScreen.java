package com.niixlabs.lucidadvancements.client.gui.screen;

import com.niixlabs.lucidadvancements.Constants;
import com.niixlabs.lucidadvancements.client.gui.card.AdvancementCard;
import com.niixlabs.lucidadvancements.client.gui.card.FilterMode;
import com.niixlabs.lucidadvancements.client.gui.sidebar.SidebarNodeCache;
import com.niixlabs.lucidadvancements.config.LucidConfig;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class LucidAdvancementsScreen extends Screen implements ClientAdvancements.Listener {
    private static final int BACKDROP_COLOR = 0xD0101010;
    private static final int TOP_BAR_GRADIENT_START = 0xCC161616;
    private static final int TOP_BAR_GRADIENT_END = 0xCC121212;
    private static final int TOP_BAR_BORDER = 0xAA2A2A2A;
    private static final int SIDEBAR_GRADIENT_START = 0xCC111111;
    private static final int SIDEBAR_GRADIENT_END = 0xCC0A0A0A;
    private static final int SIDEBAR_BORDER = 0xAA2A2A2A;
    private static final int SIDEBAR_SELECTED_FILL = 0xAA252525;
    private static final int SIDEBAR_SELECTED_ACCENT = 0xFF00FFAA;
    private static final int SIDEBAR_HOVER_FILL = 0x881C1C1C;
    private static final int SIDEBAR_TEXT_SELECTED = 0xFF00FFAA;
    private static final int SIDEBAR_TEXT_IDLE = 0xFFAAAAAA;
    private static final int PROGRESS_BAR_GRADIENT_START = 0xFF141414;
    private static final int PROGRESS_BAR_GRADIENT_END = 0xFF0D0D0D;
    private static final int PROGRESS_BAR_TOP_BORDER = 0x22FFFFFF;
    private static final int PROGRESS_TRACK_BORDER = 0xAA333333;
    private static final int PROGRESS_TRACK_FILL = 0xAA1A1A1A;
    private static final int PROGRESS_FILL_START = 0xAA00FFAA;
    private static final int PROGRESS_FILL_END = 0xAA00CC88;
    private static final int PROGRESS_TEXT_COLOR = 0xFFE0E0E0;
    private static final int TRACKED_FILL_START = 0xAAAF45EE;
    private static final int TRACKED_FILL_END = 0xAA7A28CB;
    private static final int TRACKED_PERCENTAGE_COLOR = 0xFFAF45EE;
    private static final int HEADER_TITLE_COLOR = 0xFFFFFFFF;
    private static final int HEADER_DESCRIPTION_COLOR = 0xFFAAAAAA;
    private static final int HEADER_DIVIDER_COLOR = 0x88303030;
    private static final int HEADER_PERCENTAGE_COLOR = 0xFF00FFAA;
    private static final int SCROLLBAR_TRACK_COLOR = 0xAA1A1A1A;
    private static final int SCROLLBAR_THUMB_ACTIVE = 0xFF00FFAA;
    private static final int SCROLLBAR_THUMB_IDLE = 0xAA00FFAA;


    private final ClientAdvancements clientAdvancements;
    private final List<AdvancementNode> rootNodes = new ArrayList<>();
    private final Map<AdvancementNode, AdvancementProgress> progressMap = new HashMap<>();
    private final List<SidebarNodeCache> cachedSidebarNodes = new ArrayList<>();
    private final List<Renderable> customRenderables = new ArrayList<>();
    private final List<AdvancementCard> cachedCards = new ArrayList<>();
    private final Map<AdvancementNode, List<AdvancementNode>> flattenedTasksCache = new HashMap<>();

    public static @Nullable ResourceLocation advancementToFocusOnOpen = null;
    public static @Nullable ResourceLocation lastSelectedTabId = null;
    public static final Set<String> TRACKED_ADVANCEMENTS = new HashSet<>();
    private static boolean configLoaded = false;

    private EditBox searchBox;
    private FilterMode currentFilter = FilterMode.ALL;
    private LucidDropdown<FilterMode> filterDropdown;

    private AdvancementNode expandedNode = null;
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

    public LucidAdvancementsScreen(ClientAdvancements clientAdvancements) {
        super(Component.literal("Lucid Advancements"));
        this.clientAdvancements = clientAdvancements;
        if (!configLoaded) {
            LucidConfig.load();
            configLoaded = true;
        }
    }

    private double getTargetScale() {
        return minecraft == null ? 1.0 : GuiScale.targetScale(minecraft);
    }

    private double getScaleFactor() {
        return minecraft == null ? 1.0 : GuiScale.scaleFactor(minecraft);
    }

    private boolean isSearching() {
        return !searchBox.getValue().isEmpty();
    }

    private int viewportHeight(int viewportY) {
        return height - viewportY - ScreenMetrics.VIEWPORT_BOTTOM_MARGIN;
    }

    private int scrollThumbHeight(int viewportHeight) {
        return Math.max(ScreenMetrics.MIN_SCROLL_THUMB_HEIGHT,
                (int) ((viewportHeight / (float) (viewportHeight + maxScroll)) * viewportHeight));
    }

    private int scrollThumbY(int viewportY, int viewportHeight, int thumbHeight) {
        return viewportY + (int) ((scrollOffset / maxScroll) * (viewportHeight - thumbHeight));
    }

    @Override
    protected <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T widget) {
        customRenderables.add(widget);
        return super.addRenderableWidget(widget);
    }

    @Override
    protected void init() {
        if (minecraft != null) {
            double targetScale = getTargetScale();
            this.width = (int) (minecraft.getWindow().getScreenWidth() / targetScale);
            this.height = (int) (minecraft.getWindow().getScreenHeight() / targetScale);
        }

        super.init();
        customRenderables.clear();
        rootNodes.clear();
        progressMap.clear();

        initTopBarWidgets();

        ResourceLocation savedTab = lastSelectedTabId;
        clientAdvancements.setListener(this);
        lastSelectedTabId = savedTab;
        needsRecalculation = true;
        rebuildSidebarCache();

        checkFocus();
    }

    private void initTopBarWidgets() {
        int currentX = width - ScreenMetrics.CONTENT_MARGIN;

        int searchWidth = 120;
        currentX -= searchWidth;
        searchBox = new EditBox(font, currentX, 16, searchWidth, 16, Component.translatable(Constants.MOD_ID + ".gui.search.placeholder"));
        searchBox.setResponder(text -> {
            scrollOffset = 0;
            needsRecalculation = true;
        });
        addRenderableWidget(searchBox);

        int filterWidth = 110;
        currentX -= (filterWidth + 6);
        filterDropdown = new LucidDropdown<>(currentX, 16, filterWidth, 16, currentFilter,
                List.of(FilterMode.values()), FilterMode::getDisplayName, mode -> {
            currentFilter = mode;
            scrollOffset = 0;
            needsRecalculation = true;
        });
        addRenderableWidget(filterDropdown);

        int clearWidth = 95;
        currentX -= (clearWidth + 6);
        LucidButton clearTrackedButton = new LucidButton(currentX, 16, clearWidth, 16,
                Component.translatable(Constants.MOD_ID + ".gui.clear_tracked.label"), btn -> {
            TRACKED_ADVANCEMENTS.clear();
            needsRecalculation = true;
        });
        addRenderableWidget(clearTrackedButton);

        int scaleWidth = 70;
        currentX -= (scaleWidth + 6);
        LucidButton scaleButton = new LucidButton(currentX, 16, scaleWidth, 16,
                Component.translatable(Constants.MOD_ID + ".gui.scale.label", scaleModeLabel(LucidConfig.customGuiScale)), btn -> {
            int nextScale = LucidConfig.customGuiScale >= 4 ? 0 : LucidConfig.customGuiScale + 1;
            LucidConfig.updateAndSave("customGuiScale", nextScale);
            btn.setMessage(Component.translatable(Constants.MOD_ID + ".gui.scale.label", scaleModeLabel(nextScale)));

            if (minecraft != null) {
                init(minecraft, minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight());
            }
        });
        addRenderableWidget(scaleButton);
    }

    private String scaleModeLabel(int customScale) {
        return customScale == 0
                ? Component.translatable(Constants.MOD_ID + ".gui.scale.mode.vanilla").getString()
                : Component.translatable(Constants.MOD_ID + ".gui.scale.mode.custom", customScale).getString();
    }

    private void checkFocus() {
        if (advancementToFocusOnOpen == null) {
            restoreLastSelectedTab();
            return;
        }

        ResourceLocation targetId = advancementToFocusOnOpen;
        advancementToFocusOnOpen = null;

        AdvancementNode targetRoot = findRootContaining(targetId);
        if (targetRoot == null) {
            return;
        }

        AdvancementNode targetNode = targetId.equals(targetRoot.holder().id())
                ? targetRoot
                : findNode(targetRoot, targetId);

        searchBox.setValue("");
        currentFilter = FilterMode.ALL;
        clientAdvancements.setSelectedTab(targetRoot.holder(), true);
        selectedRoot = targetRoot;
        expandedNode = targetNode;
        lastSelectedTabId = targetRoot.holder().id();

        recalculateCards();
        scrollToCard(targetNode, targetRoot);
        scrollSidebarToSelection();
    }

    private void restoreLastSelectedTab() {
        if (lastSelectedTabId == null) {
            selectedRoot = null;
            return;
        }
        selectedRoot = null;
        for (AdvancementNode root : rootNodes) {
            if (root.holder().id().equals(lastSelectedTabId)) {
                selectedRoot = root;
                clientAdvancements.setSelectedTab(root.holder(), true);
                break;
            }
        }
    }

    @Nullable
    private AdvancementNode findRootContaining(ResourceLocation targetId) {
        for (AdvancementNode root : rootNodes) {
            if (root.holder().id().equals(targetId) || findNode(root, targetId) != null) {
                return root;
            }
        }
        return null;
    }

    @Nullable
    private AdvancementNode findNode(AdvancementNode root, ResourceLocation targetId) {
        for (AdvancementNode node : collectTasks(root)) {
            if (node.holder().id().equals(targetId)) {
                return node;
            }
        }
        return null;
    }

    private void scrollToCard(AdvancementNode targetNode, AdvancementNode targetRoot) {
        if (targetNode == targetRoot) {
            scrollOffset = 0;
            return;
        }

        int yOffset = 0;
        for (AdvancementCard card : cachedCards) {
            if (card.getNode() == targetNode) {
                int viewportY = ScreenMetrics.viewportY(false);
                int viewportHeight = viewportHeight(viewportY);
                int halfCard = card.getHeight() / 2;
                int halfViewport = viewportHeight / 2;
                scrollOffset = Mth.clamp(yOffset + halfCard - halfViewport, 0, maxScroll);
                return;
            }
            yOffset += card.getHeight() + ScreenMetrics.CARD_SPACING;
        }
    }

    private void scrollSidebarToSelection() {
        int rootIndex = rootNodes.indexOf(selectedRoot);
        if (rootIndex == -1) {
            return;
        }

        int sidebarViewport = height - ScreenMetrics.SIDEBAR_PROGRESS_HEIGHT - 24;
        maxSidebarScroll = Math.max(0, (cachedSidebarNodes.size() * ScreenMetrics.SIDEBAR_ROW_HEIGHT) - sidebarViewport);

        int targetScroll = (rootIndex * ScreenMetrics.SIDEBAR_ROW_HEIGHT) + (ScreenMetrics.SIDEBAR_ROW_HEIGHT / 2) - (sidebarViewport / 2);
        sidebarScroll = Mth.clamp(targetScroll, 0, maxSidebarScroll);
    }

    private void rebuildSidebarCache() {
        cachedSidebarNodes.clear();
        if (font == null) {
            return;
        }

        int maxTextWidth = (int) ((ScreenMetrics.SIDEBAR_WIDTH - 32) / 0.85f);

        rootNodes.sort((a, b) -> {
            String namespaceA = a.holder().id().getNamespace();
            String namespaceB = b.holder().id().getNamespace();

            if (namespaceA.equals("minecraft") && !namespaceB.equals("minecraft")) {
                return -1;
            }
            if (!namespaceA.equals("minecraft") && namespaceB.equals("minecraft")) {
                return 1;
            }

            String titleA = a.holder().value().display().map(d -> d.getTitle().getString()).orElse("");
            String titleB = b.holder().value().display().map(d -> d.getTitle().getString()).orElse("");
            return titleA.compareToIgnoreCase(titleB);
        });

        cachedSidebarNodes.add(new SidebarNodeCache(null, font, maxTextWidth));
        for (AdvancementNode root : rootNodes) {
            cachedSidebarNodes.add(new SidebarNodeCache(root, font, maxTextWidth));
        }
    }

    @Override
    public void removed() {
        super.removed();
        clientAdvancements.setListener(null);
    }

    @Override
    public void onAddAdvancementRoot(AdvancementNode node) {
        if (node.root() == node && node.holder().value().display().isPresent() && !rootNodes.contains(node)) {
            rootNodes.add(node);
            rebuildSidebarCache();
            recalculateGlobalStats();
        }
    }

    @Override
    public void onRemoveAdvancementRoot(AdvancementNode node) {
        rootNodes.remove(node);
        if (selectedRoot == node) {
            selectedRoot = null;
        }
        rebuildSidebarCache();
        recalculateGlobalStats();
    }

    @Override
    public void onAddAdvancementTask(AdvancementNode node) {
        recalculateGlobalStats();
    }

    @Override
    public void onRemoveAdvancementTask(AdvancementNode node) {
        recalculateGlobalStats();
    }

    @Override
    public void onAdvancementsCleared() {
        rootNodes.clear();
        progressMap.clear();
        selectedRoot = null;
        totalAdvancements = 0;
        completedAdvancements = 0;
        needsRecalculation = true;
        rebuildSidebarCache();
        flattenedTasksCache.clear();
    }

    @Override
    public void onUpdateAdvancementProgress(AdvancementNode node, AdvancementProgress progress) {
        progressMap.put(node, progress);
        recalculateGlobalStats();
    }

    @Override
    public void onSelectedTabChanged(@Nullable AdvancementHolder holder) {
        selectedRoot = null;
        lastSelectedTabId = null;

        if (holder != null) {
            for (AdvancementNode node : rootNodes) {
                if (node.holder().equals(holder)) {
                    selectedRoot = node;
                    lastSelectedTabId = node.holder().id();
                    break;
                }
            }
        }

        scrollOffset = 0;
        expandedNode = null;
        needsRecalculation = true;
    }

    private int calculateTrackedCompleted() {
        int completed = 0;
        for (AdvancementNode root : rootNodes) {
            for (AdvancementNode node : collectTasks(root)) {
                if (TRACKED_ADVANCEMENTS.contains(node.holder().id().toString())) {
                    AdvancementProgress progress = progressMap.get(node);
                    if (progress != null && progress.isDone()) {
                        completed++;
                    }
                }
            }
        }
        return completed;
    }

    private void recalculateGlobalStats() {
        totalAdvancements = 0;
        completedAdvancements = 0;
        for (AdvancementNode root : rootNodes) {
            for (AdvancementNode node : collectTasks(root)) {
                if (node.holder().value().display().isPresent()) {
                    AdvancementProgress progress = progressMap.get(node);
                    totalAdvancements++;
                    if (progress != null && progress.isDone()) {
                        completedAdvancements++;
                    }
                }
            }
        }
        needsRecalculation = true;
    }

    private List<AdvancementNode> collectTasks(AdvancementNode root) {
        return flattenedTasksCache.computeIfAbsent(root, r -> {
            List<AdvancementNode> list = new ArrayList<>();
            collect(r, list);
            list.remove(r);
            return list;
        });
    }

    private void collect(AdvancementNode node, List<AdvancementNode> list) {
        list.add(node);
        for (AdvancementNode child : node.children()) {
            collect(child, list);
        }
    }

    private void recalculateCards() {
        cachedCards.clear();

        if (rootNodes.isEmpty()) {
            maxScroll = 0;
            needsRecalculation = false;
            return;
        }

        int contentWidth = ScreenMetrics.contentWidth(width);

        String query = searchBox.getValue().toLowerCase();
        boolean searching = !query.isEmpty();

        String modIdFilter = null;
        String textFilter = query;

        if (query.startsWith("@")) {
            String rest = query.substring(1);
            int spaceIndex = rest.indexOf(' ');
            modIdFilter = spaceIndex == -1 ? rest : rest.substring(0, spaceIndex);
            textFilter = spaceIndex == -1 ? "" : rest.substring(spaceIndex + 1).trim();
        }

        List<AdvancementNode> nodesToDisplay = collectNodesToDisplay(searching);

        for (AdvancementNode child : nodesToDisplay) {
            if (child.holder().value().display().isEmpty()) {
                continue;
            }
            DisplayInfo display = child.holder().value().display().get();
            AdvancementProgress progress = progressMap.get(child);
            boolean done = progress != null && progress.isDone();

            if (!matchesFilterMode(child, done, display, progress)) {
                continue;
            }

            boolean expanded = expandedNode == child;
            boolean tracked = TRACKED_ADVANCEMENTS.contains(child.holder().id().toString());
            AdvancementCard card = new AdvancementCard(child, display, progress, expanded, tracked, font, contentWidth);

            if (searching && !matchesSearch(child, card, modIdFilter, textFilter)) {
                continue;
            }

            cachedCards.add(card);
        }

        if (currentFilter == FilterMode.PARTIAL) {
            cachedCards.sort(Comparator.comparingDouble(AdvancementCard::getProgressRatio).reversed());
        } else if (currentFilter == FilterMode.ALL) {
            Collections.sort(cachedCards);
        }

        int totalCardsHeight = 0;
        for (AdvancementCard card : cachedCards) {
            totalCardsHeight += card.getHeight() + ScreenMetrics.CARD_SPACING;
        }

        int viewportY = ScreenMetrics.viewportY(searching);
        maxScroll = Math.max(0, totalCardsHeight - viewportHeight(viewportY));
        scrollOffset = Mth.clamp(scrollOffset, 0, maxScroll);

        needsRecalculation = false;
    }

    private List<AdvancementNode> collectNodesToDisplay(boolean searching) {
        List<AdvancementNode> nodes = new ArrayList<>();
        if (searching || selectedRoot == null) {
            for (AdvancementNode root : rootNodes) {
                nodes.addAll(collectTasks(root));
            }
        } else {
            nodes.addAll(collectTasks(selectedRoot));
        }
        return nodes;
    }

    private boolean matchesFilterMode(AdvancementNode node, boolean done, DisplayInfo display, @Nullable AdvancementProgress progress) {
        return switch (currentFilter) {
            case COMPLETED -> done;
            case INCOMPLETE -> !done;
            case CHALLENGES -> display.getType() == AdvancementType.CHALLENGE;
            case PARTIAL -> !done && progress != null && progress.getPercent() > 0f;
            case TRACKED -> TRACKED_ADVANCEMENTS.contains(node.holder().id().toString());
            case ALL -> true;
        };
    }

    private boolean matchesSearch(AdvancementNode child, AdvancementCard card, @Nullable String modIdFilter, String textFilter) {
        if (modIdFilter != null && !child.holder().id().getNamespace().toLowerCase().contains(modIdFilter)) {
            return false;
        }
        if (textFilter.isEmpty()) {
            return true;
        }

        return card.cachedSearchTitle.contains(textFilter)
                || card.cachedSearchDesc.contains(textFilter)
                || card.cachedSearchCategory.contains(textFilter);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        double scaleFactor = getScaleFactor();
        int scaledMouseX = (int) (mouseX * scaleFactor);
        int scaledMouseY = (int) (mouseY * scaleFactor);

        guiGraphics.pose().pushPose();
        if (scaleFactor != 1.0) {
            float invScale = (float) (1.0 / scaleFactor);
            guiGraphics.pose().scale(invScale, invScale, 1.0f);
        }

        guiGraphics.fill(0, 0, width, height, BACKDROP_COLOR);

        if (needsRecalculation) {
            recalculateCards();
        }

        for (Renderable renderable : customRenderables) {
            renderable.render(guiGraphics, scaledMouseX, scaledMouseY, partialTick);
        }

        renderTopBar(guiGraphics);
        renderSidebar(guiGraphics, scaleFactor, scaledMouseX, scaledMouseY);
        renderProgressBar(guiGraphics);

        boolean searching = isSearching();
        int contentX = ScreenMetrics.contentX();
        int contentWidth = ScreenMetrics.contentWidth(width);
        int viewportY = ScreenMetrics.viewportY(searching);
        int viewportHeight = viewportHeight(viewportY);

        renderContentHeader(guiGraphics, contentX, searching);

        HoverResult hover = renderCardList(guiGraphics, scaleFactor, contentX, contentWidth, viewportY, viewportHeight, scaledMouseX, scaledMouseY);
        renderMainScrollbar(guiGraphics, viewportY, viewportHeight);

        filterDropdown.renderOptions(guiGraphics, scaledMouseX, scaledMouseY);

        guiGraphics.pose().popPose();

        renderHoverTooltip(guiGraphics, hover, mouseX, mouseY);
    }

    private void renderTopBar(GuiGraphics guiGraphics) {
        int contentX = ScreenMetrics.contentX();
        guiGraphics.fillGradient(ScreenMetrics.SIDEBAR_WIDTH, 0, width, ScreenMetrics.TOP_BAR_HEIGHT, TOP_BAR_GRADIENT_START, TOP_BAR_GRADIENT_END);
        guiGraphics.fill(ScreenMetrics.SIDEBAR_WIDTH, ScreenMetrics.TOP_BAR_HEIGHT - 1, width, ScreenMetrics.TOP_BAR_HEIGHT, TOP_BAR_BORDER);
        guiGraphics.drawString(font, Component.translatable("key.advancements"), contentX, 20, SIDEBAR_TEXT_SELECTED, true);
    }

    private void renderSidebar(GuiGraphics guiGraphics, double scaleFactor, int scaledMouseX, int scaledMouseY) {
        int sidebarWidth = ScreenMetrics.SIDEBAR_WIDTH;

        guiGraphics.fillGradient(0, 0, sidebarWidth, height, SIDEBAR_GRADIENT_START, SIDEBAR_GRADIENT_END);
        guiGraphics.fill(sidebarWidth - 1, 0, sidebarWidth, height, SIDEBAR_BORDER);

        maxSidebarScroll = Math.max(0, (cachedSidebarNodes.size() * ScreenMetrics.SIDEBAR_ROW_HEIGHT)
                - (height - ScreenMetrics.SIDEBAR_PROGRESS_HEIGHT - 24));
        sidebarScroll = Mth.clamp(sidebarScroll, 0, maxSidebarScroll);

        int scissorX2 = (int) Math.round(sidebarWidth / scaleFactor);
        int scissorY2 = (int) Math.round((height - ScreenMetrics.SIDEBAR_PROGRESS_HEIGHT) / scaleFactor);
        guiGraphics.enableScissor(0, 0, scissorX2, scissorY2);

        int sidebarViewportBottom = height - ScreenMetrics.SIDEBAR_PROGRESS_HEIGHT;
        int rowY = ScreenMetrics.SIDEBAR_TOP_PADDING - (int) sidebarScroll;
        for (SidebarNodeCache cache : cachedSidebarNodes) {
            renderSidebarRow(guiGraphics, cache, rowY, sidebarWidth, scaledMouseX, scaledMouseY, sidebarViewportBottom);
            rowY += ScreenMetrics.SIDEBAR_ROW_HEIGHT;
        }

        rowY = ScreenMetrics.SIDEBAR_TOP_PADDING - (int) sidebarScroll;
        for (SidebarNodeCache cache : cachedSidebarNodes) {
            guiGraphics.renderItem(cache.icon, 8, rowY + 9);
            rowY += ScreenMetrics.SIDEBAR_ROW_HEIGHT;
        }

        guiGraphics.disableScissor();
    }

    private void renderSidebarRow(GuiGraphics guiGraphics, SidebarNodeCache cache, int rowY, int sidebarWidth, int scaledMouseX, int scaledMouseY, int sidebarViewportBottom) {
        boolean selected = cache.node == selectedRoot;

        if (selected) {
            guiGraphics.fill(4, rowY, sidebarWidth - 4, rowY + ScreenMetrics.SIDEBAR_ITEM_HEIGHT, SIDEBAR_SELECTED_FILL);
            guiGraphics.fill(4, rowY, 6, rowY + ScreenMetrics.SIDEBAR_ITEM_HEIGHT, SIDEBAR_SELECTED_ACCENT);
        } else if (scaledMouseY <= sidebarViewportBottom && scaledMouseX >= 4 && scaledMouseX <= sidebarWidth - 4
                && scaledMouseY >= rowY && scaledMouseY <= rowY + ScreenMetrics.SIDEBAR_ITEM_HEIGHT) {
            guiGraphics.fill(4, rowY, sidebarWidth - 4, rowY + ScreenMetrics.SIDEBAR_ITEM_HEIGHT, SIDEBAR_HOVER_FILL);
        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(28, rowY + 14, 0);
        guiGraphics.pose().scale(0.85f, 0.85f, 1.0f);
        guiGraphics.drawString(font, cache.displayTitle, 0, 0, selected ? SIDEBAR_TEXT_SELECTED : SIDEBAR_TEXT_IDLE, true);
        guiGraphics.pose().popPose();
    }


    private void renderProgressBar(GuiGraphics guiGraphics) {
        int sidebarWidth = ScreenMetrics.SIDEBAR_WIDTH;
        int progressAreaY = height - ScreenMetrics.SIDEBAR_PROGRESS_HEIGHT;

        guiGraphics.fillGradient(0, progressAreaY, sidebarWidth - 1, height, PROGRESS_BAR_GRADIENT_START, PROGRESS_BAR_GRADIENT_END);
        guiGraphics.fill(0, progressAreaY, sidebarWidth - 1, progressAreaY + 1, PROGRESS_BAR_TOP_BORDER);

        if (totalAdvancements <= 0) {
            return;
        }

        float percentage = (float) completedAdvancements / totalAdvancements;
        int barWidth = sidebarWidth - 16;
        int barX = 8;
        int barY = height - 14;

        guiGraphics.fill(barX - 1, barY - 1, barX + barWidth + 1, barY + 7, PROGRESS_TRACK_BORDER);
        guiGraphics.fill(barX, barY, barX + barWidth, barY + 6, PROGRESS_TRACK_FILL);
        guiGraphics.fillGradient(barX, barY, barX + (int) (barWidth * percentage), barY + 6, PROGRESS_FILL_START, PROGRESS_FILL_END);

        String progressText = Component.translatable(Constants.MOD_ID + ".gui.progress_text", completedAdvancements, totalAdvancements).getString();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(sidebarWidth / 2f, barY - 11, 0);
        guiGraphics.pose().scale(0.82f, 0.82f, 1.0f);
        guiGraphics.drawCenteredString(font, Component.literal(progressText), 0, 0, PROGRESS_TEXT_COLOR);
        guiGraphics.pose().popPose();
    }

    private String formatProgress(int completed, int total) {
        int percentage = total > 0 ? Math.round((completed * 100f) / total) : 0;
        return completed + "/" + total + " (" + percentage + "%)";
    }

    private void renderContentHeader(GuiGraphics guiGraphics, int contentX, boolean searching) {
        Component headerTitle;
        Component headerDescription;

        if (selectedRoot != null && selectedRoot.holder().value().display().isPresent()) {
            DisplayInfo rootDisplay = selectedRoot.holder().value().display().get();
            headerTitle = rootDisplay.getTitle();
            headerDescription = rootDisplay.getDescription();
        } else if (selectedRoot == null) {
            headerTitle = Component.translatable(Constants.MOD_ID + ".gui.global_category.title");
            headerDescription = Component.translatable(Constants.MOD_ID + ".gui.global_category.desc");
        } else {
            return;
        }

        int total = cachedCards.size();
        int completed = 0;
        for (AdvancementCard card : cachedCards) {
            AdvancementProgress progress = progressMap.get(card.getNode());
            if (progress != null && progress.isDone()) {
                completed++;
            }
        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(1.2f, 1.2f, 1.2f);
        guiGraphics.drawString(font, headerTitle, (int) (contentX / 1.2f), (int) ((ScreenMetrics.TOP_BAR_HEIGHT + 10) / 1.2f), HEADER_TITLE_COLOR, true);
        guiGraphics.pose().popPose();

        int barY = ScreenMetrics.TOP_BAR_HEIGHT + 18;
        int currentRightX = width - ScreenMetrics.CONTENT_MARGIN;

        if (total > 0) {
            currentRightX = renderHeaderProgressBar(guiGraphics, completed, total, currentRightX, barY, false);
        }

        int trackedTotal = TRACKED_ADVANCEMENTS.size();
        if (trackedTotal >= 2) {
            int trackedCompleted = calculateTrackedCompleted();
            if (total > 0) currentRightX -= 24;
            renderHeaderProgressBar(guiGraphics, trackedCompleted, trackedTotal, currentRightX, barY, true);
        }

        guiGraphics.drawString(font, headerDescription, contentX, ScreenMetrics.TOP_BAR_HEIGHT + 28, HEADER_DESCRIPTION_COLOR, true);
        guiGraphics.fill(contentX, ScreenMetrics.TOP_BAR_HEIGHT + 44, width - ScreenMetrics.CONTENT_MARGIN, ScreenMetrics.TOP_BAR_HEIGHT + 45, HEADER_DIVIDER_COLOR);
    }

    private int renderHeaderProgressBar(GuiGraphics guiGraphics, int completed, int total, int rightX, int barY, boolean isTracked) {
        float percentage = total > 0 ? (float) completed / total : 0f;

        int barWidth = 80;
        int barHeight = 7;
        int barX = rightX - barWidth;

        String progressText;
        if (isTracked) {
            String prefix = Component.translatable(Constants.MOD_ID + ".gui.progressbar.tracked.prefix").getString();
            progressText = prefix + formatProgress(completed, total);
        } else {
            progressText = formatProgress(completed, total);
        }

        int textWidth = font.width(progressText);
        int textX = barX + barWidth - textWidth;
        int textY = barY - 2 - barHeight;

        int percentColor = isTracked ? TRACKED_PERCENTAGE_COLOR : HEADER_PERCENTAGE_COLOR;
        int fillStart = isTracked ? TRACKED_FILL_START : PROGRESS_FILL_START;
        int fillEnd = isTracked ? TRACKED_FILL_END : PROGRESS_FILL_END;

        guiGraphics.drawString(font, progressText, textX, textY, percentColor, true);
        guiGraphics.fill(barX - 1, barY - 1, barX + barWidth + 1, barY + barHeight + 1, PROGRESS_TRACK_BORDER);
        guiGraphics.fill(barX, barY, barX + barWidth, barY + barHeight, PROGRESS_TRACK_FILL);

        if (completed > 0) {
            int fillWidth = (int) (barWidth * percentage);
            guiGraphics.fillGradient(barX, barY, barX + fillWidth, barY + barHeight, fillStart, fillEnd);
        }

        return barX;
    }

    private HoverResult renderCardList(GuiGraphics guiGraphics, double scaleFactor, int contentX, int contentWidth,
                                       int viewportY, int viewportHeight, int scaledMouseX, int scaledMouseY) {
        int scissorX1 = (int) Math.round(contentX / scaleFactor);
        int scissorY1 = (int) Math.round(viewportY / scaleFactor);
        int scissorX2 = (int) Math.round((width - ScreenMetrics.CONTENT_MARGIN) / scaleFactor);
        int scissorY2 = (int) Math.round((viewportY + viewportHeight) / scaleFactor);
        guiGraphics.enableScissor(scissorX1, scissorY1, scissorX2, scissorY2);

        int cardY = viewportY - (int) scrollOffset;
        for (AdvancementCard card : cachedCards) {
            if (isCardVisible(cardY, card, viewportY, viewportHeight)) {
                card.renderBackgroundAndText(guiGraphics, font, contentX, cardY, contentWidth, scaledMouseX, scaledMouseY, viewportY, viewportHeight, filterDropdown.isOpen());
            }
            cardY += card.getHeight() + ScreenMetrics.CARD_SPACING;
        }

        ItemStack hoveredIcon = null;
        String hoveredCriterionTag = null;

        cardY = viewportY - (int) scrollOffset;
        for (AdvancementCard card : cachedCards) {
            if (isCardVisible(cardY, card, viewportY, viewportHeight)) {
                card.renderIcon(guiGraphics, contentX, cardY);

                ItemStack possibleHover = card.getHoveredIcon(scaledMouseX, scaledMouseY, contentX, cardY, viewportY, viewportHeight, filterDropdown.isOpen());
                if (possibleHover != null) {
                    hoveredIcon = possibleHover;
                }

                String possibleTag = card.getHoveredCriterionTag(font, scaledMouseX, scaledMouseY, contentX, cardY, viewportY, viewportHeight, filterDropdown.isOpen());
                if (possibleTag != null) {
                    hoveredCriterionTag = possibleTag;
                }
            }
            cardY += card.getHeight() + ScreenMetrics.CARD_SPACING;
        }

        guiGraphics.disableScissor();
        return new HoverResult(hoveredIcon, hoveredCriterionTag);
    }

    private boolean isCardVisible(int cardY, AdvancementCard card, int viewportY, int viewportHeight) {
        return cardY + card.getHeight() > viewportY && cardY < viewportY + viewportHeight;
    }

    private void renderMainScrollbar(GuiGraphics guiGraphics, int viewportY, int viewportHeight) {
        if (maxScroll <= 0) {
            return;
        }

        int scrollbarX = width - ScreenMetrics.SCROLLBAR_RIGHT_MARGIN;
        int thumbHeight = scrollThumbHeight(viewportHeight);
        int thumbY = scrollThumbY(viewportY, viewportHeight, thumbHeight);

        guiGraphics.fill(scrollbarX, viewportY, scrollbarX + ScreenMetrics.SCROLLBAR_WIDTH, viewportY + viewportHeight, SCROLLBAR_TRACK_COLOR);
        int thumbColor = draggingMainScrollbar ? SCROLLBAR_THUMB_ACTIVE : SCROLLBAR_THUMB_IDLE;
        guiGraphics.fill(scrollbarX, thumbY, scrollbarX + ScreenMetrics.SCROLLBAR_WIDTH, thumbY + thumbHeight, thumbColor);
    }

    private void renderHoverTooltip(GuiGraphics guiGraphics, HoverResult hover, int mouseX, int mouseY) {
        if (hover.icon() != null) {
            guiGraphics.renderTooltip(font, hover.icon(), mouseX, mouseY);
        } else if (hover.criterionTag() != null) {
            guiGraphics.renderTooltip(font, Component.literal(hover.criterionTag()), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double scaleFactor = getScaleFactor();
        mouseX *= scaleFactor;
        mouseY *= scaleFactor;

        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        if (filterDropdown.isOpen()) {
            filterDropdown.mouseClickedOptions(mouseX, mouseY);
            return true;
        }

        if (mouseX <= ScreenMetrics.SIDEBAR_WIDTH) {
            if (handleSidebarClick(mouseX, mouseY)) {
                return true;
            }
        } else if (handleContentClick(mouseX, mouseY)) {
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean handleSidebarClick(double mouseX, double mouseY) {
        int sidebarViewportBottom = height - ScreenMetrics.SIDEBAR_PROGRESS_HEIGHT;
        if (mouseY > sidebarViewportBottom) {
            return false;
        }

        int rowY = ScreenMetrics.SIDEBAR_TOP_PADDING - (int) sidebarScroll;
        for (SidebarNodeCache cache : cachedSidebarNodes) {
            if (mouseY >= rowY && mouseY <= rowY + ScreenMetrics.SIDEBAR_ITEM_HEIGHT) {
                selectSidebarNode(cache);
                return true;
            }
            rowY += ScreenMetrics.SIDEBAR_ROW_HEIGHT;
        }
        return false;
    }

    private void selectSidebarNode(SidebarNodeCache cache) {
        if (cache.node == null) {
            clientAdvancements.setSelectedTab(null, true);
            lastSelectedTabId = null;
            selectedRoot = null;
        } else {
            clientAdvancements.setSelectedTab(cache.node.holder(), true);
            selectedRoot = cache.node;
            lastSelectedTabId = cache.node.holder().id();
        }

        scrollOffset = 0;
        needsRecalculation = true;
    }

    private boolean handleContentClick(double mouseX, double mouseY) {
        boolean searching = isSearching();
        int viewportY = ScreenMetrics.viewportY(searching);
        int viewportHeight = viewportHeight(viewportY);
        int contentX = ScreenMetrics.contentX();
        int contentWidth = ScreenMetrics.contentWidth(width);

        if (mouseX >= contentX && mouseX <= contentX + contentWidth && mouseY >= viewportY && mouseY <= viewportY + viewportHeight
                && handleCardClick(mouseX, mouseY, contentX, contentWidth, viewportY, viewportHeight)) {
            return true;
        }

        return handleScrollbarGrab(mouseX, mouseY, viewportY, viewportHeight);
    }

    private boolean handleCardClick(double mouseX, double mouseY, int contentX, int contentWidth, int viewportY, int viewportHeight) {
        int cardY = viewportY - (int) scrollOffset;
        for (AdvancementCard card : cachedCards) {
            if (mouseY >= cardY && mouseY <= cardY + card.getHeight()) {
                return handleCardInteraction(card, mouseX, mouseY, contentX, contentWidth, cardY, viewportY, viewportHeight);
            }
            cardY += card.getHeight() + ScreenMetrics.CARD_SPACING;
        }
        return false;
    }

    private boolean handleCardInteraction(AdvancementCard card, double mouseX, double mouseY, int contentX, int contentWidth,
                                          int cardY, int viewportY, int viewportHeight) {
        if (card.isTrackIconHovered(mouseX, mouseY, contentX, cardY, contentWidth, viewportY, viewportHeight, filterDropdown.isOpen())) {
            toggleTracked(card);
            return true;
        }

        if (card.isExpanded()) {
            int trackButtonX = contentX + 40;
            int trackButtonY = cardY + card.getBaseHeight() + 4;
            if (mouseX >= trackButtonX && mouseX <= trackButtonX + 70 && mouseY >= trackButtonY && mouseY <= trackButtonY + 12) {
                toggleTracked(card);
                return true;
            }
        }

        expandedNode = (expandedNode == card.getNode()) ? null : card.getNode();
        needsRecalculation = true;
        return true;
    }

    private void toggleTracked(AdvancementCard card) {
        String id = card.getNode().holder().id().toString();
        if (!TRACKED_ADVANCEMENTS.remove(id)) {
            TRACKED_ADVANCEMENTS.add(id);
        }
        needsRecalculation = true;
    }

    private boolean handleScrollbarGrab(double mouseX, double mouseY, int viewportY, int viewportHeight) {
        if (maxScroll <= 0) {
            return false;
        }

        int scrollbarX = width - ScreenMetrics.SCROLLBAR_RIGHT_MARGIN;
        if (mouseX < scrollbarX - 2 || mouseX > scrollbarX + 5) {
            return false;
        }

        int thumbHeight = scrollThumbHeight(viewportHeight);
        int thumbY = scrollThumbY(viewportY, viewportHeight, thumbHeight);

        if (mouseY >= thumbY && mouseY <= thumbY + thumbHeight) {
            draggingMainScrollbar = true;
            dragClickOffset = mouseY - thumbY;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        double scaleFactor = getScaleFactor();
        mouseY *= scaleFactor;

        if (draggingMainScrollbar && maxScroll > 0) {
            boolean searching = isSearching();
            int viewportY = ScreenMetrics.viewportY(searching);
            int viewportHeight = viewportHeight(viewportY);
            int thumbHeight = scrollThumbHeight(viewportHeight);
            int trackHeight = viewportHeight - thumbHeight;

            if (trackHeight > 0) {
                double targetThumbY = mouseY - dragClickOffset;
                double scrollPercentage = Mth.clamp((targetThumbY - viewportY) / trackHeight, 0.0, 1.0);
                scrollOffset = scrollPercentage * maxScroll;
            }
            return true;
        }
        return super.mouseDragged(mouseX * scaleFactor, mouseY, button, dragX * scaleFactor, dragY * scaleFactor);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        double scaleFactor = getScaleFactor();
        if (button == 0) {
            draggingMainScrollbar = false;
        }
        return super.mouseReleased(mouseX * scaleFactor, mouseY * scaleFactor, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        double scaleFactor = getScaleFactor();
        mouseX *= scaleFactor;

        if (filterDropdown.isOpen()) {
            filterDropdown.close();
            return true;
        }

        if (mouseX <= ScreenMetrics.SIDEBAR_WIDTH) {
            sidebarScroll = Mth.clamp(sidebarScroll - (scrollY * 20), 0.0, maxSidebarScroll);
            return true;
        }
        if (maxScroll > 0) {
            scrollOffset = Mth.clamp(scrollOffset - (scrollY * 30), 0.0, maxScroll);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY * scaleFactor, scrollX, scrollY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private record HoverResult(@Nullable ItemStack icon, @Nullable String criterionTag) {
    }
}