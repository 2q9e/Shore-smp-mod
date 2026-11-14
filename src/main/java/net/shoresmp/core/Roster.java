/*
 * Decompiled with CFR 0.152.
 */
package net.shoresmp.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.shoresmp.core.Wire;

public final class Roster {
    private long generation = Long.MIN_VALUE;
    private int expected;
    private final Map<Integer, List<Wire.User>> pending = new HashMap<Integer, List<Wire.User>>();
    private Map<UUID, Wire.User> users = Map.of();

    public boolean accept(Wire.Message message) {
        if (message.op() != 1 || message.generation() < this.generation) {
            return false;
        }
        if (message.generation() > this.generation) {
            this.generation = message.generation();
            this.expected = message.pages();
            this.pending.clear();
        }
        if (message.pages() != this.expected) {
            return false;
        }
        this.pending.put(message.page(), message.users());
        if (this.pending.size() != this.expected) {
            return false;
        }
        HashMap next = new HashMap();
        this.pending.values().forEach(page -> page.forEach(u -> next.put(u.id(), u)));
        this.users = Map.copyOf(next);
        return true;
    }

    public Map<UUID, Wire.User> users() {
        return this.users;
    }

    public void clear() {
        this.generation = Long.MIN_VALUE;
        this.expected = 0;
        this.pending.clear();
        this.users = Map.of();
    }
}

