package you.jass.betterhitreg.ui;

import java.awt.*;

public class UIPanel implements UIElement {
    public final int x, y, width, height;
    public final UITheme theme;
    public final boolean gradient;

    public UIPanel(int x, int y, int width, int height, UITheme theme, boolean gradient) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.theme = theme;
        this.gradient = gradient;
    }

    @Override
    public void render(Object renderer, int mouseX, int mouseY) {
        Color baseBg = theme.background();
        Color baseBorder = theme.border();
        Color bgStart = gradient ? baseBg.brighter().brighter() : baseBg;
        Color borderStart = gradient ? baseBorder.brighter() : baseBorder;
        if (gradient) UIUtils.drawGradientRectangle(renderer, x, y, width, height, bgStart, baseBg);
        else UIUtils.drawRectangle(renderer, x, y, width, height, baseBg);
        if (gradient) UIUtils.drawGradientBorder(renderer, x, y, width, height, borderStart, baseBorder);
        else UIUtils.renderOutline(renderer, x, y, width, height, baseBorder);
    }

    @Override public boolean mouseClicked(double mx, double my, int button) { return false; }
    @Override public boolean mouseDragged(double mx, double my, int button, double dx, double dy) { return false; }
    @Override public boolean mouseReleased(double mx, double my, int button) { return false; }
}
