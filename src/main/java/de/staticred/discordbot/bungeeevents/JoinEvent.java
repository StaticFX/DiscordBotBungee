package de.staticred.discordbot.bungeeevents;

import de.staticred.discordbot.Main;
import de.staticred.discordbot.db.VerifyDAO;
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
                Member m = Main.getInstance().getMemberFromPlayer(player);
                Main.INSTANCE.updateRoles(m,player);
                if(Main.INSTANCE.syncNickname) m.getGuild().modifyNickname(m, player.getName()).queue();
            }

        } catch(
                SQLException ex)

        {
            ex.printStackTrace();
        }
    }






}
