package you.jass.betterhitreg.settings;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

//version 1.21.11-
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

//version 26.1+
//import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;
//import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

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

            dispatcher.register(root.executes(context -> guide()));
        });
    }

    public static int guide() {
        message("/hitreg <command> (press " + getUIKey() + " for UI)", "/hitreg " + Toggle.TOGGLE.key());
        message("custom hitreg: " + "§f" + Settings.getHitreg() + "§7ms", "/hitreg set 0");

        for (Toggle toggle : Toggle.values()) {
            if (toggle == Toggle.TOGGLE) {
                message("§fhitreg toggled§7: " + onOrOff(toggle.toggled()), "/hitreg " + toggle.key());
                continue;
            }

            message("§f" + toggle.label() + "§7: " + onOrOff(toggle.toggled()), "/hitreg " + toggle.key());
        }

        if (Settings.getBoolean("tutorial")) Settings.set("tutorial", "false");
        return 1;
    }

    public static String getUIKey() {
        return you.jass.betterhitreg.BetterHitreg.uiKey.saveString()
                .replace("key.keyboard.", "")
                .replace("key.mouse.", "")
                .replace(".", " ")
                .toUpperCase();
    }

    public static int setHitreg(int hitreg) {
        if (hitreg < 0) {
            Settings.set("toggled", "false");
            message("custom hitreg §7is now §coff", "/hitreg " + Toggle.TOGGLE.key());
            return 1;
        }

        Settings.set("hitreg", String.valueOf(hitreg));
        message("hitreg §7set to §f" + hitreg + "§7ms", "/hitreg setHitreg 0");

        if (!Toggle.TOGGLE.toggled()) Toggle.TOGGLE.toggle();
        return 1;
    }

    public static int setMuffle(int muffle) {
        if (muffle <= 0) {
            Settings.setFloat("muffle_amount", 0);
            message("hitsound muffling §cdisabled", "/hitreg setMuffle 0");
            return 1;
        }

        Settings.setFloat("muffle_amount", muffle / 100f);
        message("hitsound muffling §7set to §f" + muffle + "§7%", "/hitreg setMuffle 0");
        return 1;
    }

    public static int setSharpen(int sharpen) {
        if (sharpen <= 0) {
            Settings.setFloat("sharpen_amount", 0);
            message("hitsound sharpening §cdisabled", "/hitreg setSharpen 0");
            return 1;
        }

        Settings.setFloat("sharpen_amount", sharpen / 100f);
        message("hitsound sharpening §7set to §f" + sharpen + "§7%", "/hitreg setSharpen 0");
        return 1;
    }

    public static String onOrOff(boolean setting) {
        return setting ? "§aon§7" : "§coff§7";
    }
}
