package com.niixlabs.lucidadvancements.mixin;

import com.niixlabs.lucidadvancements.client.gui.overlay.LucidNotificationOverlay;
import com.niixlabs.lucidadvancements.config.LucidConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.RecipeToast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(RecipeToast.class)
public abstract class RecipeToastMixin implements LucidNotificationOverlay.Provider {
    @Shadow @Final private List<RecipeHolder<?>> recipes;

    @Inject(method = "addOrUpdate", at = @At("HEAD"), cancellable = true)
    private static void lucid$addOrUpdate(ToastComponent toastComponent, RecipeHolder<?> recipe, CallbackInfo ci) {
        LucidNotificationOverlay.add(notification(recipe));
        ci.cancel();
    }

    @Override
    public LucidNotificationOverlay.Notification lucid$notification() {
        if (recipes.isEmpty()) {
            return null;
        }

        return notification(recipes.get(recipes.size() - 1));
    }

    private static LucidNotificationOverlay.Notification notification(RecipeHolder<?> recipe) {
        ItemStack icon = ItemStack.EMPTY;

        if (Minecraft.getInstance().level != null) {
            icon = recipe.value().getResultItem(Minecraft.getInstance().level.registryAccess());
        }
        if (icon.isEmpty()) {
            icon = recipe.value().getToastSymbol();
        }

        return LucidNotificationOverlay.Notification.recipe(
                icon,
                Component.translatable("recipe.toast.title"),
                Component.translatable("recipe.toast.description"),
                LucidConfig.notificationNormalAccent,
                LucidConfig.notificationTitleColor
        );
    }
}
