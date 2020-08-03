package de.staticred.discordbot.bungeecommands.dbcommand.subcommands;

import de.staticred.discordbot.DBVerifier;
import de.staticred.discordbot.files.ConfigFileManager;
import de.staticred.discordbot.files.DiscordFileManager;
import de.staticred.discordbot.files.MessagesFileManager;
import de.staticred.discordbot.util.SubCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;

public class DBReloadSubCommand extends SubCommand {

    public DBReloadSubCommand(String name, CommandSender sender, String[] args) {
        super(name, sender, args);
    }

    @Override
    public void execute(String name, CommandSender sender, String[] args) {
        if(!sender.hasPermission("db.cmd.reload") && !sender.hasPermission("db.*")) {
            sender.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("NoPermissions",true)));
            return;
        }

        if(args.length != 1) {
            sender.sendMessage(new TextComponent("§cUse: /db reload"));
            return;
        }

        ConfigFileManager.INSTANCE.loadFile();
        MessagesFileManager.INSTANCE.loadFile();
        DiscordFileManager.INSTANCE.loadFile();

        sender.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("Reloaded",true)));
    }
}
