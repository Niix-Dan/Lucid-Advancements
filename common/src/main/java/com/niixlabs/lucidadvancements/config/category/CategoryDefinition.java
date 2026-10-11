package com.niixlabs.lucidadvancements.config.category;

public final class CategoryDefinition {
    public String id;
    public String advancement_root;
    public String title;
    public String description;
    public CategoryIcon icon;
    public boolean enabled = true;

    public CategoryDefinition(String id, String advancementRoot, String title, String description, CategoryIcon icon) {
        this.id = id;
        this.advancement_root = advancementRoot;
        this.title = title;
        this.description = description;
        this.icon = icon;
    }
}