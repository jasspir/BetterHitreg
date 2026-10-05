package you.jass.betterhitreg.mixin;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import you.jass.betterhitreg.settings.Toggle;
import you.jass.betterhitreg.utility.InputTracker;

//26.3 grabs the mouse in sdl relative mode, which clamps the cursor position to the window
//so the movement minecraft turns the camera with is only visible in the deltas passed to onMove

@Mixin(MouseHandler.class)
public class MouseMixin {
    //version 26.3+
    @Inject(method = "onMove", at = @At("HEAD"))
    private void onMove(long handle, double x, double y, double dx, double dy, CallbackInfo ci) {
        if (Toggle.DEBUG_INPUTS.toggled()) InputTracker.onMove(dx, dy);
    }
}
