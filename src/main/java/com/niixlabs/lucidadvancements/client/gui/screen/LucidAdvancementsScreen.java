package com.niixlabs.lucidadvancements.client.gui.screen;

import com.niixlabs.lucidadvancements.Constants;
import com.niixlabs.lucidadvancements.client.cache.TrackedAdvancementsCache;
import com.niixlabs.lucidadvancements.client.gui.card.AdvancementCard;
import com.niixlabs.lucidadvancements.client.gui.card.FilterMode;
import com.niixlabs.lucidadvancements.client.gui.config.LucidConfigScreen;
import com.niixlabs.lucidadvancements.client.gui.sidebar.SidebarNodeCache;
import com.niixlabs.lucidadvancements.client.gui.util.GuiScale;
import com.niixlabs.lucidadvancements.client.gui.util.LucidScrollHandler;
import com.niixlabs.lucidadvancements.config.LucidConfig;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class LucidAdvancementsScreen extends Screen implements ClientAdvancements.Listener {
    private final ClientAdvancements clientAdvancements;
    private final List<AdvancementNode> rootNodes = new ArrayList<>();
    private final Map<AdvancementNode, AdvancementProgress> progressMap = new HashMap<>();
    private final List<SidebarNodeCache> cachedSidebarNodes = new ArrayList<>();
    private final List<Renderable> customRenderables = new ArrayList<>();
    private final List<AdvancementCard> cachedCards = new ArrayList<>();
    private final Map<AdvancementNode, List<AdvancementNode>> flattenedTasksCache = new HashMap<>();

    public static @Nullable Identifier advancementToFocusOnOpen = null;
    public static @Nullable Identifier lastSelectedTabId = null;
    public static String lastSearchQuery = "";
    public static final Set<String> TRACKED_ADVANCEMENTS = new HashSet<>();
    private static boolean configLoaded = false;

    private EditBox searchBox;
    private FilterMode currentFilter = FilterMode.ALL;
    private LucidDropdown<FilterMode> filterDropdown;
    private LucidDropdown<Integer> scaleDropdown;

    private AdvancementNode expandedNode = null;
    private AdvancementNode selectedRoot = null;

    private final LucidScrollHandler mainScroll = new LucidScrollHandler();
    private final LucidScrollHandler sidebarScroll = new LucidScrollHandler();

    private int totalAdvancements = 0;
    private int completedAdvancements = 0;
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
        return height - viewportY - ScreenMetrics.viewportBottomMargin();
    }

    private boolean isDropdownOpen() {
        return filterDropdown.isOpen() || scaleDropdown.isOpen();
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
            this.width = (int) Math.ceil(minecraft.getWindow().getScreenWidth() / targetScale);
            this.height = (int) Math.ceil(minecraft.getWindow().getScreenHeight() / targetScale);
            TrackedAdvancementsCache.syncIfNeeded(minecraft);
        }

        super.init();
        customRenderables.clear();
        rootNodes.clear();
        progressMap.clear();

        initTopBarWidgets();

        Identifier savedTab = lastSelectedTabId;
        clientAdvancements.setListener(this);
        lastSelectedTabId = savedTab;
        needsRecalculation = true;
        rebuildSidebarCache();

        checkFocus();
    }

    private void initTopBarWidgets() {
        int currentX = width - ScreenMetrics.contentMargin();

        int configWidth = 16;
        currentX -= configWidth;
        LucidButton configButton = new LucidButton(currentX, 16, configWidth, 16,
                Component.literal("⚙"), btn -> {
            if (minecraft != null) {
                minecraft.setScreen(new LucidConfigScreen(this));
            }
        });
        addRenderableWidget(configButton);

        int clearWidth = 16;
        currentX -= (clearWidth + 3);
        LucidButton clearTrackedButton = new LucidButton(currentX, 16, clearWidth, 16,
                Component.literal(LucidConfig.cardIconTracked), btn -> {
            TRACKED_ADVANCEMENTS.clear();
            TrackedAdvancementsCache.persist();
            needsRecalculation = true;

            //TranslationExporter.exportCurrentAdvancements(progressMap);
        });
        addRenderableWidget(clearTrackedButton);

        if (!LucidConfig.keepSearchQuery) lastSearchQuery = "";

        int searchWidth = 120;
        currentX -= (searchWidth + 6);
        searchBox = new EditBox(font, currentX, 16, searchWidth, 16, Component.translatable(Constants.MOD_ID + ".gui.search.placeholder"));
        searchBox.setValue(lastSearchQuery);
        searchBox.setSuggestion(lastSearchQuery.isEmpty() ? Component.translatable(Constants.MOD_ID + ".gui.search.placeholder").getString() : null);
        searchBox.setResponder(text -> {
            lastSearchQuery = text;
            searchBox.setSuggestion(text.isEmpty() ? Component.translatable(Constants.MOD_ID + ".gui.search.placeholder").getString() : null);
            mainScroll.setScrollOffset(0);
            needsRecalculation = true;
        });
        addRenderableWidget(searchBox);

        int filterWidth = 110;
        currentX -= (filterWidth + 6);
        filterDropdown = new LucidDropdown<>(currentX, 16, filterWidth, 16, currentFilter,
                List.of(FilterMode.values()), FilterMode::getDisplayName, mode -> {
            currentFilter = mode;
            mainScroll.setScrollOffset(0);
            needsRecalculation = true;
        });
        addRenderableWidget(filterDropdown);

        int scaleWidth = 40;
        currentX -= (scaleWidth + 6);
        List<Integer> scaleOptions = minecraft != null ? GuiScale.supportedScaleOptions(minecraft) : List.of(0);
        int initialScale = scaleOptions.contains(LucidConfig.customGuiScale) ? LucidConfig.customGuiScale : 0;
        scaleDropdown = new LucidDropdown<>(currentX, 16, scaleWidth, 16, initialScale, scaleOptions,
                this::scaleModeLabel, scale -> {
            LucidConfig.updateAndSave("customGuiScale", scale);
            if (minecraft != null) init();
        });
        addRenderableWidget(scaleDropdown);
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

        Identifier targetId = advancementToFocusOnOpen;
        advancementToFocusOnOpen = null;

        AdvancementNode targetRoot = findRootContaining(targetId);
        if (targetRoot == null) {
            return;
        }

        AdvancementNode targetNode = targetId.equals(targetRoot.holder().id()) ? targetRoot : findNode(targetRoot, targetId);

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
    private AdvancementNode findRootContaining(Identifier targetId) {
        for (AdvancementNode root : rootNodes) {
            if (root.holder().id().equals(targetId) || findNode(root, targetId) != null) {
                return root;
            }
        }
        return null;
    }

    @Nullable
    private AdvancementNode findNode(AdvancementNode root, Identifier targetId) {
        for (AdvancementNode node : collectTasks(root)) {
            if (node.holder().id().equals(targetId)) {
                return node;
            }
        }
        return null;
    }

    private void scrollToCard(AdvancementNode targetNode, AdvancementNode targetRoot) {
        if (targetNode == targetRoot) {
            mainScroll.setScrollOffset(0);
            return;
        }

        int yOffset = 0;
        for (AdvancementCard card : cachedCards) {
            if (card.getNode() == targetNode) {
                int viewportY = ScreenMetrics.viewportY(false);
                int viewportHeight = viewportHeight(viewportY);
                int halfCard = card.getHeight() / 2;
                int halfViewport = viewportHeight / 2;
                mainScroll.setScrollOffset(yOffset + halfCard - halfViewport);
                return;
            }
            yOffset += card.getHeight() + ScreenMetrics.cardSpacing();
        }
    }

    private void scrollSidebarToSelection() {
        int rootIndex = rootNodes.indexOf(selectedRoot);
        if (rootIndex == -1) {
            return;
        }

        int sidebarViewport = height - ScreenMetrics.sidebarProgressHeight() - 24;
        sidebarScroll.updateMaxScroll(Math.max(0, (cachedSidebarNodes.size() * ScreenMetrics.sidebarRowHeight()) - sidebarViewport));

        int targetScroll = (rootIndex * ScreenMetrics.sidebarRowHeight()) + (ScreenMetrics.sidebarRowHeight() / 2) - (sidebarViewport / 2);
        sidebarScroll.setScrollOffset(targetScroll);
    }

    private void rebuildSidebarCache() {
        cachedSidebarNodes.clear();
        if (font == null) {
            return;
        }

        int maxTextWidth = (int) ((ScreenMetrics.sidebarWidth() - 32) / 0.85f);

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

        mainScroll.setScrollOffset(0);
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
            mainScroll.updateMaxScroll(0);
            needsRecalculation = false;
            return;
        }

        int contentWidth = ScreenMetrics.contentWidth(width);
        String query = searchBox.getValue().toLowerCase();
        boolean searching = !query.isEmpty();

        Set<String> modIdFilters = new HashSet<>();
        StringBuilder textFilterBuilder = new StringBuilder();

        for (String token : query.split(" ")) {
            if (token.startsWith("@") && token.length() > 1) {
                modIdFilters.add(token.substring(1));
            } else if (!token.isEmpty()) {
                textFilterBuilder.append(token).append(' ');
            }
        }

        String textFilter = textFilterBuilder.toString().trim();
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

            if (searching && !matchesSearch(child, card, modIdFilters, textFilter)) {
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
            totalCardsHeight += card.getHeight() + ScreenMetrics.cardSpacing();
        }

        int viewportY = ScreenMetrics.viewportY(searching);
        mainScroll.updateMaxScroll(totalCardsHeight - viewportHeight(viewportY));

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

    private boolean matchesSearch(AdvancementNode child, AdvancementCard card, Set<String> modIdFilters, String textFilter) {
        if (!modIdFilters.isEmpty()) {
            String namespace = child.holder().id().getNamespace().toLowerCase();
            boolean matchesAnyMod = modIdFilters.stream().anyMatch(namespace::contains);
            if (!matchesAnyMod) {
                return false;
            }
        }

        if (textFilter.isEmpty()) {
            return true;
        }

        return card.cachedSearchTitle.contains(textFilter)
                || card.cachedSearchDesc.contains(textFilter)
                || card.cachedSearchCategory.contains(textFilter);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        double scaleFactor = getScaleFactor();
        int scaledMouseX = (int) (mouseX * scaleFactor);
        int scaledMouseY = (int) (mouseY * scaleFactor);

        guiGraphics.pose().pushMatrix();
        if (scaleFactor != 1.0) {
            float invScale = (float) (1.0 / scaleFactor);
            guiGraphics.pose().scale(invScale, invScale);
        }

        guiGraphics.fill(0, 0, width, height, LucidConfig.screenBackdropColor);

        if (needsRecalculation) {
            recalculateCards();
        }

        for (Renderable renderable : customRenderables) {
            renderable.extractRenderState(guiGraphics, scaledMouseX, scaledMouseY, partialTick);
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
        mainScroll.renderScrollbar(guiGraphics, width, viewportY, viewportHeight);

        filterDropdown.renderOptions(guiGraphics, scaledMouseX, scaledMouseY);
        scaleDropdown.renderOptions(guiGraphics, scaledMouseX, scaledMouseY);

        guiGraphics.pose().popMatrix();

        renderHoverTooltip(guiGraphics, hover, mouseX, mouseY);
    }

    private void renderTopBar(GuiGraphicsExtractor guiGraphics) {
        int contentX = ScreenMetrics.contentX();
        guiGraphics.fillGradient(ScreenMetrics.sidebarWidth(), 0, width, ScreenMetrics.topBarHeight(), LucidConfig.screenTopBarGradientStart, LucidConfig.screenTopBarGradientEnd);
        guiGraphics.fill(ScreenMetrics.sidebarWidth(), ScreenMetrics.topBarHeight() - 1, width, ScreenMetrics.topBarHeight(), LucidConfig.screenTopBarBorder);
        guiGraphics.text(font, Component.translatable("key.advancements"), contentX, 20, LucidConfig.screenSidebarTextSelected, true);
    }

    private void renderSidebar(GuiGraphicsExtractor guiGraphics, double scaleFactor, int scaledMouseX, int scaledMouseY) {
        int sidebarWidth = ScreenMetrics.sidebarWidth();
        int viewportBottom = height - ScreenMetrics.sidebarProgressHeight();

        guiGraphics.fillGradient(0, 0, sidebarWidth, height, LucidConfig.screenSidebarGradientStart, LucidConfig.screenSidebarGradientEnd);
        guiGraphics.fill(sidebarWidth - 1, 0, sidebarWidth, height, LucidConfig.screenSidebarBorder);

        sidebarScroll.updateMaxScroll((cachedSidebarNodes.size() * ScreenMetrics.sidebarRowHeight()) - (viewportBottom - ScreenMetrics.sidebarTopPadding()));

        int scissorX2 = (int) Math.round(sidebarWidth / scaleFactor);
        int scissorY2 = (int) Math.round((height - ScreenMetrics.sidebarProgressHeight()) / scaleFactor);
        guiGraphics.enableScissor(0, 0, scissorX2, scissorY2);

        int rowY = ScreenMetrics.sidebarTopPadding() - (int) sidebarScroll.getScrollOffset();

        for (SidebarNodeCache cache : cachedSidebarNodes) {
            if (rowY + ScreenMetrics.sidebarRowHeight() > 0 && rowY < viewportBottom) {
                renderSidebarRow(guiGraphics, cache, rowY, sidebarWidth, scaledMouseX, scaledMouseY, viewportBottom);
                int iconOffsetY = (ScreenMetrics.sidebarRowHeight() - 16) / 2;
                guiGraphics.item(cache.icon, 8, rowY + iconOffsetY);
            }
            rowY += ScreenMetrics.sidebarRowHeight();
        }

        guiGraphics.disableScissor();
    }

    private void renderSidebarRow(GuiGraphicsExtractor guiGraphics, SidebarNodeCache cache, int rowY, int sidebarWidth, int scaledMouseX, int scaledMouseY, int sidebarViewportBottom) {
        boolean selected = cache.node == selectedRoot;

        int itemOffsetY = (ScreenMetrics.sidebarRowHeight() - ScreenMetrics.sidebarItemHeight()) / 2;
        int itemStartY = rowY + itemOffsetY;
        int itemEndY = itemStartY + ScreenMetrics.sidebarItemHeight();

        if (selected) {
            guiGraphics.fill(4, itemStartY, sidebarWidth - 4, itemEndY, LucidConfig.screenSidebarSelectedFill);
            guiGraphics.fill(4, itemStartY, 6, itemEndY, LucidConfig.screenSidebarSelectedAccent);
        } else if (scaledMouseY <= sidebarViewportBottom && scaledMouseX >= 4 && scaledMouseX <= sidebarWidth - 4
                && scaledMouseY >= itemStartY && scaledMouseY <= itemEndY) {
            guiGraphics.fill(4, itemStartY, sidebarWidth - 4, itemEndY, LucidConfig.screenSidebarHoverFill);
        }

        int textOffsetY = (ScreenMetrics.sidebarRowHeight() - 8) / 2;

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(28, rowY + textOffsetY);
        guiGraphics.pose().scale(0.85f, 0.85f);
        guiGraphics.text(font, cache.displayTitle, 0, 0, selected ? LucidConfig.screenSidebarTextSelected : LucidConfig.screenSidebarTextIdle, true);
        guiGraphics.pose().popMatrix();
    }

    private void renderProgressBar(GuiGraphicsExtractor guiGraphics) {
        int sidebarWidth = ScreenMetrics.sidebarWidth();
        int progressAreaY = height - ScreenMetrics.sidebarProgressHeight();

        guiGraphics.fillGradient(0, progressAreaY, sidebarWidth - 1, height, LucidConfig.screenProgressBarGradientStart, LucidConfig.screenProgressBarGradientEnd);
        guiGraphics.fill(0, progressAreaY, sidebarWidth - 1, progressAreaY + 1, LucidConfig.screenProgressBarTopBorder);

        if (totalAdvancements <= 0) {
            return;
        }

        float percentage = (float) completedAdvancements / totalAdvancements;
        int barWidth = sidebarWidth - 16;
        int barX = 8;
        int barY = height - 14;

        guiGraphics.fill(barX - 1, barY - 1, barX + barWidth + 1, barY + 7, LucidConfig.screenProgressTrackBorder);
        guiGraphics.fill(barX, barY, barX + barWidth, barY + 6, LucidConfig.screenProgressTrackFill);
        guiGraphics.fillGradient(barX, barY, barX + (int) (barWidth * percentage), barY + 6, LucidConfig.screenProgressFillStart, LucidConfig.screenProgressFillEnd);

        String progressText = Component.translatable(Constants.MOD_ID + ".gui.progress_text", completedAdvancements, totalAdvancements).getString();
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(sidebarWidth / 2f, barY - 11);
        guiGraphics.pose().scale(0.82f, 0.82f);
        guiGraphics.centeredText(font, Component.literal(progressText), 0, 0, LucidConfig.screenProgressTextColor);
        guiGraphics.pose().popMatrix();
    }

    private String formatProgress(int completed, int total) {
        int percentage = total > 0 ? Math.round((completed * 100f) / total) : 0;
        return completed + "/" + total + " (" + percentage + "%)";
    }

    private void renderContentHeader(GuiGraphicsExtractor guiGraphics, int contentX, boolean searching) {
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

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().scale(1.2f, 1.2f);
        guiGraphics.text(font, headerTitle, (int) (contentX / 1.2f), (int) ((ScreenMetrics.topBarHeight() + 10) / 1.2f), LucidConfig.screenHeaderTitleColor, true);
        guiGraphics.pose().popMatrix();

        int barY = ScreenMetrics.topBarHeight() + 18;
        int currentRightX = width - ScreenMetrics.contentMargin();

        if (total > 0) {
            currentRightX = renderHeaderProgressBar(guiGraphics, completed, total, currentRightX, barY, false);
        }

        int trackedTotal = TRACKED_ADVANCEMENTS.size();
        if (trackedTotal >= 2) {
            int trackedCompleted = calculateTrackedCompleted();
            if (total > 0) currentRightX -= 24;
            renderHeaderProgressBar(guiGraphics, trackedCompleted, trackedTotal, currentRightX, barY, true);
        }

        guiGraphics.text(font, headerDescription, contentX, ScreenMetrics.topBarHeight() + 28, LucidConfig.screenHeaderDescriptionColor, true);
        guiGraphics.fill(contentX, ScreenMetrics.topBarHeight() + 44, width - ScreenMetrics.contentMargin(), ScreenMetrics.topBarHeight() + 45, LucidConfig.screenHeaderDividerColor);
    }

    private int renderHeaderProgressBar(GuiGraphicsExtractor guiGraphics, int completed, int total, int rightX, int barY, boolean isTracked) {
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

        int percentColor = isTracked ? LucidConfig.screenTrackedPercentageColor : LucidConfig.screenHeaderPercentageColor;
        int fillStart = isTracked ? LucidConfig.screenTrackedFillStart : LucidConfig.screenProgressFillStart;
        int fillEnd = isTracked ? LucidConfig.screenTrackedFillEnd : LucidConfig.screenProgressFillEnd;

        guiGraphics.text(font, progressText, textX, textY, percentColor, true);
        guiGraphics.fill(barX - 1, barY - 1, barX + barWidth + 1, barY + barHeight + 1, LucidConfig.screenProgressTrackBorder);
        guiGraphics.fill(barX, barY, barX + barWidth, barY + barHeight, LucidConfig.screenProgressTrackFill);

        if (completed > 0) {
            int fillWidth = (int) (barWidth * percentage);
            guiGraphics.fillGradient(barX, barY, barX + fillWidth, barY + barHeight, fillStart, fillEnd);
        }

        return barX;
    }

    private HoverResult renderCardList(GuiGraphicsExtractor guiGraphics, double scaleFactor, int contentX, int contentWidth,
                                       int viewportY, int viewportHeight, int scaledMouseX, int scaledMouseY) {
        int scissorX1 = (int) Math.round(contentX / scaleFactor);
        int scissorY1 = (int) Math.round(viewportY / scaleFactor);
        int scissorX2 = (int) Math.round((width - ScreenMetrics.contentMargin()) / scaleFactor);
        int scissorY2 = (int) Math.round((viewportY + viewportHeight) / scaleFactor);
        guiGraphics.enableScissor(scissorX1, scissorY1, scissorX2, scissorY2);

        boolean isBlocked = isDropdownOpen();

        int cardY = viewportY - (int) mainScroll.getScrollOffset();
        for (AdvancementCard card : cachedCards) {
            if (isCardVisible(cardY, card, viewportY, viewportHeight)) {
                card.renderBackgroundAndText(guiGraphics, font, contentX, cardY, contentWidth, scaledMouseX, scaledMouseY, viewportY, viewportHeight, isBlocked);
            }
            cardY += card.getHeight() + ScreenMetrics.cardSpacing();
        }

        ItemStack hoveredIcon = null;
        String hoveredCriterionTag = null;

        cardY = viewportY - (int) mainScroll.getScrollOffset();
        for (AdvancementCard card : cachedCards) {
            if (isCardVisible(cardY, card, viewportY, viewportHeight)) {
                card.renderIcon(guiGraphics, contentX, cardY);

                ItemStack possibleHover = card.getHoveredIcon(scaledMouseX, scaledMouseY, contentX, cardY, viewportY, viewportHeight, isBlocked);
                if (possibleHover != null) {
                    hoveredIcon = possibleHover;
                }

                String possibleTag = card.getHoveredCriterionTag(font, scaledMouseX, scaledMouseY, contentX, cardY, viewportY, viewportHeight, isBlocked);
                if (possibleTag != null) {
                    hoveredCriterionTag = possibleTag;
                }
            }
            cardY += card.getHeight() + ScreenMetrics.cardSpacing();
        }

        guiGraphics.disableScissor();
        return new HoverResult(hoveredIcon, hoveredCriterionTag);
    }

    private boolean isCardVisible(int cardY, AdvancementCard card, int viewportY, int viewportHeight) {
        return cardY + card.getHeight() > viewportY && cardY < viewportY + viewportHeight;
    }

    private void renderHoverTooltip(GuiGraphicsExtractor guiGraphics, HoverResult hover, int mouseX, int mouseY) {
        /*if (hover.icon() != null) {
            guiGraphics.renderTooltip(font, hover.icon(), mouseX, mouseY);
        } else if (hover.criterionTag() != null) {
            guiGraphics.renderTooltip(font, Component.literal(hover.criterionTag()), mouseX, mouseY);
        }*/
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();

        double scaleFactor = getScaleFactor();
        mouseX *= scaleFactor;
        mouseY *= scaleFactor;

        if (button != 0) {
            return super.mouseClicked(event, doubleClick);
        }

        if (filterDropdown.isOpen()) {
            filterDropdown.mouseClickedOptions(mouseX, mouseY);
            return true;
        }

        if (scaleDropdown.isOpen()) {
            scaleDropdown.mouseClickedOptions(mouseX, mouseY);
            return true;
        }

        if (mouseX <= ScreenMetrics.sidebarWidth()) {
            if (handleSidebarClick(mouseX, mouseY)) {
                return true;
            }
        } else if (handleContentClick(mouseX, mouseY)) {
            return true;
        }

        return super.mouseClicked(event, doubleClick);
    }

    private boolean handleSidebarClick(double mouseX, double mouseY) {
        int sidebarViewportBottom = height - ScreenMetrics.sidebarProgressHeight();
        if (mouseY > sidebarViewportBottom) {
            return false;
        }

        int rowY = ScreenMetrics.sidebarTopPadding() - (int) sidebarScroll.getScrollOffset();
        for (SidebarNodeCache cache : cachedSidebarNodes) {
            if (mouseY >= rowY && mouseY <= rowY + ScreenMetrics.sidebarItemHeight()) {
                selectSidebarNode(cache);
                return true;
            }
            rowY += ScreenMetrics.sidebarRowHeight();
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

        mainScroll.setScrollOffset(0);
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

        return mainScroll.handleMouseDown(mouseX, mouseY, width, viewportY, viewportHeight);
    }

    private boolean handleCardClick(double mouseX, double mouseY, int contentX, int contentWidth, int viewportY, int viewportHeight) {
        int cardY = viewportY - (int) mainScroll.getScrollOffset();
        for (AdvancementCard card : cachedCards) {
            if (mouseY >= cardY && mouseY <= cardY + card.getHeight()) {
                return handleCardInteraction(card, mouseX, mouseY, contentX, contentWidth, cardY, viewportY, viewportHeight);
            }
            cardY += card.getHeight() + ScreenMetrics.cardSpacing();
        }
        return false;
    }

    private boolean handleCardInteraction(AdvancementCard card, double mouseX, double mouseY, int contentX, int contentWidth,
                                          int cardY, int viewportY, int viewportHeight) {
        if (card.isTrackIconHovered(mouseX, mouseY, contentX, cardY, contentWidth, viewportY, viewportHeight, isDropdownOpen())) {
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
        TrackedAdvancementsCache.persist();
        needsRecalculation = true;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();

        double scaleFactor = getScaleFactor();
        mouseY *= scaleFactor;

        boolean searching = isSearching();
        int viewportY = ScreenMetrics.viewportY(searching);
        int viewportHeight = viewportHeight(viewportY);

        if (mainScroll.handleMouseDragged(mouseY, viewportY, viewportHeight)) {
            return true;
        }

        return super.mouseDragged(event, dragX * scaleFactor, dragY * scaleFactor);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        double scaleFactor = getScaleFactor();
        if (event.button() == 0) {
            mainScroll.setDragging(false);
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        double scaleFactor = getScaleFactor();
        mouseX *= scaleFactor;

        if (isDropdownOpen()) {
            filterDropdown.close();
            scaleDropdown.close();
            return true;
        }

        if (mouseX <= ScreenMetrics.sidebarWidth()) {
            sidebarScroll.handleMouseScrolled(scrollY, 20);
            return true;
        }
        if (mainScroll.handleMouseScrolled(scrollY, 30)) {
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