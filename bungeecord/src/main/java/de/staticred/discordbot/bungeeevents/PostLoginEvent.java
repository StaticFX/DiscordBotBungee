package de.staticred.discordbot.bungeeevents;

import de.staticred.discordbot.DBVerifier;
import de.staticred.discordbot.db.VerifyDAO;
import de.staticred.discordbot.files.ConfigFileManager;
import de.staticred.discordbot.files.DiscordMessageFileManager;
import de.staticred.discordbot.files.MessagesFileManager;
import de.staticred.discordbot.files.SettingsFileManager;
import de.staticred.discordbot.util.Debugger;
import de.staticred.discordbot.util.MemberManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.sql.SQLException;

public class PostLoginEvent implements Listener {

    @EventHandler
    public void onJoin(net.md_5.bungee.api.event.PostLoginEvent event) {

        ProxiedPlayer player = event.getPlayer();


        if(event.getPlayer().hasPermission("db.debug")) {
            if(!ConfigFileManager.INSTANCE.disableUpdateChecker()) {
                if(DBVerifier.getInstance().isUpdateAvailable)
                    player.sendMessage(new TextComponent("§8[§bDBVerifier§8] There is a new version available on the spigot.org\n§ahttps://www.spigotmc.org/resources/dbverifier-bungeecord-discord-verify-plugin.72232/"));
            }
        }

        if(!DBVerifier.configVersion.equals(ConfigFileManager.INSTANCE.getConfigVersion())) {
            player.sendMessage(new TextComponent("§8[§aDiscordBot§8] §4Your config.yml version is not compatible with the §4plugin-config version."));
            player.sendMessage(new TextComponent("§8[§aDiscordBot§8] §4Config version: §l" + ConfigFileManager.INSTANCE.getConfigVersion()));
            player.sendMessage(new TextComponent("§8[§aDiscordBot§8] §4Plugin version: §l" + DBVerifier.configVersion));
            player.sendMessage(new TextComponent("§8[§aDiscordBot§8] §4It may come to errors."));

            TextComponent tc = new TextComponent();
            tc.setText("§8[§aDiscordBot§8] §4You can find the newest version over §e§lhere");
            tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§aconfig.yml").create()));
            tc.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/StaticFX/DiscordBotBungee/blob/1.5.0/bungeecord/src/main/resources/config.yml"));
            TextComponent sendedText = new TextComponent();
            sendedText.addExtra(tc);
            player.sendMessage(sendedText);

        }

        if(!SettingsFileManager.INSTANCE.isSetup()  && (player.hasPermission("discord.setup") || player.hasPermission("db.*"))) {
            player.sendMessage(new TextComponent("§8[§aDiscordBot§8] §aHey, looks like my plugin isn´t setup yet. \n§aMy wizard will guide you trough the process."));
            player.sendMessage(new TextComponent("§aUse /setup to start now!"));
        }

        if(!DBVerifier.msgVersion.equals(MessagesFileManager.getInstance().getVersion())) {
            player.sendMessage(new TextComponent("§8[§aDiscordBot§8] §4Your minecraftmessages.yml version is not compatible with the §4plugin-config version."));
            player.sendMessage(new TextComponent("§8[§aDiscordBot§8] §4Config version: §l" + MessagesFileManager.INSTANCE.getVersion()));
            player.sendMessage(new TextComponent("§8[§aDiscordBot§8] §4Plugin version: §l" + DBVerifier.msgVersion));


            TextComponent tc = new TextComponent();
            tc.setText("§8[§aDiscordBot§8] §4You can find the newest version over §e§lhere");
            tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§aminecraftMessages.yml").create()));
            tc.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/StaticFX/DiscordBotBungee/blob/1.5.0/bungeecord/src/main/resources/MinecraftMessages.yml"));
            TextComponent sendedText = new TextComponent();
            sendedText.addExtra(tc);
            player.sendMessage(sendedText);

            player.sendMessage(new TextComponent("§8[§aDiscordBot§8] §4It may come to errors."));
        }

        if(!DBVerifier.dcMSGVersion.equals(DiscordMessageFileManager.INSTANCE.getVersion())) {
            player.sendMessage(new TextComponent("§8[§aDiscordBot§8] §4Your discordmessages.yml version is not compatible with the §4plugin-config version."));
            player.sendMessage(new TextComponent("§8[§aDiscordBot§8] §4Config version: §l" + MessagesFileManager.INSTANCE.getVersion()));
            player.sendMessage(new TextComponent("§8[§aDiscordBot§8] §4Plugin version: §l" + DBVerifier.dcMSGVersion));


            TextComponent tc = new TextComponent();
            tc.setText("§8[§aDiscordBot§8] §4You can find the newest version over §e§lhere");
            tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§adiscordMessages.yml").create()));
            tc.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/StaticFX/DiscordBotBungee/blob/1.5.0/bungeecord/src/main/resources/DiscordMessages.yml"));
            TextComponent sendedText = new TextComponent();
            sendedText.addExtra(tc);
            player.sendMessage(sendedText);

            player.sendMessage(new TextComponent("§8[§aDiscordBot§8] §4It may come to errors."));
        }

        if(!SettingsFileManager.INSTANCE.isSetup()) {
            System.out.println("§8[§bDBVerifier§8] §aPlease setup the Plugin to make it work!");
            return;
        }




        try {
            boolean verified = VerifyDAO.INSTANCE.isPlayerVerified(player.getUniqueId());

            VerifyDAO.INSTANCE.updateUserName(player);

            if(!verified && !DBVerifier.getInstance().getStringFromConfig("DiscordLinkColored",false).isEmpty()) {
                TextComponent tc = new TextComponent();
                tc.setText(DBVerifier.getInstance().getStringFromConfig("DiscordLinkColored",false));
                tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(DBVerifier.getInstance().getStringFromConfig("JoinOurDiscord",false)).create()));
                tc.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, DBVerifier.getInstance().getStringFromConfig("DiscordLinkRaw",false)));
                TextComponent sendedText = new TextComponent(DBVerifier.getInstance().getStringFromConfig("JoinOurDiscordMainText",true));
                sendedText.addExtra(tc);
                player.sendMessage(sendedText);
                return;
            }

            if (!verified) return;


            ProxyServer.getInstance().getScheduler().runAsync(DBVerifier.getInstance(),() -> {
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

                if(ConfigFileManager.INSTANCE.autoUpdate()) {
                    DBVerifier.getInstance().removeAllRolesFromMember(m);
                    MemberManager.updateRoles(m,player);
                }

                DBVerifier.getInstance().playerSRVVerifiedHashMap.put(player.getUniqueId(), verified);
            });




        }catch (SQLException e) {
            Debugger.debugMessage("[INFO] A player which is not registered on the database joined the server. This must be a error by your networking system and the player will not be able to verify himself, or do any type of commands.");
            return;
        }

    }

}
