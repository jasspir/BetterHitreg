package you.jass.betterhitreg.mixin;

//version 26.2-
//import org.lwjgl.glfw.GLFW;

//version 26.3+
import com.mojang.blaze3d.platform.SDLEventHandler;
import org.lwjgl.sdl.SDL_Event;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import you.jass.betterhitreg.settings.Toggle;
import you.jass.betterhitreg.utility.InputTracker;

//26.3 replaced glfw with sdl, so hook minecraft's sdl event loop instead

//version 26.2-
//@Mixin(GLFW.class)

//version 26.3+
@Mixin(SDLEventHandler.class)

public class GLFWMixin {
    //version 26.2-
    //@Inject(method = "glfwPollEvents", at = @At("RETURN"))
    //private static void glfwPollEvents(CallbackInfo ci) {
    //    if (Toggle.DEBUG_INPUTS.toggled()) InputTracker.update();
    //}

    //version 26.3+
    @Inject(method = "pollEvents", at = @At("RETURN"))
    private void pollEvents(CallbackInfo ci) {
        if (Toggle.DEBUG_INPUTS.toggled()) InputTracker.update();
    }

    //version 26.3+
    @Inject(method = "handleMouseMotionEvent", at = @At("HEAD"))
    private void onMouseMotion(SDL_Event event, CallbackInfo ci) {
        if (Toggle.DEBUG_INPUTS.toggled()) InputTracker.onRawMove(event.motion().xrel(), event.motion().yrel());
    }
}
