package com.niixlabs.lucidadvancements.client.gui.config;

import com.niixlabs.lucidadvancements.Constants;
import com.niixlabs.lucidadvancements.client.gui.screen.LucidAdvancementsScreen;
import com.niixlabs.lucidadvancements.client.gui.util.GuiScale;
import com.niixlabs.lucidadvancements.client.gui.util.LucidScrollHandler;
import com.niixlabs.lucidadvancements.config.ConfigOption;
import com.niixlabs.lucidadvancements.config.ConfigSection;
import com.niixlabs.lucidadvancements.config.LucidConfig;
import com.niixlabs.lucidadvancements.config.category.CategoryConfigManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class LucidConfigScreen extends Screen {
    private static final int BACK_BUTTON_WIDTH = 90;
    private static final int BACK_BUTTON_HEIGHT = 18;
    private static final int BACK_BUTTON_Y = 16;

    private static final int RELOAD_BUTTON_WIDTH = 90;
    private static final int RELOAD_BUTTON_GAP = 6;

    private final Screen previousScreen;
    private ColorPickerModal activeColorPicker;

    private final LucidScrollHandler mainScroll = new LucidScrollHandler();
    private final LucidScrollHandler sidebarScroll = new LucidScrollHandler();

    private final List<ConfigEntry> entries = new ArrayList<>();
    private final List<SidebarSection> sidebarSections = new ArrayList<>();
    private final List<Integer> sectionOffsets = new ArrayList<>();
    private SidebarSection selectedSection = null;
    private boolean needsRecalculation = true;

    public LucidConfigScreen(Screen previousScreen) {
        super(Component.literal("Lucid Advancements Config"));
        this.previousScreen = previousScreen;
    }

    private double getTargetScale() {
        return minecraft == null ? 1.0 : GuiScale.targetScale(minecraft);
    }

    private double getScaleFactor() {
        return minecraft == null ? 1.0 : GuiScale.scaleFactor(minecraft);
    }

    @Override
    protected void init() {
        if (minecraft != null) {
            double targetScale = getTargetScale();
            this.width = (int) Math.ceil(minecraft.getWindow().getScreenWidth() / targetScale);
            this.height = (int) Math.ceil(minecraft.getWindow().getScreenHeight() / targetScale);
        }
        super.init();

        entries.clear();
        sidebarSections.clear();

        SectionHeaderEntry currentHeader = null;
        for (Field field : LucidConfig.class.getDeclaredFields()) {
            ConfigSection sectionAnn = field.getAnnotation(ConfigSection.class);
            if (sectionAnn != null) {
                currentHeader = new SectionHeaderEntry(sectionAnn.value());
                entries.add(currentHeader);
                sidebarSections.add(new SidebarSection(sectionAnn.value(), currentHeader));
            }

            ConfigOption optionAnn = field.getAnnotation(ConfigOption.class);
            if (optionAnn != null) {
                if (currentHeader == null) {
                    String general = Component.translatable(Constants.MOD_ID + ".gui.config.section.general").getString();
                    currentHeader = new SectionHeaderEntry(general);
                    entries.add(currentHeader);
                    sidebarSections.add(new SidebarSection(general, currentHeader));
                }

                try {
                    Class<?> type = field.getType();
                    Object value = field.get(null);
                    if (type == boolean.class) {
                        entries.add(new BooleanOptionEntry(field, optionAnn, (Boolean) value));
                    } else {
                        entries.add(new TextOptionEntry(field, optionAnn, value, font));
                    }
                } catch (Exception ignored) {}
            }
        }

        if (!sidebarSections.isEmpty()) {
            selectedSection = sidebarSections.get(0);
        }

        needsRecalculation = true;
    }

    private void recalculateLayout() {
        sectionOffsets.clear();
        int totalHeight = 0;
        int sectionIndex = 0;
        for (ConfigEntry entry : entries) {
            if (sectionIndex < sidebarSections.size() && entry == sidebarSections.get(sectionIndex).header) {
                sectionOffsets.add(totalHeight);
                sectionIndex++;
            }
            totalHeight += entry.getHeight();
        }

        int viewportY = LucidConfig.screenTopBarHeight;
        int viewportHeight = height - viewportY - LucidConfig.screenViewportBottomMargin;

        mainScroll.updateMaxScroll(totalHeight - viewportHeight);
        sidebarScroll.updateMaxScroll((sidebarSections.size() * 18) - (height - LucidConfig.screenTopBarHeight - 24));

        needsRecalculation = false;
    }

    private void updateSelectedSectionFromScroll() {
        if (sectionOffsets.isEmpty()) {
            return;
        }

        int scrollOffset = (int) mainScroll.getScrollOffset();
        int activeIndex = 0;

        for (int i = 0; i < sectionOffsets.size(); i++) {
            if (sectionOffsets.get(i) <= scrollOffset) {
                activeIndex = i;
            } else {
                break;
            }
        }

        selectedSection = sidebarSections.get(activeIndex);
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

        guiGraphics.fill(0, 0, width, height, LucidConfig.screenBackdropColor);

        if (needsRecalculation) {
            recalculateLayout();
        }
        updateSelectedSectionFromScroll();

        renderTopBar(guiGraphics, scaledMouseX, scaledMouseY);
        renderSidebar(guiGraphics, scaleFactor, scaledMouseX, scaledMouseY);
        renderContent(guiGraphics, scaleFactor, scaledMouseX, scaledMouseY, partialTick);

        if (activeColorPicker != null) {
            activeColorPicker.render(guiGraphics, font, width, height, scaledMouseX, scaledMouseY);
        }

        guiGraphics.pose().popPose();
    }

    private void renderTopBar(GuiGraphics guiGraphics, int scaledMouseX, int scaledMouseY) {
        int sidebarWidth = LucidConfig.screenSidebarWidth;
        guiGraphics.fillGradient(sidebarWidth, 0, width, LucidConfig.screenTopBarHeight, LucidConfig.screenTopBarGradientStart, LucidConfig.screenTopBarGradientEnd);
        guiGraphics.fill(sidebarWidth, LucidConfig.screenTopBarHeight - 1, width, LucidConfig.screenTopBarHeight, LucidConfig.screenTopBarBorder);
        guiGraphics.drawString(font, Component.translatable(Constants.MOD_ID + ".gui.config.title"), sidebarWidth + LucidConfig.screenContentMargin, 20, LucidConfig.screenHeaderTitleColor, true);

        int backX = width - LucidConfig.screenContentMargin - BACK_BUTTON_WIDTH;
        renderTopBarButton(guiGraphics, backX, scaledMouseX, scaledMouseY,
                Constants.MOD_ID + ".gui.config.save_and_exit", BACK_BUTTON_WIDTH);

        int reloadX = backX - RELOAD_BUTTON_GAP - RELOAD_BUTTON_WIDTH;
        renderTopBarButton(guiGraphics, reloadX, scaledMouseX, scaledMouseY,
                Constants.MOD_ID + ".gui.config.reload", RELOAD_BUTTON_WIDTH);
    }

    private void renderTopBarButton(GuiGraphics guiGraphics, int buttonX, int scaledMouseX, int scaledMouseY, String translationKey, int buttonWidth) {
        boolean hovered = scaledMouseX >= buttonX && scaledMouseX <= buttonX + buttonWidth
                && scaledMouseY >= BACK_BUTTON_Y && scaledMouseY <= BACK_BUTTON_Y + BACK_BUTTON_HEIGHT;

        int bgColor = hovered ? LucidConfig.widgetBackgroundHovered : LucidConfig.widgetBackgroundIdle;
        int borderColor = hovered ? LucidConfig.widgetBorderHovered : LucidConfig.widgetBorderIdle;

        guiGraphics.fill(buttonX, BACK_BUTTON_Y, buttonX + buttonWidth, BACK_BUTTON_Y + BACK_BUTTON_HEIGHT, bgColor);
        guiGraphics.fill(buttonX, BACK_BUTTON_Y, buttonX + buttonWidth, BACK_BUTTON_Y + 1, borderColor);
        guiGraphics.fill(buttonX, BACK_BUTTON_Y + BACK_BUTTON_HEIGHT - 1, buttonX + buttonWidth, BACK_BUTTON_Y + BACK_BUTTON_HEIGHT, borderColor);
        guiGraphics.fill(buttonX, BACK_BUTTON_Y, buttonX + 1, BACK_BUTTON_Y + BACK_BUTTON_HEIGHT, borderColor);
        guiGraphics.fill(buttonX + buttonWidth - 1, BACK_BUTTON_Y, buttonX + buttonWidth, BACK_BUTTON_Y + BACK_BUTTON_HEIGHT, borderColor);

        guiGraphics.drawCenteredString(font, Component.translatable(translationKey), buttonX + buttonWidth / 2, BACK_BUTTON_Y + 5, hovered ? LucidConfig.widgetTextHovered : LucidConfig.widgetTextIdle);
    }

    private void renderSidebar(GuiGraphics guiGraphics, double scaleFactor, int scaledMouseX, int scaledMouseY) {
        int sidebarWidth = LucidConfig.screenSidebarWidth;

        guiGraphics.fillGradient(0, 0, sidebarWidth, height, LucidConfig.screenSidebarGradientStart, LucidConfig.screenSidebarGradientEnd);
        guiGraphics.fill(sidebarWidth - 1, 0, sidebarWidth, height, LucidConfig.screenSidebarBorder);

        int scissorX2 = (int) Math.round(sidebarWidth / scaleFactor);
        int scissorY2 = (int) Math.round(height / scaleFactor);
        guiGraphics.enableScissor(0, 0, scissorX2, scissorY2);

        int rowY = LucidConfig.screenSidebarTopPadding - (int) sidebarScroll.getScrollOffset();

        for (SidebarSection section : sidebarSections) {
            boolean selected = section == selectedSection;
            int itemHeight = 14;

            if (selected) {
                guiGraphics.fill(4, rowY, sidebarWidth - 4, rowY + itemHeight, LucidConfig.screenSidebarSelectedFill);
                guiGraphics.fill(4, rowY, 6, rowY + itemHeight, LucidConfig.screenSidebarSelectedAccent);
            } else if (scaledMouseX >= 4 && scaledMouseX <= sidebarWidth - 4 && scaledMouseY >= rowY && scaledMouseY <= rowY + itemHeight) {
                guiGraphics.fill(4, rowY, sidebarWidth - 4, rowY + itemHeight, LucidConfig.screenSidebarHoverFill);
            }

            guiGraphics.pose().pushPose();

            float textScale = 0.85f;
            float scaledFontHeight = font.lineHeight * textScale;

            int textY = rowY + (int) ((itemHeight - scaledFontHeight) / 2);

            guiGraphics.pose().translate(10, textY, 0);
            guiGraphics.pose().scale(textScale, textScale, 1.0f);

            String displayTitle = section.title;

            int maxTextWidth = (int) ((sidebarWidth - 20) / textScale);
            if (font.width(displayTitle) > maxTextWidth) {
                displayTitle = font.plainSubstrByWidth(displayTitle, maxTextWidth - font.width(LucidConfig.sidebarTruncationEllipsis)) + LucidConfig.sidebarTruncationEllipsis;
            }

            guiGraphics.drawString(font, displayTitle, 0, 0, selected ? LucidConfig.screenSidebarTextSelected : LucidConfig.screenSidebarTextIdle, true);
            guiGraphics.pose().popPose();

            rowY += 18;
        }

        guiGraphics.disableScissor();
    }

    private void renderContent(GuiGraphics guiGraphics, double scaleFactor, int scaledMouseX, int scaledMouseY, float partialTick) {
        int contentX = LucidConfig.screenSidebarWidth + LucidConfig.screenContentMargin;
        int contentWidth = width - LucidConfig.screenSidebarWidth - (LucidConfig.screenContentMargin * 2);
        int viewportY = LucidConfig.screenTopBarHeight;
        int viewportHeight = height - viewportY - LucidConfig.screenViewportBottomMargin;

        int scissorX1 = (int) Math.round(contentX / scaleFactor);
        int scissorY1 = (int) Math.round(viewportY / scaleFactor);
        int scissorX2 = (int) Math.round((width - LucidConfig.screenContentMargin) / scaleFactor);
        int scissorY2 = (int) Math.round((viewportY + viewportHeight) / scaleFactor);
        guiGraphics.enableScissor(scissorX1, scissorY1, scissorX2, scissorY2);

        int currentY = viewportY - (int) mainScroll.getScrollOffset();
        for (ConfigEntry entry : entries) {
            if (currentY + entry.getHeight() > viewportY && currentY < viewportY + viewportHeight) {
                entry.updatePosition(contentX, currentY, contentWidth);
                entry.render(guiGraphics, font, contentX, currentY, contentWidth, scaledMouseX, scaledMouseY, partialTick);
            }
            currentY += entry.getHeight();
        }

        guiGraphics.disableScissor();
        mainScroll.renderScrollbar(guiGraphics, width, viewportY, viewportHeight);
    }

    private void openColorPicker(TextOptionEntry entry) {
        activeColorPicker = new ColorPickerModal(entry, font, width, height);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double scaleFactor = getScaleFactor();
        mouseX *= scaleFactor;
        mouseY *= scaleFactor;

        if (activeColorPicker != null) {
            if (button == 0) {
                if (activeColorPicker.isApplyClicked(mouseX, mouseY)) {
                    activeColorPicker.commit();
                    activeColorPicker = null;
                    return true;
                }
                if (activeColorPicker.isCancelClicked(mouseX, mouseY) || activeColorPicker.isOutsideModal(mouseX, mouseY)) {
                    activeColorPicker = null;
                    return true;
                }
                activeColorPicker.mouseClicked(mouseX, mouseY, button);
            }
            return true;
        }

        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        clearTextFocus();

        int backX = width - LucidConfig.screenContentMargin - BACK_BUTTON_WIDTH;
        if (mouseX >= backX && mouseX <= backX + BACK_BUTTON_WIDTH && mouseY >= BACK_BUTTON_Y && mouseY <= BACK_BUTTON_Y + BACK_BUTTON_HEIGHT) {
            saveAll();
            if (minecraft != null) minecraft.setScreen(previousScreen);
            return true;
        }

        int reloadX = backX - RELOAD_BUTTON_GAP - RELOAD_BUTTON_WIDTH;
        if (mouseX >= reloadX && mouseX <= reloadX + RELOAD_BUTTON_WIDTH && mouseY >= BACK_BUTTON_Y && mouseY <= BACK_BUTTON_Y + BACK_BUTTON_HEIGHT) {
            reloadEverything();
            return true;
        }

        if (mouseX <= LucidConfig.screenSidebarWidth) {
            int rowY = LucidConfig.screenSidebarTopPadding - (int) sidebarScroll.getScrollOffset();
            for (SidebarSection section : sidebarSections) {
                if (mouseY >= rowY && mouseY <= rowY + 14) {
                    selectedSection = section;
                    scrollToSection(section);
                    return true;
                }
                rowY += 18;
            }
            return true;
        }

        int contentX = LucidConfig.screenSidebarWidth + LucidConfig.screenContentMargin;
        int contentWidth = width - LucidConfig.screenSidebarWidth - (LucidConfig.screenContentMargin * 2);
        int viewportY = LucidConfig.screenTopBarHeight;
        int viewportHeight = height - viewportY - LucidConfig.screenViewportBottomMargin;

        if (mainScroll.handleMouseDown(mouseX, mouseY, width, viewportY, viewportHeight)) {
            return true;
        }

        int currentY = viewportY - (int) mainScroll.getScrollOffset();
        for (ConfigEntry entry : entries) {
            if (currentY + entry.getHeight() > viewportY && currentY < viewportY + viewportHeight) {
                entry.updatePosition(contentX, currentY, contentWidth);
                if (entry instanceof TextOptionEntry textEntry && textEntry.isSwatchClicked(mouseX, mouseY)) {
                    openColorPicker(textEntry);
                    return true;
                }
                if (entry.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
            }
            currentY += entry.getHeight();
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void clearTextFocus() {
        for (ConfigEntry entry : entries) {
            if (entry instanceof TextOptionEntry textEntry) {
                textEntry.setFocused(false);
            }
        }
    }

    private void scrollToSection(SidebarSection section) {
        int yOffset = 0;
        for (ConfigEntry entry : entries) {
            if (entry == section.header) {
                break;
            }
            yOffset += entry.getHeight();
        }
        mainScroll.setScrollOffset(yOffset);
    }

    private void saveAll() {
        for (ConfigEntry entry : entries) {
            entry.save();
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        double scaleFactor = getScaleFactor();
        mouseX *= scaleFactor;
        mouseY *= scaleFactor;

        if (activeColorPicker != null) {
            activeColorPicker.mouseDragged(mouseX, mouseY);
            return true;
        }

        int viewportY = LucidConfig.screenTopBarHeight;
        int viewportHeight = height - viewportY - LucidConfig.screenViewportBottomMargin;

        if (mainScroll.handleMouseDragged(mouseY, viewportY, viewportHeight)) {
            return true;
        }

        for (ConfigEntry entry : entries) {
            if (entry.mouseDragged(mouseX, mouseY, button, dragX * scaleFactor, dragY * scaleFactor)) {
                return true;
            }
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX * scaleFactor, dragY * scaleFactor);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (activeColorPicker != null) {
            activeColorPicker.mouseReleased();
            return true;
        }
        if (button == 0) {
            mainScroll.setDragging(false);
        }
        return super.mouseReleased(mouseX * getScaleFactor(), mouseY * getScaleFactor(), button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (activeColorPicker != null) {
            return true;
        }

        double scaleFactor = getScaleFactor();
        mouseX *= scaleFactor;

        if (mouseX <= LucidConfig.screenSidebarWidth) {
            sidebarScroll.handleMouseScrolled(scrollY, LucidConfig.sidebarScrollSpeed);
            return true;
        }
        if (mainScroll.handleMouseScrolled(scrollY, LucidConfig.mainScrollSpeed)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY * scaleFactor, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (activeColorPicker != null) {
            if (keyCode == 256) {
                activeColorPicker = null;
                return true;
            }
            return activeColorPicker.keyPressed(keyCode, scanCode, modifiers);
        }

        for (ConfigEntry entry : entries) {
            if (entry.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (activeColorPicker != null) {
            return activeColorPicker.charTyped(codePoint, modifiers);
        }

        for (ConfigEntry entry : entries) {
            if (entry.charTyped(codePoint, modifiers)) {
                return true;
            }
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public static class SidebarSection {
        public final String title;
        public final SectionHeaderEntry header;

        public SidebarSection(String title, SectionHeaderEntry header) {
            this.title = title;
            this.header = header;
        }
    }

    public static abstract class ConfigEntry {
        protected final String name;
        protected final Field field;
        protected final ConfigOption option;
        protected int x, y, width;

        public ConfigEntry(Field field, ConfigOption option) {
            this.field = field;
            this.name = field != null ? field.getName() : "";
            this.option = option;
        }

        public abstract void render(GuiGraphics guiGraphics, Font font, int x, int y, int width, int mouseX, int mouseY, float partialTick);

        public int getHeight() {
            return 32;
        }

        public void updatePosition(int x, int y, int width) {
            this.x = x;
            this.y = y;
            this.width = width;
        }

        public void renderLabel(GuiGraphics guiGraphics, Font font) {
            String formattedName = Component.translatable(Constants.MOD_ID + ".gui.config." + name + ".name").getString();
            String formattedDesc = Component.translatable(Constants.MOD_ID + ".gui.config." + name + ".desc").getString();

            guiGraphics.drawString(font, formattedName, x + 10, y + 6, LucidConfig.widgetTextIdle, true);
            if (!formattedDesc.isEmpty()) {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().scale(0.8f, 0.8f, 1.0f);
                guiGraphics.drawString(font, formattedDesc, (int) ((x + 10) / 0.8f), (int) ((y + 18) / 0.8f), 0xFFAAAAAA, true);
                guiGraphics.pose().popPose();
            }
        }

        public boolean mouseClicked(double mouseX, double mouseY, int button) { return false; }
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) { return false; }
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) { return false; }
        public boolean charTyped(char codePoint, int modifiers) { return false; }

        public abstract void save();
    }

    public static class SectionHeaderEntry extends ConfigEntry {
        private final String title;

        public SectionHeaderEntry(String title) {
            super(null, null);
            this.title = title;
        }

        @Override
        public int getHeight() {
            return 35;
        }

        @Override
        public void render(GuiGraphics guiGraphics, Font font, int x, int y, int width, int mouseX, int mouseY, float partialTick) {
            guiGraphics.fill(x, y + 20, x + width, y + 35, LucidConfig.screenHeaderDividerColor);
            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(1.2f, 1.2f, 1.0f);
            guiGraphics.drawString(font, title, (int) ((x + 5) / 1.2f), (int) ((y + 24) / 1.2f), LucidConfig.screenHeaderTitleColor, true);
            guiGraphics.pose().popPose();
        }

        @Override
        public void save() {}
    }

    public static class TextOptionEntry extends ConfigEntry {
        private final EditBox editBox;

        public TextOptionEntry(Field field, ConfigOption option, Object value, Font font) {
            super(field, option);
            this.editBox = new EditBox(font, 0, 0, 80, 16, Component.empty());
            this.editBox.setMaxLength(256);

            if (option.hex() && value instanceof Integer) {
                this.editBox.setValue(String.format("0x%08X", (Integer) value));
            } else {
                this.editBox.setValue(String.valueOf(value));
            }
        }

        @Override
        public void updatePosition(int x, int y, int width) {
            super.updatePosition(x, y, width);
            this.editBox.setX(x + width - 80);
            this.editBox.setY(y + 8);
        }

        @Override
        public void render(GuiGraphics guiGraphics, Font font, int x, int y, int width, int mouseX, int mouseY, float partialTick) {
            renderLabel(guiGraphics, font);

            if (option.hex()) {
                try {
                    int color = (int) Long.parseLong(editBox.getValue().replace("0x", "").replace("#", "").trim(), 16);
                    int boxX = editBox.getX() - 20;
                    guiGraphics.fill(boxX, y + 8, boxX + 16, y + 24, 0xFF000000);
                    guiGraphics.fill(boxX + 1, y + 9, boxX + 15, y + 23, color);
                } catch (Exception ignored) {}
            }

            editBox.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            boolean clicked = editBox.mouseClicked(mouseX, mouseY, button);
            if (clicked) {
                editBox.setFocused(true);
            }
            return clicked;
        }

        public void setFocused(boolean focused) {
            editBox.setFocused(focused);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            return editBox.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            return editBox.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean charTyped(char codePoint, int modifiers) {
            return editBox.charTyped(codePoint, modifiers);
        }

        @Override
        public void save() {
            try {
                Class<?> type = field.getType();
                String rawValue = editBox.getValue();
                if (type == int.class) {
                    int val = option.hex() ? (int) Long.parseLong(rawValue.replace("0x", "").replace("#", "").trim(), 16) : Integer.parseInt(rawValue);
                    field.setInt(null, val);
                    LucidConfig.updateAndSave(field.getName(), val);
                } else if (type == double.class) {
                    double val = Double.parseDouble(rawValue);
                    field.setDouble(null, val);
                    LucidConfig.updateAndSave(field.getName(), val);
                } else if (type == String.class) {
                    field.set(null, rawValue);
                    LucidConfig.updateAndSave(field.getName(), rawValue);
                }
            } catch (Exception ignored) {}
        }

        public boolean isSwatchClicked(double mouseX, double mouseY) {
            if (!option.hex()) {
                return false;
            }
            int boxX = editBox.getX() - 20;
            return mouseX >= boxX && mouseX <= boxX + 16 && mouseY >= y + 8 && mouseY <= y + 24;
        }

        public String getValue() {
            return editBox.getValue();
        }

        public void setValueFromPicker(String value) {
            editBox.setValue(value);
        }
    }

    public static class BooleanOptionEntry extends ConfigEntry {
        private boolean value;

        public BooleanOptionEntry(Field field, ConfigOption option, boolean value) {
            super(field, option);
            this.value = value;
        }

        @Override
        public void render(GuiGraphics guiGraphics, Font font, int x, int y, int width, int mouseX, int mouseY, float partialTick) {
            renderLabel(guiGraphics, font);

            int boxSize = 16;
            int boxX = x + width - 26;
            int boxY = y + 8;

            boolean hovered = mouseX >= boxX && mouseX <= boxX + boxSize && mouseY >= boxY && mouseY <= boxY + boxSize;

            int bgColor = hovered ? LucidConfig.widgetBackgroundHovered : LucidConfig.widgetBackgroundIdle;
            int borderColor = hovered ? LucidConfig.widgetBorderHovered : LucidConfig.widgetBorderIdle;

            guiGraphics.fill(boxX, boxY, boxX + boxSize, boxY + boxSize, bgColor);
            guiGraphics.fill(boxX, boxY, boxX + boxSize, boxY + 1, borderColor);
            guiGraphics.fill(boxX, boxY + boxSize - 1, boxX + boxSize, boxY + boxSize, borderColor);
            guiGraphics.fill(boxX, boxY, boxX + 1, boxY + boxSize, borderColor);
            guiGraphics.fill(boxX + boxSize - 1, boxY, boxX + boxSize, boxY + boxSize, borderColor);

            if (value) {
                guiGraphics.fill(boxX + 4, boxY + 4, boxX + boxSize - 4, boxY + boxSize - 4, 0xFFFFFFFF);
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            int boxSize = 16;
            int boxX = x + width - 26;
            int boxY = y + 8;

            if (mouseX >= boxX && mouseX <= boxX + boxSize && mouseY >= boxY && mouseY <= boxY + boxSize) {
                value = !value;
                return true;
            }
            return false;
        }

        @Override
        public void save() {
            try {
                field.setBoolean(null, value);
                LucidConfig.updateAndSave(field.getName(), value);
            } catch (Exception ignored) {}
        }
    }

    public static class ColorPickerModal {
        private static final int MODAL_WIDTH = 220;
        private static final int MODAL_HEIGHT = 300;
        private static final int SV_SIZE = 150;
        private static final int SLIDER_WIDTH = 16;
        private static final int SLIDER_GAP = 10;
        private static final int PADDING = 16;
        private static final float Z_LEVEL = 400f;

        private final TextOptionEntry target;
        private final EditBox hexInput;

        private float hue;
        private float saturation;
        private float brightness;
        private float alpha;

        private final int modalX, modalY;
        private final int svX, svY;
        private final int hueX, hueY;
        private final int alphaX, alphaY;

        private boolean draggingSV;
        private boolean draggingHue;
        private boolean draggingAlpha;

        public ColorPickerModal(TextOptionEntry target, Font font, int screenWidth, int screenHeight) {
            this.target = target;
            this.modalX = (screenWidth - MODAL_WIDTH) / 2;
            this.modalY = (screenHeight - MODAL_HEIGHT) / 2;
            this.svX = modalX + PADDING;
            this.svY = modalY + 36;
            this.hueX = svX + SV_SIZE + SLIDER_GAP;
            this.hueY = svY;
            this.alphaX = svX;
            this.alphaY = svY + SV_SIZE + 14;

            int hexY = alphaY + 26;
            this.hexInput = new EditBox(font, svX + 26, hexY, SV_SIZE + SLIDER_GAP + SLIDER_WIDTH - 26, 16, Component.empty());
            this.hexInput.setMaxLength(8);

            long parsed;
            try {
                parsed = Long.parseLong(target.getValue().replace("0x", "").replace("#", "").trim(), 16);
            } catch (Exception e) {
                parsed = 0xFFFFFFFFL;
            }
            int argb = (int) parsed;
            this.alpha = ((argb >> 24) & 0xFF) / 255f;
            int rgb = argb & 0xFFFFFF;
            float[] hsb = java.awt.Color.RGBtoHSB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, null);
            this.hue = hsb[0] * 360f;
            this.saturation = hsb[1];
            this.brightness = hsb[2];

            syncHexInput();
        }

        private int currentRGB() {
            return java.awt.Color.HSBtoRGB(hue / 360f, saturation, brightness) & 0xFFFFFF;
        }

        private int currentARGB() {
            return ((int) (alpha * 255) << 24) | currentRGB();
        }

        private void syncHexInput() {
            hexInput.setValue(String.format("%08X", currentARGB()));
        }

        private void applyFromHexInput() {
            try {
                long parsed = Long.parseLong(hexInput.getValue().replace("0x", "").replace("#", "").trim(), 16);
                int argb = (int) parsed;
                this.alpha = ((argb >> 24) & 0xFF) / 255f;
                int rgb = argb & 0xFFFFFF;
                float[] hsb = java.awt.Color.RGBtoHSB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, null);
                this.hue = hsb[0] * 360f;
                this.saturation = hsb[1];
                this.brightness = hsb[2];
            } catch (Exception ignored) {}
        }

        public void commit() {
            applyFromHexInput();
            target.setValueFromPicker(String.format("0x%08X", currentARGB()));
        }

        public void render(GuiGraphics guiGraphics, Font font, int screenWidth, int screenHeight, int mouseX, int mouseY) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, Z_LEVEL);

            guiGraphics.fill(0, 0, screenWidth, screenHeight, 0xAA000000);

            guiGraphics.fill(modalX, modalY, modalX + MODAL_WIDTH, modalY + MODAL_HEIGHT, LucidConfig.screenBackdropColor);
            guiGraphics.fill(modalX, modalY, modalX + MODAL_WIDTH, modalY + 1, LucidConfig.screenSidebarBorder);
            guiGraphics.fill(modalX, modalY + MODAL_HEIGHT - 1, modalX + MODAL_WIDTH, modalY + MODAL_HEIGHT, LucidConfig.screenSidebarBorder);
            guiGraphics.fill(modalX, modalY, modalX + 1, modalY + MODAL_HEIGHT, LucidConfig.screenSidebarBorder);
            guiGraphics.fill(modalX + MODAL_WIDTH - 1, modalY, modalX + MODAL_WIDTH, modalY + MODAL_HEIGHT, LucidConfig.screenSidebarBorder);

            guiGraphics.drawString(font, Component.translatable(Constants.MOD_ID + ".gui.config.color_picker.title"), modalX + PADDING, modalY + 14, LucidConfig.screenHeaderTitleColor, true);

            renderSVBox(guiGraphics);
            renderHueSlider(guiGraphics);
            renderAlphaSlider(guiGraphics);
            renderPreview(guiGraphics);

            hexInput.render(guiGraphics, mouseX, mouseY, 0f);

            renderButton(guiGraphics, font, modalX + PADDING, buttonY(), 80, "apply", mouseX, mouseY);
            renderButton(guiGraphics, font, modalX + PADDING + 90, buttonY(), 80, "cancel", mouseX, mouseY);

            guiGraphics.pose().popPose();
        }

        private void renderSVBox(GuiGraphics guiGraphics) {
            int hueRGB = java.awt.Color.HSBtoRGB(hue / 360f, 1f, 1f) & 0xFFFFFF;
            int step = 4;
            for (int px = 0; px < SV_SIZE; px += step) {
                float s = px / (float) SV_SIZE;
                int topColor = lerpColor(0xFFFFFFFF, 0xFF000000 | hueRGB, s);
                guiGraphics.fillGradient(svX + px, svY, svX + px + step, svY + SV_SIZE, topColor, 0xFF000000);
            }

            int cursorX = svX + Math.round(saturation * SV_SIZE);
            int cursorY = svY + Math.round((1f - brightness) * SV_SIZE);
            drawCursorRing(guiGraphics, cursorX, cursorY);
        }

        private void renderHueSlider(GuiGraphics guiGraphics) {
            int[] stops = {0xFFFF0000, 0xFFFFFF00, 0xFF00FF00, 0xFF00FFFF, 0xFF0000FF, 0xFFFF00FF, 0xFFFF0000};
            int segmentHeight = SV_SIZE / (stops.length - 1);
            for (int i = 0; i < stops.length - 1; i++) {
                guiGraphics.fillGradient(hueX, hueY + i * segmentHeight, hueX + SLIDER_WIDTH, hueY + (i + 1) * segmentHeight, stops[i], stops[i + 1]);
            }

            int markerY = hueY + Math.round((hue / 360f) * SV_SIZE);
            guiGraphics.fill(hueX - 2, markerY - 1, hueX + SLIDER_WIDTH + 2, markerY + 1, 0xFFFFFFFF);
        }

        private void renderAlphaSlider(GuiGraphics guiGraphics) {
            int totalWidth = SV_SIZE + SLIDER_GAP + SLIDER_WIDTH;
            int rgb = currentRGB();

            for (int px = 0; px < totalWidth; px += 4) {
                boolean checker = (px / 8) % 2 == 0;
                guiGraphics.fill(alphaX + px, alphaY, alphaX + px + 4, alphaY + 12, checker ? 0xFFAAAAAA : 0xFF777777);
            }

            for (int px = 0; px < totalWidth; px += 2) {
                float t = px / (float) totalWidth;
                int a = (int) (t * 255);
                int color = (a << 24) | rgb;
                guiGraphics.fill(alphaX + px, alphaY, alphaX + px + 2, alphaY + 12, color);
            }

            int markerX = alphaX + Math.round(alpha * totalWidth);
            guiGraphics.fill(markerX - 1, alphaY - 2, markerX + 1, alphaY + 14, 0xFFFFFFFF);
        }

        private void renderPreview(GuiGraphics guiGraphics) {
            int previewX = svX;
            int previewY = hexInput.getY();
            int size = 16;

            for (int px = 0; px < size; px += 4) {
                for (int py = 0; py < size; py += 4) {
                    boolean checker = ((px / 4) + (py / 4)) % 2 == 0;
                    guiGraphics.fill(previewX + px, previewY + py, previewX + px + 4, previewY + py + 4, checker ? 0xFFCCCCCC : 0xFF888888);
                }
            }
            guiGraphics.fill(previewX, previewY, previewX + size, previewY + size, currentARGB());
        }

        private void drawCursorRing(GuiGraphics guiGraphics, int cx, int cy) {
            guiGraphics.fill(cx - 4, cy - 4, cx + 4, cy - 3, 0xFFFFFFFF);
            guiGraphics.fill(cx - 4, cy + 3, cx + 4, cy + 4, 0xFFFFFFFF);
            guiGraphics.fill(cx - 4, cy - 4, cx - 3, cy + 4, 0xFFFFFFFF);
            guiGraphics.fill(cx + 3, cy - 4, cx + 4, cy + 4, 0xFFFFFFFF);
        }

        private int buttonY() {
            return modalY + MODAL_HEIGHT - 34;
        }

        private void renderButton(GuiGraphics guiGraphics, Font font, int x, int y, int width, String key, int mouseX, int mouseY) {
            boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 18;
            int bgColor = hovered ? LucidConfig.widgetBackgroundHovered : LucidConfig.widgetBackgroundIdle;
            int borderColor = hovered ? LucidConfig.widgetBorderHovered : LucidConfig.widgetBorderIdle;

            guiGraphics.fill(x, y, x + width, y + 18, bgColor);
            guiGraphics.fill(x, y, x + width, y + 1, borderColor);
            guiGraphics.fill(x, y + 17, x + width, y + 18, borderColor);
            guiGraphics.fill(x, y, x + 1, y + 18, borderColor);
            guiGraphics.fill(x + width - 1, y, x + width, y + 18, borderColor);

            guiGraphics.drawCenteredString(font, Component.translatable(Constants.MOD_ID + ".gui.config.color_picker." + key), x + width / 2, y + 5, hovered ? LucidConfig.widgetTextHovered : LucidConfig.widgetTextIdle);
        }

        private int lerpColor(int from, int to, float t) {
            int fa = (from >> 24) & 0xFF, fr = (from >> 16) & 0xFF, fg = (from >> 8) & 0xFF, fb = from & 0xFF;
            int ta = (to >> 24) & 0xFF, tr = (to >> 16) & 0xFF, tg = (to >> 8) & 0xFF, tb = to & 0xFF;
            int a = (int) (fa + (ta - fa) * t);
            int r = (int) (fr + (tr - fr) * t);
            int g = (int) (fg + (tg - fg) * t);
            int b = (int) (fb + (tb - fb) * t);
            return (a << 24) | (r << 16) | (g << 8) | b;
        }

        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (isInside(mouseX, mouseY, svX, svY, SV_SIZE, SV_SIZE)) {
                draggingSV = true;
                updateSV(mouseX, mouseY);
                return true;
            }
            if (isInside(mouseX, mouseY, hueX, hueY, SLIDER_WIDTH, SV_SIZE)) {
                draggingHue = true;
                updateHue(mouseY);
                return true;
            }
            if (isInside(mouseX, mouseY, alphaX, alphaY, SV_SIZE + SLIDER_GAP + SLIDER_WIDTH, 12)) {
                draggingAlpha = true;
                updateAlpha(mouseX);
                return true;
            }

            boolean hitInput = hexInput.mouseClicked(mouseX, mouseY, button);
            hexInput.setFocused(hitInput);
            return true;
        }

        public boolean mouseDragged(double mouseX, double mouseY) {
            if (draggingSV) {
                updateSV(mouseX, mouseY);
                return true;
            }
            if (draggingHue) {
                updateHue(mouseY);
                return true;
            }
            if (draggingAlpha) {
                updateAlpha(mouseX);
                return true;
            }
            return false;
        }

        public void mouseReleased() {
            draggingSV = false;
            draggingHue = false;
            draggingAlpha = false;
        }

        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (keyCode == 257) {
                applyFromHexInput();
                syncHexInput();
                return true;
            }
            return hexInput.keyPressed(keyCode, scanCode, modifiers);
        }

        public boolean charTyped(char codePoint, int modifiers) {
            return hexInput.charTyped(codePoint, modifiers);
        }

        public boolean isApplyClicked(double mouseX, double mouseY) {
            return isInside(mouseX, mouseY, modalX + PADDING, buttonY(), 80, 18);
        }

        public boolean isCancelClicked(double mouseX, double mouseY) {
            return isInside(mouseX, mouseY, modalX + PADDING + 90, buttonY(), 80, 18);
        }

        public boolean isOutsideModal(double mouseX, double mouseY) {
            return !isInside(mouseX, mouseY, modalX, modalY, MODAL_WIDTH, MODAL_HEIGHT);
        }

        private void updateSV(double mouseX, double mouseY) {
            saturation = clamp01((float) (mouseX - svX) / SV_SIZE);
            brightness = 1f - clamp01((float) (mouseY - svY) / SV_SIZE);
            syncHexInput();
        }

        private void updateHue(double mouseY) {
            hue = clamp01((float) (mouseY - hueY) / SV_SIZE) * 360f;
            syncHexInput();
        }

        private void updateAlpha(double mouseX) {
            int width = SV_SIZE + SLIDER_GAP + SLIDER_WIDTH;
            alpha = clamp01((float) (mouseX - alphaX) / width);
            syncHexInput();
        }

        private float clamp01(float value) {
            return Math.max(0f, Math.min(1f, value));
        }

        private boolean isInside(double mouseX, double mouseY, int x, int y, int w, int h) {
            return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        }
    }

    private void reloadEverything() {
        LucidConfig.load();
        CategoryConfigManager.reloadAll();

        if (previousScreen instanceof LucidAdvancementsScreen advancementsScreen) {
            advancementsScreen.refreshCategoryData();
        }

        if (minecraft != null) {
            minecraft.setScreen(new LucidConfigScreen(previousScreen));
        }
    }
}