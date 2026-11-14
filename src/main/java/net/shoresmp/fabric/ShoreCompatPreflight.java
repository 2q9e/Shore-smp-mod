/*
 * Decompiled with CFR 0.152.
 */
package net.shoresmp.fabric;

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public final class ShoreCompatPreflight
implements PreLaunchEntrypoint {
    public void onPreLaunch() {
        if (!Boolean.getBoolean("shoremod.compatSmoke") && !Boolean.getBoolean("shoremod.compatPreflight")) {
            return;
        }
        int failures = 0;
        for (String name : new String[]{"net.minecraft.block.AbstractBlock$AbstractBlockState", "net.minecraft.client.Mouse", "net.minecraft.client.font.GlyphBaker", "net.minecraft.client.gui.hud.ChatHud", "net.minecraft.client.gui.hud.PlayerListHud", "net.minecraft.client.gui.screen.ingame.HandledScreen", "net.minecraft.client.network.ClientCommonNetworkHandler", "net.minecraft.client.network.ClientPlayNetworkHandler", "net.minecraft.client.option.GameOptions", "net.minecraft.client.option.SimpleOption", "net.minecraft.client.render.GameRenderer", "net.minecraft.client.render.entity.PlayerEntityRenderer", "net.minecraft.client.texture.AbstractTexture"}) {
            try {
                Class.forName(name, false, Thread.currentThread().getContextClassLoader());
                System.out.println("SHORE_HOOK_PASS " + name);
            }
            catch (Throwable error) {
                ++failures;
                System.err.println("SHORE_HOOK_FAIL " + name);
                error.printStackTrace();
            }
        }
        if (failures != 0) {
            System.err.println("SHORE_COMPAT_MIXINS_FAIL count=" + failures);
            System.exit(1);
        }
        System.out.println("SHORE_COMPAT_MIXINS_PASS");
        if (Boolean.getBoolean("shoremod.compatPreflight")) {
            System.exit(0);
        }
    }
}

