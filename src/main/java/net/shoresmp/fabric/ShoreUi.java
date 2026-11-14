/*
 * Decompiled with CFR 0.152.
 */
package net.shoresmp.fabric;

import java.io.InputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_1011;
import net.minecraft.class_1041;
import net.minecraft.class_1043;
import net.minecraft.class_1044;
import net.minecraft.class_10799;
import net.minecraft.class_11719;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3298;
import net.minecraft.class_332;
import net.minecraft.class_5348;
import net.shoresmp.fabric.ShoreClient;
import org.joml.Matrix3x2fStack;

final class ShoreUi {
    private static final Map<Integer, class_2960> CORNERS = new HashMap<Integer, class_2960>();
    private static final Map<String, class_2960> ICONS = new HashMap<String, class_2960>();
    private static final Map<String, class_1011> ICON_SOURCES = new HashMap<String, class_1011>();
    private static final Map<String, class_2561> STYLED = new HashMap<String, class_2561>();
    final class_310 mc = class_310.method_1551();
    class_332 ctx;
    float u;
    int gs;
    float W;
    float H;
    private static final float[] OVERSAMPLE = new float[]{1.0f, 1.125f, 1.25f, 1.375f, 1.5f, 1.625f, 1.75f, 1.875f, 2.0f, 2.125f, 2.25f, 2.375f, 2.5f, 2.625f, 2.75f, 2.875f, 3.0f, 3.25f, 3.5f, 3.75f, 4.0f, 4.5f, 5.0f, 5.5f, 6.0f};
    boolean sans;
    boolean noBold;

    ShoreUi() {
    }

    static float unit(class_310 mc, int menuScale) {
        int fbH = mc.method_22683().method_4506();
        return Math.clamp((float)fbH / 500.0f, 1.0f, 4.5f) * (float)menuScale / 100.0f;
    }

    void begin(class_332 ctx, float unit) {
        this.ctx = ctx;
        this.u = unit;
        class_1041 w = this.mc.method_22683();
        this.gs = w.method_4495();
        this.W = (float)w.method_4489() / this.u;
        this.H = (float)w.method_4506() / this.u;
        ctx.method_51448().pushMatrix();
        ctx.method_51448().scale(1.0f / (float)this.gs, 1.0f / (float)this.gs);
    }

    void end() {
        this.ctx.method_51448().popMatrix();
    }

    int p(float v) {
        return Math.round(v * this.u);
    }

    float fromGui(double gui) {
        return (float)(gui * (double)this.gs / (double)this.u);
    }

    void rect(float x, float y, float w, float h, int color) {
        int x0 = this.p(x);
        int y0 = this.p(y);
        int x1 = this.p(x + w);
        int y1 = this.p(y + h);
        if (x1 > x0 && y1 > y0) {
            this.ctx.method_25294(x0, y0, x1, y1, color);
        }
    }

    void hline(float x, float y, float w, int color) {
        int x0 = this.p(x);
        int y0 = this.p(y);
        this.ctx.method_25294(x0, y0, this.p(x + w), y0 + 1, color);
    }

    void round(float x, float y, float w, float h, float r, int color) {
        this.roundPx(this.p(x), this.p(y), this.p(x + w), this.p(y + h), this.p(r), color);
    }

    void outline(float x, float y, float w, float h, float r, int border, int fill) {
        int x0 = this.p(x);
        int y0 = this.p(y);
        int x1 = this.p(x + w);
        int y1 = this.p(y + h);
        int rr = this.p(r);
        this.roundPx(x0, y0, x1, y1, rr, border);
        this.roundPx(x0 + 1, y0 + 1, x1 - 1, y1 - 1, Math.max(0, rr - 1), fill);
    }

    void ring(float x, float y, float w, float h, float r, int color, int inner) {
        int x0 = this.p(x);
        int y0 = this.p(y);
        int x1 = this.p(x + w);
        int y1 = this.p(y + h);
        int rr = this.p(r);
        int t = Math.max(1, Math.round(this.u * 0.9f));
        this.roundPx(x0, y0, x1, y1, rr, color);
        this.roundPx(x0 + t, y0 + t, x1 - t, y1 - t, Math.max(0, rr - t), inner);
    }

    void circle(float cx, float cy, float d, int color) {
        this.round(cx - d / 2.0f, cy - d / 2.0f, d, d, d / 2.0f, color);
    }

    void roundPx(int x0, int y0, int x1, int y1, int r, int color) {
        int w = x1 - x0;
        int h = y1 - y0;
        if (w <= 0 || h <= 0 || color >>> 24 == 0) {
            return;
        }
        if ((r = Math.min(r, Math.min(w, h) / 2)) <= 0) {
            this.ctx.method_25294(x0, y0, x1, y1, color);
            return;
        }
        class_2960 tex = this.corner(r);
        int d = r * 2;
        this.ctx.method_25291(class_10799.field_56883, tex, x0, y0, 0.0f, 0.0f, r, r, d, d, color);
        this.ctx.method_25291(class_10799.field_56883, tex, x1 - r, y0, (float)r, 0.0f, r, r, d, d, color);
        this.ctx.method_25291(class_10799.field_56883, tex, x0, y1 - r, 0.0f, (float)r, r, r, d, d, color);
        this.ctx.method_25291(class_10799.field_56883, tex, x1 - r, y1 - r, (float)r, (float)r, r, r, d, d, color);
        if (x1 - r > x0 + r) {
            this.ctx.method_25294(x0 + r, y0, x1 - r, y1, color);
        }
        if (y1 - r > y0 + r) {
            this.ctx.method_25294(x0, y0 + r, x0 + r, y1 - r, color);
            this.ctx.method_25294(x1 - r, y0 + r, x1, y1 - r, color);
        }
    }

    private class_2960 corner(int r) {
        return CORNERS.computeIfAbsent(r, radius -> {
            int d = radius * 2;
            int ss = 4;
            class_1011 img = new class_1011(d, d, false);
            for (int y = 0; y < d; ++y) {
                for (int x = 0; x < d; ++x) {
                    int inside = 0;
                    for (int sy = 0; sy < ss; ++sy) {
                        for (int sx = 0; sx < ss; ++sx) {
                            double py;
                            double px = (double)x + ((double)sx + 0.5) / (double)ss - (double)radius.intValue();
                            if (!(px * px + (py = (double)y + ((double)sy + 0.5) / (double)ss - (double)radius.intValue()) * py <= (double)radius.intValue() * (double)radius.intValue())) continue;
                            ++inside;
                        }
                    }
                    int a = Math.round((float)inside * 255.0f / (float)(ss * ss));
                    img.method_61941(x, y, a << 24 | 0xFFFFFF);
                }
            }
            class_2960 id = class_2960.method_60655((String)"shoremod", (String)("dyn/corner_" + radius));
            this.mc.method_1531().method_4616(id, (class_1044)new class_1043(() -> "shoremod corner " + radius, img));
            return id;
        });
    }

    void icon(String name, float x, float y, float size, int color) {
        int s = Math.max(1, this.p(size));
        class_2960 id = this.iconTexture(name, s);
        if (id == null) {
            return;
        }
        this.ctx.method_25291(class_10799.field_56883, id, this.p(x), this.p(y), 0.0f, 0.0f, s, s, s, s, name.equals("@logo") ? -1 : color);
    }

    private class_2960 iconTexture(String name, int s) {
        String key = name + "@" + s;
        class_2960 cached = ICONS.get(key);
        if (cached != null) {
            return cached;
        }
        class_1011 src = ICON_SOURCES.computeIfAbsent(name, n -> {
            class_2960 file = n.equals("@logo") ? class_2960.method_60655((String)"shoremod", (String)"textures/font/badge.png") : class_2960.method_60655((String)"shoremod", (String)("textures/gui/icon/" + n + ".png"));
            try (InputStream in = ((class_3298)this.mc.method_1478().method_14486(file).orElseThrow()).method_14482()) {
                return class_1011.method_4309(in);
            } catch (IOException e) {
                ShoreClient.LOG.warn("Missing ShoreMod icon {}", n);
                return null;
            }
        });
        if (src == null) {
            return null;
        }
        class_1011 out = ShoreUi.downscale(src, s);
        class_2960 id = class_2960.method_60655((String)"shoremod", (String)("dyn/icon/" + name.replace("@", "") + "_" + s));
        this.mc.method_1531().method_4616(id, (class_1044)new class_1043(() -> "shoremod icon " + key, out));
        ICONS.put(key, id);
        return id;
    }

    private static class_1011 downscale(class_1011 src, int s) {
        int sw = src.method_4307();
        int sh = src.method_4323();
        class_1011 out = new class_1011(s, s, false);
        double fx = (double)sw / (double)s;
        double fy = (double)sh / (double)s;
        for (int dy = 0; dy < s; ++dy) {
            double y0 = (double)dy * fy;
            double y1 = (double)(dy + 1) * fy;
            for (int dx = 0; dx < s; ++dx) {
                double x0 = (double)dx * fx;
                double x1 = (double)(dx + 1) * fx;
                double a = 0.0;
                double r = 0.0;
                double g = 0.0;
                double b = 0.0;
                double area = 0.0;
                for (int sy = (int)y0; sy < Math.min(sh, (int)Math.ceil(y1)); ++sy) {
                    double wy = Math.min(y1, (double)(sy + 1)) - Math.max(y0, (double)sy);
                    for (int sx = (int)x0; sx < Math.min(sw, (int)Math.ceil(x1)); ++sx) {
                        double wx = Math.min(x1, (double)(sx + 1)) - Math.max(x0, (double)sx);
                        double w = wx * wy;
                        int c = src.method_61940(sx, sy);
                        double ca = (double)(c >>> 24) / 255.0;
                        a += ca * w;
                        r += (double)(c >> 16 & 0xFF) * ca * w;
                        g += (double)(c >> 8 & 0xFF) * ca * w;
                        b += (double)(c & 0xFF) * ca * w;
                        area += w;
                    }
                }
                int oa = (int)Math.round(a / area * 255.0);
                int or = 0;
                int og = 0;
                int ob = 0;
                if (a > 0.0) {
                    or = (int)Math.round(r / a);
                    og = (int)Math.round(g / a);
                    ob = (int)Math.round(b / a);
                }
                out.method_61941(dx, dy, oa << 24 | Math.min(255, or) << 16 | Math.min(255, og) << 8 | Math.min(255, ob));
            }
        }
        return out;
    }

    private class_2561 styled(String s, boolean boldWanted, float k) {
        boolean bold;
        boolean bl = bold = boldWanted && !this.noBold;
        if (!this.sans) {
            return STYLED.computeIfAbsent((bold ? "b|" : "r|") + s, x -> class_2561.method_43470((String)s).method_10862(class_2583.field_24360.method_10982(Boolean.valueOf(bold))));
        }
        int key = Math.round(k * 1000.0f);
        return STYLED.computeIfAbsent((bold ? "sb" : "sr") + key + "|" + s, x -> class_2561.method_43470((String)s).method_10862(class_2583.field_24360.method_27704((class_11719)new class_11719.class_11721(class_2960.method_60655((String)"shoremod", (String)("px_" + (bold ? "b" : "r") + "_" + key))))));
    }

    private float snap(float size) {
        if (!this.sans) {
            return Math.max(1, Math.round(size * this.u / 9.0f));
        }
        float want = size * this.u / 11.0f;
        float best = OVERSAMPLE[0];
        for (float k : OVERSAMPLE) {
            if (!(Math.abs(k - want) < Math.abs(best - want))) continue;
            best = k;
        }
        return best;
    }

    float width(String s, boolean bold, float size) {
        float k = this.snap(size);
        return (float)this.mc.field_1772.method_27525((class_5348)this.styled(s, bold, k)) * k / this.u;
    }

    void text(String s, boolean bold, float size, float x, float y, int color) {
        int c = color & 0xFFFFFF;
        boolean lightText = ((c >> 16 & 0xFF) * 299 + (c >> 8 & 0xFF) * 587 + (c & 0xFF) * 114) / 1000 > 120;
        this.draw(s, bold, size, x, y, color, lightText && !this.sans);
    }

    void textShadow(String s, boolean bold, float size, float x, float y, int color) {
        this.draw(s, bold, size, x, y, color, true);
    }

    private void draw(String s, boolean bold, float size, float x, float y, int color, boolean shadow) {
        if (s.isEmpty()) {
            return;
        }
        float k = this.snap(size);
        Matrix3x2fStack m = this.ctx.method_51448();
        m.pushMatrix();
        float glyphH = this.sans ? 7.92f : 8.0f;
        float offset = this.sans ? (size * this.u - glyphH * k) / 2.0f + k * 1.0f : (size * this.u - 8.0f * k) / 2.0f;
        m.translate((float)this.p(x), (float)Math.round(y * this.u + offset));
        m.scale(k, k);
        this.ctx.method_51439(this.mc.field_1772, this.styled(s, bold, k), 0, 0, color, shadow);
        m.popMatrix();
    }

    void text(String s, boolean bold, float size, float x, float y, int color, float maxW) {
        this.text(this.fit(s, bold, size, maxW), bold, size, x, y, color);
    }

    void centered(String s, boolean bold, float size, float cx, float y, int color, float maxW) {
        String f = this.fit(s, bold, size, maxW);
        this.text(f, bold, size, cx - this.width(f, bold, size) / 2.0f, y, color);
    }

    String fit(String s, boolean bold, float size, float maxW) {
        if (maxW <= 0.0f || this.width(s, bold, size) <= maxW) {
            return s;
        }
        int lo = 0;
        int hi = s.length();
        while (lo < hi) {
            int mid = (lo + hi + 1) / 2;
            if (this.width(s.substring(0, mid).stripTrailing() + "...", bold, size) <= maxW) {
                lo = mid;
                continue;
            }
            hi = mid - 1;
        }
        return s.substring(0, lo).stripTrailing() + "...";
    }

    void scissor(float x, float y, float w, float h) {
        this.ctx.method_44379(this.p(x), this.p(y), this.p(x + w), this.p(y + h));
    }

    void endScissor() {
        this.ctx.method_44380();
    }

    static int mix(int a, int b, float t) {
        t = Math.clamp(t, 0.0f, 1.0f);
        int aa = a >>> 24;
        int ar = a >> 16 & 0xFF;
        int ag = a >> 8 & 0xFF;
        int ab = a & 0xFF;
        int ba = b >>> 24;
        int br = b >> 16 & 0xFF;
        int bg = b >> 8 & 0xFF;
        int bb = b & 0xFF;
        return Math.round((float)aa + (float)(ba - aa) * t) << 24 | Math.round((float)ar + (float)(br - ar) * t) << 16 | Math.round((float)ag + (float)(bg - ag) * t) << 8 | Math.round((float)ab + (float)(bb - ab) * t);
    }

    static int alpha(int color, float a) {
        return Math.round((float)(color >>> 24) * a) << 24 | color & 0xFFFFFF;
    }
}

