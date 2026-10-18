package com.niixlabs.lucidadvancements.client.gui.card;

import com.niixlabs.lucidadvancements.Constants;
import net.minecraft.network.chat.Component;

public enum FilterMode {
    ALL(Constants.MOD_ID + ".gui.filter.all"),
    COMPLETED(Constants.MOD_ID + ".gui.filter.completed"),
    INCOMPLETE(Constants.MOD_ID + ".gui.filter.incomplete"),
    CHALLENGES(Constants.MOD_ID + ".gui.filter.challenges"),
    PARTIAL(Constants.MOD_ID + ".gui.filter.partial");

    private final String translationKey;

    FilterMode(String translationKey) {
        this.translationKey = translationKey;
    }

    public String getDisplayName() {
        return Component.translatable(translationKey).getString();
    }
}