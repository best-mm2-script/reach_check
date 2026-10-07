package com.example.hitdistance;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.network.packet.s2c.play.EntityDamageS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Locale;

public class HitDistanceClient implements ClientModInitializer {
    private static boolean enabled = true;

    @Override
    public void onInitializeClient() {
        // /reach toggles the display on or off
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registry) ->
            dispatcher.register(ClientCommandManager.literal("reach").executes(ctx -> {
                enabled = !enabled;
                ctx.getSource().sendFeedback(Text.literal("Hit distance display: " + (enabled ? "ON" : "OFF")));
                return 1;
            })));
    }

    /** Called from the mixin whenever the server tells us an entity took damage. */
    public static void onDamage(EntityDamageS2CPacket packet) {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity me = mc.player;
        ClientWorld world = mc.world;
        if (!enabled || me == null || world == null) return;
        if (packet.entityId() != me.getId()) return; // only when WE got hurt

        DamageSource source = packet.createDamageSource(world);
        Entity attacker = source.getAttacker();
        if (attacker == null || attacker == me) return; // fall damage, fire, etc.

        // Reach = from the attacker's eyes to the closest point of your hitbox
        // (this is what Minecraft's attack range actually measures).
        Vec3d eye = attacker.getEyePos();
        Box box = me.getBoundingBox();
        Vec3d closest = new Vec3d(
                MathHelper.clamp(eye.x, box.minX, box.maxX),
                MathHelper.clamp(eye.y, box.minY, box.maxY),
                MathHelper.clamp(eye.z, box.minZ, box.maxZ));
        double reach = eye.distanceTo(closest);
        double center = attacker.distanceTo(me);

        String name = attacker.getName().getString();
        boolean projectile = source.getSource() != null && source.getSource() != attacker;

        String r = String.format(Locale.ROOT, "%.2f", reach);
        String c = String.format(Locale.ROOT, "%.2f", center);

        Text chat = Text.literal("[Reach] ").formatted(Formatting.RED)
                .append(Text.literal(name + (projectile ? " shot you from " : " hit you from ")).formatted(Formatting.WHITE))
                .append(Text.literal(r + " blocks").formatted(Formatting.YELLOW))
                .append(Text.literal(" (center to center " + c + ")").formatted(Formatting.GRAY));
        me.sendMessage(chat, false);

        // short version above the hotbar
        me.sendMessage(Text.literal(name + ": " + r).formatted(Formatting.YELLOW), true);
    }
}
