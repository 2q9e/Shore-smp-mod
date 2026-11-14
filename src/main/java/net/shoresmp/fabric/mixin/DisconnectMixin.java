/*
 * Decompiled with CFR 0.152.
 */
package net.shoresmp.fabric.mixin;

import net.minecraft.class_8673;
import net.minecraft.class_9812;
import net.shoresmp.fabric.ShoreClient;
import net.shoresmp.fabric.ShoreTools;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_8673.class})
public abstract class DisconnectMixin {
    @Inject(method={"method_10839"}, at={@At(value="HEAD")})
    private void shore$disconnect(class_9812 info, CallbackInfo ci) {
        String reason;
        ShoreTools.lastDisconnectReason = reason = info.comp_2853().getString();
        if (ShoreClient.recorder != null) {
            ShoreClient.recorder.disconnected(reason);
        }
    }
}

