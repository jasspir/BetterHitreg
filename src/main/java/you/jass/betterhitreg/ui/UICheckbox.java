package you.jass.betterhitreg.ui;

import net.minecraft.client.gui.Font;

import java.awt.*;
import java.util.function.Consumer;

public class UICheckbox implements UIElement {
    public final int x, y, size, gap;
    public final Font textRenderer;
    public final String label;
    public boolean checked;
    public final Consumer<Boolean> onChange;
    public final UITheme theme;
    public final boolean gradient;

    public UICheckbox(int x, int y, int size, int gap, Font textRenderer, String label, UITheme theme, boolean gradient, boolean initial, Consumer<Boolean> onChange) {
        this.x = x;
        this.y = y - 6;
        this.size = size;
        this.gap = gap;
        this.textRenderer = textRenderer;
        this.label = label;
        this.checked = initial;
        this.onChange = onChange;
        this.theme = theme;
        this.gradient = gradient;
    }

    @Override
    public void render(Object renderer, int mx, int my) {
        boolean hovered = isHovered(mx, my);

        Color baseText;
        Color bg;
        Color border;

        if (hovered) {
            baseText = theme.hovered();
            bg = theme.hovered();
            border = theme.hovered();
        } else if (checked) {
            baseText = theme.highlighted();
            bg = theme.highlighted();
            border = theme.highlighted();
        } else {
            baseText = theme.text();
            bg = theme.background();
            border = theme.border();
        }

        if (checked && !hovered && gradient) UIUtils.drawGradientText(renderer, textRenderer, label, x, y + 2, baseText.brighter(), baseText.darker(), false);
        else UIUtils.drawText(renderer, textRenderer, label, x, y + 2, baseText, false);

        UIUtils.renderOutline(renderer, x + gap, y, size, size, border);

        if (checked) UIUtils.drawRectangle(renderer, x + gap + 2, y + 2, size - 4, size - 4, bg);
    }

    private boolean isHovered(double mx, double my) {
        int textWidth = textRenderer.width(label);
        int height = Math.max(size, textRenderer.lineHeight);

        int startX = x;
        int endX   = x + gap + size;
        int startY = y;
        int endY   = y + height;

        return mx >= startX && mx <= endX && my >= startY && my <= endY;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0 && isHovered(mx, my)) {
            checked = !checked;
            onChange.accept(checked);
            playSound();
            return true;
        }
        return false;
    }

    @Override public boolean mouseDragged(double mx, double my, int button, double dx, double dy) { return false; }
    @Override public boolean mouseReleased(double mx, double my, int button) { return false; }
}
