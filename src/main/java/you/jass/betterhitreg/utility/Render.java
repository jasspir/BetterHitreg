package you.jass.betterhitreg.utility;

//version 1.21.11+
import net.minecraft.gizmos.Gizmos;

import net.minecraft.client.Camera;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.joml.Matrix4f;
import you.jass.betterhitreg.hitreg.Hitreg;
import you.jass.betterhitreg.settings.Settings;
import you.jass.betterhitreg.settings.Toggle;

import java.awt.*;
import java.util.Stack;

import static you.jass.betterhitreg.hitreg.Hitreg.*;

public class Render {
    private static int FAR_HITBOX = 0xFFFFFFFF;
    private static int NEAR_HITBOX = 0xFFFFFFFF;
    private static int FAR_CROSS = 0xFFFFFFFF;
    private static int NEAR_CROSS = 0xFFFFFFFF;
    private static int FAR_CROSS_WITH_HITBOX = 0xFFFFFFFF;
    private static int NEAR_CROSS_WITH_HITBOX = 0xFFFFFFFF;
    private static int FAR_SERVER_HITBOX = 0xFFFFFFFF;
    private static int NEAR_SERVER_HITBOX = 0xFFFFFFFF;
    private static int FAR_YOUR_REACH = 0xFFFFFFFF;
    private static int NEAR_YOUR_REACH = 0xFFFFFFFF;
    private static int FAR_THEIR_REACH = 0xFFFFFFFF;
    private static int NEAR_THEIR_REACH = 0xFFFFFFFF;
    private static int NEAR_YOUR_JUMP_RANGE = 0xFFFFFFFF;
    private static int NEAR_THEIR_JUMP_RANGE = 0xFFFFFFFF;
    private static int FAR_YOUR_JUMP_RANGE = 0xFFFFFFFF;
    private static int FAR_THEIR_JUMP_RANGE = 0xFFFFFFFF;
    public static int JUMP_RESET_GLOW = 0xFFFFFFFF;
    public static int PERFECT_HIT_GLOW = 0xFFFFFFFF;

    public static void updateColors() {
        FAR_HITBOX = getColor("hitbox_far_color", "hitbox_far_opacity");
        NEAR_HITBOX = getColor("hitbox_near_color", "hitbox_near_opacity");
        FAR_CROSS = getColor("cross_far_color", "cross_far_opacity");
        NEAR_CROSS = getColor("cross_near_color", "cross_near_opacity");
        FAR_CROSS_WITH_HITBOX = getColor("cross_far_color_with_hitbox", "cross_with_hitbox_far_opacity");
        NEAR_CROSS_WITH_HITBOX = getColor("cross_near_color_with_hitbox", "cross_with_hitbox_near_opacity");
        FAR_SERVER_HITBOX = getColor("server_hitbox_far_color", "server_hitbox_far_opacity");
        NEAR_SERVER_HITBOX = getColor("server_hitbox_near_color", "server_hitbox_near_opacity");
        FAR_YOUR_REACH = getColor("your_reach_far_color", "your_reach_far_opacity");
        NEAR_YOUR_REACH = getColor("your_reach_near_color", "your_reach_near_opacity");
        FAR_THEIR_REACH = getColor("their_reach_far_color", "their_reach_far_opacity");
        NEAR_THEIR_REACH = getColor("their_reach_near_color", "their_reach_near_opacity");
        FAR_THEIR_JUMP_RANGE = getColor("their_jump_far_color", "their_jump_far_opacity");
        NEAR_THEIR_JUMP_RANGE = getColor("their_jump_near_color", "their_jump_near_opacity");
        NEAR_YOUR_JUMP_RANGE = getColor("your_jump_near_color", "your_jump_near_opacity");
        FAR_YOUR_JUMP_RANGE = getColor("your_jump_far_color", "your_jump_far_opacity");
        JUMP_RESET_GLOW = getColor("jump_reset_color", "jump_reset_opacity");
        PERFECT_HIT_GLOW = getColor("perfect_hit_color", "perfect_hit_opacity");
    }

    public static int getColor(String colorKey, String opacityKey) {
        String hex = Settings.get(colorKey);
        int opacity = Settings.getInt(opacityKey);
        if (hex == null) hex = "FFFFFF";
        int alpha = Math.max(0, Math.min(255, opacity));
        Color rgb = Color.decode("#" + hex.replace("#", ""));
        Color argb = new Color(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), alpha);
        return argb.getRGB();
    }

    public static void render(Camera camera) {
        if (client.player == null || client.level == null) return;
        boolean isHitbox = Toggle.RENDER_HITBOX.toggled();
        boolean isCross = Toggle.RENDER_CROSS.toggled();
        boolean isServerHitbox = Toggle.RENDER_SERVER_HITBOX.toggled();
        boolean isYourReach = Toggle.RENDER_YOUR_REACH.toggled() && !inSky;
        boolean isTheirReach = Toggle.RENDER_THEIR_REACH.toggled() && !theirInSky;
        boolean isYourJump = Toggle.RENDER_YOUR_JUMP.toggled() && !inSky;
        boolean isTheirJump = Toggle.RENDER_THEIR_JUMP.toggled() && !theirInSky;

        if (!Hitreg.bothAlive || Hitreg.targetInvisible) {
            if (isYourReach || isYourJump) {
                Vec3 player = MultiVersion.getLerpedPosition(client.player);
                Vec3 center = new Vec3(player.x, Hitreg.ground, player.z);
                if (isYourReach) ring(camera, center, 3, 64, 3, FAR_YOUR_REACH);
                if (isYourJump) ring(camera, center, 4, 64, 3, FAR_YOUR_JUMP_RANGE);
            }

            return;
        }

        if (isYourReach || isTheirReach || isYourJump || isTheirJump || isHitbox || isCross || isServerHitbox) {
            Vec3 closest = getClosestPoint(client.player, target);
            double distance = client.player.getEyePosition().distanceToSqr(closest);
            boolean withinHitRange = distance <= 9;
            boolean withinJumpRange = distance <= 16;

            if (isYourReach || isYourJump) {
                Vec3 player = MultiVersion.getLerpedPosition(client.player);
                Vec3 center = new Vec3(player.x, Hitreg.ground, player.z);
                if (isYourReach) ring(camera, center, 3, 64, 3, withinHitRange ? NEAR_YOUR_REACH : FAR_YOUR_REACH);
                if (isYourJump) ring(camera, center, 4, 64, 3, withinJumpRange ? NEAR_YOUR_JUMP_RANGE : FAR_YOUR_JUMP_RANGE);
            }

            if (isTheirReach || isTheirJump) {
                Vec3 player = MultiVersion.getLerpedPosition(target);
                Vec3 center = new Vec3(player.x, Hitreg.theirGround, player.z);
                if (isTheirReach) ring(camera, center, 3, 64, 3, withinHitRange ? NEAR_THEIR_REACH : FAR_THEIR_REACH);
                if (isTheirJump) ring(camera, center, 4, 64, 3, withinJumpRange ? NEAR_THEIR_JUMP_RANGE : FAR_THEIR_JUMP_RANGE);
            }

            if (isHitbox) {
                int color = withinHitRange ? NEAR_HITBOX : FAR_HITBOX;
                if (Toggle.PERFECT_HIT_COLOR.toggled() && System.currentTimeMillis() - Hitreg.lastPerfectHit <= 500) color = PERFECT_HIT_GLOW;
                else if (Toggle.JUMP_RESET_COLOR.toggled() && System.currentTimeMillis() - Hitreg.lastJumpReset <= 500) color = JUMP_RESET_GLOW;
                box(camera, getBoundingBox(target), 3, color);
            }

            if (isCross && distance <= 100) {
                int color = isHitbox || isServerHitbox ? (withinHitRange ? NEAR_CROSS_WITH_HITBOX : FAR_CROSS_WITH_HITBOX) : (withinHitRange ? NEAR_CROSS : FAR_CROSS);
                cross(camera, closest, 3, 30, 0.005, color);
            }

            if (isServerHitbox) {
                int color = withinHitRange ? NEAR_SERVER_HITBOX : FAR_SERVER_HITBOX;
                if (Toggle.PERFECT_HIT_COLOR.toggled() && System.currentTimeMillis() - Hitreg.lastPerfectHit <= 500) color = PERFECT_HIT_GLOW;
                else if (Toggle.JUMP_RESET_COLOR.toggled() && System.currentTimeMillis() - Hitreg.lastJumpReset <= 500) color = JUMP_RESET_GLOW;
                box(camera, target.getBoundingBox(), 3, color);
            }
        }
    }

    public static AABB getBoundingBox(Entity entity) {
        Vec3 lerpedPos = MultiVersion.getLerpedPosition(entity);
        Vec3 actualPos = MultiVersion.getBasePosition(entity);
        Vec3 delta = lerpedPos.subtract(actualPos);
        AABB box = entity.getBoundingBox();
        return box.move(delta);
    }

    public static void box(Camera camera, AABB box, float thickness, int rgba) {
        //corners
        Vec3 c0 = new Vec3(box.minX, box.minY, box.minZ);
        Vec3 c1 = new Vec3(box.maxX, box.minY, box.minZ);
        Vec3 c2 = new Vec3(box.maxX, box.minY, box.maxZ);
        Vec3 c3 = new Vec3(box.minX, box.minY, box.maxZ);
        Vec3 c4 = new Vec3(box.minX, box.maxY, box.minZ);
        Vec3 c5 = new Vec3(box.maxX, box.maxY, box.minZ);
        Vec3 c6 = new Vec3(box.maxX, box.maxY, box.maxZ);
        Vec3 c7 = new Vec3(box.minX, box.maxY, box.maxZ);

        //bottom face
        line(camera, c0, c1, thickness, rgba);
        line(camera, c1, c2, thickness, rgba);
        line(camera, c2, c3, thickness, rgba);
        line(camera, c3, c0, thickness, rgba);

        //top face
        line(camera, c4, c5, thickness, rgba);
        line(camera, c5, c6, thickness, rgba);
        line(camera, c6, c7, thickness, rgba);
        line(camera, c7, c4, thickness, rgba);

        //vertical walls
        line(camera, c0, c4, thickness, rgba);
        line(camera, c1, c5, thickness, rgba);
        line(camera, c2, c6, thickness, rgba);
        line(camera, c3, c7, thickness, rgba);
    }

    public static void line(Camera camera, Vec3 start, Vec3 end, float thickness, int rgba) {
        //version 1.21.10-
        //PoseStack ms = new PoseStack();

        //version 1.21.10-
        //Vec3 cameraPosition = camera.getPosition();

        //version 1.21.11+
        Vec3 cameraPosition = camera.position();

        //version 1.21.8-
        //ms.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
        //ms.mulPose(Axis.YP.rotationDegrees(camera.getYRot() + 180f));

        //version 1.21.10-
        //Matrix4f mat = ms.last().pose();
        //Vec3 a = start.subtract(cameraPosition);
        //Vec3 b = end.subtract(cameraPosition);
        //Vec3 mid = a.add(b).scale(0.5);
        //float distance = (float) mid.length();
        //float worldHalfWidth = thickness * distance * 0.001f;
        //Vec3 dir  = b.subtract(a).normalize();
        //Vec3 view = mid.normalize().scale(-1);
        //Vec3 perpendicular = dir.cross(view).normalize().scale(worldHalfWidth);
        //Vec3 v0 = a.add(perpendicular);
        //Vec3 v1 = a.subtract(perpendicular);
        //Vec3 v2 = b.subtract(perpendicular);
        //Vec3 v3 = b.add(perpendicular);
        //MultiVersion.render(mat, v0, v1, v2, v3, rgba);

        //version 1.21.11+
        Gizmos.line(start, end, rgba, thickness);
    }

    public static void ring(Camera camera, Vec3 center, double radius, int segments, float thickness, int rgba) {
        if (segments < 3) segments = 3;

        double angleDelta = 2.0 * Math.PI / segments;
        double cosDelta = Math.cos(angleDelta);
        double sinDelta = Math.sin(angleDelta);

        double x = radius;
        double z = 0;

        Vec3 prev = new Vec3(center.x + x, center.y, center.z + z);

        for (int i = 1; i <= segments; i++) {
            double nx = x * cosDelta - z * sinDelta;
            double nz = x * sinDelta + z * cosDelta;

            Vec3 next = new Vec3(center.x + nx, center.y, center.z + nz);
            line(camera, prev, next, thickness, rgba);

            x = nx;
            z = nz;
            prev = next;
        }
    }

    public static void cross(Camera camera, Vec3 center, float pixelThickness, double pixelHalfLength, double nudgeTowardVec3d, int rgba) {
        //version 1.21.10-
        //Vec3 cameraPosition = camera.getPosition();

        //version 1.21.11+
        Vec3 cameraPosition = camera.position();

        Vec3 position = cameraPosition.subtract(center);

        if (nudgeTowardVec3d > 0.0) {
            Vec3 nudgeDir = position.normalize();
            center = center.add(nudgeDir.scale(nudgeTowardVec3d));
            position = cameraPosition.subtract(center);
        }

        double distance = cameraPosition.distanceTo(center);

        double SCALE = 0.001;
        double worldHalfLength = pixelHalfLength * distance * SCALE;

        Vec3 forward = position.normalize();
        Vec3 worldUp = new Vec3(0.0, 1.0, 0.0);
        Vec3 right = worldUp.cross(forward);

        if (right.lengthSqr() < 1.0E-6) {
            right = new Vec3(1.0, 0.0, 0.0);
        } else {
            right = right.normalize();
        }

        Vec3 up = forward.cross(right).normalize();

        //horizontal
        Vec3 x0 = center.add(right.scale(-worldHalfLength));
        Vec3 x1 = center.add(right.scale( worldHalfLength));

        //vertical
        Vec3 y0 = center.add(up.scale(-worldHalfLength));
        Vec3 y1 = center.add(up.scale( worldHalfLength));

        line(camera, x0, x1, pixelThickness, rgba);
        line(camera, y0, y1, pixelThickness, rgba);
    }

    public static Vec3 getClosestPoint(Entity entity1, Entity entity2) {
        Vec3 eye = MultiVersion.getLerpedPosition(entity1).add(0, entity1.getEyeHeight(entity1.getPose()), 0);
        AABB box = getBoundingBox(entity2);

        double closestX = clamp(eye.x, box.min(Direction.Axis.X), box.max(Direction.Axis.X));
        double closestY = clamp(eye.y, box.min(Direction.Axis.Y), box.max(Direction.Axis.Y));
        double closestZ = clamp(eye.z, box.min(Direction.Axis.Z), box.max(Direction.Axis.Z));

        return new Vec3(closestX, closestY, closestZ);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }
}
