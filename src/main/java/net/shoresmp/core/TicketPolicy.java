/*
 * Decompiled with CFR 0.152.
 */
package net.shoresmp.core;

import java.net.URI;
import java.util.Locale;

public final class TicketPolicy {
    public static final String HOME = "https://www.shoresmp.net/support";

    public static boolean allowed(String value) {
        if (value == null) {
            return false;
        }
        try {
            URI uri = URI.create(value);
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null || uri.getRawUserInfo() != null || uri.getPort() != -1 && uri.getPort() != 443) {
                return false;
            }
            String host = uri.getHost().toLowerCase(Locale.ROOT);
            String path = uri.normalize().getPath();
            if (host.equals("tickets.shoresmp.net") || host.equals("portal.shoresmp.net")) {
                return true;
            }
            if (host.equals("shoresmp.net") || host.equals("www.shoresmp.net")) {
                return path.equals("/support") || path.startsWith("/support/") || path.startsWith("/api/v1/auth/handoff/");
            }
            return host.equals("discord.com") && (path.equals("/login") || path.startsWith("/oauth2/") || path.startsWith("/api/oauth2/"));
        }
        catch (IllegalArgumentException e) {
            return false;
        }
    }
}

