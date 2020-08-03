package de.staticred.discordbot.bungeecommands.dbcommand.subcommands;

import de.staticred.discordbot.DBVerifier;
import de.staticred.discordbot.files.SettingsFileManager;
import de.staticred.discordbot.util.SubCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;

public class DBDebugSubCommand extends SubCommand {

    public DBDebugSubCommand(String name, CommandSender sender, String[] args) {
        super(name, sender, args);
    }


    @Override
    public void execute(String name, CommandSender sender, String[] args) {
        if(!sender.hasPermission("db.cmd.debug") && !sender.hasPermission("db.*")) {
            sender.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("NoPermissions",true)));
            return;
        }

        if(args.length != 1) {
            sender.sendMessage(new TextComponent("§cUse: /db debug"));
            return;
        }

        SettingsFileManager.INSTANCE.toggleDebug();
        DBVerifier.getInstance().debugMode = SettingsFileManager.INSTANCE.isDebug();

        sender.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("DebugModeSwitched",true).replaceAll("%mode%",Boolean.toString(DBVerifier.getInstance().debugMode))));
    }
}
