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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessageSource;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ChatComponent.class, priority = 1050)
public class MixinChatHud {

    @Shadow @Final private Minecraft minecraft;

    @Inject(
            method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void addMessage(Component message, @Nullable MessageSignature signature,
            GuiMessageSource source, @Nullable GuiMessageTag indicator, CallbackInfo ci) {
        MessageDispatcher.getInstance().handleText(message, signature, indicator);
        ci.cancel();
    }

    @Inject(method = "clearMessages", at = @At("HEAD"), cancellable = true)
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
        ci.setReturnValue(AdvancedChatScreen.PERMANENT_FOCUS || minecraft.gui.screen() instanceof AdvancedChatScreen);
    }

    /**
     * 26.2 replaced ChatHud$Interactable (whose `style` field was read after render) with
     * ChatComponent$DrawingFocusedGraphicsAccess, which is a Consumer&lt;Style&gt; fed the
     * hovered style as the chat draws. Hooking that consumer is both simpler and more
     * direct than re-deriving the style after the fact.
     */
    @Mixin(targets = "net.minecraft.client.gui.components.ChatComponent$DrawingFocusedGraphicsAccess")
    public static class MixinDrawingFocusedGraphicsAccess {

        @Inject(method = "accept(Lnet/minecraft/network/chat/Style;)V", at = @At("RETURN"))
        private void advancedchatcore$captureHoveredStyle(
                @Nullable Style style, CallbackInfo ci) {
            ChatHudStyleHolder.setLastHoveredStyle(style);
        }
    }
}
