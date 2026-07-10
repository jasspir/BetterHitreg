package you.jass.betterhitreg.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.WalkAnimationState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import you.jass.betterhitreg.hitreg.Hitreg;
import you.jass.betterhitreg.settings.Toggle;
import you.jass.betterhitreg.utility.DontAnimate;
import you.jass.betterhitreg.utility.MultiVersion;
import you.jass.betterhitreg.utility.OnlyAnimate;

@Mixin(LivingEntity.class)
public abstract class DamageMixin {
    @Shadow @Nullable private DamageSource lastDamageSource;

    @Shadow private long lastDamageStamp;

    @Shadow @Final public WalkAnimationState walkAnimation;

    @Shadow public int hurtDuration;

    @Shadow public int hurtTime;

    @Shadow protected abstract void playHurtSound(DamageSource damageSource);

    @ModifyVariable(method = "handleDamageEvent(Lnet/minecraft/world/damagesource/DamageSource;)V", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/entity/LivingEntity;getHurtSound(Lnet/minecraft/world/damagesource/DamageSource;)Lnet/minecraft/sounds/SoundEvent;"), ordinal = 0)
    private SoundEvent handleDamageEvent(SoundEvent original, DamageSource damageSource) {
        //only silence your own hurt sound, other entities aren't "their hits"
        LivingEntity entity = (LivingEntity) (Object) this;
        boolean isYou = Hitreg.client.player != null && Hitreg.client.player.getId() == entity.getId();
        return Toggle.SILENCE_THEM.toggled() && isYou ? null : original;
    }

    @Inject(method = "handleDamageEvent", at = @At("HEAD"), cancellable = true)
    private void handleDamageEvent(DamageSource damageSource, CallbackInfo ci) {
        if (damageSource instanceof DontAnimate) {
            LivingEntity entity = (LivingEntity) (Object) this;
            entity.invulnerableTime = 20;
            lastDamageSource = damageSource;

            lastDamageStamp = MultiVersion.getLevelTime(entity);

            //if it's you play the sound since it's client sided
            if (!Toggle.SILENCE_THEM.toggled() && Hitreg.client.player != null && Hitreg.client.player.getId() == entity.getId()) playHurtSound(damageSource);

            ci.cancel();
        }

        if (damageSource instanceof OnlyAnimate) {
            walkAnimation.setSpeed(1.5F);
            hurtDuration = 10;
            hurtTime = hurtDuration;
            ci.cancel();
        }
    }
}