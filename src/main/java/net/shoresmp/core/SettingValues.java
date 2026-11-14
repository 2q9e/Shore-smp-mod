/*
 * Decompiled with CFR 0.152.
 */
package net.shoresmp.core;

import java.util.OptionalInt;

public final class SettingValues {
    private SettingValues() {
    }

    public static OptionalInt integer(String text, int min, int max) {
        if (text == null || !text.matches("[0-9]{1,6}")) {
            return OptionalInt.empty();
        }
        int value = Integer.parseInt(text);
        return value >= min && value <= max ? OptionalInt.of(value) : OptionalInt.empty();
    }

    public static OptionalInt color(String text) {
        if (text == null || !text.matches("#?[0-9a-fA-F]{6}")) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(Integer.parseInt(text.replace("#", ""), 16));
    }

    public static double brightness(double vanilla, int strength) {
        double base = Double.isFinite(vanilla) ? Math.clamp(vanilla, 0.0, 16.0) : 1.0;
        return base + (16.0 - base) * (double)Math.clamp((long)strength, 0, 100) / 100.0;
    }
}

