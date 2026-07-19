package com.niixlabs.lucidadvancements.client.cache;

import com.niixlabs.lucidadvancements.client.gui.screen.LucidAdvancementsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.fml.loading.FMLPaths;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class TrackedAdvancementsCache {
    private static final String FILE_NAME = "config/lucidadvancements.dat";
    private static final Map<String, Set<String>> CACHE = new HashMap<>();

    private static boolean loaded = false;
    private static String currentWorldId = null;

    private TrackedAdvancementsCache() {}

    public static void syncIfNeeded(Minecraft mc) {
        if (mc.player == null) {
            currentWorldId = null;
            return;
        }

        if (currentWorldId != null) {
            return;
        }

        String worldId = resolveWorldId(mc);
        if (worldId == null) {
            return;
        }

        ensureLoaded();
        currentWorldId = worldId;

        LucidAdvancementsScreen.TRACKED_ADVANCEMENTS.clear();
        LucidAdvancementsScreen.TRACKED_ADVANCEMENTS.addAll(CACHE.getOrDefault(worldId, Set.of()));
    }

    public static void persist() {
        if (currentWorldId == null) {
            return;
        }

        CACHE.put(currentWorldId, new HashSet<>(LucidAdvancementsScreen.TRACKED_ADVANCEMENTS));
        write();
    }

    private static String resolveWorldId(Minecraft mc) {
        if (mc.player == null) {
            return null;
        }

        if (mc.isLocalServer() && mc.getSingleplayerServer() != null) {
            Path root = mc.getSingleplayerServer().getWorldPath(LevelResource.ROOT);
            return "sp:" + root.toAbsolutePath().normalize();
        }

        ServerData server = mc.getCurrentServer();
        if (server != null && server.ip != null) {
            return "mp:" + server.ip.toLowerCase(Locale.ROOT);
        }

        return null;
    }

    private static void ensureLoaded() {
        if (loaded) {
            return;
        }
        loaded = true;

        Path path = filePath();
        if (!Files.exists(path)) {
            return;
        }

        try {
            for (String line : Files.readAllLines(path)) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                int separator = line.indexOf('=');
                if (separator < 0) continue;

                String worldId = line.substring(0, separator);
                String[] ids = line.substring(separator + 1).split(",");

                Set<String> tracked = new HashSet<>();
                for (String id : ids) {
                    if (!id.isBlank()) tracked.add(id);
                }
                CACHE.put(worldId, tracked);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void write() {
        Path path = filePath();
        try {
            Files.createDirectories(path.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                for (Map.Entry<String, Set<String>> entry : CACHE.entrySet()) {
                    if (entry.getValue().isEmpty()) continue;
                    writer.write(entry.getKey() + "=" + String.join(",", entry.getValue()) + "\n");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Path filePath() {
        return FMLPaths.GAMEDIR.get().resolve(FILE_NAME);
    }
}