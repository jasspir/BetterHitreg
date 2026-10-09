package you.jass.betterhitreg.utility;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.KeyMapping;

//version 26.2-
//import org.lwjgl.glfw.GLFW;

//version 26.3+
import org.lwjgl.sdl.SDLMouse;
import you.jass.betterhitreg.settings.Toggle;

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
    private static boolean expectedTurn;
    private static double expectedTurnX;
    private static double expectedTurnY;

    //version 26.3+
    private static double rawMovedX;
    private static double rawMovedY;

    private static final boolean[] minecraftInput = new boolean[Input.values().length];

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

        checkGLFW(window, Input.UP, client.options.keyUp, time);
        checkGLFW(window, Input.DOWN, client.options.keyDown, time);
        checkGLFW(window, Input.LEFT, client.options.keyLeft, time);
        checkGLFW(window, Input.RIGHT, client.options.keyRight, time);
        checkGLFW(window, Input.JUMP, client.options.keyJump, time);
        checkGLFW(window, Input.LEFT_CLICK, client.options.keyAttack, time);
        checkGLFW(window, Input.RIGHT_CLICK, client.options.keyUse, time);

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

        Input.MOUSE_DELTA_X.value = rawDeltaX;
        Input.MOUSE_DELTA_Y.value = rawDeltaY;
        setRaw(Input.MOUSE_DELTA_X, rawDeltaX != 0, rawDeltaX, time);
        setRaw(Input.MOUSE_DELTA_Y, rawDeltaY != 0, rawDeltaY, time);

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
        minecraftInput[input.ordinal()] = toggled;
    }

    private static void setRaw(Input input, boolean toggled, double value, long time) {
        if (input.toggled != toggled) {
            input.previousDuration = time - input.changed;
            input.changed = time;
        }
        input.toggled = toggled;
        input.value = value;
    }

    private static void checkGLFW(Window window, Input input, KeyMapping key, long time) {
        boolean rawToggled = isKeyDown(window, key);
        input.suspicious = minecraftInput[input.ordinal()] != rawToggled;
        setRaw(input, rawToggled, rawToggled ? 1.0 : 0.0, time);
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

    public static void setExpectedTurn(double x, double y) {
        expectedTurnX = x;
        expectedTurnY = y;
        expectedTurn = true;
    }

    public static boolean hasExpectedTurn() {
        return expectedTurn;
    }

    public static void checkTurn(double yaw, double pitch) {
        if (!expectedTurn) return;

        double errorX = Math.abs(yaw - expectedTurnX);
        double errorY = Math.abs(pitch - expectedTurnY);

        if (errorX > 0.0001) Input.MOUSE_DELTA_X.suspicious = true;
        if (errorY > 0.0001) Input.MOUSE_DELTA_Y.suspicious = true;
    }

    public static void clearExpectedTurn() {
        expectedTurn = false;
    }

    public static void flagMouseX() {
        Input.MOUSE_DELTA_X.suspicious = true;
    }

    public static void flagMouseY() {
        Input.MOUSE_DELTA_Y.suspicious = true;
    }
}