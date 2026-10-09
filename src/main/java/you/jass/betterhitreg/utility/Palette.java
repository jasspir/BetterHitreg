package you.jass.betterhitreg.utility;

public enum Palette {
    CHRISTMAS("d1d1d1", "a8a8a8", "F3F0E6", "B72D3C", "9DE3A5"),
    HALLOWEEN("000000", "646464", "FFB02E", "f7b611", "FFB02E"),
    CHINESE_NEW_YEAR("280707", "D9A82E", "FFF0C7", "FFCB55", "E53935"),
    EASTER("151020", "BFA7F2", "F7F0FF", "BFE8C3", "E7A7D8"),
    NEW_YEAR("280707", "D9A82E", "FFF0C7", "FFCB55", "E53935");

    public final String background, border, text, hovered, highlighted;

    Palette(String bg, String border, String text, String hovered, String highlighted) {
        this.background = bg;
        this.border = border;
        this.text = text;
        this.hovered = hovered;
        this.highlighted = highlighted;
    }

    String color(String role) {
        return switch (role) {
            case "BACKGROUND" -> background;
            case "BORDER" -> border;
            case "TEXT" -> text;
            case "HOVERED" -> hovered;
            case "HIGHLIGHTED" -> highlighted;
            default -> null;
        };
    }
}