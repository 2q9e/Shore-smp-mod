/*
 * Decompiled with CFR 0.152.
 */
package net.shoresmp.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class Wire {
    public static final String CHANNEL = "shoremod:wire";
    public static final int VERSION = 1;
    public static final int HELLO = 0;
    public static final int ROSTER = 1;
    public static final int CLIP = 2;
    public static final int MAX_BYTES = 30000;
    public static final int PAGE_SIZE = 500;

    private static byte[] encode(int op, Writer writer) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(bytes);
            out.writeByte(1);
            out.writeByte(op);
            writer.write(out);
            out.flush();
            if (bytes.size() > 30000) {
                throw new IllegalArgumentException("Message too large");
            }
            return bytes.toByteArray();
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static byte[] hello() {
        return Wire.encode(0, out -> {});
    }

    public static byte[] clip(String reason) {
        return Wire.encode(2, out -> out.writeUTF(reason.substring(0, Math.min(1000, reason.length()))));
    }

    public static List<byte[]> roster(long generation, Collection<User> users) {
        List<User> list = users.stream().sorted(Comparator.comparing(u -> u.id().toString())).toList();
        int pages = Math.max(1, (list.size() + 500 - 1) / 500);
        if (pages > 128) {
            throw new IllegalArgumentException("Too many users");
        }
        ArrayList<byte[]> result = new ArrayList<byte[]>();
        for (int p = 0; p < pages; ++p) {
            int page = p;
            List<User> slice = list.subList(p * 500, Math.min(list.size(), (p + 1) * 500));
            result.add(Wire.encode(1, out -> {
                out.writeLong(generation);
                out.writeShort(page);
                out.writeShort(pages);
                out.writeShort(slice.size());
                for (User user : slice) {
                    out.writeLong(user.id().getMostSignificantBits());
                    out.writeLong(user.id().getLeastSignificantBits());
                    out.writeUTF(user.name());
                }
            }));
        }
        return List.copyOf(result);
    }

    public static Message decode(byte[] data) throws IOException {
        if (data.length < 2 || data.length > 30000) {
            throw new IOException("Invalid payload length");
        }
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));
        if (in.readUnsignedByte() != 1) {
            throw new IOException("Protocol version mismatch");
        }
        int op = in.readUnsignedByte();
        long generation = 0L;
        int page = 0;
        int pages = 0;
        String reason = "";
        ArrayList<User> users = new ArrayList<User>();
        if (op == 1) {
            generation = in.readLong();
            page = in.readUnsignedShort();
            pages = in.readUnsignedShort();
            int count = in.readUnsignedShort();
            if (pages < 1 || pages > 128 || page >= pages || count > 500) {
                throw new IOException("Invalid roster page");
            }
            try {
                for (int i = 0; i < count; ++i) {
                    users.add(new User(new UUID(in.readLong(), in.readLong()), in.readUTF()));
                }
            }
            catch (IllegalArgumentException e) {
                throw new IOException(e);
            }
        } else if (op == 2) {
            reason = in.readUTF();
            if (reason.length() > 1000) {
                throw new IOException("Reason too long");
            }
        } else if (op != 0) {
            throw new IOException("Unknown opcode");
        }
        if (in.available() != 0) {
            throw new IOException("Trailing payload data");
        }
        return new Message(op, generation, page, pages, List.copyOf(users), reason);
    }

    private static interface Writer {
        public void write(DataOutputStream var1) throws IOException;
    }

    public record User(UUID id, String name) {
        public User {
            Objects.requireNonNull(id);
            if (name == null || !name.matches("[A-Za-z0-9_]{1,16}")) {
                throw new IllegalArgumentException("Invalid player name");
            }
        }
    }

    public record Message(int op, long generation, int page, int pages, List<User> users, String reason) {
    }
}

