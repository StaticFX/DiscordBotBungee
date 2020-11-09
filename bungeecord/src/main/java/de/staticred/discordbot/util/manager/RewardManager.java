package de.staticred.discordbot.util.manager;

import de.staticred.discordbot.DBVerifier;
import de.staticred.discordbot.db.RewardsDAO;
import de.staticred.discordbot.db.VerifyDAO;
import de.staticred.discordbot.files.ConfigFileManager;
import de.staticred.discordbot.files.RewardsFileManager;
import de.staticred.discordbot.util.Debugger;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.SQLException;

public class RewardManager {


    public static void executeVerifyRewardProcess(ProxiedPlayer p) {
        try {
            if(!RewardsDAO.INSTANCE.hasPlayerBeenRewarded(p.getUniqueId())  || ConfigFileManager.INSTANCE.igrnoreRewardState()) {
                for(String command2 : RewardsFileManager.INSTANCE.getCommandsOnVerifiedBungee()) {
                    if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Execute bungeecord cmd: " + command2);
                    ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), command2.replace("%player%",p.getName()));
                }

                for(String command2 : RewardsFileManager.INSTANCE.getCommandsOnVerifiedBukkit()) {
                    DBVerifier.getInstance().bukkitMessageHandler.sendCommand(p,command2);
                    if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Execute bukkit cmd: " + command2);
                }

                if(!ConfigFileManager.INSTANCE.igrnoreRewardState())
                    RewardsDAO.INSTANCE.setPlayerRewardState(p.getUniqueId(),true);
            }
        } catch (
                SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    public static void executeVerifyUnlinkProcess(ProxiedPlayer p) {
        try {
            if(!RewardsDAO.INSTANCE.hasPlayerBeenRewarded(p.getUniqueId()) || ConfigFileManager.INSTANCE.igrnoreRewardState()) {
                for(String command2 : RewardsFileManager.INSTANCE.getCommandsOnUnVerifiedBungee()) {
                    if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Execute bungeecord cmd: " + command2);
                    ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), command2.replace("%player%",p.getName()));
                }

                for(String command2 : RewardsFileManager.INSTANCE.getCommandsOnUnVerifiedBukkit()) {
                    DBVerifier.getInstance().bukkitMessageHandler.sendCommand(p,command2);
                    if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Execute bukkit cmd: " + command2);
                }

                if(!ConfigFileManager.INSTANCE.igrnoreRewardState())
                    RewardsDAO.INSTANCE.setPlayerRewardState(p.getUniqueId(),true);
            }
        } catch (
                SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    public static void executeVerifyUnlinkProcessBungeeCord(String name) {
        try {

            if(!RewardsDAO.INSTANCE.hasPlayerBeenRewarded(VerifyDAO.INSTANCE.getUUIDByName(name)) || ConfigFileManager.INSTANCE.igrnoreRewardState()) {
                for(String command2 : RewardsFileManager.INSTANCE.getCommandsOnUnVerifiedBungee()) {
                    if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Execute bungeecord cmd: " + command2);
                    ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), command2.replace("%player%",name));
                }

                if(!ConfigFileManager.INSTANCE.igrnoreRewardState())
                    RewardsDAO.INSTANCE.setPlayerRewardState(VerifyDAO.INSTANCE.getUUIDByName(name), true);
            }
        } catch (
                SQLException throwables) {
            throwables.printStackTrace();
        }

    }


}
