package de.staticred.discordbot.bungeecommands.dbcommand.subcommands;

import de.staticred.discordbot.DBVerifier;
import de.staticred.discordbot.db.VerifyDAO;
import de.staticred.discordbot.util.MemberManager;
import de.staticred.discordbot.util.SubCommand;
import de.staticred.discordbot.util.UUIDFetcher;
import net.dv8tion.jda.api.entities.Member;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.SQLException;
import java.util.UUID;

public class DBInfoSubCommand extends SubCommand {

    public DBInfoSubCommand(String name, CommandSender sender, String[] args) {
        super(name, sender, args);
    }

    @Override
    public void execute(String name, CommandSender sender, String[] args) {
        if(!sender.hasPermission("db.cmd.info") && !sender.hasPermission("db.*")) {
            sender.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("NoPermissions",true)));
            return;
        }

        if(args.length != 2) {
            sender.sendMessage(new TextComponent("§cUse: /db info <player>"));
            return;
        }
        String inputName = args[1];

        UUID uuid = null;
        try {
            uuid = VerifyDAO.INSTANCE.getUUIDByName(inputName);
        } catch (SQLException e) {
            e.printStackTrace();
            return;
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

        sender.sendMessage(new TextComponent("§aInformation about: §e§l" + inputName));
        try {
            sender.sendMessage(new TextComponent("§aDiscordID §7-> §e" + VerifyDAO.INSTANCE.getDiscordID(uuid)));
            Member member = MemberManager.getMemberFromPlayer(uuid);
            sender.sendMessage(new TextComponent("§aDiscordName §7-> §e" + member.getUser().getAsTag()));
            sender.sendMessage(new TextComponent("§aOnline §7-> §e" + member.getOnlineStatus().toString()));
            sender.sendMessage(new TextComponent("§aRoles §7-> §e" + member.getRoles().toString()));
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }
}
