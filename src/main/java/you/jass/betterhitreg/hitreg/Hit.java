package you.jass.betterhitreg.hitreg;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.phys.Vec3;
import you.jass.betterhitreg.settings.Settings;
import you.jass.betterhitreg.settings.Toggle;
import you.jass.betterhitreg.utility.HitTracker;
import you.jass.betterhitreg.utility.MultiVersion;
import you.jass.betterhitreg.utility.OnlyAnimate;
import you.jass.betterhitreg.utility.Scheduler;

import java.util.ArrayList;

import static you.jass.betterhitreg.hitreg.Hitreg.*;
import static you.jass.betterhitreg.hitreg.Hitreg.alreadyAnimated;
import static you.jass.betterhitreg.hitreg.Hitreg.alreadyKnockedBack;
import static you.jass.betterhitreg.hitreg.Hitreg.lastTarget;
import static you.jass.betterhitreg.hitreg.Hitreg.newTarget;
import static you.jass.betterhitreg.hitreg.Hitreg.sprintIsReset;
import static you.jass.betterhitreg.utility.MultiVersion.*;

public class Hit {
    public LivingEntity target;
    public float cooldown;
    public boolean tooEarlyForDamage;
    public boolean tooEarlyForSpecial;
    public boolean hadShield;
    public boolean wasBlocked;
    public boolean wasSprinting;
    public boolean wasMovingFast;
    public boolean wasMovingForward;
    public boolean wasFalling;
    public boolean wasOnGround;
    public boolean wasClimbing;
    public boolean wasTouchingWater;
    public boolean wasInVehicle;
    public boolean wasBlind;
    public boolean wasInvisible;
    public boolean wasHoldingSword;
    public boolean swordHadSharpness;
    public boolean sprintWasReset;

    public boolean shouldAnimate;
    public boolean shouldMakeSound;
    public boolean shouldSoundBeLegacy;
    public boolean shouldSpawnParticles;
    public boolean shouldKnockback;
    public boolean shouldCrit;
    public boolean shouldSweep;
    public boolean shouldPick;
    public boolean shouldFullPick;
    public boolean shouldHalfPick;
    public boolean shouldSpawnSharpnessParticles;

    public SoundEvent expectedSound;
    public HitType type;
    public ArrayList<HitType> potentialServerTypes = new ArrayList<>();
    public boolean wasServerRight;
    public boolean wasAnimated;
    public boolean wasNewTarget;
    public boolean wasHitByAnother;
    public long timestamp;

    public Hit() {
        timestamp = System.currentTimeMillis();
    }

    public void updateSettings() {
        shouldAnimate = !Toggle.HIDE_ANIMATIONS.toggled() && !tooEarlyForDamage;
        shouldMakeSound = !Toggle.SILENCE_SELF.toggled();
        shouldSoundBeLegacy = Toggle.LEGACY_SOUNDS.toggled();
        shouldSpawnParticles = !Toggle.HIDE_ALL_PARTICLES.toggled();
        shouldSpawnSharpnessParticles = !Toggle.HIDE_OTHER_PARTICLES.toggled() && (swordHadSharpness || Toggle.PARTICLES_EVERY_HIT.toggled());
    }

    public void load() {
        shouldKnockback = !tooEarlyForSpecial && wasSprinting && sprintWasReset;
        shouldCrit = !tooEarlyForSpecial && !shouldKnockback && wasFalling && !wasOnGround && !wasClimbing && !wasTouchingWater && !wasInVehicle && !wasBlind;

        //version 1.21.1-
        //shouldSweep = !tooEarlyForSpecial && !shouldKnockback && wasHoldingSword && wasOnGround && !wasMovingFast;

        //version 1.21.2+
        shouldSweep = !tooEarlyForSpecial && !shouldKnockback && wasHoldingSword && wasOnGround && !wasMovingFast && !wasMovingForward;

        shouldPick = !shouldKnockback && !shouldCrit && !shouldSweep;
        shouldFullPick = !tooEarlyForSpecial && shouldPick;
        shouldHalfPick = !shouldFullPick && shouldPick;

        type = HitType.of(this);
        if (type == null) return;
        expectedSound = type.getMainSound();

        //decide once at hit time whether the mod replaces the server's feedback, the target's blocking state may change before the server's packets arrive
        boolean handled = Hitreg.isToggled();
        Hitreg.lastSwingHandled = handled;

        if (!tooEarlyForDamage) {
            HitTracker.add(this);
            Hitreg.lastHitHandled = handled;
        }

        if (handled) Scheduler.schedule(Settings.getHitreg(), this::run);
    }

    public void run() {
        if (target == null) return;
        updateSettings();

        if (shouldAnimate) {target.handleDamageEvent(new OnlyAnimate(target.damageSources().generic()));

        if (shouldMakeSound) {
            Vec3 location = getLerpedPosition(target);

            if (shouldSoundBeLegacy) {
                if (!tooEarlyForDamage) HitType.HALF_PICK.playSounds(location);
            } else {
                type.playSounds(location);
            }
        }

        if (shouldSpawnParticles) {
            if (shouldCrit) playParticles("CRIT", target);
            if (shouldSpawnSharpnessParticles) playParticles("ENCHANTED_HIT", target);
        }
    }
}
