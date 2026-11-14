/*
 * Decompiled with CFR 0.152.
 */
package net.shoresmp.fabric.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import java.util.List;
import java.util.Set;
import net.minecraft.class_11642;
import net.minecraft.class_12137;
import net.minecraft.class_2960;
import net.minecraft.class_380;
import net.shoresmp.fabric.mixin.TextureSamplerAccessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={class_11642.class})
public abstract class GlyphBakerMixin {
    @Shadow
    @Final
    private class_2960 field_61622;
    @Shadow
    @Final
    private List<class_380> field_61623;
    @Unique
    private static final Set<String> SHORE_UI_FONTS = Set.of("ui", "ui-bold", "ui-small", "heading");

    @Inject(method={"method_72797"}, at={@At(value="RETURN")})
    private void shore$smoothUiAtlas(CallbackInfoReturnable<?> ci) {
        if (!this.field_61622.method_12836().equals("shoremod")) {
            return;
        }
        if (!SHORE_UI_FONTS.contains(this.field_61622.method_12832()) && !this.field_61622.method_12832().startsWith("px_")) {
            return;
        }
        class_12137 linear = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
        for (class_380 atlas : this.field_61623) {
            if (atlas.method_75484() == linear) continue;
            ((TextureSamplerAccessor)atlas).shore$setSampler(linear);
        }
    }
}

