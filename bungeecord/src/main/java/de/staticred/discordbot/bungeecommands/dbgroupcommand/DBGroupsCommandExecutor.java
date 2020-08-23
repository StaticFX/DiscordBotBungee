package de.staticred.discordbot.bungeecommands.dbgroupcommand;

import de.staticred.discordbot.bungeecommands.dbcommand.subcommands.*;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class DBGroupsCommandExecutor extends Command {

    public DBGroupsCommandExecutor(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer) {
            if(!sender.hasPermission("db.cmd") && !sender.hasPermission("db.*")) {
                return;
            }
        }

        if(args.length == 0) {
            sender.sendMessage(new TextComponent("§8---§aDCVerifier§8---"));
            sender.sendMessage(new TextComponent("§a/dbgroup list §7- §eInformation about all groups."));
            sender.sendMessage(new TextComponent("§a/dbgroup info §7- §eInformation about a specific group."));
            sender.sendMessage(new TextComponent("§a/dbgroup checkrole §7- §eCheck if the groups role can be found"));
            return;
        }


        String subCommand = args[0];

        switch (subCommand.toUpperCase()) {

            case "LIST": {
                new DBReloadSubCommand("list",sender,args).execute("list",sender,args);
                return;
            }
            case "INFO": {
                new DBDebugSubCommand("info",sender,args).execute("info",sender,args);
                return;
            }
            case "CHECKROLE": {
                new DBInfoSubCommand("checkrole", sender, args).execute("checkrole", sender, args);
                return;
            }
        }
        sender.sendMessage(new TextComponent("§8---§aDCVerifier§8---"));
        sender.sendMessage(new TextComponent("§a/dbgroup list §7- §eInformation about all groups."));
        sender.sendMessage(new TextComponent("§a/dbgroup info §7- §eInformation about a specific group."));
        sender.sendMessage(new TextComponent("§a/dbgroup checkrole §7- §eCheck if the groups role can be found"));
    }
}
