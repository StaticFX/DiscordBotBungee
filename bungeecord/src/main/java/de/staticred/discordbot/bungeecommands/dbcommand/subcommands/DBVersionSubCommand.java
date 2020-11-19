package de.staticred.discordbot.bungeecommands.dbcommand.subcommands;

import de.staticred.discordbot.DBVerifier;
import de.staticred.discordbot.files.ConfigFileManager;
import de.staticred.discordbot.util.SubCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;

public class DBVersionSubCommand extends SubCommand {

    public DBVersionSubCommand(String name, CommandSender sender, String[] args) {
        super(name, sender, args);
    }

    @Override
    public void execute(String name, CommandSender sender, String[] args) {

        if(!sender.hasPermission("db.cmd.version") && !sender.hasPermission("db.*")) {
            sender.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("NoPermissions",true)));
            return;
        }

        if(args.length != 1) {
            sender.sendMessage(new TextComponent("§cUse: /db version"));
            return;
        }

        if(DBVerifier.getInstance().isUpdateAvailable()) {
            sender.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("prefix",false) + "§cThere is a new version available on the spigot.org\n§ahttps://www.spigotmc.org/resources/dbverifier-bungeecord-discord-verify-plugin.72232/"));
            return;
        }
        sender.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("prefix",false) + "§aYou're running the latest version. Current version: §e " + DBVerifier.pluginVersion));


    }
}
