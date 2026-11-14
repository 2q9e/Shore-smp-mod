/*
 * Decompiled with CFR 0.152.
 */
package net.shoresmp.fabric.mixin;

import net.minecraft.class_4184;
import net.minecraft.class_757;
import net.shoresmp.fabric.ShoreClient;
import net.shoresmp.fabric.ShoreTools;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={class_757.class})
public abstract class GameRendererMixin {
    @Inject(method={"method_3192"}, at={@At(value="TAIL")})
    private void shore$capture(CallbackInfo ci) {
        if (ShoreClient.recorder != null) {
            ShoreClient.recorder.capture();
        }
    }

    @Inject(method={"method_3196"}, at={@At(value="RETURN")}, cancellable=true)
    private void shore$zoom(class_4184 camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Float> cir) {
        if (!changingFov || ShoreClient.config == null) {
            return;
        }
        double divisor = ShoreTools.fovDivisor();
        if (divisor != 1.0) {
            cir.setReturnValue(Float.valueOf((float)((double)cir.getReturnValueF() / divisor)));
        }
    }
}

