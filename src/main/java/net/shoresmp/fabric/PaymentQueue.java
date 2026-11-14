/*
 * Decompiled with CFR 0.152.
 */
package net.shoresmp.fabric;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import net.minecraft.class_310;
import net.minecraft.class_634;
import net.minecraft.class_640;
import net.shoresmp.core.Payments;
import net.shoresmp.fabric.ShoreClient;

public final class PaymentQueue {
    private final Deque<Payments.Payment> pending = new ArrayDeque<Payments.Payment>();
    private class_634 connection;
    private long nextSend;
    private long sessionEpoch;
    private int sent;
    private int skipped;

    public boolean running() {
        return !this.pending.isEmpty();
    }

    public void start(List<Payments.Payment> plan) {
        class_310 client = class_310.method_1551();
        if (this.running()) {
            throw new IllegalArgumentException("A payment queue is already active. Use /paycancel first.");
        }
        if (client.method_1562() == null || ShoreClient.lastRosterAt == 0L) {
            throw new IllegalArgumentException("Connect to a server with the ShoreMod plugin first.");
        }
        this.pending.addAll(plan);
        this.connection = client.method_1562();
        this.sessionEpoch = ShoreClient.sessionEpoch;
        this.nextSend = 0L;
        this.skipped = 0;
        this.sent = 0;
        ShoreClient.tell("Payment queue started: $" + String.valueOf(Payments.total(plan)) + " maximum. /paycancel stops remaining payments.");
    }

    public void tick(class_310 client) {
        if (!this.running()) {
            return;
        }
        if (client.method_1562() != this.connection || this.sessionEpoch != ShoreClient.sessionEpoch || client.field_1724 == null || ShoreClient.lastRosterAt == 0L) {
            this.cancel(true);
            return;
        }
        long now = System.nanoTime();
        if (now < this.nextSend) {
            return;
        }
        while (!this.pending.isEmpty()) {
            Payments.Payment payment = this.pending.removeFirst();
            class_640 entry = this.connection.method_2871(payment.user().id());
            if (entry == null || !ShoreClient.roster.users().containsKey(payment.user().id()) || !entry.method_2966().name().equals(payment.user().name())) {
                ++this.skipped;
                continue;
            }
            try {
                this.connection.method_45730("pay " + payment.user().name() + " " + payment.amount().toPlainString());
            }
            catch (RuntimeException e) {
                ShoreClient.LOG.warn("Payment command send failed; cancelling without retry", (Throwable)e);
                this.cancel(false);
                ShoreClient.tellError("Payment queue stopped after a send error. Check your balance and server messages before starting again.");
                return;
            }
            ++this.sent;
            this.nextSend = now + 2000000000L;
            break;
        }
        if (this.pending.isEmpty()) {
            ShoreClient.tell("Finished: " + this.sent + " /pay commands sent, " + this.skipped + " players skipped. Check the server's payment responses.");
        }
    }

    public void cancel(boolean notify) {
        int count = this.pending.size();
        this.pending.clear();
        this.connection = null;
        if (notify) {
            ShoreClient.tell("Cancelled " + count + " remaining payments. Already-sent payments cannot be undone.");
        }
    }

    public String status() {
        return this.sent + " commands sent; " + this.pending.size() + " queued; " + this.skipped + " skipped.";
    }
}

