package you.jass.betterhitreg.settings;

public enum Color {
    CROSS_FAR("FFFFFF", 255, "render"),
    CROSS_NEAR("FF0000", 255, "render"),
    CROSS_FAR_WITH_HITBOX("0000FF", 255, "render"),
    CROSS_NEAR_WITH_HITBOX("0000FF", 255, "render"),
    HITBOX_FAR("FFFFFF", 255, "render"),
    HITBOX_NEAR("FF0000", 255, "render"),
    SERVER_HITBOX("7F00FF", 125, "render"),
    YOUR_REACH_FAR("FFFFFF", 255, "render"),
    YOUR_REACH_NEAR("FF0000", 255, "render"),
    THEIR_REACH_FAR("FFFFFF", 255, "render"),
    THEIR_REACH_NEAR("FF0000", 255, "render"),
    YOUR_JUMP_FAR("007FFF", 255, "render"),
    YOUR_JUMP_NEAR("007FFF", 255, "render"),
    THEIR_JUMP_FAR("007FFF", 255, "render"),
    THEIR_JUMP_NEAR("007FFF", 255, "render"),
    JUMP_RESET("FFFF00", 255, "render"),
    PERFECT_HIT("00FF00", 255, "render"),
    BACKGROUND("000000", 230, "ui"),
    BORDER("646464", 255, "ui"),
    TEXT("DEDEDE", 255, "ui"),
    HOVERED("FFF3A6", 255, "ui"),
    HIGHLIGHTED("FFE350", 255, "ui");

    private final String hex;
    private final int opacity;
    private final String category;

    Color(String hex, int opacity, String category) {
        this.hex = hex;
        this.opacity = opacity;
        this.category = category;
    }

    public String hex() {
        return hex;
    }

    public int opacity() {
        return opacity;
    }

    public String category() {
        return category;
    }

    public String colorKey() {
        return name().toLowerCase() + "_color";
    }

    public String opacityKey() {
        return name().toLowerCase() + "_opacity";
    }
}