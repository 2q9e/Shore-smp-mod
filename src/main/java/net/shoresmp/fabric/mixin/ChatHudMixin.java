/*
 * Decompiled with CFR 0.152.
 */
package net.shoresmp.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.class_1109;
import net.minecraft.class_1113;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_303;
import net.minecraft.class_310;
import net.minecraft.class_338;
import net.minecraft.class_3417;
import net.minecraft.class_5250;
import net.minecraft.class_6880;
import net.minecraft.class_746;
import net.minecraft.class_7469;
import net.minecraft.class_7591;
import net.shoresmp.fabric.Config;
import net.shoresmp.fabric.ShoreClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_338.class})
public abstract class ChatHudMixin {
    @Shadow
    @Final
    private List<class_303> field_2061;
    @Shadow
    private int field_2066;
    @Shadow
    @Final
    private List<class_303.class_7590> field_2064;
    @Unique
    private final Map<class_303, String> shore$keys = new WeakHashMap<class_303, String>();
    @Unique
    private final Map<class_303, Integer> shore$counts = new WeakHashMap<class_303, Integer>();
    @Unique
    private String shore$pendingKey;
    @Unique
    private boolean shore$pendingMention;
    @Unique
    private static final Set<class_303> SHORE_MENTIONS = Collections.newSetFromMap(new WeakHashMap());
    @Unique
    private static final Set<class_303.class_7590> SHORE_MENTION_LINES = Collections.newSetFromMap(new WeakHashMap());
    @Unique
    private static final int[] SHORE_MENTION_COLORS = new int[]{16106818, -1, 0xFF5C5C, 6084188, 6069759};
    @Unique
    private int shore$count = 1;

    @ModifyConstant(method={"method_58744(Lnet/minecraft/class_303;)V", "method_1815"}, constant={@Constant(intValue=100)})
    private int shore$historyLimit(int vanilla) {
        if (ShoreClient.config == null) {
            return 5000;
        }
        return ShoreClient.config.extendedChat ? ShoreClient.config.chatHistoryLimit : vanilla;
    }

    @Unique
    private static boolean shore$mentions(String plain) {
        Config config = ShoreClient.config;
        class_746 player = class_310.method_1551().field_1724;
        ArrayList<String> words = new ArrayList<String>();
        if (player != null) {
            words.add(player.method_5477().getString());
        }
        for (String w : config.highlightWords) {
            if (w == null || w.isBlank()) continue;
            words.add(w.strip());
        }
        String self = player == null ? null : player.method_5477().getString();
        for (String w : words) {
            Matcher m = Pattern.compile("(?<![A-Za-z0-9_])" + Pattern.quote(w) + "(?![A-Za-z0-9_])", 2).matcher(plain);
            while (m.find()) {
                boolean subject;
                String after = plain.substring(m.end()).stripLeading();
                String before = plain.substring(0, m.start());
                boolean own = w.equalsIgnoreCase(self);
                boolean sender = own && m.start() <= 24 && !after.isEmpty() && ":>]\u00bb".indexOf(after.charAt(0)) >= 0;
                boolean bl = subject = own && before.replaceAll("\\[[^\\]]*\\]|[^A-Za-z0-9_]", "").isEmpty();
                if (sender || subject) continue;
                return true;
            }
        }
        return false;
    }

    @Inject(method={"method_1815"}, at={@At(value="TAIL")})
    private void shore$markMention(class_303 line, CallbackInfo ci) {
        if (this.shore$pendingMention) {
            SHORE_MENTIONS.add(line);
            this.shore$pendingMention = false;
        }
        if (!SHORE_MENTIONS.contains(line) || this.field_2064.isEmpty()) {
            return;
        }
        for (int i = 0; i < this.field_2064.size(); ++i) {
            SHORE_MENTION_LINES.add(this.field_2064.get(i));
            if (i + 1 >= this.field_2064.size() || this.field_2064.get(i + 1).comp_898()) break;
        }
    }

    @WrapOperation(method={"method_75802"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_338$class_12233;method_75809(IIIII)V")})
    private static void shore$highlight(class_338.class_12233 backend, int x1, int y1, int x2, int y2, int color, Operation<Void> original, @Local(argsOnly=true) class_303.class_7590 line, @Local(argsOnly=true, ordinal=1) float fade) {
        Config config = ShoreClient.config;
        if (config == null || !config.mentionHighlight || !SHORE_MENTION_LINES.contains(line)) {
            original.call(new Object[]{backend, x1, y1, x2, y2, color});
            return;
        }
        int rgb = config.mentionColor == 5 ? config.mentionCustomColor : (config.mentionColor == 1 ? config.accent : SHORE_MENTION_COLORS[config.mentionColor]);
        int alpha = Math.round(Math.clamp(fade, 0.0f, 1.0f) * 112.0f);
        original.call(new Object[]{backend, x1, y1, x2, y2, alpha << 24 | rgb});
        original.call(new Object[]{backend, x1, y1, x1 + 2, y2, Math.round(Math.clamp(fade, 0.0f, 1.0f) * 255.0f) << 24 | rgb});
    }

    @Inject(method={"method_44811(Lnet/minecraft/class_2561;Lnet/minecraft/class_7469;Lnet/minecraft/class_7591;)V"}, at={@At(value="HEAD")}, order=900)
    private void shore$duplicates(class_2561 message, class_7469 signature, class_7591 indicator, CallbackInfo ci) {
        String key;
        this.shore$count = 1;
        this.shore$pendingKey = null;
        Config config = ShoreClient.config;
        if (config == null) {
            return;
        }
        this.shore$pendingKey = key = message.getString().strip();
        boolean bl = this.shore$pendingMention = config.mentionHighlight && ChatHudMixin.shore$mentions(key);
        if (!config.filterDuplicates || key.isEmpty()) {
            return;
        }
        int group = 0;
        for (int i = 0; i < Math.min(config.stackLookback, this.field_2061.size()); ++i) {
            class_303 line = this.field_2061.get(i);
            int len = 0;
            while (group + len < this.field_2064.size() && group + ++len < this.field_2064.size() && !this.field_2064.get(group + len).comp_898()) {
            }
            if (key.equals(this.shore$keys.get(line)) && Objects.equals(indicator, line.comp_894())) {
                this.shore$count = Math.min(999999, this.shore$counts.getOrDefault(line, 1) + 1);
                this.field_2061.remove(i);
                for (int k = 0; k < len && group < this.field_2064.size(); ++k) {
                    this.field_2064.remove(group);
                }
                if (this.field_2066 > 0) {
                    this.field_2066 = Math.max(0, this.field_2066 - len);
                }
                return;
            }
            group += len;
        }
    }

    @Inject(method={"method_44811(Lnet/minecraft/class_2561;Lnet/minecraft/class_7469;Lnet/minecraft/class_7591;)V"}, at={@At(value="TAIL")})
    private void shore$rememberLine(class_2561 message, class_7469 signature, class_7591 indicator, CallbackInfo ci) {
        if (this.field_2061.isEmpty() || this.shore$pendingKey == null) {
            return;
        }
        class_303 line = this.field_2061.getFirst();
        this.shore$keys.put(line, this.shore$pendingKey);
        this.shore$counts.put(line, this.shore$count);
    }

    @ModifyVariable(method={"method_44811(Lnet/minecraft/class_2561;Lnet/minecraft/class_7469;Lnet/minecraft/class_7591;)V"}, at=@At(value="HEAD"), argsOnly=true, ordinal=0, order=1100)
    private class_2561 shore$decorate(class_2561 message) {
        boolean highlight;
        if (ShoreClient.config == null) {
            return message;
        }
        Config config = ShoreClient.config;
        class_5250 result = class_2561.method_43473();
        String plain = message.getString().toLowerCase(Locale.ROOT);
        if (config.timestamps) {
            String pattern = (config.timestamps24 ? "HH:mm" : "h:mm") + (config.timestampSeconds ? ":ss" : "") + (config.timestamps24 ? "" : " a");
            result.method_10852((class_2561)class_2561.method_43470((String)("[" + LocalTime.now().format(DateTimeFormatter.ofPattern(pattern, Locale.ROOT)) + "] ")).method_54663(8559278));
        }
        if ((highlight = this.shore$pendingMention) && config.mentionSound && this.shore$count == 1) {
            class_310.method_1551().method_1483().method_4873((class_1113)class_1109.method_47978((class_6880)class_3417.field_14622, (float)1.6f));
        }
        result.method_10852(message);
        if (config.filterDuplicates && this.shore$count > 1) {
            result.method_10852((class_2561)class_2561.method_43470((String)(switch (config.stackStyle) {
                case 1 -> " (" + this.shore$count + ")";
                case 2 -> " x" + this.shore$count;
                default -> " [x" + this.shore$count + "]";
            })).method_10862(class_2583.field_24360.method_36139(0xAAAAAA)));
        }
        return result;
    }
}

