package you.jass.betterhitreg.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LimbAnimator;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.sound.SoundEvent;
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
import you.jass.betterhitreg.utility.OnlyAnimate;

@Mixin(LivingEntity.class)
public abstract class DamageMixin {
    @Shadow @Nullable private DamageSource lastDamageSource;

    @Shadow private long lastDamageTime;

    @Shadow @Final public LimbAnimator limbAnimator;

    @Shadow public int maxHurtTime;

    @Shadow public int hurtTime;

    @Shadow protected abstract void playHurtSound(DamageSource damageSource);

    @ModifyVariable(method = "onDamaged(Lnet/minecraft/entity/damage/DamageSource;)V", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/entity/LivingEntity;getHurtSound(Lnet/minecraft/entity/damage/DamageSource;)Lnet/minecraft/sound/SoundEvent;"), ordinal = 0)
    private SoundEvent onDamaged(SoundEvent original, DamageSource damageSource) {
        //only silence your own hurt sound, other entities aren't "their hits"
        LivingEntity entity = (LivingEntity) (Object) this;
        boolean isYou = Hitreg.client.player != null && Hitreg.client.player.getId() == entity.getId();
        return Toggle.SILENCE_THEM.toggled() && isYou ? null : original;
    }

    @Inject(method = "onDamaged", at = @At("HEAD"), cancellable = true)
    private void onDamaged(DamageSource damageSource, CallbackInfo ci) {
        if (damageSource instanceof DontAnimate) {
            LivingEntity entity = (LivingEntity) (Object) this;
            entity.timeUntilRegen = 20;
            lastDamageSource = damageSource;

            //version 1.21.8-
            //lastDamageTime = entity.getWorld().getTime();

            //version 1.21.9+
            lastDamageTime = entity.getEntityWorld().getTime();

            //if it's you play the sound since it's client sided
            if (!Toggle.SILENCE_THEM.toggled() && Hitreg.client.player != null && Hitreg.client.player.getId() == entity.getId()) playHurtSound(damageSource);

            ci.cancel();
        }

        if (damageSource instanceof OnlyAnimate) {
            limbAnimator.setSpeed(1.5F);
            maxHurtTime = 10;
            hurtTime = maxHurtTime;
            ci.cancel();
        }
    }
}