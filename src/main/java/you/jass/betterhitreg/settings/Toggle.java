package you.jass.betterhitreg.settings;

import you.jass.betterhitreg.utility.MultiVersion;

public enum Toggle {
    TOGGLE("toggle", "custom hitreg", true),
    SAFE_REGS_ONLY("safeRegsOnly", "safe regs only", true),
    IGNORE_SHIELD_HOLDERS("ignoreShieldHolders", "ignore shield holders", false),
    ALERT_DELAYS("alertDelays", "alert delays", false),
    ALERT_GHOSTS("alertGhosts", "alert ghosts", false),
    ALERT_INCONSISTENCIES("alertInconsistencies", "alert inconsistencies", false),
    ALERT_FIGHTS("alertFights", "alert fights", false),
    LEGACY_SOUNDS("legacySounds", "1.8 sounds", false),
    HIDE_ANIMATIONS("hideAnimations", "hide animations", false),
    HIDE_ARMOR("hideArmor", "hide armor", false),
    HIDE_ALL_PARTICLES("hideAllParticles", "hide crit particles", false),
    HIDE_OTHER_PARTICLES("hideOtherParticles", "only crit/sweep particles", false),
    PARTICLES_EVERY_HIT("particlesEveryHit", "particles on every hit", false),
    SILENCE_OTHER_FIGHTS("silenceOtherFights", "silence other fights", false),
    SILENCE_SELF("silenceSelf", "silence your hits", false),
    SILENCE_THEM("silenceThem", "silence their hits", false),
    SILENCE_NON_HITS("silenceNonHits", "silence non-hits", false),
    HIDE_OTHER_FIGHTS("hideOtherFights", "hide other fights", false),
    RENDER_HITBOX("renderHitbox", "render target hitbox", false),
    RENDER_CROSS("renderCross", "render target cross", false),
    RENDER_SERVER_HITBOX("RenderServerHitbox", "render server hitbox", false),
    RENDER_APPROACH_HITBOX("RenderApproachHitbox", "render approach hitbox", false),
    RENDER_YOUR_REACH("RenderYourReach", "render your reach", false),
    RENDER_THEIR_REACH("RenderTheirReach", "render their reach", false),
    RENDER_YOUR_JUMP("RenderYourJump", "render your jump range", false),
    RENDER_THEIR_JUMP("RenderTheirJump", "render their jump range", false),
    PERFECT_HIT_COLOR("PerfectHitColor", "color first tick hits", false),
    JUMP_RESET_COLOR("JumpResetColor", "color jump resets", false),
    ALERT_JUMP_RESETS("AlertJumpResets", "alert jump resets", false),
    VOID_WORLD("VoidWorld", "void world", false),
    CUSTOM_GROUND("CustomGround", "custom ground", false);

    private final String key;
    private final String label;
    private final boolean defaultValue;

    Toggle(String key, String label, boolean defaultValue) {
        this.key = key;
        this.label = label;
        this.defaultValue = defaultValue;
    }

    public String key() {
        return key;
    }

    public String label() {
        return label;
    }

    public boolean defaultValue() {
        return defaultValue;
    }

    public boolean toggled() {
        return Boolean.parseBoolean(Settings.get(key));
    }

    public boolean toggle() {
        boolean value = Settings.toggle(key);
        String command = "/hitreg " + key;
        MultiVersion.message(label + " §7is now " + (value ? "§aon§7" : "§coff§7"), command);

        switch (this) {
            case SAFE_REGS_ONLY -> MultiVersion.message("§7first hits " + (value ? "will no longer" : "will now") + " use custom hitreg", command);
            case IGNORE_SHIELD_HOLDERS -> MultiVersion.message("§7players with a shield (blocking or not) " + (value ? "will no longer" : "will now") + " be affected by custom hitreg", command);
            case RENDER_HITBOX, RENDER_CROSS, RENDER_SERVER_HITBOX, RENDER_APPROACH_HITBOX, RENDER_YOUR_REACH, RENDER_THEIR_REACH, RENDER_YOUR_JUMP, RENDER_THEIR_JUMP, PERFECT_HIT_COLOR, JUMP_RESET_COLOR, CUSTOM_GROUND -> MultiVersion.message("§7colors can be edited via §f/hitreg color <key> <hex> <opacity>", command);
            case VOID_WORLD -> MultiVersion.message("§7this feature may not work if you're using render mods or a modded client", command);
        }

        return value;
    }
}