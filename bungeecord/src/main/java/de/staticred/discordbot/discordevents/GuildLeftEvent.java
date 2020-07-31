package de.staticred.discordbot.discordevents;

import de.staticred.discordbot.db.VerifyDAO;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.sql.SQLException;

public class GuildLeftEvent extends ListenerAdapter {


    public void onGuildMemberLeave(GuildMemberLeaveEvent e) {
        Member m = e.getMember();
        try {
            VerifyDAO.INSTANCE.removeDiscordIDByDiscordID(m);
        } catch (
                SQLException ex) {
            ex.printStackTrace();
        }
    }
}
