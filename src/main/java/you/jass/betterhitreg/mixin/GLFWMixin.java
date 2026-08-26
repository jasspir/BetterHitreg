package you.jass.betterhitreg.mixin;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import you.jass.betterhitreg.settings.Toggle;
import you.jass.betterhitreg.utility.InputTracker;

@Mixin(GLFW.class)
public class GLFWMixin {
    @Inject(method = "glfwPollEvents", at = @At("RETURN"))
    private static void glfwPollEvents(CallbackInfo ci) {
        if (Toggle.DEBUG_INPUTS.toggled()) InputTracker.update();
    }
}