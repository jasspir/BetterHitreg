package you.jass.betterhitreg.mixin;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.math.MathHelper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import you.jass.betterhitreg.hitreg.Hit;
import you.jass.betterhitreg.hitreg.Hitreg;
import you.jass.betterhitreg.utility.MultiVersion;

import static you.jass.betterhitreg.hitreg.Hitreg.*;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class AttackMixin {
    @Inject(method = "attackEntity", at = @At("HEAD"))
    private static void attack(PlayerEntity player, Entity target, CallbackInfo ci) {
        if (client.player == null || !(target instanceof LivingEntity) || target instanceof ArmorStandEntity || !target.isAlive() || target.isInvulnerable()) return;
        Hitreg.target = (LivingEntity) target;

        //hitting before 500ms is too fast to deal damage, lower it by half a tick (25ms) because it's not exact and can be lower
        long sinceLastHit = System.currentTimeMillis() - lastAttack;
        boolean hitEarly = sinceLastHit < 475;

        //load the hit, this is where our custom hit animation & sound prepares to play
        Hit hit = new Hit();
        hit.target = Hitreg.target;
        hit.cooldown = client.player.getAttackCooldownProgress(0.5f);
        hit.tooEarlyForDamage = hitEarly;
        hit.tooEarlyForSpecial = hit.cooldown <= 0.9f;
        hit.hadShield = Hitreg.target.isHolding(Items.SHIELD);
        hit.wasBlocked = Hitreg.target.isBlocking();
        hit.wasSprinting = client.player.isSprinting();
        hit.wasFalling = client.player.fallDistance > 0;
        hit.wasOnGround = client.player.isOnGround();
        hit.wasClimbing = client.player.isClimbing();
        hit.wasTouchingWater = client.player.isTouchingWater();
        hit.wasInVehicle = client.player.hasVehicle();
        hit.wasBlind = client.player.hasStatusEffect(StatusEffects.BLINDNESS);
        hit.wasHoldingSword = client.player.getMainHandStack().isIn(ItemTags.SWORDS);
        //vanilla only sweeps below 2.5x movement speed
        hit.wasMovingFast = client.player.getMovement().horizontalLengthSquared() >= MathHelper.square(client.player.getMovementSpeed() * 2.5);
        hit.swordHadSharpness = MultiVersion.hasSharpness();
        hit.sprintWasReset = sprintIsReset;
        hit.wasNewTarget = lastTarget != target.getId();
        hit.wasHitByAnother = target.timeUntilRegen > 10 && sinceLastHit >= 1000;
        hit.wasInvisible = target.isInvisible();

        if (!hitEarly) {
            hitWasFarFromPrevious = lastAttackLocation.squaredDistanceTo(MultiVersion.getBasePosition(client.player)) >= 2500;
            if (!fighting) fightStartedAt = System.currentTimeMillis();
            fighting = true;
            hitByAnother = hit.wasHitByAnother;
            newTarget = hit.wasNewTarget;
            lastAttackLocation = MultiVersion.getBasePosition(client.player);
            lastAttack = System.currentTimeMillis();
            lastTarget = target.getId();
            sprintIsReset = false;
            alreadyAnimated = false;
            alreadyKnockedBack = false;
            yourHits++;
            updateFightState();

            //if they hit the opponent on the first tick available after not being in range
            if (lastTickInRange == tick && lastTickOutOfRange == tick - 1) lastPerfectHit = System.currentTimeMillis();
        }

        hit.load();
    }
}