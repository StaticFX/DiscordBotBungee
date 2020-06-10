package de.staticred.discordbot.util;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class Debugger {

    public static void debugMessage(String string) {
        System.out.println("[VerifyBot] [DEBUG] " + string);
        for(ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            if(player.hasPermission("db.debug")) {
                player.sendMessage(new TextComponent("§8[§aDEBUG§8] §r" + string));
            }
        }
    }

}
