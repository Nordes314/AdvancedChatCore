/*
 * Copyright (C) 2021 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchatcore.config.gui.widgets;

import fi.dy.masa.malilib.gui.GuiTextFieldGeneric;
import io.github.darkkronicle.advancedchatcore.util.FindType;
import io.github.darkkronicle.advancedchatcore.util.SearchUtils;
import io.github.darkkronicle.advancedchatcore.util.StringMatch;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.Font;

public class WidgetIntBox extends GuiTextFieldGeneric {

    @Setter @Getter private Runnable apply = null;

    public WidgetIntBox(int x, int y, int width, int height, Font textRenderer) {
        super(x, y, width, height, textRenderer);
        this.setBordered(true);
    }

    /**
     * EditBox#setFilter was removed in 26.2, so the numbers-only rule is enforced by
     * letting the insert happen and rolling it back when the result isn't an integer.
     */
    @Override
    public void insertText(String text) {
        String before = this.getValue();
        int cursor = this.getCursorPosition();
        super.insertText(text);
        if (!isInteger(this.getValue())) {
            super.setValue(before);
            this.setCursorPosition(cursor);
        }
    }

    private static boolean isInteger(String text) {
        if (text.isEmpty()) {
            return true;
        }
        try {
            // Only allow numbers!
            Integer.valueOf(text);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public Integer getInt() {
        String text = this.getValue();
        if (text == null || text.length() == 0) {
            return null;
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            // Extra catch
            Optional<List<StringMatch>> omatches =
                    SearchUtils.findMatches(text, "[0-9]+", FindType.REGEX);
            if (!omatches.isPresent()) {
                return null;
            }
            for (StringMatch m : omatches.get()) {
                try {
                    return Integer.parseInt(m.match);
                } catch (NumberFormatException err) {
                    return null;
                }
            }
        }
        return null;
    }
}
