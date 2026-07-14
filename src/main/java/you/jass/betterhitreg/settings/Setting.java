package you.jass.betterhitreg.settings;

public enum Setting {
    TUTORIAL("tutorial", "toggle", "true"),
    HITREG("hitreg", "configure", "0"),
    TOTAL_FIGHTS("total_fights", "tracked", "0"),
    FIGHT_PLAYTIME_SECONDS("fight_playtime_(seconds)", "tracked", "0"),
    MUFFLE_AMOUNT("muffle_amount", "configure", "0"),
    SHARPEN_AMOUNT("sharpen_amount", "configure", "0"),
    METRONOME("metronome", "configure", "0"),
    GRID_FLOOR("floor_grid_size", "configure", "0");

    private final String key;
    private final String category;
    private final String defaultValue;

    Setting(String key, String category, String defaultValue) {
        this.key = key;
        this.category = category;
        this.defaultValue = defaultValue;
    }

    public double get() {
        return Settings.getDouble(key);
    }

    public String key() {
        return key;
    }

    public String category() {
        return category;
    }

    public String defaultValue() {
        return defaultValue;
    }
}