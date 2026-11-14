/*
 * Decompiled with CFR 0.152.
 */
package net.shoresmp.core;

public record BrowserWindow(int x, int y, int width, int height) {
    public static BrowserWindow fit(int screenWidth, int screenHeight) {
        int w = Math.max(1, Math.min(840, Math.min(Math.max(240, (int)((double)screenWidth * 0.78)), screenWidth - 24)));
        int h = Math.max(1, Math.min(520, Math.min(Math.max(160, (int)((double)screenHeight * 0.8)), screenHeight - 24)));
        return new BrowserWindow((screenWidth - w) / 2, (screenHeight - h) / 2, w, h);
    }

    public int contentX() {
        return this.x + 8;
    }

    public int contentY() {
        return this.y + 30;
    }

    public int contentWidth() {
        return Math.max(1, this.width - 16);
    }

    public int contentHeight() {
        return Math.max(1, this.height - 64);
    }

    public boolean contains(double mx, double my) {
        return mx >= (double)this.contentX() && my >= (double)this.contentY() && mx < (double)(this.contentX() + this.contentWidth()) && my < (double)(this.contentY() + this.contentHeight());
    }

    public int mouseX(double mx, double scale) {
        return (int)Math.floor((mx - (double)this.contentX()) * scale);
    }

    public int mouseY(double my, double scale) {
        return (int)Math.floor((my - (double)this.contentY()) * scale);
    }
}

