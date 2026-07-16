package com.niixlabs.lucidadvancements.mixin;

import com.niixlabs.lucidadvancements.client.gui.access.AdvancementProgressAccess;
import com.niixlabs.lucidadvancements.client.gui.screen.LucidAdvancementsScreen;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(value = Screen.class, priority = 500)
public class ScreenMixin {
    @Inject(method = "handleComponentClicked", at = @At("HEAD"), cancellable = true)
    private void onHandleComponentClicked(Style style, CallbackInfoReturnable<Boolean> cir) {
        if (style != null && style.getHoverEvent() != null) {
            Minecraft mc = Minecraft.getInstance();
            HoverEvent hoverEvent = style.getHoverEvent();

            if (hoverEvent.getAction() == HoverEvent.Action.SHOW_TEXT) {
                Component advInfo = hoverEvent.getValue(HoverEvent.Action.SHOW_TEXT);

                if (advInfo != null && mc.player != null) {
                    String hoverText = advInfo.getString();
                    ResourceLocation advancementId = null;

                    Map<AdvancementHolder, AdvancementProgress> vanillaProgressMap =
                            ((AdvancementProgressAccess) mc.player.connection.getAdvancements()).lucid$getProgressMap();

                    for (AdvancementHolder holder : vanillaProgressMap.keySet()) {
                        var display = holder.value().display();

                        if (display.isPresent()) {
                            String desc = display.get().getDescription().getString();
                            String title = display.get().getTitle().getString();

                            if (!desc.isBlank() && hoverText.contains(desc) && hoverText.contains(title)) {
                                advancementId = holder.id();
                                break;
                            }
                        }
                    }

                    if (advancementId != null) {
                        LucidAdvancementsScreen.advancementToFocusOnOpen = advancementId;
                        mc.setScreen(new AdvancementsScreen(mc.player.connection.getAdvancements()));

                        cir.setReturnValue(true);
                    }
                }
            }
        }
    }
}
