/*
 * Decompiled with CFR 0.152.
 */
package net.shoresmp.fabric;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.List;
import net.minecraft.class_310;
import net.shoresmp.fabric.Config;
import net.shoresmp.fabric.ShoreClient;
import net.shoresmp.fabric.ShoreScreen;

public final class ShoreSettingsChecks {
    private ShoreSettingsChecks() {
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError((Object)message);
        }
    }

    private static ShoreScreen.Row row(ShoreScreen screen, String title) throws ReflectiveOperationException {
        Field field = ShoreScreen.class.getDeclaredField("rows");
        field.setAccessible(true);
        return ((List)field.get((Object)screen)).stream().filter(r -> r.title.equals(title)).findFirst().orElseThrow();
    }

    public static void run() {
        Config c = ShoreClient.config;
        boolean enabled = c.fullbright;
        int strength = c.fullbrightStrength;
        int size = c.hudSize("fps");
        int color = c.crosshairCustomColor;
        int mode = c.crosshairColor;
        try {
            c.fullbright = false;
            double vanilla = (Double)class_310.method_1551().field_1690.method_42473().method_41753();
            ShoreScreen screen = new ShoreScreen(null);
            screen.showModule("fullbright");
            ShoreScreen.Input numeric = ShoreSettingsChecks.row((ShoreScreen)screen, (String)"Brightness boost").input;
            numeric.set("37", true);
            ShoreSettingsChecks.check(c.fullbrightStrength == 37, "custom brightness is applied");
            numeric.set("101", true);
            ShoreSettingsChecks.check(c.fullbrightStrength == 37 && !numeric.validation.apply(numeric.text).isEmpty(), "over-limit input is rejected");
            numeric.set("NaN", true);
            ShoreSettingsChecks.check(c.fullbrightStrength == 37, "non-numeric input is rejected");
            c.fullbright = true;
            double bright = (Double)class_310.method_1551().field_1690.method_42473().method_41753();
            ShoreSettingsChecks.check(bright > vanilla && bright <= 16.0, "fullbright reaches the real game gamma getter");
            class_310.method_1551().field_1690.method_1640();
            String saved = Files.readString(class_310.method_1551().field_1697.toPath().resolve("options.txt"));
            double stored = saved.lines().filter(line -> line.startsWith("gamma:")).mapToDouble(line -> Double.parseDouble(line.substring(6))).findFirst().orElseThrow();
            ShoreSettingsChecks.check(stored == vanilla, "saving game options cannot persist boosted gamma");
            ShoreSettingsChecks.check((Double)class_310.method_1551().field_1690.method_42473().method_41753() == bright, "fullbright resumes after options save");
            c.fullbright = false;
            ShoreSettingsChecks.check((Double)class_310.method_1551().field_1690.method_42473().method_41753() == vanilla, "fullbright restores the unchanged vanilla value");
            screen.showModule("fps");
            ShoreSettingsChecks.row((ShoreScreen)screen, (String)"Element size").input.set("173", true);
            ShoreSettingsChecks.check(c.hudSize("fps") == 173, "typed values do not snap to slider presets");
            screen.showModule("crosshair");
            ShoreScreen.Input rgb = ShoreSettingsChecks.row((ShoreScreen)screen, (String)"Custom crosshair color").input;
            rgb.set("#12AB34", true);
            ShoreSettingsChecks.check(c.crosshairCustomColor == 1223476 && c.crosshairColor == 6, "custom hex color is applied");
            rgb.set("#GGGGGG", true);
            ShoreSettingsChecks.check(c.crosshairCustomColor == 1223476, "invalid hex cannot overwrite a color");
            ShoreClient.LOG.info("SHORE_CUSTOM_SETTINGS_PASS");
        }
        catch (IOException | ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
        finally {
            c.fullbright = enabled;
            c.fullbrightStrength = strength;
            c.hudSizes.put("fps", size);
            c.crosshairCustomColor = color;
            c.crosshairColor = mode;
            c.save();
        }
    }
}

