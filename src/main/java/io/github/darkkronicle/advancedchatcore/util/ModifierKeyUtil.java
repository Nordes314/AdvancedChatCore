package io.github.darkkronicle.advancedchatcore.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.SystemKeycodes;
import net.minecraft.client.util.InputUtil;

public class ModifierKeyUtil {
    public static boolean hasControlDown() {
        if (SystemKeycodes.IS_MAC_OS) {
            return InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow(), 343)
                    || InputUtil.isKeyPressed(
                    MinecraftClient.getInstance().getWindow(),
                    347
            );
        } else {
            return InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow(), 341)
                    || InputUtil.isKeyPressed(
                    MinecraftClient.getInstance().getWindow(),
                    345
            );
        }
    }

    public static boolean hasShiftDown() {
        return InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow(), 340)
                || InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow(), 344);
    }

    public static boolean hasAltDown() {
        return InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow(), 342)
                || InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow(), 346);
    }
}
