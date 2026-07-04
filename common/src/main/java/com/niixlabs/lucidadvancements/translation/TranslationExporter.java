package com.niixlabs.lucidadvancements.translation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public final class TranslationExporter {
    private static final String EXPORT_DIRECTORY = "lucid_exported_translations";
    private static final String RECIPES_PREFIX = "recipes/";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private TranslationExporter() {}

    public static void exportCurrentAdvancements(Map<AdvancementNode, AdvancementProgress> progressMap) {
        Map<String, Map<String, String>> exports = new HashMap<>();

        for (Map.Entry<AdvancementNode, AdvancementProgress> entry : progressMap.entrySet()) {
            collectAdvancementEntries(entry.getKey(), entry.getValue(), exports);
        }

        writeExports(exports);
    }

    private static void collectAdvancementEntries(AdvancementNode node, AdvancementProgress progress,
                                                  Map<String, Map<String, String>> exports) {
        ResourceLocation id = node.holder().id();
        String path = id.getPath();
        if (path.startsWith(RECIPES_PREFIX)) {
            return;
        }

        String cleanPath = path.contains("/") ? path.substring(path.lastIndexOf('/') + 1) : path;
        Map<String, String> modEntries = exports.computeIfAbsent(id.getNamespace(), namespace -> new TreeMap<>());

        addCriteria(modEntries, cleanPath, progress.getCompletedCriteria());
        addCriteria(modEntries, cleanPath, progress.getRemainingCriteria());
    }

    private static void addCriteria(Map<String, String> modEntries, String cleanPath, Iterable<String> criteria) {
        for (String criterion : criteria) {
            if (criterion.toLowerCase().contains("recipe") || criterion.contains(":")) {
                continue;
            }
            String jsonKey = "requirements." + cleanPath + "." + criterion.toLowerCase();
            modEntries.put(jsonKey, CriterionNameFormatter.formatDisplayName(criterion));
        }
    }

    private static void writeExports(Map<String, Map<String, String>> exports) {
        File baseDir = new File(Minecraft.getInstance().gameDirectory, EXPORT_DIRECTORY);

        for (Map.Entry<String, Map<String, String>> entry : exports.entrySet()) {
            if (entry.getValue().isEmpty()) {
                continue;
            }

            File modDir = new File(baseDir, entry.getKey());
            if (!modDir.exists() && !modDir.mkdirs()) {
                continue;
            }

            try (FileWriter writer = new FileWriter(new File(modDir, "en_us.json"))) {
                GSON.toJson(entry.getValue(), writer);
            } catch (IOException ignored) {
            }
        }
    }
}