/*
 * Decompiled with CFR 0.152.
 */
package net.shoresmp.fabric;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.class_1269;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_310;
import net.minecraft.class_746;
import net.shoresmp.fabric.Config;
import net.shoresmp.fabric.ShoreClient;

public final class ShorePvp {
    private static class_1309 target;
    private static long targetAt;
    private static final Map<UUID, Integer> pops;

    private ShorePvp() {
    }

    public static void register() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.method_8608() && player == class_310.method_1551().field_1724) {
                ShorePvp.onAttack(player, entity);
            }
            return class_1269.field_5811;
        });
    }

    private static void onAttack(class_1657 player, class_1297 entity) {
        long now = System.currentTimeMillis();
        if (entity instanceof class_1309) {
            class_1309 living;
            target = living = (class_1309)entity;
            targetAt = now;
        }
    }

    public static void tick(class_310 client) {
        class_746 player = client.field_1724;
        if (player == null) {
            target = null;
            pops.clear();
            return;
        }
        if (target != null && (target.method_31481() || !target.method_5805() || System.currentTimeMillis() - targetAt > 5000L)) {
            target = null;
        }
    }

    public static class_1309 target() {
        return target;
    }

    public static void onEntityStatus(class_1297 entity, byte status) {
        Config c = ShoreClient.config;
        class_310 client = class_310.method_1551();
        if (c == null || !(entity instanceof class_1657)) {
            return;
        }
        class_1657 p = (class_1657)entity;
        if (status == 35) {
            int n = pops.merge(p.method_5667(), 1, Integer::sum);
            if (c.totemPops && (p != client.field_1724 || c.totemPopsSelf)) {
                ShoreClient.tell(p.method_5477().getString() + " popped " + n + (n == 1 ? " totem" : " totems"));
            }
        } else if (status == 3) {
            Integer n = pops.remove(p.method_5667());
            if (c.totemPops && n != null && p != client.field_1724) {
                ShoreClient.tell(p.method_5477().getString() + " died after popping " + n + (n == 1 ? " totem" : " totems"));
            }
        }
    }

    static {
        pops = new HashMap<UUID, Integer>();
    }
}

