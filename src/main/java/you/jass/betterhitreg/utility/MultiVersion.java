package you.jass.betterhitreg.utility;

//version 1.19.4
//import net.minecraft.client.gui.DrawableHelper;

//version 1.20.4-
//import net.minecraft.client.util.math.MatrixStack;
//import net.minecraft.client.util.math.MatrixStack;
//import net.minecraft.particle.DefaultParticleType;

//version 1.20.5-
//import net.minecraft.nbt.NbtElement;

//version 1.20+
//import net.minecraft.client.gui.DrawContext;

//version 1.20.5+
//import net.minecraft.particle.SimpleParticleType;

//version 1.21 - 1.21.4
//import net.minecraft.client.render.VertexFormat;

//version 1.21.5 - 1.21.10
//import com.mojang.blaze3d.buffers.GpuBuffer;
//import com.mojang.blaze3d.vertex.VertexFormat;

//version 1.21.11
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.world.debug.gizmo.GizmoDrawing;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.font.TextRenderer;

import net.minecraft.client.render.*;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import org.joml.Matrix4f;
import you.jass.betterhitreg.ui.UIUtils;

import java.awt.*;

import static you.jass.betterhitreg.hitreg.Hitreg.client;

public class MultiVersion {
    public static String getVersion() {
        //version 1.19.4
        //return "1.19.4";

        //version 1.20
        //return "1.20";

        //version 1.20.1
        //return "1.20.1";

        //version 1.20.2
        //return "1.20.2";

        //version 1.20.3
        //return "1.20.3";

        //version 1.20.4
        //return "1.20.4";

        //version 1.20.5
        //return "1.20.5";

        //version 1.20.6
        //return "1.20.6";

        //version 1.21
        //return "1.21";

        //version 1.21.1
        //return "1.21.1";

        //version 1.21.2
        //return "1.21.2";

        //version 1.21.3
        //return "1.21.3";

        //version 1.21.4
        //return "1.21.4";

        //version 1.21.5
        //return "1.21.5";

        //version 1.21.6
        //return "1.21.6";

        //version 1.21.7
        //return "1.21.7";

        //version 1.21.8
        //return "1.21.8";

        //version 1.21.9
        //return "1.21.9";

        //version 1.21.10
        //return "1.21.10";

        //version 1.21.11
        return "1.21.11";
    }

    public static Vec3d getLerpedPosition(Entity entity) {
        if (client.world == null || entity == null) return Vec3d.ZERO;

        //version 1.20.6-
        //return entity.getLerpedPos(client.getTickDelta());

        //version 1.21 - 1.21.4
        //return entity.getLerpedPos(client.getRenderTickCounter().getTickDelta(true));

        //version 1.21.5+
        return entity.getLerpedPos(client.getRenderTickCounter().getTickProgress(true));
    }

    public static Vec3d getBasePosition(Entity entity) {
        if (client.world == null || entity == null) return Vec3d.ZERO;

        //version 1.21.8-
        //return entity.getPos();

        //version 1.21.9+
        return entity.getEntityPos();
    }

    public static boolean isMovingFast() {
        //vanilla doesn't sweep when moving faster than your movement speed, 1.21.2 changed the check to compare actual movement against 2.5x

        //version 1.21.1-
        //return client.player.horizontalSpeed - client.player.prevHorizontalSpeed >= client.player.getMovementSpeed();

        //version 1.21.2+
        return client.player.getMovement().horizontalLengthSquared() >= MathHelper.square(client.player.getMovementSpeed() * 2.5);
    }

    public static void playParticles(String type, Entity entity) {
        if (client.world == null || entity == null) return;
        Vec3d position = getLerpedPosition(entity);
        for (int i = 0; i < 20; i++) {
            double x = Math.random() - 0.5;
            double y = Math.random() - 0.5;
            double z = Math.random() - 0.5;
            Vec3d direction = new Vec3d(x, y, z).normalize();

            //version 1.20.4-
            //DefaultParticleType particle = ParticleTypes.ASH;

            //version 1.20.5+
            SimpleParticleType particle = ParticleTypes.ASH;

            if (type.equals("CRIT")) particle = ParticleTypes.CRIT;
            else if (type.equals("ENCHANTED_HIT")) particle = ParticleTypes.ENCHANTED_HIT;

            //version 1.21.4-
//            client.world.addParticle(
//                    particle,
//                    position.x + x,
//                    position.y + (entity.getHeight() / 2) + y,
//                    position.z + z,
//                    direction.x * 0.5,
//                    direction.y * 0.5,
//                    direction.z * 0.5);

            //version 1.21.5+
            client.world.addParticleClient(
            particle,
            position.x + x,
            position.y + (entity.getHeight() / 2) + y,
            position.z + z,
            direction.x * 0.5,
            direction.y * 0.5,
            direction.z * 0.5);
        }
    }

    public static void message(String message, String command) {
        boolean settingHitreg = !command.contains("reset") && command.contains("set");
        Text hoverText = Text.literal("§7Click to " + (settingHitreg ? "set" :  "toggle"));
        if (command.equals("/hitreg")) hoverText = Text.literal("§7Click to configure");

        //version 1.21.4-
//        ClickEvent clickEvent = new ClickEvent(!settingHitreg ? ClickEvent.Action.RUN_COMMAND : ClickEvent.Action.SUGGEST_COMMAND, command);
//        HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText);

        //version 1.21.5+
        ClickEvent clickEvent = !settingHitreg ? new ClickEvent.RunCommand(command) : new ClickEvent.SuggestCommand(command);
        HoverEvent hoverEvent = new HoverEvent.ShowText(hoverText);

        Text text = Text.literal("Hitreg §8|§r " + message).setStyle(Style.EMPTY
                .withColor(TextColor.fromRgb(0xFFD700))
                .withClickEvent(clickEvent)
                .withHoverEvent(hoverEvent));
        if (client.player != null) client.player.sendMessage(text, false);
    }

    public static int getAnimationId(EntityAnimationS2CPacket packet) {
        //version 1.20.6-
        //return packet.getId();

        //version 1.21+
        return packet.getEntityId();
    }

    public static boolean hasSharpness() {
        if (client.player.getMainHandStack().hasEnchantments()) {
            //version 1.20.4-
            //for (NbtElement enchantment : client.player.getMainHandStack().getEnchantments()) {
            //if (enchantment.asString().contains("sharpness")) {
            //return true;
            //}
            //}

            //version 1.20.5+
            for (RegistryEntry<Enchantment> enchantment : client.player.getMainHandStack().getEnchantments().getEnchantments()) {
                if (enchantment.getIdAsString().equalsIgnoreCase("minecraft:sharpness")) {
                    return true;
                }
            }
        }

        return false;
    }

    public static void drawRectangle(Object renderer, int x, int y, int w, int h, Color c) {
        if (w <= 0 || h <= 0 || c == null) return;

        //version 1.19.4
        //MatrixStack ms = (MatrixStack) renderer;
        //DrawableHelper.fill(ms, x, y, x + w, y + h, c.getRGB());

        //version 1.20+
        DrawContext ctx = (DrawContext) renderer;
        ctx.fill(x, y, x + w, y + h, c.getRGB());
    }

    public static void drawGradientRectangle(Object renderer, int x, int y, int w, int h, Color start, Color end) {
        if (w <= 0 || h <= 0 || start == null || end == null) return;

        //version 1.19.4
        //MatrixStack ms = (MatrixStack) renderer;
        //for (int i = 0; i < h; i++) {
        //float t = (h > 1) ? (float) i / (h - 1) : 0f;
        //Color blended = UIUtils.blend(start, end, t);
        //DrawableHelper.fill(ms, x, y + i, x + w, y + i + 1, blended.getRGB());
        //}

        //version 1.20+
        DrawContext ctx = (DrawContext) renderer;
        for (int i = 0; i < h; i++) {
            float t = (h > 1) ? (float) i / (h - 1) : 0f;
            Color blended = UIUtils.blend(start, end, t);
            ctx.fill(x, y + i, x + w, y + i + 1, blended.getRGB());
        }
    }

    public static void drawHorizontalGradient(Object renderer, int x, int y, int w, int h, Color leftColor, Color rightColor) {
        if (w <= 0 || h <= 0 || leftColor == null || rightColor == null) return;

        //version 1.19.4
        //MatrixStack ms = (MatrixStack) renderer;
        //for (int i = 0; i < w; i++) {
        //float t = (w > 1) ? (float) i / (w - 1) : 0f;
        //Color blended = UIUtils.blend(leftColor, rightColor, t);
        //DrawableHelper.fill(ms, x + i, y, x + i + 1, y + h, blended.getRGB());
        //}

        //version 1.20+
        DrawContext ctx = (DrawContext) renderer;
        for (int i = 0; i < w; i++) {
            float t = (w > 1) ? (float) i / (w - 1) : 0f;
            Color blended = UIUtils.blend(leftColor, rightColor, t);
            ctx.fill(x + i, y, x + i + 1, y + h, blended.getRGB());
        }
    }

    public static void drawBorder(Object renderer, int x, int y, int w, int h, Color c) {
        if (w <= 0 || h <= 0 || c == null) return;

        //version 1.19.4
        //MatrixStack ms = (MatrixStack) renderer;
        //DrawableHelper.drawBorder(ms, x, y, w, h, c.getRGB());

        //version 1.20 - 1.21.8
//        DrawContext ctx = (DrawContext) renderer;
//        ctx.drawBorder(x, y, w, h, c.getRGB());

        //version 1.21.9+
        DrawContext ctx = (DrawContext) renderer;
        ctx.drawStrokedRectangle(x, y, w, h, c.getRGB());
    }

    public static void drawGradientBorder(Object renderer, int x, int y, int w, int h, Color start, Color end) {
        if (w <= 0 || h <= 0 || start == null || end == null) return;

        //version 1.19.4
        //MatrixStack ms = (MatrixStack) renderer;
        //DrawableHelper.enableScissor(x, y, x + w, y + 1);
        //drawGradientRectangle(ms, x, y, w, h, start, end);
        //DrawableHelper.disableScissor();
        //DrawableHelper.enableScissor(x, y, x + 1, y + h);
        //drawGradientRectangle(ms, x, y, w, h, start, end);
        //DrawableHelper.disableScissor();
        //DrawableHelper.enableScissor(x, y + h - 1, x + w, y + h);
        //drawGradientRectangle(ms, x, y, w, h, start, end);
        //DrawableHelper.disableScissor();
        //DrawableHelper.enableScissor(x + w - 1, y, x + w, y + h);
        //drawGradientRectangle(ms, x, y, w, h, start, end);
        //DrawableHelper.disableScissor();

        //version 1.20+
        DrawContext ctx = (DrawContext) renderer;
        ctx.enableScissor(x, y, x + w, y + 1);
        drawGradientRectangle(ctx, x, y, w, h, start, end);
        ctx.disableScissor();
        ctx.enableScissor(x, y, x + 1, y + h);
        drawGradientRectangle(ctx, x, y, w, h, start, end);
        ctx.disableScissor();
        ctx.enableScissor(x, y + h - 1, x + w, y + h);
        drawGradientRectangle(ctx, x, y, w, h, start, end);
        ctx.disableScissor();
        ctx.enableScissor(x + w - 1, y, x + w, y + h);
        drawGradientRectangle(ctx, x, y, w, h, start, end);
        ctx.disableScissor();
    }

    public static void drawText(Object renderer, TextRenderer tr, String s, int x, int y, Color c, boolean center) {
        if (s == null || tr == null || c == null) return;
        if (center) x -= tr.getWidth(s) / 2;

        //version 1.19.4
        //MatrixStack ms = (MatrixStack) renderer;
        //tr.drawWithShadow(ms, s, x, y, c.getRGB());

        //version 1.20+
        DrawContext ctx = (DrawContext) renderer;
        ctx.drawTextWithShadow(tr, s, x, y, c.getRGB());
    }

    public static void drawGradientText(Object renderer, TextRenderer tr, String s, int x, int y, Color start, Color end, boolean center) {
        if (s == null || tr == null || start == null || end == null) return;

        if (center) x -= tr.getWidth(s) / 2;
        final int last = s.length() - 1;
        int cx = x;

        //version 1.19.4
//        MatrixStack ms = (MatrixStack) renderer;
//        for (int i = 0; i <= last; i++) {
//            float t = (last > 0) ? (float) i / (float) last : 0f;
//            float shiftedT = (float) ((t - UIUtils.getShift()) % 1d);
//            if (shiftedT < 0f) shiftedT += 1f;
//            Color col = UIUtils.blend(start, end, 1f - Math.abs(2f * shiftedT - 1f));
//            String ch = s.substring(i, i + 1);
//            tr.drawWithShadow(ms, ch, cx, y, col.getRGB());
//            cx += tr.getWidth(ch);
//        }

        //version 1.20+
        DrawContext ctx = (DrawContext) renderer;
        for (int i = 0; i <= last; i++) {
            float t = (last > 0) ? (float) i / (float) last : 0f;
            float shiftedT = (float) ((t - UIUtils.getShift()) % 1d);
            if (shiftedT < 0f) shiftedT += 1f;
            Color col = UIUtils.blend(start, end, 1f - Math.abs(2f * shiftedT - 1f));
            String ch = s.substring(i, i + 1);
            ctx.drawTextWithShadow(tr, ch, cx, y, col.getRGB());
            cx += tr.getWidth(ch);
        }
    }

    public static void render(Matrix4f matrix, Vec3d vertex0, Vec3d vertex1, Vec3d vertex2, Vec3d vertex3, int color) {
        //version 1.20.6-
        //Tessellator tess = Tessellator.getInstance();
        //BufferBuilder buf = tess.getBuffer();
        //buf.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        //buf.vertex(matrix, (float) vertex0.x, (float) vertex0.y, (float) vertex0.z).color(color).next();
        //buf.vertex(matrix, (float) vertex1.x, (float) vertex1.y, (float) vertex1.z).color(color).next();
        //buf.vertex(matrix, (float) vertex2.x, (float) vertex2.y, (float) vertex2.z).color(color).next();
        //buf.vertex(matrix, (float) vertex3.x, (float) vertex3.y, (float) vertex3.z).color(color).next();
        //RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        //RenderSystem.disableCull();
        //tess.draw();
        //RenderSystem.enableCull();

        //version 1.21 - 1.21.10
//        Tessellator tess = Tessellator.getInstance();
//        BufferBuilder buf = tess.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
//        buf.vertex(matrix, (float) vertex0.x, (float) vertex0.y, (float) vertex0.z).color(color);
//        buf.vertex(matrix, (float) vertex1.x, (float) vertex1.y, (float) vertex1.z).color(color);
//        buf.vertex(matrix, (float) vertex2.x, (float) vertex2.y, (float) vertex2.z).color(color);
//        buf.vertex(matrix, (float) vertex3.x, (float) vertex3.y, (float) vertex3.z).color(color);
//        RenderLayer.getDebugQuads().draw(buf.end());

        //version 1.21.11+
        GizmoDrawing.quad(vertex0, vertex1, vertex2, vertex3, DrawStyle.filled(color));
    }
}