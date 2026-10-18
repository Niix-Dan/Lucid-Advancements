package com.niixlabs.lucidadvancements.client.gui.overlay;

import com.niixlabs.lucidadvancements.Constants;
import com.niixlabs.lucidadvancements.client.gui.util.GuiScale;
import com.niixlabs.lucidadvancements.config.LucidConfig;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class LucidNotificationOverlay {
    private static final int EFFECT_CACHE_INTERVAL_MS = 50;
    private static final int MAX_RECIPE_ICONS = 16;
    private static final int MIN_NOTIFICATION_HEIGHT = 30;
    private static final long ICON_ROTATION_MS = 1000L;
    private static final int MIN_EFFECT_ROW_HEIGHT = 22;

    private static final List<TimedNotification> NOTIFICATIONS = new ArrayList<>();
    private static ClientLevel lastLevel = null;

    private static List<MobEffectInstance> effectCache = List.of();
    private static long effectCacheAt = 0L;
    private static ClientLevel effectCacheLevel = null;
    private static LocalPlayer effectCachePlayer = null;

    private LucidNotificationOverlay() {}

    public static void add(Notification notification) {
        if (notification == null) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level != lastLevel) {
            lastLevel = mc.level;
            clear();
        }

        long now = Util.getMillis();
        NOTIFICATIONS.removeIf(timed -> now >= timed.expiresAt);

        if (notification.recipe) {
            for (TimedNotification timed : NOTIFICATIONS) {
                if (timed.notification.recipe) {
                    timed.notification.appendIcons(notification.itemIcons);
                    timed.expiresAt = now + notificationDuration(mc, timed.notification.itemIcons, true);
                    return;
                }
            }
        }

        NOTIFICATIONS.add(new TimedNotification(notification, now, now + notificationDuration(mc, notification.itemIcons, notification.recipe)));

        int maxNotifications = Math.max(1, LucidConfig.notificationMaxCount);
        while (NOTIFICATIONS.size() > maxNotifications) {
            NOTIFICATIONS.remove(0);
        }

        playToastIn();
    }

    public static void render(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != lastLevel) {
            lastLevel = mc.level;
            clear();
        }

        if (mc.player == null || mc.options.hideGui || NOTIFICATIONS.isEmpty()) {
            return;
        }

        long now = Util.getMillis();
        if (NOTIFICATIONS.removeIf(timed -> now >= timed.expiresAt)) {
            playToastOut();
        }

        if (NOTIFICATIONS.isEmpty()) {
            return;
        }

        double targetScale = GuiScale.targetScale(mc);
        float scaleMod = GuiScale.scaleModifier(mc);
        int screenWidth = (int) Math.ceil(mc.getWindow().getScreenWidth() / targetScale);
        int screenHeight = (int) Math.ceil(mc.getWindow().getScreenHeight() / targetScale);
        int limit = screenHeight - LucidConfig.notificationBottomScreenMargin;

        int notificationWidth = LucidConfig.notificationWidth;
        int height = Math.max(MIN_NOTIFICATION_HEIGHT, LucidConfig.notificationHeight);
        int x = (screenWidth - notificationWidth) / 2;
        int y = Math.max(0, LucidConfig.notificationTopMargin);

        Font font = mc.font;

        guiGraphics.pose().pushPose();

        if (scaleMod != 1.0f) {
            guiGraphics.pose().scale(scaleMod, scaleMod, 1.0f);
        }

        for (TimedNotification timed : NOTIFICATIONS) {
            if (y + height > limit) {
                break;
            }

            timed.advanceIcon(now);
            List<ItemStack> icons = timed.notification.itemIcons;
            ItemStack icon = icons.isEmpty() ? ItemStack.EMPTY : icons.get(timed.iconIndex);

            renderNotification(guiGraphics, font, timed.notification, icon, x, y, notificationWidth, height);
            y += height + LucidConfig.notificationSpacing;
        }

        guiGraphics.pose().popPose();
    }

    public static void renderEffects(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui || !LucidConfig.effectEnabled) {
            return;
        }

        List<MobEffectInstance> effects = visibleEffects(mc);
        if (effects.isEmpty()) {
            return;
        }

        double targetScale = GuiScale.targetScale(mc);
        float scaleMod = GuiScale.scaleModifier(mc);
        int screenWidth = (int) Math.ceil(mc.getWindow().getScreenWidth() / targetScale);
        int screenHeight = (int) Math.ceil(mc.getWindow().getScreenHeight() / targetScale);
        int limit = screenHeight - LucidConfig.effectBottomScreenMargin;

        int effectWidth = Math.max(1, LucidConfig.effectWidth);
        int rowHeight = Math.max(MIN_EFFECT_ROW_HEIGHT, LucidConfig.effectRowHeight);
        int spacing = Math.max(0, LucidConfig.effectRowSpacing);
        int startY = LucidConfig.effectStartY;
        int availableRows = (limit - startY + spacing) / (rowHeight + spacing);
        int rows = Math.min(Math.max(1, LucidConfig.effectMaxRows), availableRows);
        if (rows <= 0) {
            return;
        }

        int maxRight = screenWidth - LucidConfig.effectRightMargin;
        maxRight = Math.min(maxRight, screenWidth / 2 - spacing);
        if (!NOTIFICATIONS.isEmpty()) {
            maxRight = Math.min(maxRight, (screenWidth - LucidConfig.notificationWidth) / 2 - spacing);
        }

        Font font = mc.font;
        int rendered = 0;

        guiGraphics.pose().pushPose();

        if (scaleMod != 1.0f) {
            guiGraphics.pose().scale(scaleMod, scaleMod, 1.0f);
        }

        for (int i = 0; i < effects.size(); i++) {
            int column = i / rows;
            int row = i % rows;
            int x = Math.max(0, LucidConfig.effectLeftMargin) + column * (effectWidth + spacing);
            if (x + effectWidth > maxRight) {
                break;
            }

            int y = startY + row * (rowHeight + spacing);
            renderEffect(guiGraphics, font, mc, effects.get(i), x, y, effectWidth, rowHeight);
            rendered++;
        }

        if (rendered < effects.size()) {
            int hidden = effects.size() - rendered;
            int infoY = rendered == 0 ? startY : startY + rows * (rowHeight + spacing);
            if (infoY + font.lineHeight > limit) {
                infoY = Math.max(startY, limit - font.lineHeight);
            }

            guiGraphics.drawString(font, Component.translatable(Constants.MOD_ID + ".overlay.hidden_count", hidden),
                    Math.max(0, LucidConfig.effectLeftMargin), infoY, LucidConfig.effectSubtitleColor, true);
        }

        guiGraphics.pose().popPose();
    }

    public static void clear() {
        clearNotifications();
        effectCache = List.of();
        effectCacheAt = 0L;
        effectCacheLevel = null;
        effectCachePlayer = null;
    }

    private static void clearNotifications() {
        NOTIFICATIONS.clear();
    }

    private static long notificationDuration(Minecraft mc, List<ItemStack> icons, boolean recipe) {
        double multiplier = mc.options.notificationDisplayTime().get();
        long base = Math.max(1L, (long) (Math.max(1, LucidConfig.notificationDuration) * multiplier));
        return recipe ? Math.max(base, icons.size() * ICON_ROTATION_MS + 1000L) : base;
    }

    private static void playToastIn() {
        Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(SoundEvents.UI_TOAST_IN, 1.0F, 1.0F)
        );
    }

    private static void playToastOut() {
        Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(SoundEvents.UI_TOAST_OUT, 1.0F, 1.0F)
        );
    }

    private static void renderNotification(GuiGraphics guiGraphics, Font font, Notification notification, ItemStack icon, int x, int y, int width, int height) {
        guiGraphics.fill(x, y, x + width, y + height, LucidConfig.notificationBgColor);
        guiGraphics.fill(x, y, x + LucidConfig.notificationAccentWidth, y + height, notification.accent);

        int textY = y + (height - 18) / 2;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 50);

        if (!icon.isEmpty()) {
            guiGraphics.renderItem(icon, x + LucidConfig.notificationIconXOffset, y + (height - 16) / 2);
        }

        String title = font.plainSubstrByWidth(notification.title.getString(), width - LucidConfig.notificationTitleMaxWidthPadding);
        String subtitle = font.plainSubstrByWidth(notification.subtitle.getString(), width - LucidConfig.notificationTitleMaxWidthPadding);

        guiGraphics.drawString(font, title, x + LucidConfig.notificationTitleXOffset, textY, notification.titleColor, true);
        guiGraphics.drawString(font, subtitle, x + LucidConfig.notificationTitleXOffset, textY + 11, LucidConfig.notificationSubtitleColor, true);

        guiGraphics.pose().popPose();
    }

    private static void renderEffect(GuiGraphics guiGraphics, Font font, Minecraft mc, MobEffectInstance effect, int x, int y, int width, int rowHeight) {
        MobEffect value = effect.getEffect().value();
        int accent = value.isBeneficial() ? LucidConfig.effectBeneficialAccent
                : value.getCategory() == MobEffectCategory.HARMFUL ? LucidConfig.effectHarmfulAccent : LucidConfig.effectNeutralAccent;

        guiGraphics.fill(x, y, x + width, y + rowHeight, LucidConfig.effectBgColor);
        guiGraphics.fill(x, y, x + LucidConfig.effectAccentWidth, y + rowHeight, accent);

        int textY = y + (rowHeight - 18) / 2;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 50);
        guiGraphics.blit(x + LucidConfig.effectIconXOffset, y + (rowHeight - 16) / 2, 0, 16, 16,
                mc.getMobEffectTextures().get(effect.getEffect()));

        String name = font.plainSubstrByWidth(effectName(effect).getString(), width - LucidConfig.effectTitleMaxWidthPadding);
        String duration = MobEffectUtil.formatDuration(effect, 1.0F, tickRate(mc)).getString();

        guiGraphics.drawString(font, name, x + LucidConfig.effectTitleXOffset, textY, LucidConfig.effectTitleColor, true);
        guiGraphics.drawString(font, duration, x + LucidConfig.effectTitleXOffset, textY + 11, LucidConfig.effectSubtitleColor, true);

        guiGraphics.pose().popPose();
    }

    private static Component effectName(MobEffectInstance effect) {
        MutableComponent name = effect.getEffect().value().getDisplayName().copy();
        int amplifier = effect.getAmplifier();

        if (amplifier >= 1 && amplifier <= 9) {
            name.append(CommonComponents.SPACE)
                    .append(Component.translatable("enchantment.level." + (amplifier + 1)));
        }

        return name;
    }

    private static float tickRate(Minecraft mc) {
        return mc.level == null ? 20.0F : mc.level.tickRateManager().tickrate();
    }

    private static List<MobEffectInstance> visibleEffects(Minecraft mc) {
        if (mc.player == null) {
            return List.of();
        }

        long now = Util.getMillis();
        if (mc.level != effectCacheLevel || mc.player != effectCachePlayer || now - effectCacheAt >= EFFECT_CACHE_INTERVAL_MS) {
            effectCacheLevel = mc.level;
            effectCachePlayer = mc.player;
            effectCacheAt = now;

            List<MobEffectInstance> effects = new ArrayList<>();
            for (MobEffectInstance effect : mc.player.getActiveEffects()) {
                if (effect.showIcon()) {
                    effects.add(effect);
                }
            }

            effects.sort(Collections.reverseOrder());
            effectCache = effects;
        }

        return effectCache;
    }

    public interface Provider {
        Notification lucid$notification();
    }

    public static final class Notification {
        private final List<ItemStack> itemIcons;
        private final Component title;
        private final Component subtitle;
        private final int accent;
        private final int titleColor;
        private final boolean recipe;

        private Notification(List<ItemStack> itemIcons, Component title, Component subtitle, int accent, int titleColor, boolean recipe) {
            this.itemIcons = itemIcons;
            this.title = title;
            this.subtitle = subtitle;
            this.accent = accent;
            this.titleColor = titleColor;
            this.recipe = recipe;
        }

        public static Notification item(ItemStack icon, Component title, Component subtitle, int accent, int titleColor) {
            return new Notification(new ArrayList<>(List.of(icon)), title, subtitle, accent, titleColor, false);
        }

        public static Notification recipe(ItemStack icon, Component title, Component subtitle, int accent, int titleColor) {
            return new Notification(new ArrayList<>(List.of(icon)), title, subtitle, accent, titleColor, true);
        }

        private void appendIcons(List<ItemStack> icons) {
            int remaining = MAX_RECIPE_ICONS - itemIcons.size();
            if (remaining <= 0) {
                return;
            }

            itemIcons.addAll(icons.subList(0, Math.min(remaining, icons.size())));
        }
    }

    private static final class TimedNotification {
        private final Notification notification;
        private long expiresAt;
        private long nextIconAt;
        private int iconIndex;

        private TimedNotification(Notification notification, long now, long expiresAt) {
            this.notification = notification;
            this.expiresAt = expiresAt;
            this.nextIconAt = now + ICON_ROTATION_MS;
        }

        private void advanceIcon(long now) {
            int iconCount = notification.itemIcons.size();
            if (iconCount <= 1) {
                iconIndex = 0;
                return;
            }

            while (now >= nextIconAt) {
                iconIndex = (iconIndex + 1) % iconCount;
                nextIconAt += ICON_ROTATION_MS;
            }
        }
    }
}
