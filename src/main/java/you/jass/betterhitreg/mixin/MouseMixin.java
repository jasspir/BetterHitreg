package you.jass.betterhitreg.mixin;

import com.mojang.blaze3d.Blaze3D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.util.SmoothDouble;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import you.jass.betterhitreg.settings.Toggle;
import you.jass.betterhitreg.utility.InputTracker;

@Mixin(MouseHandler.class)
public class MouseMixin {
    @Final @Shadow private Minecraft minecraft;
    @Unique private double simDX, simDY, simX, simY, lastTime;
    @Unique private boolean simHasPosition;
    @Unique private final SmoothDouble smoothX = new SmoothDouble();
    @Unique private final SmoothDouble smoothY = new SmoothDouble();

    //TODO minecraft.getWindow().handle() doesn't exist on 1.19.4 maybe higher versions
    //version 26.2-
    @Inject(method = "onMove", at = @At("HEAD"))
    private void onMove(long window, double x, double y, CallbackInfo ci) {
        if (!Toggle.DEBUG_INPUTS.toggled() || window != minecraft.getWindow().handle()) return;
        if (simHasPosition && minecraft.isWindowActive()) {
            simDX += x - simX;
            simDY += y - simY;
        }
        simX = x;
        simY = y;
        simHasPosition = true;
    }

    //version 26.3+
    @Inject(method = "onMove", at = @At("HEAD"))
    private void onMove(long window, double x, double y, double dx, double dy, CallbackInfo ci) {
        if (!Toggle.DEBUG_INPUTS.toggled() || window != minecraft.getWindow().handle()) return;
        simDX += dx;
        simDY += dy;
    }

    //TODO isn't the same in 1.19.4 maybe higher versions
    @Inject(method = "handleAccumulatedMovement", at = @At("HEAD"))
    private void simulate(CallbackInfo ci) {
        if (!Toggle.DEBUG_INPUTS.toggled()) return;

        double time = Blaze3D.getTime();
        double d = time - lastTime;
        lastTime = time;

        if (!minecraft.isWindowActive() || !minecraft.mouseHandler.isMouseGrabbed() || minecraft.player == null) {
            simDX = simDY = 0d;
            return;
        }

        double e = minecraft.options.sensitivity().get() * 0.6 + 0.2;
        double f = e * e * e, g = f * 8.0, j, k;

        if (minecraft.options.smoothCamera) {
            j = smoothX.getNewDeltaValue(simDX * g, d * g);
            k = smoothY.getNewDeltaValue(simDY * g, d * g);
        } else if (minecraft.options.getCameraType().isFirstPerson() && minecraft.player.isScoping()) {
            smoothX.reset();
            smoothY.reset();
            j = simDX * f;
            k = simDY * f;
        } else {
            smoothX.reset();
            smoothY.reset();
            j = simDX * g;
            k = simDY * g;
        }

        double expectedX = minecraft.options.invertMouseX().get() ? -j : j;
        double expectedY = minecraft.options.invertMouseY().get() ? -k : k;

        InputTracker.setExpectedTurn(expectedX, expectedY);
        
        simDX = simDY = 0d;
    }
}