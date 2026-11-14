/*
 * Decompiled with CFR 0.152.
 */
package net.shoresmp.fabric;

import de.keksuccino.rinku.Rinku;
import de.keksuccino.rinku.RinkuBrowser;
import de.keksuccino.rinku.RinkuClient;
import de.keksuccino.rinku.RinkuRenderCoordinator;
import de.keksuccino.rinku.binarydownload.RinkuDownloadListener;
import java.net.URI;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_10799;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_156;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_364;
import net.minecraft.class_408;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.minecraft.class_5348;
import net.minecraft.class_5481;
import net.shoresmp.core.BrowserStartup;
import net.shoresmp.core.BrowserWindow;
import net.shoresmp.core.TicketPolicy;
import net.shoresmp.fabric.ShoreClient;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.browser.CefPaintElementType;
import org.cef.callback.CefContextMenuParams;
import org.cef.callback.CefMenuModel;
import org.cef.handler.CefContextMenuHandler;
import org.cef.handler.CefContextMenuHandlerAdapter;
import org.cef.handler.CefLifeSpanHandler;
import org.cef.handler.CefLifeSpanHandlerAdapter;
import org.cef.handler.CefLoadHandler;
import org.cef.handler.CefLoadHandlerAdapter;
import org.cef.handler.CefRequestHandler;
import org.cef.handler.CefRequestHandlerAdapter;
import org.cef.network.CefRequest;

public final class TicketsScreen
extends class_437 {
    private final class_437 parent;
    private RinkuBrowser browser;
    private CefClient cef;
    private BrowserWindow window;
    private boolean failed;
    private boolean browserFocused;
    private int pressedButtons;
    private long waitingSince;
    private volatile boolean nativeCreated;
    private volatile boolean loadFinished;
    private long browserStarted;
    private volatile String loadError;
    private String status = "Starting the ticket browser...";

    public TicketsScreen(class_437 parent) {
        super((class_2561)class_2561.method_43470((String)"ShoreSMP Support"));
        this.parent = parent;
    }

    public static void open() {
        class_310 client = class_310.method_1551();
        class_437 parent = client.field_1755 instanceof class_408 ? null : client.field_1755;
        ShoreClient.openNextTick(() -> new TicketsScreen(parent));
    }

    protected void method_25426() {
        this.window = BrowserWindow.fit((int)this.field_22789, (int)this.field_22790);
        if (this.waitingSince == 0L) {
            this.waitingSince = System.nanoTime();
        }
        this.method_37063(class_4185.method_46430((class_2561)class_2561.method_43470((String)"Close"), b -> this.method_25419()).method_46434(this.window.x() + this.window.width() - 52, this.window.y() + 5, 46, 20).method_46431());
        this.method_37063(class_4185.method_46430((class_2561)class_2561.method_43470((String)"Reload"), b -> this.restart()).method_46434(this.window.x() + this.window.width() - 108, this.window.y() + 5, 52, 20).method_46431());
        this.method_37063(class_4185.method_46430((class_2561)class_2561.method_43470((String)"Open website"), b -> class_156.method_668().method_673(URI.create("https://www.shoresmp.net/support"))).method_46434(this.window.x() + 8, this.window.y() + this.window.height() - 27, 102, 20).method_46431());
        if (this.browser != null) {
            this.resizeBrowser();
        } else {
            this.updateStartup();
        }
        ShoreClient.recorder.clear();
    }

    private void restart() {
        this.releaseBrowser();
        this.nativeCreated = false;
        this.loadFinished = false;
        this.loadError = null;
        this.failed = false;
        this.waitingSince = System.nanoTime();
        this.updateStartup();
    }

    public void method_25393() {
        if (this.browser == null && !this.failed) {
            this.updateStartup();
        }
        if (this.browser != null && !this.failed) {
            if (this.loadError != null) {
                this.failed = true;
                this.status = this.loadError;
            } else if (!this.browser.isTextureReady() && System.nanoTime() - this.browserStarted > 30000000000L) {
                this.failed = true;
                this.status = "The browser produced no frame. Reload recreates it; Open website opens tickets in your browser.";
                ShoreClient.LOG.warn("Ticket browser frame timeout: nativeCreated={}, loadFinished={}, valid={}", new Object[]{this.nativeCreated, this.loadFinished, this.browser.isValid()});
            }
        }
    }

    private void updateStartup() {
        try {
            FabricLoader loader = FabricLoader.getInstance();
            boolean dawn = loader.isModLoaded("dawn") || System.getProperty("dawn.ipc.enabled") != null;
            boolean staffBlock = dawn && loader.isModLoaded("jqsh16staffutilities");
            RinkuDownloadListener download = RinkuDownloadListener.INSTANCE;
            BrowserStartup.State state = BrowserStartup.state((boolean)Rinku.isInitialized(), (boolean)download.isDone(), (boolean)download.isFailed(), (boolean)Rinku.isInitializationAllowed(), (boolean)staffBlock, (long)(System.nanoTime() - this.waitingSince));
            switch (state) {
                case READY: {
                    this.createBrowser();
                    break;
                }
                case DOWNLOADING: {
                    this.status = "Installing browser: " + download.getTask().getString() + " (" + Math.round(download.getProgress() * 100.0f) + "%)";
                    break;
                }
                case STARTING: {
                    this.status = "Starting the browser engine...";
                    break;
                }
                case INCOMPATIBLE: {
                    this.failed = true;
                    this.status = "Staff Utilities disables in-game browsers on Dawn. Use Open website here, or run standard Fabric for the embedded browser.";
                    break;
                }
                case FAILED: {
                    this.failed = true;
                    this.status = "The browser engine failed to start. Use Open website, or check latest.log for the Rinku error and restart Minecraft.";
                    break;
                }
                case TIMED_OUT: {
                    this.failed = true;
                    this.status = "The browser engine did not start. Reload retries; Open website lets you access tickets now.";
                }
            }
        }
        catch (LinkageError | RuntimeException e) {
            this.fail(e);
        }
    }

    private void createBrowser() {
        this.cef = Rinku.getApp().getHandle().createClient();
        RinkuClient wrapper = new RinkuClient(this.cef);
        this.cef.addRequestHandler((CefRequestHandler)new CefRequestHandlerAdapter(){

            public boolean onBeforeBrowse(CefBrowser b, CefFrame frame, CefRequest request, boolean gesture, boolean redirect) {
                if (!frame.isMain()) {
                    return false;
                }
                String url = request.getURL();
                if ("about:blank".equals(url) || TicketPolicy.allowed((String)url)) {
                    return false;
                }
                ShoreClient.LOG.warn("Ticket browser blocked navigation outside portal/sign-in routes");
                class_310.method_1551().execute(() -> ShoreClient.tellError("This link is outside the ticket portal. Use Open website to follow it in your browser."));
                return true;
            }

            public boolean onOpenURLFromTab(CefBrowser b, CefFrame frame, String url, boolean gesture) {
                return true;
            }
        });
        wrapper.addLoadHandler((CefLoadHandler)new CefLoadHandlerAdapter(){

            public void onLoadEnd(CefBrowser b, CefFrame frame, int code) {
                if (b == TicketsScreen.this.browser && frame.isMain()) {
                    TicketsScreen.this.loadFinished = true;
                    ShoreClient.LOG.info("Ticket browser main frame loaded: HTTP {}", (Object)code);
                }
            }

            public void onLoadError(CefBrowser b, CefFrame frame, CefLoadHandler.ErrorCode code, String message, String url) {
                if (b == TicketsScreen.this.browser && frame.isMain() && code != CefLoadHandler.ErrorCode.ERR_ABORTED) {
                    TicketsScreen.this.loadError = "Tickets could not load (" + String.valueOf(code) + "). Reload retries; Open website opens tickets externally.";
                    ShoreClient.LOG.warn("Ticket browser load failed: {} {}", (Object)code, (Object)message);
                }
            }
        });
        this.cef.addLifeSpanHandler((CefLifeSpanHandler)new CefLifeSpanHandlerAdapter(){

            public void onAfterCreated(CefBrowser b) {
                TicketsScreen.this.nativeCreated = true;
                class_310.method_1551().execute(() -> {
                    if (TicketsScreen.this.browser != b) {
                        return;
                    }
                    TicketsScreen.this.resizeBrowser();
                    b.setWindowVisibility(true);
                    b.invalidate(CefPaintElementType.PET_VIEW);
                    ShoreClient.LOG.info("Ticket browser native instance ready");
                });
            }

            public boolean onBeforePopup(CefBrowser b, CefFrame frame, String url, String name) {
                if (TicketPolicy.allowed((String)url)) {
                    class_310.method_1551().execute(() -> {
                        if (TicketsScreen.this.browser == b) {
                            TicketsScreen.this.browser.loadURL(url);
                        }
                    });
                }
                return true;
            }
        });
        wrapper.addContextMenuHandler((CefContextMenuHandler)new CefContextMenuHandlerAdapter(){

            public void onBeforeContextMenu(CefBrowser b, CefFrame frame, CefContextMenuParams params, CefMenuModel model) {
                model.clear();
            }
        });
        this.browser = new RinkuBrowser(wrapper, "https://www.shoresmp.net/support", false);
        this.browserStarted = System.nanoTime();
        this.resizeBrowser();
        this.browser.setCloseAllowed();
        this.browser.useBrowserControls(false);
        this.browser.createImmediately();
        this.status = "Loading ShoreSMP support...";
    }

    private void fail(Throwable error) {
        this.failed = true;
        this.status = "The ticket browser could not start. Use Open website, or check latest.log and Reload to retry.";
        ShoreClient.LOG.error("Ticket browser initialization failed", error);
        this.releaseBrowser();
    }

    private double scale() {
        return this.field_22787.method_22683().method_4495();
    }

    private int bx(double x) {
        return this.window.mouseX(x, this.scale());
    }

    private int by(double y) {
        return this.window.mouseY(y, this.scale());
    }

    private void resizeBrowser() {
        this.browser.resize(Math.max(1, (int)((double)this.window.contentWidth() * this.scale())), Math.max(1, (int)((double)this.window.contentHeight() * this.scale())));
    }

    public void method_25394(class_332 ctx, int mx, int my, float delta) {
        if (this.browser != null) {
            RinkuRenderCoordinator.pumpOnRenderThread();
        }
        ctx.method_25294(this.window.x() - 1, this.window.y() - 1, this.window.x() + this.window.width() + 1, this.window.y() + this.window.height() + 1, 0xFF000000 | ShoreClient.config.accent);
        ctx.method_25294(this.window.x(), this.window.y(), this.window.x() + this.window.width(), this.window.y() + this.window.height(), -267708379);
        ctx.method_25303(this.field_22793, this.field_22793.method_27523("ShoreSMP Support", Math.max(1, this.window.width() - 126)), this.window.x() + 8, this.window.y() + 11, -1);
        if (this.browser != null && this.browser.isTextureReady() && this.browser.getTextureIdentifier() != null) {
            ctx.method_25290(class_10799.field_56883, this.browser.getTextureIdentifier(), this.window.contentX(), this.window.contentY(), 0.0f, 0.0f, this.window.contentWidth(), this.window.contentHeight(), this.window.contentWidth(), this.window.contentHeight());
        } else {
            List lines = this.field_22793.method_1728((class_5348)class_2561.method_43470((String)this.status), Math.max(1, this.window.contentWidth() - 24));
            int y = this.window.contentY() + Math.max(8, (this.window.contentHeight() - Math.min(lines.size(), 6) * 12) / 2);
            for (int i = 0; i < Math.min(lines.size(), 6); ++i) {
                ctx.method_35720(this.field_22793, (class_5481)lines.get(i), this.window.contentX() + 12, y + i * 12, this.failed ? -14975 : -1);
            }
        }
        super.method_25394(ctx, mx, my, delta);
    }

    public boolean method_25402(class_11909 click, boolean doubled) {
        if (this.window.contains(click.comp_4798(), click.comp_4799()) && this.browser != null) {
            this.method_25395(null);
            this.browserFocused = true;
            this.browser.sendMousePress(this.bx(click.comp_4798()), this.by(click.comp_4799()), click.method_74245());
            this.browser.setFocus(true);
            if (click.method_74245() >= 0 && click.method_74245() < 8) {
                this.pressedButtons |= 1 << click.method_74245();
            }
            return true;
        }
        this.browserFocused = false;
        if (this.browser != null) {
            this.browser.setFocus(false);
        }
        return super.method_25402(click, doubled);
    }

    public boolean method_25406(class_11909 click) {
        int mask;
        int n = mask = click.method_74245() >= 0 && click.method_74245() < 8 ? 1 << click.method_74245() : 0;
        if (this.browser != null && (this.pressedButtons & mask) != 0) {
            this.pressedButtons &= ~mask;
            this.browser.sendMouseRelease(this.bx(click.comp_4798()), this.by(click.comp_4799()), click.method_74245());
            return true;
        }
        return super.method_25406(click);
    }

    public boolean method_25403(class_11909 click, double dx, double dy) {
        if (this.browser != null && this.pressedButtons != 0) {
            this.browser.sendMouseMove(this.bx(click.comp_4798()), this.by(click.comp_4799()));
            return true;
        }
        return super.method_25403(click, dx, dy);
    }

    public void method_16014(double x, double y) {
        if (this.browser != null && (this.window.contains(x, y) || this.pressedButtons != 0)) {
            this.browser.sendMouseMove(this.bx(x), this.by(y));
        }
    }

    public boolean method_25401(double x, double y, double horizontal, double vertical) {
        if (this.browser != null && this.window.contains(x, y)) {
            this.browser.sendMouseWheel(this.bx(x), this.by(y), vertical, 0);
            return true;
        }
        return super.method_25401(x, y, horizontal, vertical);
    }

    public boolean method_25404(class_11908 input) {
        if (input.comp_4795() == 256) {
            this.method_25419();
            return true;
        }
        if (this.browser != null && this.browserFocused) {
            this.browser.sendKeyPress(input.comp_4795(), (long)input.comp_4796(), input.comp_4797());
            return true;
        }
        return super.method_25404(input);
    }

    public boolean method_16803(class_11908 input) {
        if (this.browser != null && this.browserFocused) {
            this.browser.sendKeyRelease(input.comp_4795(), (long)input.comp_4796(), input.comp_4797());
            return true;
        }
        return super.method_16803(input);
    }

    public boolean method_25400(class_11905 input) {
        if (this.browser != null && this.browserFocused) {
            for (char ch : input.method_74226().toCharArray()) {
                this.browser.sendKeyTyped(ch, input.comp_4794());
            }
            return true;
        }
        return super.method_25400(input);
    }

    private void releaseBrowser() {
        RinkuBrowser oldBrowser = this.browser;
        CefClient oldCef = this.cef;
        this.browser = null;
        this.cef = null;
        this.pressedButtons = 0;
        this.browserFocused = false;
        try {
            if (oldBrowser != null) {
                oldBrowser.close();
            }
        }
        catch (RuntimeException e) {
            ShoreClient.LOG.warn("Browser close failed", (Throwable)e);
        }
        try {
            if (oldCef != null) {
                oldCef.dispose();
            }
        }
        catch (RuntimeException e) {
            ShoreClient.LOG.warn("Browser client cleanup failed", (Throwable)e);
        }
    }

    public void method_25432() {
        this.releaseBrowser();
    }

    public void method_25419() {
        this.field_22787.method_1507(this.parent);
    }

    public boolean method_25421() {
        return false;
    }
}

