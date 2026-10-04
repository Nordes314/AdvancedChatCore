/*
 * Copyright (C) 2021-2022 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchatcore.chat;

import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.render.GuiContext;
import fi.dy.masa.malilib.util.KeyCodes;
import fi.dy.masa.malilib.util.data.Color4f;
import io.github.darkkronicle.advancedchatcore.AdvancedChatCore;
import io.github.darkkronicle.advancedchatcore.config.ConfigStorage;
import io.github.darkkronicle.advancedchatcore.config.gui.GuiConfigHandler;
import io.github.darkkronicle.advancedchatcore.gui.IconButton;
import io.github.darkkronicle.advancedchatcore.interfaces.AdvancedChatScreenSection;
import io.github.darkkronicle.advancedchatcore.util.ChatHudStyleHolder;
import io.github.darkkronicle.advancedchatcore.util.ModifierKeyUtil;
import io.github.darkkronicle.advancedchatcore.util.RowList;
import lombok.Getter;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class AdvancedChatScreen extends GuiBase {

    public static boolean PERMANENT_FOCUS = false;

    private String finalHistory = "";
    private int messageHistorySize = -1;
    private int startHistory = -1;
    private boolean passEvents = false;

    /** Chat field at the bottom of the screen */
    @Getter protected AdvancedTextField chatField;

    /** What the chat box started out with */
    @Getter private String originalChatText = "";

    private static String last = "";
    private final List<AdvancedChatScreenSection> sections = new ArrayList<>();

    @Getter
    private final RowList<ButtonBase> rightSideButtons = new RowList<>();

    @Getter
    private final RowList<ButtonBase> leftSideButtons = new RowList<>();

    @Override
    protected void closeGui(boolean showParent) {
        if (ConfigStorage.ChatScreen.PERSISTENT_TEXT.config.getBooleanValue()) {
            last = chatField.getValue();
        }
        super.closeGui(showParent);
    }

    public AdvancedChatScreen() {
        super();
        setupSections();
    }

    public AdvancedChatScreen(boolean passEvents) {
        this();
        this.passEvents = passEvents;
    }

    public AdvancedChatScreen(int indexOfLast) {
        this();
        startHistory = indexOfLast;
    }

    public AdvancedChatScreen(String originalChatText) {
        this();
        this.originalChatText = originalChatText;
    }

    private void setupSections() {
        for (Function<AdvancedChatScreen, AdvancedChatScreenSection> supplier : ChatScreenSectionHolder.getInstance().getSectionSuppliers()) {
            AdvancedChatScreenSection section = supplier.apply(this);
            if (section != null) {
                sections.add(section);
            }
        }
    }

    private Color4f getColor() {
        return ConfigStorage.ChatScreen.COLOR.config.getColor();
    }

    public void resetCurrentMessage() {
        this.messageHistorySize = this.minecraft.gui.hud.getChat().getRecentChat().size();
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        if (passEvents) {
            return true;
        }
        return super.charTyped(input);
    }

    public void initGui() {
        super.initGui();
        this.rightSideButtons.clear();
        this.leftSideButtons.clear();
        resetCurrentMessage();
        this.chatField =
                new AdvancedTextField(
                        this.font,
                        4,
                        this.height - 12,
                        this.width - 10,
                        12,
                        Component.translatable("chat.editBox")) {
                    protected MutableComponent getNarrationMessage() {
                        return null;
                    }
                };
        if (ConfigStorage.ChatScreen.MORE_TEXT.config.getBooleanValue()) {
            this.chatField.setMaxLength(64000);
        } else {
            this.chatField.setMaxLength(256);
        }
        this.chatField.setBordered(false);
        if (!this.originalChatText.isEmpty()) {
            this.chatField.setText(this.originalChatText);
        } else if (ConfigStorage.ChatScreen.PERSISTENT_TEXT.config.getBooleanValue()
                && !last.isEmpty()) {
            this.chatField.setText(last);
        }
        this.chatField.setResponder(this::onChatFieldUpdate);

        // Add settings button
        rightSideButtons.add("settings", new IconButton(0, 0, 14, 64, Identifier.fromNamespaceAndPath(AdvancedChatCore.MOD_ID, "textures/gui/settings.png"), (button) -> GuiBase.openGui(GuiConfigHandler.getInstance().getDefaultScreen())));

        this.addWidget(this.chatField);

        this.setInitialFocus(this.chatField);

        for (AdvancedChatScreenSection section : sections) {
            section.initGui();
        }

        int originalX = minecraft.getWindow().getGuiScaledWidth() - 1;
        int y = minecraft.getWindow().getGuiScaledHeight() - 30;
        for (int i = 0; i < rightSideButtons.rowSize(); i++) {
            List<ButtonBase> buttonList = rightSideButtons.get(i);
            int maxHeight = 0;
            int x = originalX;
            for (ButtonBase button : buttonList) {
                maxHeight = Math.max(maxHeight, button.getHeight());
                x -= button.getWidth() + 1;
                button.setPosition(x, y);
                addButton(button, null);
            }
            y -= maxHeight + 1;
        }
        originalX = 1;
        y = minecraft.getWindow().getGuiScaledHeight() - 30;
        for (int i = 0; i < leftSideButtons.rowSize(); i++) {
            List<ButtonBase> buttonList = leftSideButtons.get(i);
            int maxHeight = 0;
            int x = originalX;
            for (ButtonBase button : buttonList) {
                maxHeight = Math.max(maxHeight, button.getHeight());
                button.setPosition(x, y);
                addButton(button, null);
                x += button.getWidth() + 1;
            }
            y -= maxHeight + 1;
        }
        if (startHistory >= 0) {
            setChatFromHistory(-startHistory - 1);
        }

    }

    public void resize(int width, int height) {
        String string = this.chatField.getValue();
        this.init(width, height);
        this.setText(string);
        for (AdvancedChatScreenSection section : sections) {
            section.resize(width, height);
        }
    }

    @Override
    public void removed() {
        for (AdvancedChatScreenSection section : sections) {
            section.removed();
        }
    }

    public void tick() {
        this.chatField.tick();
    }

    private void onChatFieldUpdate(String chatText) {
        String string = this.chatField.getValue();
        for (AdvancedChatScreenSection section : sections) {
            section.onChatFieldUpdate(chatText, string);
        }
    }

    @Override
    public boolean keyReleased(KeyEvent input) {
        if (passEvents) {
            InputConstants.Key key = InputConstants.getKey(input);
            KeyMapping.set(key, false);
        }
        return false;
    }

    public boolean keyPressed(KeyEvent input) {
        if (!passEvents) {
            for (AdvancedChatScreenSection section : sections) {
                if (section.keyPressed(input)) {
                    return true;
                }
            }
            if (super.keyPressed(input)) {
                return true;
            }
        }
        if (input.key() == KeyCodes.KEY_ESCAPE) {
            // Exit out
            GuiBase.openGui(null);
            return true;
        }
        if (input.key() == KeyCodes.KEY_ENTER || input.key() == KeyCodes.KEY_KP_ENTER) {
            String string = this.chatField.getValue().trim();
            // Strip message and send
            MessageSender.getInstance().sendMessage(string);
            this.chatField.setText("");
            last = "";
            // Exit
            GuiBase.openGui(null);
            return true;
        }
        if (input.key() == KeyCodes.KEY_UP) {
            // Go through previous history
            this.setChatFromHistory(-1);
            return true;
        }
        if (input.key() == KeyCodes.KEY_DOWN) {
            // Go through previous history
            this.setChatFromHistory(1);
            return true;
        }
        if (input.key() == KeyCodes.KEY_PAGE_UP) {
            // Scroll
            minecraft.gui.hud
                    .getChat()
                    .scrollChat(this.minecraft.gui.hud.getChat().getLinesPerPage() - 1);
            return true;
        }
        if (input.key() == KeyCodes.KEY_PAGE_DOWN) {
            // Scroll
            minecraft.gui.hud
                    .getChat()
                    .scrollChat(-this.minecraft.gui.hud.getChat().getLinesPerPage() + 1);
            return true;
        }
        if (passEvents) {
            this.chatField.setText("");
            InputConstants.Key key = InputConstants.getKey(input);
            KeyMapping.set(key, true);
            KeyMapping.click(key);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (verticalAmount > 1.0D) {
            verticalAmount = 1.0D;
        }

        if (verticalAmount < -1.0D) {
            verticalAmount = -1.0D;
        }

        for (AdvancedChatScreenSection section : sections) {
            if (section.mouseScrolled(mouseX, mouseY, verticalAmount)) {
                return true;
            }
        }
        if (!ModifierKeyUtil.hasShiftDown()) {
            verticalAmount *= 7.0D;
        }

        // Send to hud to scroll
        minecraft.gui.hud.getChat().scrollChat((int) verticalAmount);
        return true;
    }

    @Override
    protected void drawScreenBackground(GuiContext ctx, int mouseX, int mouseY)
    {
        // malilib apparently changed it so that it has a nice black background in newer versions, ive removed it here because it screws up the chat
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        for (AdvancedChatScreenSection section : sections) {
            if (section.mouseClicked(click, doubled)) {
                return true;
            }
        }

        Style style = ChatHudStyleHolder.getLastHoveredStyle();
        if (style != null) {
            if (style.getClickEvent() != null) {
                Screen.defaultHandleGameClickEvent(style.getClickEvent(), this.minecraft, this);
                return true;
            }
        }

        if (super.mouseClicked(click, doubled)) {
            return true;
        }

        return this.chatField.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent click,  double deltaX, double deltaY) {
        for (AdvancedChatScreenSection section : sections) {
            if (section.mouseDragged(click, deltaX, deltaY)) {
                return true;
            }
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    protected void insertText(String text, boolean override) {
        if (override) {
            this.chatField.setText(text);
        } else {
            this.chatField.insertText(text);
        }
    }

    public void setChatFromHistory(int i) {
        int targetIndex = this.messageHistorySize + i;
        int maxIndex = this.minecraft.gui.hud.getChat().getRecentChat().size();
        targetIndex = Mth.clamp(targetIndex, 0, maxIndex);
        if (targetIndex != this.messageHistorySize) {
            if (targetIndex == maxIndex) {
                this.messageHistorySize = maxIndex;
                this.chatField.setText(this.finalHistory);
            } else {
                if (this.messageHistorySize == maxIndex) {
                    this.finalHistory = this.chatField.getValue();
                }

                String hist = this.minecraft.gui.hud.getChat().getRecentChat().get(targetIndex);
                this.chatField.setText(hist);
                for (AdvancedChatScreenSection section : sections) {
                    section.setChatFromHistory(hist);
                }
                this.messageHistorySize = targetIndex;
            }
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float partialTicks) {
        // Render ChatComponent FIRST (before GuiBase does anything)
        ChatComponent hud = this.minecraft.gui.hud.getChat();
        hud.extractRenderState(context, this.minecraft.font, this.minecraft.gui.hud.getGuiTicks(), mouseX, mouseY,
                ChatComponent.DisplayMode.FOREGROUND, true);

        // Then render chat field
        this.setFocused(this.chatField);
        this.chatField.setFocused(true);
        this.chatField.extractRenderState(context, mouseX, mouseY, partialTicks);

        // Then buttons (from GuiBase)
        super.extractRenderState(context, mouseX, mouseY, partialTicks);

        // Custom sections last
        for (AdvancedChatScreenSection section : sections) {
            section.extractRenderState(context, mouseX, mouseY, partialTicks);
        }
    }

    private void setText(String text) {
        this.chatField.setText(text);
    }


}
