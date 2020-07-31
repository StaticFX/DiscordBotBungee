package de.staticred.discordbot.discordcommands;

import de.staticred.discordbot.DBVerifier;
import de.staticred.discordbot.db.VerifyDAO;
import de.staticred.discordbot.files.BlockedServerFileManager;
import de.staticred.discordbot.files.ConfigFileManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.awt.*;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public class VerifyCommandExecutor {

    public VerifyCommandExecutor(Member m, TextChannel tc, Message command, String[] args) {

        execute(m,tc,command,args);

    }

    private void execute(Member m, TextChannel tc, Message command, String[] args) {

        if(!DBVerifier.getInstance().setuped) return;

        if(args.length != 2) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setDescription(DBVerifier.getInstance().getStringFromConfig("VerifyDiscordSyntax",false) + m.getAsMention());
            embedBuilder.setColor(Color.red);
            tc.sendMessage(embedBuilder.build()).queue(msg -> msg.delete().queueAfter(10, TimeUnit.SECONDS));
            command.delete().queueAfter(10, TimeUnit.SECONDS);
            return;
        }


        String name = args[1];
        ProxiedPlayer player = DBVerifier.INSTANCE.getProxy().getPlayer(name);

        try {
            if(VerifyDAO.INSTANCE.isDiscordIDInUse(m.getId())) {
                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setDescription(DBVerifier.getInstance().getStringFromConfig("AlreadyLinked",false) + m.getAsMention());
                embedBuilder.setColor(Color.red);
                tc.sendMessage(embedBuilder.build()).queue(msg -> msg.delete().queueAfter(10, TimeUnit.SECONDS));
                command.delete().queueAfter(10, TimeUnit.SECONDS);
                return;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return;
        }

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setDescription(DBVerifier.getInstance().getStringFromConfig("SearchingForPlayer",false).replaceAll("%name%",name) + m.getAsMention());
        embedBuilder.setColor(Color.orange);
        tc.sendMessage(embedBuilder.build()).queue(msg -> msg.delete().queueAfter(10, TimeUnit.SECONDS));

        if(player == null) {
            embedBuilder.setDescription(DBVerifier.getInstance().getStringFromConfig("PlayerNotFound",false).replaceAll("%name%",name) + m.getAsMention());
            embedBuilder.setColor(Color.red);
            tc.sendMessage(embedBuilder.build()).queue(msg -> msg.delete().queueAfter(10, TimeUnit.SECONDS));
            command.delete().queueAfter(10,TimeUnit.SECONDS);
            return;
        }

        try {
            if(VerifyDAO.INSTANCE.isPlayerVerified(player.getUniqueId())) {
                embedBuilder.setDescription(DBVerifier.getInstance().getStringFromConfig("AlreadyVerified",false).replaceAll("%name%",name) + m.getAsMention());
                embedBuilder.setColor(Color.red);
                tc.sendMessage(embedBuilder.build()).queue(msg -> msg.delete().queueAfter(10, TimeUnit.SECONDS));
                command.delete().queueAfter(10,TimeUnit.SECONDS);
                return;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return;
        }

        if(DBVerifier.getInstance().playerChannelHashMap.containsKey(player)) {
            embedBuilder.setDescription(DBVerifier.getInstance().getStringFromConfig("SendInquiry",false).replaceAll("%name%",name) + m.getAsMention());
            embedBuilder.setColor(Color.red);
            tc.sendMessage(embedBuilder.build()).queue(msg -> msg.delete().queueAfter(10, TimeUnit.SECONDS));
            command.delete().queueAfter(10,TimeUnit.SECONDS);
            return;
        }
        if(DBVerifier.getInstance().playerMemberHashMap.containsKey(player)) {
            embedBuilder.setDescription(DBVerifier.getInstance().getStringFromConfig("AlreadyInquiry",false).replaceAll("%name%",name) + m.getAsMention());
            embedBuilder.setColor(Color.red);
            tc.sendMessage(embedBuilder.build()).queue(msg -> msg.delete().queueAfter(10, TimeUnit.SECONDS));
            command.delete().queueAfter(10,TimeUnit.SECONDS);
            return;
        }

        if(BlockedServerFileManager.INSTANCE.getBlockedServers().contains(player.getServer().getInfo().getName())) {
            embedBuilder.setDescription(DBVerifier.getInstance().getStringFromConfig("CantVerifyOnThisServer",false).replaceAll("%name%",name) + m.getAsMention());
            embedBuilder.setColor(Color.red);
            tc.sendMessage(embedBuilder.build()).queue(msg -> msg.delete().queueAfter(10, TimeUnit.SECONDS));
            command.delete().queueAfter(10,TimeUnit.SECONDS);
            return;
        }

        embedBuilder.setDescription(DBVerifier.getInstance().getStringFromConfig("FoundPlayer",false).replaceAll("%name%",name) + m.getAsMention());
        embedBuilder.setColor(Color.green);
        tc.sendMessage(embedBuilder.build()).queue(msg -> msg.delete().queueAfter(10, TimeUnit.SECONDS));
        command.delete().queueAfter(10,TimeUnit.SECONDS);

        player.sendMessage(new net.md_5.bungee.api.chat.TextComponent(DBVerifier.getInstance().getStringFromConfig("PendingInquiry",true).replaceAll("%user%",m.getEffectiveName())));


        String commandStrng = ConfigFileManager.INSTANCE.getString("verifycommand");

        net.md_5.bungee.api.chat.TextComponent textC = new net.md_5.bungee.api.chat.TextComponent();
        textC.setText("§a§lAccept");
        textC.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(DBVerifier.getInstance().getStringFromConfig("AcceptInquiry",false)).create()));
        textC.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + commandStrng + " accept"));
        net.md_5.bungee.api.chat.TextComponent tc2 = new net.md_5.bungee.api.chat.TextComponent();
        tc2.setText("§c§lDecline");
        tc2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(DBVerifier.getInstance().getStringFromConfig("DenyInquiry",false)).create()));
        tc2.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + commandStrng + " decline"));
        net.md_5.bungee.api.chat.TextComponent tc3 = new net.md_5.bungee.api.chat.TextComponent(" §7| ");
        textC.addExtra(tc3);
        textC.addExtra(tc2);
        player.sendMessage(textC);


        DBVerifier.getInstance().playerMemberHashMap.put(player,m);
        DBVerifier.getInstance().playerChannelHashMap.put(player,tc);

        ProxyServer.getInstance().getScheduler().schedule(DBVerifier.INSTANCE, () -> {
                if(DBVerifier.getInstance().playerMemberHashMap.containsKey(player) && DBVerifier.getInstance().playerChannelHashMap.containsKey(player)) {
                    DBVerifier.getInstance().playerChannelHashMap.remove(player,tc);
                    DBVerifier.getInstance().playerMemberHashMap.remove(player,m);
                    player.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("CanceledDueToTimeLimit",true)));
            }
        },20, TimeUnit.SECONDS);
    }

}
