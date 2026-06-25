package com.niixlabs.lucidadvancements;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class LucidAdvancements {
    public LucidAdvancements(IEventBus eventBus) {
        CommonClass.init();
    }
}