package de.staticred.discordbot.bungeecommands.dbcommand;

import de.staticred.discordbot.bungeecommands.dbcommand.subcommands.*;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class DBCommandExecutor extends Command {

    public DBCommandExecutor(String name) {
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
            sender.sendMessage(new TextComponent("§a/db reload §7- §eReload all files."));
            sender.sendMessage(new TextComponent("§a/db debug §7- §eTurn on/off debug mode."));
            sender.sendMessage(new TextComponent("§a/db info §7- §eGives all information of from a linked player."));
            sender.sendMessage(new TextComponent("§a/db unlink §7- §eUnlinks a player."));
            sender.sendMessage(new TextComponent("§a/db update §7- §eForce a player rankupdate."));
            return;
        }


        String subCommand = args[0];

        switch (subCommand.toUpperCase()) {

            case "RELOAD": {
                new DBReloadSubCommand("dbreload",sender,args).execute("dbreload",sender,args);
                return;
            }
            case "DEBUG": {
                new DBDebugSubCommand("dbdebug",sender,args).execute("dbdebug",sender,args);
                return;
            }
            case "INFO": {
                new DBInfoSubCommand("dbinfo",sender,args).execute("dbinfo",sender,args);
                return;
            }
            case "UNLINK": {
                new DBUnlinkSubCommand("dbunlink",sender,args).execute("dbunlink",sender,args);
                return;
            }
            case "UPDATE": {
                new DBUpdateSubCommand("dbupdate",sender,args).execute("dbupdate",sender,args);
                return;
            }
            case "RESET": {
                new DBResetSubCommand("dbupdate",sender,args).execute("dbupdate",sender,args);
                return;
            }
        }

        sender.sendMessage(new TextComponent("§8---§aDCVerifier§8---"));
        sender.sendMessage(new TextComponent("§a/db reload &7- &eReload all files."));
        sender.sendMessage(new TextComponent("§a/db debug &7- &eTurn on/off debug mode."));
        sender.sendMessage(new TextComponent("§a/db info &7- &eGives all information of from a linked player."));
        sender.sendMessage(new TextComponent("§a/db unlink &7- &eUnlinks a player."));
        sender.sendMessage(new TextComponent("§a/db update &7- &eForce a player rankupdate."));
    }
}
