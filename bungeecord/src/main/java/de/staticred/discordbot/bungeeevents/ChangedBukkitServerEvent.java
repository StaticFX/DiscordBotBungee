package de.staticred.discordbot.bungeeevents;

import de.staticred.discordbot.DBVerifier;
import de.staticred.discordbot.db.VerifyDAO;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public class ChangedBukkitServerEvent implements Listener {

    @EventHandler
    public void onServerChange(ServerSwitchEvent event) {

        ProxiedPlayer switcher = event.getPlayer();

        DBVerifier.getInstance().bukkitMessageHandler.sendPlayerVerifedRequest(switcher);

        ProxyServer.getInstance().getScheduler().schedule(DBVerifier.getInstance(),() -> {

            try {
                if(VerifyDAO.INSTANCE.isPlayerVerified(switcher.getUniqueId()) && !DBVerifier.getInstance().playerSRVVerifiedHashMap.get(switcher.getUniqueId())) {
                    DBVerifier.getInstance().bukkitMessageHandler.sendPlayerVerified(switcher,VerifyDAO.INSTANCE.getDiscordID(switcher.getUniqueId()));
                }
                if(!VerifyDAO.INSTANCE.isPlayerVerified(switcher.getUniqueId()) && DBVerifier.getInstance().playerSRVVerifiedHashMap.get(switcher.getUniqueId())) {
                    DBVerifier.getInstance().bukkitMessageHandler.sendPlayerUnlinked(switcher,VerifyDAO.INSTANCE.getDiscordID(switcher.getUniqueId()));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }, 2, TimeUnit.SECONDS);

    }

}
