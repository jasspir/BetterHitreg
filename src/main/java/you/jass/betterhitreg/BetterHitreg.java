package you.jass.betterhitreg;

//version 1.21.8-
//import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

//version 1.21.10+
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.gui.DrawContext;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import you.jass.betterhitreg.settings.Commands;
import you.jass.betterhitreg.ui.UIScreen;
import you.jass.betterhitreg.utility.Render;

import static you.jass.betterhitreg.hitreg.Hitreg.*;

public class BetterHitreg implements ModInitializer {
    public static KeyBinding uiKey;
    public static KeyBinding handKey;
    public static KeyBinding leftKey;
    public static KeyBinding rightKey;
    public static KeyBinding upKey;
    public static KeyBinding downKey;
    public static int handSwitchCooldown;
    public static int scoreCooldown;
    public static int leftScore;
    public static int rightScore;

    @Override
    public void onInitialize() {
        client = MinecraftClient.getInstance();
        Commands.initialize();
        Render.updateColors();

        ClientTickEvents.START_CLIENT_TICK.register(client -> tick());

        //1.21.9 doesn't have worldrenderevents so we do it in WorldMixin

        //version 1.21.8-
//        WorldRenderEvents.END.register(context -> {
//            Render.render(context.camera());
//        });

        //version 1.21.10+
        WorldRenderEvents.END_MAIN.register(context -> {
            Render.render(context.gameRenderer().getCamera());
        });

        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            if (client.world == null || client.textRenderer == null || (leftScore == 0 && rightScore == 0)) return;
            String scoreText = "Score: " + leftScore + " - " + rightScore;

            //version 1.19.4
            //client.textRenderer.drawWithShadow(context, scoreText, 10, 10, 0xFFFFFFFF);

            //version 1.20+
            context.drawTextWithShadow(client.textRenderer, scoreText, 10, 10, 0xFFFFFFFF);
        });

        //version 1.21.8-
//        uiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
//                "Open Hitreg Menu",
//                InputUtil.Type.KEYSYM,
//                GLFW.GLFW_KEY_H,
//                "Hitreg"
//        ));
//        handKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
//                "Switch Hand",
//                InputUtil.Type.KEYSYM,
//                GLFW.GLFW_KEY_UNKNOWN,
//                "Hitreg"
//        ));
//        leftKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
//                "Increase Left Score",
//                InputUtil.Type.KEYSYM,
//                GLFW.GLFW_KEY_LEFT,
//                "Hitreg"
//        ));
//        rightKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
//                "Increase Right Score",
//                InputUtil.Type.KEYSYM,
//                GLFW.GLFW_KEY_RIGHT,
//                "Hitreg"
//        ));
//        upKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
//                "Send Score to Chat",
//                InputUtil.Type.KEYSYM,
//                GLFW.GLFW_KEY_UP,
//                "Hitreg"
//        ));
//        downKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
//                "Reset Last Score",
//                InputUtil.Type.KEYSYM,
//                GLFW.GLFW_KEY_DOWN,
//                "Hitreg"
//        ));

        //version 1.21.9+
        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of("betterhitreg", "hitreg"));
        uiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Open Hitreg Menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H, category
        ));
        handKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Switch Hand",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN, category
        ));
        leftKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Increase Left Score",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT,
                category
        ));
        rightKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Increase Right Score",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT,
                category
        ));
        upKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Send Score to Chat",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UP,
                category
        ));
        downKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Reset Score",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_DOWN,
                category
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (uiKey.wasPressed() && client.currentScreen == null) client.setScreen(new UIScreen());

            if (handKey.wasPressed() && handSwitchCooldown == 0 && client.currentScreen == null) {
                client.options.getMainArm().setValue(client.options.getMainArm().getValue().getOpposite());
                client.player.setMainArm(client.options.getMainArm().getValue());
                client.options.sendClientSettings();
                handSwitchCooldown = 5;
            }

            if (scoreCooldown == 0 && client.currentScreen == null) {
                if (leftKey.wasPressed()) leftScore++;
                if (rightKey.wasPressed()) rightScore++;
                if (upKey.wasPressed() && (leftScore > 0 || rightScore > 0) && client.getNetworkHandler() != null) client.getNetworkHandler().sendChatMessage(leftScore + "-" + rightScore);
                if (downKey.wasPressed()) {
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