package you.jass.betterhitreg.ui;

import net.minecraft.client.gui.Font;
import java.awt.*;
import java.util.function.Consumer;

public class UICheckboxSlider implements UIElement {
    private final UISlider slider;
    private final UICheckbox checkbox;

    public UICheckboxSlider(int sliderX, int sliderY, int sliderTextX, int sliderWidth, float min, float max, float initial, int sliderGap, float precision, String sliderText, String prefix, String suffix, Font textRenderer, UITheme theme, boolean gradient, int checkboxX, int checkboxY, int checkboxSize, boolean checkboxInitial, Consumer<Float> onDrag, Consumer<Integer> onStop, Consumer<Boolean> onCheckChange) {
        this.slider = new UISlider(
                sliderX, sliderY, sliderTextX, sliderWidth,
                min, max, initial, sliderGap, precision,
                sliderText, prefix, suffix, textRenderer,
                theme, gradient, false, onDrag, onStop
        );

        this.checkbox = new UICheckbox(
                checkboxX, checkboxY, checkboxSize, 0,
                textRenderer, "", theme, gradient,
                checkboxInitial, onCheckChange
        );
    }

    @Override
    public void render(Object renderer, int mx, int my) {
        renderSlider(renderer, mx, my, checkbox.checked);
        checkbox.render(renderer, mx, my);
    }

    private void renderSlider(Object renderer, int mx, int my, boolean highlighted) {
        boolean hovered = mx >= slider.x && mx <= slider.x + slider.width && my >= slider.y - 3 && my <= slider.y + 4;

        Color baseText = highlighted ? slider.theme.highlighted() : slider.theme.text();
        Color baseTrack = slider.theme.background();
        Color baseThumb = hovered ? slider.theme.border().brighter() : slider.theme.border();

        UIUtils.drawHorizontalGradient(renderer, slider.x, slider.y - 1, slider.width, 2, baseTrack, baseTrack);

        double clampedValue = Math.max(slider.min, Math.min(slider.max, slider.value));
        double normalized = (clampedValue - slider.min) / (slider.max - slider.min);
        int tx = (int) (slider.x + normalized * (slider.width - 2));

        UIUtils.drawRectangle(renderer, tx, slider.y - 4, 2, 8, baseThumb);

        String result = slider.prefix + (int) slider.value + slider.suffix;

        if (highlighted && slider.gradient) {
            UIUtils.drawGradientText(renderer, slider.textRenderer, slider.text, slider.textX, slider.y - 4, slider.theme.highlighted().brighter(), slider.theme.highlighted().darker(), false);
            UIUtils.drawGradientText(renderer, slider.textRenderer, result, slider.x + slider.width + slider.gap, slider.y - 4, slider.theme.highlighted().brighter(), slider.theme.highlighted().darker(), true);
        } else {
            UIUtils.drawText(renderer, slider.textRenderer, slider.text, slider.textX, slider.y - 4, baseText, false);
            UIUtils.drawText(renderer, slider.textRenderer, result, slider.x + slider.width + slider.gap, slider.y - 4, baseText, true);
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (checkbox.mouseClicked(mx, my, button)) return true;
        return slider.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        return slider.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        return slider.mouseReleased(mx, my, button);
    }

    public float getValue() {
        return slider.getValue();
    }

    public boolean isChecked() {
        return checkbox.checked;
    }
}