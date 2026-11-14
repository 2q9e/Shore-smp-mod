/*
 * Decompiled with CFR 0.152.
 */
package net.shoresmp.fabric;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.imageio.ImageIO;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.class_1041;
import net.minecraft.class_1132;
import net.minecraft.class_11909;
import net.minecraft.class_11910;
import net.minecraft.class_1267;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1452;
import net.minecraft.class_1657;
import net.minecraft.class_1661;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1928;
import net.minecraft.class_1934;
import net.minecraft.class_1935;
import net.minecraft.class_1940;
import net.minecraft.class_2168;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2561;
import net.minecraft.class_2586;
import net.minecraft.class_2595;
import net.minecraft.class_276;
import net.minecraft.class_303;
import net.minecraft.class_310;
import net.minecraft.class_312;
import net.minecraft.class_318;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.class_338;
import net.minecraft.class_3908;
import net.minecraft.class_408;
import net.minecraft.class_437;
import net.minecraft.class_465;
import net.minecraft.class_476;
import net.minecraft.class_490;
import net.minecraft.class_5285;
import net.minecraft.class_5317;
import net.minecraft.class_7701;
import net.minecraft.class_7712;
import net.minecraft.class_9288;
import net.minecraft.class_9334;
import net.shoresmp.fabric.ClipSavedScreen;
import net.shoresmp.fabric.Config;
import net.shoresmp.fabric.ShoreClient;
import net.shoresmp.fabric.ShoreHud;
import net.shoresmp.fabric.ShorePvp;
import net.shoresmp.fabric.ShoreScreen;
import net.shoresmp.fabric.ShoreTools;

final class ShoreSelfTest {
    private static final Deque<Step> steps = new ArrayDeque<Step>();
    private static final List<String> report = new ArrayList<String>();
    private static int countdown = -1;
    private static int sx;
    private static int sy;
    private static double zoomSens;
    private static double zoomSensWhile;
    private static double zoomScaleWhile;

    private ShoreSelfTest() {
    }

    static void register() {
        String mode = System.getProperty("shoremod.selftest", "");
        if (mode.isEmpty()) {
            return;
        }
        ShoreClient.LOG.warn("ShoreMod self test enabled: {}", (Object)mode);
        if (mode.equals("ui")) {
            ShoreSelfTest.planUi();
        } else if (mode.equals("world")) {
            ShoreSelfTest.planWorld();
        }
        ClientTickEvents.END_CLIENT_TICK.register(ShoreSelfTest::tick);
    }

    private static void step(int delay, Consumer<class_310> action) {
        steps.add(new Step(delay, c -> true, action));
    }

    private static void await(Predicate<class_310> ready, int delay) {
        steps.add(new Step(delay, ready, c -> {}));
    }

    private static void tick(class_310 client) {
        Step s = steps.peek();
        if (s == null) {
            return;
        }
        if (countdown < 0) {
            countdown = s.delay();
        }
        if (countdown > 0) {
            --countdown;
            return;
        }
        if (!s.ready().test(client)) {
            return;
        }
        steps.poll();
        countdown = -1;
        try {
            s.action().accept(client);
        }
        catch (Throwable t) {
            ShoreSelfTest.log("ERROR in step: " + String.valueOf(t));
            ShoreClient.LOG.error("Self test step failed", t);
        }
    }

    private static void log(String line) {
        report.add(line);
        ShoreClient.LOG.warn("[selftest] {}", (Object)line);
    }

    private static void shot(class_310 c, String name) {
        class_318.method_22690((File)c.field_1697, (String)("selftest_" + name + ".png"), (class_276)c.method_1522(), (int)1, t -> {});
    }

    private static void finish(class_310 c) {
        try {
            Files.write(c.field_1697.toPath().resolve("shoremod-selftest.txt"), report, new OpenOption[0]);
        }
        catch (Exception e) {
            ShoreClient.LOG.error("report", (Throwable)e);
        }
        c.method_1592();
    }

    private static void scale(class_310 c, int s) {
        c.field_1690.method_42474().method_41748(Integer.valueOf(s));
        c.method_15993();
    }

    private static void planUi() {
        ShoreSelfTest.enterWorld();
        ShoreSelfTest.step(40, c -> {});
        for (int s : new int[]{2, 3}) {
            ShoreSelfTest.step(2, c -> ShoreSelfTest.scale(c, s));
            ShoreSelfTest.step(4, c -> {
                if (!(c.field_1755 instanceof ShoreScreen)) {
                    c.method_1507((class_437)new ShoreScreen(null));
                }
                ((ShoreScreen)c.field_1755).showGrid("All", "");
            });
            ShoreSelfTest.step(12, c -> ShoreSelfTest.shot(c, "gui" + s + "_grid"));
            ShoreSelfTest.step(2, c -> {
                ShoreScreen sc = (ShoreScreen)c.field_1755;
                sc.method_25401((double)sc.field_22789 * 0.5, (double)sc.field_22790 * 0.7, 0.0, -30.0);
            });
            ShoreSelfTest.step(20, c -> ShoreSelfTest.shot(c, "gui" + s + "_grid_scrolled"));
            ShoreSelfTest.step(2, c -> ((ShoreScreen)c.field_1755).showGrid("HUD", ""));
            ShoreSelfTest.step(10, c -> ShoreSelfTest.shot(c, "gui" + s + "_hud"));
            ShoreSelfTest.step(2, c -> ((ShoreScreen)c.field_1755).showGrid("All", "zzz"));
            ShoreSelfTest.step(10, c -> ShoreSelfTest.shot(c, "gui" + s + "_empty"));
            for (String id : new String[]{"zoom", "crosshair", "shulker", "items", "fps"}) {
                ShoreSelfTest.step(2, c -> ((ShoreScreen)c.field_1755).showModule(id));
                ShoreSelfTest.step(10, c -> ShoreSelfTest.shot(c, "gui" + s + "_module_" + id));
            }
            ShoreSelfTest.step(2, c -> {
                ShoreClient.config.crosshairMode = 1;
                ((ShoreScreen)c.field_1755).showModule("crosshair");
            });
            ShoreSelfTest.step(10, c -> ShoreSelfTest.shot(c, "gui" + s + "_module_crosshair_image"));
            ShoreSelfTest.step(2, c -> {
                ShoreClient.config.crosshairMode = 0;
                ShoreScreen sc = (ShoreScreen)c.field_1755;
                sc.showModule("crosshair");
                sc.method_25401((double)sc.field_22789 * 0.5, (double)sc.field_22790 * 0.7, 0.0, -30.0);
            });
            ShoreSelfTest.step(20, c -> ShoreSelfTest.shot(c, "gui" + s + "_module_crosshair_scrolled"));
            for (String p : new String[]{"GENERAL", "CONTROLS", "UPDATES"}) {
                ShoreSelfTest.step(2, c -> ((ShoreScreen)c.field_1755).showPage(p));
                ShoreSelfTest.step(10, c -> ShoreSelfTest.shot(c, "gui" + s + "_page_" + p.toLowerCase()));
            }
            ShoreSelfTest.step(2, c -> ((ShoreScreen)c.field_1755).showGrid("All", ""));
        }
        ShoreSelfTest.step(2, c -> c.method_1507((class_437)new ClipSavedScreen(null, c.field_1697.toPath().resolve("clips/x.avi"), 28.4)));
        ShoreSelfTest.step(10, c -> ShoreSelfTest.shot(c, "dialog_clip"));
        ShoreSelfTest.step(2, c -> c.method_1507(null));
        ShoreSelfTest.step(20, c -> ShoreSelfTest.shot(c, "hud_ui"));
        ShoreSelfTest.step(4, c -> {
            ShoreSelfTest.log("UI screenshots done");
            ShoreSelfTest.finish(c);
        });
    }

    private static void planWorld() {
        ShoreSelfTest.enterWorld();
        ShoreSelfTest.planInventory();
    }

    private static void enterWorld() {
        ShoreSelfTest.await(c -> c.method_18506() == null && c.field_1755 != null, 200);
        ShoreSelfTest.step(0, c -> {
            c.field_1690.field_1837 = false;
            String name = "ShoreSelfTest-" + System.currentTimeMillis();
            class_1940 info = new class_1940(name, class_1934.field_9215, false, class_1267.field_5801, true, new class_1928(class_7701.field_40183), class_7712.field_40260);
            c.method_41735().method_41895(name, info, class_5285.method_64223(), class_5317::method_64225, null);
        });
        ShoreSelfTest.await(c -> c.field_1724 != null && c.field_1687 != null && c.method_1576() != null && c.field_1755 == null, 100);
    }

    private static void planInventory() {
        ShoreSelfTest.step(0, c -> {
            Config cfg = ShoreClient.config;
            cfg.hudCoords = true;
            cfg.hudBiome = true;
            cfg.hudClock = true;
            cfg.mouseTweaks = true;
            cfg.tweakRmb = true;
            cfg.tweakLmb = true;
            cfg.tweakShift = true;
            cfg.tweakWheel = true;
            class_1132 server = c.method_1576();
            UUID id = c.field_1724.method_5667();
            server.execute(() -> {
                class_3222 sp = server.method_3760().method_14602(id);
                class_1661 inv = sp.method_31548();
                inv.method_5448();
                inv.method_5447(9, new class_1799((class_1935)class_1802.field_8831, 64));
                inv.method_5447(10, new class_1799((class_1935)class_1802.field_20412, 64));
                inv.method_5447(11, new class_1799((class_1935)class_1802.field_20412, 64));
                inv.method_5447(12, new class_1799((class_1935)class_1802.field_20412, 32));
                inv.method_5447(13, new class_1799((class_1935)class_1802.field_8583, 10));
                inv.method_5447(14, new class_1799((class_1935)class_1802.field_8583, 10));
                inv.method_5447(15, new class_1799((class_1935)class_1802.field_8583, 10));
                inv.method_5447(18, new class_1799((class_1935)class_1802.field_8858, 64));
                inv.method_5447(19, new class_1799((class_1935)class_1802.field_8858, 64));
                inv.method_5447(20, new class_1799((class_1935)class_1802.field_8858, 16));
                inv.method_5447(21, new class_1799((class_1935)class_1802.field_8110, 10));
                inv.method_5447(22, new class_1799((class_1935)class_1802.field_8110, 10));
                inv.method_5447(23, new class_1799((class_1935)class_1802.field_8110, 10));
                inv.method_5447(0, new class_1799((class_1935)class_1802.field_8377));
                class_2338 pos = sp.method_24515().method_10078();
                class_3218 world = sp.method_51469();
                world.method_8501(pos, class_2246.field_10034.method_9564());
                class_2586 patt0$temp = world.method_8321(pos);
                if (patt0$temp instanceof class_2595) {
                    class_2595 chest = (class_2595)patt0$temp;
                    sp.method_17355((class_3908)chest);
                }
            });
        });
        ShoreSelfTest.await(c -> c.field_1755 instanceof class_476, 10);
        ShoreSelfTest.step(10, c -> {
            class_465 screen = (class_465)c.field_1755;
            sx = ShoreSelfTest.field(screen, "x");
            sy = ShoreSelfTest.field(screen, "y");
            ShoreSelfTest.log("Chest open. All input below goes through Minecraft's real GLFW mouse callbacks.");
            ShoreSelfTest.pickup(c, 27);
        });
        ShoreSelfTest.step(2, c -> {});
        ShoreSelfTest.step(4, c -> {
            ShoreSelfTest.moveTo(c, 3);
            ShoreSelfTest.button(c, 1, 0, true);
        });
        ShoreSelfTest.step(6, c -> ShoreSelfTest.moveTo(c, 4));
        ShoreSelfTest.step(6, c -> ShoreSelfTest.moveTo(c, 5));
        ShoreSelfTest.step(6, c -> ShoreSelfTest.button(c, 1, 0, false));
        ShoreSelfTest.step(8, c -> {
            int a = ShoreSelfTest.count(c, 3);
            int b = ShoreSelfTest.count(c, 4);
            int d = ShoreSelfTest.count(c, 5);
            int cur = ShoreSelfTest.cursor(c).method_7947();
            ShoreSelfTest.log(ShoreSelfTest.verdict(a == 1 && b == 1 && d == 1 && cur == 61) + " right-click drag: chest slots 3/4/5 = " + a + "/" + b + "/" + d + " (want 1/1/1), cursor = " + cur + " (want 61)");
            ShoreSelfTest.pickup(c, 27);
        });
        ShoreSelfTest.step(6, c -> ShoreSelfTest.pickup(c, 31));
        ShoreSelfTest.step(4, c -> {
            ShoreSelfTest.moveTo(c, 32);
            ShoreSelfTest.button(c, 0, 0, true);
        });
        ShoreSelfTest.step(6, c -> ShoreSelfTest.moveTo(c, 33));
        ShoreSelfTest.step(6, c -> ShoreSelfTest.button(c, 0, 0, false));
        ShoreSelfTest.step(8, c -> {
            int cur = ShoreSelfTest.cursor(c).method_7947();
            int left = ShoreSelfTest.count(c, 32) + ShoreSelfTest.count(c, 33);
            ShoreSelfTest.log(ShoreSelfTest.verdict(cur == 30 && left == 0) + " left-click collect: cursor = " + cur + " logs (want 30), left in slots = " + left + " (want 0)");
            ShoreSelfTest.pickup(c, 10);
        });
        ShoreSelfTest.step(6, c -> {
            ShoreSelfTest.moveTo(c, 28);
            ShoreSelfTest.button(c, 0, 1, true);
        });
        ShoreSelfTest.step(6, c -> ShoreSelfTest.moveTo(c, 29));
        ShoreSelfTest.step(6, c -> ShoreSelfTest.moveTo(c, 30));
        ShoreSelfTest.step(6, c -> ShoreSelfTest.button(c, 0, 1, false));
        ShoreSelfTest.step(8, c -> {
            int inChest = 0;
            for (int i = 0; i < 27; ++i) {
                if (!ShoreSelfTest.stack(c, i).method_31574(class_1802.field_20412)) continue;
                inChest += ShoreSelfTest.count(c, i);
            }
            int left = ShoreSelfTest.count(c, 28) + ShoreSelfTest.count(c, 29) + ShoreSelfTest.count(c, 30);
            ShoreSelfTest.log(ShoreSelfTest.verdict(inChest == 160 && left == 0) + " shift-drag: cobblestone in chest = " + inChest + " (want 160), left in inventory = " + left + " (want 0)");
        });
        ShoreSelfTest.step(4, c -> {
            ShoreSelfTest.moveTo(c, 10);
            ShoreSelfTest.scroll(c, -1.0);
        });
        ShoreSelfTest.step(12, c -> {
            int chest = ShoreSelfTest.count(c, 10);
            int inv = 0;
            for (int i = 27; i < 63; ++i) {
                if (!ShoreSelfTest.stack(c, i).method_31574(class_1802.field_8583)) continue;
                inv += ShoreSelfTest.count(c, i);
            }
            ShoreSelfTest.log(ShoreSelfTest.verdict(chest == 29 && inv == 1) + " wheel down: chest logs = " + chest + " (want 29), inventory logs = " + inv + " (want 1)");
            ShoreSelfTest.scroll(c, 1.0);
        });
        ShoreSelfTest.step(12, c -> {
            int chest = ShoreSelfTest.count(c, 10);
            ShoreSelfTest.log(ShoreSelfTest.verdict(chest == 30) + " wheel up: chest logs = " + chest + " (want 30)");
        });
        ShoreSelfTest.step(4, c -> {
            ShoreSelfTest.moveToBackground(c, 36);
            ShoreSelfTest.button(c, 0, 1, true);
        });
        ShoreSelfTest.step(6, c -> ShoreSelfTest.moveTo(c, 36));
        ShoreSelfTest.step(6, c -> ShoreSelfTest.moveTo(c, 37));
        ShoreSelfTest.step(6, c -> ShoreSelfTest.moveTo(c, 38));
        ShoreSelfTest.step(6, c -> ShoreSelfTest.button(c, 0, 1, false));
        ShoreSelfTest.step(10, c -> {
            int left = ShoreSelfTest.count(c, 36) + ShoreSelfTest.count(c, 37) + ShoreSelfTest.count(c, 38);
            int inChest = 0;
            for (int i = 0; i < 27; ++i) {
                if (!ShoreSelfTest.stack(c, i).method_31574(class_1802.field_8858)) continue;
                inChest += ShoreSelfTest.count(c, i);
            }
            ShoreSelfTest.log(ShoreSelfTest.verdict(left == 0 && inChest == 144) + " shift-drag started between slots: sand in chest = " + inChest + " (want 144), left = " + left + " (want 0)");
            ShoreSelfTest.pickup(c, 41);
        });
        ShoreSelfTest.step(6, c -> {
            ShoreSelfTest.moveToBackground(c, 36);
            ShoreSelfTest.button(c, 0, 0, true);
        });
        ShoreSelfTest.step(6, c -> ShoreSelfTest.moveTo(c, 39));
        ShoreSelfTest.step(6, c -> ShoreSelfTest.moveTo(c, 40));
        ShoreSelfTest.step(6, c -> ShoreSelfTest.button(c, 0, 0, false));
        ShoreSelfTest.step(10, c -> {
            int cur = ShoreSelfTest.cursor(c).method_7947();
            int left = ShoreSelfTest.count(c, 39) + ShoreSelfTest.count(c, 40);
            ShoreSelfTest.log(ShoreSelfTest.verdict(cur == 30 && left == 0) + " left-click collect started between slots: cursor = " + cur + " gravel (want 30), left = " + left + " (want 0)");
            ShoreSelfTest.pickup(c, 41);
            ShoreSelfTest.shot(c, "chest");
        });
        ShoreSelfTest.step(4, c -> {
            c.field_1724.method_7346();
            class_1132 server = c.method_1576();
            UUID id = c.field_1724.method_5667();
            server.execute(() -> {
                int i;
                class_1661 inv = server.method_3760().method_14602(id).method_31548();
                for (i = 9; i < 36; ++i) {
                    inv.method_5447(i, class_1799.field_8037);
                }
                for (i = 1; i < 9; ++i) {
                    inv.method_5447(i, class_1799.field_8037);
                }
                inv.method_5447(9, new class_1799((class_1935)class_1802.field_20391, 64));
                inv.method_5447(10, new class_1799((class_1935)class_1802.field_20391, 64));
                inv.method_5447(11, new class_1799((class_1935)class_1802.field_8145, 8));
                inv.method_5447(12, new class_1799((class_1935)class_1802.field_8145, 8));
                inv.method_5447(13, new class_1799((class_1935)class_1802.field_8145, 8));
                class_1799 box = new class_1799((class_1935)class_1802.field_8816);
                class_9288 contents = class_9288.method_57493(List.of(new class_1799((class_1935)class_1802.field_8477, 12), new class_1799((class_1935)class_1802.field_8463, 3), class_1799.field_8037, new class_1799((class_1935)class_1802.field_8634, 16), class_1799.field_8037, class_1799.field_8037, new class_1799((class_1935)class_1802.field_8288), class_1799.field_8037, class_1799.field_8037, new class_1799((class_1935)class_1802.field_8281, 64)));
                box.method_57379(class_9334.field_49622, contents);
                inv.method_5447(17, box);
            });
        });
        ShoreSelfTest.step(10, c -> c.method_1507((class_437)new class_490((class_1657)c.field_1724)));
        ShoreSelfTest.step(6, c -> {
            class_465 screen = (class_465)c.field_1755;
            sx = ShoreSelfTest.field(screen, "x");
            sy = ShoreSelfTest.field(screen, "y");
            ShoreSelfTest.moveTo(c, 9);
            ShoreSelfTest.button(c, 0, 1, true);
        });
        ShoreSelfTest.step(6, c -> ShoreSelfTest.moveTo(c, 10));
        ShoreSelfTest.step(6, c -> ShoreSelfTest.button(c, 0, 1, false));
        ShoreSelfTest.step(10, c -> {
            int hotbar = 0;
            for (int i = 36; i < 45; ++i) {
                if (!ShoreSelfTest.stack(c, i).method_31574(class_1802.field_20391)) continue;
                hotbar += ShoreSelfTest.count(c, i);
            }
            ShoreSelfTest.log(ShoreSelfTest.verdict(hotbar == 128 && ShoreSelfTest.count(c, 9) + ShoreSelfTest.count(c, 10) == 0) + " inventory screen shift-drag: stone in hotbar = " + hotbar + " (want 128), left = " + (ShoreSelfTest.count(c, 9) + ShoreSelfTest.count(c, 10)));
            ShoreSelfTest.pickup(c, 13);
        });
        ShoreSelfTest.step(6, c -> {
            ShoreSelfTest.moveTo(c, 11);
            ShoreSelfTest.button(c, 0, 0, true);
        });
        ShoreSelfTest.step(6, c -> ShoreSelfTest.moveTo(c, 12));
        ShoreSelfTest.step(6, c -> ShoreSelfTest.button(c, 0, 0, false));
        ShoreSelfTest.step(10, c -> {
            int cur = ShoreSelfTest.cursor(c).method_7947();
            ShoreSelfTest.log(ShoreSelfTest.verdict(cur == 24 && ShoreSelfTest.count(c, 11) + ShoreSelfTest.count(c, 12) == 0) + " inventory screen left-click collect: cursor = " + cur + " flint (want 24), slots 11/12 = " + ShoreSelfTest.count(c, 11) + "/" + ShoreSelfTest.count(c, 12));
            ShoreSelfTest.pickup(c, 13);
        });
        ShoreSelfTest.step(4, c -> {
            class_1132 server = c.method_1576();
            UUID id = c.field_1724.method_5667();
            server.execute(() -> {
                int i;
                class_1661 inv = server.method_3760().method_14602(id).method_31548();
                for (i = 1; i < 9; ++i) {
                    inv.method_5447(i, class_1799.field_8037);
                }
                for (i = 27; i < 32; ++i) {
                    inv.method_5447(i, new class_1799((class_1935)class_1802.field_8288));
                }
            });
        });
        ShoreSelfTest.step(10, c -> {
            ShoreSelfTest.moveTo(c, 27);
            ShoreSelfTest.button(c, 0, 1, true);
        });
        ShoreSelfTest.step(6, c -> ShoreSelfTest.moveTo(c, 31));
        ShoreSelfTest.step(6, c -> ShoreSelfTest.button(c, 0, 1, false));
        ShoreSelfTest.step(10, c -> {
            int i;
            int left = 0;
            int hotbar = 0;
            for (i = 27; i < 32; ++i) {
                left += ShoreSelfTest.count(c, i);
            }
            for (i = 36; i < 45; ++i) {
                if (!ShoreSelfTest.stack(c, i).method_31574(class_1802.field_8288)) continue;
                ++hotbar;
            }
            ShoreSelfTest.log(ShoreSelfTest.verdict(left == 0 && hotbar == 5) + " fast shift-sweep over 5 totems: moved to hotbar = " + hotbar + " (want 5), left = " + left + " (want 0)");
        });
        ShoreSelfTest.step(6, c -> {
            Config cf = ShoreClient.config;
            cf.shulkerMode = 1;
            cf.shulkerScale = 150;
            cf.shulkerBorderMode = 1;
            ShoreSelfTest.moveTo(c, 17);
        });
        ShoreSelfTest.step(8, c -> ShoreSelfTest.shot(c, "shulker"));
        ShoreSelfTest.step(2, c -> {
            ShoreClient.config.shulkerMode = 0;
        });
        ShoreSelfTest.step(2, c -> {
            c.field_1724.method_7346();
            class_338 chat = c.field_1705.method_1743();
            ShoreClient.config.filterDuplicates = true;
            ShoreClient.config.stackStyle = 0;
            for (String m : new String[]{"Stack test A", "Stack test B", "Stack test A", "Stack test C", "Stack test A"}) {
                chat.method_1812((class_2561)class_2561.method_43470((String)m));
            }
        });
        ShoreSelfTest.step(4, c -> {
            try {
                Field f = class_338.class.getDeclaredField("messages");
                f.setAccessible(true);
                List<class_303> msgs = (List<class_303>)f.get(c.field_1705.method_1743());
                Field v = class_338.class.getDeclaredField("visibleMessages");
                v.setAccessible(true);
                List<class_303.class_7590> vis = (List<class_303.class_7590>)v.get(c.field_1705.method_1743());
                long aCount = msgs.stream().filter(m -> m.comp_893().getString().contains("Stack test A")).count();
                String newest = ((class_303)msgs.getFirst()).comp_893().getString();
                String second = ((class_303)msgs.get(1)).comp_893().getString();
                String third = ((class_303)msgs.get(2)).comp_893().getString();
                long groups = vis.stream().filter(class_303.class_7590::comp_898).count();
                boolean ok = aCount == 1L && newest.endsWith("Stack test A [x3]") && second.endsWith("Stack test C") && third.endsWith("Stack test B") && groups == (long)msgs.size();
                ShoreSelfTest.log(ShoreSelfTest.verdict(ok) + " chat stacking: newest = \"" + newest + "\", then \"" + second + "\", \"" + third + "\"; copies of A = " + aCount + " (want 1); visible groups " + groups + " = messages " + msgs.size());
            }
            catch (ReflectiveOperationException e) {
                ShoreSelfTest.log("FAIL chat stacking: " + String.valueOf(e));
            }
            c.method_1507((class_437)new class_408("", false));
        });
        ShoreSelfTest.step(8, c -> ShoreSelfTest.shot(c, "chat"));
        ShoreSelfTest.step(2, c -> {
            ShoreClient.config.mentionHighlight = true;
            ShoreClient.config.mentionColor = 0;
            String me = c.field_1724.method_5477().getString();
            class_338 chat = c.field_1705.method_1743();
            chat.method_1812((class_2561)class_2561.method_43470((String)("<Steve> hey " + me + " want to duel?")));
            chat.method_1812((class_2561)class_2561.method_43470((String)("<" + me + "> sure, meet at spawn")));
            chat.method_1812((class_2561)class_2561.method_43470((String)"<Alex> nice weather today"));
        });
        ShoreSelfTest.step(4, c -> {
            try {
                Field f = class_338.class.getDeclaredField("messages");
                f.setAccessible(true);
                List msgs = (List)f.get(c.field_1705.method_1743());
                Field m = class_338.class.getDeclaredField("SHORE_MENTIONS");
                m.setAccessible(true);
                Set marked = (Set)m.get(null);
                boolean other = marked.contains(msgs.get(2));
                boolean own = marked.contains(msgs.get(1));
                boolean none = marked.contains(msgs.get(0));
                ShoreSelfTest.log(ShoreSelfTest.verdict(other && !own && !none) + " mentions: \"" + ((class_303)msgs.get(2)).comp_893().getString() + "\" highlighted = " + other + " (want true); own message = " + own + " (want false); unrelated = " + none + " (want false)");
            }
            catch (ReflectiveOperationException e) {
                ShoreSelfTest.log("FAIL mentions: " + String.valueOf(e));
            }
            c.method_1507((class_437)new class_408("", false));
        });
        ShoreSelfTest.step(8, c -> ShoreSelfTest.shot(c, "mentions"));
        ShoreSelfTest.step(2, c -> {
            c.method_1507(null);
            Config cfg = ShoreClient.config;
            cfg.keystrokes = true;
            cfg.targetHud = true;
            cfg.potionHud = true;
            cfg.itemCounter = true;
            class_1132 server = c.method_1576();
            class_2168 src = server.method_3739();
            String me = c.field_1724.method_5477().getString();
            server.execute(() -> {
                for (String cmd : new String[]{"give " + me + " totem_of_undying 3", "give " + me + " ender_pearl 16", "give " + me + " golden_apple 8", "effect give " + me + " speed 120 1", "effect give " + me + " strength 90 0", "execute at " + me + " run summon pig ^ ^ ^2 {NoAI:1b,Invulnerable:1b}"}) {
                    server.method_3734().method_44252(src, cmd);
                }
            });
        });
        ShoreSelfTest.step(20, c -> {
            List pigs = c.field_1687.method_8390(class_1452.class, c.field_1724.method_5829().method_1014(6.0), p -> true);
            if (pigs.isEmpty()) {
                ShoreSelfTest.log("FAIL pvp: no pig to hit");
                return;
            }
            c.field_1761.method_2918((class_1657)c.field_1724, (class_1297)pigs.getFirst());
        });
        ShoreSelfTest.step(12, c -> {
            List pigs = c.field_1687.method_8390(class_1452.class, c.field_1724.method_5829().method_1014(6.0), p -> true);
            if (!pigs.isEmpty()) {
                c.field_1761.method_2918((class_1657)c.field_1724, (class_1297)pigs.getFirst());
            }
        });
        ShoreSelfTest.step(6, c -> {
            class_1309 t = ShorePvp.target();
            ShoreSelfTest.log(ShoreSelfTest.verdict(t != null) + " target HUD: " + (t == null ? "none" : t.method_5477().getString()));
        });
        ShoreSelfTest.step(6, c -> ShoreSelfTest.shot(c, "pvp_hud"));
        ShoreSelfTest.step(2, c -> {
            try {
                BufferedImage img = new BufferedImage(32, 32, 2);
                Graphics2D g = img.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(new Color(255, 60, 60));
                g.setStroke(new BasicStroke(3.0f));
                g.drawOval(4, 4, 24, 24);
                g.fillOval(14, 14, 4, 4);
                g.dispose();
                Path file = c.field_1697.toPath().resolve("selftest_crosshair.png");
                ImageIO.write((RenderedImage)img, "png", file.toFile());
                Config cf = ShoreClient.config;
                cf.crosshair = true;
                cf.crosshairMode = 1;
                cf.crosshairImageSize = 24;
                cf.crosshairImage = ShoreHud.CrosshairImage.importFile(file);
                cf.itemsCustom = new ArrayList<String>(List.of("cobweb"));
                class_1132 server = c.method_1576();
                String me = c.field_1724.method_5477().getString();
                server.execute(() -> server.method_3734().method_44252(server.method_3739(), "give " + me + " cobweb 5"));
            }
            catch (Exception e) {
                ShoreSelfTest.log("FAIL crosshair image setup: " + String.valueOf(e));
            }
        });
        ShoreSelfTest.step(10, c -> {
            boolean loaded = ShoreHud.CrosshairImage.texture(ShoreClient.config.crosshairImage) != null;
            ShoreSelfTest.log(ShoreSelfTest.verdict(loaded) + " crosshair image loaded from " + ShoreClient.config.crosshairImage);
            ShoreSelfTest.shot(c, "crosshair_image");
            Config cf = ShoreClient.config;
            cf.crosshairMode = 0;
            cf.crosshairColor = 1;
            cf.crosshairDot = true;
            cf.crosshairLength = 6;
            cf.crosshairGap = 3;
        });
        ShoreSelfTest.step(6, c -> ShoreSelfTest.shot(c, "crosshair_shape"));
        ShoreSelfTest.step(2, c -> {
            zoomSens = (Double)c.field_1690.method_42495().method_41753();
            ShoreClient.config.zoomEnabled = true;
            ShoreClient.config.zoomToggle = false;
            ShoreTools.zoomKey.method_23481(true);
        });
        ShoreSelfTest.step(6, c -> {
            zoomScaleWhile = ShoreTools.lookScale();
            zoomSensWhile = (Double)c.field_1690.method_42495().method_41753();
            ShoreTools.zoomKey.method_23481(false);
        });
        ShoreSelfTest.step(6, c -> {
            double after = (Double)c.field_1690.method_42495().method_41753();
            boolean ok = zoomScaleWhile < 1.0 && zoomSensWhile == zoomSens && after == zoomSens && ShoreTools.lookScale() == 1.0;
            ShoreSelfTest.log(ShoreSelfTest.verdict(ok) + " zoom sensitivity: setting before/while/after = " + zoomSens + "/" + zoomSensWhile + "/" + after + ", turn scale while zoomed = " + String.format(Locale.ROOT, "%.2f", zoomScaleWhile) + " (want < 1), after = " + ShoreTools.lookScale());
        });
        ShoreSelfTest.step(2, c -> {
            c.method_1507(null);
            ShoreClient.config.hudSizes.put("fps", 200);
            ShoreClient.config.cps = true;
            ShoreClient.config.crosshair = true;
            ShoreClient.config.crosshairColor = 1;
            ShoreClient.config.crosshairDot = true;
        });
        ShoreSelfTest.step(20, c -> ShoreSelfTest.shot(c, "hud"));
        ShoreSelfTest.step(2, c -> {
            ShoreSelfTest.log("World tests done");
            ShoreSelfTest.finish(c);
        });
    }

    private static String verdict(boolean ok) {
        return ok ? "PASS" : "FAIL";
    }

    private static void pickup(class_310 c, int id) {
        c.field_1761.method_2906(((class_465)c.field_1755).method_17577().field_7763, id, 0, class_1713.field_7790, (class_1657)c.field_1724);
    }

    private static void moveTo(class_310 c, int slotId) {
        class_1735 s = ShoreSelfTest.slot(c, slotId);
        class_1041 w = c.method_22683();
        double gx = sx + s.field_7873 + 8;
        double gy = sy + s.field_7872 + 8;
        ShoreSelfTest.invoke(c, "onCursorPos", new Class[]{Long.TYPE, Double.TYPE, Double.TYPE}, w.method_4490(), gx * (double)w.method_4480() / (double)w.method_4486(), gy * (double)w.method_4507() / (double)w.method_4502());
    }

    private static void moveToBackground(class_310 c, int slotId) {
        class_1735 s = ShoreSelfTest.slot(c, slotId);
        class_1041 w = c.method_22683();
        double gx = sx + s.field_7873 - 3;
        double gy = sy + s.field_7872 + 8;
        ShoreSelfTest.invoke(c, "onCursorPos", new Class[]{Long.TYPE, Double.TYPE, Double.TYPE}, w.method_4490(), gx * (double)w.method_4480() / (double)w.method_4486(), gy * (double)w.method_4507() / (double)w.method_4502());
    }

    private static void button(class_310 c, int button, int mods, boolean press) {
        ShoreSelfTest.invoke(c, "onMouseButton", new Class[]{Long.TYPE, class_11910.class, Integer.TYPE}, c.method_22683().method_4490(), new class_11910(button, mods), press ? 1 : 0);
    }

    private static void scroll(class_310 c, double amount) {
        ShoreSelfTest.invoke(c, "onMouseScroll", new Class[]{Long.TYPE, Double.TYPE, Double.TYPE}, c.method_22683().method_4490(), 0.0, amount);
    }

    private static void invoke(class_310 c, String name, Class<?>[] types, Object ... args) {
        try {
            Method m = class_312.class.getDeclaredMethod(name, types);
            m.setAccessible(true);
            m.invoke((Object)c.field_1729, args);
        }
        catch (ReflectiveOperationException e) {
            throw new IllegalStateException(name, e);
        }
    }

    private static class_1735 slot(class_310 c, int id) {
        return ((class_465)c.field_1755).method_17577().method_7611(id);
    }

    private static class_1799 stack(class_310 c, int id) {
        return ShoreSelfTest.slot(c, id).method_7677();
    }

    private static int count(class_310 c, int id) {
        return ShoreSelfTest.stack(c, id).method_7947();
    }

    private static class_1799 cursor(class_310 c) {
        return ((class_465)c.field_1755).method_17577().method_34255();
    }

    private static class_11909 click(class_310 c, int slotId, int button, int mods) {
        class_1735 s = ShoreSelfTest.slot(c, slotId);
        return new class_11909((double)(sx + s.field_7873 + 8), (double)(sy + s.field_7872 + 8), new class_11910(button, mods));
    }

    private static int field(Object o, String name) {
        try {
            Field f = class_465.class.getDeclaredField(name);
            f.setAccessible(true);
            return f.getInt(o);
        }
        catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private record Step(int delay, Predicate<class_310> ready, Consumer<class_310> action) {
    }
}

