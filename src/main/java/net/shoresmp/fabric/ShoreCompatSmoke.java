/*
 * Decompiled with CFR 0.152.
 */
package net.shoresmp.fabric;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.shoresmp.fabric.ShoreClient;
import net.shoresmp.fabric.ShoreScreen;
import net.shoresmp.fabric.ShoreSettingsChecks;

public final class ShoreCompatSmoke {
    private static final boolean ACTIVE = Boolean.getBoolean("shoremod.compatSmoke");
    private static long started;
    private static volatile int frames;
    private static boolean done;
    private static long lastOpen;
    private static int stage;

    public static void rendered() {
        if (ACTIVE) {
            ++frames;
        }
    }

    public static void install() {
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            ShoreSettingsChecks.run();
            started = System.nanoTime();
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (started == 0L || done) {
                return;
            }
            if (!(client.field_1755 instanceof ShoreScreen) && client.method_18506() == null && System.nanoTime() - lastOpen > 1000000000L) {
                lastOpen = System.nanoTime();
                ShoreClient.openNextTick(() -> new ShoreScreen(client.field_1755));
            }
            if (client.field_1755 instanceof ShoreScreen && client.method_18506() == null) {
                if (frames >= 20 && stage == 0) {
                    ((ShoreScreen)client.field_1755).showModule("fullbright");
                    ++stage;
                }
                if (frames >= 40 && stage == 1) {
                    ((ShoreScreen)client.field_1755).showModule("crosshair");
                    ++stage;
                }
                if (frames >= 60 && stage == 2) {
                    ((ShoreScreen)client.field_1755).showPage("GENERAL");
                    ++stage;
                }
                if (frames >= 100) {
                    done = true;
                    ShoreClient.LOG.info("SHORE_COMPAT_SMOKE_PASS target=1.21.11");
                    client.method_1592();
                }
            }
            if (!done && System.nanoTime() - started > 150000000000L) {
                done = true;
                ShoreClient.LOG.error("SHORE_COMPAT_SMOKE_FAIL target=1.21.11 screen={} overlay={} frames={}", new Object[]{client.field_1755, client.method_18506(), frames});
                client.method_1592();
            }
        });
    }
}

