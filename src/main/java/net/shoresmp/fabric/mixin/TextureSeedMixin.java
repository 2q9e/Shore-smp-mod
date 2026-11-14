/*
 * Decompiled with CFR 0.152.
 */
package net.shoresmp.fabric.mixin;

import net.minecraft.class_2338;
import net.minecraft.class_4970;
import net.shoresmp.fabric.AntiLeakTextures;
import net.shoresmp.fabric.ShoreClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={class_4970.class_4971.class})
public abstract class TextureSeedMixin {
    @Inject(method={"method_26190"}, at={@At(value="RETURN")}, cancellable=true)
    private void shore$privateTextureSeed(class_2338 pos, CallbackInfoReturnable<Long> ci) {
        if (ShoreClient.config != null && ShoreClient.config.antiLeakTextures) {
            ci.setReturnValue((Object)AntiLeakTextures.seed((Long)ci.getReturnValue()));
        }
    }
}

