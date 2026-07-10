package you.jass.betterhitreg.ui;

import net.minecraft.client.gui.Font;
import you.jass.betterhitreg.utility.MultiVersion;

import java.awt.*;

public final class UIUtils {
    private static float shift = 0f;
    private static final float STEP = 0.01f;
    private static final long TICK_INTERVAL_NS = 50_000_000L;
    private static long lastTickTime = System.nanoTime();

    public static void update() {
        long now = System.nanoTime();
        if (now - lastTickTime >= TICK_INTERVAL_NS) {
            lastTickTime += TICK_INTERVAL_NS;
            shift += STEP;
            if (shift >= 1f) shift -= 1f;
        }
    }

    public static float getShift() {
        return shift;
    }

    public static void drawRectangle(Object renderer, int x, int y, int w, int h, Color c) {
        MultiVersion.drawRectangle(renderer, x, y, w, h, c);
    }

    public static void drawGradientRectangle(Object renderer, int x, int y, int w, int h, Color start, Color end) {
        MultiVersion.drawGradientRectangle(renderer, x, y, w, h, start, end);
    }

    public static void drawHorizontalGradient(Object renderer, int x, int y, int w, int h, Color leftColor, Color rightColor) {
        MultiVersion.drawHorizontalGradient(renderer, x, y, w, h, leftColor, rightColor);
    }

    public static void renderOutline(Object renderer, int x, int y, int w, int h, Color c) {
        MultiVersion.renderOutline(renderer, x, y, w, h, c);
    }

    public static void drawGradientBorder(Object renderer, int x, int y, int w, int h, Color start, Color end) {
        MultiVersion.drawGradientBorder(renderer, x, y, w, h, start, end);
    }

    public static void drawText(Object renderer, Font tr, String s, int x, int y, Color c, boolean center) {
        MultiVersion.drawText(renderer, tr, s, x, y, c, center);
    }

    public static void drawGradientText(Object renderer, Font tr, String s, int x, int y, Color start, Color end, boolean center) {
        MultiVersion.drawGradientText(renderer, tr, s, x, y, start, end, center);
    }

    public static void drawHLine(Object renderer, int x, int y, int length, Color c, int thickness) {
        drawRectangle(renderer, x, y - thickness/2, length, thickness, c);
    }

    public static void drawHGradientLine(Object renderer, int x, int y, int length, Color left, Color right, int thickness) {
        drawGradientRectangle(renderer, x, y - thickness/2, length, thickness, left, right);
    }

    public static Color blend(Color c1, Color c2, float t) {
        float inv = 1f - t;
        int a = (int)(c1.getAlpha() * inv + c2.getAlpha() * t);
        int r = (int)(c1.getRed()   * inv + c2.getRed()   * t);
        int g = (int)(c1.getGreen() * inv + c2.getGreen() * t);
        int b = (int)(c1.getBlue()  * inv + c2.getBlue()  * t);
        return new Color(r, g, b, a);
    }
}
