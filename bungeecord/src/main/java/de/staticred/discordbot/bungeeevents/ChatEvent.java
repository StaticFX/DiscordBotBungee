package de.staticred.discordbot.bungeeevents;

import de.staticred.discordbot.DBVerifier;
import de.staticred.discordbot.files.DiscordFileManager;
import de.staticred.discordbot.util.GroupInfo;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ChatEvent implements Listener {

    @EventHandler
    public void onChatMessage(net.md_5.bungee.api.event.ChatEvent event) {

        ProxiedPlayer player = (ProxiedPlayer) event.getSender();
        String message = event.getMessage();

        if(DBVerifier.getInstance().playerCreatingGroupStateHashMap.containsKey(player.getUniqueId())) {
            event.setCancelled(true);
            if(message.equalsIgnoreCase("cancel")) {
                player.sendMessage(new TextComponent("§aYou canceled the process!"));
                DBVerifier.getInstance().playerCreatingGroupStateHashMap.remove(player.getUniqueId());
                DBVerifier.getInstance().playerGroupInfoHashMap.remove(player.getUniqueId());
                return;
            }

            int state = DBVerifier.getInstance().playerCreatingGroupStateHashMap.get(player.getUniqueId());

            switch (state) {
                case 0: {
                    String permissions = message;
                    player.sendMessage(new TextComponent("§aYou successfully set the name. §aPlease type now the prefix §afor the group"));
                    GroupInfo info = DBVerifier.getInstance().playerGroupInfoHashMap.get(player.getUniqueId());
                    info.setPermission(permissions);
                    DBVerifier.getInstance().playerGroupInfoHashMap.replace(player.getUniqueId(),info);
                    DBVerifier.getInstance().playerCreatingGroupStateHashMap.replace(player.getUniqueId(),1);
                    return;
                }
                case 1: {
                    String prefix = message;
                    player.sendMessage(new TextComponent("§aYou successfully set the name. §aPlease type now if the group §ashould be dynamic or not. §e(True/False)"));
                    GroupInfo info = DBVerifier.getInstance().playerGroupInfoHashMap.get(player.getUniqueId());
                    info.setPrefix(prefix);
                    DBVerifier.getInstance().playerGroupInfoHashMap.replace(player.getUniqueId(),info);
                    DBVerifier.getInstance().playerCreatingGroupStateHashMap.replace(player.getUniqueId(),2);
                    return;
                }
                case 2: {
                    boolean bool;
                    try {
                        bool = Boolean.getBoolean(message);
                    }catch (Exception e) {
                        player.sendMessage(new TextComponent("§cThe given value is not a boolean!"));
                        return;
                    }

                    GroupInfo info = DBVerifier.getInstance().playerGroupInfoHashMap.get(player.getUniqueId());
                    player.sendMessage(new TextComponent("§aYou successfully set the dynamic value. §aPlease type now how §athe group is called on the discord (Role)"));
                    info.setDynamic(bool);
                    DBVerifier.getInstance().playerGroupInfoHashMap.replace(player.getUniqueId(),info);
                    DBVerifier.getInstance().playerCreatingGroupStateHashMap.replace(player.getUniqueId(),3);
                    return;
                }
                case 3: {
                    String role = message;
                    GroupInfo info = DBVerifier.getInstance().playerGroupInfoHashMap.get(player.getUniqueId());
                    player.sendMessage(new TextComponent("§aYou successfully set the role. §aThe setup is done and the §agroup will be created."));
                    info.setDiscordGroup(role);
                    DiscordFileManager.INSTANCE.createGroup(info);
                    DBVerifier.getInstance().playerGroupInfoHashMap.remove(player.getUniqueId(),info);
                    DBVerifier.getInstance().playerCreatingGroupStateHashMap.remove(player.getUniqueId(),3);
                    return;
                }
            }



        }



    }

}
