package de.staticred.discordbot.bungeecommands;

import de.staticred.discordbot.DBVerifier;
import de.staticred.discordbot.api.EventManager;
import de.staticred.discordbot.db.RewardsDAO;
import de.staticred.discordbot.db.VerifyDAO;
import de.staticred.discordbot.api.event.UserClickedMessageEvent;
import de.staticred.discordbot.api.event.UserUnverifiedEvent;
import de.staticred.discordbot.api.event.UserVerifiedEvent;
import de.staticred.discordbot.files.BlockedServerFileManager;
import de.staticred.discordbot.files.ConfigFileManager;
import de.staticred.discordbot.files.DiscordMessageFileManager;
import de.staticred.discordbot.files.RewardsFileManager;
import de.staticred.discordbot.util.Debugger;
import de.staticred.discordbot.util.MemberManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

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




            try {

                UserVerifiedEvent event2 = new UserVerifiedEvent(m,p,tc);
                EventManager.instance.fireEvent(event2);
                if(event2.isCanceled()) return;

                if(DBVerifier.getInstance().useSRV) {
                    DBVerifier.getInstance().bukkitMessageHandler.sendPlayerVerified(p,m.getId());
                }

                VerifyDAO.INSTANCE.setPlayerAsVerified(p.getUniqueId());
                VerifyDAO.INSTANCE.addDiscordID(p, m);
                MemberManager.updateRoles(m,p);
            } catch (SQLException e) {
                p.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("InternalError",true)));
                e.printStackTrace();
                return;
            }


            int time = ConfigFileManager.INSTANCE.getTime();
            if(time != -1) {
                tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbed("Verifed",m)).queue(msg -> msg.delete().queueAfter(time, TimeUnit.SECONDS));
            }else {
                tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbed("Verifed",m)).queue();
            }

            DBVerifier.getInstance().playerChannelHashMap.remove(p);
            DBVerifier.getInstance().playerMemberHashMap.remove(p);

            if(ConfigFileManager.INSTANCE.getSyncName()) {
                if(m.isOwner()) {
                    p.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("MemberIsOwner",true)));
                }
            }


            try {
                if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Checking if player got rewarded");
                if(!RewardsDAO.INSTANCE.hasPlayerBeenRewarded(p.getUniqueId())) {
                    if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Player has not been rewarded or ignoring");
                    if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Execute all Bungeecord commands");
                    for(String command : RewardsFileManager.INSTANCE.getCommandsOnVerifiedBungee()) {
                        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Execute bungeecord cmd: " + command);
                        ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), command.replace("%player%",p.getName()));
                    }
                    if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Execute all Bukkit commands");
                    for(String command : RewardsFileManager.INSTANCE.getCommandsOnVerifiedBukkit()) {
                        DBVerifier.getInstance().bukkitMessageHandler.sendCommand(p,command);
                        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Execute bukkit cmd: " + command);
                    }

                    if(!ConfigFileManager.INSTANCE.igrnoreRewardState())
                        RewardsDAO.INSTANCE.setPlayerRewardState(p.getUniqueId(),true);
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            } catch (HierarchyException e) {
                Debugger.debugMessage("Can't modify a member with higher or equal highest role than the bot! Can't modify " + m.getNickname());
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
            int time = ConfigFileManager.INSTANCE.getTime();
            if(time != -1) {
                tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbed("InquiryHasBeenDenied",m)).queue(msg -> msg.delete().queueAfter(time, TimeUnit.SECONDS));
            }else {
                tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbed("InquiryHasBeenDenied",m)).queue();
            }
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
            MemberManager.updateRoles(m,p);

            if(DBVerifier.INSTANCE.syncNickname) {

                if (m.isOwner()) {
                    p.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("MemberIsOwner", false)));
                } else {
                    try {
                        m.getGuild().modifyNickname(m, p.getName()).queue();
                    } catch (HierarchyException e) {
                        Debugger.debugMessage("Can't modify a member with higher or equal highest role than the bot! Can't modify " + m.getNickname());
                    }
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
            try {
                m = MemberManager.getMemberFromPlayer(p.getUniqueId());
            } catch (SQLException throwables) {
                throwables.printStackTrace();
                p.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("InternalError",true)));
                return;
            }


            if(m.isOwner()) {
                p.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("CantUnlink",true)));
                return;
            }


            try {
                UserUnverifiedEvent event = new UserUnverifiedEvent(m,p);
                EventManager.instance.fireEvent(event);
                if(event.isCanceled()) return;

                if(DBVerifier.getInstance().useSRV) {
                    DBVerifier.getInstance().bukkitMessageHandler.sendPlayerUnlinked(p,VerifyDAO.INSTANCE.getDiscordID(p.getUniqueId()));
                }

                VerifyDAO.INSTANCE.removeDiscordID(p);
                VerifyDAO.INSTANCE.setPlayerAsUnVerified(p.getUniqueId());
                DBVerifier.INSTANCE.removeAllRolesFromMember(m);


            } catch (SQLException e) {
                p.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("InternalError",true)));
                e.printStackTrace();
                return;
            }

            try {
                if(!RewardsDAO.INSTANCE.hasPlayerBeenRewarded(p.getUniqueId())) {
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
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

            p.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("UnlinkedYourSelf",true)));
            return;
        }
        p.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("VerifyPrefix",true)));
    }
}
