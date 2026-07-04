package com.niixlabs.lucidadvancements.mixin;

import com.niixlabs.lucidadvancements.client.gui.access.AdvancementProgressAccess;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.multiplayer.ClientAdvancements;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(ClientAdvancements.class)
public class ClientAdvancementsMixin implements AdvancementProgressAccess {
    @Shadow @Final private Map<AdvancementHolder, AdvancementProgress> progress;

    @Override
    public Map<AdvancementHolder, AdvancementProgress> lucid$getProgressMap() {
        return this.progress;
    }
}
