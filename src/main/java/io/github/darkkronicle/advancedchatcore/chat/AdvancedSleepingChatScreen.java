/*
 * Copyright (C) 2021-2022 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchatcore.chat;

import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.util.KeyCodes;
import fi.dy.masa.malilib.util.StringUtils;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;

public class AdvancedSleepingChatScreen extends AdvancedChatScreen  {

    public AdvancedSleepingChatScreen() {
        super("");
    }

    public void initGui() {
        super.initGui();
        ButtonGeneric stopSleep =
                new ButtonGeneric(
                        this.width / 2 - 100,
                        this.height - 40,
                        200,
                        20,
                        StringUtils.translate("multiplayer.stopSleeping"));
        this.addButton(stopSleep, (button, mouseButton) -> stopSleeping());
    }

    public void onClose() {
        this.stopSleeping();
    }

    public boolean keyPressed(KeyEvent input) {
        if (input.key() == KeyCodes.KEY_ESCAPE) {
            this.stopSleeping();
        } else if (input.key() == KeyCodes.KEY_ENTER || input.key() == KeyCodes.KEY_KP_ENTER) {
            String string = this.chatField.getValue().trim();
            if (!string.isEmpty()) {
                MessageSender.getInstance().sendMessage(string);
            }

            this.chatField.setText("");
            this.minecraft.gui.hud.getChat().resetChatScroll();
            // Prevents really weird interactions with chat history
            resetCurrentMessage();
            return true;
        }

        return super.keyPressed(input);
    }

    private void stopSleeping() {
        ClientPacketListener clientPlayNetworkHandler = this.minecraft.player.connection;
        clientPlayNetworkHandler.send(
                new ServerboundPlayerCommandPacket(
                        this.minecraft.player, ServerboundPlayerCommandPacket.Action.STOP_SLEEPING));
        GuiBase.openGui(null);
    }
}
