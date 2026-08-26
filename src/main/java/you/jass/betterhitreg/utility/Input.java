package you.jass.betterhitreg.utility;

public enum Input {
    UP("Up"),
    DOWN("Down"),
    LEFT("Left"),
    RIGHT("Right"),
    JUMP("Jump"),
    LEFT_CLICK("LeftClick"),
    RIGHT_CLICK("RightClick"),
    MOUSE_DELTA_X("MouseΔX"),
    MOUSE_DELTA_Y("MouseΔY");

    public final String name;
    public boolean toggled;
    public double value;
    public boolean suspicious;
    public long changed;
    public long duration;
    public long previousDuration;

    Input(String name) {
        this.name = name;
    }
}