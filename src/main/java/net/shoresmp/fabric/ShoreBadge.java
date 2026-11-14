/*
 * Decompiled with CFR 0.152.
 */
package net.shoresmp.fabric;

import net.minecraft.class_11719;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_2960;

public final class ShoreBadge {
    private static final class_2583 LOGO_STYLE = class_2583.field_24360.method_27704((class_11719)new class_11719.class_11721(class_2960.method_60655((String)"shoremod", (String)"badge"))).method_36139(0xFFFFFF);
    private static final class_2583 TAB_STYLE = class_2583.field_24360.method_27704((class_11719)new class_11719.class_11721(class_2960.method_60655((String)"shoremod", (String)"badge_tab"))).method_36139(0xFFFFFF);

    public static class_2561 prependTab(class_2561 label) {
        return class_2561.method_43473().method_10852((class_2561)class_2561.method_43470((String)"\ue000").method_10862(TAB_STYLE)).method_27693(" ").method_10852(label);
    }

    public static class_2561 prepend(class_2561 label) {
        return class_2561.method_43473().method_10852((class_2561)class_2561.method_43470((String)"\ue000").method_10862(LOGO_STYLE)).method_27693(" ").method_10852(label);
    }
}

