package you.jass.betterhitreg.mixin;

import com.mojang.blaze3d.audio.Channel;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.EXTEfx;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import you.jass.betterhitreg.hitreg.Hitreg;

@Mixin(Channel.class)
public abstract class SourceMixin {
    @Final @Shadow private int source;

    @Inject(method = "play", at = @At("TAIL"))
    private void play(CallbackInfo ci) {
        if (!AL.getCapabilities().ALC_EXT_EFX || Hitreg.shouldFilter == 0) return;
        int filter = 0;
        boolean useFilter = false;

        if (Hitreg.shouldFilter > 0) {
            Hitreg.shouldFilter--;

            if (Hitreg.muffleAmount != 0) {
                filter = EXTEfx.alGenFilters();
                EXTEfx.alFilteri(filter, EXTEfx.AL_FILTER_TYPE, EXTEfx.AL_FILTER_LOWPASS);
                EXTEfx.alFilterf(filter, EXTEfx.AL_LOWPASS_GAINHF, 1 - Hitreg.muffleAmount);
                useFilter = true;
            }

            if (Hitreg.sharpenAmount != 0) {
                Hitreg.shouldFilter--;
                filter = EXTEfx.alGenFilters();
                EXTEfx.alFilteri(filter, EXTEfx.AL_FILTER_TYPE, EXTEfx.AL_FILTER_HIGHPASS);
                EXTEfx.alFilterf(filter, EXTEfx.AL_HIGHPASS_GAINLF, 1 - Hitreg.sharpenAmount);
                useFilter = true;
            }
        }

        if (useFilter) AL10.alSourcei(source, EXTEfx.AL_DIRECT_FILTER, filter);
    }
}