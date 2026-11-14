/*
 * Decompiled with CFR 0.152.
 */
package net.shoresmp.core;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.shoresmp.core.FrameRing;

public final class MjpegAvi {
    public static void write(Path target, List<FrameRing.Frame> source, int fps, long endNanos) throws IOException {
        if (source.isEmpty()) {
            throw new IOException("No gameplay frames buffered yet");
        }
        if (fps < 1 || fps > 60) {
            throw new IllegalArgumentException("FPS");
        }
        int width = source.getFirst().width();
        int height = source.getFirst().height();
        long start = source.getFirst().nanos();
        int count = Math.max(1, Math.min(fps * 30, (int)Math.ceil((double)(endNanos - start) / 1.0E9 * (double)fps)));
        ArrayList<FrameRing.Frame> frames = new ArrayList<FrameRing.Frame>();
        int index = 0;
        for (int i = 0; i < count; ++i) {
            long time = start + (long)i * 1000000000L / (long)fps;
            while (index + 1 < source.size() && source.get(index + 1).nanos() <= time) {
                ++index;
            }
            frames.add(source.get(index));
        }
        int max = source.stream().mapToInt(f -> f.jpeg().length).max().orElseThrow();
        try (RandomAccessFile out = new RandomAccessFile(target.toFile(), "rw");){
            out.setLength(0L);
            MjpegAvi.four(out, "RIFF");
            long riffSize = out.getFilePointer();
            MjpegAvi.le(out, 0);
            MjpegAvi.four(out, "AVI ");
            long hdrl = MjpegAvi.list(out, "hdrl");
            MjpegAvi.four(out, "avih");
            MjpegAvi.le(out, 56);
            MjpegAvi.le(out, 1000000 / fps);
            MjpegAvi.le(out, max * fps);
            MjpegAvi.le(out, 0);
            MjpegAvi.le(out, 16);
            MjpegAvi.le(out, count);
            MjpegAvi.le(out, 0);
            MjpegAvi.le(out, 1);
            MjpegAvi.le(out, max);
            MjpegAvi.le(out, width);
            MjpegAvi.le(out, height);
            for (int i = 0; i < 4; ++i) {
                MjpegAvi.le(out, 0);
            }
            long strl = MjpegAvi.list(out, "strl");
            MjpegAvi.four(out, "strh");
            MjpegAvi.le(out, 56);
            MjpegAvi.four(out, "vids");
            MjpegAvi.four(out, "MJPG");
            MjpegAvi.le(out, 0);
            MjpegAvi.shortLE(out, 0);
            MjpegAvi.shortLE(out, 0);
            MjpegAvi.le(out, 0);
            MjpegAvi.le(out, 1);
            MjpegAvi.le(out, fps);
            MjpegAvi.le(out, 0);
            MjpegAvi.le(out, count);
            MjpegAvi.le(out, max);
            MjpegAvi.le(out, -1);
            MjpegAvi.le(out, 0);
            MjpegAvi.shortLE(out, 0);
            MjpegAvi.shortLE(out, 0);
            MjpegAvi.shortLE(out, width);
            MjpegAvi.shortLE(out, height);
            MjpegAvi.four(out, "strf");
            MjpegAvi.le(out, 40);
            MjpegAvi.le(out, 40);
            MjpegAvi.le(out, width);
            MjpegAvi.le(out, height);
            MjpegAvi.shortLE(out, 1);
            MjpegAvi.shortLE(out, 24);
            MjpegAvi.four(out, "MJPG");
            MjpegAvi.le(out, width * height * 3);
            for (int i = 0; i < 4; ++i) {
                MjpegAvi.le(out, 0);
            }
            MjpegAvi.endList(out, strl);
            MjpegAvi.endList(out, hdrl);
            long movi = MjpegAvi.list(out, "movi");
            ArrayList<Integer> offsets = new ArrayList<Integer>();
            for (FrameRing.Frame frame : frames) {
                offsets.add(Math.toIntExact(out.getFilePointer() - (movi + 4L)));
                MjpegAvi.four(out, "00dc");
                MjpegAvi.le(out, frame.jpeg().length);
                out.write(frame.jpeg());
                if ((frame.jpeg().length & 1) == 0) continue;
                out.write(0);
            }
            MjpegAvi.endList(out, movi);
            MjpegAvi.four(out, "idx1");
            MjpegAvi.le(out, frames.size() * 16);
            for (int i = 0; i < frames.size(); ++i) {
                MjpegAvi.four(out, "00dc");
                MjpegAvi.le(out, 16);
                MjpegAvi.le(out, (Integer)offsets.get(i));
                MjpegAvi.le(out, ((FrameRing.Frame)frames.get(i)).jpeg().length);
            }
            long length = out.length();
            out.seek(riffSize);
            MjpegAvi.le(out, Math.toIntExact(length - 8L));
        }
    }

    private static long list(RandomAccessFile out, String type) throws IOException {
        MjpegAvi.four(out, "LIST");
        long size = out.getFilePointer();
        MjpegAvi.le(out, 0);
        MjpegAvi.four(out, type);
        return size;
    }

    private static void endList(RandomAccessFile out, long at) throws IOException {
        long end = out.getFilePointer();
        out.seek(at);
        MjpegAvi.le(out, Math.toIntExact(end - at - 4L));
        out.seek(end);
    }

    private static void four(RandomAccessFile out, String text) throws IOException {
        out.writeBytes(text);
    }

    private static void le(RandomAccessFile out, int value) throws IOException {
        out.writeInt(Integer.reverseBytes(value));
    }

    private static void shortLE(RandomAccessFile out, int value) throws IOException {
        out.writeShort(Short.reverseBytes((short)value));
    }
}

