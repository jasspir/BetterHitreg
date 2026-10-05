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

            for (Setting setting : Setting.values()) {
                if (!setting.category().equals("configure")) {
                    continue;
                }

                root.then(literal(setting.commandName())
                        .then(argument("value", IntegerArgumentType.integer())
                                .executes(context -> {
                                    int value = IntegerArgumentType.getInteger(context, "value");
                                    return setSetting(setting, value);
                                }))
                        .executes(context -> {
                            int value = Integer.parseInt(setting.defaultValue());
                            return setSetting(setting, value);
                        }));
            }

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

    public static int setSetting(Setting setting, double value) {
        String extra = "";
        double multiplier = 1;
        if (setting == Setting.HITREG && !Toggle.TOGGLE.toggled()) message("custom hitreg §7is currently off, use §f/hitreg toggle §7to enable it", "/hitreg toggle");
        else if (setting == Setting.METRONOME) extra = " §7ticks (" + (value * 50) + "ms)";
        else if (setting == Setting.GRID_RANGE || setting == Setting.SOUND_RECENCY_THRESHOLD || setting == Setting.APPROACH_HITBOX_RANGE || setting == Setting.GROUND_HEIGHT) extra = " §7(default is " + setting.defaultValue() + ")";
        else if (setting == Setting.MUFFLE_AMOUNT || setting == Setting.SHARPEN_AMOUNT) {
            value = value / 100f;
            multiplier = 100;
            extra = "§7%";
        }

        int displayValue = (int) (value * multiplier);

        Settings.set(setting.key(), String.valueOf(value));
        if (setting.isDisabled(value)) message(setting.displayName().toLowerCase() + " §cdisabled", "/hitreg " + setting.commandName() + " " + displayValue);
        else message(setting.displayName().toLowerCase() + " §7set to §f" + displayValue + extra, "/hitreg " + setting.commandName() + " " + displayValue);
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
