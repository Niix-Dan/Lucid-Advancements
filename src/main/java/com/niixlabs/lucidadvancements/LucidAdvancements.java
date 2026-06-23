package com.niixlabs.lucidadvancements;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(LucidAdvancements.MODID)
public class LucidAdvancements {
    public static final String MODID = "lucidadvancements";
    private static final Logger LOGGER = LogUtils.getLogger();

    public LucidAdvancements(IEventBus modEventBus, ModContainer modContainer) {

    }
}
