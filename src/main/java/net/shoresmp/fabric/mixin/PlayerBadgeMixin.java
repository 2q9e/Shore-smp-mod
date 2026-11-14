/*
 * Decompiled with CFR 0.152.
 */
package net.shoresmp.fabric.mixin;

import net.minecraft.class_10055;
import net.minecraft.class_1007;
import net.minecraft.class_11890;
import net.shoresmp.fabric.ShoreBadge;
import net.shoresmp.fabric.ShoreClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_1007.class})
public abstract class PlayerBadgeMixin {
    @Inject(method={"method_62604(Lnet/minecraft/class_11890;Lnet/minecraft/class_10055;F)V"}, at={@At(value="TAIL")})
    private void shore$badge(class_11890 player, class_10055 state, float delta, CallbackInfo ci) {
        if (!(ShoreClient.config.badges && ShoreClient.config.badgeNametags && state.field_53337 != null && ShoreClient.roster.users().containsKey(player.method_5667()))) {
            return;
        }
        state.field_53337 = ShoreBadge.prepend(state.field_53337);
    }
}

