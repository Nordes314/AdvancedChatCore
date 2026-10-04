package io.github.darkkronicle.advancedchatcore.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.input.InputQuirks;
import com.mojang.blaze3d.platform.InputConstants;

public class ModifierKeyUtil {
    public static boolean hasControlDown() {
        if (InputQuirks.REPLACE_CTRL_KEY_WITH_CMD_KEY) {
            return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), 343)
                    || InputConstants.isKeyDown(
                    Minecraft.getInstance().getWindow(),
                    347
            );
        } else {
            return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), 341)
                    || InputConstants.isKeyDown(
                    Minecraft.getInstance().getWindow(),
                    345
            );
        }
    }

    public static boolean hasShiftDown() {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), 340)
                || InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), 344);
    }

    public static boolean hasAltDown() {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), 342)
                || InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), 346);
    }
}
