package com.niixlabs.lucidadvancements.config.category;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class CategoryConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private static final Map<String, ModCategoryData> MOD_DATA = new HashMap<>();
    private static final Map<String, CategoryDefinition> ROOT_INDEX = new HashMap<>();

    private static Path dataDir() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("config/lucid-advancements/data");
    }

    private static Path fileFor(String modId) {
        return dataDir().resolve(modId + ".json");
    }

    public static void ensureCategoryFor(AdvancementNode rootNode) {
        String rootId = rootNode.holder().id().toString();
        CategoryDefinition cached = ROOT_INDEX.get(rootId);
        if (cached != null && cached.localized) {
            return;
        }

        String modId = rootNode.holder().id().getNamespace();
        ModCategoryData data = MOD_DATA.computeIfAbsent(modId, CategoryConfigManager::loadOrCreate);

        CategoryDefinition existing = findByRoot(data, rootId);
        if (existing != null) {
            if (!existing.localized) {
                localizeFields(existing, rootNode);
                existing.localized = true;
                save(modId, data);
            }
            ROOT_INDEX.put(rootId, existing);
            return;
        }

        CategoryDefinition created = buildDefault(rootNode);
        data.categories.add(created);
        ROOT_INDEX.put(rootId, created);
        save(modId, data);
    }

    public static boolean isEnabled(ResourceLocation rootId) {
        return resolve(rootId).map(def -> def.enabled).orElse(true);
    }

    private static CategoryDefinition findByRoot(ModCategoryData data, String rootId) {
        for (CategoryDefinition def : data.categories) {
            if (rootId.equals(def.advancement_root)) {
                return def;
            }
        }
        return null;
    }

    private static CategoryDefinition buildDefault(AdvancementNode rootNode) {
        ResourceLocation rootId = rootNode.holder().id();
        String title = rootId.getPath();
        String description = "";
        String iconItemId = "minecraft:book";

        Optional<DisplayInfo> display = rootNode.holder().value().display();
        if (display.isPresent()) {
            title = toConfigText(display.get().getTitle());
            description = toConfigText(display.get().getDescription());
            ResourceLocation itemKey = BuiltInRegistries.ITEM.getKey(display.get().getIcon().getItem());
            if (itemKey != null) {
                iconItemId = itemKey.toString();
            }
        }

        return new CategoryDefinition(
                rootId.getPath(),
                rootId.toString(),
                title,
                description,
                CategoryIcon.ofItem(iconItemId)
        );
    }

    private static String toConfigText(Component component) {
        return localizeField(component.getString(), component);
    }

    private static void localizeFields(CategoryDefinition definition, AdvancementNode rootNode) {
        Optional<DisplayInfo> display = rootNode.holder().value().display();
        if (display.isEmpty()) {
            return;
        }
        definition.title = localizeField(definition.title, display.get().getTitle());
        definition.description = localizeField(definition.description, display.get().getDescription());
    }

    private static String localizeField(String stored, Component component) {
        if (component.getContents() instanceof TranslatableContents translatable && translatable.getArgs().length == 0) {
            return translatable.getKey();
        }
        return stored;
    }

    private static ModCategoryData loadOrCreate(String modId) {
        Path path = fileFor(modId);
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                ModCategoryData loaded = GSON.fromJson(reader, ModCategoryData.class);
                if (loaded != null) {
                    if (loaded.categories == null) loaded.categories = new java.util.ArrayList<>();
                    indexAll(loaded);
                    return loaded;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new ModCategoryData(modId);
    }

    private static void indexAll(ModCategoryData data) {
        for (CategoryDefinition def : data.categories) {
            if (def.advancement_root != null) {
                ROOT_INDEX.put(def.advancement_root, def);
            }
        }
    }

    private static void save(String modId, ModCategoryData data) {
        Path path = fileFor(modId);
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(data, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Optional<CategoryDefinition> resolve(ResourceLocation rootId) {
        return Optional.ofNullable(ROOT_INDEX.get(rootId.toString()));
    }

    public static ResolvedIcon resolveIcon(@Nullable CategoryDefinition def, ItemStack fallback) {
        if (def == null || def.icon == null) {
            return new ResolvedIcon.Item(fallback);
        }

        return switch (def.icon.type()) {
            case ITEM -> {
                Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(def.icon.value()));
                yield new ResolvedIcon.Item(item != null ? new ItemStack(item) : fallback);
            }
            case TEXTURE -> {
                String modId = def.advancement_root != null
                        ? ResourceLocation.parse(def.advancement_root).getNamespace()
                        : "minecraft";
                ResourceLocation texture = CategoryTextureManager.load(modId, def.icon.value());
                yield texture != null ? new ResolvedIcon.Texture(texture) : new ResolvedIcon.Item(fallback);
            }
        };
    }

    public static void reloadAll() {
        MOD_DATA.clear();
        ROOT_INDEX.clear();
        CategoryTextureManager.clearCache();
        CategoryAssetInitializer.ensureGlobalCategoryIcon();
    }
}