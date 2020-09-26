package de.staticred.discordbot.bungeeevents;

import de.staticred.discordbot.DBVerifier;
import de.staticred.discordbot.db.RewardsDAO;
import de.staticred.discordbot.db.VerifyDAO;
import de.staticred.discordbot.files.ConfigFileManager;
import de.staticred.discordbot.files.DiscordMessageFileManager;
import de.staticred.discordbot.files.MessagesFileManager;
import de.staticred.discordbot.files.SettingsFileManager;
import de.staticred.discordbot.util.Debugger;
import de.staticred.discordbot.util.MemberManager;
import net.dv8tion.jda.api.entities.Member;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.sql.SQLException;
import java.util.UUID;

public class JoinEvent implements Listener {


    @EventHandler(priority = 0)
    public void onJoin(LoginEvent e) {

        if(e.isCancelled() && ConfigFileManager.INSTANCE.cancelJoinEvent()) {
            return;
        }

        if(!SettingsFileManager.INSTANCE.isSetup()) {
            Debugger.debugMessage("Plugin not setup yet, not adding players to verify.");
            return;
        }

        UUID uuid = e.getConnection().getUniqueId();

        try {

            if (!VerifyDAO.INSTANCE.isPlayerInDataBase(uuid)) {
                VerifyDAO.INSTANCE.addPlayerAsUnverified(uuid);
            }

            if(!RewardsDAO.INSTANCE.isPlayerInTable(uuid)) {
                RewardsDAO.INSTANCE.addPlayerAsUnRewarded(uuid);
            }

        } catch(SQLException ex) {
            ex.printStackTrace();
        }
    }
}
