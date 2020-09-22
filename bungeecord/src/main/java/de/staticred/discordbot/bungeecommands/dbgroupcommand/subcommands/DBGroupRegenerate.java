package de.staticred.discordbot.bungeecommands.dbgroupcommand.subcommands;

import de.staticred.discordbot.DBVerifier;
import de.staticred.discordbot.files.DiscordFileManager;
import de.staticred.discordbot.util.SubCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;

public class DBGroupRegenerate extends SubCommand {

    public DBGroupRegenerate(String name, CommandSender sender, String[] args) {
        super(name, sender, args);
    }

    @Override
    public void execute(String name, CommandSender sender, String[] args) {

        if(!sender.hasPermission("db.cmd.dbgroup.regenerate") && !sender.hasPermission("db.*")) {
            sender.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("NoPermissions",true)));
            return;
        }

        if(args.length != 1) {
            sender.sendMessage(new TextComponent("§cUse: /dbgroup regenerate"));
            return;
        }

        if(DiscordFileManager.INSTANCE.getAllGroups().size() == 0) {
            sender.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("NoGroupsYet",true)));
            return;
        }

        DiscordFileManager.INSTANCE.generateGroupConfig();
        sender.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("GroupsRegenerated",true)));
    }
}
