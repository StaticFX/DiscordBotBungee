package de.staticred.discordbot.discordcommands;

import de.staticred.discordbot.DBVerifier;
import de.staticred.discordbot.db.VerifyDAO;
import de.staticred.discordbot.files.ConfigFileManager;
import de.staticred.discordbot.files.DiscordMessageFileManager;
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
            int time = ConfigFileManager.INSTANCE.getTime();


            if(time != -1) {
                tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbed("UnlinkDiscordSyntax")).queue(msg -> msg.delete().queueAfter(time, TimeUnit.SECONDS));
                command.delete().queueAfter(time,TimeUnit.SECONDS);
            }else {
                tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbed("UnlinkDiscordSyntax")).queue();
            }

            return;
        }


        try {
            if(!VerifyDAO.INSTANCE.isDiscordIDInUse(m.getId())) {
                int time = ConfigFileManager.INSTANCE.getTime();

                if(time != -1) {
                    tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbed("NotVerifiedYet")).queue(msg -> msg.delete().queueAfter(time, TimeUnit.SECONDS));
                    command.delete().queueAfter(time,TimeUnit.SECONDS);
                }else {
                    tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbed("NotVerifiedYet")).queue();
                }

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
            DBVerifier.getInstance().removeAllRolesFromMember(m);
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        int time = ConfigFileManager.INSTANCE.getTime();

        if(time != -1) {
            tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbed("SuccessfullyUnlinked")).queue(msg -> msg.delete().queueAfter(time, TimeUnit.SECONDS));
            command.delete().queueAfter(time,TimeUnit.SECONDS);
        }else {
            tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbed("SuccessfullyUnlinked")).queue();
        }
    }

}
