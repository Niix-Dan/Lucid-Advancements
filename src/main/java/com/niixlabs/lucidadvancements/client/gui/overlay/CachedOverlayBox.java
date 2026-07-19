package com.niixlabs.lucidadvancements.client.gui.overlay;

import java.util.List;

record CachedOverlayBox(List<String> rawRemaining, String titleText, List<String> critLines, int boxHeight, int titleColor) {
}