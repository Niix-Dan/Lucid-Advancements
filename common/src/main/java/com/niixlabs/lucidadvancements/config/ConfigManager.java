package com.niixlabs.lucidadvancements.config;

import net.minecraft.client.Minecraft;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ConfigManager {
    private static final String FILE_NAME = "config/lucidadvancements.properties";
    private static final String HEADER =
            "# Lucid Advancements Configuration File\n# Colors use ARGB Hexadecimal format (e.g., 0xAARRGGBB)\n\n";

    private ConfigManager() {}

    private static Path configPath() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve(FILE_NAME);
    }

    public static void load(Class<?> configClass) {
        Path path = configPath();
        try {
            if (!Files.exists(path)) {
                save(configClass);
                return;
            }

            Map<String, String> fileValues = readProperties(path);

            for (Field field : configClass.getDeclaredFields()) {
                ConfigOption option = field.getAnnotation(ConfigOption.class);
                if (option == null) {
                    continue;
                }
                String rawValue = fileValues.get(field.getName());
                if (rawValue != null) {
                    applyValue(field, option, rawValue);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Map<String, String> readProperties(Path path) throws Exception {
        Map<String, String> values = new HashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    values.put(parts[0].trim(), parts[1].trim());
                }
            }
        }
        return values;
    }

    private static void applyValue(Field field, ConfigOption option, String rawValue) throws IllegalAccessException {
        if (field.getType() == int.class) {
            field.setInt(null, option.hex() ? parseHex(rawValue) : Integer.parseInt(rawValue));
        } else if (field.getType() == double.class) {
            field.setDouble(null, Double.parseDouble(rawValue));
        } else if (field.getType() == boolean.class) {
            field.setBoolean(null, Boolean.parseBoolean(rawValue));
        } else if (field.getType() == String.class) {
            field.set(null, rawValue);
        }
    }

    private static int parseHex(String rawValue) {
        String clean = rawValue.replace("0x", "").replace("#", "").trim();
        return (int) Long.parseLong(clean, 16);
    }

    public static void save(Class<?> configClass) {
        Path path = configPath();
        try {
            Files.createDirectories(path.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                writer.write(HEADER);
                for (Field field : configClass.getDeclaredFields()) {
                    writeSectionHeader(writer, field);
                    writeOption(writer, field);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void writeSectionHeader(BufferedWriter writer, Field field) throws Exception {
        ConfigSection section = field.getAnnotation(ConfigSection.class);
        if (section != null) {
            writer.write("\n# --- " + section.value() + " ---\n");
        }
    }

    private static void writeOption(BufferedWriter writer, Field field) throws Exception {
        ConfigOption option = field.getAnnotation(ConfigOption.class);
        if (option == null) {
            return;
        }
        if (!option.comment().isEmpty()) {
            writer.write("# " + option.comment() + "\n");
        }

        Object value = field.get(null);
        String formattedValue = (option.hex() && value instanceof Integer intValue)
                ? "0x" + String.format("%08X", intValue)
                : String.valueOf(value);

        writer.write(field.getName() + "=" + formattedValue + "\n");
    }

    public static void updateAndSave(Class<?> configClass, String fieldName, Object value) {
        Path path = configPath();
        try {
            Field field = configClass.getDeclaredField(fieldName);
            field.set(null, value);

            if (!Files.exists(path)) {
                save(configClass);
                return;
            }

            ConfigOption option = field.getAnnotation(ConfigOption.class);
            String newValue = (option != null && option.hex() && value instanceof Integer intValue)
                    ? "0x" + String.format("%08X", intValue)
                    : String.valueOf(value);

            rewriteLine(path, fieldName, newValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void rewriteLine(Path path, String targetKey, String newValue) throws Exception {
        List<String> lines = Files.readAllLines(path);
        boolean updated = false;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.startsWith("#") || !line.contains("=")) {
                continue;
            }
            String key = line.split("=", 2)[0].trim();
            if (key.equals(targetKey)) {
                lines.set(i, targetKey + "=" + newValue);
                updated = true;
                break;
            }
        }

        if (!updated) {
            lines.add(targetKey + "=" + newValue);
        }

        Files.write(path, lines);
    }
}