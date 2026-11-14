/*
 * Decompiled with CFR 0.152.
 */
package net.shoresmp.fabric.mixin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.class_1041;
import net.minecraft.class_11909;
import net.minecraft.class_1657;
import net.minecraft.class_1661;
import net.minecraft.class_1703;
import net.minecraft.class_1713;
import net.minecraft.class_1723;
import net.minecraft.class_1734;
import net.minecraft.class_1735;
import net.minecraft.class_1747;
import net.minecraft.class_1767;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_2371;
import net.minecraft.class_2480;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_312;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_465;
import net.minecraft.class_9288;
import net.minecraft.class_9334;
import net.shoresmp.fabric.Config;
import net.shoresmp.fabric.ShoreClient;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={class_465.class})
public abstract class HandledScreenMixin
extends class_437 {
    @Shadow
    @Final
    protected class_1703 field_2797;
    @Unique
    private static final int SHORE_NONE = 0;
    @Unique
    private static final int SHORE_PLACE = 1;
    @Unique
    private static final int SHORE_MOVE = 2;
    @Unique
    private static final int SHORE_COLLECT = 3;
    @Shadow
    protected class_1735 field_2787;
    @Unique
    private int shore$mode = 0;
    @Unique
    private class_1735 shore$lastSlot;
    @Unique
    private final Set<class_1735> shore$visited = new HashSet<class_1735>();
    @Unique
    private class_1799 shore$filter = class_1799.field_8037;
    @Unique
    private double shore$lx;
    @Unique
    private double shore$ly;
    @Shadow
    protected int field_2776;
    @Shadow
    protected int field_2800;

    @Shadow
    private class_1735 method_64240(double x, double y) {
        throw new AssertionError();
    }

    @Shadow
    protected abstract boolean method_2381(double var1, double var3, int var5, int var6);

    protected HandledScreenMixin(class_2561 title) {
        super(title);
    }

    @Unique
    private static Config shore$cfg() {
        return ShoreClient.config;
    }

    @Unique
    private static boolean shore$on(boolean sub) {
        Config c = HandledScreenMixin.shore$cfg();
        return c != null && c.mouseTweaks && sub;
    }

    @Unique
    private void shore$click(class_1735 slot, int button, class_1713 type) {
        class_310 client = class_310.method_1551();
        if (client.field_1724 == null || client.field_1761 == null) {
            return;
        }
        client.field_1761.method_2906(this.field_2797.field_7763, slot.field_7874, button, type, (class_1657)client.field_1724);
    }

    @Inject(method={"method_25402"}, at={@At(value="HEAD")}, cancellable=true)
    private void shore$press(class_11909 click, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
        double mx = click.comp_4798();
        double my = click.comp_4799();
        int button = click.method_74245();
        Config c = HandledScreenMixin.shore$cfg();
        class_1735 slot = this.method_64240(mx, my);
        if (c == null) {
            return;
        }
        class_1799 cursor = this.field_2797.method_34255();
        boolean inside = slot != null || !this.method_2381(mx, my, this.field_2776, this.field_2800);
        this.shore$visited.clear();
        this.shore$lastSlot = slot;
        this.shore$lx = mx;
        this.shore$ly = my;
        this.shore$filter = cursor.method_7960() ? class_1799.field_8037 : cursor.method_7972();
        boolean shift = click.method_74239();
        this.shore$mode = 0;
        if (!inside) {
            return;
        }
        if (button == 1 && !shift && !cursor.method_7960() && HandledScreenMixin.shore$on(c.tweakRmb)) {
            this.shore$mode = 1;
            if (slot != null) {
                this.shore$place(slot);
            }
            cir.setReturnValue((Object)true);
        } else if (button == 0 && shift && HandledScreenMixin.shore$on(c.tweakShift)) {
            this.shore$mode = 2;
            if (slot != null) {
                this.shore$visited.add(slot);
                if (!this.shore$matches(slot)) {
                    cir.setReturnValue((Object)true);
                    this.shore$visited.remove(slot);
                }
            } else {
                cir.setReturnValue((Object)true);
            }
        } else if (button == 0 && !shift && !cursor.method_7960() && HandledScreenMixin.shore$on(c.tweakLmb)) {
            if (slot == null) {
                this.shore$mode = 3;
                cir.setReturnValue((Object)true);
            } else if (slot.method_7681() && class_1799.method_31577((class_1799)slot.method_7677(), (class_1799)cursor)) {
                this.shore$mode = 3;
                this.shore$visited.add(slot);
                this.shore$collect(slot);
                cir.setReturnValue((Object)true);
            }
        }
    }

    @Unique
    private boolean shore$matches(class_1735 slot) {
        return slot.method_7681() && (this.shore$filter.method_7960() || class_1799.method_31577((class_1799)slot.method_7677(), (class_1799)this.shore$filter));
    }

    @Inject(method={"method_25403"}, at={@At(value="HEAD")}, cancellable=true)
    private void shore$drag(class_11909 click, double dx, double dy, CallbackInfoReturnable<Boolean> cir) {
        if (this.shore$mode == 0) {
            return;
        }
        this.shore$step(click.comp_4798(), click.comp_4799());
        cir.setReturnValue((Object)true);
    }

    @Inject(method={"method_71085"}, at={@At(value="HEAD")})
    private void shore$poll(class_332 ctx, int mx, int my, float delta, CallbackInfo ci) {
        if (this.shore$mode == 0) {
            return;
        }
        class_312 mouse = class_310.method_1551().field_1729;
        class_1041 window = class_310.method_1551().method_22683();
        this.shore$step(mouse.method_68879(window), mouse.method_68883(window));
    }

    @Inject(method={"method_25432"}, at={@At(value="HEAD")}, require=0)
    private void shore$closed(CallbackInfo ci) {
        this.shore$reset();
    }

    @Inject(method={"method_25406"}, at={@At(value="HEAD")}, cancellable=true)
    private void shore$release(class_11909 click, CallbackInfoReturnable<Boolean> cir) {
        int button = click.method_74245();
        if (this.shore$mode == 1 && button == 1 || this.shore$mode == 3 && button == 0) {
            this.shore$reset();
            cir.setReturnValue((Object)true);
        } else if (this.shore$mode == 2 && button == 0) {
            this.shore$reset();
        }
    }

    @Unique
    private void shore$reset() {
        this.shore$mode = 0;
        this.shore$lastSlot = null;
        this.shore$visited.clear();
    }

    @Unique
    private void shore$step(double mx, double my) {
        double dx = mx - this.shore$lx;
        double dy = my - this.shore$ly;
        int n = Math.max(1, (int)Math.ceil(Math.hypot(dx, dy) / 3.0));
        for (int i = 1; i <= n && this.shore$mode != 0; ++i) {
            this.shore$visit(this.method_64240(this.shore$lx + dx * (double)i / (double)n, this.shore$ly + dy * (double)i / (double)n));
        }
        this.shore$lx = mx;
        this.shore$ly = my;
    }

    @Unique
    private void shore$visit(class_1735 slot) {
        if (slot == this.shore$lastSlot) {
            return;
        }
        this.shore$lastSlot = slot;
        if (slot == null) {
            return;
        }
        switch (this.shore$mode) {
            case 1: {
                this.shore$place(slot);
                break;
            }
            case 2: {
                if (!this.shore$matches(slot) || !this.shore$visited.add(slot)) break;
                this.shore$quickMove(slot);
                break;
            }
            case 3: {
                if (!this.shore$visited.add(slot)) break;
                this.shore$collect(slot);
                break;
            }
        }
    }

    @Unique
    private void shore$place(class_1735 slot) {
        class_310 client = class_310.method_1551();
        class_1799 cursor = this.field_2797.method_34255();
        if (client.field_1724 == null || cursor.method_7960() || !slot.method_7682() || !slot.method_7680(cursor)) {
            return;
        }
        class_1799 stack = slot.method_7677();
        if (!(stack.method_7960() || class_1799.method_31577((class_1799)stack, (class_1799)cursor) && stack.method_7947() < slot.method_7676(cursor))) {
            return;
        }
        this.shore$click(slot, 1, class_1713.field_7790);
    }

    @Unique
    private void shore$quickMove(class_1735 slot) {
        class_310 client = class_310.method_1551();
        if (client.field_1724 == null || !slot.method_7682() || !slot.method_7674((class_1657)client.field_1724)) {
            return;
        }
        this.shore$click(slot, 0, class_1713.field_7794);
    }

    @Unique
    private void shore$collect(class_1735 slot) {
        class_310 client = class_310.method_1551();
        class_1799 cursor = this.field_2797.method_34255();
        class_1799 stack = slot.method_7677();
        if (client.field_1724 == null || cursor.method_7960() || stack.method_7960() || !slot.method_7682() || !slot.method_7674((class_1657)client.field_1724)) {
            return;
        }
        if (!class_1799.method_31577((class_1799)stack, (class_1799)cursor)) {
            return;
        }
        if (cursor.method_7947() + stack.method_7947() > Math.min(cursor.method_7914(), slot.method_7676(cursor))) {
            return;
        }
        if (!slot.method_7680(cursor)) {
            return;
        }
        this.shore$click(slot, 0, class_1713.field_7790);
        this.shore$click(slot, 0, class_1713.field_7790);
    }

    @Inject(method={"method_25401"}, at={@At(value="HEAD")}, cancellable=true)
    private void shore$wheel(double mx, double my, double horizontal, double vertical, CallbackInfoReturnable<Boolean> cir) {
        boolean done;
        Config c = HandledScreenMixin.shore$cfg();
        if (vertical == 0.0 || !HandledScreenMixin.shore$on(c != null && c.tweakWheel) || !this.field_2797.method_34255().method_7960()) {
            return;
        }
        class_1735 slot = this.method_64240(mx, my);
        if (slot == null || !slot.method_7682()) {
            return;
        }
        boolean bl = done = vertical < 0.0 ? this.shore$pushOne(slot) : this.shore$pullOne(slot);
        if (done || slot.method_7681()) {
            cir.setReturnValue((Object)true);
        }
    }

    @Unique
    private List<class_1735> shore$otherSide(class_1735 from) {
        ArrayList<class_1735> out = new ArrayList<class_1735>();
        boolean playerOnly = this.field_2797 instanceof class_1723;
        boolean fromHotbar = from.field_7871 instanceof class_1661 && from.method_34266() < 9;
        for (class_1735 s : this.field_2797.field_7761) {
            if (s == from || !s.method_7682() || s instanceof class_1734 || (playerOnly ? !(s.field_7871 instanceof class_1661) || s.method_34266() >= 36 || fromHotbar == s.method_34266() < 9 : s.field_7871 == from.field_7871)) continue;
            out.add(s);
        }
        return out;
    }

    @Unique
    private boolean shore$pushOne(class_1735 from) {
        class_310 client = class_310.method_1551();
        class_1799 stack = from.method_7677();
        if (stack.method_7960() || client.field_1724 == null || !from.method_7674((class_1657)client.field_1724)) {
            return false;
        }
        class_1735 target = null;
        for (class_1735 s : this.shore$otherSide(from)) {
            if (!s.method_7681() || !class_1799.method_31577((class_1799)s.method_7677(), (class_1799)stack) || s.method_7677().method_7947() >= s.method_7676(stack) || !s.method_7680(stack)) continue;
            target = s;
            break;
        }
        if (target == null) {
            for (class_1735 s : this.shore$otherSide(from)) {
                if (s.method_7681() || !s.method_7680(stack)) continue;
                target = s;
                break;
            }
        }
        if (target == null) {
            return false;
        }
        this.shore$click(from, 0, class_1713.field_7790);
        this.shore$click(target, 1, class_1713.field_7790);
        this.shore$click(from, 0, class_1713.field_7790);
        return true;
    }

    @Unique
    private boolean shore$pullOne(class_1735 into) {
        class_310 client = class_310.method_1551();
        class_1799 here = into.method_7677();
        if (here.method_7960() || client.field_1724 == null || here.method_7947() >= into.method_7676(here)) {
            return false;
        }
        class_1735 source = null;
        for (class_1735 s : this.shore$otherSide(into)) {
            if (!s.method_7681() || !class_1799.method_31577((class_1799)s.method_7677(), (class_1799)here) || !s.method_7674((class_1657)client.field_1724)) continue;
            source = s;
            break;
        }
        if (source == null) {
            return false;
        }
        this.shore$click(source, 0, class_1713.field_7790);
        this.shore$click(into, 1, class_1713.field_7790);
        this.shore$click(source, 0, class_1713.field_7790);
        return true;
    }

    @Inject(method={"method_2380"}, at={@At(value="HEAD")}, cancellable=true)
    private void shore$shulkerTooltip(class_332 ctx, int mx, int my, CallbackInfo ci) {
        long w;
        class_1747 block;
        Config c = HandledScreenMixin.shore$cfg();
        if (c == null || !c.shulkerPreview || this.field_2787 == null || !this.field_2797.method_34255().method_7960()) {
            return;
        }
        class_1799 stack = this.field_2787.method_7677();
        class_1792 class_17922 = stack.method_7909();
        if (!(class_17922 instanceof class_1747) || !((class_17922 = (block = (class_1747)class_17922).method_7711()) instanceof class_2480)) {
            return;
        }
        class_2480 box = (class_2480)class_17922;
        if (c.shulkerMode == 0 && GLFW.glfwGetKey((long)(w = class_310.method_1551().method_22683().method_4490()), (int)340) != 1 && GLFW.glfwGetKey((long)w, (int)344) != 1) {
            return;
        }
        ci.cancel();
        class_2371 items = class_2371.method_10213((int)27, (Object)class_1799.field_8037);
        ((class_9288)stack.method_58695(class_9334.field_49622, (Object)class_9288.field_49334)).method_57492(items);
        float sc = (float)c.shulkerScale / 100.0f;
        int titleH = c.shulkerTitle ? 13 : 0;
        int pw = 172;
        int ph = 63 + titleH;
        float sw = (float)pw * sc;
        float sh = (float)ph * sc;
        float x = mx + 12;
        float y = my - 12;
        if (x + sw > (float)(this.field_22789 - 4)) {
            x = (float)(mx - 12) - sw;
        }
        y = Math.clamp(y, 4.0f, Math.max(4.0f, (float)this.field_22790 - sh - 4.0f));
        class_1767 dye = box.method_10528();
        int border = switch (c.shulkerBorderMode) {
            case 0 -> {
                if (dye != null) {
                    yield 0xFF000000 | dye.method_7787();
                }
                yield -7447360;
            }
            case 1 -> 0xFF000000 | c.accent;
            default -> 0;
        };
        int bg = Math.round((float)c.shulkerBgOpacity * 2.55f) << 24 | 0x101216;
        Matrix3x2fStack m = ctx.method_51448();
        m.pushMatrix();
        m.translate(x, y);
        m.scale(sc, sc);
        if (border != 0) {
            ctx.method_25294(1, 0, pw - 1, ph, border);
            ctx.method_25294(0, 1, pw, ph - 1, border);
        }
        ctx.method_25294(2, 1, pw - 2, ph - 1, bg);
        ctx.method_25294(1, 2, pw - 1, ph - 2, bg);
        if (c.shulkerTitle) {
            ctx.method_27535(this.field_22793, stack.method_7964(), 6, 6, -1);
        }
        for (int i = 0; i < 27; ++i) {
            class_1799 item;
            int sx = 5 + i % 9 * 18;
            int sy = 5 + titleH + i / 9 * 18;
            if (c.shulkerEmptySlots) {
                ctx.method_25294(sx, sy, sx + 17, sy + 17, 0x22FFFFFF);
            }
            if ((item = (class_1799)items.get(i)).method_7960()) continue;
            ctx.method_51427(item, sx, sy);
            ctx.method_51431(this.field_22793, item, sx, sy);
        }
        m.popMatrix();
    }
}

