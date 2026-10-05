package you.jass.betterhitreg.settings;
public enum Setting {
    TUTORIAL("tutorial", "toggle", "true", null),
    HITREG("hitreg", "configure", "0", "0"),
    TOTAL_FIGHTS("total_fights", "tracked", "0", null),
    FIGHT_PLAYTIME_SECONDS("fight_playtime_(seconds)", "tracked", "0", null),

    MUFFLE_AMOUNT("muffle_amount", "configure", "0", "0"),
    SHARPEN_AMOUNT("sharpen_amount", "configure", "0", "0"),
    METRONOME("metronome", "configure", "0", "9"),

    GRID_SIZE("floor_grid_size", "configure", "0", null),
    GRID_RANGE("floor_grid_range", "configure", "16", null),
    GROUND_HEIGHT("ground_height", "configure", "0.01", null),
    GROUND_SIZE("ground_size", "configure", "512", null),
    SCORE_X("score_x", "configure", "1", null),
    SCORE_Y("score_y", "configure", "1", null),

    APPROACH_HITBOX_RANGE("approach_hitbox_range", "configure", "6", null),

    //the sound hit window is how far apart the server can send a hit's sounds and its damage event while still counting them as the same hit
    SOUND_HIT_WINDOW("sound_hit_window", "configure", "150", null),
    SOUND_RECENCY_THRESHOLD("sound_recency_threshold", "configure", "50", null);

    private final String key;
    private final String category;
    private final String defaultValue;
    private final String disabledValue;

    Setting(String key, String category, String defaultValue, String disabledValue) {
        this.key = key;
        this.category = category;
        this.defaultValue = defaultValue;
        this.disabledValue = disabledValue;
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

    public String disabledValue() {
        return disabledValue;
    }

    public boolean isDisabled(double value) {
        return disabledValue != null && Double.parseDouble(disabledValue) == value;
    }

    public String displayName() {
        String name = key.replace("_", " ").replace("-", " ");
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    public String commandName() {
        StringBuilder result = new StringBuilder("set");
        boolean capitalizeNext = true;

        for (char character : key.toCharArray()) {
            if (!Character.isLetterOrDigit(character)) {
                capitalizeNext = true;
                continue;
            }

            if (capitalizeNext) {
                result.append(Character.toUpperCase(character));
                capitalizeNext = false;
            } else {
                result.append(character);
            }
        }

        return result.toString();
    }
}