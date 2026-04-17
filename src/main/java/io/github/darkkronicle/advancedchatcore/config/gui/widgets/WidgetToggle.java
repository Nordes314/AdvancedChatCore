/*
 * Copyright (C) 2021 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchatcore.config.gui.widgets;

import fi.dy.masa.malilib.gui.button.ButtonOnOff;
import lombok.Getter;
import net.minecraft.client.gui.Click;

public class WidgetToggle extends ButtonOnOff {

    @Getter private boolean currentlyOn;

    public WidgetToggle(
            int x,
            int y,
            int width,
            boolean rightAlign,
            String translationKey,
            boolean isCurrentlyOn,
            String... hoverStrings) {
        super(x, y, width, rightAlign, translationKey, isCurrentlyOn, hoverStrings);
        this.currentlyOn = isCurrentlyOn;
    }

    @Override
    protected boolean onMouseClickedImpl(Click click, boolean doubled) {
        this.currentlyOn = !this.currentlyOn;
        this.updateDisplayString(this.currentlyOn);
        return super.onMouseClickedImpl(click, doubled);
    }

    public void setOn(boolean on) {
        this.currentlyOn = on;
        this.updateDisplayString(on);
    }
}
