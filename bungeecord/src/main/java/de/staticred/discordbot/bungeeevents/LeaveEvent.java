package de.staticred.discordbot.bungeeevents;

import de.staticred.discordbot.db.VerifyDAO;
import de.staticred.discordbot.files.SettingsFileManager;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.sql.SQLException;

public class LeaveEvent implements Listener {

    @EventHandler
    public void onLeave(PlayerDisconnectEvent e) {

        ProxiedPlayer p = e.getPlayer();


        if(!SettingsFileManager.INSTANCE.isSetup()) return;

        try {
            if(!VerifyDAO.INSTANCE.isPlayerInDataBase(p)) {
                VerifyDAO.INSTANCE.addPlayerAsUnverified(p);
            }
            VerifyDAO.INSTANCE.updateUserName(p);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

    }

}
