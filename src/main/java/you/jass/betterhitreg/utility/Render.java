package you.jass.betterhitreg.utility;

//version 1.21.11+
import net.minecraft.gizmos.Gizmos;

import net.minecraft.client.Camera;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import org.joml.Matrix4f;

import you.jass.betterhitreg.hitreg.Hitreg;
import you.jass.betterhitreg.settings.Setting;
import you.jass.betterhitreg.settings.Style;
import you.jass.betterhitreg.settings.Toggle;

import static you.jass.betterhitreg.hitreg.Hitreg.*;

public class Render {
    public static void render(Camera camera) {
        if (client.player == null || client.level == null) return;
        boolean isHitbox = Toggle.RENDER_HITBOX.toggled();
        boolean isCross = Toggle.RENDER_CROSS.toggled();
        boolean isServerHitbox = Toggle.RENDER_SERVER_HITBOX.toggled();
        boolean isApproachHitbox = Toggle.RENDER_APPROACH_HITBOX.toggled();
        boolean isYourReach = Toggle.RENDER_YOUR_REACH.toggled() && !inSky;
        boolean isTheirReach = Toggle.RENDER_THEIR_REACH.toggled() && !theirInSky;
        boolean isYourJump = Toggle.RENDER_YOUR_JUMP.toggled() && !inSky;
        boolean isTheirJump = Toggle.RENDER_THEIR_JUMP.toggled() && !theirInSky;
        
        renderFloor(camera);

        if (!Hitreg.bothAlive || Hitreg.targetInvisible) {
            if (isYourReach || isYourJump) {
                Vec3 player = MultiVersion.getLerpedPosition(client.player);
                Vec3 center = new Vec3(player.x, Hitreg.ground, player.z);
                if (isYourReach) ring(camera, center, 3, 64, 3, Style.YOUR_REACH_FAR.argb());
                if (isYourJump) ring(camera, center, 4, 64, 3, Style.YOUR_JUMP_FAR.argb());
            }

            return;
        }

        if (isYourReach || isTheirReach || isYourJump || isTheirJump || isHitbox || isCross || isServerHitbox || isApproachHitbox) {
            double distance = getDistance(client.player, target);
            boolean withinHitRange = distance <= 9;
            boolean withinJumpRange = distance <= 16;

            if (isYourReach || isYourJump) {
                Vec3 player = MultiVersion.getLerpedPosition(client.player);
                Vec3 center = new Vec3(player.x, Hitreg.ground, player.z);
                if (isYourReach) ring(camera, center, 3, 64, 3, withinHitRange ? Style.YOUR_REACH_NEAR.argb() : Style.YOUR_REACH_FAR.argb());
                if (isYourJump) ring(camera, center, 4, 64, 3, withinJumpRange ? Style.YOUR_JUMP_NEAR.argb() : Style.YOUR_JUMP_FAR.argb());
            }

            if (isTheirReach || isTheirJump) {
                Vec3 player = MultiVersion.getLerpedPosition(target);
                Vec3 center = new Vec3(player.x, Hitreg.theirGround, player.z);
                if (isTheirReach) ring(camera, center, 3, 64, 3, withinHitRange ? Style.THEIR_REACH_NEAR.argb() : Style.THEIR_REACH_FAR.argb());
                if (isTheirJump) ring(camera, center, 4, 64, 3, withinJumpRange ? Style.THEIR_JUMP_NEAR.argb() : Style.THEIR_JUMP_FAR.argb());
            }

            if (isHitbox) {
                int color = withinHitRange ? Style.HITBOX_NEAR.argb() : Style.HITBOX_FAR.argb();
                if (Toggle.PERFECT_HIT_COLOR.toggled() && System.currentTimeMillis() - Hitreg.lastPerfectHit <= 500) color = Style.PERFECT_HIT.argb();
                else if (Toggle.JUMP_RESET_COLOR.toggled() && System.currentTimeMillis() - Hitreg.lastJumpReset <= 500) color = Style.JUMP_RESET.argb();
                box(camera, getBoundingBox(target), 3, color);
            }

            if (isCross && distance <= 100) {
                int color = isHitbox || isServerHitbox ? (withinHitRange ? Style.CROSS_NEAR_WITH_HITBOX.argb() : Style.CROSS_FAR_WITH_HITBOX.argb()) : (withinHitRange ? Style.CROSS_NEAR.argb() : Style.CROSS_FAR.argb());
                cross(camera, getClosestPoint(client.player, target), 3, 30, 0.005, color);
            }

            if (isServerHitbox) {
                int color = withinHitRange ? Style.SERVER_HITBOX_NEAR.argb() : Style.SERVER_HITBOX_FAR.argb();
                if (Toggle.PERFECT_HIT_COLOR.toggled() && System.currentTimeMillis() - Hitreg.lastPerfectHit <= 500) color = Style.PERFECT_HIT.argb();
                else if (Toggle.JUMP_RESET_COLOR.toggled() && System.currentTimeMillis() - Hitreg.lastJumpReset <= 500) color = Style.JUMP_RESET.argb();
                box(camera, target.getBoundingBox(), 3, color);
            }

            if (isApproachHitbox && distance >= 9 && distance <= Setting.APPROACH_HITBOX_RANGE.get() * Setting.APPROACH_HITBOX_RANGE.get()) {
                int color = Style.APPROACH_HITBOX.argb();
                double exp = ((distance - 9) / 9) * 0.5;
                box(camera, getBoundingBox(target).inflate(exp, exp, exp), 3, color);
            }
        }
    }

    public static void renderFloor(Camera camera) {
        if (ground == Integer.MAX_VALUE) return;

        boolean customGround = Toggle.CUSTOM_GROUND.toggled();
        int step = (int) Setting.GRID_SIZE.get();

        if (!customGround && step <= 0) return;
        double y = ground + 0.01;

        //version 1.21.10-
        //Vec3 pos = camera.getPosition();

        //version 1.21.11+
        Vec3 pos = camera.position();

        if (Toggle.CUSTOM_GROUND.toggled()) {
            int size = 512;
            Vec3 v0 = new Vec3(pos.x - size, y, pos.z - size);
            Vec3 v1 = new Vec3(pos.x - size, y, pos.z + size);
            Vec3 v2 = new Vec3(pos.x + size, y, pos.z + size);
            Vec3 v3 = new Vec3(pos.x + size, y, pos.z - size);
            MultiVersion.render(v0, v1, v2, v3, Style.GROUND.argb());
        }

        if (step != 0) {
            double radius = Setting.GRID_RANGE.get();

            int minX = (int) Math.floor((pos.x - radius) / step) * step;
            int maxX = (int) Math.ceil ((pos.x + radius) / step) * step;
            int minZ = (int) Math.floor((pos.z - radius) / step) * step;
            int maxZ = (int) Math.ceil ((pos.z + radius) / step) * step;

            for (int x = minX; x <= maxX; x += step) {
                for (int z = minZ; z <= maxZ; z += step) {
                    if (x < maxX) drawLineIfVisible(camera, pos, new Vec3(x, y, z), new Vec3(x + step, y, z), 0, radius);
                    if (z < maxZ) drawLineIfVisible(camera, pos, new Vec3(x, y, z), new Vec3(x, y, z + step), 0, radius);
                }
            }
        }
    }

    private static void drawLineIfVisible(Camera camera, Vec3 playerPos, Vec3 start, Vec3 end, double fadeStart, double fadeEnd) {
        double distToSeg = closestDistanceToSegment(playerPos, start, end);
        if (distToSeg > fadeEnd) return;
        double fadeProgress = Math.max(0, Math.min(1, (distToSeg - fadeStart) / (fadeEnd - fadeStart)));
        fadeProgress = fadeProgress * fadeProgress * (3 - 2 * fadeProgress);
        int alpha = (int) (255 * (1 - fadeProgress));
        int color = (alpha << 24) | (Style.GRID.rgb());
        line(camera, start, end, 3, color);
    }

    private static double closestDistanceToSegment(Vec3 point, Vec3 segStart, Vec3 segEnd) {
        double segLenSq = segStart.distanceToSqr(segEnd);
        if (segLenSq == 0.0) return point.distanceTo(segStart);
        double fraction = ((point.x - segStart.x) * (segEnd.x - segStart.x) + (point.z - segStart.z) * (segEnd.z - segStart.z)) / segLenSq;
        fraction = Math.max(0, Math.min(1, fraction));
        Vec3 closestPoint = new Vec3(segStart.x + fraction * (segEnd.x - segStart.x), segStart.y, segStart.z + fraction * (segEnd.z - segStart.z));
        return point.distanceTo(closestPoint);
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

    public static void cross(Camera camera, Vec3 center, float pixelThickness, double pixelHalfLength, double nudgeTowardVec3, int rgba) {
        //version 1.21.10-
        //Vec3 cameraPosition = camera.getPosition();

        //version 1.21.11+
        Vec3 cameraPosition = camera.position();

        Vec3 position = cameraPosition.subtract(center);

        if (nudgeTowardVec3 > 0.0) {
            Vec3 nudgeDir = position.normalize();
            center = center.add(nudgeDir.scale(nudgeTowardVec3));
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

    public static double getDistance(Entity entity1, Entity entity2) {
        Vec3 eye = MultiVersion.getLerpedPosition(entity1).add(0, entity1.getEyeHeight(entity1.getPose()), 0);
        Vec3 closest = getClosestPoint(entity1, entity2);
        return eye.distanceToSqr(closest);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }
}
