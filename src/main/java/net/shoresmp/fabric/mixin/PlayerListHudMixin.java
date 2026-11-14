/*
 * Decompiled with CFR 0.152.
 */
package net.shoresmp.fabric.mixin;

import net.minecraft.class_2561;
import net.minecraft.class_355;
import net.minecraft.class_640;
import net.shoresmp.fabric.ShoreBadge;
import net.shoresmp.fabric.ShoreClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={class_355.class})
public abstract class PlayerListHudMixin {
    @Inject(method={"method_1918"}, at={@At(value="RETURN")}, cancellable=true)
    private void shore$tabBadge(class_640 entry, CallbackInfoReturnable<class_2561> cir) {
        if (ShoreClient.config != null && ShoreClient.config.badges && ShoreClient.config.badgeTab && cir.getReturnValue() != null && ShoreClient.roster.users().containsKey(entry.method_2966().id())) {
            cir.setReturnValue(ShoreBadge.prependTab(cir.getReturnValue()));
        }
    }
}

