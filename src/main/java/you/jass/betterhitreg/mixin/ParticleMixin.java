package you.jass.betterhitreg.mixin;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import you.jass.betterhitreg.settings.Toggle;

import static you.jass.betterhitreg.hitreg.Hitreg.client;

@Mixin(ParticleEngine.class)
public class ParticleMixin {
    @Inject(method = "makeParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)Lnet/minecraft/client/particle/Particle;", at = @At("HEAD"), cancellable = true)
    private void onCreateParticle(ParticleOptions parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ, CallbackInfoReturnable<Particle> cir) {
        if (parameters.getType() == ParticleTypes.FIREWORK) return;
        if (Toggle.HIDE_ALL_PARTICLES.toggled()) cir.setReturnValue(null);
        else if (Toggle.HIDE_OTHER_PARTICLES.toggled() && parameters.getType() != ParticleTypes.CRIT && parameters.getType() != ParticleTypes.SWEEP_ATTACK) cir.setReturnValue(null);
        else if (Toggle.HIDE_OTHER_FIGHTS.toggled() && client.player != null && client.player.distanceToSqr(x, y, z) > 30) cir.setReturnValue(null);
    }
}