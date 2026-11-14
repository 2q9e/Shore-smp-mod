/*
 * Decompiled with CFR 0.152.
 */
package net.shoresmp.fabric;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.class_310;
import net.shoresmp.core.Payments;
import net.shoresmp.core.Wire;
import net.shoresmp.fabric.PaymentScreen;
import net.shoresmp.fabric.ShoreClient;
import net.shoresmp.fabric.ShoreScreen;
import net.shoresmp.fabric.TicketsScreen;

public final class ShoreCommands {
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registry) -> {
            dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)ClientCommandManager.literal((String)"shore").executes(ctx -> {
                ShoreClient.openNextTick(() -> new ShoreScreen(null));
                return 1;
            })).then(((LiteralArgumentBuilder)ClientCommandManager.literal((String)"deaths").executes(ctx -> ShoreCommands.deaths())).then(ClientCommandManager.literal((String)"clear").executes(ctx -> {
                ShoreClient.config.deaths.clear();
                ShoreClient.config.save();
                ShoreClient.tell("Death history cleared.");
                return 1;
            }))));
            dispatcher.register((LiteralArgumentBuilder)ClientCommandManager.literal((String)"tickets").executes(ctx -> {
                TicketsScreen.open();
                return 1;
            }));
            dispatcher.register((LiteralArgumentBuilder)ClientCommandManager.literal((String)"paycancel").executes(ctx -> {
                ShoreClient.payments.cancel(true);
                return 1;
            }));
            dispatcher.register((LiteralArgumentBuilder)ClientCommandManager.literal((String)"paystatus").executes(ctx -> {
                ShoreClient.tell(ShoreClient.payments.status());
                return 1;
            }));
            dispatcher.register((LiteralArgumentBuilder)ClientCommandManager.literal((String)"payall").then(ClientCommandManager.argument((String)"amount", (ArgumentType)StringArgumentType.word()).executes(ctx -> ShoreCommands.plan("all", StringArgumentType.getString((CommandContext)ctx, (String)"amount"), 0))));
            dispatcher.register((LiteralArgumentBuilder)ClientCommandManager.literal((String)"splitpay").then(ClientCommandManager.argument((String)"total", (ArgumentType)StringArgumentType.word()).executes(ctx -> ShoreCommands.plan("split", StringArgumentType.getString((CommandContext)ctx, (String)"total"), 0))));
            dispatcher.register((LiteralArgumentBuilder)ClientCommandManager.literal((String)"payrandom").then(((RequiredArgumentBuilder)ClientCommandManager.argument((String)"amount", (ArgumentType)StringArgumentType.word()).executes(ctx -> ShoreCommands.plan("random", StringArgumentType.getString((CommandContext)ctx, (String)"amount"), 1))).then(ClientCommandManager.argument((String)"count", (ArgumentType)IntegerArgumentType.integer((int)1, (int)10000)).executes(ctx -> ShoreCommands.plan("random", StringArgumentType.getString((CommandContext)ctx, (String)"amount"), IntegerArgumentType.getInteger((CommandContext)ctx, (String)"count"))))));
            dispatcher.register((LiteralArgumentBuilder)ClientCommandManager.literal((String)"payrain").then(ClientCommandManager.argument((String)"total", (ArgumentType)StringArgumentType.word()).then(ClientCommandManager.argument((String)"winners", (ArgumentType)IntegerArgumentType.integer((int)1, (int)10000)).executes(ctx -> ShoreCommands.plan("rain", StringArgumentType.getString((CommandContext)ctx, (String)"total"), IntegerArgumentType.getInteger((CommandContext)ctx, (String)"winners"))))));
        });
    }

    private static int deaths() {
        List<String> deaths = ShoreClient.config.deaths;
        if (deaths.isEmpty()) {
            ShoreClient.tell("No deaths recorded yet.");
            return 1;
        }
        ShoreClient.tell("Recent deaths (newest first):");
        for (int i = 0; i < deaths.size(); ++i) {
            ShoreClient.tell(i + 1 + ".  " + deaths.get(i));
        }
        return 1;
    }

    private static int plan(String mode, String value, int count) {
        try {
            class_310 client = class_310.method_1551();
            if (client.field_1724 == null || client.method_1562() == null || ShoreClient.lastRosterAt == 0L) {
                throw new IllegalArgumentException("The ShoreMod plugin has not supplied an online roster.");
            }
            if (ShoreClient.payments.running()) {
                throw new IllegalArgumentException("Use /paycancel before creating another payment queue.");
            }
            ArrayList<Wire.User> users = new ArrayList<Wire.User>(ShoreClient.roster.users().values().stream().filter(u -> !u.id().equals(client.field_1724.method_5667())).filter(u -> client.method_1562().method_2871(u.id()) != null).sorted(Comparator.comparing(Wire.User::name)).toList());
            BigDecimal amount = Payments.amount((String)value);
            if (mode.equals("random") || mode.equals("rain")) {
                if (count > users.size()) {
                    throw new IllegalArgumentException("Only " + users.size() + " eligible players are in tab.");
                }
                Collections.shuffle(users);
                users = new ArrayList<Wire.User>(users.subList(0, count));
            }
            List payments = switch (mode) {
                case "split" -> Payments.split(users, (BigDecimal)amount, null);
                case "rain" -> Payments.split(users, (BigDecimal)amount, (Random)new Random());
                default -> Payments.each(users, (BigDecimal)amount);
            };
            PaymentScreen preview = new PaymentScreen(null, payments, client.method_1562());
            ShoreClient.openNextTick(() -> preview);
            return 1;
        }
        catch (IllegalArgumentException e) {
            ShoreClient.tellError(e.getMessage());
            return 0;
        }
    }
}

