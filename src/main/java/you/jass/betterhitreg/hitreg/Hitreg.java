package you.jass.betterhitreg.hitreg;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import you.jass.betterhitreg.settings.Commands;
import you.jass.betterhitreg.settings.Setting;
import you.jass.betterhitreg.settings.Settings;
import you.jass.betterhitreg.settings.Toggle;
import you.jass.betterhitreg.ui.UIUtils;
import you.jass.betterhitreg.utility.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static you.jass.betterhitreg.utility.MultiVersion.message;

public class Hitreg {
    private static final Logger log = LoggerFactory.getLogger(Hitreg.class);
    public static Minecraft client;
    public static int lastTarget;
    public static LivingEntity target;
    public static int tick;
    public static long lastAttack;
    public static long lastAttacked;
    public static long lastAnimation;
    public static boolean alreadyAnimated;
    public static boolean alreadyKnockedBack;
    public static boolean wasMovingForward;
    public static boolean sprintIsReset = true;
    public static boolean lastAttackWasBlocked;
    public static boolean lastHitHandled;
    public static boolean lastSwingHandled;
    public static boolean fighting = false;
    public static boolean wasGhosted;
    public static boolean newTarget = true;
    public static boolean hitByAnother;
    public static boolean targetHasShield;
    public static boolean targetIsBlocking;
    public static boolean hitWasFarFromPrevious;
    public static RegQueue last100Regs = new RegQueue(100);
    public static Vec3 lastAttackLocation = Vec3.ZERO;
    public static Vec3 targetLocation = Vec3.ZERO;
    public static Vec3 previousTargetLocation = Vec3.ZERO;
    public static long fightStartedAt;
    public static double ground;
    public static boolean inSky;
    public static double theirGround;
    public static boolean theirInSky;
    public static boolean bothAlive;
    public static boolean withinFight;
    public static boolean targetInvisible;
    public static double distance;
    public static int lastTickHit;
    public static int lastTickInRange;
    public static int lastTickOutOfRange;
    public static int lastTickJumped;
    public static int lastTickBacked;
    public static long lastJumpReset;
    public static long lastPerfectHit;
    public static int playerId;
    public static int fightsThisSession;
    public static long lastNonGhost;
    public static int shouldFilter;
    public static float muffleAmount;
    public static float sharpenAmount;
    public static int yourSwings;
    public static int theirSwings;
    public static int yourHits;
    public static int theirHits;
    public static boolean wasSwinging;
    public static boolean tutorialAlreadySeen;
    public static int tickWorldLoaded;

    //TODO make opponent swing start fight

    public static void tick() {
        if (client.player == null || client.level == null) return;
        tick++;
        playerId = client.player.getId();

        int metronome = Settings.getInt("metronome");

        if (metronome >= 10) {
            if (tick % metronome == 0) {
                client.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1));
            }
        }

        muffleAmount = (Math.max(0, Math.min(1, Settings.getFloat("muffle_amount"))));
        sharpenAmount = (Math.max(0, Math.min(1, Settings.getFloat("sharpen_amount"))));

        UIUtils.update();
        updateFightState();
        updateGround();

        //A hit can briefly toggle the client's sprint state while W remains held. Only a new
        //forward input represents a real sprint reset; otherwise the next hit sounds like a
        //sprint hit even though sprint was never reset.
        boolean movingForward = client.options.keyUp.isDown();
        if (movingForward && !wasMovingForward) sprintIsReset = true;
        wasMovingForward = movingForward;

        boolean swinging = client.options.keyAttack.isDown();
        if (swinging && !wasSwinging) yourSwings++;
        wasSwinging = swinging;

        if (client.player.invulnerableTime == 20) lastTickHit = tick;
        if (bothAlive && client.player.getEyePosition().distanceToSqr(Render.getClosestPoint(client.player, target)) <= 9) lastTickInRange = tick;
        else lastTickOutOfRange = tick;
        if (client.options.keyJump.isDown() && MultiVersion.isOnGround(client.player)) lastTickJumped = tick;
        if (client.options.keyDown.isDown()) lastTickBacked = tick;

        //if the last time you were damaged was 5 ticks ago
        if (tick - lastTickHit == 5) {
            int jumpReset = lastTickJumped - lastTickHit;
            if (jumpReset >= -1 && jumpReset <= 1) {
                //landed
                lastJumpReset = System.currentTimeMillis();
                if (Toggle.JUMP_RESET_SOUND.toggled()) client.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 1));
            }
            else if (tick - lastTickBacked > 10 && jumpReset >= -3 && jumpReset <= 3) {
                //missed
            }
        }

        //if youre not fighting clear stats
        if (!fighting) {
            yourSwings = 0;
            theirSwings = 0;
            yourHits = 0;
            theirHits = 0;
        }

        //if the fight ended, clear all expected hits to prevent any false ghosts, else remove all unneeded hits naturally
        if (!withinFight) {
            if (fighting) {
                long duration = (System.currentTimeMillis() - fightStartedAt) / 1_000;

                //if the fight lasted at least 10 seconds, at most 10 minutes, and you hit at least once, track it
                if (duration >= 10 && duration <= 600 && lastNonGhost >= fightStartedAt) {
                    fightsThisSession++;
                    Settings.addFight(duration);

                    if (Toggle.ALERT_FIGHTS.toggled()) {
                        message("fight §7took §f" + formatTime(duration) + " §7(#" + fightsThisSession + "/#" + Settings.getInt("total_fights") + ")", "/hitreg alertDelays");
                        if (yourHits != 0 && yourSwings != 0) message("Your §7Accuracy: §f" + Math.round((((float) yourHits / yourSwings) * 100)) + "% §7(" + yourHits + "/" + yourSwings + ")", "");
                        if (theirHits != 0 && theirSwings != 0) message("Their §7Accuracy: §f" + Math.round((((float) theirHits / theirSwings) * 100)) + "% §7(" + theirHits + "/" + theirSwings + ")", "");
                    }
                }
            }

            fighting = false;
            targetLocation = Vec3.ZERO;
            previousTargetLocation = Vec3.ZERO;
            HitTracker.clear();
        } else {
            HitTracker.process();
        }

        //if the target moves backwards, they may be taking knockback
        if (targetTakingKnockback() && !alreadyKnockedBack) {
            long knockbackDelay = System.currentTimeMillis() - lastAttack;
            if (Toggle.ALERT_DELAYS.toggled() && knockbackDelay <= 500) message("knockback §7took at minimum §f" + knockbackDelay + "§7ms", "/hitreg alertDelays");
            alreadyKnockedBack = true;
        }

        if (target != null) {
            targetInvisible = target.isInvisible() || target.isSpectator();
            previousTargetLocation = targetLocation;
            targetLocation = MultiVersion.getBasePosition(target);
        }

        if (Settings.isTutorial() && !tutorialAlreadySeen) {
            message("Thanks for using BetterHitreg!", "/hitreg");
            message("use /hitreg or press " + Commands.getUIKey() + " to configure", "/hitreg");
            message("(you can click on these messages)", "/hitreg");
            tutorialAlreadySeen = true;
        }
    }

    public static String formatTime(long duration) {
        long minutes = TimeUnit.SECONDS.toMinutes(duration);
        long seconds = duration % 60;
        if (minutes == 0 && seconds == 0) return "no time";

        String m = "";
        String s = "";

        if (minutes > 1) m = minutes + " minutes";
        if (minutes == 1) m = minutes + " minute";
        if (seconds > 1) s = seconds + " seconds";
        if (seconds == 1) s = seconds + " second";

        if (minutes == 0) return s;
        if (seconds == 0) return m;
        return m + " " + s;
    }

    public static void updateGround() {
        if (client.player == null) return;

        ground = getGround(client.player);
        inSky = ground == Integer.MAX_VALUE;

        if (target != null) {
            theirGround = getGround(target);
            theirInSky = theirGround == Integer.MAX_VALUE;
        }
    }

    public static double getGround(Entity entity) {
        if (MultiVersion.isOnGround(entity)) return entity.getY();
        else {
            int x = entity.getBlockX();
            int y = entity.getBlockY();
            int z = entity.getBlockZ();

            for (int i = 0; i <= 3; i++) {
                int under = y - i;
                BlockPos position = new BlockPos(x, under, z);
                VoxelShape shape = client.level.getBlockState(position).getCollisionShape(client.level, position);
                if (!shape.isEmpty()) return under + shape.max(Direction.Axis.Y);
            }
        }

        return Integer.MAX_VALUE;
    }

    public static int getPing(UUID uuid) {
        if (client.getConnection() == null) return 0;
        PlayerInfo entry = client.getConnection().getPlayerInfo(uuid);
        return entry == null ? -1 : entry.getLatency();
    }

    public static int getPlayersPing() {
        if (client.player == null) return 0;
        return getPing(client.player.getUUID());
    }

    public static int getTargetsPing() {
        if (target == null) return 0;
        return getPing(target.getUUID());
    }

    public static boolean isToggled() {
        if (!Toggle.TOGGLE.toggled()) return false;
        if (!withinFight || targetIsBlocking) return false;
        if (Toggle.SAFE_REGS_ONLY.toggled() && (newTarget || wasGhosted || hitByAnother || hitWasFarFromPrevious)) return false;
        if (Toggle.IGNORE_SHIELD_HOLDERS.toggled() && targetHasShield) return false;
        return true;
    }

    public static void updateFightState() {
        bothAlive = client.player != null && target != null && client.player.isAlive() && target.isAlive() && !client.player.isSpectator() && !target.isSpectator();
        targetHasShield = target != null && Hitreg.target.isHolding(Items.SHIELD);
        //isUsingItem is also true while eating/drinking, isBlocking checks the shield is actually raised
        targetIsBlocking = targetHasShield && Hitreg.target.isBlocking();

        if (bothAlive) {
            distance = distanceToTarget();
            withinFight = distance <= 30;
        } else {
            withinFight = false;
        }
    }

    public static double distanceToTarget() {
        if (client.player == null || target == null) return Double.MAX_VALUE;
        return distanceFrom(MultiVersion.getBasePosition(client.player), MultiVersion.getBasePosition(target));
    }

    public static double distanceFromPlayer(Vec3 position) {
        if (client.player == null) return Double.MAX_VALUE;
        return distanceFrom(MultiVersion.getBasePosition(client.player), position);
    }

    public static double distanceFromTarget(Vec3 position) {
        if (target == null) return Double.MAX_VALUE;
        return distanceFrom(MultiVersion.getBasePosition(target), position);
    }

    public static double distanceFrom(Vec3 a, Vec3 b) {
        if (a == null || b == null) return Double.MAX_VALUE;
        double dx = a.x - b.x;
        double dz = a.z - b.z;
        return Math.sqrt(dx * dx + dz * dz);
    }

    public static boolean targetTakingKnockback(double angleThreshold, double minimumSpeed, boolean horizontalOnly) {
        if (client.player == null) return false;

        double mvx = targetLocation.x - previousTargetLocation.x;
        double mvy = targetLocation.y - previousTargetLocation.y;
        double mvz = targetLocation.z - previousTargetLocation.z;

        double pvx = lastAttackLocation.x - targetLocation.x;
        double pvy = lastAttackLocation.y - targetLocation.y;
        double pvz = lastAttackLocation.z - targetLocation.z;

        if (horizontalOnly) {
            mvy = 0;
            pvy = 0;
        }

        //are they moving fast enough
        double mLen = Math.sqrt(mvx*mvx + mvy*mvy + mvz*mvz);
        if (mLen < minimumSpeed) return false;

        //how far away are they
        double pLen = Math.sqrt(pvx*pvx + pvy*pvy + pvz*pvz);
        if (pLen == 0.0) return false;

        //normalized
        double dot = mvx*pvx + mvy*pvy + mvz*pvz;
        double cosAngle = dot / (mLen * pLen);

        //does the movement direction meet our threshold
        double thresholdCos = Math.cos(Math.toRadians(angleThreshold));

        //are they moving away
        return cosAngle < thresholdCos;
    }

    public static boolean targetTakingKnockback() {
        return targetTakingKnockback(120, 1e-3, true);
    }
}
