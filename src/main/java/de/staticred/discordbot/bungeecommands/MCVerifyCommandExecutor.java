package de.staticred.discordbot.bungeecommands;

import de.staticred.discordbot.DBVerifier;
import de.staticred.discordbot.api.EventManager;
import de.staticred.discordbot.db.RewardsDAO;
import de.staticred.discordbot.db.SRVDAO;
import de.staticred.discordbot.db.VerifyDAO;
import de.staticred.discordbot.event.UserClickedMessageEvent;
import de.staticred.discordbot.event.UserUnverifiedEvent;
import de.staticred.discordbot.event.UserVerifiedEvent;
import de.staticred.discordbot.files.BlockedServerFileManager;
import de.staticred.discordbot.files.RewardsFileManager;
import de.staticred.discordbot.util.MemberManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import sun.rmi.rmic.Main;

import java.awt.*;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public class MCVerifyCommandExecutor extends Command {

    public MCVerifyCommandExecutor(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender commandSender, String[] args) {

        if (!(commandSender instanceof ProxiedPlayer)) {
            commandSender.sendMessage(new TextComponent("You must be a player!"));
            return;
        }

        ProxiedPlayer p = (ProxiedPlayer) commandSender;


        if (args.length != 1) {
            p.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("VerifyPrefix",true)));
            return;
        }

        if (args[0].equalsIgnoreCase("accept")) {
            UserClickedMessageEvent event = new UserClickedMessageEvent(p,true);
            EventManager.instance.fireEvent(event);
            if(event.isCanceled()) return;

            if (!DBVerifier.getInstance().playerMemberHashMap.containsKey(p) && !DBVerifier.getInstance().playerChannelHashMap.containsKey(p)) {
                p.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("NoInquiries",true)));
                return;
            }

            if(BlockedServerFileManager.INSTANCE.getBlockedServers().contains(p.getServer().getInfo().getName())) {
                p.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("CantVerifyOnThisServer",true)));
                return;
            }

            Member m = DBVerifier.getInstance().playerMemberHashMap.get(p);
            TextChannel tc = DBVerifier.getInstance().playerChannelHashMap.get(p);

            DBVerifier.INSTANCE.removeAllRolesFromMember(m);
            DBVerifier.INSTANCE.updateRoles(m,p);

            if(DBVerifier.INSTANCE.syncNickname) {
                if(m.isOwner()) {
                    p.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("MemberIsOwner",false)));
                }else {
                    m.getGuild().modifyNickname(m,p.getName()).queue();
                }
            }


            try {

                UserVerifiedEvent event2 = new UserVerifiedEvent(m,p,tc);
                EventManager.instance.fireEvent(event2);
                if(event2.isCanceled()) return;



                VerifyDAO.INSTANCE.setPlayerAsVerified(p.getUniqueId());
                VerifyDAO.INSTANCE.addDiscordID(p, m);
                if(DBVerifier.getInstance().useSRV) SRVDAO.INSTANCE.link(p,m.getId());
            } catch (SQLException e) {
                p.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("InternalError",true)));
                e.printStackTrace();
                return;
            }

            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setDescription("You have been verified " + m.getAsMention());
            embedBuilder.setColor(Color.green);
            tc.sendMessage(embedBuilder.build()).queue(msg -> msg.delete().queueAfter(10, TimeUnit.SECONDS));
            DBVerifier.getInstance().playerChannelHashMap.remove(p);
            DBVerifier.getInstance().playerMemberHashMap.remove(p);

            try {
                if(!RewardsDAO.INSTANCE.hasPlayerBeenRewarded(p.getUniqueId())) {
                    for(String command : RewardsFileManager.INSTANCE.getCommandsOnVerified()) {
                        ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), command.replace("%player%",p.getName()));
                    }
                    RewardsDAO.INSTANCE.setPlayerRewardState(p.getUniqueId(),true);
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

            p.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("Verified",true)));
            return;

        } else if (args[0].equalsIgnoreCase("decline")) {
            UserClickedMessageEvent event = new UserClickedMessageEvent(p,true);
            EventManager.instance.fireEvent(event);
            if(event.isCanceled()) return;
            if (!DBVerifier.getInstance().playerMemberHashMap.containsKey(p) && !DBVerifier.getInstance().playerChannelHashMap.containsKey(p)) {
                p.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("NoInquiries",true)));
                return;
            }

            TextChannel tc = DBVerifier.getInstance().playerChannelHashMap.get(p);
            Member m = DBVerifier.getInstance().playerMemberHashMap.get(p);
            DBVerifier.getInstance().playerChannelHashMap.remove(p);
            DBVerifier.getInstance().playerMemberHashMap.remove(p);
            p.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("Declined",true)));
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setDescription("The inquiry has been denied " + m.getAsMention());
            embedBuilder.setColor(Color.red);
            tc.sendMessage(embedBuilder.build()).queue(msg -> msg.delete().queueAfter(10,TimeUnit.SECONDS));
            return;
        } else if (args[0].equalsIgnoreCase("update")) {
            try {
                if (!VerifyDAO.INSTANCE.hasDiscordID(p)) {
                    p.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("NotLinkedYet",true)));
                    return;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                p.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("InternalError",true)));
                return;
            }

            Member m;

            try {
                m = MemberManager.getMemberFromPlayer(p.getUniqueId());
            } catch (SQLException e) {
                p.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("InternalError",true)));
                e.printStackTrace();
                return;
            }

            if (m == null) {
                p.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("InternalError",true)));
                return;
            }

            DBVerifier.INSTANCE.removeAllRolesFromMember(m);
            DBVerifier.INSTANCE.updateRoles(m,p);

            if(DBVerifier.INSTANCE.syncNickname) {

                if (m.isOwner()) {
                    p.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("MemberIsOwner", false)));
                } else {
                    m.getGuild().modifyNickname(m, p.getName()).queue();
                }

            }

            p.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("UpdatedRankMC",true)));
            return;
        }else if(args[0].equalsIgnoreCase("unlink")) {

            try {
                if (!VerifyDAO.INSTANCE.hasDiscordID(p)) {
                    p.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("NotLinkedYet",true)));
                    return;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                p.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("InternalError",true)));
                return;
            }

            User u;
            try {
                u = DBVerifier.getInstance().jda.getUserById(VerifyDAO.INSTANCE.getDiscordID(p.getUniqueId()));
            } catch (SQLException e) {
                e.printStackTrace();
                p.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("InternalError",true)));
                return;
            }

            Member m = null;

            if (!DBVerifier.getInstance().jda.getGuilds().isEmpty()) {
                for (Guild guild : DBVerifier.getInstance().jda.getGuilds()) {
                    if (u != null)
                        m = guild.getMember(u);
                }
            } else {
                return;
            }


            if (m == null) {
                p.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("InternalError",true)));
                return;
            }


            if(m.isOwner()) {
                p.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("CantUnlink",true)));
                return;
            }

            DBVerifier.INSTANCE.removeAllRolesFromMember(m);

            try {
                UserUnverifiedEvent event = new UserUnverifiedEvent(m,p);
                EventManager.instance.fireEvent(event);
                if(event.isCanceled()) return;
                VerifyDAO.INSTANCE.removeDiscordID(p);
                VerifyDAO.INSTANCE.setPlayerAsUnVerified(p.getUniqueId());
                if(DBVerifier.getInstance().useSRV)
                SRVDAO.INSTANCE.unlink(p);
            } catch (SQLException e) {
                p.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("InternalError",true)));
                e.printStackTrace();
                return;
            }

            for(String command : RewardsFileManager.INSTANCE.getCommandsOnUnVerified()) {
                ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), command.replace("%player%",p.getName()));
            }

            p.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("UnlinkedYourSelf",true)));
            return;
        }
        p.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("VerifyPrefix",true)));
    }
}
