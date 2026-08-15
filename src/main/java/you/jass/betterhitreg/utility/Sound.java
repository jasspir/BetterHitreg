package you.jass.betterhitreg.utility;

import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.phys.Vec3;
import you.jass.betterhitreg.hitreg.HitType;
import you.jass.betterhitreg.hitreg.Hitreg;
import you.jass.betterhitreg.settings.Setting;
import you.jass.betterhitreg.settings.Toggle;

import static you.jass.betterhitreg.hitreg.Hitreg.*;

public class Sound {
    public ClientboundSoundPacket packet;
    public String sound;
    public Vec3 location;
    public SoundEvent event;
    public HitType hitType;
    public long timestamp;
    public boolean modern;
    public boolean legacy;
    public boolean processed;
    public boolean skip;

    public Sound(ClientboundSoundPacket packet) {
        this.packet = packet;
        this.sound = packet.getSound().unwrapKey().isPresent() ? packet.getSound().unwrapKey().get().toString() : "";
        this.location = new Vec3(packet.getX(), packet.getY(), packet.getZ());
        this.event = packet.getSound().value();

        //subtract 2 because we run this on the main thread which runs 1-5ms later than the network thread
        this.timestamp = System.currentTimeMillis() - 2;

        this.legacy = packet.getSound().kind() == Holder.Kind.DIRECT;
        if (!legacy) this.modern = sound.contains("hurt") || sound.contains("player.attack");
    }

    public void register() {
        if ((modern || legacy) && couldBeFromYou()) {
            this.hitType = HitType.of(event);
            if (hitType != null) HitTracker.add(this);
        }
    }

    public void play() {
        if (client.level == null) return;
        client.level.playSeededSound(client.player, packet.getX(), packet.getY(), packet.getZ(), packet.getSound(), packet.getSource(), packet.getVolume(), packet.getPitch(), packet.getSeed());
    }

    public boolean nearPlayer() {
        return distanceFromPlayer(location) <= 5.5;
    }

    public boolean nearTarget() {
        return distanceFromTarget(location) <= 5.5;
    }

    public boolean withinFight() {
        return nearPlayer() || nearTarget();
    }

    public boolean wasRecent() {
        return distanceFromTimestamp(System.currentTimeMillis()) <= Setting.SOUND_RECENCY_THRESHOLD.get();
    }

    public long distanceFromTimestamp(long time) {
        return Math.abs(time - timestamp);
    }

    //15ms was the highest amount of jitter that I found didn't affect other fights
    //use 50ms for most cases except silencing other fights in case the user has an unstable connection

    public boolean wasFromYou() {
        //if you attacked over a second ago, it wasn't you assuming your hit didn't have 1,000ms delay
        if (timestamp - lastAttack > 1000) return false;
        long you = distanceFromTimestamp(lastAnimation);
        long them = distanceFromTimestamp(lastAttacked);
        return you <= them && you <= (Toggle.SILENCE_OTHER_FIGHTS.toggled() ? 15 : 50);
    }

    public boolean wasFromThem() {
        long you = distanceFromTimestamp(lastAnimation);
        long them = distanceFromTimestamp(lastAttacked);
        return them <= you && them <= (Toggle.SILENCE_OTHER_FIGHTS.toggled() ? 15 : 50);
    }

    public boolean couldBeFromYou() {
        return timestamp - lastAttack <= 500 && Hitreg.withinFight && nearTarget();
    }
}