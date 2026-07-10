package you.jass.betterhitreg.utility;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import you.jass.betterhitreg.hitreg.Hitreg;
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
    public static long dealtDamageTimestamp;
    public static long tookDamageTimestamp;

    public static boolean processDamage(ClientboundDamageEventPacket packet) {
        //on network thread?
        if (!Minecraft.getInstance().isSameThread()) {
            if (lastTarget == packet.entityId()) {
                dealtDamageTimestamp = System.currentTimeMillis();
            } else if (Hitreg.playerId == packet.entityId()) tookDamageTimestamp = System.currentTimeMillis();
        }

        //on main thread?
        else {
            if (lastTarget == packet.entityId()) {
                //if processing never ran on the network thread, we need to update the timestamp here
                if (System.currentTimeMillis() - dealtDamageTimestamp > 50) dealtDamageTimestamp = System.currentTimeMillis() - 2;

                boolean withinFight = Hitreg.withinFight;
                boolean hasBeenAnimated = alreadyAnimated;
                HitTracker.add(new Animation(dealtDamageTimestamp));
                lastAnimation = dealtDamageTimestamp;
                alreadyAnimated = true;
                long delay = dealtDamageTimestamp - lastAttack;
                if (Toggle.ALERT_DELAYS.toggled() && !hasBeenAnimated && delay <= 500) message("hitreg §7was §f" + delay + "§7ms", "/hitreg alertDelays");
                if (delay <= 500) last100Regs.addDelay((int) delay);
                if (!Hitreg.lastHitHandled && withinFight && Toggle.PARTICLES_EVERY_HIT.toggled()) playParticles("ENCHANTED_HIT", target);
                processDelayedSounds(true);
            }

            else if (client.player != null && client.player.getId() == packet.entityId()) {
                //if processing never ran on the network thread, we need to update the timestamp here
                if (System.currentTimeMillis() - tookDamageTimestamp > 50) tookDamageTimestamp = System.currentTimeMillis() - 2;

                lastAttacked = tookDamageTimestamp;
                //fall/environment damage shouldn't count as their hit
                if (packet.sourceCauseId() == lastTarget) Hitreg.theirHits++;
                processDelayedSounds(false);
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

    public static boolean processSound(ClientboundSoundPacket packet) {
        Sound sound = new Sound(packet);
        sound.register();

        if (sound.modern || sound.legacy) {
            boolean result = processSound(sound);
            if (!Hitreg.lastHitHandled && !Hitreg.lastSwingHandled && !Toggle.SILENCE_SELF.toggled() && !Toggle.SILENCE_THEM.toggled() && !Toggle.SILENCE_OTHER_FIGHTS.toggled()) {
                sound.skip = true;
                return true;
            }
            return result;
        }

        return true;
    }

    private static final Queue<Sound> delayedSounds = new LinkedList<>();

    private static void processDelayedSounds(boolean fromYou) {
        if (client.level == null || client.player == null) return;
        Sound sound;
        while ((sound = delayedSounds.poll()) != null) {
            if (sound.skip) continue;
            boolean shouldPlay;
            if (!sound.wasRecent()) shouldPlay = !Toggle.SILENCE_OTHER_FIGHTS.toggled();
            else if (fromYou) shouldPlay = !Toggle.SILENCE_SELF.toggled() && !Hitreg.lastHitHandled;
            else shouldPlay = !Toggle.SILENCE_THEM.toggled();
            if (shouldPlay) sound.play();
        }
    }

    private static boolean processSound(Sound sound) {
        boolean soundWithinFight = sound.withinFight();

        //if the sound happened far away, then block it if were silencing other fights and skip it if were not
        if (!soundWithinFight) return !Toggle.SILENCE_OTHER_FIGHTS.toggled();

        //block nodamage sounds because they don't actually register hits so we don't know who they're from
        //nodamage answers a swing rather than a registered hit, so use the swing-time decision
        if (Hitreg.lastSwingHandled && sound.sound.contains("nodamage")) return false;

        //if the sound wasn't from either of you
        boolean fromYou = sound.wasFromYou();
        boolean fromThem = sound.wasFromThem();
        if (!fromYou && !fromThem) {
            //delay knockback sounds because for some reason knockback sounds come before hit registration on most servers
            //don't delay it if its already been processed though, or it would just keep delaying indefinitely
            //if it was from either of you, no need to delay it as it came after hit registration, minemenclub sends it after
            if (!sound.processed && sound.sound.contains("knockback")) {
                sound.processed = true;
                delayedSounds.add(sound);
                return false;
            }

            //if it wasn't from either of you and were silencing other fights, silence it
            if (Toggle.SILENCE_OTHER_FIGHTS.toggled()) return false;
        }

        //block the sound based on whether you hit them or they hit you
        if (fromYou && (Hitreg.lastHitHandled || Toggle.SILENCE_SELF.toggled())) return false;
        if (fromThem && Toggle.SILENCE_THEM.toggled()) return false;

        //block all modern attack sounds if legacy sounds are enabled
        if (!sound.legacy && Toggle.LEGACY_SOUNDS.toggled() && !sound.sound.contains("hurt")) return false;

        return true;
    }
}