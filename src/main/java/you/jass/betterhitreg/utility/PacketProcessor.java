package you.jass.betterhitreg.utility;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import you.jass.betterhitreg.hitreg.HitType;
import you.jass.betterhitreg.hitreg.Hitreg;
import you.jass.betterhitreg.settings.Setting;
import you.jass.betterhitreg.settings.Toggle;

import java.util.*;

import static you.jass.betterhitreg.hitreg.Hitreg.alreadyAnimated;
import static you.jass.betterhitreg.hitreg.Hitreg.client;
import static you.jass.betterhitreg.hitreg.Hitreg.last100Regs;
import static you.jass.betterhitreg.hitreg.Hitreg.lastAnimation;
import static you.jass.betterhitreg.hitreg.Hitreg.lastAttack;
import static you.jass.betterhitreg.hitreg.Hitreg.lastAttacked;
import static you.jass.betterhitreg.hitreg.Hitreg.lastTarget;
import static you.jass.betterhitreg.hitreg.Hitreg.target;
import static you.jass.betterhitreg.utility.MultiVersion.*;

public class PacketProcessor {
    private static final Logger log = LoggerFactory.getLogger("BetterHitreg");
    public static long dealtDamageTimestamp;
    public static long tookDamageTimestamp;

    private record Received(Packet<?> packet, long timestamp) {}

    //when packets reached the network thread, the main thread can run a whole frame later which would skew the
    //timing between a hit's damage event and its sounds, so every packet we compare is stamped on arrival
    private static final Deque<Received> received = new ArrayDeque<>();

    public static void stamp(Packet<?> packet) {
        synchronized (received) {
            received.addLast(new Received(packet, System.currentTimeMillis()));
        }
    }

    public static long receivedAt(Packet<?> packet) {
        long now = System.currentTimeMillis();
        synchronized (received) {
            Iterator<Received> iterator = received.iterator();
            while (iterator.hasNext()) {
                Received entry = iterator.next();
                if (entry.packet() == packet) {
                    iterator.remove();
                    return entry.timestamp();
                }

                //drop stamps that were never looked up
                if (now - entry.timestamp() > 1000) iterator.remove();
            }
        }

        //bundled packets only ever run on the main thread, subtract 2 because it runs 1-5ms later than the network thread
        return now - 2;
    }

    public static boolean processDamage(ClientboundDamageEventPacket packet) {
        //on network thread?
        if (!Minecraft.getInstance().isSameThread()) {
            if (lastTarget == packet.entityId() || Hitreg.playerId == packet.entityId()) stamp(packet);
        }

        //on main thread?
        else {
            if (lastTarget == packet.entityId()) {
                dealtDamageTimestamp = receivedAt(packet);

                boolean withinFight = Hitreg.withinFight;
                boolean hasBeenAnimated = alreadyAnimated;
                HitTracker.add(new Animation(dealtDamageTimestamp));
                lastAnimation = dealtDamageTimestamp;
                alreadyAnimated = true;
                long delay = dealtDamageTimestamp - lastAttack;
                debug("damage event on target +" + delay + "ms after attack" + (hasBeenAnimated ? " (already animated)" : ""));
                if (Toggle.ALERT_DELAYS.toggled() && !hasBeenAnimated && delay <= 500) message("hitreg §7was §f" + delay + "§7ms", "/hitreg alertDelays");
                if (delay <= 500) last100Regs.addDelay((int) delay);
                if (!Hitreg.lastHitHandled && withinFight && Toggle.PARTICLES_EVERY_HIT.toggled()) playParticles("ENCHANTED_HIT", target);
                processDelayedSounds(true, dealtDamageTimestamp);
            }

            else if (client.player != null && client.player.getId() == packet.entityId()) {
                tookDamageTimestamp = receivedAt(packet);

                lastAttacked = tookDamageTimestamp;
                debug("damage event on you +" + (tookDamageTimestamp - lastAttack) + "ms after your attack");
                //fall/environment damage shouldn't count as their hit
                if (packet.sourceCauseId() == lastTarget) Hitreg.theirHits++;
                processDelayedSounds(false, tookDamageTimestamp);
            }
        }

        return true;
    }

    public static boolean processAnimation(ClientboundAnimatePacket packet) {
        if (lastTarget != getAction(packet)) return true;
        boolean withinFight = Hitreg.withinFight;

        //swing hand
        if (packet.getAction() == 0 || packet.getAction() == 3) Hitreg.theirSwings++;

        //crit particle
        if (packet.getAction() == 4) {
            if (Hitreg.lastHitHandled && withinFight) return false;
        }

        //enchanted particle
        else if (packet.getAction() == 5) {
            if ((Toggle.PARTICLES_EVERY_HIT.toggled() || Hitreg.lastHitHandled) && withinFight) return false;
        }

        return true;
    }

    private static Packet<?> replaying;

    public static void replay(ClientboundSoundEntityPacket packet) {
        if (client.getConnection() == null) return;
        replaying = packet;
        try {
            client.getConnection().handleSoundEntityEvent(packet);
        } finally {
            replaying = null;
        }
    }

    public static boolean processSound(Packet<?> packet) {
        if (packet == replaying) return true;
        Sound sound = Sound.of(packet);
        if (sound == null) return true;
        sound.register();

        if (sound.modern || sound.legacy) {
            boolean result = processSound(sound);
            if (!Hitreg.lastHitHandled && !Hitreg.lastSwingHandled && !Toggle.SILENCE_SELF.toggled() && !Toggle.SILENCE_THEM.toggled() && !Toggle.SILENCE_OTHER_FIGHTS.toggled()) {
                sound.skip = true;
                debug(sound, true, "custom hitreg isn't replacing this hit");
                return true;
            }
            debug(sound, result, sound.reason);
            return result;
        }

        return true;
    }

    private static final Deque<Sound> delayedSounds = new ArrayDeque<>();

    private enum Owner { YOU, THEM, OTHER }

    private static void processDelayedSounds(boolean fromYou, long damageTimestamp) {
        if (client.level == null || client.player == null) return;
        Iterator<Sound> iterator = delayedSounds.iterator();
        while (iterator.hasNext()) {
            Sound sound = iterator.next();
            if (sound.skip) {
                iterator.remove();
                continue;
            }

            //a sound held for your hit arrived after you attacked, so give it the full window to be claimed by your damage event
            long window = fromYou && sound.heldForHit ? Sound.HIT_WINDOW : (long) Setting.SOUND_RECENCY_THRESHOLD.get();
            boolean claimed = sound.distanceFromTimestamp(damageTimestamp) <= window;

            //a damage event can't claim the other side's sounds, e.g. when you trade hits and their damage event arrives first
            //keep them waiting for their own damage event, or the timeout
            if (!fromYou && sound.heldForHit && (!claimed || sound.isYourHitSound())) continue;
            if (fromYou && sound.isTheirHitSound()) continue;

            iterator.remove();
            release(sound, !claimed ? Owner.OTHER : fromYou ? Owner.YOU : Owner.THEM);
        }
    }

    //release held sounds whose damage event never came, e.g. the hit ghosted or it was another fight
    public static void tick() {
        if (delayedSounds.isEmpty()) return;
        long now = System.currentTimeMillis();
        long hold = Math.max(Sound.HIT_WINDOW, (long) Setting.SOUND_RECENCY_THRESHOLD.get());
        Iterator<Sound> iterator = delayedSounds.iterator();
        while (iterator.hasNext()) {
            Sound sound = iterator.next();
            if (sound.skip) iterator.remove();
            else if (now - sound.timestamp > hold) {
                iterator.remove();
                release(sound, Owner.OTHER);
            }
        }
    }

    private static void release(Sound sound, Owner owner) {
        boolean shouldPlay = switch (owner) {
            case YOU -> !Toggle.SILENCE_SELF.toggled() && !Hitreg.lastHitHandled;
            case THEM -> !Toggle.SILENCE_THEM.toggled();
            case OTHER -> !Toggle.SILENCE_OTHER_FIGHTS.toggled();
        };
        if (shouldPlay) sound.play();
        debug(sound, shouldPlay, "held, released as " + owner.name().toLowerCase() + (owner == Owner.OTHER ? " (no damage event in time)" : ""));
    }

    private static boolean processSound(Sound sound) {
        boolean soundWithinFight = sound.withinFight();

        //if the sound happened far away, then block it if were silencing other fights and skip it if were not
        if (!soundWithinFight) {
            sound.reason = "outside the fight";
            return !Toggle.SILENCE_OTHER_FIGHTS.toggled();
        }

        //Disabling a raised shield is not a sprint/knockback hit. The server can still send the
        //knockback attack sound for it, so suppress just that sound while leaving shield sounds
        //and any other server feedback untouched.
        if (Hitreg.lastAttackWasBlocked && sound.hitType == HitType.KNOCKBACK && sound.couldBeFromYou()) {
            sound.reason = "knockback on a blocked hit";
            return false;
        }

        //block nodamage sounds because they don't actually register hits so we don't know who they're from
        //nodamage answers a swing rather than a registered hit, so use the swing-time decision
        if (Hitreg.lastSwingHandled && sound.sound.contains("nodamage")) {
            sound.reason = "nodamage";
            return false;
        }

        //if the sound wasn't from either of you
        boolean fromYou = sound.wasFromYou();
        boolean fromThem = sound.wasFromThem();
        if (!fromYou && !fromThem) {
            //hold sounds that may have arrived before the damage event that registers their hit, then let that damage event decide
            //vanilla sends the knockback sound before it, and other servers can send any of a hit's sounds early
            //don't hold it if its already been processed though, or it would just keep delaying indefinitely
            sound.heldForHit = sound.couldBeFromYou() && !alreadyAnimated && HitType.of(sound.event) != null;
            if (!sound.processed && (sound.heldForHit || sound.sound.contains("knockback"))) {
                sound.processed = true;
                sound.reason = "held for a damage event";
                delayedSounds.add(sound);
                return false;
            }

            //if it wasn't from either of you and were silencing other fights, silence it
            sound.reason = "not matched to you or them";
            if (Toggle.SILENCE_OTHER_FIGHTS.toggled()) return false;
        }

        //block the sound based on whether you hit them or they hit you
        if (fromYou && (Hitreg.lastHitHandled || Toggle.SILENCE_SELF.toggled())) {
            sound.reason = "from your hit";
            return false;
        }

        if (fromThem && Toggle.SILENCE_THEM.toggled()) {
            sound.reason = "from their hit";
            return false;
        }

        //block all modern attack sounds if legacy sounds are enabled
        if (!sound.legacy && Toggle.LEGACY_SOUNDS.toggled() && !sound.sound.contains("hurt")) {
            sound.reason = "modern attack sound";
            return false;
        }

        if (fromYou) sound.reason = "from your hit, custom hitreg off";
        else if (fromThem) sound.reason = "from their hit";
        return true;
    }

    public static void debug(String text) {
        if (Toggle.DEBUG_SOUNDS.toggled()) log.info("[debug sounds] {}", text);
    }

    private static void debug(Sound sound, boolean played, String reason) {
        if (!Toggle.DEBUG_SOUNDS.toggled() || !sound.withinFight()) return;
        long sinceDamage = sound.timestamp - lastAnimation;
        debug((played ? "played " : "blocked ") + sound + ", " + (sinceDamage >= 0 ? "+" : "") + sinceDamage + "ms from target damage event: " + reason);

        //the server's sound played on top of your custom one, their hit's sounds are supposed to play
        if (played && Hitreg.lastHitHandled && sound.hitType != null && sound.couldBeFromYou() && !sound.isTheirHitSound() && (sound.isYourHitSound() || !sound.reason.startsWith("from their hit"))) {
            message("§cpossible double §7" + sound + " (" + reason + ")", "/hitreg debugSounds");
        }
    }
}
