package you.jass.betterhitreg.mixin;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.tags.ItemTags;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import you.jass.betterhitreg.hitreg.Hit;
import you.jass.betterhitreg.hitreg.Hitreg;
import you.jass.betterhitreg.utility.MultiVersion;

import static you.jass.betterhitreg.hitreg.Hitreg.*;

@Mixin(MultiPlayerGameMode.class)
public abstract class AttackMixin {
    @Inject(method = "attack", at = @At("HEAD"))
    private static void attack(Player player, Entity target, CallbackInfo ci) {
        if (client.player == null || !(target instanceof LivingEntity) || target instanceof ArmorStand || !target.isAlive() || target.isInvulnerable()) return;
        Hitreg.target = (LivingEntity) target;

        //hitting before 500ms is too fast to deal damage, lower it by half a tick (25ms) because it's not exact and can be lower
        long sinceLastHit = System.currentTimeMillis() - lastAttack;
        boolean hitEarly = sinceLastHit < 475;

        //load the hit, this is where our custom hit animation & sound prepares to play
        Hit hit = new Hit();
        hit.target = Hitreg.target;
        hit.cooldown = client.player.getAttackStrengthScale(0.5f);
        hit.tooEarlyForDamage = hitEarly;
        hit.tooEarlyForSpecial = hit.cooldown <= 0.9f;
        hit.hadShield = Hitreg.target.isHolding(Items.SHIELD);
        hit.wasBlocked = Hitreg.target.isBlocking();
        hit.wasSprinting = client.player.isSprinting();
        hit.wasFalling = client.player.fallDistance > 0;
        hit.wasOnGround = MultiVersion.isOnGround(client.player);
        hit.wasClimbing = client.player.onClimbable();
        hit.wasTouchingWater = client.player.isInWater();
        hit.wasInVehicle = client.player.isPassenger();
        hit.wasBlind = client.player.hasEffect(MobEffects.BLINDNESS);
        hit.wasHoldingSword = client.player.getMainHandItem().is(ItemTags.SWORDS);
        hit.wasMovingFast = MultiVersion.isMovingFast();
        hit.wasMovingForward = client.options.keyUp.isDown();
        hit.swordHadSharpness = MultiVersion.hasSharpness();
        hit.sprintWasReset = sprintIsReset;
        hit.wasNewTarget = lastTarget != target.getId();
        hit.wasHitByAnother = target.invulnerableTime > 10 && sinceLastHit >= 1000;
        hit.wasInvisible = target.isInvisible();


        if (!hitEarly) {
            hitWasFarFromPrevious = lastAttackLocation.distanceToSqr(MultiVersion.getBasePosition(client.player)) >= 2500;
            if (!fighting) fightStartedAt = System.currentTimeMillis();
            fighting = true;
            hitByAnother = hit.wasHitByAnother;
            newTarget = hit.wasNewTarget;
            lastAttackWasBlocked = hit.wasBlocked;
            lastAttackLocation = MultiVersion.getBasePosition(client.player);
            lastAttack = System.currentTimeMillis();
            lastTarget = target.getId();
            usedItem = false;
            lastAttackTick = tick;
            lastHitWasSpecial = !hit.tooEarlyForSpecial;
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
