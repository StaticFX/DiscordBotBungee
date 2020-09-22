package de.staticred.discordbot.bungeecommands.dbgroupcommand.subcommands;

import de.staticred.discordbot.DBVerifier;
import de.staticred.discordbot.files.DiscordFileManager;
import de.staticred.discordbot.util.SubCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;

public class DBGroupInfoSubCommand extends SubCommand {

    public DBGroupInfoSubCommand(String name, CommandSender sender, String[] args) {
        super(name, sender, args);
    }

    @Override
    public void execute(String name, CommandSender sender, String[] args) {

        if(!sender.hasPermission("db.cmd.dbgroup.info") && !sender.hasPermission("db.*")) {
            sender.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("NoPermissions",true)));
            return;
        }

        if(args.length != 2) {
            sender.sendMessage(new TextComponent("§cUse: /dbgroup info <group>"));
            return;
        }

        String givenGroup = args[1];

        if(DiscordFileManager.INSTANCE.getAllGroups().size() == 0) {
            sender.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("GroupsDoesntExist",true)));
            return;
        }

        if(!DiscordFileManager.INSTANCE.doesGroupExist(givenGroup)) {
            sender.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("NoGroupsYet",true)));
            return;
        }

        sender.sendMessage(new TextComponent("§aInfo about: §e" + givenGroup));
        sender.sendMessage(new TextComponent("§aGroupName: §e" + DiscordFileManager.INSTANCE.getDiscordGroupNameForGroup(givenGroup)));
        sender.sendMessage(new TextComponent("§aPermission: §e" + DiscordFileManager.INSTANCE.getPermissionsForGroup(givenGroup)));
        sender.sendMessage(new TextComponent("§aDynamic: §e" + DiscordFileManager.INSTANCE.isDynamicGroup(givenGroup)));
        sender.sendMessage(new TextComponent("§aPrefix §e" + DiscordFileManager.INSTANCE.getPrefix(givenGroup)));

    }

}
