/*
 * Decompiled with CFR 0.152.
 */
package net.shoresmp.fabric;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_437;
import net.minecraft.class_634;
import net.shoresmp.core.Payments;
import net.shoresmp.fabric.ShoreClient;
import net.shoresmp.fabric.ShoreDialog;

public final class PaymentScreen
extends ShoreDialog {
    private static final int PER_PAGE = 5;
    private final List<Payments.Payment> plan;
    private final class_634 connection;
    private final long sessionEpoch = ShoreClient.sessionEpoch;
    private final long createdAt = System.nanoTime();
    private int page;

    public PaymentScreen(class_437 parent, List<Payments.Payment> plan, class_634 connection) {
        super("Confirm payments", parent);
        this.plan = List.copyOf(plan);
        this.connection = connection;
    }

    private int pages() {
        return Math.max(1, (this.plan.size() + 5 - 1) / 5);
    }

    @Override
    protected String icon() {
        return "check";
    }

    @Override
    protected String subtitle() {
        return "Up to $" + String.valueOf(Payments.total(this.plan)) + " across " + this.plan.size() + " player" + (this.plan.size() == 1 ? "" : "s");
    }

    @Override
    protected List<ShoreDialog.Line> lines() {
        ArrayList<ShoreDialog.Line> out = new ArrayList<ShoreDialog.Line>();
        for (int i = this.page * 5; i < Math.min(this.plan.size(), this.page * 5 + 5); ++i) {
            Payments.Payment p = this.plan.get(i);
            out.add(new ShoreDialog.Line(p.user().name() + "   $" + String.valueOf(p.amount()), -723465, false));
        }
        while (out.size() < Math.min(5, this.plan.size())) {
            out.add(new ShoreDialog.Line("", -723465, false));
        }
        out.add(new ShoreDialog.Line((String)(this.pages() > 1 ? "Page " + (this.page + 1) + " of " + this.pages() + "  \u00b7  " : "") + "One command every 2 seconds. No automatic retries.", -6577750, false));
        return out;
    }

    @Override
    protected List<ShoreDialog.Button> buttons() {
        ArrayList<ShoreDialog.Button> out = new ArrayList<ShoreDialog.Button>();
        if (this.pages() > 1) {
            out.add(new ShoreDialog.Button("Previous", 0, () -> {
                if (this.page > 0) {
                    --this.page;
                }
            }));
            out.add(new ShoreDialog.Button("Next", 0, () -> {
                if (this.page + 1 < this.pages()) {
                    ++this.page;
                }
            }));
        }
        out.add(new ShoreDialog.Button("Cancel", 0, this::method_25419));
        out.add(new ShoreDialog.Button("Start payments", 1, () -> {
            try {
                if (this.field_22787.method_1562() != this.connection || this.sessionEpoch != ShoreClient.sessionEpoch) {
                    throw new IllegalArgumentException("Server session changed; create a new payment plan.");
                }
                if (System.nanoTime() - this.createdAt > 60000000000L) {
                    throw new IllegalArgumentException("Payment preview expired; create a new payment plan.");
                }
                ShoreClient.payments.start(this.plan);
                this.method_25419();
            }
            catch (IllegalArgumentException e) {
                ShoreClient.tellError(e.getMessage());
                this.method_25419();
            }
        }));
        return out;
    }
}

