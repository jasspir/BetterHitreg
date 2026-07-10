package you.jass.betterhitreg.ui;

import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import you.jass.betterhitreg.hitreg.Hitreg;

public interface UIElement {
    void render(Object renderer, int mouseX, int mouseY);
    boolean mouseClicked(double mouseX, double mouseY, int button);
    boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY);
    boolean mouseReleased(double mouseX, double mouseY, int button);
    default void playSound() {
        Hitreg.client.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1));
    }
}
