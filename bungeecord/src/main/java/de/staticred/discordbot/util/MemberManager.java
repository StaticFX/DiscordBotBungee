package de.staticred.discordbot.util;

import de.staticred.discordbot.DBVerifier;
import de.staticred.discordbot.db.VerifyDAO;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.sql.SQLException;
import java.util.UUID;

public class MemberManager {

    public static Member getMemberFromPlayer(UUID uuid) throws SQLException {
        User u;

        String id = VerifyDAO.INSTANCE.getDiscordID(uuid);

        u = DBVerifier.getInstance().jda.retrieveUserById(id).complete();
        Member m = null;
        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage(id);

        if (!DBVerifier.getInstance().jda.getGuilds().isEmpty()) {
            for (Guild guild : DBVerifier.getInstance().jda.getGuilds()) {
                if (u != null)
                    m = guild.retrieveMember(u).complete();
            }
        } else {
            throw new SQLException("There was an internal error! The member of the player can´t be found. Please contact the developer of this plugin.") ;
        }
        return m;
    }

}
