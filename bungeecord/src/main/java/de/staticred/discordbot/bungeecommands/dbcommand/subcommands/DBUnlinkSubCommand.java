package de.staticred.discordbot.bungeecommands.dbcommand.subcommands;

import de.staticred.discordbot.DBVerifier;
import de.staticred.discordbot.db.VerifyDAO;
import de.staticred.discordbot.util.MemberManager;
import de.staticred.discordbot.util.SubCommand;
import de.staticred.discordbot.util.UUIDFetcher;
import de.staticred.discordbot.util.manager.RewardManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.SQLException;
import java.util.UUID;

public class DBUnlinkSubCommand extends SubCommand {

    public DBUnlinkSubCommand(String name, CommandSender sender, String[] args) {
        super(name, sender, args);
    }

    @Override
    public void execute(String name, CommandSender sender, String[] args) {
        if(!sender.hasPermission("db.cmd.unlink") && !sender.hasPermission("db.*")) {
            sender.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("NoPermissions",true)));
            return;
        }

        if(args.length != 2) {
            sender.sendMessage(new TextComponent("§cUse: /db unlink <player>"));
            return;
        }

        String inputName = args[1];

        UUID uuid = null;
        try {
            uuid = VerifyDAO.INSTANCE.getUUIDByName(inputName);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if(uuid == null) {
            sender.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("UserNotFound",true)));
            return;
        }

        try {
            if(!VerifyDAO.INSTANCE.isPlayerVerified(uuid)) {
                sender.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("UserNotVerifiedYet",true)));
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        ProxiedPlayer target = ProxyServer.getInstance().getPlayer(uuid);

        if(target != null) RewardManager.executeVerifyUnlinkProcess(target);

        try {
            DBVerifier.getInstance().removeAllRolesFromMember(MemberManager.getMemberFromPlayer(uuid));
            VerifyDAO.INSTANCE.setPlayerAsUnVerified(uuid);
            VerifyDAO.INSTANCE.removeDiscordID(uuid);
            sender.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("UnlinkedPlayer",true)));
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

    }
}
