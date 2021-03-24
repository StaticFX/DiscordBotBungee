package de.staticred.discordbot.bungeecommands.dbcommand.subcommands;

import de.staticred.discordbot.DBVerifier;
import de.staticred.discordbot.db.RewardsDAO;
import de.staticred.discordbot.db.VerifyDAO;
import de.staticred.discordbot.files.VerifyFileManager;
import de.staticred.discordbot.util.SubCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;

import java.sql.SQLException;
import java.util.Set;
import java.util.UUID;

/**
 * @author Devin Fritz
 * @version 1.0.0
 */
public class DBPortSubCommand extends SubCommand {

    public DBPortSubCommand(String name, CommandSender sender, String[] args) {
        super(name, sender, args);
    }

    @Override
    public void execute(String name, CommandSender sender, String[] args) {
        if(!sender.hasPermission("db.cmd.portto") && !sender.hasPermission("db.*")) {
            sender.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("NoPermissions",true)));
            return;
        }

        if (args.length != 2) {
            sender.sendMessage(new TextComponent("§cUse: /db portTo file/db"));
            return;
        }

        String portTo = args[1];

        if (portTo.equalsIgnoreCase("file")) {

            if (!DBVerifier.getInstance().useSQL) {
                sender.sendMessage(new TextComponent("§cCant port to file when DB is turned off"));
                return;
            }

            try {
                if (!VerifyDAO.INSTANCE.hasUsersInDataBase()) {
                    sender.sendMessage(new TextComponent("§cDB is empty"));
                    return;
                }

                Set<UUID> users = VerifyDAO.INSTANCE.getAllUsers();

                for (UUID user : users) {

                    if (!VerifyFileManager.INSTANCE.isPlayerInFile(user))
                        VerifyFileManager.INSTANCE.addPlayerAsUnverified(user);

                    if (VerifyDAO.INSTANCE.isPlayerVerified(user)) {
                        VerifyFileManager.INSTANCE.setVerifiedState(VerifyDAO.INSTANCE.getDiscordID(user), true);
                    }
                    VerifyFileManager.INSTANCE.setRewardState(user, RewardsDAO.INSTANCE.hasPlayerBeenRewarded(user));
                    VerifyFileManager.INSTANCE.updateUserName(user, VerifyDAO.INSTANCE.getName(VerifyDAO.INSTANCE.getDiscordID(user)));
                }
            } catch (SQLException exception) {
                exception.printStackTrace();
                return;
            }
            sender.sendMessage(new TextComponent("§aDone"));
            return;
        } else if (args[1].equalsIgnoreCase("db")) {
            if (!DBVerifier.getInstance().useSQL) {
                sender.sendMessage(new TextComponent("§cCant port to DB when DB is turned off"));
                return;
            }

            try {
                Set<UUID> users = VerifyFileManager.INSTANCE.getAllUsers();

                for (UUID user : users) {
                    if (!VerifyDAO.INSTANCE.isPlayerInDataBase(user)) {
                        VerifyDAO.INSTANCE.addPlayerAsUnverified(user);
                    }

                    if (VerifyFileManager.INSTANCE.isPlayerVerified(user)) {
                        VerifyDAO.INSTANCE.setPlayerAsVerified(user);
                        VerifyDAO.INSTANCE.addDiscordID(user, VerifyFileManager.INSTANCE.getDiscordID(user));
                    }
                    RewardsDAO.INSTANCE.setPlayerRewardState(user, VerifyFileManager.INSTANCE.getRewardState(user));
                    VerifyDAO.INSTANCE.updateUserName(user, VerifyDAO.INSTANCE.getName(VerifyDAO.INSTANCE.getDiscordID(user)));
                }
            } catch (SQLException exception) {
                exception.printStackTrace();
                return;
            }
            sender.sendMessage(new TextComponent("§aDone"));
            return;
        }

        sender.sendMessage(new TextComponent("§cUse: /db portTo file/db"));
    }
}
