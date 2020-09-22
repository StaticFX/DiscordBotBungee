package de.staticred.discordbot.bungeecommands.dbgroupcommand;

import de.staticred.discordbot.DBVerifier;
import de.staticred.discordbot.bungeecommands.dbcommand.subcommands.*;
import de.staticred.discordbot.bungeecommands.dbgroupcommand.subcommands.DBGroupCreateSubCommand;
import de.staticred.discordbot.bungeecommands.dbgroupcommand.subcommands.DBGroupGetRoleSubCommand;
import de.staticred.discordbot.bungeecommands.dbgroupcommand.subcommands.DBGroupInfoSubCommand;
import de.staticred.discordbot.bungeecommands.dbgroupcommand.subcommands.DBGroupListSubCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class DBGroupCommandExecutor extends Command {

    public DBGroupCommandExecutor(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        if(!sender.hasPermission("db.cmd.dbgroup") && !sender.hasPermission("db.*")) {
            sender.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("NoPermissions",true)));
            return;
        }

        if(args.length == 0) {
            sender.sendMessage(new TextComponent("§8---§aDCVerifier§8---"));
            sender.sendMessage(new TextComponent("§a/dbgroup list §7- §eList of all groups."));
            sender.sendMessage(new TextComponent("§a/dbgroup info §7- §eInfo about one group."));
            sender.sendMessage(new TextComponent("§a/dbgroup getrole §7- §eget the discord role linked to the group"));
            sender.sendMessage(new TextComponent("§a/dbgroup regenerate §7- §eRegenerate the discord.yml file (will §edelete your settings)."));
            sender.sendMessage(new TextComponent("§8---§cTools§8---"));
            sender.sendMessage(new TextComponent("§a/dbgroup create §7- §eCreate a group."));
            sender.sendMessage(new TextComponent("§a/dbgroup delete §7- §eDelete a group."));
            return;
        }


        String subCommand = args[0];

        switch (subCommand.toUpperCase()) {

            case "LIST": {
                new DBGroupListSubCommand("dblist",sender,args).execute("dblist",sender,args);
                return;
            }
            case "INFO": {
                new DBGroupInfoSubCommand("dbinfo",sender,args).execute("dbinfo",sender,args);
                return;
            }
            case "GETROLE": {
                new DBGroupGetRoleSubCommand("getrole",sender,args).execute("getrole",sender,args);
                return;
            }
            case "CREATE": {
                new DBGroupCreateSubCommand("dbcreate",sender,args).execute("dbcreate",sender,args);
                return;
            }
            case "DELETE": {
                new DBResetSubCommand("dbdelete",sender,args).execute("dbdelete",sender,args);
                return;
            }
        }

        sender.sendMessage(new TextComponent("§8---§aDCVerifier§8---"));
        sender.sendMessage(new TextComponent("§a/dbgroup list §7- §eList of all groups."));
        sender.sendMessage(new TextComponent("§a/dbgroup info §7- §eInfo about one group."));
        sender.sendMessage(new TextComponent("§a/dbgroup getrole §7- §eget the discord role linked to the group"));
        sender.sendMessage(new TextComponent("§a/dbgroup regenerate §7- §eRegenerate the discord.yml file (will §edelete your settings)."));
        sender.sendMessage(new TextComponent("§8---§cTools§8---"));
        sender.sendMessage(new TextComponent("§a/dbgroup create §7- §eCreate a group."));
        sender.sendMessage(new TextComponent("§a/dbgroup delete §7- §eDelete a group."));



    }
}
