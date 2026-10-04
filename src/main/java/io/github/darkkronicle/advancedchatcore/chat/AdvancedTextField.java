/*
 * Copyright (C) 2021 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchatcore.chat;

import fi.dy.masa.malilib.util.KeyCodes;
import io.github.darkkronicle.advancedchatcore.config.ConfigStorage;
import io.github.darkkronicle.advancedchatcore.util.*;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.*;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class AdvancedTextField extends EditBox {

    private final static int MAX_HISTORY = 50;

    /**
     * Stores the last saved snapshot of the box. This ensures that not every character update is
     * put in, but instead groups.
     */
    private String lastSaved = "";

    /** Snapshots of chat box */
    private final List<String> history = new ArrayList<>();

    private int focusedTicks = 0;
    private List<Component> renderLines = new ArrayList<>();
    private Font textRenderer;
    private String suggestion = null;
    private int maxLength = 32;
    private int selectionEnd;
    private int selectionStart;
    // TODO Split?
    private BiFunction<String, Integer, FormattedCharSequence> renderTextProvider = (string, firstCharacterIndex) -> FormattedCharSequence.forward(string, Style.EMPTY);

    private int historyIndex = -1;

    public AdvancedTextField(Font textRenderer, int x, int y, int width, int height, Component text) {
        this(textRenderer, x, y, width, height, null, text);
    }

    public AdvancedTextField(
            Font textRenderer,
            int x,
            int y,
            int width,
            int height,
            @Nullable EditBox copyFrom,
            Component text) {
        super(textRenderer, x, y, width, height, copyFrom, text);
        history.add("");
        this.textRenderer = textRenderer;
        updateRender();
    }

    //@Override
    public void tick() {
        focusedTicks++;
    }

    @Override
    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
        super.setMaxLength(maxLength);
    }

    public static boolean isUndo(KeyEvent input) {
        // Undo (Ctrl + Z)
        return input.key() == KeyCodes.KEY_Z && input.hasControlDown() && !input.hasAltDown();
    }

    /** Triggers undo for the text box */
    public void undo() {
        // Save the current snapshot if it's been edited
        if (!this.lastSaved.equals(this.getValue()) && historyIndex < 0) {
            addToHistory(getValue());
        }
        // History index < 0 means not in the middle of undoing
        if (historyIndex < 0) {
            historyIndex = history.size() - 1;
        }
        // Check that we're not at index 0
        if (historyIndex != 0) {
            historyIndex--;
        }
        // Set the text but don't update
        setText(history.get(historyIndex), false);
    }

    public void redo() {
        if (historyIndex < 0 || historyIndex >= history.size() - 1) {
            // No stuff to redo...
            return;
        }
        historyIndex++;
        setText(history.get(historyIndex), false);
    }

    @Override
    public void insertText(String text) {
        super.insertText(text);
        updateHistory();
        updateRender();
    }

    @Override
    public void deleteChars(int characterOffset) {
        super.deleteChars(characterOffset);
        updateHistory();
        updateRender();
    }

    @Override
    public void setSuggestion(@Nullable String suggestion) {
        this.suggestion = suggestion;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {

        int renderY = getY() - (renderLines.size() - 1) * (textRenderer.lineHeight + 2);
        if (click.button() < renderY - 2 || click.y() > getY() + height + 2 || click.x() < getX() - 2 || click.x() > getX() + width + 4) {
            return false;
        }
        return super.mouseClicked(click, doubled);
    }


    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        int color = 0xFFE0E0E0;
        int cursor = getCursorPosition();
        int cursorRow = renderLines.size() - 1;
        boolean renderCursor = this.isFocused() && focusedTicks / 6 % 2 == 0;
        int renderY = getY() - (renderLines.size() - 1) * (textRenderer.lineHeight + 2);
        int endX = 0;
        int charCount = 0;
        int cursorX = -1;
        boolean selection = selectionStart != selectionEnd;
        boolean started = false;
        boolean ended = false;
        int selStart;
        int selEnd;
        if (this.selectionStart < this.selectionEnd) {
            selStart = this.selectionStart;
            selEnd = this.selectionEnd;
        } else {
            selStart = this.selectionEnd;
            selEnd = this.selectionStart;
        }
        int x = getX();
        int y = getY()    ;
        context.fill(getX() - 2, renderY - 2, getX() + width + 4, getY() + height + 4, ConfigStorage.ChatScreen.COLOR.config.getIntegerValue());
        for (int line = 0; line < renderLines.size(); line++) {
            Component text = renderLines.get(line);
            if (cursor >= charCount && cursor < text.getString().length() + charCount) {
                cursorX = textRenderer.width(text.getString().substring(0, cursor - charCount));
                cursorRow = line;
            }
            endX = x + textRenderer.width(text);
            context.text(textRenderer, text, x, renderY, color);
            if (selection) {
                if (!started && selStart >= charCount && selStart <= text.getString().length() + charCount) {
                    started = true;
                    int startX = textRenderer.width(TextUtil.truncate(text, new StringMatch("", 0, selStart - charCount)));
                    if (selEnd > charCount && selEnd <= text.getString().length() + charCount) {
                        ended = true;
                        int sEndX = textRenderer.width(TextUtil.truncate(text, new StringMatch("", 0, selEnd - charCount)));
                        drawSelectionHighlight(context, x + startX, renderY - 1, x + sEndX, renderY + textRenderer.lineHeight);
                    } else {
                        int sEndX = textRenderer.width(text);
                        drawSelectionHighlight(context, x + startX, renderY - 1, x + sEndX, renderY + textRenderer.lineHeight);
                    }
                } else if (started && !ended) {
                    if (selEnd >= charCount && selEnd <= text.getString().length() + charCount) {
                        ended = true;
                        int sEndX = textRenderer.width(TextUtil.truncate(text, new StringMatch("", 0, selEnd - charCount)));
                        drawSelectionHighlight(context, x, renderY - 1, x + sEndX, renderY + textRenderer.lineHeight);
                    } else {
                        int sEndX = textRenderer.width(text);
                        drawSelectionHighlight(context, x, renderY - 1, x + sEndX, renderY + textRenderer.lineHeight);
                    }
                }
            }
            renderY += textRenderer.lineHeight + 2;
            charCount += text.getString().length();
        }
        if (cursorX < 0) {
            cursorX = endX;
        }
        boolean cursorAtEnd = getCursorPosition() == getValue().length();
        if (!cursorAtEnd && this.suggestion != null) {
            context.text(textRenderer, this.suggestion, endX - 1, y, -8355712);
        }
        if (renderCursor) {
            int cursorY = y - (renderLines.size() - 1 - cursorRow) * (textRenderer.lineHeight + 2);
            if (cursorAtEnd) {
                context.fill(cursorX, cursorY - 1, cursorX + 1, cursorY + 1 + this.textRenderer.lineHeight, -3092272);
            } else {
                context.text(textRenderer, "_", x + cursorX, cursorY, color);
            }
        }
    }

    private void drawSelectionHighlight(GuiGraphicsExtractor context, int x1, int y1, int x2, int y2) {
        int x = getX();
        int y = getY();
        int i;
        if (x1 < x2) {
            i = x1;
            x1 = x2;
            x2 = i;
        }
        if (y1 < y2) {
            i = y1;
            y1 = y2;
            y2 = i;
        }
        if (x2 > x + this.width) {
            x2 = x + this.width;
        }
        if (x1 > x + this.width) {
            x1 = x + this.width;
        }

        context.fill(RenderPipelines.GUI_TEXT_HIGHLIGHT, x1, y1, x2, y2, 0xFF0000FF);
    }

    @Override
    public void setCursorPosition(int cursor) {
        this.selectionStart = Mth.clamp(cursor, 0, getValue().length());
        super.setCursorPosition(cursor);
    }

    @Override
    public void setHighlightPos(int index) {
        int i = getValue().length();
        this.selectionEnd = Mth.clamp(index, 0, i);
        super.setHighlightPos(index);
    }

    /**
     * Sets the text for the text field
     *
     * @param text Component to set
     * @param update Updates the history
     */
    public void setText(String text, boolean update) {
        // Wrapper class for setText
        super.setValue(text);
        if (update) {
            updateHistory();
        }
        updateRender();
    }

    @Override
    public void setValue(String text) {
        setText(text, true);
    }

    /** Convenience wrapper kept for callers that used the old yarn name. */
    public void setText(String text) {
        setText(text, true);
    }

    private void updateRender() {
        FormattedCharSequence formatted = renderTextProvider.apply(getValue(), 0);
        renderLines = StyleFormatter.wrapText(textRenderer, getWidth(), new TextBuilder().append(formatted).build());
    }

    private void updateHistory() {
        if (historyIndex >= 0) {
            // Remove all history after what has gone back
            pruneHistory(historyIndex + 1);
            historyIndex = -1;
        }
        // Check to see if it should log
        int dif = getValue().length() - lastSaved.length();
        double sim = TextUtil.similarity(getValue(), lastSaved);
        if (sim >= .3 && (dif < 5 && dif * -1 < 5) || (sim >= .9)) {
            return;
        }
        addToHistory(getValue());
    }

    private void addToHistory(String text) {
        this.lastSaved = text;
        this.history.add(text);
        while (this.history.size() > MAX_HISTORY) {
            this.history.removeFirst();
        }
    }

    /**
     * Remove's all history past a certain index
     *
     * @param index Index to prune from. Non-inclusive.
     */
    private void pruneHistory(int index) {
        if (index == 0) {
            history.clear();
            return;
        }
        while (history.size() > index) {
            history.removeLast();
        }
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (!this.isActive()) {
            return false;
        }
        if (!isUndo(input)) {
            return super.keyPressed(input);
        }
        if (input.hasShiftDown()) {
            redo();
        } else {
            undo();
        }
        return true;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput builder) {
        // Crashes here because Component is null
    }

    @Override
    public void deleteWords(int wordOffset) {
        if (!this.getValue().isEmpty()) {
            if (this.selectionEnd != this.selectionStart) {
                this.setText("");
            } else {
                this.deleteChars(this.getWordPosition(wordOffset) - this.selectionStart);
            }
        }
    }
}
