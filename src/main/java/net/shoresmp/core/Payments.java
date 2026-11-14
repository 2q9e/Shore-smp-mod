/*
 * Decompiled with CFR 0.152.
 */
package net.shoresmp.core;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.shoresmp.core.Wire;

public final class Payments {
    public static BigDecimal amount(String input) {
        if (!input.matches("[0-9]{1,12}(\\.[0-9]{1,2})?")) {
            throw new IllegalArgumentException("Use a positive amount with up to 2 decimals");
        }
        BigDecimal value = new BigDecimal(input).setScale(2);
        if (value.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        return value;
    }

    public static List<Payment> each(List<Wire.User> users, BigDecimal amount) {
        Payments.requireUsers(users);
        return users.stream().map(u -> new Payment((Wire.User)u, amount)).toList();
    }

    public static List<Payment> split(List<Wire.User> users, BigDecimal total, Random random) {
        Payments.requireUsers(users);
        long cents = total.movePointRight(2).longValueExact();
        if (cents < (long)users.size()) {
            throw new IllegalArgumentException("Budget must cover at least $0.01 per recipient");
        }
        long remaining = cents;
        ArrayList<Payment> result = new ArrayList<Payment>();
        for (int i = 0; i < users.size(); ++i) {
            long share;
            int left = users.size() - i;
            if (left == 1) {
                share = remaining;
            } else if (random == null) {
                share = cents / (long)users.size() + (long)((long)i < cents % (long)users.size() ? 1 : 0);
            } else {
                long cap = Math.min(remaining - (long)left + 1L, Math.max(1L, remaining / (long)left * 2L));
                share = random.nextLong(cap) + 1L;
            }
            result.add(new Payment(users.get(i), BigDecimal.valueOf(share, 2)));
            remaining -= share;
        }
        return List.copyOf(result);
    }

    public static BigDecimal total(List<Payment> plan) {
        return plan.stream().map(Payment::amount).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2);
    }

    private static void requireUsers(List<Wire.User> users) {
        if (users.isEmpty()) {
            throw new IllegalArgumentException("No eligible ShoreMod users in tab");
        }
        if (users.stream().map(Wire.User::id).distinct().count() != (long)users.size()) {
            throw new IllegalArgumentException("Duplicate recipients");
        }
    }

    public record Payment(Wire.User user, BigDecimal amount) {
    }
}

