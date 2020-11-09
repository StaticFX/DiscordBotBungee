package de.staticred.discordbot.util;

import de.staticred.discordbot.DBVerifier;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class Debugger {

    public static void debugMessage(String string) {
        if(!DBVerifier.getInstance().debugMode) return;

        System.out.println("[DBVerifier] [DEBUG] " + string);
        if(!DBVerifier.getInstance().debugMode) return;
        for(ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            if(player.hasPermission("db.debug") || player.hasPermission("db.*")) {
                player.sendMessage(new TextComponent("§8[§aDBVerifier§8] [§aDEBUG§8] §r" + string));
            }
        }
    }


    public static void debugMessage(Object string) {
        debugMessage(string.toString());
    }
}
