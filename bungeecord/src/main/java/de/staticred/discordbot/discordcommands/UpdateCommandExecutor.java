package de.staticred.discordbot.discordcommands;

import de.staticred.discordbot.DBVerifier;
import de.staticred.discordbot.db.VerifyDAO;
import de.staticred.discordbot.files.ConfigFileManager;
import de.staticred.discordbot.files.DiscordMessageFileManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.awt.*;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class UpdateCommandExecutor {

    public UpdateCommandExecutor(Member m, TextChannel tc, Message command, String args[]) {
        execute(m,tc,command,args);
    }

    private void execute(Member m, TextChannel tc, Message command, String[] args) {

        EmbedBuilder embedBuilder = new EmbedBuilder();
        if(args.length != 1) {
            int time = ConfigFileManager.INSTANCE.getTime();

            if(time != -1) {
                tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbed("UpdateDiscordSyntax")).queue(msg -> msg.delete().queueAfter(time, TimeUnit.SECONDS));
                command.delete().queueAfter(time,TimeUnit.SECONDS);
            }else {
                tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbed("UpdateDiscordSyntax")).queue();
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

        ProxiedPlayer target = null;
        try {
            target = DBVerifier.getInstance().getProxy().getPlayer(UUID.fromString(VerifyDAO.INSTANCE.getUUIDByDiscordID(m.getId())));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        int time = ConfigFileManager.INSTANCE.getTime();

        if(target == null) {
            if(time != -1) {
                tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbed("MustBeOnServer")).queue(msg -> msg.delete().queueAfter(time, TimeUnit.SECONDS));
                command.delete().queueAfter(time,TimeUnit.SECONDS);
            }else {
                tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbed("MustBeOnServer")).queue();
            }
            return;
        }


        DBVerifier.getInstance().removeAllRolesFromMember(m);
        DBVerifier.getInstance().updateRoles(m,target);

        if(DBVerifier.INSTANCE.syncNickname) {
            if(m.isOwner()) {

                if(time != -1) {
                    tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbed("NoInquiries")).queue(msg -> msg.delete().queueAfter(time, TimeUnit.SECONDS));
                    command.delete().queueAfter(time,TimeUnit.SECONDS);
                }else {
                    tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbed("NoInquiries")).queue();
                }
            }else {
                m.getGuild().modifyNickname(m,target.getName()).queue();
            }
        }


        if(time != -1) {
            tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbed("UpdatedRank")).queue(msg -> msg.delete().queueAfter(time, TimeUnit.SECONDS));
            command.delete().queueAfter(time,TimeUnit.SECONDS);
        }else {
            tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbed("UpdatedRank")).queue();
        }
    }
}
