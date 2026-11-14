/*
 * Decompiled with CFR 0.152.
 */
package net.shoresmp.fabric.mixin;

import net.minecraft.class_310;
import net.minecraft.class_7172;
import net.shoresmp.core.SettingValues;
import net.shoresmp.fabric.BrightnessState;
import net.shoresmp.fabric.Config;
import net.shoresmp.fabric.ShoreClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={class_7172.class})
public abstract class BrightnessMixin {
    @Inject(method={"method_41753"}, at={@At(value="RETURN")}, cancellable=true)
    private void shore$brightness(CallbackInfoReturnable<Object> cir) {
        Config config = ShoreClient.config;
        if (config == null || !config.fullbright || config.fullbrightStrength == 0 || BrightnessState.saving()) {
            return;
        }
        class_310 client = class_310.method_1551();
        if (client == null || client.field_1690 == null || this != client.field_1690.method_42473()) {
            return;
        }
        Object object = cir.getReturnValue();
        if (object instanceof Double) {
            Double vanilla = (Double)object;
            cir.setReturnValue((Object)SettingValues.brightness((double)vanilla, (int)config.fullbrightStrength));
        }
    }
}

