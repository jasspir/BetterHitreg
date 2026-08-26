package you.jass.betterhitreg.utility;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

import static you.jass.betterhitreg.hitreg.Hitreg.client;

public class InputTracker {
    private static double lastMouseX;
    private static double lastMouseY;
    private static double lastGLFWX;
    private static double lastGLFWY;
    private static final double[] glfwMouseX = new double[1];
    private static final double[] glfwMouseY = new double[1];

    public static void update() {
        Window window = client.getWindow();
        long time = System.nanoTime();

        set(Input.UP, client.options.keyUp.isDown(), time);
        set(Input.DOWN, client.options.keyDown.isDown(), time);
        set(Input.LEFT, client.options.keyLeft.isDown(), time);
        set(Input.RIGHT, client.options.keyRight.isDown(), time);
        set(Input.JUMP, client.options.keyJump.isDown(), time);
        set(Input.LEFT_CLICK, client.options.keyAttack.isDown(), time);
        set(Input.RIGHT_CLICK, client.options.keyUse.isDown(), time);

        double mouseX = client.mouseHandler.xpos();
        double mouseY = client.mouseHandler.ypos();
        double mouseDeltaX = mouseX - lastMouseX;
        double mouseDeltaY = mouseY - lastMouseY;
        Input.MOUSE_DELTA_X.value = mouseDeltaX;
        Input.MOUSE_DELTA_Y.value = mouseDeltaY;
        set(Input.MOUSE_DELTA_X, mouseDeltaX != 0, time);
        set(Input.MOUSE_DELTA_Y, mouseDeltaY != 0, time);

        checkGLFW(window, Input.UP, client.options.keyUp);
        checkGLFW(window, Input.DOWN, client.options.keyDown);
        checkGLFW(window, Input.LEFT, client.options.keyLeft);
        checkGLFW(window, Input.RIGHT, client.options.keyRight);
        checkGLFW(window, Input.JUMP, client.options.keyJump);
        checkGLFW(window, Input.LEFT_CLICK, client.options.keyAttack);
        checkGLFW(window, Input.RIGHT_CLICK, client.options.keyUse);

        //version 1.21.11-
        long id = window.getWindow();

        //version 26+
        //long id = window.handle();

        GLFW.glfwGetCursorPos(id, glfwMouseX, glfwMouseY);

        double glfwX = glfwMouseX[0];
        double glfwY = glfwMouseY[0];
        double glfwDeltaX = glfwX - lastGLFWX;
        double glfwDeltaY = glfwY - lastGLFWY;

        Input.MOUSE_DELTA_X.suspicious = mouseDeltaX != glfwDeltaX;
        Input.MOUSE_DELTA_Y.suspicious = mouseDeltaY != glfwDeltaY;

        for (Input input : Input.values()) input.duration = input.changed == 0 ? 0 : time - input.changed;

        lastMouseX = mouseX;
        lastMouseY = mouseY;
        lastGLFWX = glfwX;
        lastGLFWY = glfwY;
    }

    private static void set(Input input, boolean toggled, long time) {
        if (input.toggled != toggled) {
            input.previousDuration = time - input.changed;
            input.changed = time;
        }

        input.toggled = toggled;
    }

    private static void checkGLFW(Window window, Input input, KeyMapping key) {
        input.suspicious = input.toggled != isKeyDown(window, key);
    }

    private static boolean isKeyDown(Window window, KeyMapping key) {
        InputConstants.Key input = InputConstants.getKey(key.saveString());
        if (input.getType() == InputConstants.Type.MOUSE) {
            //version 1.21.11-
            //return GLFW.glfwGetMouseButton(window.getWindow(), input.getValue()) == GLFW.GLFW_PRESS;

            //version 26+
            return GLFW.glfwGetMouseButton(window.handle(), input.getValue()) == GLFW.GLFW_PRESS;
        }

        //version 1.21.11-
        //return InputConstants.isKeyDown(window.getWindow(), input.getValue());

        //version 26+
        return InputConstants.isKeyDown(window, input.getValue());
    }
}