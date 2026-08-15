package you.jass.betterhitreg.hitreg;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Arrays;

public enum HitType {
    TOO_EARLY(SoundEvents.PLAYER_ATTACK_WEAK),
    KNOCKBACK(SoundEvents.PLAYER_ATTACK_KNOCKBACK,
            SoundEvents.PLAYER_ATTACK_STRONG,
            SoundEvents.PLAYER_HURT),
    CRITICAL(SoundEvents.PLAYER_ATTACK_CRIT,
            SoundEvents.PLAYER_HURT),
    SWEEP(SoundEvents.PLAYER_ATTACK_SWEEP,
            SoundEvents.PLAYER_HURT),
    FULL_PICK(SoundEvents.PLAYER_ATTACK_STRONG,
            SoundEvents.PLAYER_HURT),
    HALF_PICK(SoundEvents.PLAYER_HURT);

    private final List<SoundEvent> sounds;

    HitType(SoundEvent... sounds) {
        this.sounds = Arrays.asList(sounds);
    }

    public void playSounds(Vec3 location) {
        Minecraft client = Hitreg.client;
        if (client.level == null || client.player == null) return;
        for (SoundEvent sound : sounds) {
            if (sound.equals(SoundEvents.PLAYER_HURT)) sound = getHurtSound();
            client.level.playSound(client.player, location.x, location.y, location.z, sound, SoundSource.PLAYERS, 1, 1);
        }
    }

    public List<SoundEvent> getSounds() {
        return sounds;
    }

    public SoundEvent getMainSound() {
        //version 1.19.4 - 1.20.4
        //return sounds.isEmpty() ? null : sounds.get(0);

        //version 1.20.5+
        return sounds.getFirst();
    }

    public static HitType of(Hit hit) {
        if (hit.tooEarlyForDamage) return TOO_EARLY;
        if (hit.shouldKnockback) return KNOCKBACK;
        if (hit.shouldCrit) return CRITICAL;
        if (hit.shouldSweep) return SWEEP;
        if (hit.shouldFullPick) return FULL_PICK;
        if (hit.shouldHalfPick) return HALF_PICK;
        return null;
    }

    public static HitType of(SoundEvent sound) {
        if (sound.equals(SoundEvents.PLAYER_ATTACK_WEAK)) return TOO_EARLY;
        if (sound.equals(SoundEvents.PLAYER_ATTACK_KNOCKBACK)) return KNOCKBACK;
        if (sound.equals(SoundEvents.PLAYER_ATTACK_CRIT)) return CRITICAL;
        if (sound.equals(SoundEvents.PLAYER_ATTACK_SWEEP)) return SWEEP;
        if (sound.equals(SoundEvents.PLAYER_ATTACK_STRONG)) return FULL_PICK;
        if (sound.equals(SoundEvents.PLAYER_HURT)) return HALF_PICK;

        //version 1.21.1-
        //if (sound.getLocation().toLanguageKey().contains("hurt")) return HALF_PICK;

        //version 1.21.2+
        if (sound.location().toLanguageKey().contains("hurt")) return HALF_PICK;

        return null;
    }
    
    public static SoundEvent getHurtSound() {
        if (Hitreg.target == null) return SoundEvents.PLAYER_HURT;
        return Hitreg.target.getHurtSound(Hitreg.target.damageSources().generic());
    }
}
