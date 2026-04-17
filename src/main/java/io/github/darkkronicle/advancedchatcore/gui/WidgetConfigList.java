/*
 * Copyright (C) 2021 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchatcore.gui;

import fi.dy.masa.malilib.gui.GuiTextFieldGeneric;
import fi.dy.masa.malilib.gui.interfaces.ISelectionListener;
import fi.dy.masa.malilib.gui.widgets.WidgetListBase;
import fi.dy.masa.malilib.gui.wrappers.TextFieldWrapper;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;

public abstract class WidgetConfigList<TYPE, WIDGET extends WidgetConfigListEntry<TYPE>>
        extends WidgetListBase<TYPE, WIDGET> {

    protected List<TextFieldWrapper<GuiTextFieldGeneric>> textFields = new ArrayList<>();

    public WidgetConfigList(
            int x,
            int y,
            int width,
            int height,
            ISelectionListener<TYPE> selectionListener,
            Screen parent) {
        super(x, y, width, height, selectionListener);
        this.browserEntryHeight = 22;
        this.setParent(parent);
    }

    @Override
    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
        this.clearElements();
        this.clearAndInit();
    }

    @Override
    protected void reCreateListEntryWidgets() {
        textFields.clear();
        super.reCreateListEntryWidgets();
    }

    public void addTextField(TextFieldWrapper<GuiTextFieldGeneric> text) {
        textFields.add(text);
    }

    protected void clearTextFieldFocus() {
        for (TextFieldWrapper<GuiTextFieldGeneric> field : this.textFields) {
            GuiTextFieldGeneric textField = field.textField();
            textField.setFocused(false); // Just unfocus all of them
        }
    }

    @Override
    public boolean onMouseClicked(Click click, boolean doubled) {
        clearTextFieldFocus();
        return super.onMouseClicked(click, doubled);
    }

    @Override
    public boolean onKeyTyped(KeyInput input) {
        for (WidgetConfigListEntry<TYPE> widget : this.listWidgets) {
            if (widget.onKeyTyped(input)) {
                return true;
            }
        }
        return super.onKeyTyped(input);
    }
}
