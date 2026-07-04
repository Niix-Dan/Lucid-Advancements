package com.niixlabs.lucidadvancements.client.gui.access;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;

import java.util.Map;

public interface AdvancementProgressAccess {
    Map<AdvancementHolder, AdvancementProgress> lucid$getProgressMap();
}