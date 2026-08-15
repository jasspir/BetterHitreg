package you.jass.betterhitreg.settings;

import you.jass.betterhitreg.utility.Render;

import java.awt.*;

public enum Style {
    CROSS_FAR("FFFFFF", 255, "render"),
    CROSS_NEAR("FF0000", 255, "render"),
    CROSS_FAR_WITH_HITBOX("0000FF", 255, "render"),
    CROSS_NEAR_WITH_HITBOX("0000FF", 255, "render"),
    HITBOX_FAR("FFFFFF", 255, "render"),
    HITBOX_NEAR("FF0000", 255, "render"),
    SERVER_HITBOX_FAR("7F00FF", 125, "render"),
    SERVER_HITBOX_NEAR("7F00FF", 125, "render"),
    APPROACH_HITBOX("FFFFFF", 125, "render"),
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
    GRID("FFFFFF", 255, "render"),
    GROUND("000000", 255, "render"),
    BACKGROUND("000000", 230, "ui"),
    BORDER("646464", 255, "ui"),
    TEXT("DEDEDE", 255, "ui"),
    HOVERED("FFF3A6", 255, "ui"),
    HIGHLIGHTED("FFE350", 255, "ui");

    private String hex;
    private int opacity;
    private final String category;
    private Color color;
    private int argb;
    private int abgr;
    private int rgb;

    Style(String hex, int opacity, String category) {
        this.hex = hex;
        this.opacity = opacity;
        this.category = category;

        update();
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

    public Color color() {
        return color;
    }

    public int argb() {return argb;}

    public int abgr() {return abgr;}

    public int rgb() {return rgb;}

    public String colorKey() {
        return name().toLowerCase() + "_color";
    }

    public String opacityKey() {
        return name().toLowerCase() + "_opacity";
    }

    public void set(String hex, int opacity) {
        this.hex = hex;
        this.opacity = opacity;
        Settings.set(colorKey(), hex);
        Settings.setInt(opacityKey(), opacity);
        update();
    }

    public void update() {
        int alpha = Math.max(0, Math.min(255, opacity));
        java.awt.Color rgb = java.awt.Color.decode("#" + hex.replace("#", ""));
        color = new java.awt.Color(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), alpha);
        argb = color.getRGB();
        abgr = (argb & 0xFF00FF00) | ((argb & 0xFF) << 16) | ((argb >> 16) & 0xFF);
        this.rgb = argb & 0x00FFFFFF;
    }

    public static void updateAll() {
        for (Style style : Style.values()) style.update();
    }
}