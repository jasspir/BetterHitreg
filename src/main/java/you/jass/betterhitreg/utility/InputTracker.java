package you.jass.betterhitreg.utility;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.KeyMapping;

//version 26.2-
//import org.lwjgl.glfw.GLFW;

//version 26.3+
import org.lwjgl.BufferUtils;
import org.lwjgl.sdl.SDLMouse;
import java.nio.FloatBuffer;

import static you.jass.betterhitreg.hitreg.Hitreg.client;

public class InputTracker {
    private static double lastMouseX;
    private static double lastMouseY;
    private static double lastGLFWX;
    private static double lastGLFWY;
    private static final double[] glfwMouseX = new double[1];
    private static final double[] glfwMouseY = new double[1];
    private static double movedX;
    private static double movedY;

    //version 26.3+
    private static double rawMovedX;
    private static double rawMovedY;

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

        //version 26.2-
        //double mouseX = client.mouseHandler.xpos();
        //double mouseY = client.mouseHandler.ypos();
        //double mouseDeltaX = mouseX - lastMouseX;
        //double mouseDeltaY = mouseY - lastMouseY;
        //lastMouseX = mouseX;
        //lastMouseY = mouseY;

        //version 26.3+
        double mouseDeltaX = movedX;
        double mouseDeltaY = movedY;
        movedX = 0;
        movedY = 0;

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

        //version 1.21.8-
        //long id = window.getWindow();

        //version 1.21.9 - 26.2
        //long id = window.handle();

        //version 26.2-
        //GLFW.glfwGetCursorPos(id, glfwMouseX, glfwMouseY);
        //double glfwX = glfwMouseX[0];
        //double glfwY = glfwMouseY[0];
        //double rawDeltaX = glfwX - lastGLFWX;
        //double rawDeltaY = glfwY - lastGLFWY;
        //lastGLFWX = glfwX;
        //lastGLFWY = glfwY;

        //version 26.3+
        double rawDeltaX = rawMovedX;
        double rawDeltaY = rawMovedY;
        rawMovedX = 0;
        rawMovedY = 0;

        Input.MOUSE_DELTA_X.suspicious = mouseDeltaX != rawDeltaX;
        Input.MOUSE_DELTA_Y.suspicious = mouseDeltaY != rawDeltaY;

        for (Input input : Input.values()) input.duration = input.changed == 0 ? 0 : time - input.changed;
    }

    public static void onMove(double dx, double dy) {
        movedX += dx;
        movedY += dy;
    }

    //version 26.3+
    public static void onRawMove(float dx, float dy) {
        rawMovedX += dx;
        rawMovedY += dy;
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
            //version 1.21.8-
            //return GLFW.glfwGetMouseButton(window.getWindow(), input.getValue()) == GLFW.GLFW_PRESS;

            //version 1.21.9 - 26.2
            //return GLFW.glfwGetMouseButton(window.handle(), input.getValue()) == GLFW.GLFW_PRESS;

            //version 26.3+
            return (SDLMouse.SDL_GetMouseState(null, null) & (1 << (input.getValue() - 1))) != 0;
        }

        //version 1.21.8-
        //return InputConstants.isKeyDown(window.getWindow(), input.getValue());

        //version 1.21.9 - 26.2
        //return InputConstants.isKeyDown(window, input.getValue());

        //version 26.3+
        return InputConstants.isKeyDown(input.getValue());
    }
}