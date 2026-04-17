package io.github.darkkronicle.advancedchatcore.util;

import net.minecraft.text.Style;
import org.jspecify.annotations.Nullable;

public class ChatHudStyleHolder {
    @Nullable
    private static Style lastHoveredStyle = null;

    public static void setLastHoveredStyle(@Nullable Style style) {
        lastHoveredStyle = style;
    }

    @Nullable
    public static Style getLastHoveredStyle() {
        return lastHoveredStyle;
    }
}