package you.jass.betterhitreg.mixin;

import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import you.jass.betterhitreg.hitreg.HitType;
import you.jass.betterhitreg.hitreg.Hitreg;
import you.jass.betterhitreg.utility.MultiVersion;
import you.jass.betterhitreg.settings.Toggle;

@Mixin(SoundEngine.class)
public class SoundMixin {
    //version 1.21.5-
//    @Inject(method = "play(Lnet/minecraft/client/resources/sounds/SoundInstance;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/ChannelAccess$ChannelHandle;execute(Ljava/util/function/Consumer;)V", ordinal = 0, shift = At.Shift.AFTER))
//    private void before(SoundInstance sound, CallbackInfo ci) {
//        if (sound == null || MultiVersion.getSoundPath(sound) == null) return;
//        if (Hitreg.muffleAmount != 0 || Hitreg.sharpenAmount != 0) filter(sound);
//    }

    //version 1.21.5-
//    @Inject (method = "play(Lnet/minecraft/client/resources/sounds/SoundInstance;)V", at = @At("HEAD"), cancellable = true)
//    private void play(SoundInstance sound, CallbackInfo ci) {
//        if (sound == null || MultiVersion.getSoundPath(sound) == null || sound.getSource() != SoundSource.PLAYERS) return;
//        if (MultiVersion.getSoundPath(sound).startsWith("entity.player.attack")) {
//            //1.8 doesn't have attack sounds
//            if (Toggle.LEGACY_SOUNDS.toggled()) ci.cancel();
//            return;
//        }
//        if (MultiVersion.getSoundPath(sound).startsWith("entity.player.hurt")) return;
//        if (Toggle.SILENCE_NON_HITS.toggled()) ci.cancel();
//    }

    //version 1.21.6+
    @Inject(method = "play(Lnet/minecraft/client/resources/sounds/SoundInstance;)Lnet/minecraft/client/sounds/SoundEngine$PlayResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/ChannelAccess$ChannelHandle;execute(Ljava/util/function/Consumer;)V", ordinal = 0, shift = At.Shift.AFTER))
    private void before(SoundInstance sound, CallbackInfoReturnable<?> cir) {
        if (sound == null || MultiVersion.getSoundPath(sound) == null) return;
        if (Hitreg.muffleAmount != 0 || Hitreg.sharpenAmount != 0) filter(sound);
    }

    //version 1.21.6+
    @Inject (method = "play(Lnet/minecraft/client/resources/sounds/SoundInstance;)Lnet/minecraft/client/sounds/SoundEngine$PlayResult;", at = @At("HEAD"), cancellable = true)
    private void play(SoundInstance sound, CallbackInfoReturnable<SoundEngine.PlayResult> cir) {
        if (sound == null || MultiVersion.getSoundPath(sound) == null || sound.getSource() != SoundSource.PLAYERS) return;
        if (MultiVersion.getSoundPath(sound).startsWith("entity.player.attack")) {
            if (Toggle.LEGACY_SOUNDS.toggled()) cir.cancel();
            return;
        }
        if (MultiVersion.getSoundPath(sound).startsWith("entity.player.hurt")) return;
        if (Toggle.SILENCE_NON_HITS.toggled()) cir.cancel();
    }

    @Unique
    private void filter(SoundInstance sound) {
        if (!MultiVersion.getSoundPath(sound).startsWith("entity.player.attack") && !MultiVersion.getSoundPath(sound).contains("hurt")) return;
        if (Hitreg.muffleAmount != 0 || Hitreg.sharpenAmount != 0) Hitreg.shouldFilter++;
    }
}