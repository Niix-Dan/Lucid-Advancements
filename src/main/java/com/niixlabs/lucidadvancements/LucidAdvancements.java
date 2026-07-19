package com.niixlabs.lucidadvancements;

import com.niixlabs.lucidadvancements.config.LucidConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(Constants.MOD_ID)
public class LucidAdvancements {
    public LucidAdvancements(IEventBus eventBus) {
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            LucidConfig.load();

            if (LucidConfig.useConfigWatcher) LucidConfig.startWatcher();
        }
    }
}
