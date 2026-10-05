package you.jass.betterhitreg.utility;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import you.jass.betterhitreg.hitreg.HitType;
import you.jass.betterhitreg.hitreg.Hitreg;
import you.jass.betterhitreg.settings.Setting;
import you.jass.betterhitreg.settings.Toggle;

import static you.jass.betterhitreg.hitreg.Hitreg.*;

public class Sound {
    public Packet<?> packet;
    public String sound;
    public Vec3 location;
    public SoundEvent event;
    public HitType hitType;
    public long timestamp;
    public boolean modern;
    public boolean legacy;
    public boolean processed;
    public boolean skip;
    public boolean heldForHit;
    public Entity attachedTo;
    public String reason = "";

    private Sound(Packet<?> packet, Holder<SoundEvent> holder, Vec3 location) {
        this.packet = packet;
        this.location = location;
        this.event = holder.value();

        //servers like mcpvp send the sound event inline instead of by registry id, so it has no key, read the name off the event itself
        this.sound = holder.unwrapKey().isPresent() ? holder.unwrapKey().get().toString() : MultiVersion.getSoundName(event);

        //use the time the packet reached the network thread so it lines up with damage event timestamps
        this.timestamp = PacketProcessor.receivedAt(packet);

        this.legacy = holder.kind() == Holder.Kind.DIRECT;
        if (!legacy) this.modern = sound.contains("hurt") || sound.contains("player.attack");
    }

    public static Sound of(Packet<?> packet) {
        if (packet instanceof ClientboundSoundPacket positioned) {
            return new Sound(packet, positioned.getSound(), new Vec3(positioned.getX(), positioned.getY(), positioned.getZ()));
        }

        //some servers attach hit sounds to an entity instead of a position
        if (packet instanceof ClientboundSoundEntityPacket attached && client.level != null) {
            Entity entity = client.level.getEntity(attached.getId());
            if (entity == null) return null;
            Sound sound = new Sound(packet, attached.getSound(), MultiVersion.getBasePosition(entity));
            sound.attachedTo = entity;
            return sound;
        }

        return null;
    }

    public void register() {
        if ((modern || legacy) && couldBeFromYou()) {
            this.hitType = HitType.of(event);
            if (hitType != null) HitTracker.add(this);
        }
    }

    public void play() {
        if (client.level == null) return;
        if (packet instanceof ClientboundSoundPacket positioned) client.level.playSeededSound(client.player, positioned.getX(), positioned.getY(), positioned.getZ(), positioned.getSound(), positioned.getSource(), positioned.getVolume(), positioned.getPitch(), positioned.getSeed());
        else if (packet instanceof ClientboundSoundEntityPacket attached) PacketProcessor.replay(attached);
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
        if (isTheirHitSound() || you > them && !isYourHitSound()) return false;
        return you <= (Toggle.SILENCE_OTHER_FIGHTS.toggled() ? 15 : Setting.SOUND_RECENCY_THRESHOLD.get());
    }

    public boolean wasFromThem() {
        long you = distanceFromTimestamp(lastAnimation);
        long them = distanceFromTimestamp(lastAttacked);
        return !isYourHitSound() && them <= you && them <= (Toggle.SILENCE_OTHER_FIGHTS.toggled() ? 15 : Setting.SOUND_RECENCY_THRESHOLD.get());
    }

    //attack sounds play at the attacker and hurt sounds at whoever was hurt, so when you trade hits
    //the location still tells your hit's sounds apart from theirs even if the timing can't
    public boolean isYourHitSound() {
        if (sound.contains("player.attack")) return closerToYou();
        if (sound.contains("hurt")) return closerToTarget();
        return false;
    }

    public boolean isTheirHitSound() {
        if (sound.contains("player.attack")) return closerToTarget();
        if (sound.contains("hurt")) return closerToYou();
        return false;
    }

    private boolean closerToYou() {
        return distanceFromPlayer(location) + 1 < distanceFromTarget(location);
    }

    private boolean closerToTarget() {
        return distanceFromTarget(location) + 1 < distanceFromPlayer(location);
    }

    public boolean couldBeFromYou() {
        return timestamp - lastAttack <= 500 && timestamp >= lastAttack && Hitreg.withinFight && nearTarget();
    }

    @Override
    public String toString() {
        String name = sound.replace("ResourceKey[minecraft:sound_event / ", "").replace("]", "").replace("minecraft:", "");
        String side = closerToYou() ? "at you" : closerToTarget() ? "at target" : "between you";
        if (attachedTo != null) side = "attached to " + (client.player != null && attachedTo.getId() == client.player.getId() ? "you" : target != null && attachedTo.getId() == target.getId() ? "target" : "entity " + attachedTo.getId());
        return name + " (" + side + ") +" + (timestamp - lastAttack) + "ms after attack";
    }
}
