/*
 * Decompiled with CFR 0.152.
 */
package net.shoresmp.fabric;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.class_124;
import net.minecraft.class_156;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_3675;
import net.minecraft.class_437;
import net.minecraft.class_8710;
import net.shoresmp.core.ModUpdates;
import net.shoresmp.core.Roster;
import net.shoresmp.core.Wire;
import net.shoresmp.fabric.ClipRecorder;
import net.shoresmp.fabric.Config;
import net.shoresmp.fabric.PaymentQueue;
import net.shoresmp.fabric.ShoreCommands;
import net.shoresmp.fabric.ShoreCompatSmoke;
import net.shoresmp.fabric.ShoreHud;
import net.shoresmp.fabric.ShorePayload;
import net.shoresmp.fabric.ShorePvp;
import net.shoresmp.fabric.ShoreScreen;
import net.shoresmp.fabric.ShoreSelfTest;
import net.shoresmp.fabric.ShoreTools;
import net.shoresmp.fabric.UpdatePayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ShoreClient
implements ClientModInitializer {
    public static final Logger LOG = LoggerFactory.getLogger((String)"ShoreMod");
    public static Config config;
    public static final Roster roster;
    public static ClipRecorder recorder;
    public static final PaymentQueue payments;
    public static long lastRosterAt;
    public static long sessionEpoch;
    private static final AtomicReference<Supplier<class_437>> pendingScreen;
    private long nextHello;
    private static long nextUpdateCheck;
    public static ModUpdates.Notice latestUpdate;
    private static String announcedUpdate;
    private Double originalChatOpacity;
    public static class_304 menuKey;
    public static class_304 clipKey;
    private static final class_304.class_11900 CATEGORY;

    public static void openNextTick(Supplier<class_437> screen) {
        pendingScreen.set(screen);
    }

    public static String version() {
        return ((ModContainer)FabricLoader.getInstance().getModContainer("shoremod").orElseThrow()).getMetadata().getVersion().getFriendlyString();
    }

    public static boolean updateAvailable() {
        return latestUpdate != null && ModUpdates.newer((String)latestUpdate.version(), (String)ShoreClient.version());
    }

    public static void checkUpdates() {
        nextUpdateCheck = 0L;
    }

    public static void openUpdate() {
        if (latestUpdate != null && !latestUpdate.url().isEmpty()) {
            class_156.method_668().method_673(URI.create(latestUpdate.url()));
        }
    }

    public static String keyName(class_304 key) {
        return key == null || key.method_1415() ? "Unbound" : key.method_16007().getString();
    }

    public void onInitializeClient() {
        config = Config.load();
        if (Boolean.getBoolean("shoremod.compatSmoke")) {
            ShoreCompatSmoke.install();
        }
        recorder = new ClipRecorder();
        class_304 menu = menuKey = this.key("menu", 344);
        class_304 clip = clipKey = this.key("clip", 298);
        ShoreTools.zoomKey = this.key("zoom", 67);
        ShoreHud.register();
        ShoreTools.register();
        ShorePvp.register();
        PayloadTypeRegistry.playC2S().register(UpdatePayload.ID, UpdatePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(UpdatePayload.ID, UpdatePayload.CODEC);
        ClientPlayNetworking.registerGlobalReceiver(UpdatePayload.ID, (payload, context) -> {
            try {
                ModUpdates.Notice notice = ModUpdates.decode((byte[])payload.bytes());
                context.client().execute(() -> {
                    latestUpdate = notice;
                    if (ShoreClient.updateAvailable() && !notice.version().equals(announcedUpdate)) {
                        announcedUpdate = notice.version();
                        ShoreClient.tell("Update " + notice.version() + " is available. You have " + ShoreClient.version() + ". Open /shore > Updates.");
                    }
                });
            }
            catch (IOException e) {
                LOG.warn("Invalid update announcement", (Throwable)e);
            }
        });
        PayloadTypeRegistry.playC2S().register(ShorePayload.ID, ShorePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ShorePayload.ID, ShorePayload.CODEC);
        ClientPlayNetworking.registerGlobalReceiver(ShorePayload.ID, (payload, context) -> {
            try {
                Wire.Message message = Wire.decode((byte[])payload.bytes());
                if (message.op() == 0) {
                    ++sessionEpoch;
                    roster.clear();
                    lastRosterAt = 0L;
                    payments.cancel(false);
                } else if (message.op() == 1 && roster.accept(message)) {
                    lastRosterAt = System.nanoTime();
                } else if (message.op() == 2) {
                    recorder.trigger(message.reason(), true);
                }
            }
            catch (IOException e) {
                LOG.warn("Invalid ShoreMod server payload", (Throwable)e);
            }
        });
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            ++sessionEpoch;
            latestUpdate = null;
            announcedUpdate = "";
            nextUpdateCheck = 0L;
            roster.clear();
            lastRosterAt = 0L;
            this.nextHello = 0L;
            payments.cancel(false);
            recorder.beginSession();
            ShoreTools.joined(client);
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            ++sessionEpoch;
            pendingScreen.set(null);
            latestUpdate = null;
            announcedUpdate = "";
            payments.cancel(false);
            roster.clear();
            lastRosterAt = 0L;
        });
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> recorder.trigger(message.getString(), false));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            Supplier screen = pendingScreen.getAndSet(null);
            if (screen != null) {
                client.method_1507((class_437)screen.get());
            }
            while (menu.method_1436()) {
                client.method_1507((class_437)new ShoreScreen(client.field_1755));
            }
            while (clip.method_1436()) {
                recorder.trigger("Manually saved gameplay", true);
            }
            long now = System.nanoTime();
            if (client.method_1562() != null && now >= nextUpdateCheck && ClientPlayNetworking.canSend(UpdatePayload.ID)) {
                ClientPlayNetworking.send((class_8710)new UpdatePayload(ModUpdates.request()));
                nextUpdateCheck = now + 60000000000L;
            }
            if (client.method_1562() != null && now >= this.nextHello && ClientPlayNetworking.canSend(ShorePayload.ID)) {
                ClientPlayNetworking.send((class_8710)new ShorePayload(Wire.hello()));
                this.nextHello = now + 10000000000L;
            }
            if (lastRosterAt != 0L && now - lastRosterAt > 35000000000L) {
                roster.clear();
                lastRosterAt = 0L;
            }
            payments.tick(client);
            ShoreTools.tick(client);
            ShorePvp.tick(client);
            if (ShoreClient.config.extendedChat) {
                client.field_1690.method_42550().method_41748(ShoreClient.config.chatOpacity);
            } else if (this.originalChatOpacity != null) {
                client.field_1690.method_42550().method_41748(this.originalChatOpacity);
            }
        });
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            this.originalChatOpacity = (Double)client.field_1690.method_42550().method_41753();
        });
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            recorder.close();
            ShoreTools.restoreZoomInput(client);
            if (this.originalChatOpacity != null) {
                client.field_1690.method_42550().method_41748(this.originalChatOpacity);
            }
        });
        ShoreCommands.register();
        ShoreSelfTest.register();
    }

    private class_304 key(String name, int code) {
        return KeyBindingHelper.registerKeyBinding((class_304)new class_304("key.shoremod." + name, class_3675.class_307.field_1668, code, CATEGORY));
    }

    public static void tell(String value) {
        ShoreClient.message(value, 15397623);
    }

    public static void tellError(String value) {
        ShoreClient.message(value, 0xFF9696);
    }

    private static void message(String value, int color) {
        class_310 client = class_310.method_1551();
        client.execute(() -> client.field_1705.method_1743().method_1812((class_2561)class_2561.method_43473().method_10852((class_2561)class_2561.method_43470((String)"SHOREMOD").method_54663(ShoreClient.config.accent).method_27692(class_124.field_1067)).method_10852((class_2561)class_2561.method_43470((String)" \u00bb ").method_54663(8360606)).method_10852((class_2561)class_2561.method_43470((String)value).method_54663(color))));
    }

    static {
        roster = new Roster();
        payments = new PaymentQueue();
        pendingScreen = new AtomicReference();
        announcedUpdate = "";
        CATEGORY = class_304.class_11900.method_74698((class_2960)class_2960.method_60655((String)"shoremod", (String)"controls"));
    }
}

