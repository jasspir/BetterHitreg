package you.jass.betterhitreg;

//version 1.21.8-
//import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

//version 1.21.10 - 1.21.11
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;

//version 26.1+
//import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
//import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;

//version 1.21.11-
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import you.jass.betterhitreg.settings.Settings;
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
        Render.updateColors();

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

        //version 1.19.4
//        HudRenderCallback.EVENT.register((context, tickCounter) -> {
//            if (client.level == null || client.font == null || (leftScore == 0 && rightScore == 0)) return;
//            String scoreText = "Score: " + leftScore + " - " + rightScore;
//            client.font.drawShadow(context, scoreText, 10, 10, 0xFFFFFFFF);
//        });

        //version 1.20 - 1.21.11
        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            if (client.level == null || client.font == null || (leftScore == 0 && rightScore == 0)) return;
            String scoreText = "Score: " + leftScore + " - " + rightScore;
            context.drawString(client.font, scoreText, 10, 10, 0xFFFFFFFF);
        });

        //version 26.1+
//        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("betterhitreg", "score"), (context, tickCounter) -> {
//            if (client.level == null || client.font == null || (leftScore == 0 && rightScore == 0)) return;
//            String scoreText = "Score: " + leftScore + " - " + rightScore;
//            context.text(client.font, scoreText, 10, 10, 0xFFFFFFFF, true);
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
}