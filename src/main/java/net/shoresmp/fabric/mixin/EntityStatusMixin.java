/*
 * Decompiled with CFR 0.152.
 */
package net.shoresmp.fabric.mixin;

import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_2663;
import net.minecraft.class_310;
import net.minecraft.class_634;
import net.shoresmp.fabric.ShorePvp;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_634.class})
public abstract class EntityStatusMixin {
    @Inject(method={"method_11148"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_2600;method_11074(Lnet/minecraft/class_2596;Lnet/minecraft/class_2547;Lnet/minecraft/class_11980;)V", shift=At.Shift.AFTER)})
    private void shore$totemPops(class_2663 packet, CallbackInfo ci) {
        class_310 client = class_310.method_1551();
        if (client.field_1687 == null) {
            return;
        }
        class_1297 entity = packet.method_11469((class_1937)client.field_1687);
        if (entity != null) {
            ShorePvp.onEntityStatus(entity, packet.method_11470());
        }
    }
}

