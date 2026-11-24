package you.jass.betterhitreg.mixin;

import com.mojang.blaze3d.audio.Channel;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.EXTEfx;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Channel.class)
public abstract class SourceMixin {
    @Final @Shadow private int source;

    @Unique
    private int betterhitreg$filter;

    @Inject(method = "play", at = @At("TAIL"))
    private void betterhitreg$play(CallbackInfo ci) {
        // Consume pending settings even without EFX support.
        AudioFilterContext.Settings settings = AudioFilterContext.take((Channel) (Object) this);
        if (!AL.getCapabilities().ALC_EXT_EFX) return;

        // No settings means the reused channel must be detached from its previous filter.
        if (settings == null) {
            betterhitreg$detachFilter();
            return;
        }

        float muffle = betterhitreg$clamp01(settings.muffle());
        float sharpen = betterhitreg$clamp01(settings.sharpen());

        if (muffle <= 0.0F && sharpen <= 0.0F) {
            betterhitreg$detachFilter();
            return;
        }

        if (betterhitreg$filter == 0) {
            betterhitreg$filter = EXTEfx.alGenFilters();
        }
        if (betterhitreg$filter == 0) return;

        // Reset the profile on reuse to prevent stale gain values.
        if (muffle > 0.0F && sharpen > 0.0F) {
            EXTEfx.alFilteri(betterhitreg$filter, EXTEfx.AL_FILTER_TYPE, EXTEfx.AL_FILTER_BANDPASS);
            EXTEfx.alFilterf(betterhitreg$filter, EXTEfx.AL_BANDPASS_GAIN, 1.0F);
            EXTEfx.alFilterf(betterhitreg$filter, EXTEfx.AL_BANDPASS_GAINLF, 1.0F - sharpen);
            EXTEfx.alFilterf(betterhitreg$filter, EXTEfx.AL_BANDPASS_GAINHF, 1.0F - muffle);
        } else if (muffle > 0.0F) {
            EXTEfx.alFilteri(betterhitreg$filter, EXTEfx.AL_FILTER_TYPE, EXTEfx.AL_FILTER_LOWPASS);
            EXTEfx.alFilterf(betterhitreg$filter, EXTEfx.AL_LOWPASS_GAIN, 1.0F);
            EXTEfx.alFilterf(betterhitreg$filter, EXTEfx.AL_LOWPASS_GAINHF, 1.0F - muffle);
        } else {
            EXTEfx.alFilteri(betterhitreg$filter, EXTEfx.AL_FILTER_TYPE, EXTEfx.AL_FILTER_HIGHPASS);
            EXTEfx.alFilterf(betterhitreg$filter, EXTEfx.AL_HIGHPASS_GAIN, 1.0F);
            EXTEfx.alFilterf(betterhitreg$filter, EXTEfx.AL_HIGHPASS_GAINLF, 1.0F - sharpen);
        }

        AL10.alSourcei(source, EXTEfx.AL_DIRECT_FILTER, betterhitreg$filter);
    }

    @Unique
    private static float betterhitreg$clamp01(float value) {
        if (Float.isNaN(value)) return 0.0F;
        return Math.max(0.0F, Math.min(1.0F, value));
    }

    @Unique
    private void betterhitreg$detachFilter() {
        if (betterhitreg$filter != 0) {
            AL10.alSourcei(source, EXTEfx.AL_DIRECT_FILTER, EXTEfx.AL_FILTER_NULL);
        }
    }

    @Inject(method = "destroy", at = @At("HEAD"))
    private void betterhitreg$destroy(CallbackInfo ci) {
        AudioFilterContext.clear((Channel) (Object) this);
        if (betterhitreg$filter == 0 || !AL.getCapabilities().ALC_EXT_EFX) return;

        // Detach before deleting: OpenAL does not allow deleting an attached filter.
        AL10.alSourcei(source, EXTEfx.AL_DIRECT_FILTER, EXTEfx.AL_FILTER_NULL);
        EXTEfx.alDeleteFilters(betterhitreg$filter);
        betterhitreg$filter = 0;
    }
}
