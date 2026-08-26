package you.jass.betterhitreg.settings;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;
import you.jass.betterhitreg.BetterHitreg;
import you.jass.betterhitreg.ui.UIScreen;
import you.jass.betterhitreg.utility.MultiVersion;
import you.jass.betterhitreg.utility.Scheduler;

//version 1.21.11-
//import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
//import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

//version 26.1+
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

import static you.jass.betterhitreg.utility.MultiVersion.message;

public class Commands {
    public static void initialize() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registry) -> {
            var root = literal("hitreg");

            for (Toggle toggle : Toggle.values()) {
                root = root.then(literal(toggle.key())
                .executes(context -> {
                   toggle.toggle();
                   return 1;
                }));
            }

            var colorArg = literal("color");
            for (Style style : Style.values()) {
                var styleArg = literal(style.name().toLowerCase())
                        .executes(context -> showColor(style))
                        .then(argument("hex", StringArgumentType.word())
                                .executes(context -> setColor(context, style, false))
                                .then(argument("opacity", IntegerArgumentType.integer(0, 255))
                                        .executes(context -> setColor(context, style, true))));
                colorArg.then(styleArg);
            }
            root.then(colorArg);

            root = root.then(literal("setHitreg")
                   .then(argument("value", IntegerArgumentType.integer())
                   .executes(context -> setHitreg(IntegerArgumentType.getInteger(context, "value"))))
                   .executes(context -> setHitreg(0)));

            root = root.then(literal("setMuffle")
                    .then(argument("value", IntegerArgumentType.integer())
                    .executes(context -> setMuffle(IntegerArgumentType.getInteger(context, "value"))))
                    .executes(context -> setMuffle(0)));

            root = root.then(literal("setSharpen")
                    .then(argument("value", IntegerArgumentType.integer())
                            .executes(context -> setSharpen(IntegerArgumentType.getInteger(context, "value"))))
                    .executes(context -> setSharpen(0)));

            root = root.then(literal("setMetronome")
                    .then(argument("value", IntegerArgumentType.integer())
                            .executes(context -> setMetronome(IntegerArgumentType.getInteger(context, "value"))))
                    .executes(context -> setMetronome(0)));


            root = root.then(literal("setGridSize")
                    .then(argument("value", IntegerArgumentType.integer())
                            .executes(context -> setGridSize(IntegerArgumentType.getInteger(context, "value"))))
                    .executes(context -> setGridSize(0)));

            root = root.then(literal("setGridRange")
                    .then(argument("value", IntegerArgumentType.integer())
                            .executes(context -> setGridRange(IntegerArgumentType.getInteger(context, "value"))))
                    .executes(context -> setGridRange(0)));


            root = root.then(literal("setSoundRecencyThreshold")
                    .then(argument("value", IntegerArgumentType.integer())
                            .executes(context -> setSoundRecencyThreshold(IntegerArgumentType.getInteger(context, "value"))))
                    .executes(context -> setSoundRecencyThreshold(50)));

            root = root.then(literal("setApproachHitboxRange")
                    .then(argument("value", IntegerArgumentType.integer())
                            .executes(context -> setApproachHitboxRange(IntegerArgumentType.getInteger(context, "value"))))
                    .executes(context -> setApproachHitboxRange(6)));

            dispatcher.register(root.executes(context -> menu()));
        });
    }

    public static int menu() {
        Scheduler.schedule(50, ()-> {
            if (!MultiVersion.isScreenOpen()) MultiVersion.openScreen(new UIScreen());
        });

        return 1;
    }

    public static String getUIKey() {
        return BetterHitreg.uiKey.saveString()
                .replace("key.keyboard.", "")
                .replace("key.mouse.", "")
                .replace(".", " ")
                .toUpperCase();
    }

    public static int setHitreg(int hitreg) {
        if (hitreg < 0) hitreg = 0;
        Settings.set("hitreg", String.valueOf(hitreg));
        message("hitreg §7set to §f" + hitreg + "§7ms", "/hitreg setHitreg " + hitreg);
        if (!Toggle.TOGGLE.toggled()) message("custom hitreg §7is currently off, use §f/hitreg toggle §7to enable it", "/hitreg toggle");
        return 1;
    }

    public static int setMuffle(int muffle) {
        if (muffle <= 0) {
            Settings.setFloat("muffle_amount", 0);
            message("hitsound muffling §cdisabled", "/hitreg setMuffle " + muffle);
            return 1;
        }

        Settings.setFloat("muffle_amount", muffle / 100f);
        message("hitsound muffling §7set to §f" + muffle + "§7%", "/hitreg setMuffle " + muffle);
        return 1;
    }

    public static int setSharpen(int sharpen) {
        if (sharpen <= 0) {
            Settings.setFloat("sharpen_amount", 0);
            message("hitsound sharpening §cdisabled", "/hitreg setSharpen 0");
            return 1;
        }

        Settings.setFloat("sharpen_amount", sharpen / 100f);
        message("hitsound sharpening §7set to §f" + sharpen + "§7%", "/hitreg setSharpen " + sharpen);
        return 1;
    }

    public static int setMetronome(int metronome) {
        if (metronome < 0) metronome = 0;
        if (metronome == 0) message("metronome §cdisabled", "/hitreg metronome");
        else message("metronome §7set to §f" + metronome + " §7ticks (" + (metronome * 50) + "ms)", "/hitreg metronome " + metronome);
        Settings.setInt("metronome", metronome);
        return 1;
    }

    public static int setGridSize(int size) {
        Settings.setInt("floor_grid_size", size);
        message("grid size §7set to §f" + size, "/hitreg setGridSize " + size);
        return 1;
    }

    public static int setGridRange(int range) {
        Settings.setInt("floor_grid_range", range);
        message("grid range §7set to §f" + range + " §7(default is 16)", "/hitreg setGridRange " + range);
        return 1;
    }

    public static int setSoundRecencyThreshold(int threshold) {
        Settings.setInt("sound_recency_threshold", threshold);
        message("sound recency threshold §7set to §f" + threshold + " §7(default is 50)", "/hitreg setSoundRecencyThreshold " + threshold);
        return 1;
    }

    public static int setApproachHitboxRange(int range) {
        Settings.setInt("approach_hitbox_range", range);
        message("approach hitbox range §7set to §f" + range + " §7(default is 6)", "/hitreg setApproachHitboxRange " + range);
        return 1;
    }

    public static String onOrOff(boolean setting) {
        return setting ? "§aon§7" : "§coff§7";
    }

    private static int showColor(Style style) {
        message(style.name() + " current color: #" + style.hex() + " opacity: " + style.opacity(), "hitreg color " + style.name());
        return 1;
    }

    private static int setColor(CommandContext<FabricClientCommandSource> context, Style style, boolean hasOpacity) {
        String oldHex = style.hex();
        int oldOpacity = style.opacity();

        String newHex = StringArgumentType.getString(context, "hex");
        if (!isValidHex(newHex)) {
            message("Invalid hexadecimal color", "hitreg color " + style.name());
            return 1;
        }

        newHex = newHex.toUpperCase();

        int newOpacity = hasOpacity ? IntegerArgumentType.getInteger(context, "opacity") : 255;
        style.set(newHex, newOpacity);

       message("Changed " + style.name() + " from #" + oldHex + " " + oldOpacity + " to #" + newHex + " " + newOpacity, "hitreg color " + style.name());
        return 1;
    }

    private static boolean isValidHex(String hex) {
        return hex.matches("^[0-9a-fA-F]{6}$");
    }
}
