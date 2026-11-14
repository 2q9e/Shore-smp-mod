/*
 * Decompiled with CFR 0.152.
 */
package net.shoresmp.core;

public final class BrowserStartup {
    public static State state(boolean initialized, boolean downloaded, boolean downloadFailed, boolean initializationAllowed, boolean dawnStaffBlock, long waitingNanos) {
        if (initialized) {
            return State.READY;
        }
        if (dawnStaffBlock && !initializationAllowed) {
            return State.INCOMPATIBLE;
        }
        if (downloadFailed) {
            return State.FAILED;
        }
        if (!downloaded) {
            return State.DOWNLOADING;
        }
        if (!initializationAllowed) {
            return State.FAILED;
        }
        return waitingNanos >= 20000000000L ? State.TIMED_OUT : State.STARTING;
    }

    public static enum State {
        READY,
        DOWNLOADING,
        STARTING,
        INCOMPATIBLE,
        FAILED,
        TIMED_OUT;

    }
}

