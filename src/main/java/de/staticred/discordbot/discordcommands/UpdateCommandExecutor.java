package de.staticred.discordbot.discordcommands;

import de.staticred.discordbot.Main;
import de.staticred.discordbot.db.VerifyDAO;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.md_5.bungee.api.chat.TextComponent;
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
            embedBuilder.setDescription(Main.getInstance().getStringFromConfig("UpdateDiscordSyntax",false) + m.getAsMention());
            embedBuilder.setColor(Color.red);
            tc.sendMessage(embedBuilder.build()).queue(msg -> msg.delete().queueAfter(10, TimeUnit.SECONDS));
            command.delete().queueAfter(10,TimeUnit.SECONDS);
            return;
        }


        try {
            if(!VerifyDAO.INSTANCE.isDiscordIDInUse(m.getId())) {
                embedBuilder.setDescription(Main.getInstance().getStringFromConfig("NotVerifiedYet",false) + m.getAsMention());
                embedBuilder.setColor(Color.red);
                tc.sendMessage(embedBuilder.build()).queue(msg -> msg.delete().queueAfter(10, TimeUnit.SECONDS));
                command.delete().queueAfter(10,TimeUnit.SECONDS);
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        ProxiedPlayer target = null;
        try {
            target = Main.getInstance().getProxy().getPlayer(UUID.fromString(VerifyDAO.INSTANCE.getUUIDByDiscordID(m.getId())));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if(target == null) {
            embedBuilder.setDescription("Error. Please be on the server to update your rank!");
            embedBuilder.setColor(Color.green);
            tc.sendMessage(embedBuilder.build()).queue(msg -> msg.delete().queueAfter(10, TimeUnit.SECONDS));
            command.delete().queueAfter(10,TimeUnit.SECONDS);
            return;
        }


        Main.getInstance().removeAllRolesFromMember(m);
        Main.getInstance().updateRoles(m,target);

        if(Main.INSTANCE.syncNickname) {
            if(m.isOwner()) {
                embedBuilder.setDescription(Main.getInstance().getStringFromConfig("NoInquiries",false));
                embedBuilder.setColor(Color.RED);
                tc.sendMessage(embedBuilder.build()).queue(msg -> msg.delete().queueAfter(10, TimeUnit.SECONDS));
                command.delete().queueAfter(10,TimeUnit.SECONDS);
            }else {
                m.getGuild().modifyNickname(m,target.getName()).queue();
            }
        }


        embedBuilder.setDescription(Main.getInstance().getStringFromConfig("UpdatedRank",false) + m.getAsMention());
        embedBuilder.setColor(Color.green);
        tc.sendMessage(embedBuilder.build()).queue(msg -> msg.delete().queueAfter(10, TimeUnit.SECONDS));
        command.delete().queueAfter(10,TimeUnit.SECONDS);
        return;
    }
}
