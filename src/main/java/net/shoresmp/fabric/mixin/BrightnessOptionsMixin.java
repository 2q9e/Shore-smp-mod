/*
 * Decompiled with CFR 0.152.
 */
package net.shoresmp.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.class_315;
import net.shoresmp.fabric.BrightnessState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value={class_315.class})
public abstract class BrightnessOptionsMixin {
    @WrapMethod(method={"method_1640"})
    private void shore$preserveSavedBrightness(Operation<Void> original) {
        BrightnessState.withoutOverride(() -> original.call(new Object[0]));
    }
}

