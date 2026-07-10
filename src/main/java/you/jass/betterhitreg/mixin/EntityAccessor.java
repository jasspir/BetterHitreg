package you.jass.betterhitreg.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface EntityAccessor {
    @Invoker("getHurtSound")
    SoundEvent getHurtSound(DamageSource source);
}