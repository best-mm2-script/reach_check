package com.example.hitdistance.mixin;

import com.example.hitdistance.HitDistanceClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.EntityDamageS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
    @Inject(method = "onEntityDamage", at = @At("TAIL"))
    private void hitdistance$onDamage(EntityDamageS2CPacket packet, CallbackInfo ci) {
        HitDistanceClient.onDamage(packet);
    }
}
