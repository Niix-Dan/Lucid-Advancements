package com.niixlabs.lucidadvancements.client.gui.config;

import com.niixlabs.lucidadvancements.Constants;
import com.niixlabs.lucidadvancements.client.gui.util.GuiScale;
import com.niixlabs.lucidadvancements.client.gui.util.LucidScrollHandler;
import com.niixlabs.lucidadvancements.config.ConfigOption;
import com.niixlabs.lucidadvancements.config.ConfigSection;
import com.niixlabs.lucidadvancements.config.LucidConfig;
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

    private final Screen previousScreen;
    private final LucidScrollHandler mainScroll = new LucidScrollHandler();
    private final LucidScrollHandler sidebarScroll = new LucidScrollHandler();

    private final List<ConfigEntry> entries = new ArrayList<>();
    private final List<SidebarSection> sidebarSections = new ArrayList<>();
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
        int totalHeight = 0;
        for (ConfigEntry entry : entries) {
            totalHeight += entry.getHeight();
        }
        int viewportY = LucidConfig.screenTopBarHeight;
        int viewportHeight = height - viewportY - LucidConfig.screenViewportBottomMargin;

        mainScroll.updateMaxScroll(totalHeight - viewportHeight);
        sidebarScroll.updateMaxScroll((sidebarSections.size() * 18) - (height - LucidConfig.screenTopBarHeight - 24));

        needsRecalculation = false;
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

        renderTopBar(guiGraphics, scaledMouseX, scaledMouseY);
        renderSidebar(guiGraphics, scaleFactor, scaledMouseX, scaledMouseY);
        renderContent(guiGraphics, scaleFactor, scaledMouseX, scaledMouseY, partialTick);

        guiGraphics.pose().popPose();
    }

    private void renderTopBar(GuiGraphics guiGraphics, int scaledMouseX, int scaledMouseY) {
        int sidebarWidth = LucidConfig.screenSidebarWidth;
        guiGraphics.fillGradient(sidebarWidth, 0, width, LucidConfig.screenTopBarHeight, LucidConfig.screenTopBarGradientStart, LucidConfig.screenTopBarGradientEnd);
        guiGraphics.fill(sidebarWidth, LucidConfig.screenTopBarHeight - 1, width, LucidConfig.screenTopBarHeight, LucidConfig.screenTopBarBorder);
        guiGraphics.drawString(font, Component.translatable(Constants.MOD_ID + ".gui.config.title"), sidebarWidth + LucidConfig.screenContentMargin, 20, LucidConfig.screenHeaderTitleColor, true);

        int backX = width - LucidConfig.screenContentMargin - BACK_BUTTON_WIDTH;
        boolean hovered = scaledMouseX >= backX && scaledMouseX <= backX + BACK_BUTTON_WIDTH
                && scaledMouseY >= BACK_BUTTON_Y && scaledMouseY <= BACK_BUTTON_Y + BACK_BUTTON_HEIGHT;

        int bgColor = hovered ? LucidConfig.widgetBackgroundHovered : LucidConfig.widgetBackgroundIdle;
        int borderColor = hovered ? LucidConfig.widgetBorderHovered : LucidConfig.widgetBorderIdle;

        guiGraphics.fill(backX, BACK_BUTTON_Y, backX + BACK_BUTTON_WIDTH, BACK_BUTTON_Y + BACK_BUTTON_HEIGHT, bgColor);
        guiGraphics.fill(backX, BACK_BUTTON_Y, backX + BACK_BUTTON_WIDTH, BACK_BUTTON_Y + 1, borderColor);
        guiGraphics.fill(backX, BACK_BUTTON_Y + BACK_BUTTON_HEIGHT - 1, backX + BACK_BUTTON_WIDTH, BACK_BUTTON_Y + BACK_BUTTON_HEIGHT, borderColor);
        guiGraphics.fill(backX, BACK_BUTTON_Y, backX + 1, BACK_BUTTON_Y + BACK_BUTTON_HEIGHT, borderColor);
        guiGraphics.fill(backX + BACK_BUTTON_WIDTH - 1, BACK_BUTTON_Y, backX + BACK_BUTTON_WIDTH, BACK_BUTTON_Y + BACK_BUTTON_HEIGHT, borderColor);

        guiGraphics.drawCenteredString(font, Component.translatable(Constants.MOD_ID + ".gui.config.save_and_exit"), backX + BACK_BUTTON_WIDTH / 2, BACK_BUTTON_Y + 5, hovered ? LucidConfig.widgetTextHovered : LucidConfig.widgetTextIdle);
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

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double scaleFactor = getScaleFactor();
        mouseX *= scaleFactor;
        mouseY *= scaleFactor;

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
        if (button == 0) {
            mainScroll.setDragging(false);
        }
        return super.mouseReleased(mouseX * getScaleFactor(), mouseY * getScaleFactor(), button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        double scaleFactor = getScaleFactor();
        mouseX *= scaleFactor;

        if (mouseX <= LucidConfig.screenSidebarWidth) {
            sidebarScroll.handleMouseScrolled(scrollY, 20);
            return true;
        }
        if (mainScroll.handleMouseScrolled(scrollY, 30)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY * scaleFactor, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (ConfigEntry entry : entries) {
            if (entry.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
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
        protected final String comment;
        protected final Field field;
        protected final ConfigOption option;
        protected int x, y, width;

        public ConfigEntry(Field field, ConfigOption option) {
            this.field = field;
            this.name = field != null ? field.getName() : "";
            this.comment = option != null ? option.comment() : "";
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
}