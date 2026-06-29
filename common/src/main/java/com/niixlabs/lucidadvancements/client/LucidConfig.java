package com.niixlabs.lucidadvancements.client;

import net.minecraft.client.Minecraft;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LucidConfig {
    private static final Path CONFIG_PATH = Minecraft.getInstance().gameDirectory.toPath().resolve("config/lucidadvancements.properties");

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface ConfigOption {
        String comment() default "";
        boolean hex() default false;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface ConfigSection {
        String value();
    }

    @ConfigOption(comment = "Interface scale behavior. 0 = Auto/Vanilla, 1-2 = Fixed Custom Scale Factor")
    public static int customGuiScale = 0;

    @ConfigSection("HUD OVERLAY CONFIGURATION")
    @ConfigOption(comment = "Background fill color for the pinned advancement boxes", hex = true)
    public static int overlayBgColor = 0x880A0A0A;

    @ConfigOption(comment = "Vertical strip color for normal advancements pinned to HUD", hex = true)
    public static int overlayNormalAccent = 0xFF00FFAA;

    @ConfigOption(comment = "Vertical strip color for challenge advancements pinned to HUD", hex = true)
    public static int overlayChallengeAccent = 0xFFAA00FF;

    @ConfigOption(comment = "Text color for normal advancement titles on HUD", hex = true)
    public static int overlayTitleColor = 0xFFFFFFFF;

    @ConfigOption(comment = "Text color for challenge advancement titles on HUD", hex = true)
    public static int overlayChallengeTitleColor = 0xFFFF77FF;

    @ConfigSection("ADVANCEMENT CARDS MENU CONFIGURATION\n# Unobtained Normal Advancements")
    @ConfigOption(comment = "Background color gradient start", hex = true)
    public static int cardNormalBg1 = 0xAA1A1A1A;

    @ConfigOption(comment = "Background color gradient end", hex = true)
    public static int cardNormalBg2 = 0xAA121212;

    @ConfigOption(comment = "Border color", hex = true)
    public static int cardNormalBorder = 0x882A2A2A;

    @ConfigOption(comment = "Title text color", hex = true)
    public static int cardNormalTitle = 0xFFFFFFFF;

    @ConfigSection("Unobtained Challenge/Rare Advancements")
    @ConfigOption(comment = "Border color", hex = true)
    public static int cardRareBorder = 0xAA42245A;

    @ConfigOption(comment = "Title text color", hex = true)
    public static int cardRareTitle = 0xFFB388CC;

    @ConfigSection("Completed Normal Advancements")
    @ConfigOption(comment = "Background color gradient start", hex = true)
    public static int cardObtainedBg1 = 0xBB142E1F;

    @ConfigOption(comment = "Background color gradient end", hex = true)
    public static int cardObtainedBg2 = 0xBB0C1E14;

    @ConfigOption(comment = "Border color", hex = true)
    public static int cardObtainedBorder = 0xAA00FFAA;

    @ConfigOption(comment = "Title text color", hex = true)
    public static int cardObtainedTitle = 0xFF77FF77;

    @ConfigSection("Completed Challenge/Rare Advancements")
    @ConfigOption(comment = "Background color gradient start", hex = true)
    public static int cardObtainedRareBg1 = 0xBB2D1438;

    @ConfigOption(comment = "Background color gradient end", hex = true)
    public static int cardObtainedRareBg2 = 0xBB1E0C26;

    @ConfigOption(comment = "Border color", hex = true)
    public static int cardObtainedRareBorder = 0xAAAA00FF;

    @ConfigOption(comment = "Title text color", hex = true)
    public static int cardObtainedRareTitle = 0xFFFF77FF;

    public static void load() {
        try {
            if (!Files.exists(CONFIG_PATH)) {
                save();
                return;
            }

            Map<String, String> fileValues = new HashMap<>();
            try (BufferedReader reader = Files.newBufferedReader(CONFIG_PATH)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) continue;
                    String[] parts = line.split("=", 2);
                    if (parts.length == 2) {
                        fileValues.put(parts[0].trim(), parts[1].trim());
                    }
                }
            }

            for (Field field : LucidConfig.class.getDeclaredFields()) {
                if (field.isAnnotationPresent(ConfigOption.class)) {
                    ConfigOption option = field.getAnnotation(ConfigOption.class);
                    String key = field.getName();

                    if (fileValues.containsKey(key)) {
                        String rawValue = fileValues.get(key);

                        if (field.getType() == int.class) {
                            if (option.hex()) {
                                String clean = rawValue.replace("0x", "").replace("#", "").trim();
                                field.setInt(null, (int) Long.parseLong(clean, 16));
                            } else {
                                field.setInt(null, Integer.parseInt(rawValue));
                            }
                        } else if (field.getType() == boolean.class) {
                            field.setBoolean(null, Boolean.parseBoolean(rawValue));
                        } else if (field.getType() == String.class) {
                            field.set(null, rawValue);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(CONFIG_PATH)) {
                writer.write("# Lucid Advancements Configuration File\n");
                writer.write("# Colors use ARGB Hexadecimal format (e.g., 0xAARRGGBB)\n\n");

                for (Field field : LucidConfig.class.getDeclaredFields()) {
                    if (field.isAnnotationPresent(ConfigSection.class)) {
                        ConfigSection section = field.getAnnotation(ConfigSection.class);
                        writer.write("\n# --- " + section.value() + " ---\n");
                    }

                    if (field.isAnnotationPresent(ConfigOption.class)) {
                        ConfigOption option = field.getAnnotation(ConfigOption.class);
                        if (!option.comment().isEmpty()) {
                            writer.write("# " + option.comment() + "\n");
                        }

                        String key = field.getName();
                        Object value = field.get(null);

                        if (option.hex() && value instanceof Integer) {
                            writer.write(key + "=0x" + String.format("%08X", (Integer) value) + "\n");
                        } else {
                            writer.write(key + "=" + value + "\n");
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateAndSave(String fieldName, Object value) {
        try {
            Field field = LucidConfig.class.getDeclaredField(fieldName);
            field.set(null, value);

            if (!Files.exists(CONFIG_PATH)) {
                save();
                return;
            }

            ConfigOption option = field.getAnnotation(ConfigOption.class);
            String targetKey = field.getName();
            String newValueStr;

            if (option != null && option.hex() && value instanceof Integer) {
                newValueStr = "0x" + String.format("%08X", (Integer) value);
            } else {
                newValueStr = String.valueOf(value);
            }

            List<String> lines = Files.readAllLines(CONFIG_PATH);
            boolean updated = false;

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (!line.startsWith("#") && line.contains("=")) {
                    String[] parts = line.split("=", 2);
                    if (parts[0].trim().equals(targetKey)) {
                        lines.set(i, targetKey + "=" + newValueStr);
                        updated = true;
                        break;
                    }
                }
            }

            if (!updated) {
                lines.add(targetKey + "=" + newValueStr);
            }

            Files.write(CONFIG_PATH, lines);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}