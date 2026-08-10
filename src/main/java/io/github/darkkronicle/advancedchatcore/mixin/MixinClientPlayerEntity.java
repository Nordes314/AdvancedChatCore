package io.github.darkkronicle.advancedchatcore.mixin;

import io.github.darkkronicle.advancedchatcore.chat.AdvancedChatScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class MixinClientPlayerEntity extends Entity {

    @Shadow @Final protected Minecraft minecraft;

    @Shadow public float portalEffectIntensity;

    public MixinClientPlayerEntity(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Inject(
            method="handlePortalTransitionEffect",
            at = @At(value="INVOKE", target="Lnet/minecraft/client/gui/Gui;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V"),
            cancellable = true
    )
    public void tickNauseaHook(CallbackInfo ci) {
        if (minecraft.gui.screen() instanceof AdvancedChatScreen) {
            ci.cancel();
            portalEffectIntensity += 0.0125f;
            if (this.portalEffectIntensity >= 1.0f) {
                this.portalEffectIntensity = 1.0f;
            }
            if(this.portalProcess != null){
                portalProcess.setAsInsidePortalThisTick(false);
            }
        }
    }


}
