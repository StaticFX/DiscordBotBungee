package de.staticred.discordbot.bungeeevents;

import de.staticred.discordbot.Main;
import de.staticred.discordbot.db.VerifyDAO;
import de.staticred.discordbot.files.ConfigFileManager;
import de.staticred.discordbot.files.VerifyFileManager;
import net.dv8tion.jda.api.entities.Member;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.sql.SQLException;

public class JoinEvent implements Listener {


    @EventHandler
    public void onJoin(PostLoginEvent e) {

        ProxiedPlayer player = e.getPlayer();

        if(!Main.configVersion.equals(ConfigFileManager.INSTANCE.getConfigVersion())) {
            player.sendMessage(new TextComponent("§8[§aDiscordBot§8] §4Your config version is not compatible with your §4plugin-config version."));
            player.sendMessage(new TextComponent("§8[§aDiscordBot§8] §4Config version:" + ConfigFileManager.INSTANCE.getConfigVersion()));
            player.sendMessage(new TextComponent("§8[§aDiscordBot§8] §4Plugin version:" + Main.configVersion));
            player.sendMessage(new TextComponent("§8[§aDiscordBot§8] §4It may come to errors."));
        }

        if(!Main.setuped && player.hasPermission("discord.setup")) {
            player.sendMessage(new TextComponent("§8[§aDiscordBot§8] §aHey, looks like my plugin isn´t setup yet. \n§aMy wizard will guide you trough the process."));
            player.sendMessage(new TextComponent("§aUse /setup to start now!"));
        }

        if(!Main.setuped) return;

        try {

            if (!VerifyDAO.INSTANCE.isPlayerInDataBase(player)) {
                VerifyDAO.INSTANCE.addPlayerAsUnverified(player);
            }

            if (!VerifyDAO.INSTANCE.isPlayerVerified(player)) {
                TextComponent tc = new TextComponent();
                tc.setText(Main.getInstance().getStringFromConfig("DiscordLinkColored",false));
                tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Main.getInstance().getStringFromConfig("JoinOurDiscord",false)).create()));
                tc.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, Main.getInstance().getStringFromConfig("DiscordLinkRaw",false)));
                TextComponent sendedText = new TextComponent(Main.getInstance().getStringFromConfig("JoinOurDiscordMainText",true));
                sendedText.addExtra(tc);
                player.sendMessage(sendedText);
            }


            VerifyDAO.INSTANCE.updateUserName(player);
            VerifyDAO.INSTANCE.updateRank(player);

            if(VerifyDAO.INSTANCE.isPlayerVerified(player)) {

                Member m;

                try {
                    m = Main.INSTANCE.getMemberFromPlayer(player);
                } catch (SQLException ex) {
                    player.sendMessage(new TextComponent(Main.getInstance().getStringFromConfig("InternalError",true)));
                    ex.printStackTrace();
                    return;
                }
                if (m == null) {
                    player.sendMessage(new TextComponent(Main.getInstance().getStringFromConfig("InternalError",true)));
                    return;
                }

                Main.getInstance().removeAllRolesFromMember(m);
                Main.getInstance().updateRoles(m,player);

                if(Main.INSTANCE.syncNickname) {
                    if(!m.isOwner()) {
                        m.getGuild().modifyNickname(m, player.getName()).queue();
                    }
                }
            }

        } catch(SQLException ex) {
            ex.printStackTrace();
        }
    }
}
