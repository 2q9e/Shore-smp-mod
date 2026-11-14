/*
 * Decompiled with CFR 0.152.
 */
package net.shoresmp.core;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public final class FrameRing {
    private final Deque<Frame> frames = new ArrayDeque<Frame>();
    private final long durationNanos;
    private final long maxBytes;
    private long bytes;

    public FrameRing(long durationNanos, long maxBytes) {
        this.durationNanos = durationNanos;
        this.maxBytes = maxBytes;
    }

    public synchronized void add(Frame frame) {
        if (!(this.frames.isEmpty() || this.frames.getLast().width() == frame.width() && this.frames.getLast().height() == frame.height())) {
            this.clear();
        }
        if (!this.frames.isEmpty() && frame.nanos() <= this.frames.getLast().nanos()) {
            return;
        }
        this.frames.addLast(frame);
        this.bytes += (long)frame.jpeg().length;
        while (!(this.frames.isEmpty() || this.bytes <= this.maxBytes && frame.nanos() - this.frames.getFirst().nanos() <= this.durationNanos)) {
            this.bytes -= (long)this.frames.removeFirst().jpeg().length;
        }
    }

    public synchronized List<Frame> snapshot(long endNanos) {
        return this.frames.stream().filter(f -> f.nanos() <= endNanos && endNanos - f.nanos() <= this.durationNanos).toList();
    }

    public synchronized void clear() {
        this.frames.clear();
        this.bytes = 0L;
    }

    public record Frame(long nanos, byte[] jpeg, int width, int height) {
    }
}

