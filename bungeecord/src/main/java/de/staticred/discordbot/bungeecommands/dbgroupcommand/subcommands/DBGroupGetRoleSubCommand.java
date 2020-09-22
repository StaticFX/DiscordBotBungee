package de.staticred.discordbot.bungeecommands.dbgroupcommand.subcommands;

import de.staticred.discordbot.DBVerifier;
import de.staticred.discordbot.files.ConfigFileManager;
import de.staticred.discordbot.files.DiscordFileManager;
import de.staticred.discordbot.util.SubCommand;
import net.dv8tion.jda.api.entities.Role;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;

import javax.security.auth.login.Configuration;

public class DBGroupGetRoleSubCommand extends SubCommand {

    public DBGroupGetRoleSubCommand(String name, CommandSender sender, String[] args) {
        super(name, sender, args);
    }

    @Override
    public void execute(String name, CommandSender sender, String[] args) {

        if (!sender.hasPermission("db.cmd.dbgroup.getrole") && !sender.hasPermission("db.*")) {
            sender.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("NoPermissions", true)));
            return;
        }

        if (args.length != 2) {
            sender.sendMessage(new TextComponent("§cUse: /dbgroup getRole <group>"));
            return;
        }

        String givenGroup = args[1];

        if (DiscordFileManager.INSTANCE.getAllGroups().size() == 0) {
            sender.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("GroupsDoesntExist", true)));
            return;
        }

        if (!DiscordFileManager.INSTANCE.doesGroupExist(givenGroup)) {
            sender.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("NoGroupsYet", true)));
            return;
        }


        Role role;

        if(ConfigFileManager.INSTANCE.useTokens()) {
            role = DBVerifier.getInstance().jda.getRoleById(DiscordFileManager.INSTANCE.getDiscordGroupIDForGroup(givenGroup));
        }else{

            if(DBVerifier.getInstance().jda.getRolesByName(DiscordFileManager.INSTANCE.getDiscordGroupNameForGroup(givenGroup),false).size() == 0) {
                sender.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("RoleFound",true).replaceAll("%group%",givenGroup).replaceAll("%role%","null")));
                return;
            }

            role = DBVerifier.getInstance().jda.getRolesByName(DiscordFileManager.INSTANCE.getDiscordGroupNameForGroup(givenGroup),false).get(0);
        }

        if(role == null) {
            sender.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("RoleFound",true).replaceAll("%group%",givenGroup).replaceAll("%role%","null")));
        }else{
            sender.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("RoleFound",true).replaceAll("%group%",givenGroup).replaceAll("%role%",role.getName())));
        }


    }

}
