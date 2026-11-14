/*
 * Decompiled with CFR 0.152.
 */
package net.shoresmp.fabric;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import net.minecraft.class_156;
import net.minecraft.class_437;
import net.shoresmp.fabric.ShoreDialog;
import net.shoresmp.fabric.TicketsScreen;

public final class ClipSavedScreen
extends ShoreDialog {
    private final Path clip;
    private final double seconds;

    public ClipSavedScreen(class_437 parent, Path clip, double seconds) {
        super("Gameplay clip saved", parent);
        this.clip = clip;
        this.seconds = seconds;
    }

    @Override
    protected String icon() {
        return "record";
    }

    @Override
    protected String subtitle() {
        return String.format(Locale.ROOT, "%.1f seconds saved to this computer", this.seconds);
    }

    @Override
    protected List<ShoreDialog.Line> lines() {
        return List.of(new ShoreDialog.Line("Think your punishment was a mistake?", -723465, true), new ShoreDialog.Line("Open a ticket and attach this clip as evidence.", -6577750, false));
    }

    @Override
    protected List<ShoreDialog.Button> buttons() {
        return List.of(new ShoreDialog.Button("Close", 0, this::method_25419), new ShoreDialog.Button("Show clip", 0, () -> class_156.method_668().method_60932(this.clip.getParent())), new ShoreDialog.Button("Open ticket", 1, TicketsScreen::open));
    }
}

