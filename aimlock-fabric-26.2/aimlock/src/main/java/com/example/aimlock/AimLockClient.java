package com.example.aimlock;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class AimLockClient implements ClientModInitializer {
    // ===== 설정값 =====
    private static final double RANGE = 20.0;      // 탐지 거리(블록)
    private static final boolean TARGET_MONSTERS = true;
    private static final boolean TARGET_PLAYERS = true;
    private static final boolean NEED_LINE_OF_SIGHT = true; // 벽 뒤 대상 제외
    private static final float SMOOTHNESS = 1.0f;  // 1.0 = 즉시 고정, 0.2 = 부드럽게 따라감

    private static KeyMapping toggleKey;
    private static boolean enabled = false;

    @Override
    public void onInitializeClient() {
        KeyMapping.Category category =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath("aimlock", "main"));

        toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.aimlock.toggle", InputConstants.Type.KEYSYM, InputConstants.KEY_Z, category));

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    private void onTick(Minecraft mc) {
        while (toggleKey.consumeClick()) {
            enabled = !enabled;
            if (mc.player != null) {
                mc.player.displayClientMessage(
                    Component.literal("AimLock: " + (enabled ? "ON" : "OFF")), true);
            }
        }
        if (!enabled || mc.player == null || mc.level == null || mc.screen != null) return;

        LocalPlayer me = mc.player;
        LivingEntity target = null;
        double best = RANGE * RANGE;

        for (Entity e : mc.level.entitiesForRendering()) {
            if (e == me || !(e instanceof LivingEntity le) || !le.isAlive()) continue;
            boolean ok = (TARGET_MONSTERS && e instanceof Monster)
                      || (TARGET_PLAYERS && e instanceof Player p && !p.isSpectator());
            if (!ok) continue;
            if (NEED_LINE_OF_SIGHT && !me.hasLineOfSight(le)) continue;
            double d = me.distanceToSqr(le);
            if (d < best) { best = d; target = le; }
        }
        if (target == null) return;

        Vec3 eye = me.getEyePosition();
        Vec3 aim = target.getEyePosition();
        double dx = aim.x - eye.x, dy = aim.y - eye.y, dz = aim.z - eye.z;
        double flat = Math.sqrt(dx * dx + dz * dz);

        float wantYaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float wantPitch = (float) -Math.toDegrees(Math.atan2(dy, flat));

        float yawDiff = wrap(wantYaw - me.getYRot());
        float pitchDiff = wantPitch - me.getXRot();

        me.setYRot(me.getYRot() + yawDiff * SMOOTHNESS);
        me.setXRot(Math.max(-90f, Math.min(90f, me.getXRot() + pitchDiff * SMOOTHNESS)));
    }

    private static float wrap(float a) {
        a %= 360f;
        if (a >= 180f) a -= 360f;
        if (a < -180f) a += 360f;
        return a;
    }
}
