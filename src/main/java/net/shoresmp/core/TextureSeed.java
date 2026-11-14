/*
 * Decompiled with CFR 0.152.
 */
package net.shoresmp.core;

public final class TextureSeed {
    private TextureSeed() {
    }

    public static long scramble(long position, long salt) {
        long v = position ^ salt;
        v = (v ^ v >>> 30) * -4658895280553007687L;
        v = (v ^ v >>> 27) * -7723592293110705685L;
        return v ^ v >>> 31;
    }
}

