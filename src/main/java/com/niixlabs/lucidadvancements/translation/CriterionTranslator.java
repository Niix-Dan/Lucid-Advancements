package com.niixlabs.lucidadvancements.translation;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.locale.Language;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class CriterionTranslator {
    private static final String NAMESPACE = "lucidadvancements";
    private static final Gson GSON = new Gson();
    private static final String[] REGISTRY_PREFIXES = {
            "biome", "entity", "item", "block", "cat_variant", "frog_variant", "wolf_variant", "painting"
    };

    private static final Map<String, Map<String, String>> MOD_TRANSLATIONS = new HashMap<>();
    private static final Map<String, String> RESOLVED_CRITERIA = new HashMap<>();
    private static Language lastLanguageInstance = null;

    private CriterionTranslator() {}

    public static String resolve(Identifier advancementId, String rawCriterion) {
        Language currentLanguageInstance = Language.getInstance();
        if (currentLanguageInstance != lastLanguageInstance) {
            clearCache();
            lastLanguageInstance = currentLanguageInstance;
        }

        String currentLanguage = Minecraft.getInstance().getLanguageManager().getSelected();
        String cacheKey = advancementId + "|" + rawCriterion;
        return RESOLVED_CRITERIA.computeIfAbsent(cacheKey, key -> resolveUncached(advancementId, rawCriterion, currentLanguage));
    }

    private static String resolveUncached(Identifier advancementId, String rawCriterion, String currentLanguage) {
        String fromModFile = findModTranslation(advancementId, rawCriterion, currentLanguage);
        if (fromModFile != null) {
            return fromModFile;
        }
        String fromRegistry = findRegistryTranslation(rawCriterion);
        if (fromRegistry != null) {
            return fromRegistry;
        }
        return CriterionNameFormatter.formatDisplayName(rawCriterion);
    }

    private static String findModTranslation(Identifier advancementId, String criterionName, String currentLanguage) {
        String modId = advancementId.getNamespace();
        String path = advancementId.getPath();
        String cleanPath = path.contains("/") ? path.substring(path.lastIndexOf('/') + 1) : path;
        String jsonKey = "requirements." + cleanPath + "." + criterionName.toLowerCase();

        Map<String, String> translations = MOD_TRANSLATIONS.computeIfAbsent(modId, id -> loadModTranslations(id, currentLanguage));
        return translations.get(jsonKey);
    }

    private static String findRegistryTranslation(String rawCriterion) {
        int colonIndex = rawCriterion.indexOf(':');
        if (colonIndex < 0) {
            return null;
        }

        String registryId = extractIdentifier(rawCriterion, colonIndex);
        if (registryId == null) {
            return null;
        }

        int idColon = registryId.indexOf(':');
        String namespace = registryId.substring(0, idColon);
        String path = registryId.substring(idColon + 1);

        for (String prefix : REGISTRY_PREFIXES) {
            String key = prefix + "." + namespace + "." + path;
            if (I18n.exists(key)) {
                return I18n.get(key);
            }
        }
        return null;
    }

    private static String extractIdentifier(String text, int colonIndex) {
        int start = colonIndex;
        while (start > 0 && isIdentifierChar(text.charAt(start - 1))) {
            start--;
        }
        int end = colonIndex + 1;
        while (end < text.length() && isIdentifierChar(text.charAt(end))) {
            end++;
        }
        return start < end ? text.substring(start, end) : null;
    }

    private static boolean isIdentifierChar(char c) {
        return (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_' || c == '-' || c == '/' || c == '.';
    }

    private static Map<String, String> loadModTranslations(String modId, String language) {
        Map<String, String> translations = new HashMap<>();
        loadTranslationFile(modId, "en_us", translations);
        if (!language.equalsIgnoreCase("en_us")) {
            loadTranslationFile(modId, language, translations);
        }
        return translations;
    }

    private static void loadTranslationFile(String modId, String language, Map<String, String> target) {
        Identifier location = Identifier.fromNamespaceAndPath(
                NAMESPACE, "translations/" + modId + "/" + language.toLowerCase() + ".json");

        Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(location);
        if (resource.isEmpty()) {
            return;
        }

        try (Reader reader = resource.get().openAsReader()) {
            Map<String, String> loaded = GSON.fromJson(reader, new TypeToken<Map<String, String>>() {
            }.getType());
            if (loaded != null) {
                target.putAll(loaded);
            }
        } catch (Exception ignored) {
        }
    }

    public static void clearCache() {
        MOD_TRANSLATIONS.clear();
        RESOLVED_CRITERIA.clear();
    }
}