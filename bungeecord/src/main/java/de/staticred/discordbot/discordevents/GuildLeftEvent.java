package de.staticred.discordbot.discordevents;

import de.staticred.discordbot.db.VerifyDAO;
import de.staticred.discordbot.util.Debugger;
import de.staticred.discordbot.util.manager.RewardManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.sql.SQLException;

public class GuildLeftEvent extends ListenerAdapter {

    public void onGuildMemberRemove(GuildMemberRemoveEvent e) {
        Debugger.debugMessage("Member " + e.getMember().getEffectiveName() + " left the server.");
        Member m = e.getMember();
        try {
            RewardManager.executeVerifyUnlinkProcessBungeeCord(VerifyDAO.INSTANCE.getName(m.getId()));
            VerifyDAO.INSTANCE.setPlayerAsUnverified(m.getId());
            VerifyDAO.INSTANCE.removeDiscordIDByDiscordID(m);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
