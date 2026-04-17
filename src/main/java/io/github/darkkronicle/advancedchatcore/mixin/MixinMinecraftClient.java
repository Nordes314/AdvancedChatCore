/*
 * Copyright (C) 2021-2022 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchatcore.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.darkkronicle.advancedchatcore.chat.AdvancedChatScreen;
import io.github.darkkronicle.advancedchatcore.chat.AdvancedSleepingChatScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.screen.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {

    @Inject(method = "openChatScreen",
            at = @At(value = "HEAD"), cancellable = true)
    public void openChatScreen(ChatHud.ChatMethod method, CallbackInfo ci) {
        MinecraftClient.getInstance().setScreen(new AdvancedChatScreen(method.getReplacement()));
        ci.cancel();
    }

    @WrapOperation(method = "tick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/hud/ChatHud;setClientScreen(Lnet/minecraft/client/gui/hud/ChatHud$ChatMethod;Lnet/minecraft/client/gui/screen/ChatScreen$Factory;)V"))
    public void openSleepingChatScreen(ChatHud instance, ChatHud.ChatMethod method, ChatScreen.Factory<?> factory, Operation<Void> original) {
        MinecraftClient.getInstance().setScreen(new AdvancedSleepingChatScreen());
    }
}
