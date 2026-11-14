/*
 * Decompiled with CFR 0.152.
 */
package net.shoresmp.fabric;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_1044;
import net.minecraft.class_10799;
import net.minecraft.class_11719;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1309;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1935;
import net.minecraft.class_2350;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_315;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_338;
import net.minecraft.class_640;
import net.minecraft.class_746;
import net.minecraft.class_7923;
import net.shoresmp.fabric.Config;
import net.shoresmp.fabric.ShoreClient;
import net.shoresmp.fabric.ShorePvp;
import net.shoresmp.fabric.ShoreTools;
import org.joml.Matrix3x2fStack;

public final class ShoreHud {
    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm");
    private static final class_1304[] SLOTS = new class_1304[]{class_1304.field_6169, class_1304.field_6174, class_1304.field_6172, class_1304.field_6166, class_1304.field_6173, class_1304.field_6171};
    private static final class_2960 UI = class_2960.method_60655((String)"shoremod", (String)"ui");
    private static final int TEXT = -1;
    private static final int BOX_H = 16;
    private static final int GEAR_H = 20;
    private static final int PAD_X = 5;
    private static final int KEY = 20;
    private static final int KEY_GAP = 2;
    static final int[] CROSSHAIR_COLORS = new int[]{-1, -1, -11141291, -43691, -11141121, -171};
    static final String[] CROSSHAIR_NAMES = new String[]{"White", "Accent", "Green", "Red", "Cyan", "Yellow", "Custom"};

    private ShoreHud() {
    }

    public static void register() {
        HudElementRegistry.addLast((class_2960)class_2960.method_60655((String)"shoremod", (String)"hud"), (ctx, tick) -> ShoreHud.render(ctx));
        HudElementRegistry.replaceElement((class_2960)VanillaHudElements.CROSSHAIR, vanilla -> (ctx, tick) -> {
            Config c = ShoreClient.config;
            class_310 client = class_310.method_1551();
            if (c == null || !c.crosshair || !client.field_1690.method_31044().method_31034() || client.field_1690.field_1842 || client.method_53526().method_53536() || client.field_1724 == null || client.field_1724.method_7325()) {
                vanilla.render(ctx, tick);
            } else {
                ShoreHud.crosshair(ctx);
            }
        });
    }

    private static class_2561 ui(String s) {
        return class_2561.method_43470((String)s).method_10862(class_2583.field_24360.method_27704((class_11719)new class_11719.class_11721(UI)));
    }

    private static List<Readout> lines(class_310 client) {
        class_640 entry;
        Config c = ShoreClient.config;
        class_746 player = client.field_1724;
        ArrayList<Readout> out = new ArrayList<Readout>();
        boolean hidePosition = false;
        if (c.hudFps) {
            out.add(new Readout("fps", client.method_47599() + " FPS"));
        }
        if (c.hudPing && player != null && client.method_1562() != null && !client.method_1542() && (entry = client.method_1562().method_2871(player.method_5667())) != null) {
            out.add(new Readout("ping", entry.method_2959() + " ms"));
        }
        if (c.hudCoords && player != null && !hidePosition) {
            out.add(new Readout("coords", "XYZ: " + player.method_31477() + ", " + player.method_31478() + ", " + player.method_31479()));
            class_2350 facing = player.method_5735();
            String axis = switch (facing) {
                case class_2350.field_11043 -> "-Z";
                case class_2350.field_11035 -> "+Z";
                case class_2350.field_11034 -> "+X";
                case class_2350.field_11039 -> "-X";
                default -> "";
            };
            out.add(new Readout("coords", ShoreHud.title(facing.method_15434()) + " (" + axis + ")"));
        }
        if (c.hudBiome && player != null && client.field_1687 != null && !hidePosition) {
            out.add(new Readout("biome", client.field_1687.method_23753(player.method_24515()).method_40230().map(k -> ShoreHud.title(k.method_29177().method_12832())).orElse("Unknown")));
        }
        if (c.hudClock) {
            out.add(new Readout("clock", LocalTime.now().format(CLOCK)));
        }
        class_1309 target = ShorePvp.target();
        if (c.targetHud && target != null) {
            out.add(new Readout("target", target.method_5477().getString() + "  \u2764 " + String.format(Locale.ROOT, "%.1f", Float.valueOf(target.method_6032() + target.method_6067())) + (c.targetDistance ? String.format(Locale.ROOT, "  %.1fm", Float.valueOf(target.method_5739((class_1297)player))) : "")));
        }
        if (c.potionHud && player != null) {
            ArrayList<class_1293> effects = new ArrayList<class_1293>(player.method_6026());
            effects.sort(Comparator.comparingInt(e -> -e.method_5584()));
            for (class_1293 e2 : effects) {
                String name = ((class_1291)e2.method_5579().comp_349()).method_5560().getString();
                int lvl = e2.method_5578() + 1;
                String roman = lvl <= 1 ? "" : " " + (lvl <= 10 ? (new String[]{"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"})[lvl] : String.valueOf(lvl));
                int secs = e2.method_5584() / 20;
                String time = e2.method_48559() ? "\u221e" : String.format(Locale.ROOT, "%d:%02d", secs / 60, secs % 60);
                out.add(new Readout("potions", name + roman + "  " + time));
            }
        }
        if (c.cps) {
            out.add(new Readout("cps", c.cpsRight ? ShoreTools.cps(true) + " | " + ShoreTools.cps(false) + " CPS" : ShoreTools.cps(true) + " CPS"));
        }
        if (player != null && c.toggleSprintHud) {
            if (c.toggleSneak && client.field_1690.field_1832.method_1434()) {
                out.add(new Readout("togglesprint", "Sneaking (Toggled)"));
            } else if (c.toggleSprint && client.field_1690.field_1867.method_1434()) {
                out.add(new Readout("togglesprint", "Sprinting (Toggled)"));
            }
        }
        return out;
    }

    static String title(String id) {
        StringBuilder sb = new StringBuilder();
        for (String part : id.split("_")) {
            if (part.isEmpty()) continue;
            if (!sb.isEmpty()) {
                sb.append(' ');
            }
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return sb.toString();
    }

    private static int box() {
        return Math.round((float)ShoreClient.config.hudBgOpacity * 2.55f) << 24;
    }

    private static int textColor() {
        Config c = ShoreClient.config;
        return switch (c.hudTextColor) {
            case 1 -> 0xFF000000 | c.accent;
            case 2 -> -3617325;
            case 3 -> 0xFF000000 | c.hudCustomColor;
            default -> -1;
        };
    }

    private static void render(class_332 ctx) {
        class_310 client = class_310.method_1551();
        Config c = ShoreClient.config;
        if (c == null || client.field_1724 == null || client.field_1690.field_1842 || client.method_53526().method_53536()) {
            return;
        }
        class_327 tr = client.field_1772;
        float global = (float)c.hudScale / 100.0f;
        ArrayList<Block> blocks = new ArrayList<Block>();
        for (Readout r : ShoreHud.lines(client)) {
            int w = tr.method_1727(r.text()) + 10;
            blocks.add(new Block(w, 16, global * (float)c.hudSize(r.id()) / 100.0f, g -> {
                ShoreHud.box(g, 0, 0, w, 16);
                g.method_51433(tr, r.text(), 5, 5, ShoreHud.textColor(), c.hudShadow);
            }));
        }
        if (c.keystrokes) {
            blocks.add(ShoreHud.keystrokes(client, global * (float)c.hudSize("keystrokes") / 100.0f));
        }
        if (c.itemCounter) {
            float sc = global * (float)c.hudSize("items") / 100.0f;
            class_1304[] inv = client.field_1724.method_31548();
            for (class_1792[] group : ShoreHud.counted(c)) {
                int n = 0;
                for (int i = 0; i < inv.method_5439(); ++i) {
                    for (class_1792 item : group) {
                        if (!inv.method_5438(i).method_31574(item)) continue;
                        n += inv.method_5438(i).method_7947();
                    }
                }
                if (n <= 0 && !c.itemsShowZero) continue;
                blocks.add(ShoreHud.itemRow(tr, new class_1799((class_1935)group[0]), String.valueOf(n), ShoreHud.textColor(), sc));
            }
        }
        if (c.hudArmor) {
            float sc = global * (float)c.hudSize("armor") / 100.0f;
            for (class_1304 slot : SLOTS) {
                int pct;
                class_1799 s = client.field_1724.method_6118(slot);
                if (s.method_7960() || !s.method_7963()) continue;
                int max = s.method_7936();
                int left = max - s.method_7919();
                int n = pct = max <= 0 ? 100 : Math.round((float)left * 100.0f / (float)max);
                int color = pct > 50 ? ShoreHud.textColor() : (pct > Math.max(c.durabilityThreshold, 20) ? -171 : -43691);
                blocks.add(ShoreHud.itemRow(tr, s, pct + "%", color, sc));
            }
        }
        if (blocks.isEmpty()) {
            return;
        }
        int sw = ctx.method_51421();
        int sh = ctx.method_51443();
        boolean right = c.hudCorner == 1 || c.hudCorner == 3;
        boolean bottom = c.hudCorner >= 2;
        float gap = 2.0f;
        float margin = 4.0f;
        float topOffset = right && !bottom && !client.field_1724.method_6026().isEmpty() ? 52.0f : 0.0f;
        int chatH = class_338.method_1818((double)((Double)client.field_1690.method_41801().method_41753())) + 48;
        float avail = Math.max(60.0f, (float)sh - 8.0f - topOffset - (right ? 0.0f : Math.min((float)sh * 0.6f, (float)chatH)));
        ArrayList columns = new ArrayList();
        ArrayList<Block> col = new ArrayList<Block>();
        float colH = 0.0f;
        for (Block b : blocks) {
            float h = (float)b.h() * b.scale();
            if (!col.isEmpty() && colH + 2.0f + h > avail) {
                columns.add(col);
                col = new ArrayList();
                colH = 0.0f;
            }
            colH += (col.isEmpty() ? 0.0f : 2.0f) + h;
            col.add(b);
        }
        columns.add(col);
        Matrix3x2fStack m = ctx.method_51448();
        float colX = 4.0f;
        for (List list : columns) {
            float height = -2.0f;
            float width = 0.0f;
            for (Block b : list) {
                height += (float)b.h() * b.scale() + 2.0f;
                width = Math.max(width, (float)b.w() * b.scale());
            }
            float y = bottom ? (float)sh - 4.0f - height - (float)(right ? 0 : 60) : 4.0f + topOffset;
            for (Block b : list) {
                float x = right ? (float)sw - colX - (float)b.w() * b.scale() : colX;
                m.pushMatrix();
                m.translate(x, y);
                m.scale(b.scale(), b.scale());
                b.draw().accept(ctx);
                m.popMatrix();
                y += (float)b.h() * b.scale() + 2.0f;
            }
            colX += width + 4.0f;
        }
    }

    private static Block itemRow(class_327 tr, class_1799 stack, String label, int color, float sc) {
        int w = 25 + tr.method_1727(label) + 5;
        return new Block(w, 20, sc, g -> {
            ShoreHud.box(g, 0, 0, w, 20);
            g.method_51427(stack, 4, 2);
            g.method_51433(tr, label, 24, 7, color, ShoreClient.config.hudShadow);
        });
    }

    private static List<class_1792[]> counted(Config c) {
        ArrayList<class_1792[]> out = new ArrayList<class_1792[]>();
        if (c.itemsTotem) {
            out.add(new class_1792[]{class_1802.field_8288});
        }
        if (c.itemsPearl) {
            out.add(new class_1792[]{class_1802.field_8634});
        }
        if (c.itemsGapple) {
            out.add(new class_1792[]{class_1802.field_8463, class_1802.field_8367});
        }
        if (c.itemsCrystal) {
            out.add(new class_1792[]{class_1802.field_8301});
        }
        if (c.itemsArrow) {
            out.add(new class_1792[]{class_1802.field_8107, class_1802.field_8236, class_1802.field_8087});
        }
        if (c.itemsXp) {
            out.add(new class_1792[]{class_1802.field_8287});
        }
        for (String raw : c.itemsCustom) {
            class_1792 item;
            class_2960 id = class_2960.method_12829((String)raw.strip().toLowerCase(Locale.ROOT));
            if (id == null || (item = (class_1792)class_7923.field_41178.method_63535(id)) == class_1802.field_8162) continue;
            out.add(new class_1792[]{item});
        }
        return out;
    }

    private static Block keystrokes(class_310 client, float sc) {
        Config c = ShoreClient.config;
        class_315 o = client.field_1690;
        class_327 tr = client.field_1772;
        int w = 64;
        int h = 42 + (c.keystrokesMouse ? 22 : 0) + (c.keystrokesSpace ? 14 : 0);
        return new Block(w, h, sc, g -> {
            ShoreHud.key(g, tr, 22, 0, 20, 20, ShoreClient.keyName(o.field_1894), o.field_1894.method_1434());
            int y = 22;
            ShoreHud.key(g, tr, 0, y, 20, 20, ShoreClient.keyName(o.field_1913), o.field_1913.method_1434());
            ShoreHud.key(g, tr, 22, y, 20, 20, ShoreClient.keyName(o.field_1881), o.field_1881.method_1434());
            ShoreHud.key(g, tr, 44, y, 20, 20, ShoreClient.keyName(o.field_1849), o.field_1849.method_1434());
            y += 22;
            if (c.keystrokesMouse) {
                int half = (w - 2) / 2;
                ShoreHud.key(g, tr, 0, y, half, 20, ShoreTools.cps(true) + " CPS", o.field_1886.method_1434());
                ShoreHud.key(g, tr, half + 2, y, w - half - 2, 20, ShoreTools.cps(false) + " CPS", o.field_1904.method_1434());
                y += 22;
            }
            if (c.keystrokesSpace) {
                boolean down = o.field_1903.method_1434();
                if (down) {
                    ShoreHud.notched(g, 0, y, w, 12, ShoreHud.pressedColor());
                } else {
                    ShoreHud.box(g, 0, y, w, 12);
                }
                g.method_25294(w / 2 - 12, y + 5, w / 2 + 12, y + 7, down ? -15724528 : ShoreHud.textColor());
            }
        });
    }

    private static void key(class_332 g, class_327 tr, int x, int y, int w, int h, String label, boolean down) {
        if (down) {
            ShoreHud.notched(g, x, y, w, h, ShoreHud.pressedColor());
        } else {
            ShoreHud.box(g, x, y, w, h);
        }
        if (label.length() > 4 && w <= 20) {
            label = label.substring(0, 1);
        }
        int tw = tr.method_1727(label);
        g.method_51433(tr, label, x + (w - tw) / 2, y + (h - 8) / 2 + 1, down ? -15724528 : ShoreHud.textColor(), !down && ShoreClient.config.hudShadow);
    }

    private static int pressedColor() {
        Config c = ShoreClient.config;
        return c.keystrokesAccent ? 0xD0000000 | c.accent : -788529153;
    }

    private static void notched(class_332 g, int x, int y, int w, int h, int color) {
        g.method_25294(x + 1, y, x + w - 1, y + h, color);
        g.method_25294(x, y + 1, x + 1, y + h - 1, color);
        g.method_25294(x + w - 1, y + 1, x + w, y + h - 1, color);
    }

    private static void crosshair(class_332 ctx) {
        class_2960 tex;
        Config c = ShoreClient.config;
        int alpha = Math.round((float)c.crosshairOpacity * 2.55f);
        int base = c.crosshairColor == 6 ? 0xFF000000 | c.crosshairCustomColor : (c.crosshairColor == 1 ? 0xFF000000 | c.accent : CROSSHAIR_COLORS[c.crosshairColor]);
        int color = alpha << 24 | base & 0xFFFFFF;
        int cx = ctx.method_51421() / 2;
        int cy = ctx.method_51443() / 2;
        if (c.crosshairMode == 1 && (tex = CrosshairImage.texture(c.crosshairImage)) != null) {
            int s = c.crosshairImageSize;
            int tint = c.crosshairTint ? color : alpha << 24 | 0xFFFFFF;
            ctx.method_25291(class_10799.field_56883, tex, cx - s / 2, cy - s / 2, 0.0f, 0.0f, s, s, s, s, tint);
            return;
        }
        int t = c.crosshairThickness;
        int len = c.crosshairLength;
        int gap = c.crosshairGap;
        int h = t / 2;
        int[][] arms = new int[][]{{cx - h, cy - gap - len, t, len}, {cx - h, cy + gap + (t % 2 == 0 ? 0 : 1), t, len}, {cx - gap - len, cy - h, len, t}, {cx + gap + (t % 2 == 0 ? 0 : 1), cy - h, len, t}};
        if (c.crosshairOutline) {
            int outline = Math.round((float)alpha * 0.75f) << 24;
            int[][] nArrayArray = arms;
            int n = nArrayArray.length;
            for (int i = 0; i < n; ++i) {
                int[] a = nArrayArray[i];
                ctx.method_25294(a[0] - 1, a[1] - 1, a[0] + a[2] + 1, a[1] + a[3] + 1, outline);
            }
            if (c.crosshairDot) {
                ctx.method_25294(cx - h - 1, cy - h - 1, cx - h + t + 1, cy - h + t + 1, outline);
            }
        }
        for (int[] a : arms) {
            ctx.method_25294(a[0], a[1], a[0] + a[2], a[1] + a[3], color);
        }
        if (c.crosshairDot) {
            ctx.method_25294(cx - h, cy - h, cx - h + t, cy - h + t, color);
        }
    }

    private static void box(class_332 ctx, int x, int y, int w, int h) {
        ctx.method_25294(x + 1, y, x + w - 1, y + h, ShoreHud.box());
        ctx.method_25294(x, y + 1, x + 1, y + h - 1, ShoreHud.box());
        ctx.method_25294(x + w - 1, y + 1, x + w, y + h - 1, ShoreHud.box());
    }

    private record Readout(String id, String text) {
    }

    private record Block(int w, int h, float scale, Consumer<class_332> draw) {
    }

    static final class CrosshairImage {
        private static final class_2960 ID = class_2960.method_60655((String)"shoremod", (String)"dyn/crosshair_image");
        private static String loadedPath = "";
        private static long loadedStamp;
        private static boolean ok;

        CrosshairImage() {
        }

        /*
         * Enabled aggressive block sorting
         * Enabled unnecessary exception pruning
         * Enabled aggressive exception aggregation
         */
        static class_2960 texture(String path) {
            long stamp;
            if (path == null) return null;
            if (path.isBlank()) {
                return null;
            }
            Path file = Path.of(path, new String[0]);
            try {
                stamp = Files.getLastModifiedTime(file, new LinkOption[0]).toMillis();
            }
            catch (Exception e) {
                return null;
            }
            if (!path.equals(loadedPath) || stamp != loadedStamp) {
                loadedPath = path;
                loadedStamp = stamp;
                ok = false;
                try (InputStream in = Files.newInputStream(file, new OpenOption[0]);){
                    class_1011 img = class_1011.method_4309((InputStream)in);
                    if (img.method_4307() > 512 || img.method_4323() > 512) {
                        img.close();
                        ShoreClient.tellError("Crosshair image must be 512x512 or smaller.");
                        class_2960 class_29602 = null;
                        return class_29602;
                    }
                    class_310.method_1551().method_1531().method_4616(ID, (class_1044)new class_1043(() -> "shoremod crosshair", img));
                    ok = true;
                }
                catch (Exception e) {
                    ShoreClient.tellError("Could not load crosshair image: " + e.getMessage());
                }
            }
            if (!ok) return null;
            class_2960 class_29602 = ID;
            return class_29602;
        }

        static String importFile(Path source) throws IOException {
            Path dir = FabricLoader.getInstance().getConfigDir().resolve("shoremod/crosshairs");
            Files.createDirectories(dir, new FileAttribute[0]);
            Path target = dir.resolve(source.getFileName().toString().replaceAll("[^A-Za-z0-9._-]", "_"));
            if (!source.toAbsolutePath().equals(target.toAbsolutePath())) {
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return target.toAbsolutePath().toString();
        }

        static Path folder() {
            return FabricLoader.getInstance().getConfigDir().resolve("shoremod/crosshairs");
        }
    }
}

