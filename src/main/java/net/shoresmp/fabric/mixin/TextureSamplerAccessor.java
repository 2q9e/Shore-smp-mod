/*
 * Decompiled with CFR 0.152.
 */
package net.shoresmp.fabric.mixin;

import net.minecraft.class_1044;
import net.minecraft.class_12137;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={class_1044.class})
public interface TextureSamplerAccessor {
    @Accessor(value="field_63613")
    public void shore$setSampler(class_12137 var1);
}

