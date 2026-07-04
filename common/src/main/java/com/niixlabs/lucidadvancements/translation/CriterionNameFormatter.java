package com.niixlabs.lucidadvancements.translation;

public final class CriterionNameFormatter {
    private CriterionNameFormatter() {}

    public static String stripNamespace(String raw) {
        int colonIndex = raw.indexOf(':');
        return colonIndex >= 0 ? raw.substring(colonIndex + 1) : raw;
    }

    public static String toReadableName(String raw) {
        return stripNamespace(raw).replace('_', ' ').toLowerCase();
    }

    public static String capitalizeWords(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for (String word : value.split(" ")) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
            }
        }
        return result.toString().trim();
    }

    public static String formatDisplayName(String rawCriterion) {
        return capitalizeWords(toReadableName(rawCriterion));
    }
}