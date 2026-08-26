package you.jass.betterhitreg;

//version 1.21.8-
//import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

//version 1.21.10 - 1.21.11
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;

//version 1.21.11-
//import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

//version 26+
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import you.jass.betterhitreg.settings.Settings;
import you.jass.betterhitreg.settings.Style;
import you.jass.betterhitreg.settings.Toggle;
import you.jass.betterhitreg.utility.Input;
import you.jass.betterhitreg.utility.MultiVersion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;

//version 1.21.9 - 1.21.10
//import net.minecraft.resources.ResourceLocation;

//version 1.21.11+
import net.minecraft.resources.Identifier;

import org.lwjgl.glfw.GLFW;
import you.jass.betterhitreg.settings.Commands;
import you.jass.betterhitreg.ui.UIScreen;
import you.jass.betterhitreg.utility.Render;

import static you.jass.betterhitreg.hitreg.Hitreg.*;

public class BetterHitreg implements ModInitializer {
    public static KeyMapping uiKey;
    public static KeyMapping handKey;
    public static KeyMapping leftKey;
    public static KeyMapping rightKey;
    public static KeyMapping upKey;
    public static KeyMapping downKey;
    public static int handSwitchCooldown;
    public static int scoreCooldown;
    public static int leftScore;
    public static int rightScore;

    @Override
    public void onInitialize() {
        client = Minecraft.getInstance();
        Commands.initialize();
        Settings.initialize();
        Style.updateAll();
        registerText();
        ClientTickEvents.START_CLIENT_TICK.register(client -> tick());

        //1.21.9 doesn't have worldrenderevents so we do it in WorldMixin

        //version 1.21.8-
//        WorldRenderEvents.END.register(context -> {
//            Render.render(context.camera());
//        });

        //version 1.21.10 - 1.21.11
        WorldRenderEvents.END_MAIN.register(context -> {
            Render.render(context.gameRenderer().getMainCamera());
        });

        //version 26.1 - 26.1.2
//        LevelRenderEvents.END_MAIN.register(context -> {
//            Render.render(context.gameRenderer().getMainCamera());
//        });

        //version 26.2+
//        LevelRenderEvents.END_MAIN.register(context -> {
//            Render.render(context.gameRenderer().mainCamera());
//        });

        //version 1.21.8-
//        uiKey = MultiVersion.registerKey(new KeyMapping(
//                "Open Hitreg Menu",
//                InputConstants.Type.KEYSYM,
//                GLFW.GLFW_KEY_H,
//                "Hitreg"
//        ));
//        handKey = MultiVersion.registerKey(new KeyMapping(
//                "Switch Hand",
//                InputConstants.Type.KEYSYM,
//                GLFW.GLFW_KEY_UNKNOWN,
//                "Hitreg"
//        ));
//        leftKey = MultiVersion.registerKey(new KeyMapping(
//                "Increase Left Score",
//                InputConstants.Type.KEYSYM,
//                GLFW.GLFW_KEY_LEFT,
//                "Hitreg"
//        ));
//        rightKey = MultiVersion.registerKey(new KeyMapping(
//                "Increase Right Score",
//                InputConstants.Type.KEYSYM,
//                GLFW.GLFW_KEY_RIGHT,
//                "Hitreg"
//        ));
//        upKey = MultiVersion.registerKey(new KeyMapping(
//                "Send Score to Chat",
//                InputConstants.Type.KEYSYM,
//                GLFW.GLFW_KEY_UP,
//                "Hitreg"
//        ));
//        downKey = MultiVersion.registerKey(new KeyMapping(
//                "Reset Last Score",
//                InputConstants.Type.KEYSYM,
//                GLFW.GLFW_KEY_DOWN,
//                "Hitreg"
//        ));

        //version 1.21.9 - 1.21.10
//        KeyMapping.Category category = KeyMapping.Category.register(ResourceLocation.fromNamespaceAndPath("betterhitreg", "hitreg"));

        //version 1.21.11+
        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("betterhitreg", "hitreg"));

        //version 1.21.9+
        uiKey = MultiVersion.registerKey(new KeyMapping(
                "Open Hitreg Menu",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_H, category
        ));
        handKey = MultiVersion.registerKey(new KeyMapping(
                "Switch Hand",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN, category
        ));
        leftKey = MultiVersion.registerKey(new KeyMapping(
                "Increase Left Score",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT,
                category
        ));
        rightKey = MultiVersion.registerKey(new KeyMapping(
                "Increase Right Score",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT,
                category
        ));
        upKey = MultiVersion.registerKey(new KeyMapping(
                "Send Score to Chat",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UP,
                category
        ));
        downKey = MultiVersion.registerKey(new KeyMapping(
                "Reset Score",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_DOWN,
                category
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (uiKey.consumeClick() && !MultiVersion.isScreenOpen()) MultiVersion.openScreen(new UIScreen());

            if (handKey.consumeClick() && handSwitchCooldown == 0 && !MultiVersion.isScreenOpen()) {
                client.options.mainHand().set(client.options.mainHand().get().getOpposite());
                client.player.setMainArm(client.options.mainHand().get());
                client.options.broadcastOptions();
                handSwitchCooldown = 5;
            }

            if (scoreCooldown == 0 && !MultiVersion.isScreenOpen()) {
                if (leftKey.consumeClick()) leftScore++;
                if (rightKey.consumeClick()) rightScore++;
                if (upKey.consumeClick() && (leftScore > 0 || rightScore > 0) && client.getConnection() != null) client.getConnection().sendChat(leftScore + "-" + rightScore);
                if (downKey.consumeClick()) {
                    leftScore = 0;
                    rightScore = 0;
                }

                scoreCooldown = 5;
            }

            if (handSwitchCooldown > 0) handSwitchCooldown--;
            if (scoreCooldown > 0) scoreCooldown--;
        });
    }

    public void registerText() {
        //version 1.19.4
        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            if (client.level == null) return;
            if (leftScore != 0 || rightScore != 0){
                client.font.drawShadow(context, leftScore + " - " + rightScore, 10, 10, 0xFFFFFFFF);
                client.font.drawShadow
            }
        });

        //version 1.20 - 1.21.11
        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            if (client.level == null) return;
            if (leftScore != 0 || rightScore != 0) context.drawString(client.font, leftScore + " - " + rightScore, 10, 10, 0xFFFFFFFF);
        });

        //version 26.1+
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("betterhitreg", "score"), (context, tickCounter) -> {
            if (client.level == null) return;
            if (leftScore != 0 || rightScore != 0) context.text(client.font, leftScore + " - " + rightScore, 10, 10, 0xFFFFFFFF, true);
        });

        //version 1.19.4
        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            if (client.level == null || !Toggle.DEBUG_INPUTS.toggled() || MultiVersion.isScreenOpen()) return;
            int y = 1;
            for (Input input : Input.values()) {
                String value;
                double ms = input.previousDuration / 1_000_000d;
                String duration = ms < 10 ? String.format("%.2f", ms) : ms < 100 ? String.format("%.1f", ms) : String.format("%.0f", ms);
                if (input == Input.MOUSE_DELTA_X || input == Input.MOUSE_DELTA_Y) value = String.valueOf(input.value);
                else value = input.toggled ? "yes" : "no";
                String string = "§f" + input.name + ": " + (input.toggled ? "§a" + value : "§c" + value) + " §7(" + "§e" + duration + "ms§7)" + " §7(" + (input.suspicious ? "§cfake" : "§areal") + "§7)";
                client.font.drawShadow(context, string, 1, y, 0xFFFFFFFF);
                y += 10;
            }
        });

        //version 1.20 - 1.21.11
        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            if (client.level == null || !Toggle.DEBUG_INPUTS.toggled() || MultiVersion.isScreenOpen()) return;
            int y = 1;
            for (Input input : Input.values()) {
                String value;
                double ms = input.previousDuration / 1_000_000d;
                String duration = ms < 10 ? String.format("%.2f", ms) : ms < 100 ? String.format("%.1f", ms) : String.format("%.0f", ms);
                if (input == Input.MOUSE_DELTA_X || input == Input.MOUSE_DELTA_Y) value = String.valueOf(input.value);
                else value = input.toggled ? "yes" : "no";
                String string = "§f" + input.name + ": " + (input.toggled ? "§a" + value : "§c" + value) + " §7(" + "§e" + duration + "ms§7)" + " §7(" + (input.suspicious ? "§cfake" : "§areal") + "§7)";
                context.drawString(client.font, string, 1, y, 0xFFFFFFFF);
                y += 10;
            }
        });

        //version 26+
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("betterhitreg", "inputs"), (context, tickCounter) -> {
            if (client.level == null || !Toggle.DEBUG_INPUTS.toggled() || MultiVersion.isScreenOpen()) return;
            int y = 1;
            for (Input input : Input.values()) {
                String value;
                double ms = input.previousDuration / 1_000_000d;
                String duration = ms < 10 ? String.format("%.2f", ms) : ms < 100 ? String.format("%.1f", ms) : String.format("%.0f", ms);
                if (input == Input.MOUSE_DELTA_X || input == Input.MOUSE_DELTA_Y) value = String.valueOf(input.value);
                else value = input.toggled ? "yes" : "no";
                String string = "§f" + input.name + ": " + (input.toggled ? "§a" + value : "§c" + value) + " §7(" + "§e" + duration + "ms§7)" + " §7(" + (input.suspicious ? "§cfake" : "§areal") + "§7)";
                context.text(client.font, string, 1, y, 0xFFFFFFFF);
                y += 10;
            }
        });
    }
}