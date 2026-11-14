/*
 * Decompiled with CFR 0.152.
 */
package net.shoresmp.fabric;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import net.minecraft.class_1011;
import net.minecraft.class_276;
import net.minecraft.class_310;
import net.minecraft.class_318;
import net.minecraft.class_437;
import net.shoresmp.core.FrameRing;
import net.shoresmp.core.MjpegAvi;
import net.shoresmp.core.PunishmentMatcher;
import net.shoresmp.core.PunishmentTarget;
import net.shoresmp.fabric.ClipSavedScreen;
import net.shoresmp.fabric.ShoreClient;
import net.shoresmp.fabric.ShoreScreen;
import net.shoresmp.fabric.TicketsScreen;

public final class ClipRecorder
implements AutoCloseable {
    private final FrameRing ring = new FrameRing(30000000000L, 0x6000000L);
    private final ExecutorService frames = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "ShoreMod-frames");
        t.setDaemon(true);
        return t;
    });
    private final ExecutorService exports = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "ShoreMod-export");
        t.setDaemon(true);
        return t;
    });
    private final AtomicBoolean capturePending = new AtomicBoolean();
    private final AtomicBoolean exporting = new AtomicBoolean();
    private final AtomicInteger epoch = new AtomicInteger();
    private long nextFrame;
    private long lastTrigger;
    private volatile long lastGameplay;
    private volatile Path lastClip;
    private volatile String status = "Buffer warming up";
    private volatile boolean closed;
    private String recordingSession;

    public Path directory() {
        return class_310.method_1551().field_1697.toPath().resolve("shoremod-clips");
    }

    public Path lastClip() {
        return this.lastClip;
    }

    public String status() {
        return ShoreClient.config.recording ? this.status : "Recording disabled";
    }

    public void beginSession() {
        class_310 client = class_310.method_1551();
        String server = client.method_1558() == null ? "singleplayer" : client.method_1558().field_3761;
        String session = String.valueOf(client.method_1548().method_44717()) + "@" + server.toLowerCase(Locale.ROOT);
        if (!session.equals(this.recordingSession) || System.nanoTime() - this.lastGameplay > 10000000000L) {
            this.clear();
        }
        this.recordingSession = session;
        ShoreClient.tell(ShoreClient.config.recording ? "Gameplay clip buffer enabled: last 30 seconds, stored locally. F9 saves; change recording in /shore." : "Gameplay recording is disabled. Change it in /shore.");
    }

    public synchronized void clear() {
        this.epoch.incrementAndGet();
        this.ring.clear();
        this.nextFrame = 0L;
    }

    private synchronized void addFrame(int captureEpoch, FrameRing.Frame frame) {
        if (!this.closed && captureEpoch == this.epoch.get()) {
            this.ring.add(frame);
        }
    }

    public void capture() {
        long now;
        class_310 client = class_310.method_1551();
        if (this.closed || !ShoreClient.config.recording || client.field_1687 == null) {
            return;
        }
        if (client.field_1755 instanceof TicketsScreen) {
            this.clear();
            return;
        }
        if (client.field_1755 instanceof ShoreScreen) {
            return;
        }
        this.lastGameplay = now = System.nanoTime();
        if (now < this.nextFrame || !this.capturePending.compareAndSet(false, true)) {
            return;
        }
        this.nextFrame = now + 1000000000L / (long)ShoreClient.config.clipFps;
        int captureEpoch = this.epoch.get();
        int maxWidth = ShoreClient.config.clipWidth;
        try {
            class_318.method_1663((class_276)client.method_1522(), image -> {
                if (this.closed) {
                    image.close();
                    this.capturePending.set(false);
                    return;
                }
                try {
                    this.frames.execute(() -> {
                        try (class_1011 class_10112 = image;){
                            if (captureEpoch != this.epoch.get()) {
                                return;
                            }
                            int w = Math.min(maxWidth, image.method_4307());
                            int h = Math.max(2, (int)((double)image.method_4323() * (double)w / (double)image.method_4307()));
                            w = Math.max(2, w / 2 * 2);
                            h = h / 2 * 2;
                            BufferedImage scaled = new BufferedImage(w, h, 1);
                            for (int y = 0; y < h; ++y) {
                                for (int x = 0; x < w; ++x) {
                                    scaled.setRGB(x, y, image.method_61940(x * image.method_4307() / w, y * image.method_4323() / h));
                                }
                            }
                            byte[] jpeg = this.compress(scaled);
                            this.addFrame(captureEpoch, new FrameRing.Frame(now, jpeg, w, h));
                            this.status = "Buffer active \u00b7 " + w + "\u00d7" + h + " \u00b7 " + ShoreClient.config.clipFps + " FPS";
                        }
                        catch (Exception e) {
                            this.status = "Capture error";
                            ShoreClient.LOG.warn("Clip frame failed", (Throwable)e);
                        }
                        finally {
                            this.capturePending.set(false);
                        }
                    });
                }
                catch (RejectedExecutionException e) {
                    image.close();
                    this.capturePending.set(false);
                }
            });
        }
        catch (RuntimeException e) {
            this.capturePending.set(false);
            ShoreClient.LOG.warn("Framebuffer capture failed", (Throwable)e);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private byte[] compress(BufferedImage image) throws IOException {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (MemoryCacheImageOutputStream out = new MemoryCacheImageOutputStream(bytes);){
            writer.setOutput(out);
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(2);
            param.setCompressionQuality(0.72f);
            writer.write(null, new IIOImage(image, null, null), param);
        }
        finally {
            writer.dispose();
        }
        return bytes.toByteArray();
    }

    public void disconnected(String reason) {
        if (ShoreClient.config.saveEveryDisconnect || PunishmentMatcher.matches((String)reason, ShoreClient.config.punishmentPhrases)) {
            this.trigger(reason, true);
        }
    }

    public void trigger(String reason, boolean explicit) {
        if (this.closed || !ShoreClient.config.recording) {
            return;
        }
        if (!explicit && !PunishmentTarget.matchesChat((String)reason, ShoreClient.config.punishmentPhrases, (String)class_310.method_1551().method_1548().method_1676())) {
            return;
        }
        long now = System.nanoTime();
        if (this.lastGameplay == 0L || now - this.lastGameplay > 15000000000L || this.lastTrigger != 0L && now - this.lastTrigger < 30000000000L || !this.exporting.compareAndSet(false, true)) {
            return;
        }
        this.lastTrigger = now;
        class_310 client = class_310.method_1551();
        String server = client.method_1558() == null ? "unknown" : client.method_1558().field_3761;
        boolean privacy = ShoreClient.config.privacy;
        int fps = ShoreClient.config.clipFps;
        int storage = ShoreClient.config.maxClipStorageMb;
        String ffmpeg = ShoreClient.config.ffmpegPath;
        String notificationSession = this.recordingSession;
        try {
            this.frames.execute(() -> {
                List snapshot = this.ring.snapshot(now);
                try {
                    this.exports.execute(() -> {
                        try {
                            if (snapshot.isEmpty()) {
                                throw new IOException("No gameplay frames buffered yet");
                            }
                            Files.createDirectories(this.directory(), new FileAttribute[0]);
                            String name = "shoremod-" + DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS").format(LocalDateTime.now());
                            Path avi = this.directory().resolve(name + ".avi");
                            Path temp = this.directory().resolve(name + ".avi.part");
                            MjpegAvi.write((Path)temp, (List)snapshot, (int)fps, (long)now);
                            Files.move(temp, avi, StandardCopyOption.REPLACE_EXISTING);
                            Path clip = this.convertIfConfigured(avi, ffmpeg);
                            double duration = Math.min(30.0, (double)(now - ((FrameRing.Frame)snapshot.getFirst()).nanos()) / 1.0E9);
                            Files.writeString(this.directory().resolve(name + ".txt"), (CharSequence)("ShoreMod gameplay evidence\nTime: " + String.valueOf(Instant.now()) + "\nReason: " + reason + "\nServer: " + (privacy ? "Hidden by privacy settings" : server) + "\nRecorded duration: " + String.format(Locale.ROOT, "%.2f", duration) + " seconds\nVideo only; no microphone or game audio.\n"), new OpenOption[0]);
                            this.lastClip = clip;
                            this.prune(storage);
                            client.execute(() -> {
                                ShoreClient.tell("Gameplay clip saved. Think your punishment was a mistake? Open a ticket and attach the clip.");
                                if (!this.closed && Objects.equals(notificationSession, this.recordingSession)) {
                                    client.method_1507((class_437)new ClipSavedScreen(client.field_1755, clip, duration));
                                }
                            });
                        }
                        catch (Exception e) {
                            ShoreClient.LOG.error("Clip export failed", (Throwable)e);
                            ShoreClient.tellError("Clip could not be saved: " + e.getMessage());
                        }
                        finally {
                            this.exporting.set(false);
                        }
                    });
                }
                catch (RejectedExecutionException e) {
                    this.exporting.set(false);
                }
            });
        }
        catch (RejectedExecutionException e) {
            this.exporting.set(false);
        }
    }

    private Path convertIfConfigured(Path avi, String executable) throws IOException, InterruptedException {
        if (executable.isBlank()) {
            return avi;
        }
        Path mp4 = avi.resolveSibling(avi.getFileName().toString().replace(".avi", ".mp4"));
        Path temp = mp4.resolveSibling(String.valueOf(mp4.getFileName()) + ".part.mp4");
        Process process = null;
        try {
            process = new ProcessBuilder(executable, "-nostdin", "-y", "-i", avi.toString(), "-c:v", "libx264", "-preset", "veryfast", "-crf", "23", "-pix_fmt", "yuv420p", "-movflags", "+faststart", temp.toString()).redirectOutput(ProcessBuilder.Redirect.DISCARD).redirectError(ProcessBuilder.Redirect.DISCARD).start();
            if (!process.waitFor(90L, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                throw new IOException("MP4 conversion timed out");
            }
            if (process.exitValue() != 0) {
                throw new IOException("FFmpeg conversion failed");
            }
            Files.move(temp, mp4, StandardCopyOption.REPLACE_EXISTING);
            Files.delete(avi);
            return mp4;
        }
        catch (InterruptedException e) {
            if (process != null) {
                process.destroyForcibly();
            }
            Thread.currentThread().interrupt();
            throw e;
        }
        catch (IOException e) {
            Files.deleteIfExists(temp);
            ShoreClient.LOG.warn("Keeping AVI after MP4 conversion failure", (Throwable)e);
            return avi;
        }
    }

    private void prune(int maxMb) throws IOException {
        try (Stream<Path> stream = Files.list(this.directory());){
            List<Path> clips = stream.filter(p -> p.getFileName().toString().matches("shoremod-.*\\.(avi|mp4)")).sorted(Comparator.comparing(p -> p.getFileName().toString())).toList();
            long bytes = 0L;
            for (Path p2 : clips) {
                bytes += Files.size(p2);
            }
            for (Path p2 : clips) {
                if (bytes <= (long)maxMb * 1024L * 1024L) {
                    break;
                }
                if (p2.equals(this.lastClip)) continue;
                long size = Files.size(p2);
                Files.delete(p2);
                bytes -= size;
                String base = p2.getFileName().toString().replaceFirst("\\.(avi|mp4)$", "");
                Files.deleteIfExists(p2.resolveSibling(base + ".txt"));
            }
        }
    }

    @Override
    public synchronized void close() {
        if (this.closed) {
            return;
        }
        this.closed = true;
        this.clear();
        this.frames.execute(this.exports::shutdown);
        this.frames.shutdown();
        this.ring.clear();
    }
}

