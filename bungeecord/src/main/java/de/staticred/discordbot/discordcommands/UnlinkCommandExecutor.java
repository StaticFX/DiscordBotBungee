package de.staticred.discordbot.discordcommands;

import de.staticred.discordbot.DBVerifier;
import de.staticred.discordbot.db.VerifyDAO;
import de.staticred.discordbot.util.Debugger;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.awt.*;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class UnlinkCommandExecutor {

    public UnlinkCommandExecutor(Member m , TextChannel tc, Message command, String args[]) {
        execute(m,tc,command,args);
    }

    private void execute(Member m, TextChannel tc, Message command, String[] args) {

        EmbedBuilder embedBuilder = new EmbedBuilder();

        if(args.length != 1) {
            embedBuilder.setDescription(DBVerifier.getInstance().getStringFromConfig("UnlinkDiscordSyntax",false) + m.getAsMention());
            embedBuilder.setColor(Color.red);
            tc.sendMessage(embedBuilder.build()).queue(msg -> msg.delete().queueAfter(10, TimeUnit.SECONDS));
            command.delete().queueAfter(10,TimeUnit.SECONDS);
            return;
        }


        try {
            if(!VerifyDAO.INSTANCE.isDiscordIDInUse(m.getId())) {
                embedBuilder.setDescription(DBVerifier.getInstance().getStringFromConfig("NotVerifiedYet",false) + m.getAsMention());
                embedBuilder.setColor(Color.red);
                tc.sendMessage(embedBuilder.build()).queue(msg -> msg.delete().queueAfter(10,TimeUnit.SECONDS));
                command.delete().queueAfter(10,TimeUnit.SECONDS);
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }



        DBVerifier.INSTANCE.removeAllRolesFromMember(m);


        UUID uuid;

        try {
            uuid = UUID.fromString(VerifyDAO.INSTANCE.getUUIDByDiscordID(m.getId()));
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);

        if(player != null) {
            if(DBVerifier.getInstance().useSRV) {
                DBVerifier.getInstance().bukkitMessageHandler.sendPlayerUnlinked(player,m.getId());
            }
        }else{
            Debugger.debugMessage("A member unlinked himself from a account which is not present on the network. The player will get unlinked on the next reconnect.");
        }



        try {
            VerifyDAO.INSTANCE.setPlayerAsUnverified(m.getId());
            VerifyDAO.INSTANCE.removeDiscordIDByDiscordID(m);
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        embedBuilder.setDescription(DBVerifier.getInstance().getStringFromConfig("SuccessfullyUnlinked",false) + m.getAsMention());
        embedBuilder.setColor(Color.green);
        tc.sendMessage(embedBuilder.build()).queue(msg -> msg.delete().queueAfter(10, TimeUnit.SECONDS));
        command.delete().queueAfter(10,TimeUnit.SECONDS);

    }

}
