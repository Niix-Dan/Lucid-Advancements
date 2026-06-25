package com.niixlabs.lucidadvancements.mixin;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.multiplayer.ClientAdvancements;
import com.niixlabs.lucidadvancements.client.gui.AdvancementProgressAccess;
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
