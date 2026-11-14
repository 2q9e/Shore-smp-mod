/*
 * Decompiled with CFR 0.152.
 */
package net.shoresmp.fabric;

import net.minecraft.class_2960;
import net.minecraft.class_8710;
import net.minecraft.class_9129;
import net.minecraft.class_9139;

public record UpdatePayload(byte[] bytes) implements class_8710
{
    public static final class_8710.class_9154<UpdatePayload> ID = new class_8710.class_9154(class_2960.method_60654((String)"shoremod:updates"));
    public static final class_9139<class_9129, UpdatePayload> CODEC = new class_9139<class_9129, UpdatePayload>(){

        public UpdatePayload decode(class_9129 buf) {
            int length = buf.readableBytes();
            if (length > 4096) {
                throw new IllegalArgumentException("ShoreMod payload too large");
            }
            byte[] bytes = new byte[length];
            buf.method_52979(bytes);
            return new UpdatePayload(bytes);
        }

        public void encode(class_9129 buf, UpdatePayload payload) {
            if (payload.bytes.length > 4096) {
                throw new IllegalArgumentException("ShoreMod payload too large");
            }
            buf.method_52983(payload.bytes);
        }
    };

    public class_8710.class_9154<? extends class_8710> method_56479() {
        return ID;
    }
}

