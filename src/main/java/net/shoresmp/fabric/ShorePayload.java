/*
 * Decompiled with CFR 0.152.
 */
package net.shoresmp.fabric;

import net.minecraft.class_2960;
import net.minecraft.class_8710;
import net.minecraft.class_9129;
import net.minecraft.class_9139;

public record ShorePayload(byte[] bytes) implements class_8710
{
    public static final class_8710.class_9154<ShorePayload> ID = new class_8710.class_9154(class_2960.method_60654((String)"shoremod:wire"));
    public static final class_9139<class_9129, ShorePayload> CODEC = new class_9139<class_9129, ShorePayload>(){

        public ShorePayload decode(class_9129 buf) {
            int length = buf.readableBytes();
            if (length > 30000) {
                throw new IllegalArgumentException("ShoreMod payload too large");
            }
            byte[] bytes = new byte[length];
            buf.method_52979(bytes);
            return new ShorePayload(bytes);
        }

        public void encode(class_9129 buf, ShorePayload payload) {
            if (payload.bytes.length > 30000) {
                throw new IllegalArgumentException("ShoreMod payload too large");
            }
            buf.method_52983(payload.bytes);
        }
    };

    public class_8710.class_9154<? extends class_8710> method_56479() {
        return ID;
    }
}

