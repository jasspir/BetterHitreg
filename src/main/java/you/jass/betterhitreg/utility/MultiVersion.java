package you.jass.betterhitreg.utility;

//version 1.19.4
//import net.minecraft.client.gui.GuiComponent;

//version 1.20 - 1.21.11
//import net.minecraft.client.gui.GuiGraphics;

//version 26+
//import net.minecraft.client.gui.GuiGraphicsExtractor;

//version 1.21.11-
//import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

//version 26+
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;

//version 1.21.11+
//import net.minecraft.gizmos.Gizmos;
//import net.minecraft.gizmos.GizmoStyle;

//version 1.21.10-
//import net.minecraft.client.renderer.RenderType;

//version 1.21.10-
//import com.mojang.blaze3d.systems.RenderSystem;
//import com.mojang.blaze3d.vertex.BufferBuilder;
//import com.mojang.blaze3d.vertex.DefaultVertexFormat;
//import com.mojang.blaze3d.vertex.Tesselator;
//import com.mojang.blaze3d.vertex.VertexFormat;
//import net.minecraft.client.renderer.GameRenderer;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.phys.Vec3;

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

        //version 26.1
        //return "26.1";

        //version 26.1.1
        //return "26.1.1";

        //version 26.1.2
        //return "26.1.2";

        //version 26.2
        //return "26.2";
    }

    public static Vec3 getLerpedPosition(Entity entity) {
        if (client.level == null || entity == null) return Vec3.ZERO;

        //version 1.20.6-
        //return entity.getPosition(client.getFrameTime());

        //version 1.21 - 1.21.1
        //return entity.getPosition(client.getTimer().getGameTimeDeltaPartialTick(true));

        //version 1.21.2+
        return entity.getPosition(client.getDeltaTracker().getGameTimeDeltaPartialTick(true));
    }

    public static Vec3 getBasePosition(Entity entity) {
        if (client.level == null || entity == null) return Vec3.ZERO;

        return entity.position();
    }

    public static KeyMapping registerKey(KeyMapping key) {
        //fabric renamed the keybinding helper for 26.1

        //version 1.21.11-
        return KeyBindingHelper.registerKeyBinding(key);

        //version 26.1+
        //return KeyMappingHelper.registerKeyMapping(key);
    }

    public static boolean isScreenOpen() {
        //26.2 moved the current screen from Minecraft into Gui

        //version 26.1.2-
        return client.screen != null;

        //version 26.2+
        //return client.gui.screen() != null;
    }

    public static void openScreen(Screen screen) {
        //version 26.1.2-
        client.setScreen(screen);

        //version 26.2+
        //client.gui.setScreen(screen);
    }

    public static String getSoundPath(SoundInstance sound) {
        //version 1.21.10-
        //return sound.getLocation() == null ? null : sound.getLocation().getPath();

        //version 1.21.11+
        return sound.getIdentifier() == null ? null : sound.getIdentifier().getPath();
    }

    public static boolean isOnGround(Entity entity) {
        //version 1.19.4
        //return entity.isOnGround();

        //version 1.20+
        return entity.onGround();
    }

    public static long getLevelTime(Entity entity) {
        //version 1.19.4
        //return entity.getLevel().getGameTime();

        //version 1.20+
        return entity.level().getGameTime();
    }

    public static boolean isMovingFast() {
        //vanilla doesn't sweep when moving faster than your movement speed, 1.21.2 changed the check to compare actual movement against 2.5x

        //version 1.21.1-
        //return client.player.walkDist - client.player.walkDistO >= client.player.getSpeed();

        //version 1.21.2+
        return client.player.getKnownMovement().horizontalDistanceSqr() >= Mth.square(client.player.getSpeed() * 2.5);
    }

    public static void playParticles(String type, Entity entity) {
        if (client.level == null || entity == null) return;
        Vec3 position = getLerpedPosition(entity);
        for (int i = 0; i < 20; i++) {
            double x = Math.random() - 0.5;
            double y = Math.random() - 0.5;
            double z = Math.random() - 0.5;
            Vec3 direction = new Vec3(x, y, z).normalize();

            SimpleParticleType particle = ParticleTypes.ASH;

            if (type.equals("CRIT")) particle = ParticleTypes.CRIT;
            else if (type.equals("ENCHANTED_HIT")) particle = ParticleTypes.ENCHANTED_HIT;

            client.level.addParticle(
            particle,
            position.x + x,
            position.y + (entity.getBbHeight() / 2) + y,
            position.z + z,
            direction.x * 0.5,
            direction.y * 0.5,
            direction.z * 0.5);
        }
    }

    public static void message(String message, String command) {
        boolean settingHitreg = !command.contains("reset") && command.contains("set");
        Component hoverText = Component.literal("§7Click to " + (settingHitreg ? "set" :  "toggle"));

        //version 1.21.4-
//        ClickEvent clickEvent = new ClickEvent(!settingHitreg ? ClickEvent.Action.RUN_COMMAND : ClickEvent.Action.SUGGEST_COMMAND, command);
//        HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText);

        //version 1.21.5+
        ClickEvent clickEvent = !settingHitreg ? new ClickEvent.RunCommand(command) : new ClickEvent.SuggestCommand(command);
        HoverEvent hoverEvent = new HoverEvent.ShowText(hoverText);

        Component text = Component.literal("Hitreg §8|§r " + message).setStyle(net.minecraft.network.chat.Style.EMPTY
                .withColor(TextColor.fromRgb(0xFFD700))
                .withClickEvent(clickEvent)
                .withHoverEvent(hoverEvent));
        //version 1.21.11-
        if (client.player != null) client.player.displayClientMessage(text, false);

        //version 26.1+
        //if (client.player != null) client.player.sendSystemMessage(text);
    }

    public static int getAction(ClientboundAnimatePacket packet) {
        return packet.getId();
    }

    public static boolean hasSharpness() {
        if (client.player.getMainHandItem().isEnchanted()) {
            //version 1.20.4-
//            for (net.minecraft.nbt.Tag enchantment : client.player.getMainHandItem().getEnchantmentTags()) {
//            if (enchantment.getAsString().contains("sharpness")) {
//            return true;
//            }
//            }

            //version 1.20.5+
            for (Holder<Enchantment> enchantment : client.player.getMainHandItem().getEnchantments().keySet()) {
                if (enchantment.getRegisteredName().equalsIgnoreCase("minecraft:sharpness")) {
                    return true;
                }
            }
        }

        return false;
    }

    public static void drawRectangle(Object renderer, int x, int y, int w, int h, Color c) {
        if (w <= 0 || h <= 0 || c == null) return;

        //version 1.19.4
        //PoseStack ms = (PoseStack) renderer;
        //GuiComponent.fill(ms, x, y, x + w, y + h, c.getRGB());

        //version 1.20 - 1.21.11
        GuiGraphics ctx = (GuiGraphics) renderer;
        ctx.fill(x, y, x + w, y + h, c.getRGB());

        //version 26.1+
//        GuiGraphicsExtractor ctx = (GuiGraphicsExtractor) renderer;
//        ctx.fill(x, y, x + w, y + h, c.getRGB());
    }

    public static void drawGradientRectangle(Object renderer, int x, int y, int w, int h, Color start, Color end) {
        if (w <= 0 || h <= 0 || start == null || end == null) return;

        //version 1.19.4
        //PoseStack ms = (PoseStack) renderer;
        //for (int i = 0; i < h; i++) {
        //float t = (h > 1) ? (float) i / (h - 1) : 0f;
        //Color blended = UIUtils.blend(start, end, t);
        //GuiComponent.fill(ms, x, y + i, x + w, y + i + 1, blended.getRGB());
        //}

        //version 1.20 - 1.21.11
        GuiGraphics ctx = (GuiGraphics) renderer;
        for (int i = 0; i < h; i++) {
            float t = (h > 1) ? (float) i / (h - 1) : 0f;
            Color blended = UIUtils.blend(start, end, t);
            ctx.fill(x, y + i, x + w, y + i + 1, blended.getRGB());
        }

        //version 26.1+
//        GuiGraphicsExtractor ctx = (GuiGraphicsExtractor) renderer;
//        for (int i = 0; i < h; i++) {
//            float t = (h > 1) ? (float) i / (h - 1) : 0f;
//            Color blended = UIUtils.blend(start, end, t);
//            ctx.fill(x, y + i, x + w, y + i + 1, blended.getRGB());
//        }
    }

    public static void drawHorizontalGradient(Object renderer, int x, int y, int w, int h, Color leftColor, Color rightColor) {
        if (w <= 0 || h <= 0 || leftColor == null || rightColor == null) return;

        //version 1.19.4
        //PoseStack ms = (PoseStack) renderer;
        //for (int i = 0; i < w; i++) {
        //float t = (w > 1) ? (float) i / (w - 1) : 0f;
        //Color blended = UIUtils.blend(leftColor, rightColor, t);
        //GuiComponent.fill(ms, x + i, y, x + i + 1, y + h, blended.getRGB());
        //}

        //version 1.20 - 1.21.11
        GuiGraphics ctx = (GuiGraphics) renderer;
        for (int i = 0; i < w; i++) {
            float t = (w > 1) ? (float) i / (w - 1) : 0f;
            Color blended = UIUtils.blend(leftColor, rightColor, t);
            ctx.fill(x + i, y, x + i + 1, y + h, blended.getRGB());
        }

        //version 26.1+
//        GuiGraphicsExtractor ctx = (GuiGraphicsExtractor) renderer;
//        for (int i = 0; i < w; i++) {
//            float t = (w > 1) ? (float) i / (w - 1) : 0f;
//            Color blended = UIUtils.blend(leftColor, rightColor, t);
//            ctx.fill(x + i, y, x + i + 1, y + h, blended.getRGB());
//        }
    }

    public static void renderOutline(Object renderer, int x, int y, int w, int h, Color c) {
        if (w <= 0 || h <= 0 || c == null) return;

        //version 1.19.4
        //PoseStack ms = (PoseStack) renderer;
        //GuiComponent.renderOutline(ms, x, y, w, h, c.getRGB());

        //version 1.20 - 1.21.8
//        GuiGraphics ctx = (GuiGraphics) renderer;
//        ctx.renderOutline(x, y, w, h, c.getRGB());

        //version 1.21.9 - 1.21.10
//        GuiGraphics ctx = (GuiGraphics) renderer;
//        ctx.submitOutline(x, y, w, h, c.getRGB());

        //version 1.21.11
        GuiGraphics ctx = (GuiGraphics) renderer;
        ctx.renderOutline(x, y, w, h, c.getRGB());

        //version 26.1+
//        GuiGraphicsExtractor ctx = (GuiGraphicsExtractor) renderer;
//        ctx.outline(x, y, w, h, c.getRGB());
    }

    public static void drawGradientBorder(Object renderer, int x, int y, int w, int h, Color start, Color end) {
        if (w <= 0 || h <= 0 || start == null || end == null) return;

        //version 1.19.4
        //PoseStack ms = (PoseStack) renderer;
        //GuiComponent.enableScissor(x, y, x + w, y + 1);
        //drawGradientRectangle(ms, x, y, w, h, start, end);
        //GuiComponent.disableScissor();
        //GuiComponent.enableScissor(x, y, x + 1, y + h);
        //drawGradientRectangle(ms, x, y, w, h, start, end);
        //GuiComponent.disableScissor();
        //GuiComponent.enableScissor(x, y + h - 1, x + w, y + h);
        //drawGradientRectangle(ms, x, y, w, h, start, end);
        //GuiComponent.disableScissor();
        //GuiComponent.enableScissor(x + w - 1, y, x + w, y + h);
        //drawGradientRectangle(ms, x, y, w, h, start, end);
        //GuiComponent.disableScissor();

        //version 1.20 - 1.21.11
        GuiGraphics ctx = (GuiGraphics) renderer;
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

        //version 26.1+
//        GuiGraphicsExtractor ctx = (GuiGraphicsExtractor) renderer;
//        ctx.enableScissor(x, y, x + w, y + 1);
//        drawGradientRectangle(ctx, x, y, w, h, start, end);
//        ctx.disableScissor();
//        ctx.enableScissor(x, y, x + 1, y + h);
//        drawGradientRectangle(ctx, x, y, w, h, start, end);
//        ctx.disableScissor();
//        ctx.enableScissor(x, y + h - 1, x + w, y + h);
//        drawGradientRectangle(ctx, x, y, w, h, start, end);
//        ctx.disableScissor();
//        ctx.enableScissor(x + w - 1, y, x + w, y + h);
//        drawGradientRectangle(ctx, x, y, w, h, start, end);
//        ctx.disableScissor();
    }

    public static void drawText(Object renderer, Font tr, String s, int x, int y, Color c, boolean center) {
        if (s == null || tr == null || c == null) return;
        if (center) x -= tr.width(s) / 2;

        //version 1.19.4
        //PoseStack ms = (PoseStack) renderer;
        //tr.drawShadow(ms, s, x, y, c.getRGB());

        //version 1.20 - 1.21.11
        GuiGraphics ctx = (GuiGraphics) renderer;
        ctx.drawString(tr, s, x, y, c.getRGB());

        //version 26.1+
//        GuiGraphicsExtractor ctx = (GuiGraphicsExtractor) renderer;
//        ctx.text(tr, s, x, y, c.getRGB(), true);
    }

    public static void drawGradientText(Object renderer, Font tr, String s, int x, int y, Color start, Color end, boolean center) {
        if (s == null || tr == null || start == null || end == null) return;

        if (center) x -= tr.width(s) / 2;
        final int last = s.length() - 1;
        int cx = x;

        //version 1.19.4
//        PoseStack ms = (PoseStack) renderer;
//        for (int i = 0; i <= last; i++) {
//            float t = (last > 0) ? (float) i / (float) last : 0f;
//            float shiftedT = (float) ((t - UIUtils.getShift()) % 1d);
//            if (shiftedT < 0f) shiftedT += 1f;
//            Color col = UIUtils.blend(start, end, 1f - Math.abs(2f * shiftedT - 1f));
//            String ch = s.substring(i, i + 1);
//            tr.drawShadow(ms, ch, cx, y, col.getRGB());
//            cx += tr.width(ch);
//        }

        //version 1.20 - 1.21.11
        GuiGraphics ctx = (GuiGraphics) renderer;
        for (int i = 0; i <= last; i++) {
            float t = (last > 0) ? (float) i / (float) last : 0f;
            float shiftedT = (float) ((t - UIUtils.getShift()) % 1d);
            if (shiftedT < 0f) shiftedT += 1f;
            Color col = UIUtils.blend(start, end, 1f - Math.abs(2f * shiftedT - 1f));
            String ch = s.substring(i, i + 1);
            ctx.drawString(tr, ch, cx, y, col.getRGB());
            cx += tr.width(ch);
        }

        //version 26.1+
//        GuiGraphicsExtractor ctx = (GuiGraphicsExtractor) renderer;
//        for (int i = 0; i <= last; i++) {
//            float t = (last > 0) ? (float) i / (float) last : 0f;
//            float shiftedT = (float) ((t - UIUtils.getShift()) % 1d);
//            if (shiftedT < 0f) shiftedT += 1f;
//            Color col = UIUtils.blend(start, end, 1f - Math.abs(2f * shiftedT - 1f));
//            String ch = s.substring(i, i + 1);
//            ctx.text(tr, ch, cx, y, col.getRGB(), true);
//            cx += tr.width(ch);
//        }
    }

    public static void render(Matrix4f matrix, Vec3 vertex0, Vec3 vertex1, Vec3 vertex2, Vec3 vertex3, int color) {
        //version 1.20.6-
        //Tesselator tess = Tesselator.getInstance();
        //BufferBuilder buf = tess.getBuilder();
        //buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        //buf.vertex(matrix, (float) vertex0.x, (float) vertex0.y, (float) vertex0.z).color(color).endVertex();
        //buf.vertex(matrix, (float) vertex1.x, (float) vertex1.y, (float) vertex1.z).color(color).endVertex();
        //buf.vertex(matrix, (float) vertex2.x, (float) vertex2.y, (float) vertex2.z).color(color).endVertex();
        //buf.vertex(matrix, (float) vertex3.x, (float) vertex3.y, (float) vertex3.z).color(color).endVertex();
        //RenderSystem.setShader(GameRenderer::getPositionColorShader);
        //RenderSystem.disableCull();
        //tess.end();
        //RenderSystem.enableCull();

        //version 1.21 - 1.21.10
//        Tesselator tess = Tesselator.getInstance();
//        BufferBuilder buf = tess.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
//        buf.addVertex(matrix, (float) vertex0.x, (float) vertex0.y, (float) vertex0.z).setColor(color);
//        buf.addVertex(matrix, (float) vertex1.x, (float) vertex1.y, (float) vertex1.z).setColor(color);
//        buf.addVertex(matrix, (float) vertex2.x, (float) vertex2.y, (float) vertex2.z).setColor(color);
//        buf.addVertex(matrix, (float) vertex3.x, (float) vertex3.y, (float) vertex3.z).setColor(color);
//        RenderType.debugQuads().draw(buf.buildOrThrow());

        //version 1.21.11+
        Gizmos.rect(vertex0, vertex1, vertex2, vertex3, GizmoStyle.fill(color));
    }

    public static void render(Vec3 vertex0, Vec3 vertex1, Vec3 vertex2, Vec3 vertex3, int color) {
        render(new PoseStack().last().pose(), vertex0, vertex1, vertex2, vertex3, color);
    }
}