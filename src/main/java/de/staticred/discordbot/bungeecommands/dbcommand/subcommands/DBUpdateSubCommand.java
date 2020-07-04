package de.staticred.discordbot.bungeecommands.dbcommand.subcommands;

import de.staticred.discordbot.Main;
import de.staticred.discordbot.util.SubCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.SQLException;

public class DBUpdateSubCommand extends SubCommand {

    public DBUpdateSubCommand(String name, CommandSender sender, String[] args) {
        super(name, sender, args);
    }

    @Override
    public void execute(String name, CommandSender sender, String[] args) {
        if(!sender.hasPermission("db.cmd.update")) {
            sender.sendMessage(new TextComponent(Main.getInstance().getStringFromConfig("NoPermissions",true)));
            return;
        }

        if(args.length != 2) {
            sender.sendMessage(new TextComponent("§cUse: /db update <player>"));
            return;
        }

        ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[1]);

        if(target == null) {
            sender.sendMessage(new TextComponent(Main.getInstance().getStringFromConfig("MCPlayerNotFound",true)));
            return;
        }

        try {
            Main.getInstance().updateRoles(Main.getInstance().getMemberFromPlayer(target.getUniqueId()),target);
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        sender.sendMessage(new TextComponent(Main.getInstance().getStringFromConfig("UpdatedRoles",true)));

    }
}
