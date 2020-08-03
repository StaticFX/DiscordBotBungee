package de.staticred.discordbot.bungeecommands.dbcommand.subcommands;

import de.staticred.discordbot.DBVerifier;
import de.staticred.discordbot.db.VerifyDAO;
import de.staticred.discordbot.util.SubCommand;
import de.staticred.discordbot.util.UUIDFetcher;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;

import java.sql.SQLException;
import java.util.UUID;

public class DBResetSubCommand extends SubCommand {

    public DBResetSubCommand(String name, CommandSender sender, String[] args) {
        super(name, sender, args);
    }

    @Override
    public void execute(String name, CommandSender sender, String[] args) {
        if(!sender.hasPermission("db.cmd.reset") && !sender.hasPermission("db.*")) {
            sender.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("NoPermissions",true)));
            return;
        }

        if(args.length != 2) {
            sender.sendMessage(new TextComponent("§cUse: /db reset <player>"));
            return;
        }

        String inputName = args[1];

        UUID uuid = UUIDFetcher.getUUID(inputName);

        if(uuid == null) {
            sender.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("UserNotFound",true)));
            return;
        }

        try {
            VerifyDAO.INSTANCE.removePlayerData(uuid);
        } catch (SQLException e) {
            e.printStackTrace();
            sender.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("InternalError",true)));
            return;
        }

        sender.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("ResetPlayer", true)));
    }
}
