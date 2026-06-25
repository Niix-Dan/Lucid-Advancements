package com.niixlabs.lucidadvancements.client.gui;

public enum SortMode {
    ALL("All"),
    COMPLETED("Completed"),
    INCOMPLETE("Incomplete"),
    CHALLENGES("Challenges");

    private final String displayName;

    SortMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public SortMode next() {
        return values()[(this.ordinal() + 1) % values().length];
    }
}
