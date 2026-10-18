package com.niixlabs.lucidadvancements.mixin;

import com.niixlabs.lucidadvancements.client.gui.access.AdvancementProgressAccess;
import com.niixlabs.lucidadvancements.config.category.CategoryConfigManager;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.AdvancementTree;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(ClientAdvancements.class)
public class ClientAdvancementsMixin implements AdvancementProgressAccess {
    @Shadow @Final private Map<AdvancementHolder, AdvancementProgress> progress;
    @Shadow @Final private AdvancementTree tree;

    @Override
    public Map<AdvancementHolder, AdvancementProgress> lucid$getProgressMap() {
        return this.progress;
    }

    @Inject(method = "update", at = @At("TAIL"))
    private void lucid$preloadCategories(ClientboundUpdateAdvancementsPacket packet, CallbackInfo ci) {
        List<AdvancementNode> roots = new ArrayList<>();
        for (AdvancementNode root : this.tree.roots()) {
            roots.add(root);
        }

        if (roots.isEmpty()) {
            return;
        }

        Minecraft.getInstance().execute(() -> CategoryConfigManager.preloadCategories(roots));
    }
}
