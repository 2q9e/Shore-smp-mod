/*
 * Decompiled with CFR 0.152.
 */
package net.shoresmp.fabric;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1109;
import net.minecraft.class_1113;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_3417;
import net.minecraft.class_437;
import net.minecraft.class_6880;
import net.shoresmp.fabric.ShoreClient;
import net.shoresmp.fabric.ShoreUi;

abstract class ShoreDialog
extends class_437 {
    protected static final int SECONDARY = 0;
    protected static final int PRIMARY = 1;
    protected static final int DANGER = 2;
    protected static final int TEXT = -723465;
    protected static final int MUTED = -6577750;
    private final ShoreUi ui = new ShoreUi();
    private final List<Hit> hits = new ArrayList<Hit>();
    protected final class_437 parent;

    protected ShoreDialog(String title, class_437 parent) {
        super((class_2561)class_2561.method_43470((String)title));
        this.parent = parent;
    }

    protected abstract String icon();

    protected abstract String subtitle();

    protected abstract List<Line> lines();

    protected abstract List<Button> buttons();

    public void method_25394(class_332 ctx, int gx, int gy, float delta) {
        this.ui.begin(ctx, ShoreUi.unit(this.field_22787, ShoreClient.config.menuScale));
        this.ui.sans = false;
        this.ui.noBold = true;
        float mx = this.ui.fromGui(this.field_22787.field_1729.method_68879(this.field_22787.method_22683()));
        float my = this.ui.fromGui(this.field_22787.field_1729.method_68883(this.field_22787.method_22683()));
        this.hits.clear();
        int accent = 0xFF000000 | ShoreClient.config.accent;
        this.ui.rect(0.0f, 0.0f, this.ui.W, this.ui.H, 0x66000000);
        List<Line> body = this.lines();
        List<Button> btns = this.buttons();
        float w = Math.min(480.0f, this.ui.W - 40.0f);
        float pad = 24.0f;
        float h = pad + 44.0f + 12.0f + (float)(body.size() * 22) + 16.0f + 38.0f + pad;
        float x = (this.ui.W - w) / 2.0f;
        float y = (this.ui.H - h) / 2.0f;
        this.ui.outline(x, y, w, h, 14.0f, -14473427, -99610091);
        this.ui.round(x + pad, y + pad, 40.0f, 40.0f, 10.0f, ShoreUi.alpha(accent, 0.16f));
        this.ui.icon(this.icon(), x + pad + 10.0f, y + pad + 10.0f, 20.0f, accent);
        this.ui.text(this.method_25440().getString(), true, 17.0f, x + pad + 54.0f, y + pad + 2.0f, -723465, w - pad * 2.0f - 54.0f);
        this.ui.text(this.subtitle(), false, 13.0f, x + pad + 54.0f, y + pad + 24.0f, -6577750, w - pad * 2.0f - 54.0f);
        float ty = y + pad + 44.0f + 16.0f;
        for (Line l : body) {
            this.ui.text(l.text(), l.bold(), 13.5f, x + pad, ty, l.color(), w - pad * 2.0f);
            ty += 22.0f;
        }
        float bx = x + w - pad;
        float by = y + h - pad - 38.0f;
        for (int i = btns.size() - 1; i >= 0; --i) {
            int bg;
            Button b = btns.get(i);
            float bw = Math.max(96.0f, this.ui.width(b.label(), true, 13.5f) + 36.0f);
            boolean hov = mx >= (bx -= bw) && my >= by && mx < bx + bw && my < by + 38.0f;
            this.hits.add(new Hit(bx, by, bw, 38.0f, b.action()));
            switch (b.style()) {
                case 1: {
                    int n;
                    if (hov) {
                        n = ShoreUi.mix(accent, -1, 0.12f);
                        break;
                    }
                    n = accent;
                    break;
                }
                case 2: {
                    int n;
                    if (hov) {
                        n = -1025955;
                        break;
                    }
                    n = -1750963;
                    break;
                }
                default: {
                    int n = bg = hov ? -14012619 : -14802393;
                }
            }
            int fg = b.style() == 1 ? (ShoreDialog.light(accent) ? -15921390 : -1) : (b.style() == 2 ? -1 : -723465);
            this.ui.round(bx, by, bw, 38.0f, 9.0f, bg);
            this.ui.centered(b.label(), true, 13.5f, bx + bw / 2.0f, by + 12.25f, fg, bw - 12.0f);
            bx -= 10.0f;
        }
        this.ui.end();
    }

    private static boolean light(int c) {
        return ((c >> 16 & 0xFF) * 299 + (c >> 8 & 0xFF) * 587 + (c & 0xFF) * 114) / 1000 > 150;
    }

    public boolean method_25402(class_11909 click, boolean doubled) {
        float px = this.ui.fromGui(click.comp_4798());
        float py = this.ui.fromGui(click.comp_4799());
        for (Hit h : this.hits) {
            if (!(px >= h.x()) || !(py >= h.y()) || !(px < h.x() + h.w()) || !(py < h.y() + h.h())) continue;
            this.field_22787.method_1483().method_4873((class_1113)class_1109.method_47978((class_6880)class_3417.field_15015, (float)1.0f));
            h.action().run();
            return true;
        }
        return true;
    }

    public void method_25419() {
        this.field_22787.method_1507(this.parent);
    }

    public boolean method_25421() {
        return false;
    }

    protected record Line(String text, int color, boolean bold) {
    }

    protected record Button(String label, int style, Runnable action) {
    }

    private record Hit(float x, float y, float w, float h, Runnable action) {
    }
}

