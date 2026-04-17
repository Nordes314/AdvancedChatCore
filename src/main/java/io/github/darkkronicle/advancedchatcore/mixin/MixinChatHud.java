/*
 * Copyright (C) 2021 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchatcore.mixin;

import io.github.darkkronicle.advancedchatcore.chat.AdvancedChatScreen;
import io.github.darkkronicle.advancedchatcore.chat.MessageDispatcher;
import io.github.darkkronicle.advancedchatcore.config.ConfigStorage;
import io.github.darkkronicle.advancedchatcore.util.ChatHudStyleHolder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ChatHud.class, priority = 1050)
public class MixinChatHud {

    @Shadow @Final private MinecraftClient client;

    @Inject(
            method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void addMessage(Text message, @Nullable MessageSignatureData signature, @Nullable MessageIndicator indicator, CallbackInfo ci) {
        MessageDispatcher.getInstance().handleText(message, signature, indicator);
        ci.cancel();
    }

    @Inject(method = "clear", at = @At("HEAD"), cancellable = true)
    private void clearMessages(boolean clearTextHistory, CallbackInfo ci) {
        if (!clearTextHistory) {
            return;
        }
        if (!ConfigStorage.General.CLEAR_ON_DISCONNECT.config.getBooleanValue()) {
            ci.cancel();
        }
    }

    @Inject(method = "isChatFocused", at = @At("HEAD"), cancellable = true)
    private void isChatFocused(CallbackInfoReturnable<Boolean> ci) {
        ci.setReturnValue(AdvancedChatScreen.PERMANENT_FOCUS || client.currentScreen instanceof AdvancedChatScreen);
    }

    @Inject(
            method = "render(Lnet/minecraft/client/gui/hud/ChatHud$Backend;IIZ)V",
            at = @At("RETURN")
    )
    private void captureHoveredStyle(ChatHud.Backend drawer, int windowHeight, int currentTick, boolean expanded, CallbackInfo ci) {
        // Check if it's the Interactable backend by checking if it's an instance of the accessor
        try {
            if (drawer instanceof ChatHudInteractableAccessor) {
                Style style = ((ChatHudInteractableAccessor) drawer).getStyle();
                ChatHudStyleHolder.setLastHoveredStyle(style);
            }
        } catch (Exception e) {
            ChatHudStyleHolder.setLastHoveredStyle(null);
        }
    }

    @Mixin(targets = "net.minecraft.client.gui.hud.ChatHud$Interactable")
    interface ChatHudInteractableAccessor {
        @org.spongepowered.asm.mixin.gen.Accessor("style")
        @Nullable
        Style getStyle();
    }
}
