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
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.sql.SQLException;

public class JoinEvent implements Listener {


    @EventHandler
    public void onJoin(PostLoginEvent e) {

        ProxiedPlayer player = e.getPlayer();

        if(!DBVerifier.configVersion.equals(ConfigFileManager.INSTANCE.getConfigVersion())) {
            player.sendMessage(new TextComponent("§8[§aDiscordBot§8] §4Your config version is not compatible with the §4plugin-config version."));
            player.sendMessage(new TextComponent("§8[§aDiscordBot§8] §4Config version: §l" + ConfigFileManager.INSTANCE.getConfigVersion()));
            player.sendMessage(new TextComponent("§8[§aDiscordBot§8] §4Plugin version: §l" + DBVerifier.configVersion));
            player.sendMessage(new TextComponent("§8[§aDiscordBot§8] §4It may come to errors."));

            TextComponent tc = new TextComponent();
            tc.setText("§8[§aDiscordBot§8] §4You can find the newest version over §e§lhere");
            tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§aconfig.yml").create()));
            tc.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/StaticFX/DiscordBotBungee/blob/master/src/main/resources/config.yml"));
            TextComponent sendedText = new TextComponent();
            sendedText.addExtra(tc);
            player.sendMessage(sendedText);

        }

        if(!SettingsFileManager.INSTANCE.isSetup()  && player.hasPermission("discord.setup") || player.hasPermission("db.*")) {
            player.sendMessage(new TextComponent("§8[§aDiscordBot§8] §aHey, looks like my plugin isn´t setup yet. \n§aMy wizard will guide you trough the process."));
            player.sendMessage(new TextComponent("§aUse /setup to start now!"));
        }

        if(!DBVerifier.msgVersion.equals(MessagesFileManager.getInstance().getVersion())) {
            player.sendMessage(new TextComponent("§8[§aDiscordBot§8] §4Your message version is not compatible with the §4plugin-config version."));
            player.sendMessage(new TextComponent("§8[§aDiscordBot§8] §4Config version: §l" + MessagesFileManager.INSTANCE.getVersion()));
            player.sendMessage(new TextComponent("§8[§aDiscordBot§8] §4Plugin version: §l" + DBVerifier.msgVersion));


            TextComponent tc = new TextComponent();
            tc.setText("§8[§aDiscordBot§8] §4You can find the newest version over §e§lhere");
            tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§aminecraftMessages.yml").create()));
            tc.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/StaticFX/DiscordBotBungee/blob/master/src/main/resources/messages.yml"));
            TextComponent sendedText = new TextComponent();
            sendedText.addExtra(tc);
            player.sendMessage(sendedText);

            player.sendMessage(new TextComponent("§8[§aDiscordBot§8] §4It may come to errors."));
        }

        if(!DBVerifier.dcMSGVersion.equals(DiscordMessageFileManager.INSTANCE.getVersion())) {
            player.sendMessage(new TextComponent("§8[§aDiscordBot§8] §4Your message version is not compatible with the §4plugin-config version."));
            player.sendMessage(new TextComponent("§8[§aDiscordBot§8] §4Config version: §l" + MessagesFileManager.INSTANCE.getVersion()));
            player.sendMessage(new TextComponent("§8[§aDiscordBot§8] §4Plugin version: §l" + DBVerifier.dcMSGVersion));


            TextComponent tc = new TextComponent();
            tc.setText("§8[§aDiscordBot§8] §4You can find the newest version over §e§lhere");
            tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§adiscordMessages.yml").create()));
            tc.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/StaticFX/DiscordBotBungee/blob/master/src/main/resources/messages.yml"));
            TextComponent sendedText = new TextComponent();
            sendedText.addExtra(tc);
            player.sendMessage(sendedText);

            player.sendMessage(new TextComponent("§8[§aDiscordBot§8] §4It may come to errors."));
        }

        if(!SettingsFileManager.INSTANCE.isSetup()) {
            Debugger.debugMessage("Plugin not setup yet, not adding players to verify.");
            return;
        }

        try {

            if (!VerifyDAO.INSTANCE.isPlayerInDataBase(player)) {
                VerifyDAO.INSTANCE.addPlayerAsUnverified(player);
            }

            boolean verified = VerifyDAO.INSTANCE.isPlayerVerified(player.getUniqueId());

            if (!verified) {
                TextComponent tc = new TextComponent();
                tc.setText(DBVerifier.getInstance().getStringFromConfig("DiscordLinkColored",false));
                tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(DBVerifier.getInstance().getStringFromConfig("JoinOurDiscord",false)).create()));
                tc.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, DBVerifier.getInstance().getStringFromConfig("DiscordLinkRaw",false)));
                TextComponent sendedText = new TextComponent(DBVerifier.getInstance().getStringFromConfig("JoinOurDiscordMainText",true));
                sendedText.addExtra(tc);
                player.sendMessage(sendedText);
            }

            if(!RewardsDAO.INSTANCE.isPlayerInTable(player.getUniqueId())) {
                RewardsDAO.INSTANCE.addPlayerAsUnRewarded(player.getUniqueId());
            }

            VerifyDAO.INSTANCE.updateUserName(player);

            DBVerifier.getInstance().playerSRVVerifiedHashMap.put(player.getUniqueId(), verified);

            if(verified) {

                Member m;

                try {
                    m = MemberManager.getMemberFromPlayer(player.getUniqueId());
                } catch (SQLException ex) {
                    player.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("InternalError",true)));
                    ex.printStackTrace();
                    return;
                }
                if (m == null) {
                    player.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("InternalError",true)));
                    return;
                }

                DBVerifier.getInstance().removeAllRolesFromMember(m);
                DBVerifier.getInstance().updateRoles(m,player);

                if(DBVerifier.INSTANCE.syncNickname) {
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
