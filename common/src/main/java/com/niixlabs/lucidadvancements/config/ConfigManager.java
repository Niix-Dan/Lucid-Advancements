package com.niixlabs.lucidadvancements.config;

import net.minecraft.client.Minecraft;

import java.io.BufferedWriter;
import java.lang.reflect.Field;
import java.nio.file.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ConfigManager {
    private static final String FILE_NAME = "config/lucidadvancements.properties";
    private static final String HEADER = "# Lucid Advancements Configuration File\n# Colors use ARGB Hexadecimal format (e.g., 0xAARRGGBB)\n\n";

    private static long lastKnownModified = 0;
    private static Thread watcherThread;

    private ConfigManager() {}

    private static Path configPath() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve(FILE_NAME);
    }

    private static void updateModifiedTime(Path path) {
        try { lastKnownModified = Files.getLastModifiedTime(path).toMillis(); }
        catch (Exception ignored) {}
    }

    public static void load(Class<?> configClass) {
        Path path = configPath();
        try {
            if (!Files.exists(path)) {
                save(configClass);
                return;
            }

            Map<String, String> values = readProperties(path);
            for (Field field : configClass.getDeclaredFields()) {
                ConfigOption option = field.getAnnotation(ConfigOption.class);
                if (option == null) continue;

                String raw = values.get(field.getName());
                if (raw != null) applyValue(field, option, raw);
            }
            updateModifiedTime(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Map<String, String> readProperties(Path path) throws Exception {
        Map<String, String> values = new HashMap<>();
        for (String line : Files.readAllLines(path)) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            String[] parts = line.split("=", 2);
            if (parts.length == 2) values.put(parts[0].trim(), parts[1].trim());
        }
        return values;
    }

    private static void applyValue(Field field, ConfigOption option, String rawValue) throws Exception {
        Class<?> type = field.getType();
        if (type == int.class) field.setInt(null, option.hex() ? parseHex(rawValue) : Integer.parseInt(rawValue));
        else if (type == double.class) field.setDouble(null, Double.parseDouble(rawValue));
        else if (type == boolean.class) field.setBoolean(null, Boolean.parseBoolean(rawValue));
        else if (type == String.class) field.set(null, rawValue);
    }

    private static int parseHex(String rawValue) {
        return (int) Long.parseLong(rawValue.replace("0x", "").replace("#", "").trim(), 16);
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
            updateModifiedTime(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void writeSectionHeader(BufferedWriter writer, Field field) throws Exception {
        ConfigSection section = field.getAnnotation(ConfigSection.class);
        if (section != null) writer.write("\n# --- " + section.value() + " ---\n");
    }

    private static void writeOption(BufferedWriter writer, Field field) throws Exception {
        ConfigOption option = field.getAnnotation(ConfigOption.class);
        if (option == null) return;
        if (!option.comment().isEmpty()) writer.write("# " + option.comment() + "\n");

        Object value = field.get(null);
        String formatted = (option.hex() && value instanceof Integer val)
                ? "0x" + String.format("%08X", val) : String.valueOf(value);

        writer.write(field.getName() + "=" + formatted + "\n");
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
            String newValue = (option != null && option.hex() && value instanceof Integer val)
                    ? "0x" + String.format("%08X", val) : String.valueOf(value);

            rewriteLine(path, fieldName, newValue);
            updateModifiedTime(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void rewriteLine(Path path, String targetKey, String newValue) throws Exception {
        List<String> lines = Files.readAllLines(path);
        boolean updated = false;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.startsWith("#") || !line.contains("=")) continue;

            if (line.split("=", 2)[0].trim().equals(targetKey)) {
                lines.set(i, targetKey + "=" + newValue);
                updated = true;
                break;
            }
        }

        if (!updated) lines.add(targetKey + "=" + newValue);
        Files.write(path, lines);
    }

    public static void startWatcher(Class<?> configClass) {
        if (watcherThread != null && watcherThread.isAlive()) return;

        watcherThread = new Thread(() -> {
            try (WatchService watcher = FileSystems.getDefault().newWatchService()) {
                Path path = configPath();
                Path dir = path.getParent();

                while (dir == null || !Files.exists(dir)) Thread.sleep(1000);
                dir.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);

                while (!Thread.currentThread().isInterrupted()) {
                    WatchKey key = watcher.take();
                    boolean changed = key.pollEvents().stream()
                            .anyMatch(e -> ((Path) e.context()).getFileName().equals(path.getFileName()));

                    if (changed) {
                        Thread.sleep(150);
                        if (Files.getLastModifiedTime(path).toMillis() > lastKnownModified) {
                            load(configClass);
                        }
                    }
                    if (!key.reset()) break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        watcherThread.setDaemon(true);
        watcherThread.setName("LucidAdvancements-ConfigWatcher");
        watcherThread.start();
    }
}