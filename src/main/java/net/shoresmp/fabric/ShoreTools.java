/*
 * Decompiled with CFR 0.152.
 */
package net.shoresmp.fabric;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.class_1041;
import net.minecraft.class_1109;
import net.minecraft.class_1113;
import net.minecraft.class_1304;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_2561;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_339;
import net.minecraft.class_3417;
import net.minecraft.class_3675;
import net.minecraft.class_412;
import net.minecraft.class_4185;
import net.minecraft.class_419;
import net.minecraft.class_437;
import net.minecraft.class_442;
import net.minecraft.class_500;
import net.minecraft.class_639;
import net.minecraft.class_642;
import net.minecraft.class_6880;
import net.minecraft.class_746;
import net.shoresmp.core.PunishmentMatcher;
import net.shoresmp.fabric.Config;
import net.shoresmp.fabric.ShoreClient;
import net.shoresmp.fabric.ShoreHud;
import net.shoresmp.fabric.ShoreScreen;
import org.lwjgl.glfw.GLFW;

public final class ShoreTools {
    private static final class_1304[] WATCHED = new class_1304[]{class_1304.field_6173, class_1304.field_6171, class_1304.field_6169, class_1304.field_6174, class_1304.field_6172, class_1304.field_6166};
    private static final DateTimeFormatter WHEN = DateTimeFormatter.ofPattern("MMM d, HH:mm", Locale.ROOT);
    public static class_304 zoomKey;
    private static double zoom;
    private static double zoomTarget;
    private static double heldLevel;
    private static boolean wasZooming;
    private static boolean toggled;
    private static boolean zoomSetSmooth;
    private static long lastZoomFrame;
    private static final Map<String, Boolean> keyWasDown;
    private static final ArrayDeque<Long> leftClicks;
    private static final ArrayDeque<Long> rightClicks;
    private static final Map<class_1304, class_1792> lastItem;
    private static final Map<class_1304, Integer> lastPct;
    private static boolean wasDead;
    private static class_642 lastServer;
    public static volatile String lastDisconnectReason;
    private static int autoAttempts;

    private ShoreTools() {
    }

    private static boolean zooming() {
        class_310 client = class_310.method_1551();
        Config c = ShoreClient.config;
        if (zoomKey == null || !c.zoomEnabled || client.field_1755 != null || client.field_1724 == null) {
            return false;
        }
        return c.zoomToggle ? toggled : zoomKey.method_1434();
    }

    public static double fovDivisor() {
        zoomTarget = wasZooming ? heldLevel : 1.0;
        long now = System.nanoTime();
        double dt = lastZoomFrame == 0L ? 1.0 : Math.min(0.1, (double)(now - lastZoomFrame) / 1.0E9);
        lastZoomFrame = now;
        if (!ShoreClient.config.zoomSmooth) {
            zoom = zoomTarget;
        } else if (Math.abs((zoom += (zoomTarget - zoom) * Math.min(1.0, dt * 14.0)) - zoomTarget) < 0.01) {
            zoom = zoomTarget;
        }
        return zoom;
    }

    public static boolean scroll(double vertical) {
        if (!ShoreTools.zooming() || !ShoreClient.config.zoomScroll || vertical == 0.0) {
            return false;
        }
        double step = 1.0 + (double)ShoreClient.config.zoomScrollSpeed / 100.0;
        heldLevel = Math.clamp(heldLevel * (vertical > 0.0 ? step : 1.0 / step), 1.5, 50.0);
        return true;
    }

    private static void zoomTick(class_310 client) {
        boolean active;
        Config c = ShoreClient.config;
        while (zoomKey != null && zoomKey.method_1436()) {
            if (!c.zoomToggle) continue;
            toggled = !toggled;
        }
        if (!c.zoomToggle || client.field_1724 == null) {
            boolean bl = toggled = c.zoomToggle && toggled && client.field_1724 != null;
        }
        if ((active = ShoreTools.zooming()) && !wasZooming) {
            heldLevel = c.zoomLevel;
            boolean bl = zoomSetSmooth = c.zoomCinematic && !client.field_1690.field_1914;
            if (zoomSetSmooth) {
                client.field_1690.field_1914 = true;
            }
            wasZooming = true;
        } else if (!active && wasZooming) {
            wasZooming = false;
            ShoreTools.restoreZoomInput(client);
        }
    }

    public static double lookScale() {
        if (!wasZooming) {
            return 1.0;
        }
        Config c = ShoreClient.config;
        return c.zoomSensMode == 0 ? (double)c.zoomSensitivity / 100.0 : 1.0 / Math.max(1.0, heldLevel);
    }

    public static void restoreZoomInput(class_310 client) {
        if (zoomSetSmooth) {
            client.field_1690.field_1914 = false;
            zoomSetSmooth = false;
        }
    }

    private static void moduleKeysTick(class_310 client) {
        Map<String, String> keys = ShoreClient.config.moduleKeys;
        if (keys.isEmpty()) {
            return;
        }
        long window = client.method_22683().method_4490();
        for (Map.Entry<String, String> e : keys.entrySet()) {
            String status;
            class_3675.class_306 key = class_3675.method_15981((String)e.getValue());
            boolean down = key.method_1442() == class_3675.class_307.field_1672 ? GLFW.glfwGetMouseButton((long)window, (int)key.method_1444()) == 1 : class_3675.method_15987((class_1041)client.method_22683(), (int)key.method_1444());
            boolean was = keyWasDown.getOrDefault(e.getKey(), false);
            keyWasDown.put(e.getKey(), down);
            if (!down || was || client.field_1755 != null || client.field_1724 == null || (status = ShoreScreen.toggleById(e.getKey())) == null) continue;
            client.field_1705.method_1758((class_2561)class_2561.method_43470((String)status), false);
        }
    }

    public static void click(int button) {
        long now = System.currentTimeMillis();
        if (button == 0) {
            leftClicks.addLast(now);
        } else if (button == 1) {
            rightClicks.addLast(now);
        }
    }

    public static int cps(boolean left) {
        ArrayDeque<Long> q = left ? leftClicks : rightClicks;
        long cutoff = System.currentTimeMillis() - 1000L;
        while (!q.isEmpty() && q.peekFirst() < cutoff) {
            q.pollFirst();
        }
        return q.size();
    }

    public static void tick(class_310 client) {
        boolean dead;
        ShoreTools.zoomTick(client);
        ShoreTools.moduleKeysTick(client);
        class_746 player = client.field_1724;
        if (player == null) {
            lastItem.clear();
            lastPct.clear();
            wasDead = false;
            return;
        }
        Config c = ShoreClient.config;
        if (c.durabilityAlert) {
            for (class_1304 slot : WATCHED) {
                class_1799 s = player.method_6118(slot);
                if (s.method_7960() || !s.method_7963() || s.method_7936() <= 0) {
                    lastItem.remove(slot);
                    lastPct.remove(slot);
                    continue;
                }
                int left = s.method_7936() - s.method_7919();
                int pct = left * 100 / s.method_7936();
                Integer before = lastPct.get(slot);
                if (lastItem.get(slot) == s.method_7909() && before != null && before > c.durabilityThreshold && pct <= c.durabilityThreshold) {
                    if (c.durabilitySound) {
                        client.method_1483().method_4873((class_1113)class_1109.method_47978((class_6880)class_3417.field_14622, (float)0.6f));
                    }
                    if (c.durabilityChat) {
                        ShoreClient.tell(s.method_7964().getString() + " has " + left + " durability left");
                    } else {
                        client.field_1705.method_1758((class_2561)class_2561.method_43470((String)"\u26a0 ").method_54663(16757850).method_10852((class_2561)s.method_7964().method_27661().method_54663(0xFFFFFF)).method_10852((class_2561)class_2561.method_43470((String)(" has " + left + " durability left")).method_54663(16757850)), false);
                    }
                }
                lastItem.put(slot, s.method_7909());
                lastPct.put(slot, pct);
            }
        }
        if ((dead = player.method_29504()) && !wasDead && c.deathLog && client.field_1687 != null) {
            String dim = ShoreHud.title(client.field_1687.method_27983().method_29177().method_12832());
            String where = player.method_31477() + " " + player.method_31478() + " " + player.method_31479();
            String server = lastServer == null ? "Singleplayer" : ShoreTools.lastServer.field_3752;
            c.deaths.addFirst(LocalDateTime.now().format(WHEN) + "  \u00b7  " + dim + "  \u00b7  " + where + "  \u00b7  " + server);
            c.save();
            if (c.deathAnnounce) {
                ShoreClient.tell("You died at " + where + " in " + dim + ". Use /shore deaths for recent deaths.");
            }
        }
        wasDead = dead;
    }

    public static void joined(class_310 client) {
        class_642 info = client.method_1558();
        if (info != null && !info.method_52811()) {
            lastServer = info;
        }
        autoAttempts = 0;
        lastDisconnectReason = "";
    }

    private static void reconnect(class_310 client) {
        if (lastServer == null) {
            return;
        }
        class_412.method_36877((class_437)new class_500((class_437)new class_442()), (class_310)client, (class_639)class_639.method_2950((String)ShoreTools.lastServer.field_3761), (class_642)lastServer, (boolean)false, null);
    }

    public static void register() {
        ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
            if (!(screen instanceof class_419) || lastServer == null || !ShoreClient.config.reconnectButton) {
                return;
            }
            List buttons = Screens.getButtons((class_437)screen);
            int bx = w / 2 - 100;
            int by = -1;
            for (class_339 b2 : buttons) {
                if (b2.method_46427() + b2.method_25364() + 4 <= by) continue;
                bx = b2.method_46426() + (b2.method_25368() - 200) / 2;
                by = b2.method_46427() + b2.method_25364() + 4;
            }
            if (by < 0) {
                by = h / 2 + 40;
            }
            Config c = ShoreClient.config;
            String reason = lastDisconnectReason == null ? "" : lastDisconnectReason;
            boolean auto = c.autoReconnect && autoAttempts < 5 && !PunishmentMatcher.matches((String)reason, c.punishmentPhrases);
            int[] ticks = new int[]{auto ? c.autoReconnectDelay * 20 : -1};
            class_4185 button = class_4185.method_46430((class_2561)ShoreTools.label(lastServer, ticks[0]), b -> {
                ticks[0] = -1;
                ShoreTools.reconnect(client);
            }).method_46434(bx, by, 200, 20).method_46431();
            buttons.add(button);
            class_4185[] cancel = new class_4185[]{null};
            if (auto) {
                cancel[0] = class_4185.method_46430((class_2561)class_2561.method_43470((String)"Cancel auto-reconnect"), b -> {
                    ticks[0] = -1;
                    button.method_25355(ShoreTools.label(lastServer, -1));
                    b.field_22764 = false;
                }).method_46434(bx, by + 24, 200, 20).method_46431();
                buttons.add(cancel[0]);
            }
            ScreenEvents.afterTick((class_437)screen).register(s -> {
                if (ticks[0] < 0) {
                    return;
                }
                ticks[0] = ticks[0] - 1;
                if (ticks[0] <= 0) {
                    ticks[0] = -1;
                    ++autoAttempts;
                    ShoreTools.reconnect(client);
                    return;
                }
                if (ticks[0] % 20 == 0) {
                    button.method_25355(ShoreTools.label(lastServer, ticks[0]));
                }
            });
        });
    }

    private static class_2561 label(class_642 info, int ticks) {
        Object name;
        Object object = name = info.field_3752 == null || info.field_3752.isBlank() ? info.field_3761 : info.field_3752;
        if (((String)name).length() > 22) {
            name = ((String)name).substring(0, 21) + "...";
        }
        if (ticks < 0) {
            return class_2561.method_43470((String)("Reconnect to " + (String)name));
        }
        return class_2561.method_43470((String)("Reconnecting in " + Math.max(1, (ticks + 19) / 20) + "s..."));
    }

    static {
        zoom = 1.0;
        zoomTarget = 1.0;
        keyWasDown = new HashMap<String, Boolean>();
        leftClicks = new ArrayDeque();
        rightClicks = new ArrayDeque();
        lastItem = new EnumMap<class_1304, class_1792>(class_1304.class);
        lastPct = new EnumMap<class_1304, Integer>(class_1304.class);
        lastDisconnectReason = "";
    }
}

