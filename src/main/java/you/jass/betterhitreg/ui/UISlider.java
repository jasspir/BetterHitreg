package you.jass.betterhitreg.ui;

import net.minecraft.client.gui.Font;

import java.awt.*;
import java.util.function.Consumer;

public class UISlider implements UIElement {
    public final int x, y, textX, width;
    public final float min, max;
    public float value;
    public final int gap;
    public final float precision;
    public final String text, prefix, suffix;
    public final Font textRenderer;
    private final float initialValue;
    public boolean dragging;
    public final Consumer<Float> onDrag;
    public final Consumer<Integer> onStop;
    public final UITheme theme;
    public final boolean gradient;
    public final boolean toggle;

    public UISlider(int x, int y, int textX, int width, float min, float max, float initial, int gap, float precision, String text, String prefix, String suffix, Font textRenderer, UITheme theme, boolean gradient, boolean toggle, Consumer<Float> onDrag, Consumer<Integer> onStop) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.min = min;
        this.max = max;
        this.gap = gap;
        this.precision = precision;
        this.prefix = prefix;
        this.suffix = suffix;
        this.text = text;
        this.textX = textX;
        this.textRenderer = textRenderer;
        this.onDrag = onDrag;
        this.onStop = onStop;
        this.initialValue = initial;
        this.value = initial;
        this.theme = theme;
        this.gradient = gradient;
        this.toggle = toggle;
    }

    @Override
    public void render(Object renderer, int mx, int my) {
        boolean hovered = mx >= x && mx <= x + width && my >= y - 3 && my <= y + 4;

        Color baseText = theme.text();
        Color baseTrack = theme.background();
        Color baseThumb = hovered ? theme.border().brighter() : theme.border();

        UIUtils.drawHorizontalGradient(renderer, x, y - 1, width, 2, baseTrack, baseTrack);

        double clampedValue = Math.max(min, Math.min(max, value));
        double normalized = (clampedValue - min) / (max - min);
        int tx = (int) (x + normalized * (width - 2));

        UIUtils.drawRectangle(renderer, tx, y - 4, 2, 8, baseThumb);

        boolean off = toggle && value <= min;
        String result = off ? "OFF" : prefix + (int) value + suffix;

        if (gradient && !off) {
            UIUtils.drawGradientText(renderer, textRenderer, text, textX, y - 4, theme.highlighted().brighter(), theme.highlighted().darker(), false);
            UIUtils.drawGradientText(renderer, textRenderer, result, x + width + gap, y - 4, theme.highlighted().brighter(), theme.highlighted().darker(), true);
        } else {
            UIUtils.drawText(renderer, textRenderer, text, textX, y - 4, baseText, false);
            UIUtils.drawText(renderer, textRenderer, result, x + width + gap, y - 4, baseText, true);
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0 && mx >= x && mx <= x + width && my >= y - 3 && my <= y + 4) {
            dragging = true;
            updateValue(mx);
            onDrag.accept(value);
            playSound();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragging) {
            updateValue(mx);
            onDrag.accept(value);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (dragging && button == 0) {
            dragging = false;
            onStop.accept((int) value);
            return true;
        }
        return false;
    }

    private void updateValue(double mx) {
        double fraction = (mx - x) / (width - 2.0);
        fraction = Math.max(0.0, Math.min(1.0, fraction));
        double raw = min + fraction * (max - min);
        double stepped = Math.round(raw / precision) * precision;
        this.value = (float) Math.max(min, Math.min(max, stepped));
    }

    public float getValue() {
        return value;
    }
}
