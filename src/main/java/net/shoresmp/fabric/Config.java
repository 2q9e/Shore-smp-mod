/*
 * Decompiled with CFR 0.152.
 */
package net.shoresmp.fabric;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.loader.api.FabricLoader;
import net.shoresmp.fabric.ShoreClient;

public final class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("shoremod.json");
    public boolean timestamps = false;
    public boolean mentionHighlight = true;
    public boolean filterDuplicates = true;
    public int chatDefaultsVersion = 2;
    public int chatHistoryLimit = 5000;
    public boolean shulkerPreview = true;
    public boolean mouseTweaks = true;
    public boolean badges = true;
    public boolean badgeNametags = true;
    public boolean badgeTab = true;
    public boolean privacy = false;
    public boolean hideChat = true;
    public boolean hideScoreboard = true;
    public boolean hideTab = true;
    public boolean hideDebug = true;
    public volatile boolean antiLeakTextures = false;
    public boolean recording = true;
    public boolean saveEveryDisconnect = false;
    public int accent = 3529682;
    public int panelOpacity = 90;
    public int clipFps = 15;
    public int clipWidth = 960;
    public int maxClipStorageMb = 1024;
    public double chatOpacity = 0.5;
    public String ffmpegPath = "";
    public List<String> punishmentPhrases = new ArrayList<String>(List.of("blacklisted modifications", "bug abusing", "temporarily banned", "permanently banned", "you are banned", "you have been banned", "unfair advantage", "cheating", "exploiting"));
    public List<String> highlightWords = new ArrayList<String>();
    public boolean mentionSound = false;
    public boolean hud = true;
    public boolean hudFps = true;
    public boolean hudPing = true;
    public boolean hudCoords = false;
    public boolean hudBiome = false;
    public boolean hudClock = false;
    public boolean hudArmor = true;
    public int hudCorner = 0;
    public int hudScale = 100;
    public Map<String, Integer> hudSizes = new HashMap<String, Integer>();
    public boolean fullbright = false;
    public int fullbrightStrength = 60;
    public int hudCustomColor = 0xFFFFFF;
    public int crosshairCustomColor = 0xFFFFFF;
    public int mentionCustomColor = 3529682;
    public boolean zoomScroll = true;
    public boolean zoomSmooth = true;
    public boolean durabilityAlert = true;
    public boolean deathLog = true;
    public boolean reconnectButton = true;
    public boolean autoReconnect = false;
    public int zoomLevel = 4;
    public int durabilityThreshold = 10;
    public int autoReconnectDelay = 10;
    public List<String> deaths = new ArrayList<String>();
    public boolean zoomEnabled = true;
    public boolean extendedChat = true;
    public boolean tweakRmb = true;
    public boolean tweakLmb = true;
    public boolean tweakShift = true;
    public boolean tweakWheel = true;
    public int menuScale = 100;
    public boolean zoomToggle = false;
    public boolean zoomCinematic = false;
    public int zoomSensMode = 1;
    public int zoomSensitivity = 60;
    public int zoomScrollSpeed = 20;
    public boolean cps = false;
    public boolean cpsRight = false;
    public boolean toggleSprint = false;
    public boolean toggleSneak = false;
    public boolean toggleSprintHud = true;
    public boolean crosshair = false;
    public boolean crosshairDot = false;
    public boolean crosshairOutline = true;
    public int crosshairColor = 0;
    public int crosshairLength = 5;
    public int crosshairGap = 2;
    public int crosshairThickness = 1;
    public boolean shulkerColorBorder = true;
    public boolean hudShadow = true;
    public boolean timestamps24 = true;
    public boolean timestampSeconds = false;
    public boolean durabilitySound = true;
    public boolean durabilityChat = false;
    public boolean deathAnnounce = true;
    public int shulkerMode = 0;
    public int hudBgOpacity = 50;
    public int hudTextColor = 0;
    public int stackStyle = 0;
    public int stackLookback = 8;
    public int mentionColor = 0;
    public boolean targetHud = false;
    public boolean potionHud = false;
    public boolean keystrokes = false;
    public boolean itemCounter = false;
    public boolean totemPops = false;
    public boolean totemPopsSelf = false;
    public boolean keystrokesMouse = true;
    public boolean keystrokesSpace = true;
    public boolean itemsTotem = true;
    public boolean itemsPearl = true;
    public boolean itemsGapple = true;
    public boolean itemsCrystal = true;
    public boolean itemsArrow = false;
    public boolean itemsXp = false;
    public List<String> itemsCustom = new ArrayList<String>();
    public boolean itemsShowZero = false;
    public boolean keystrokesAccent = false;
    public boolean targetDistance = false;
    public boolean shulkerTitle = true;
    public boolean shulkerEmptySlots = true;
    public int shulkerScale = 100;
    public int shulkerBgOpacity = 96;
    public int shulkerBorderMode = 0;
    public int crosshairMode = 0;
    public int crosshairImageSize = 15;
    public int crosshairOpacity = 100;
    public boolean crosshairTint = false;
    public String crosshairImage = "";
    public List<String> favorites = new ArrayList<String>();
    public Map<String, String> moduleKeys = new HashMap<String, String>();

    public int hudSize(String id) {
        Integer v = this.hudSizes.get(id);
        return v == null ? 100 : v;
    }

    public static Config load() {
        try {
            JsonObject data;
            Config result;
            if (Files.exists(PATH, new LinkOption[0]) && (result = (Config)GSON.fromJson((JsonElement)(data = JsonParser.parseString((String)Files.readString(PATH)).getAsJsonObject()), Config.class)) != null) {
                if (!data.has("chatDefaultsVersion")) {
                    result.timestamps = false;
                    result.mentionHighlight = false;
                    result.filterDuplicates = true;
                    result.chatDefaultsVersion = 1;
                    result.save();
                }
                if (result.chatDefaultsVersion < 2) {
                    result.mentionHighlight = true;
                    result.chatDefaultsVersion = 2;
                    result.save();
                }
                result.validate();
                return result;
            }
        }
        catch (IOException | RuntimeException e) {
            ShoreClient.LOG.warn("Could not read ShoreMod config; using defaults", (Throwable)e);
        }
        Config result = new Config();
        result.save();
        return result;
    }

    private void validate() {
        this.chatHistoryLimit = Math.clamp((long)this.chatHistoryLimit, 100, 20000);
        this.clipFps = Math.clamp((long)this.clipFps, 5, 30);
        this.clipWidth = Math.clamp((long)this.clipWidth, 320, 1920);
        this.panelOpacity = Math.clamp((long)this.panelOpacity, 20, 100);
        this.maxClipStorageMb = Math.clamp((long)this.maxClipStorageMb, 128, 16384);
        double d = this.chatOpacity = Double.isFinite(this.chatOpacity) ? Math.clamp(this.chatOpacity, 0.0, 1.0) : 0.5;
        if (this.punishmentPhrases == null) {
            this.punishmentPhrases = new ArrayList<String>();
        }
        if (this.highlightWords == null) {
            this.highlightWords = new ArrayList<String>();
        }
        if (this.ffmpegPath == null) {
            this.ffmpegPath = "";
        }
        this.privacy = false;
        if (this.deaths == null) {
            this.deaths = new ArrayList<String>();
        }
        if (this.favorites == null) {
            this.favorites = new ArrayList<String>();
        }
        if (this.moduleKeys == null) {
            this.moduleKeys = new HashMap<String, String>();
        }
        if (this.itemsCustom == null) {
            this.itemsCustom = new ArrayList<String>();
        }
        if (this.crosshairImage == null) {
            this.crosshairImage = "";
        }
        this.shulkerScale = Math.clamp((long)this.shulkerScale, 50, 200);
        this.shulkerBgOpacity = Math.clamp((long)this.shulkerBgOpacity, 0, 100);
        this.shulkerBorderMode = Math.clamp((long)this.shulkerBorderMode, 0, 2);
        this.crosshairMode = Math.clamp((long)this.crosshairMode, 0, 1);
        this.crosshairImageSize = Math.clamp((long)this.crosshairImageSize, 4, 64);
        this.crosshairOpacity = Math.clamp((long)this.crosshairOpacity, 10, 100);
        this.menuScale = Math.clamp((long)this.menuScale, 75, 150);
        this.zoomSensMode = Math.clamp((long)this.zoomSensMode, 0, 1);
        this.zoomSensitivity = Math.clamp((long)this.zoomSensitivity, 5, 100);
        this.zoomScrollSpeed = Math.clamp((long)this.zoomScrollSpeed, 5, 60);
        this.crosshairColor = Math.clamp((long)this.crosshairColor, 0, 6);
        this.crosshairLength = Math.clamp((long)this.crosshairLength, 1, 15);
        this.shulkerMode = Math.clamp((long)this.shulkerMode, 0, 1);
        this.hudBgOpacity = Math.clamp((long)this.hudBgOpacity, 0, 100);
        this.hudTextColor = Math.clamp((long)this.hudTextColor, 0, 3);
        this.stackStyle = Math.clamp((long)this.stackStyle, 0, 2);
        this.stackLookback = Math.clamp((long)this.stackLookback, 1, 20);
        this.mentionColor = Math.clamp((long)this.mentionColor, 0, 5);
        this.crosshairGap = Math.clamp((long)this.crosshairGap, 0, 10);
        this.crosshairThickness = Math.clamp((long)this.crosshairThickness, 1, 4);
        while (this.deaths.size() > 10) {
            this.deaths.removeLast();
        }
        this.hudCorner = Math.clamp((long)this.hudCorner, 0, 3);
        this.hudScale = Math.clamp((long)this.hudScale, 50, 250);
        if (this.hudSizes == null) {
            this.hudSizes = new HashMap<String, Integer>();
        }
        this.hudSizes.replaceAll((k, v) -> v == null ? 100 : Math.clamp((long)v.intValue(), 50, 250));
        this.zoomLevel = Math.clamp((long)this.zoomLevel, 2, 20);
        this.durabilityThreshold = Math.clamp((long)this.durabilityThreshold, 1, 50);
        this.autoReconnectDelay = Math.clamp((long)this.autoReconnectDelay, 3, 120);
        this.fullbrightStrength = Math.clamp((long)this.fullbrightStrength, 0, 100);
        this.hudCustomColor &= 0xFFFFFF;
        this.crosshairCustomColor &= 0xFFFFFF;
        this.mentionCustomColor &= 0xFFFFFF;
        this.favorites.removeIf(id -> id == null || id.equals("reach") || id.equals("combo"));
        this.moduleKeys.remove("reach");
        this.moduleKeys.remove("combo");
        this.hudSizes.remove("reach");
        this.hudSizes.remove("combo");
        this.accent &= 0xFFFFFF;
    }

    public void save() {
        this.validate();
        try {
            Files.createDirectories(PATH.getParent(), new FileAttribute[0]);
            Path temp = PATH.resolveSibling("shoremod.json.tmp");
            Files.writeString(temp, (CharSequence)GSON.toJson((Object)this), new OpenOption[0]);
            Files.move(temp, PATH, StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException e) {
            ShoreClient.LOG.error("Could not save ShoreMod settings", (Throwable)e);
        }
    }

    public int panelColor() {
        return (int)((double)this.panelOpacity * 2.55) << 24 | 0x131A24;
    }
}

