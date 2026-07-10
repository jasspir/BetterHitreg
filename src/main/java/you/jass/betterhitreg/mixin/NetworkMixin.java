package you.jass.betterhitreg.mixin;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import you.jass.betterhitreg.settings.Toggle;
import you.jass.betterhitreg.utility.DontAnimate;

import static you.jass.betterhitreg.hitreg.Hitreg.*;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class NetworkMixin {
    @ModifyArg(method = "onEntityDamage(Lnet/minecraft/network/packet/s2c/play/EntityDamageS2CPacket;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;onDamaged(Lnet/minecraft/entity/damage/DamageSource;)V"))
    private DamageSource onEntityDamage(DamageSource damageSource) {
        if (Toggle.HIDE_ANIMATIONS.toggled()) return new DontAnimate(damageSource);
        else if (damageSource != null && damageSource.getAttacker() != null && client.player != null && client.player.getId() == damageSource.getAttacker().getId() && lastHitHandled && withinFight && System.currentTimeMillis() - lastAttack <= 1000) return new DontAnimate(damageSource);
        return damageSource;
    }
}