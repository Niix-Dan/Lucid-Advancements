package com.niixlabs.lucidadvancements;

import com.niixlabs.lucidadvancements.config.LucidConfig;

public class CommonClass {
    public static void init() {
        LucidConfig.load();
        LucidConfig.startWatcher();
    }
}
