/*
 * Decompiled with CFR 0.152.
 */
package net.shoresmp.fabric;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import net.minecraft.class_1041;
import net.minecraft.class_1109;
import net.minecraft.class_1113;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_156;
import net.minecraft.class_2561;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_315;
import net.minecraft.class_332;
import net.minecraft.class_3417;
import net.minecraft.class_3675;
import net.minecraft.class_437;
import net.minecraft.class_6880;
import net.shoresmp.core.ModUpdates;
import net.shoresmp.core.SettingValues;
import net.shoresmp.fabric.AntiLeakTextures;
import net.shoresmp.fabric.Config;
import net.shoresmp.fabric.ShoreClient;
import net.shoresmp.fabric.ShoreCompatSmoke;
import net.shoresmp.fabric.ShoreHud;
import net.shoresmp.fabric.ShoreTools;
import net.shoresmp.fabric.ShoreUi;
import net.shoresmp.fabric.TicketsScreen;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

public final class ShoreScreen
extends class_437 {
    private static final int TEXT = -723465;
    private static final int MUTED = -6577750;
    private static final int FAINT = -10459537;
    private static final int TOP = -351136745;
    private static final int TOP_HOVER = -182706396;
    private static final int CARD = -15394788;
    private static final int CARD_HOVER = -15131615;
    private static final int EDGE = -14473427;
    private static final int EDGE_HOVER = -13354687;
    private static final int CHIP = -14934236;
    private static final int CHIP_HOVER = -14276048;
    private static final int BTN = -14802393;
    private static final int BTN_HOVER = -14012619;
    private static final int ON = -8600017;
    private static final int ON_HOVER = -7613634;
    private static final int OFF = -14802393;
    private static final int OFF_HOVER = -14210255;
    private static final int ROW_HOVER = -15000029;
    private static final int INPUT = -16052977;
    private static final int INPUT_EDGE = -14012876;
    private static final int DIVIDER = -14802650;
    private static final int TRACK = -13288895;
    private static final int HEART = -41862;
    private static final String[] CATEGORIES = new String[]{"All", "New", "HUD", "PvP", "Chat", "Inventory", "Utility", "Clips"};
    private static final String[] CORNER_NAMES = new String[]{"Top left", "Top right", "Bottom left", "Bottom right"};
    private static final int[] ACCENTS = new int[]{3529682, 5285887, 11174911, 16741789, 16747069, 16044894, 8177199, 0xEEEEEE};
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy/MM/dd", Locale.ROOT);
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("hh:mm a", Locale.ROOT);
    private static final int TOGGLE = 0;
    private static final int CHOICE = 1;
    private static final int ACTION = 2;
    private static final int INFO = 3;
    private static final int FIELD = 4;
    private static final int ACCENT = 5;
    private static final int SLIDER = 6;
    private static final int SEGMENT = 7;
    private static final int KEY = 8;
    private final Map<Row, Runnable> keyClicks = new HashMap<Row, Runnable>();
    private final ShoreUi ui = new ShoreUi();
    private final List<Module> modules = new ArrayList<Module>();
    private final List<Row> rows = new ArrayList<Row>();
    private final List<Hit> hits = new ArrayList<Hit>();
    private final class_437 parent;
    private Page page = Page.GRID;
    private Module current;
    private String category = "All";
    private String pendingSection;
    private String tooltip;
    private boolean favoritesOnly;
    private final Input search = new Input("Search", "", 40, v -> {
        this.scrollTarget = 0.0f;
        this.scroll = 0.0f;
    });
    private Input hexInput;
    private Input focused;
    private float mx;
    private float my;
    private float scroll;
    private float scrollTarget;
    private float maxScroll;
    private float frameDt;
    private float[] clip;
    private long lastFrame;
    private long resetArmedUntil;
    private long caretEpoch;
    private Object shownUpdate;
    private Row listening;
    private static ShoreScreen registry;
    private Row activeSlider;
    private float pressX;
    private static final float FS = 12.5f;
    private static final float ROW_H = 34.0f;
    private final Set<String> collapsed = new HashSet<String>();
    private static final Map<String, String[]> RESET_FIELDS;
    private static final Set<String> HUD_IDS;
    private final Map<String, Float> switchAnim = new HashMap<String, Float>();

    private Runnable keyClick(Row r) {
        return this.keyClicks.computeIfAbsent(r, x -> () -> {
            this.listening = x;
        });
    }

    public ShoreScreen(class_437 parent) {
        super((class_2561)class_2561.method_43470((String)"ShoreMod"));
        this.parent = parent;
        this.defineModules();
    }

    private static int accent() {
        return 0xFF000000 | ShoreClient.config.accent;
    }

    private static boolean light(int c) {
        return ((c >> 16 & 0xFF) * 299 + (c >> 8 & 0xFF) * 587 + (c & 0xFF) * 114) / 1000 > 150;
    }

    private static int onAccent() {
        return ShoreScreen.light(ShoreScreen.accent()) ? -15921390 : -1;
    }

    private void defineModules() {
        Config c = ShoreClient.config;
        Function<String, Consumer<ShoreScreen>> hudSettings = id -> s -> {
            s.section("Size");
            s.slider("Element size", "Only this HUD element", 50, 250, 5, "%", () -> c.hudSize((String)id), v -> c.hudSizes.put((String)id, v));
            s.section("All HUD elements");
            s.slider("Overall size", "Scales every HUD element together", 50, 250, 5, "%", () -> c.hudScale, v -> {
                c.hudScale = v;
            });
            s.choice("Position", "Corner of the screen the HUD sits in", () -> CORNER_NAMES[c.hudCorner], () -> {
                c.hudCorner = (c.hudCorner + 1) % 4;
                c.save();
            });
            s.section("Style");
            s.slider("Background opacity", "Darkness of the box behind each element", 0, 100, 5, "%", () -> c.hudBgOpacity, v -> {
                c.hudBgOpacity = v;
            });
            s.segment("Text color", "", new String[]{"White", "Accent", "Gray", "Custom"}, () -> c.hudTextColor, v -> {
                c.hudTextColor = v;
            });
            s.color("Custom text color", "Type a hex color; applies to the HUD", () -> c.hudCustomColor, v -> {
                c.hudCustomColor = v;
                c.hudTextColor = 3;
            });
            s.toggle("Text shadow", "Minecraft's drop shadow behind the text", () -> c.hudShadow, v -> {
                c.hudShadow = v;
            });
        };
        this.mod("fps", "FPS", "Your frames per second.", "HUD", "gauge", true, () -> c.hudFps, v -> {
            c.hudFps = v;
        }, hudSettings.apply("fps"));
        this.mod("ping", "Ping", "Your latency to the server in milliseconds.", "HUD", "signal", true, () -> c.hudPing, v -> {
            c.hudPing = v;
        }, hudSettings.apply("ping"));
        this.mod("coords", "Coordinates", "Your position and the direction you are facing.", "HUD", "pin", true, () -> c.hudCoords, v -> {
            c.hudCoords = v;
        }, hudSettings.apply("coords"));
        this.mod("biome", "Biome", "The biome you are standing in.", "HUD", "tree", true, () -> c.hudBiome, v -> {
            c.hudBiome = v;
        }, hudSettings.apply("biome"));
        this.mod("clock", "Clock", "Your local time on screen.", "HUD", "clock", true, () -> c.hudClock, v -> {
            c.hudClock = v;
        }, hudSettings.apply("clock"));
        this.mod("armor", "Armor Status", "Durability of everything you wear and hold.", "HUD", "armor", true, () -> c.hudArmor, v -> {
            c.hudArmor = v;
        }, hudSettings.apply("armor"));
        this.mod("stacking", "Message Stacking", "Merge repeated chat messages into one line with a count.", "Chat", "stack", false, () -> c.filterDuplicates, v -> {
            c.filterDuplicates = v;
        }, s -> {
            s.section("Counter");
            s.segment("Counter style", "How the repeat count is shown", new String[]{"[x2]", "(2)", "x2"}, () -> c.stackStyle, v -> {
                c.stackStyle = v;
            });
            s.slider("Look back", "Merge with a repeat this many messages back", 1, 20, 1, "", () -> c.stackLookback, v -> {
                c.stackLookback = v;
            });
        });
        this.mod("timestamps", "Timestamps", "Show the time beside incoming messages.", "Chat", "history", false, () -> c.timestamps, v -> {
            c.timestamps = v;
        }, s -> {
            s.section("Format");
            s.segment("Clock", "", new String[]{"24-hour", "12-hour"}, () -> c.timestamps24 ? 0 : 1, v -> {
                c.timestamps24 = v == 0;
            });
            s.toggle("Show seconds", "Add seconds to each timestamp", () -> c.timestampSeconds, v -> {
                c.timestampSeconds = v;
            });
        });
        this.mod("mentions", "Mentions", "Highlight chat lines that mention your name or words you choose.", "Chat", "bell", false, () -> c.mentionHighlight, v -> {
            c.mentionHighlight = v;
        }, s -> {
            s.section("Alerts");
            s.segment("Highlight color", "", new String[]{"Yellow", "Accent", "Red", "Green", "Blue", "Custom"}, () -> c.mentionColor, v -> {
                c.mentionColor = v;
            });
            s.color("Custom highlight color", "Type a hex color for your mention alerts", () -> c.mentionCustomColor, v -> {
                c.mentionCustomColor = v;
                c.mentionColor = 5;
            });
            s.toggle("Mention sound", "Play a soft ping when you are mentioned", () -> c.mentionSound, v -> {
                c.mentionSound = v;
            });
            s.section("Words");
            s.field("Highlight words", "Separate words with commas", "e.g. diamonds, base, trade", String.join((CharSequence)", ", c.highlightWords), v -> {
                c.highlightWords = ShoreScreen.parse(v);
                c.save();
            });
        });
        this.mod("chat", "Chat Tweaks", "Longer chat history and an adjustable background.", "Chat", "chat", false, () -> c.extendedChat, v -> {
            c.extendedChat = v;
        }, s -> {
            s.section("History");
            s.slider("History length", "Messages kept when you scroll back", 100, 20000, 100, "", () -> c.chatHistoryLimit, v -> {
                c.chatHistoryLimit = v;
            });
            s.section("Look");
            s.slider("Background", "Darkness of the backdrop behind chat", 0, 100, 1, "%", () -> (int)Math.round(c.chatOpacity * 100.0), v -> {
                c.chatOpacity = (double)v / 100.0;
            });
        });
        this.mod("mousetweaks", "Mouse Tweaks", "Drag to spread, collect and move items. Scroll to move one at a time.", "Inventory", "mouse", false, () -> c.mouseTweaks, v -> {
            c.mouseTweaks = v;
        }, s -> {
            s.section("Tweaks");
            s.toggle("Right-click drag", "Hold right-click with a stack and sweep to drop one item per slot", () -> c.tweakRmb, v -> {
                c.tweakRmb = v;
            });
            s.toggle("Left-click collect", "Hold left-click on a matching stack and sweep to gather more", () -> c.tweakLmb, v -> {
                c.tweakLmb = v;
            });
            s.toggle("Shift-drag to move", "Hold shift + left-click and sweep to move every stack you touch", () -> c.tweakShift, v -> {
                c.tweakShift = v;
            });
            s.toggle("Scroll wheel", "Scroll down on an item to send one, up to pull one back", () -> c.tweakWheel, v -> {
                c.tweakWheel = v;
            });
        });
        this.mod("shulker", "Shulker Preview", "Hold Shift while hovering a shulker box to see inside.", "Inventory", "box", false, () -> c.shulkerPreview, v -> {
            c.shulkerPreview = v;
        }, s -> {
            s.section("Preview");
            s.segment("Show contents", "When hovering a shulker box", new String[]{"Hold Shift", "Always"}, () -> c.shulkerMode, v -> {
                c.shulkerMode = v;
            });
            s.slider("Size", "Scale of the preview", 50, 200, 5, "%", () -> c.shulkerScale, v -> {
                c.shulkerScale = v;
            });
            s.section("Look");
            s.segment("Border", "", new String[]{"Dye color", "Accent", "None"}, () -> c.shulkerBorderMode, v -> {
                c.shulkerBorderMode = v;
            });
            s.slider("Background opacity", "Darkness behind the items", 0, 100, 5, "%", () -> c.shulkerBgOpacity, v -> {
                c.shulkerBgOpacity = v;
            });
            s.toggle("Show name", "The shulker box's name above the items", () -> c.shulkerTitle, v -> {
                c.shulkerTitle = v;
            });
            s.toggle("Show empty slots", "Draw a faint square for every slot", () -> c.shulkerEmptySlots, v -> {
                c.shulkerEmptySlots = v;
            });
        });
        this.mod("antileak", "Anti-Leak", "Scramble texture rotation patterns used to locate bases.", "Utility", "cube", false, () -> c.antiLeakTextures, v -> {
            c.antiLeakTextures = v;
            if (v.booleanValue()) {
                AntiLeakTextures.reshuffle();
            }
            ShoreClient.recorder.clear();
            if (v.booleanValue()) {
                ShoreClient.tell("Texture-pattern scrambling enabled. Terrain, structures and bedrock placement can still reveal your location.");
            }
            class_310 mc = class_310.method_1551();
            if (mc.field_1687 != null) {
                mc.field_1769.method_3279();
            }
        }, s -> {
            s.section("Good to know");
            s.action("New texture pattern", "Generate a different pattern right now", "Shuffle", () -> {
                AntiLeakTextures.reshuffle();
                class_310 mc = class_310.method_1551();
                if (c.antiLeakTextures && mc.field_1687 != null) {
                    mc.field_1769.method_3279();
                }
            });
        });
        this.mod("fullbright", "Fullbright", "Brighten caves and nights without changing your saved game brightness.", "Utility", "gauge", true, () -> c.fullbright, v -> {
            c.fullbright = v;
        }, s -> {
            s.section("Brightness");
            s.slider("Brightness boost", "0 keeps normal lighting; 100 gives the strongest boost", 0, 100, 1, "%", () -> c.fullbrightStrength, v -> {
                c.fullbrightStrength = v;
            });
        });
        this.mod("zoom", "Zoom", "Zoom in and out of objects. Scroll to zoom further.", "Utility", "zoom", true, () -> c.zoomEnabled, v -> {
            c.zoomEnabled = v;
        }, s -> {
            s.section("General");
            s.segment("Zoom mode", "Hold the key, or press once to zoom and again to stop", new String[]{"Hold", "Toggle"}, () -> c.zoomToggle ? 1 : 0, v -> {
                c.zoomToggle = v == 1;
            });
            s.keybind("Zoom key", "Change it in Controls", ShoreTools.zoomKey);
            s.slider("Start zoom level", "How far a press zooms in", 2, 20, 1, "x", () -> c.zoomLevel, v -> {
                c.zoomLevel = v;
            });
            s.section("Sensitivity");
            Row mode = s.segment("Sensitivity mode", "Dynamic slows the mouse more the further you zoom", new String[]{"Static", "Dynamic"}, () -> c.zoomSensMode, v -> {
                c.zoomSensMode = v;
            });
            s.slider("Static sensitivity", "Mouse speed while zoomed, in Static mode", 5, 100, 5, "%", () -> c.zoomSensitivity, v -> {
                c.zoomSensitivity = v;
            }).available = () -> c.zoomSensMode == 0;
            s.toggle("Smooth movement", "Cinematic camera while zoomed", () -> c.zoomCinematic, v -> {
                c.zoomCinematic = v;
            });
            s.section("Scroll");
            Row scroll = s.toggle("Scroll zoom", "Use the mouse wheel while zooming", () -> c.zoomScroll, v -> {
                c.zoomScroll = v;
            });
            s.slider("Scroll sensitivity", "How much each wheel step zooms", 5, 60, 5, "%", () -> c.zoomScrollSpeed, v -> {
                c.zoomScrollSpeed = v;
            }).dependsOn(scroll);
            s.section("Smooth");
            s.toggle("Smooth zoom", "Ease in and out instead of snapping", () -> c.zoomSmooth, v -> {
                c.zoomSmooth = v;
            });
        });
        this.mod("cps", "CPS", "Clicks per second, shown in the HUD.", "PvP", "click", true, () -> c.cps, v -> {
            c.cps = v;
        }, s -> {
            s.section("Display");
            s.toggle("Show right clicks", "Display left | right clicks per second", () -> c.cpsRight, v -> {
                c.cpsRight = v;
            });
            ((Consumer)hudSettings.apply("cps")).accept(s);
        });
        this.mod("togglesprint", "Toggle Sprint", "Press sprint or sneak once instead of holding it.", "Utility", "sprint", true, () -> c.toggleSprint, v -> {
            c.toggleSprint = v;
            class_315 o = class_310.method_1551().field_1690;
            o.method_42450().method_41748(v);
            o.method_1640();
        }, s -> {
            s.section("Toggle");
            s.toggle("Toggle sneak", "Press sneak once to keep sneaking", () -> c.toggleSneak, v -> {
                c.toggleSneak = v;
                class_315 o = class_310.method_1551().field_1690;
                o.method_42449().method_41748(v);
                o.method_1640();
            });
            s.toggle("Show in HUD", "Shows \"Sprinting (Toggled)\" while active", () -> c.toggleSprintHud, v -> {
                c.toggleSprintHud = v;
            });
            ((Consumer)hudSettings.apply("togglesprint")).accept(s);
        });
        this.mod("crosshair", "Custom Crosshair", "Your own crosshair: a shape you design or a picture you choose.", "Utility", "crosshair", true, () -> c.crosshair, v -> {
            c.crosshair = v;
        }, s -> {
            s.section("Style");
            s.segment("Type", "", new String[]{"Shape", "Image"}, () -> c.crosshairMode, v -> {
                c.crosshairMode = v;
                c.save();
                s.build();
            });
            s.slider("Opacity", "How see-through the crosshair is", 10, 100, 5, "%", () -> c.crosshairOpacity, v -> {
                c.crosshairOpacity = v;
            });
            if (c.crosshairMode == 1) {
                s.section("Image");
                s.info("Picture", c.crosshairImage.isBlank() ? "PNG, up to 512x512. Transparent areas stay clear." : "Saved in config/shoremod/crosshairs", () -> c.crosshairImage.isBlank() ? "None chosen" : Path.of(c.crosshairImage, new String[0]).getFileName().toString());
                s.action("Choose image", "Pick a PNG from your computer", "Browse", s::chooseCrosshairImage);
                s.slider("Image size", "Width and height on screen", 4, 64, 1, " px", () -> c.crosshairImageSize, v -> {
                    c.crosshairImageSize = v;
                });
                s.toggle("Tint with color", "Color the picture with the color below", () -> c.crosshairTint, v -> {
                    c.crosshairTint = v;
                });
                s.action("Crosshairs folder", "Open the folder your pictures are copied to", "Open", () -> {
                    try {
                        Path dir = ShoreHud.CrosshairImage.folder();
                        Files.createDirectories(dir, new FileAttribute[0]);
                        class_156.method_668().method_60932(dir);
                    }
                    catch (Exception e) {
                        ShoreClient.tellError(e.getMessage());
                    }
                });
            } else {
                s.section("Shape");
                s.slider("Length", "Length of each arm", 1, 15, 1, " px", () -> c.crosshairLength, v -> {
                    c.crosshairLength = v;
                });
                s.slider("Gap", "Space between the arms and the center", 0, 10, 1, " px", () -> c.crosshairGap, v -> {
                    c.crosshairGap = v;
                });
                s.slider("Thickness", "Width of each arm", 1, 4, 1, " px", () -> c.crosshairThickness, v -> {
                    c.crosshairThickness = v;
                });
                s.toggle("Center dot", "A dot in the middle", () -> c.crosshairDot, v -> {
                    c.crosshairDot = v;
                });
                s.toggle("Outline", "A dark edge so it shows on bright blocks", () -> c.crosshairOutline, v -> {
                    c.crosshairOutline = v;
                });
            }
            s.section("Color");
            s.segment("Color", "", ShoreHud.CROSSHAIR_NAMES, () -> c.crosshairColor, v -> {
                c.crosshairColor = v;
            });
            s.color("Custom crosshair color", "Preview your own RGB color while playing", () -> c.crosshairCustomColor, v -> {
                c.crosshairCustomColor = v;
                c.crosshairColor = 6;
            });
        });
        this.mod("durability", "Durability Alert", "A sound and message before your tools or armor break.", "Utility", "pickaxe", true, () -> c.durabilityAlert, v -> {
            c.durabilityAlert = v;
        }, s -> {
            s.section("Alert");
            s.slider("Warn at", "Remaining durability that triggers the alert", 1, 50, 1, "%", () -> c.durabilityThreshold, v -> {
                c.durabilityThreshold = v;
            });
            s.toggle("Sound", "Play a warning sound", () -> c.durabilitySound, v -> {
                c.durabilitySound = v;
            });
            s.segment("Show in", "", new String[]{"Action bar", "Chat"}, () -> c.durabilityChat ? 1 : 0, v -> {
                c.durabilityChat = v == 1;
            });
        });
        this.mod("deaths", "Death Log", "Remember where you died. Type /shore deaths to list them.", "Utility", "skull", true, () -> c.deathLog, v -> {
            c.deathLog = v;
        }, s -> {
            s.section("Messages");
            s.toggle("Announce in chat", "Tell you where you died right after it happens", () -> c.deathAnnounce, v -> {
                c.deathAnnounce = v;
            });
            s.section("History");
            s.info("Last death", c.deaths.isEmpty() ? "Nothing recorded yet" : c.deaths.getFirst().split("  \u00b7  ")[0], () -> {
                if (c.deaths.isEmpty()) {
                    return "None";
                }
                String[] p = c.deaths.getFirst().split("  \u00b7  ");
                return p.length > 2 ? p[2] + "  " + p[1] : c.deaths.getFirst();
            });
            s.info("Saved deaths", "The last 10 are kept", () -> String.valueOf(c.deaths.size()));
            if (!c.deaths.isEmpty()) {
                s.action("Clear history", "Forget every saved death location", "Clear", () -> {
                    c.deaths.clear();
                    c.save();
                    s.build();
                });
            }
        });
        this.mod("reconnect", "Reconnect", "Adds a reconnect button to the disconnect screen.", "Utility", "reconnect", true, () -> c.reconnectButton, v -> {
            c.reconnectButton = v;
        }, s -> {
            s.section("Automatic");
            Row auto = s.toggle("Auto-reconnect", "Skipped after bans or punishment messages", () -> c.autoReconnect, v -> {
                c.autoReconnect = v;
            });
            s.slider("Delay", "Wait before reconnecting", 3, 120, 1, "s", () -> c.autoReconnectDelay, v -> {
                c.autoReconnectDelay = v;
            }).dependsOn(auto);
        });
        this.mod("logos", "Shore Logos", "Show the ShoreSMP mark beside other ShoreMod players.", "Utility", "@logo", false, () -> c.badges, v -> {
            c.badges = v;
        }, s -> {
            s.section("Show on");
            s.toggle("Nametags", "Beside player names in the world", () -> c.badgeNametags, v -> {
                c.badgeNametags = v;
            });
            s.toggle("Tab list", "Beside names in the player list", () -> c.badgeTab, v -> {
                c.badgeTab = v;
            });
        });
        this.mod("clips", "Clip Recorder", "Keep the last 30 seconds so you can save punishments and moments.", "Clips", "record", false, () -> c.recording, v -> {
            c.recording = v;
            ShoreClient.recorder.clear();
        }, s -> {
            s.section("Video");
            s.slider("Frame rate", "Higher frame rates use more memory", 5, 30, 1, " FPS", () -> c.clipFps, v -> {
                c.clipFps = v;
                ShoreClient.recorder.clear();
            });
            s.slider("Resolution", "Video width, bounded to 320\u20131920 pixels", 320, 1920, 20, " px", () -> c.clipWidth, v -> {
                c.clipWidth = v;
                ShoreClient.recorder.clear();
            });
            s.keybind("Save a clip", "Save the last 30 seconds right now", ShoreClient.clipKey);
            s.section("Auto-save");
            s.toggle("Save on every disconnect", "When off, only punishment disconnects are saved", () -> c.saveEveryDisconnect, v -> {
                c.saveEveryDisconnect = v;
            });
            s.field("Punishment phrases", "Messages containing these save a clip. Separate with commas.", "e.g. you have been banned", String.join((CharSequence)", ", c.punishmentPhrases), v -> {
                c.punishmentPhrases = ShoreScreen.parse(v);
                c.save();
            });
            s.section("Storage");
            s.slider("Storage limit", "Maximum clip storage in megabytes", 128, 16384, 128, " MB", () -> c.maxClipStorageMb, v -> {
                c.maxClipStorageMb = v;
            });
            s.action("Clips folder", "Browse the videos saved on this computer", "Open", () -> {
                try {
                    Files.createDirectories(ShoreClient.recorder.directory(), new FileAttribute[0]);
                    class_156.method_668().method_60932(ShoreClient.recorder.directory());
                }
                catch (Exception e) {
                    ShoreClient.tellError(e.getMessage());
                }
            });
        });
        this.mod("keystrokes", "Keystrokes", "Shows WASD, your mouse buttons with CPS, and the space bar.", "PvP", "keyboard", true, () -> c.keystrokes, v -> {
            c.keystrokes = v;
        }, s -> {
            s.section("Keys");
            s.toggle("Mouse buttons", "Left and right click with clicks per second", () -> c.keystrokesMouse, v -> {
                c.keystrokesMouse = v;
            });
            s.toggle("Space bar", "Show the jump key", () -> c.keystrokesSpace, v -> {
                c.keystrokesSpace = v;
            });
            s.segment("Pressed color", "", new String[]{"White", "Accent"}, () -> c.keystrokesAccent ? 1 : 0, v -> {
                c.keystrokesAccent = v == 1;
            });
            ((Consumer)hudSettings.apply("keystrokes")).accept(s);
        });
        this.mod("target", "Target HUD", "Name and health of the player or mob you are fighting.", "PvP", "target", true, () -> c.targetHud, v -> {
            c.targetHud = v;
        }, s -> {
            s.section("Display");
            s.toggle("Show distance", "How far away your target is", () -> c.targetDistance, v -> {
                c.targetDistance = v;
            });
            ((Consumer)hudSettings.apply("target")).accept(s);
        });
        this.mod("potions", "Potion Effects", "Active effects with their level and time left.", "PvP", "potion", true, () -> c.potionHud, v -> {
            c.potionHud = v;
        }, hudSettings.apply("potions"));
        this.mod("items", "Item Counter", "How many totems, pearls, gapples and crystals you are carrying.", "PvP", "items", true, () -> c.itemCounter, v -> {
            c.itemCounter = v;
        }, s -> {
            s.section("Count");
            s.toggle("Totems", "", () -> c.itemsTotem, v -> {
                c.itemsTotem = v;
            });
            s.toggle("Ender pearls", "", () -> c.itemsPearl, v -> {
                c.itemsPearl = v;
            });
            s.toggle("Golden apples", "", () -> c.itemsGapple, v -> {
                c.itemsGapple = v;
            });
            s.toggle("End crystals", "", () -> c.itemsCrystal, v -> {
                c.itemsCrystal = v;
            });
            s.toggle("Arrows", "", () -> c.itemsArrow, v -> {
                c.itemsArrow = v;
            });
            s.toggle("Bottles o' enchanting", "", () -> c.itemsXp, v -> {
                c.itemsXp = v;
            });
            s.toggle("Show when zero", "Keep showing items you have none of", () -> c.itemsShowZero, v -> {
                c.itemsShowZero = v;
            });
            s.section("Your own items");
            s.field("Extra items", "Item ids separated by commas", "e.g. cobweb, obsidian, water_bucket", String.join((CharSequence)", ", c.itemsCustom), v -> {
                c.itemsCustom = ShoreScreen.parse(v);
                c.save();
            });
            ((Consumer)hudSettings.apply("items")).accept(s);
        });
        this.mod("totempops", "Totem Pop Counter", "Tells you in chat when nearby players pop a totem, and how many.", "PvP", "totem", true, () -> c.totemPops, v -> {
            c.totemPops = v;
        }, s -> {
            s.section("Messages");
            s.toggle("Include yourself", "Also count your own totem pops", () -> c.totemPopsSelf, v -> {
                c.totemPopsSelf = v;
            });
        });
        this.modules.sort(Comparator.comparing(Module::name, String.CASE_INSENSITIVE_ORDER));
    }

    private void mod(String id, String name, String desc, String cat, String icon, boolean isNew, BooleanSupplier get, Consumer<Boolean> set, Consumer<ShoreScreen> settings) {
        this.modules.add(new Module(id, name, desc, cat, icon, isNew, get, set, settings));
    }

    private boolean favorite(Module m) {
        return ShoreClient.config.favorites.contains(m.id());
    }

    private void toggleFavorite(Module m) {
        List<String> f = ShoreClient.config.favorites;
        if (!f.remove(m.id())) {
            f.add(m.id());
        }
        ShoreClient.config.save();
    }

    private void toggleModule(Module m) {
        m.set().accept(!m.get().getAsBoolean());
        ShoreClient.config.save();
    }

    private List<Module> visibleModules() {
        String q = this.search.text.strip().toLowerCase(Locale.ROOT);
        return this.modules.stream().filter(m -> this.category.equals("All") || (this.category.equals("New") ? m.isNew() : m.category().equals(this.category))).filter(m -> !this.favoritesOnly || this.favorite((Module)m)).filter(m -> q.isEmpty() || (m.name() + " " + m.desc() + " " + m.category()).toLowerCase(Locale.ROOT).contains(q)).toList();
    }

    void showGrid(String cat, String q) {
        this.page = Page.GRID;
        this.category = cat;
        this.search.set(q, false);
        this.scrollTarget = 0.0f;
        this.scroll = 0.0f;
        this.build();
    }

    void showModule(String id) {
        for (Module m : this.modules) {
            if (!m.id().equals(id)) continue;
            this.openModule(m);
            return;
        }
    }

    void showPage(String name) {
        this.openPage(Page.valueOf(name));
    }

    private void openModule(Module m) {
        this.current = m;
        this.page = Page.MODULE;
        this.scrollTarget = 0.0f;
        this.scroll = 0.0f;
        this.focused = null;
        this.build();
    }

    private void openPage(Page p) {
        this.page = p;
        this.current = null;
        this.scrollTarget = 0.0f;
        this.scroll = 0.0f;
        this.focused = null;
        this.build();
    }

    protected void method_25426() {
        this.build();
    }

    public void method_25393() {
        if (this.page == Page.UPDATES && this.shownUpdate != ShoreClient.latestUpdate) {
            this.shownUpdate = ShoreClient.latestUpdate;
            this.build();
        }
    }

    void build() {
        this.rows.clear();
        this.keyClicks.clear();
        this.listening = null;
        this.pendingSection = null;
        this.hexInput = null;
        Config c = ShoreClient.config;
        switch (this.page.ordinal()) {
            case 1: {
                if (this.current.settings() != null) {
                    this.current.settings().accept(this);
                }
                String id = this.current.id();
                this.section("Keybind");
                Row tk = this.row(8, "Toggle key", "Press it in game to switch " + this.current.name() + " on or off");
                tk.keyGet = () -> {
                    String t = c.moduleKeys.get(id);
                    return t == null ? class_3675.field_16237 : class_3675.method_15981((String)t);
                };
                tk.keySet = k -> {
                    if (k.equals((Object)class_3675.field_16237)) {
                        c.moduleKeys.remove(id);
                    } else {
                        c.moduleKeys.put(id, k.method_1441());
                    }
                    c.save();
                };
                break;
            }
            case 2: {
                this.section("Theme");
                Row r = this.row(5, "Accent color", "Used for tabs, switches and highlights");
                r.input = this.hexInput = new Input("RRGGBB", String.format("%06X", c.accent), 7, v -> {
                    if (v.matches("#?[0-9a-fA-F]{6}")) {
                        c.accent = Integer.parseInt(v.replace("#", ""), 16);
                        c.save();
                    }
                });
                this.slider("Panel opacity", "How much of the world shows through this menu", 20, 100, 1, "%", () -> c.panelOpacity, v -> {
                    c.panelOpacity = v;
                });
                this.slider("Menu size", "Scale of this menu on your screen", 75, 150, 1, "%", () -> c.menuScale, v -> {
                    c.menuScale = v;
                });
                this.section("Reset");
                this.action("Reset all settings", "Restore every ShoreMod option to its default", "Reset", this::resetAll);
                break;
            }
            case 3: {
                this.section("ShoreMod keys");
                this.keybind("Open ShoreMod", "This menu", ShoreClient.menuKey);
                this.keybind("Zoom", "Hold to zoom in", ShoreTools.zoomKey);
                this.keybind("Save a clip", "Save the last 30 seconds", ShoreClient.clipKey);
                this.section("Commands");
                this.info("/shore", "Open this menu", () -> "");
                this.info("/shore deaths", "List your recent death locations", () -> "");
                this.info("/tickets", "Open the ShoreSMP ticket site", () -> "");
                break;
            }
            case 4: {
                ModUpdates.Notice update = ShoreClient.latestUpdate;
                this.section("Version");
                this.info("Installed", "Running in this client", ShoreClient::version);
                this.info("Latest on this server", update == null ? "Join a server running the ShoreMod plugin" : (ShoreClient.updateAvailable() ? "An update is ready to install" : "You're up to date"), () -> update == null ? "Unknown" : update.version());
                this.section("Get updates");
                this.action("Check for updates", "Ask this server for the latest version", "Check", ShoreClient::checkUpdates);
                if (update != null && !update.url().isEmpty()) {
                    this.action("Download update", "Opens the server's download page", "Open", ShoreClient::openUpdate);
                    break;
                }
                this.info("Download link", "Ask ShoreSMP staff for the latest client JAR", () -> "Not set");
                break;
            }
        }
    }

    void section(String title) {
        this.pendingSection = title;
    }

    private Row row(int kind, String title, String help) {
        Row r = new Row();
        r.kind = kind;
        r.title = title;
        r.help = help;
        r.section = this.pendingSection;
        this.pendingSection = null;
        this.rows.add(r);
        return r;
    }

    Row toggle(String title, String help, BooleanSupplier get, Consumer<Boolean> set) {
        Row r = this.row(0, title, help);
        r.get = get;
        r.set = set;
        return r;
    }

    Row choice(String title, String help, Supplier<String> value, Runnable next) {
        Row r = this.row(1, title, help);
        r.value = value;
        r.action = next;
        return r;
    }

    Row action(String title, String help, String button, Runnable act) {
        Row r = this.row(2, title, help);
        r.value = () -> button;
        r.action = act;
        return r;
    }

    Row keybind(String title, String help, class_304 key) {
        Row r = this.row(8, title, help);
        r.keyGet = () -> key == null ? class_3675.field_16237 : class_3675.method_15981((String)key.method_1428());
        r.keySet = k -> {
            if (key == null) {
                return;
            }
            key.method_1422(k);
            class_304.method_1426();
            this.field_22787.field_1690.method_1640();
        };
        return r;
    }

    void chooseCrosshairImage() {
        Thread t = new Thread(() -> {
            String chosen;
            try (MemoryStack stack = MemoryStack.stackPush();){
                PointerBuffer filters = stack.mallocPointer(1);
                filters.put(stack.UTF8((CharSequence)"*.png"));
                filters.flip();
                chosen = TinyFileDialogs.tinyfd_openFileDialog((CharSequence)"Choose a crosshair image", (CharSequence)"", (PointerBuffer)filters, (CharSequence)"PNG images", (boolean)false);
            }
            if (chosen == null) {
                return;
            }
            class_310.method_1551().execute(() -> {
                try {
                    Config c = ShoreClient.config;
                    c.crosshairImage = ShoreHud.CrosshairImage.importFile(Path.of(chosen, new String[0]));
                    c.crosshairMode = 1;
                    c.crosshair = true;
                    c.save();
                    ShoreClient.tell("Crosshair image set to " + String.valueOf(Path.of(chosen, new String[0]).getFileName()) + ".");
                    if (this.page == Page.MODULE) {
                        this.build();
                    }
                }
                catch (IOException e) {
                    ShoreClient.tellError("Could not use that image: " + e.getMessage());
                }
            });
        }, "ShoreMod file picker");
        t.setDaemon(true);
        t.start();
    }

    private static String keyLabel(class_3675.class_306 k) {
        return k == null || k.equals((Object)class_3675.field_16237) ? "None" : k.method_27445().getString();
    }

    static String toggleById(String id) {
        if (registry == null) {
            registry = new ShoreScreen(null);
        }
        for (Module m : ShoreScreen.registry.modules) {
            if (!m.id().equals(id)) continue;
            m.set().accept(!m.get().getAsBoolean());
            ShoreClient.config.save();
            return m.name() + (m.get().getAsBoolean() ? " enabled" : " disabled");
        }
        return null;
    }

    Row info(String title, String help, Supplier<String> value) {
        Row r = this.row(3, title, help);
        r.value = value;
        return r;
    }

    Row slider(String title, String help, int min, int max, int step, String suffix, IntSupplier get, IntConsumer set) {
        Row r = this.row(6, title, help);
        r.min = min;
        r.max = max;
        r.step = step;
        r.suffix = suffix;
        r.iget = get;
        r.iset = set;
        r.input = new Input("Number", Integer.toString(get.getAsInt()), 6, v -> {
            OptionalInt number = SettingValues.integer((String)v, (int)min, (int)max);
            if (number.isPresent() && number.getAsInt() != get.getAsInt()) {
                set.accept(number.getAsInt());
                ShoreClient.config.save();
            }
        });
        r.input.validation = v -> SettingValues.integer((String)v, (int)min, (int)max).isPresent() ? "" : "Enter " + min + "\u2013" + max + suffix;
        r.input.currentValue = () -> Integer.toString(get.getAsInt());
        return r;
    }

    Row segment(String title, String help, String[] options, IntSupplier get, IntConsumer set) {
        Row r = this.row(7, title, help);
        r.options = options;
        r.iget = get;
        r.iset = set;
        return r;
    }

    private void setSlider(Row r, float x) {
        if (r.trackW <= 0.0f) {
            return;
        }
        float t = Math.clamp((x - r.trackX) / r.trackW, 0.0f, 1.0f);
        int v = r.min + Math.round(t * (float)(r.max - r.min) / (float)r.step) * r.step;
        if (v != r.iget.getAsInt()) {
            r.iset.accept(Math.clamp((long)v, r.min, r.max));
        }
    }

    public boolean method_25403(class_11909 click, double dx, double dy) {
        if (this.activeSlider != null) {
            this.setSlider(this.activeSlider, this.ui.fromGui(click.comp_4798()));
            return true;
        }
        return super.method_25403(click, dx, dy);
    }

    public boolean method_25406(class_11909 click) {
        if (this.activeSlider != null) {
            this.activeSlider = null;
            ShoreClient.config.save();
            return true;
        }
        return super.method_25406(click);
    }

    Row field(String title, String help, String placeholder, String value, Consumer<String> changed) {
        Row r = this.row(4, title, help);
        r.input = new Input(placeholder, value, 3000, changed);
        return r;
    }

    Row color(String title, String help, IntSupplier get, IntConsumer set) {
        Row r = this.field(title, help, "RRGGBB", String.format("%06X", get.getAsInt()), v -> {
            OptionalInt rgb = SettingValues.color((String)v);
            if (rgb.isPresent()) {
                set.accept(rgb.getAsInt());
                ShoreClient.config.save();
            }
        });
        r.input.max = 7;
        r.input.validation = v -> SettingValues.color((String)v).isPresent() ? "" : "Use six hex digits, e.g. 35DBD2";
        r.input.currentValue = () -> String.format("%06X", get.getAsInt());
        return r;
    }

    private void resetAll() {
        if (System.currentTimeMillis() > this.resetArmedUntil) {
            this.resetArmedUntil = System.currentTimeMillis() + 3000L;
            return;
        }
        Config c = ShoreClient.config;
        Config d = new Config();
        boolean reloadTextures = c.antiLeakTextures;
        for (Field f : Config.class.getFields()) {
            if (Modifier.isStatic(f.getModifiers()) || f.getName().equals("deaths") || f.getName().equals("chatDefaultsVersion")) continue;
            try {
                Object object;
                Object v = f.get(d);
                if (v instanceof List) {
                    List l = (List)v;
                    object = new ArrayList(l);
                } else {
                    object = v;
                }
                f.set(c, object);
            }
            catch (ReflectiveOperationException reflectiveOperationException) {
                // empty catch block
            }
        }
        c.save();
        ShoreClient.recorder.clear();
        if (reloadTextures && this.field_22787.field_1687 != null) {
            this.field_22787.field_1769.method_3279();
        }
        this.resetArmedUntil = 0L;
        this.build();
    }

    private static int next(int[] values, int current) {
        for (int i = 0; i < values.length; ++i) {
            if (values[i] != current) continue;
            return values[(i + 1) % values.length];
        }
        return values[0];
    }

    private static List<String> parse(String v) {
        return new ArrayList<String>(Arrays.stream(v.split(",")).map(String::strip).filter(s -> !s.isEmpty()).limit(100L).toList());
    }

    private static String formatSize(int mb) {
        return mb >= 1024 ? mb / 1024 + " GB" : mb + " MB";
    }

    private boolean over(float x, float y, float w, float h) {
        if (this.mx < x || this.my < y || this.mx >= x + w || this.my >= y + h) {
            return false;
        }
        return this.clip == null || this.mx >= this.clip[0] && this.my >= this.clip[1] && this.mx < this.clip[0] + this.clip[2] && this.my < this.clip[1] + this.clip[3];
    }

    private boolean hit(float x, float y, float w, float h, Runnable click) {
        this.hits.add(new Hit(x, y, w, h, this.clip, click, null));
        return this.over(x, y, w, h);
    }

    private boolean hitInput(float x, float y, float w, float h, Input input) {
        this.hits.add(new Hit(x, y, w, h, this.clip, null, input));
        return this.over(x, y, w, h);
    }

    public boolean method_25402(class_11909 click, boolean doubled) {
        int i;
        float px = this.ui.fromGui(click.comp_4798());
        float py = this.ui.fromGui(click.comp_4799());
        if (this.listening != null && click.method_74245() >= 2) {
            this.listening.keySet.accept(class_3675.class_307.field_1672.method_1447(click.method_74245()));
            this.listening = null;
            return true;
        }
        if (click.method_74245() != 0) {
            return true;
        }
        Row wasListening = this.listening;
        this.listening = null;
        if (wasListening != null) {
            for (i = this.hits.size() - 1; i >= 0; --i) {
                if (!this.hits.get(i).contains(px, py) || this.hits.get(i).input() != null || this.hits.get(i).click() != this.keyClick(wasListening)) continue;
                return true;
            }
        }
        for (i = this.hits.size() - 1; i >= 0; --i) {
            Hit h = this.hits.get(i);
            if (!h.contains(px, py)) continue;
            if (h.input() != null) {
                h.input().selectAll = doubled || this.focused != h.input() && h.input().currentValue != null;
                this.focused = h.input();
                this.caretEpoch = System.currentTimeMillis();
                this.placeCaret(h, px);
                return true;
            }
            this.focused = null;
            this.field_22787.method_1483().method_4873((class_1113)class_1109.method_47978((class_6880)class_3417.field_15015, (float)1.0f));
            this.pressX = px;
            h.click().run();
            return true;
        }
        this.focused = null;
        return true;
    }

    private void placeCaret(Hit h, float px) {
        Input in = h.input();
        float tx = h.x() + 12.0f;
        float best = Float.MAX_VALUE;
        int at = in.text.length();
        for (int i = 0; i <= in.text.length(); ++i) {
            float d = Math.abs(tx + this.ui.width(in.text.substring(0, i), false, 13.0f) - px);
            if (!(d < best)) continue;
            best = d;
            at = i;
        }
        in.cursor = at;
    }

    public boolean method_25401(double x, double y, double horizontal, double vertical) {
        if (this.maxScroll > 0.0f) {
            this.scrollTarget = Math.clamp(this.scrollTarget - (float)vertical * 48.0f, 0.0f, this.maxScroll);
        }
        return true;
    }

    public boolean method_25404(class_11908 input) {
        boolean ctrl;
        int key = input.comp_4795();
        boolean bl = ctrl = (input.comp_4797() & 2) != 0;
        if (this.listening != null) {
            Row r = this.listening;
            this.listening = null;
            if (key == 256) {
                return true;
            }
            r.keySet.accept(key == 259 || key == 261 ? class_3675.field_16237 : class_3675.method_15985((class_11908)input));
            return true;
        }
        if (this.focused != null) {
            Input in = this.focused;
            this.caretEpoch = System.currentTimeMillis();
            switch (key) {
                case 256: {
                    if (in.currentValue != null) {
                        in.set(in.currentValue.get(), false);
                    }
                    this.focused = null;
                    break;
                }
                case 257: 
                case 335: {
                    if (in.validation != null && !in.validation.apply(in.text).isEmpty()) break;
                    this.focused = null;
                    break;
                }
                case 259: {
                    in.backspace(ctrl);
                    break;
                }
                case 261: {
                    in.delete();
                    break;
                }
                case 263: {
                    in.selectAll = false;
                    in.cursor = Math.max(0, in.cursor - 1);
                    break;
                }
                case 262: {
                    in.selectAll = false;
                    in.cursor = Math.min(in.text.length(), in.cursor + 1);
                    break;
                }
                case 268: {
                    in.selectAll = false;
                    in.cursor = 0;
                    break;
                }
                case 269: {
                    in.selectAll = false;
                    in.cursor = in.text.length();
                    break;
                }
                case 65: {
                    if (!ctrl) break;
                    in.selectAll = true;
                    break;
                }
                case 86: {
                    if (!ctrl) break;
                    in.insert(this.field_22787.field_1774.method_1460());
                    break;
                }
                case 67: {
                    if (!ctrl) break;
                    this.field_22787.field_1774.method_1455(in.text);
                    break;
                }
            }
            return true;
        }
        if (ctrl && key == 70) {
            if (this.page != Page.GRID) {
                this.openPage(Page.GRID);
            }
            this.focused = this.search;
            return true;
        }
        if (key == 256) {
            if (this.page == Page.GRID && !this.search.text.isEmpty()) {
                this.search.set("", true);
                return true;
            }
            if (this.page != Page.GRID) {
                this.openPage(Page.GRID);
                return true;
            }
        }
        return super.method_25404(input);
    }

    public boolean method_25400(class_11905 input) {
        if (this.focused == null || !input.method_74227()) {
            return false;
        }
        this.focused.insert(input.method_74226());
        this.caretEpoch = System.currentTimeMillis();
        return true;
    }

    public void method_25394(class_332 ctx, int guiX, int guiY, float delta) {
        ShoreCompatSmoke.rendered();
        long now = System.nanoTime();
        float dt = this.lastFrame == 0L ? 0.0f : Math.min(0.1f, (float)(now - this.lastFrame) / 1.0E9f);
        this.lastFrame = now;
        this.frameDt = dt;
        this.ui.begin(ctx, ShoreUi.unit(this.field_22787, ShoreClient.config.menuScale));
        class_1041 window = this.field_22787.method_22683();
        this.mx = this.ui.fromGui(this.field_22787.field_1729.method_68879(window));
        this.my = this.ui.fromGui(this.field_22787.field_1729.method_68883(window));
        this.hits.clear();
        this.tooltip = null;
        this.clip = null;
        this.scroll += (this.scrollTarget - this.scroll) * Math.min(1.0f, dt * 14.0f);
        if (Math.abs(this.scrollTarget - this.scroll) < 0.3f) {
            this.scroll = this.scrollTarget;
        }
        this.ui.rect(0.0f, 0.0f, this.ui.W, this.ui.H, 0x55000000);
        float margin = Math.clamp(this.ui.W * 0.055f, 18.0f, 90.0f);
        float cw = Math.min(this.ui.W - margin * 2.0f, 1240.0f);
        float cx = (this.ui.W - cw) / 2.0f;
        float barY = Math.clamp(this.ui.H * 0.04f, 12.0f, 30.0f);
        float tabsY = barY + 44.0f;
        float panelY = tabsY + 50.0f;
        float panelH = this.ui.H - panelY - Math.clamp(this.ui.H * 0.04f, 12.0f, 30.0f);
        this.drawTopBar(cx, cw, barY, tabsY);
        int alpha = Math.round((float)ShoreClient.config.panelOpacity * 2.4f);
        this.ui.round(cx, panelY, cw, panelH, 12.0f, alpha << 24 | 0xD0F12);
        if (this.page == Page.GRID) {
            this.drawGrid(cx, panelY, cw, panelH, dt);
        } else {
            this.drawSettings(cx, panelY, cw, panelH, dt);
        }
        if (this.tooltip != null) {
            float tw = this.ui.width(this.tooltip, false, 12.0f) + 20.0f;
            float tx = Math.min(this.mx + 12.0f, this.ui.W - tw - 6.0f);
            float ty = this.my + 18.0f;
            this.ui.outline(tx, ty, tw, 26.0f, 6.0f, -14012876, -183364584);
            this.ui.text(this.tooltip, false, 12.0f, tx + 10.0f, ty + 7.0f, -723465);
        }
        this.ui.end();
    }

    private void drawTopBar(float cx, float cw, float barY, float tabsY) {
        Object[][] tabs;
        int accent = ShoreScreen.accent();
        this.ui.icon("@logo", cx, barY - 3.0f, 36.0f, 0);
        LocalDateTime t = LocalDateTime.now();
        String date = t.format(DATE);
        String time = t.format(TIME);
        float dw = this.ui.width(date, true, 13.0f);
        float pw = dw + this.ui.width(time, false, 13.0f) + 54.0f;
        float px = cx + 52.0f;
        this.ui.round(px, barY, pw, 30.0f, 8.0f, -351136745);
        int hour = t.getHour();
        this.ui.icon(hour >= 7 && hour < 19 ? "sun" : "moon", px + 13.0f, barY + 8.0f, 14.0f, -723465);
        this.ui.text(date, true, 13.0f, px + 35.0f, barY + 8.0f, -723465);
        this.ui.text(time, false, 13.0f, px + 35.0f + dw + 9.0f, barY + 8.0f, -6577750);
        float x = cx;
        float h = 38.0f;
        float pillW = this.ui.width("MOD MENU", true, 13.0f) + 64.0f;
        boolean sel = this.page == Page.GRID || this.page == Page.MODULE;
        boolean hov = this.hit(x, tabsY, pillW, h, () -> this.openPage(Page.GRID));
        this.ui.round(x, tabsY, pillW, h, 9.0f, sel ? (hov ? ShoreUi.mix(accent, -1, 0.12f) : accent) : (hov ? -182706396 : -351136745));
        int fg = sel ? ShoreScreen.onAccent() : -723465;
        this.ui.icon("grid", x + 20.0f, tabsY + 12.0f, 14.0f, fg);
        this.ui.text("MOD MENU", true, 13.0f, x + 43.0f, tabsY + 12.5f, fg);
        x += pillW + 10.0f;
        for (Object[] tab : tabs = new Object[][]{{"gear", "General settings", Page.GENERAL}, {"keyboard", "Controls", Page.CONTROLS}, {"download", "Updates", Page.UPDATES}}) {
            Page p = (Page)((Object)tab[2]);
            this.topButton(x, tabsY, h, (String)tab[0], (String)tab[1], this.page == p, false, () -> this.openPage(p));
            if (p == Page.UPDATES && ShoreClient.updateAvailable()) {
                this.ui.circle(x + h - 9.0f, tabsY + 9.0f, 8.0f, -41862);
            }
            x += h + 10.0f;
        }
        this.topButton(x, tabsY, h, "ticket", "Tickets", false, false, TicketsScreen::open);
        float right = cx + cw;
        this.topButton(right - h, tabsY, h, "close", "Close", false, true, this::method_25419);
        this.topButton(right - h * 2.0f - 10.0f, tabsY, h, "palette", "Theme", this.page == Page.GENERAL, true, () -> this.openPage(Page.GENERAL));
    }

    private void topButton(float x, float y, float s, String icon, String tip, boolean selected, boolean outlined, Runnable action) {
        int fg;
        int accent = ShoreScreen.accent();
        boolean hov = this.hit(x, y, s, s, action);
        if (outlined) {
            this.ui.ring(x, y, s, s, 9.0f, accent, hov ? ShoreUi.mix(-351136745, accent, 0.18f) : -351136745);
            fg = accent;
        } else if (selected) {
            this.ui.round(x, y, s, s, 9.0f, accent);
            fg = ShoreScreen.onAccent();
        } else {
            this.ui.round(x, y, s, s, 9.0f, hov ? -182706396 : -351136745);
            fg = hov ? -1 : -723465;
        }
        this.ui.icon(icon, x + (s - 17.0f) / 2.0f, y + (s - 17.0f) / 2.0f, 17.0f, fg);
        if (hov) {
            this.tooltip = tip;
        }
    }

    private void drawGrid(float px, float py, float pw, float ph, float dt) {
        String cat;
        float w;
        int accent = ShoreScreen.accent();
        float pad = 18.0f;
        float ix = px + pad;
        float iw = pw - pad * 2.0f;
        float cy = py + pad;
        float ch = 32.0f;
        float heartX = ix + iw - ch;
        float searchW = Math.clamp(iw * 0.18f, 120.0f, 240.0f);
        float searchX = heartX - 10.0f - searchW;
        boolean hov = this.hit(heartX, cy, ch, ch, () -> {
            this.favoritesOnly = !this.favoritesOnly;
            this.scrollTarget = 0.0f;
            this.scroll = 0.0f;
        });
        this.ui.round(heartX, cy, ch, ch, 8.0f, this.favoritesOnly ? ShoreUi.mix(-14934236, -41862, 0.22f) : (hov ? -14276048 : -14934236));
        this.ui.icon(this.favoritesOnly ? "heart_filled" : "heart", heartX + (ch - 15.0f) / 2.0f, cy + (ch - 15.0f) / 2.0f + 0.5f, 15.0f, this.favoritesOnly ? -41862 : (hov ? -723465 : -6577750));
        if (hov) {
            this.tooltip = "Favorites only";
        }
        this.drawInput(this.search, searchX, cy, searchW, ch, "search");
        float x = ix;
        String[] stringArray = CATEGORIES;
        int n = stringArray.length;
        for (int i = 0; i < n && !(x + (w = Math.max(44.0f, this.ui.width(cat = stringArray[i], this.sel0(cat), 13.0f) + 24.0f)) > searchX - 12.0f); ++i) {
            boolean sel = this.category.equals(cat);
            boolean h = this.hit(x, cy, w, ch, () -> {
                this.category = cat;
                this.scrollTarget = 0.0f;
                this.scroll = 0.0f;
            });
            this.ui.round(x, cy, w, ch, 8.0f, sel ? (h ? ShoreUi.mix(accent, -1, 0.12f) : accent) : (h ? -14276048 : -14934236));
            this.ui.centered(cat, sel, 13.0f, x + w / 2.0f, cy + (ch - 13.0f) / 2.0f, sel ? ShoreScreen.onAccent() : (h ? -723465 : -6577750), w - 10.0f);
            x += w + 7.0f;
        }
        float top = cy + ch + 16.0f;
        float viewH = py + ph - 10.0f - top;
        float gap = 14.0f;
        int cols = Math.max(2, (int)((iw + gap) / (168.0f + gap)));
        float cardW = (iw - 8.0f - gap * (float)(cols - 1)) / (float)cols;
        float cardH = Math.clamp(cardW * 0.8f, 150.0f, 220.0f);
        List<Module> list = this.visibleModules();
        int rowsN = (list.size() + cols - 1) / cols;
        float contentH = rowsN == 0 ? 0.0f : (float)rowsN * (cardH + gap) - gap;
        this.maxScroll = Math.max(0.0f, contentH - viewH + 4.0f);
        this.scrollTarget = Math.clamp(this.scrollTarget, 0.0f, this.maxScroll);
        this.scroll = Math.clamp(this.scroll, 0.0f, this.maxScroll);
        this.clip = new float[]{px, top, pw, viewH};
        this.ui.scissor(px, top, pw, viewH);
        boolean titleBold = true;
        for (Module m : this.modules) {
            if (!(this.ui.width(m.name(), true, 15.0f) > cardW - 74.0f)) continue;
            titleBold = false;
            break;
        }
        for (int i = 0; i < list.size(); ++i) {
            float x0 = ix + (float)(i % cols) * (cardW + gap);
            float y0 = top + (float)(i / cols) * (cardH + gap) - this.scroll;
            if (y0 + cardH < top || y0 > top + viewH) continue;
            this.drawCard(list.get(i), x0, y0, cardW, cardH, titleBold);
        }
        if (list.isEmpty()) {
            boolean favs = this.favoritesOnly && this.search.text.isBlank();
            float mid = px + pw / 2.0f;
            this.ui.icon(favs ? "heart" : "search", mid - 18.0f, top + 50.0f, 36.0f, -10459537);
            this.ui.centered(favs ? "No favorites yet" : "No mods match", true, 15.0f, mid, top + 100.0f, -6577750, iw);
            this.ui.centered(favs ? "Click the heart on a mod to pin it here." : "Try another search or category.", false, 13.0f, mid, top + 124.0f, -10459537, iw);
        }
        this.ui.endScissor();
        this.clip = null;
        this.scrollbar(px + pw - 9.0f, top, viewH, contentH);
    }

    private void drawCard(Module m, float x, float y, float w, float h, boolean titleBold) {
        boolean on = m.get().getAsBoolean();
        boolean hover = this.over(x, y, w, h);
        this.hit(x, y, w, h, () -> this.openModule(m));
        this.ui.outline(x, y, w, h, 11.0f, hover ? -13354687 : -14473427, hover ? -15131615 : -15394788);
        float pad = 18.0f;
        this.ui.text(m.name(), titleBold, 15.0f, x + pad, y + pad, on ? -723465 : -6577750, w - pad * 2.0f - 34.0f);
        boolean fav = this.favorite(m);
        float hs = 30.0f;
        float hx = x + w - pad - hs + 4.0f;
        float hy = y + 12.0f;
        boolean heartHover = this.hit(hx, hy, hs, hs, () -> this.toggleFavorite(m));
        this.ui.round(hx, hy, hs, hs, 8.0f, heartHover ? -14012619 : -14802393);
        this.ui.icon(fav ? "heart_filled" : "heart", hx + (hs - 14.0f) / 2.0f, hy + (hs - 14.0f) / 2.0f + 0.5f, 14.0f, fav ? -41862 : (heartHover ? -723465 : -6577750));
        float by = y + h - pad - 34.0f;
        float area = by - (y + 46.0f);
        float is = Math.min(52.0f, area * 0.72f);
        this.ui.icon(m.icon(), x + (w - is) / 2.0f, y + 46.0f + (area - is) / 2.0f, is, on ? -1 : -10656917);
        float gs = 34.0f;
        boolean gearHover = this.hit(x + pad, by, gs, gs, () -> this.openModule(m));
        this.ui.round(x + pad, by, gs, gs, 8.0f, gearHover ? -14012619 : -14802393);
        this.ui.icon("gear", x + pad + (gs - 15.0f) / 2.0f, by + (gs - 15.0f) / 2.0f, 15.0f, gearHover ? -723465 : -6577750);
        if (gearHover) {
            this.tooltip = "Settings";
        }
        float tx = x + pad + gs + 10.0f;
        float tw = w - pad * 2.0f - gs - 10.0f;
        boolean toggleHover = this.hit(tx, by, tw, gs, () -> this.toggleModule(m));
        this.toggleButton(tx, by, tw, gs, on, toggleHover);
    }

    private void toggleButton(float x, float y, float w, float h, boolean on, boolean hover) {
        this.ui.round(x, y, w, h, 8.0f, on ? (hover ? -7613634 : -8600017) : (hover ? -14210255 : -14802393));
        this.ui.centered(on ? "Enabled" : "Disabled", true, 13.5f, x + w / 2.0f, y + (h - 13.5f) / 2.0f, on ? -1 : -6577750, w - 10.0f);
    }

    private void scrollbar(float x, float top, float viewH, float contentH) {
        if (this.maxScroll <= 0.0f) {
            return;
        }
        float thumb = Math.max(36.0f, viewH * viewH / contentH);
        float ty = top + (viewH - thumb) * this.scroll / this.maxScroll;
        this.ui.round(x, top, 4.0f, viewH, 2.0f, 0x10FFFFFF);
        this.ui.round(x, ty, 4.0f, thumb, 2.0f, 0x50FFFFFF);
    }

    private void resetModule(Module m) {
        Config c = ShoreClient.config;
        Config d = new Config();
        if (HUD_IDS.contains(m.id())) {
            c.hudSizes.remove(m.id());
        }
        for (String name : RESET_FIELDS.getOrDefault(m.id(), new String[0])) {
            try {
                Object object;
                Field f = Config.class.getField(name);
                Object v = f.get(d);
                if (v instanceof List) {
                    List l = (List)v;
                    object = new ArrayList(l);
                } else {
                    object = v;
                }
                f.set(c, object);
            }
            catch (ReflectiveOperationException e) {
                ShoreClient.LOG.warn("Cannot reset {}", (Object)name);
            }
        }
        c.save();
        this.build();
    }

    private void drawSettings(float px, float py, float pw, float ph, float dt) {
        float pad = 18.0f;
        float ix = px + pad;
        float iw = pw - pad * 2.0f;
        float hy = py + 14.0f;
        float hh = 30.0f;
        if (this.page == Page.MODULE) {
            String reset;
            boolean rh;
            float rw;
            float rx;
            Module m = this.current;
            boolean back = this.hit(ix, hy, hh, hh, () -> this.openPage(Page.GRID));
            this.ui.round(ix, hy, hh, hh, 7.0f, back ? -14012619 : -14802393);
            this.ui.icon("back", ix + 8.0f, hy + 8.0f, 14.0f, back ? -723465 : -6577750);
            if (back) {
                this.tooltip = "Back to mods";
            }
            this.ui.round(rx, hy, rw, hh, 7.0f, (rh = this.hit(rx = ix + iw - (rw = this.ui.width(reset = "Reset Settings", false, 12.5f) + 26.0f), hy, rw, hh, () -> this.resetModule(m))) ? -14012619 : -14802393);
            this.ui.text(reset, false, 12.5f, rx + 13.0f, hy + (hh - 12.5f) / 2.0f, rh ? -723465 : -6577750);
            float fx = rx - hh - 8.0f;
            boolean fav = this.favorite(m);
            boolean fh = this.hit(fx, hy, hh, hh, () -> this.toggleFavorite(m));
            this.ui.round(fx, hy, hh, hh, 7.0f, fh ? -14012619 : -14802393);
            this.ui.icon(fav ? "heart_filled" : "heart", fx + 8.0f, hy + 8.0f, 14.0f, fav ? -41862 : (fh ? -723465 : -6577750));
            float lx = ix + hh + 12.0f;
            this.ui.icon(m.icon(), lx, hy + (hh - 20.0f) / 2.0f, 20.0f, m.get().getAsBoolean() ? -1 : -6577750);
            float nx = lx + 30.0f;
            this.ui.text(m.name(), true, 14.0f, nx, hy + (hh - 14.0f) / 2.0f, -723465);
            float dx = nx + this.ui.width(m.name(), true, 14.0f) + 12.0f;
            float maxDesc = fx - dx - 70.0f;
            String desc = this.ui.fit(m.desc(), false, 12.5f, maxDesc);
            this.ui.text(desc, false, 12.5f, dx, hy + (hh - 12.5f) / 2.0f, -6577750);
            float sx = dx + this.ui.width(desc, false, 12.5f) + 12.0f;
            boolean sh = this.hit(sx, hy + (hh - 18.0f) / 2.0f, 36.0f, 18.0f, () -> this.toggleModule(m));
            this.drawSwitch(sx, hy + hh / 2.0f, m.get().getAsBoolean(), true, sh, m.id());
        } else {
            String title = switch (this.page.ordinal()) {
                case 2 -> "General";
                case 3 -> "Controls";
                case 4 -> "Updates";
                default -> "";
            };
            String sub = switch (this.page.ordinal()) {
                case 2 -> "Theme and menu settings";
                case 3 -> "Keybinds and commands";
                case 4 -> "Your ShoreMod version";
                default -> "";
            };
            String ic = switch (this.page.ordinal()) {
                case 2 -> "gear";
                case 3 -> "keyboard";
                case 4 -> "download";
                default -> "grid";
            };
            this.ui.icon(ic, ix + 4.0f, hy + (hh - 20.0f) / 2.0f, 20.0f, -1);
            this.ui.text(title, true, 14.0f, ix + 34.0f, hy + (hh - 14.0f) / 2.0f, -723465);
            this.ui.text(sub, false, 12.5f, ix + 34.0f + this.ui.width(title, true, 14.0f) + 12.0f, hy + (hh - 12.5f) / 2.0f, -6577750, iw - 200.0f);
        }
        float top = hy + hh + 12.0f;
        float viewH = py + ph - 8.0f - top;
        float w = iw - 10.0f;
        float contentH = this.layoutGrid(ix, top, w, false);
        this.maxScroll = Math.max(0.0f, contentH - viewH + 8.0f);
        this.scrollTarget = Math.clamp(this.scrollTarget, 0.0f, this.maxScroll);
        this.scroll = Math.clamp(this.scroll, 0.0f, this.maxScroll);
        this.clip = new float[]{px, top, pw, viewH};
        this.ui.scissor(px, top, pw, viewH);
        this.layoutGrid(ix, top - this.scroll, w, true);
        this.ui.endScissor();
        this.clip = null;
        this.scrollbar(px + pw - 9.0f, top, viewH, contentH + 8.0f);
    }

    private static boolean wide(Row r) {
        return r.kind == 4 || r.kind == 5;
    }

    private static boolean full(Row r) {
        return ShoreScreen.wide(r) || r.kind == 7 && r.options.length > 3;
    }

    private static float height(Row r) {
        return ShoreScreen.wide(r) ? 64.0f : 34.0f;
    }

    private float layoutGrid(float x, float y0, float w, boolean draw) {
        float y = y0;
        int i = 0;
        String key = this.page == Page.MODULE ? this.current.id() : this.page.name();
        int accent = ShoreScreen.accent();
        while (i < this.rows.size()) {
            int j;
            String sec = this.rows.get((int)i).section != null ? this.rows.get((int)i).section : "Settings";
            for (j = i + 1; j < this.rows.size() && this.rows.get((int)j).section == null; ++j) {
            }
            boolean closed = this.collapsed.contains(key + "/" + sec);
            if (draw) {
                boolean hh = this.hit(x, y, w, 24.0f, () -> {
                    if (!this.collapsed.remove(key + "/" + sec)) {
                        this.collapsed.add(key + "/" + sec);
                    }
                });
                this.ui.icon(closed ? "chevron" : "chevron_down", x + 2.0f, y + 6.0f, 12.0f, accent);
                this.ui.text(sec, true, 13.0f, x + 22.0f, y + 5.0f, hh ? -1 : -723465);
            }
            y += 28.0f;
            if (!closed) {
                ArrayList<Row[]> lines = new ArrayList<Row[]>();
                Row pending = null;
                for (int k = i; k < j; ++k) {
                    Row r = this.rows.get(k);
                    if (ShoreScreen.full(r)) {
                        if (pending != null) {
                            lines.add(new Row[]{pending, null});
                            pending = null;
                        }
                        lines.add(new Row[]{r});
                        continue;
                    }
                    if (pending == null) {
                        pending = r;
                        continue;
                    }
                    lines.add(new Row[]{pending, r});
                    pending = null;
                }
                if (pending != null) {
                    lines.add(new Row[]{pending, null});
                }
                float pad = 16.0f;
                float cardH = pad * 2.0f - 8.0f;
                for (Row[] l : lines) {
                    cardH += ShoreScreen.height(l[0]);
                }
                if (draw) {
                    this.ui.outline(x, y, w, cardH, 9.0f, -14473427, -15394788);
                    float ly = y + pad - 4.0f;
                    float colGap = 36.0f;
                    float colW = (w - pad * 2.0f - colGap) / 2.0f;
                    for (Row[] l : lines) {
                        float lh = ShoreScreen.height(l[0]);
                        if (l.length == 1) {
                            this.drawCompactRow(l[0], x + pad, ly, w - pad * 2.0f, lh);
                        } else {
                            this.drawCompactRow(l[0], x + pad, ly, colW, lh);
                            if (l[1] != null) {
                                this.drawCompactRow(l[1], x + pad + colW + colGap, ly, colW, lh);
                            }
                        }
                        ly += lh;
                    }
                }
                y += cardH + 14.0f;
            }
            i = j;
        }
        return y - y0;
    }

    private void drawSwitch(float x, float cy, boolean value, boolean on, boolean hover, String key) {
        float an;
        float target = value ? 1.0f : 0.0f;
        Float a = this.switchAnim.get(key);
        float f = an = a == null ? target : a.floatValue() + (target - a.floatValue()) * Math.min(1.0f, this.frameDt * 16.0f);
        if (Math.abs(target - an) < 0.01f) {
            an = target;
        }
        this.switchAnim.put(key, Float.valueOf(an));
        int track = ShoreUi.mix(-13288895, -8600017, an);
        if (!on) {
            track = ShoreUi.mix(track, -15394788, 0.55f);
        }
        if (hover) {
            track = ShoreUi.mix(track, -1, 0.08f);
        }
        this.ui.round(x, cy - 9.0f, 36.0f, 18.0f, 9.0f, track);
        this.ui.circle(x + 9.0f + an * 18.0f, cy, 13.0f, on ? -1 : -7630181);
    }

    private void drawCompactRow(Row r, float x, float y, float w, float h) {
        if (y + h < this.clip[1] || y > this.clip[1] + this.clip[3]) {
            return;
        }
        int accent = ShoreScreen.accent();
        boolean on = r.available.getAsBoolean();
        float cy = ShoreScreen.wide(r) ? y + 12.0f : y + h / 2.0f;
        float tx = x + (float)(r.indent > 0 ? 12 : 0);
        String value = r.value == null ? "" : r.value.get();
        float cw = 0.0f;
        float right = x + w;
        boolean labelHover = false;
        switch (r.kind) {
            case 0: {
                cw = 36.0f;
                boolean hov = on && this.hit(x, y, w, h, () -> {
                    r.set.accept(!r.get.getAsBoolean());
                    ShoreClient.config.save();
                });
                this.drawSwitch(right - 36.0f, cy, r.get.getAsBoolean(), on, hov, "row:" + System.identityHashCode(r));
                labelHover = hov;
                break;
            }
            case 1: {
                cw = Math.max(96.0f, this.ui.width(value, false, 12.5f) + 36.0f);
                float bx = right - cw;
                boolean hov = on && this.hit(bx, cy - 12.0f, cw, 24.0f, r.action);
                this.ui.outline(bx, cy - 12.0f, cw, 24.0f, 6.0f, hov ? -12959930 : -14012876, -16052977);
                this.ui.text(value, false, 12.5f, bx + 10.0f, cy - 6.25f, on ? -723465 : -10459537, cw - 30.0f);
                this.ui.icon("chevron_down", right - 20.0f, cy - 5.0f, 10.0f, on ? -6577750 : -10459537);
                break;
            }
            case 7: {
                float sx = right;
                for (int k = r.options.length - 1; k >= 0; --k) {
                    boolean bh;
                    float bw = this.ui.width(r.options[k], false, 12.5f) + 20.0f;
                    int idx = k;
                    boolean sel = r.iget.getAsInt() == k;
                    boolean bl = bh = on && this.hit(sx -= bw, cy - 12.0f, bw, 24.0f, () -> {
                        r.iset.accept(idx);
                        ShoreClient.config.save();
                    });
                    this.ui.round(sx, cy - 12.0f, bw, 24.0f, 6.0f, sel ? (on ? accent : -14934236) : (bh ? -14276048 : -14934236));
                    this.ui.centered(r.options[k], false, 12.5f, sx + bw / 2.0f, cy - 6.25f, sel ? (on ? ShoreScreen.onAccent() : -10459537) : (on ? (bh ? -723465 : -6577750) : -10459537), bw - 4.0f);
                    sx -= 5.0f;
                }
                cw = right - sx;
                break;
            }
            case 6: {
                float tx0;
                float trackW = Math.min(128.0f, w * 0.32f);
                cw = trackW + 106.0f;
                float vx = right - cw;
                r.trackX = tx0 = right - trackW - 6.0f;
                r.trackW = trackW;
                int v = r.iget.getAsInt();
                float t = (float)(v - r.min) / (float)(r.max - r.min);
                if (this.focused != r.input) {
                    r.input.set(Integer.toString(v), false);
                }
                if (on) {
                    this.drawInput(r.input, vx, cy - 12.0f, 76.0f, 24.0f, null);
                } else {
                    this.ui.round(vx, cy - 12.0f, 76.0f, 24.0f, 5.0f, -16052977);
                    this.ui.centered(Integer.toString(v), false, 12.0f, vx + 38.0f, cy - 6.0f, -10459537, 72.0f);
                }
                this.ui.text(r.suffix.strip(), false, 10.0f, vx + 79.0f, cy - 5.0f, on ? -6577750 : -10459537, 22.0f);
                if (this.over(vx, cy - 12.0f, 76.0f, 24.0f) && this.tooltip == null) {
                    this.tooltip = "Type any whole number from " + r.min + " to " + r.max + r.suffix + ". Esc discards an invalid entry.";
                }
                boolean sh = on && this.hit(tx0 - 8.0f, cy - 12.0f, trackW + 16.0f, 24.0f, () -> {
                    this.activeSlider = r;
                    this.setSlider(r, this.pressX);
                });
                this.ui.round(tx0, cy - 2.0f, trackW, 4.0f, 2.0f, -13288895);
                this.ui.round(tx0, cy - 2.0f, Math.max(4.0f, trackW * t), 4.0f, 2.0f, on ? accent : -10459537);
                this.ui.circle(tx0 + trackW * t, cy, this.activeSlider == r || sh ? 15.0f : 13.0f, on ? accent : -7630181);
                break;
            }
            case 8: {
                boolean hov;
                boolean listen = this.listening == r;
                String label = listen ? "Press a key..." : ShoreScreen.keyLabel(r.keyGet.get());
                cw = Math.max(96.0f, this.ui.width(label, false, 12.5f) + 24.0f);
                float bx = right - cw;
                boolean bl = hov = on && this.hit(bx, cy - 12.0f, cw, 24.0f, this.keyClick(r));
                this.ui.outline(bx, cy - 12.0f, cw, 24.0f, 6.0f, listen ? accent : (hov ? -12959930 : -14012876), -16052977);
                this.ui.text(label, false, 12.5f, bx + 10.0f, cy - 6.25f, listen ? accent : (on ? -723465 : -10459537), cw - 16.0f);
                if (!hov || listen) break;
                this.tooltip = "Click, then press a key. Backspace clears.";
                break;
            }
            case 2: {
                boolean hov;
                if (r.keycap) {
                    cw = Math.max(96.0f, this.ui.width(value, false, 12.5f) + 24.0f);
                    float bx = right - cw;
                    boolean hov2 = on && this.hit(bx, cy - 12.0f, cw, 24.0f, r.action);
                    this.ui.outline(bx, cy - 12.0f, cw, 24.0f, 6.0f, hov2 ? -12959930 : -14012876, -16052977);
                    this.ui.text(value, false, 12.5f, bx + 10.0f, cy - 6.25f, on ? -6577750 : -10459537, cw - 16.0f);
                    break;
                }
                boolean armed = value.equals("Reset") && System.currentTimeMillis() < this.resetArmedUntil;
                String label = armed ? "Click to confirm" : value;
                cw = Math.max(70.0f, this.ui.width(label, false, 12.5f) + 26.0f);
                float bx = right - cw;
                boolean bl = hov = on && this.hit(bx, cy - 12.0f, cw, 24.0f, r.action);
                int bg = armed ? -1750963 : (on ? (hov ? ShoreUi.mix(accent, -1, 0.12f) : accent) : -14934236);
                this.ui.round(bx, cy - 12.0f, cw, 24.0f, 6.0f, bg);
                this.ui.centered(label, false, 12.5f, bx + cw / 2.0f, cy - 6.25f, armed ? -1 : (on ? ShoreScreen.onAccent() : -10459537), cw - 6.0f);
                break;
            }
            case 3: {
                if (value.isEmpty()) break;
                cw = Math.min(w * 0.5f, this.ui.width(value, false, 12.5f));
                this.ui.text(value, false, 12.5f, right - cw, cy - 6.25f, on ? -6577750 : -10459537, cw + 1.0f);
                break;
            }
        }
        float labelW = (ShoreScreen.wide(r) ? w : w - cw - 12.0f) - (tx - x);
        this.ui.text(r.title, false, 12.5f, tx, cy - 6.25f, on ? -723465 : -10459537, labelW);
        if (!r.help.isEmpty() && (labelHover || this.over(tx, cy - 10.0f, Math.min(labelW, this.ui.width(r.title, false, 12.5f)), 20.0f))) {
            this.tooltip = r.help;
        }
        if (r.kind == 4) {
            this.drawInput(r.input, x, y + 28.0f, w, 28.0f, null);
        }
        if (r.kind == 5) {
            float sx = x;
            float sy = y + 28.0f;
            for (int c : ACCENTS) {
                boolean sel = ShoreClient.config.accent == c;
                boolean sh = this.hit(sx, sy, 26.0f, 26.0f, () -> {
                    ShoreClient.config.accent = c;
                    ShoreClient.config.save();
                    if (this.hexInput != null) {
                        this.hexInput.set(String.format("%06X", c), false);
                    }
                });
                if (sel) {
                    this.ui.circle(sx + 13.0f, sy + 13.0f, 26.0f, -1);
                    this.ui.circle(sx + 13.0f, sy + 13.0f, 22.0f, -15394788);
                } else if (sh) {
                    this.ui.circle(sx + 13.0f, sy + 13.0f, 24.0f, 0x40FFFFFF);
                }
                this.ui.circle(sx + 13.0f, sy + 13.0f, 18.0f, 0xFF000000 | c);
                sx += 32.0f;
            }
            float hw = 110.0f;
            this.ui.text("#", false, 12.5f, x + w - hw - 14.0f, sy + 13.0f - 6.25f, -10459537);
            this.drawInput(r.input, x + w - hw, sy, hw, 26.0f, null);
        }
    }

    private void drawInput(Input in, float x, float y, float w, float h, String icon) {
        String error;
        int accent = ShoreScreen.accent();
        boolean f = this.focused == in;
        boolean hov = this.hitInput(x, y, w, h, in);
        String string = error = in.validation == null ? "" : in.validation.apply(in.text);
        this.ui.outline(x, y, w, h, 8.0f, !error.isEmpty() ? -40341 : (f ? accent : (hov ? -12959930 : -14012876)), -16052977);
        if (hov && !error.isEmpty()) {
            this.tooltip = error;
        }
        float right = icon != null ? 30.0f : 12.0f;
        float tx = x + 12.0f;
        float avail = w - 12.0f - right;
        float ty = y + (h - 13.0f) / 2.0f;
        if (icon != null) {
            this.ui.icon(icon, x + w - 24.0f, y + (h - 13.0f) / 2.0f, 13.0f, f ? -723465 : -10459537);
        }
        this.ui.scissor(x + 8.0f, y, w - 8.0f - right + 4.0f, h);
        if (in.text.isEmpty()) {
            this.ui.text(in.placeholder, false, 13.0f, tx, ty, -10459537);
            if (f && this.caretOn()) {
                this.ui.rect(tx, y + 8.0f, 1.2f, h - 16.0f, -723465);
            }
        } else {
            float caretX = this.ui.width(in.text.substring(0, in.cursor), false, 13.0f);
            float shift = Math.max(0.0f, caretX - avail + 4.0f);
            if (in.selectAll && f) {
                this.ui.round(tx - 2.0f - shift, y + 7.0f, Math.min(avail + 4.0f, this.ui.width(in.text, false, 13.0f) + 4.0f), h - 14.0f, 3.0f, ShoreUi.alpha(accent, 0.35f));
            }
            this.ui.text(in.text, false, 13.0f, tx - shift, ty, -723465);
            if (f && this.caretOn() && !in.selectAll) {
                this.ui.rect(tx + caretX - shift, y + 8.0f, 1.2f, h - 16.0f, -723465);
            }
        }
        this.ui.endScissor();
    }

    private boolean sel0(String cat) {
        return this.category.equals(cat);
    }

    private boolean caretOn() {
        return (System.currentTimeMillis() - this.caretEpoch) / 530L % 2L == 0L;
    }

    public void method_25419() {
        ShoreClient.config.save();
        this.field_22787.method_1507(this.parent);
    }

    public boolean method_25421() {
        return false;
    }

    private static /* synthetic */ void lambda$defineModules$106(Config c, int v) {
        c.zoomSensitivity = v;
    }

    private static /* synthetic */ int lambda$defineModules$105(Config c) {
        return c.zoomSensitivity;
    }

    static {
        RESET_FIELDS = Map.ofEntries(Map.entry("cps", new String[]{"cpsRight"}), Map.entry("keystrokes", new String[]{"keystrokesMouse", "keystrokesSpace", "keystrokesAccent"}), Map.entry("items", new String[]{"itemsTotem", "itemsPearl", "itemsGapple", "itemsCrystal", "itemsArrow", "itemsXp", "itemsShowZero", "itemsCustom"}), Map.entry("target", new String[]{"targetDistance"}), Map.entry("totempops", new String[]{"totemPopsSelf"}), Map.entry("togglesprint", new String[]{"toggleSneak", "toggleSprintHud"}), Map.entry("stacking", new String[]{"stackStyle", "stackLookback"}), Map.entry("timestamps", new String[]{"timestamps24", "timestampSeconds"}), Map.entry("fullbright", new String[]{"fullbrightStrength"}), Map.entry("mentions", new String[]{"mentionSound", "highlightWords", "mentionColor", "mentionCustomColor"}), Map.entry("chat", new String[]{"chatHistoryLimit", "chatOpacity"}), Map.entry("mousetweaks", new String[]{"tweakRmb", "tweakLmb", "tweakShift", "tweakWheel"}), Map.entry("shulker", new String[]{"shulkerMode", "shulkerScale", "shulkerBorderMode", "shulkerBgOpacity", "shulkerTitle", "shulkerEmptySlots"}), Map.entry("zoom", new String[]{"zoomToggle", "zoomLevel", "zoomSensMode", "zoomSensitivity", "zoomCinematic", "zoomScroll", "zoomScrollSpeed", "zoomSmooth"}), Map.entry("durability", new String[]{"durabilityThreshold", "durabilitySound", "durabilityChat"}), Map.entry("deaths", new String[]{"deathAnnounce"}), Map.entry("reconnect", new String[]{"autoReconnect", "autoReconnectDelay"}), Map.entry("logos", new String[]{"badgeNametags", "badgeTab"}), Map.entry("clips", new String[]{"clipFps", "clipWidth", "saveEveryDisconnect", "punishmentPhrases", "maxClipStorageMb"}), Map.entry("crosshair", new String[]{"crosshairLength", "crosshairGap", "crosshairThickness", "crosshairDot", "crosshairColor", "crosshairOutline", "crosshairMode", "crosshairImageSize", "crosshairOpacity", "crosshairTint", "crosshairCustomColor"}));
        HUD_IDS = Set.of("fps", "ping", "coords", "biome", "clock", "armor", "cps", "togglesprint", "keystrokes", "target", "potions", "items");
    }

    private static enum Page {
        GRID,
        MODULE,
        GENERAL,
        CONTROLS,
        UPDATES;

    }

    static final class Input {
        String text = "";
        String placeholder;
        int cursor;
        int max;
        boolean selectAll;
        Consumer<String> change;
        Function<String, String> validation;
        Supplier<String> currentValue;

        Input(String placeholder, String value, int max, Consumer<String> change) {
            this.placeholder = placeholder;
            this.max = max;
            this.change = change;
            this.set(value, false);
        }

        void set(String v, boolean notify) {
            this.text = v.length() > this.max ? v.substring(0, this.max) : v;
            this.cursor = this.text.length();
            this.selectAll = false;
            if (notify && this.change != null) {
                this.change.accept(this.text);
            }
        }

        void insert(String s) {
            if (this.selectAll) {
                this.text = "";
                this.cursor = 0;
                this.selectAll = false;
            }
            s = s.replace("\n", "").replace("\r", "");
            int room = this.max - this.text.length();
            if (room <= 0) {
                return;
            }
            if (s.length() > room) {
                s = s.substring(0, room);
            }
            this.text = this.text.substring(0, this.cursor) + s + this.text.substring(this.cursor);
            this.cursor += s.length();
            if (this.change != null) {
                this.change.accept(this.text);
            }
        }

        void backspace(boolean word) {
            if (this.selectAll) {
                this.set("", true);
                return;
            }
            if (this.cursor == 0) {
                return;
            }
            int from = word ? this.wordStart() : this.cursor - 1;
            this.text = this.text.substring(0, from) + this.text.substring(this.cursor);
            this.cursor = from;
            if (this.change != null) {
                this.change.accept(this.text);
            }
        }

        void delete() {
            if (this.selectAll) {
                this.set("", true);
                return;
            }
            if (this.cursor < this.text.length()) {
                this.text = this.text.substring(0, this.cursor) + this.text.substring(this.cursor + 1);
                if (this.change != null) {
                    this.change.accept(this.text);
                }
            }
        }

        private int wordStart() {
            int i;
            for (i = this.cursor; i > 0 && this.text.charAt(i - 1) == ' '; --i) {
            }
            while (i > 0 && this.text.charAt(i - 1) != ' ') {
                --i;
            }
            return i;
        }
    }

    private record Module(String id, String name, String desc, String category, String icon, boolean isNew, BooleanSupplier get, Consumer<Boolean> set, Consumer<ShoreScreen> settings) {
    }

    static final class Row {
        int kind;
        String title;
        String help;
        String section;
        BooleanSupplier get;
        BooleanSupplier available = () -> true;
        Consumer<Boolean> set;
        Supplier<String> value;
        Runnable action;
        Input input;
        boolean keycap;
        int indent;
        float anim = -1.0f;
        IntSupplier iget;
        IntConsumer iset;
        int min;
        int max;
        int step;
        String suffix = "";
        float trackX;
        float trackW;
        String[] options;
        Supplier<class_3675.class_306> keyGet;
        Consumer<class_3675.class_306> keySet;

        Row() {
        }

        Row dependsOn(Row master) {
            this.available = master.get;
            this.indent = 14;
            return this;
        }
    }

    private record Hit(float x, float y, float w, float h, float[] clip, Runnable click, Input input) {
        boolean contains(float px, float py) {
            if (px < this.x || py < this.y || px >= this.x + this.w || py >= this.y + this.h) {
                return false;
            }
            return this.clip == null || px >= this.clip[0] && py >= this.clip[1] && px < this.clip[0] + this.clip[2] && py < this.clip[1] + this.clip[3];
        }
    }
}

