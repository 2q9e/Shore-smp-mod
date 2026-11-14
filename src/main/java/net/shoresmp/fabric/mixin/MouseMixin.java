/*
 * Decompiled with CFR 0.152.
 */
package net.shoresmp.fabric.mixin;

import net.minecraft.class_11910;
import net.minecraft.class_310;
import net.minecraft.class_312;
import net.shoresmp.fabric.ShoreClient;
import net.shoresmp.fabric.ShoreTools;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_312.class})
public abstract class MouseMixin {
    @Inject(method={"method_1601"}, at={@At(value="HEAD")})
    private void shore$cps(long window, class_11910 input, int action, CallbackInfo ci) {
        if (action == 1 && class_310.method_1551().field_1755 == null) {
            ShoreTools.click(input.comp_4801());
        }
    }

    @ModifyArg(method={"method_1606"}, at=@At(value="INVOKE", target="Lnet/minecraft/class_746;method_5872(DD)V"), index=0)
    private double shore$zoomYaw(double dx) {
        return dx * ShoreTools.lookScale();
    }

    @ModifyArg(method={"method_1606"}, at=@At(value="INVOKE", target="Lnet/minecraft/class_746;method_5872(DD)V"), index=1)
    private double shore$zoomPitch(double dy) {
        return dy * ShoreTools.lookScale();
    }

    @Inject(method={"method_1598"}, at={@At(value="HEAD")}, cancellable=true)
    private void shore$zoomScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (ShoreClient.config != null && ShoreTools.scroll(vertical)) {
            ci.cancel();
        }
    }
}

