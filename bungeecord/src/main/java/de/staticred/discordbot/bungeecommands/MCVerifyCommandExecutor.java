package de.staticred.discordbot.bungeecommands;

import de.staticred.discordbot.DBVerifier;
import de.staticred.discordbot.api.EventManager;
import de.staticred.discordbot.api.event.UserClickedMessageEvent;
import de.staticred.discordbot.api.event.UserUnverifiedEvent;
import de.staticred.discordbot.api.event.UserVerifiedEvent;
import de.staticred.discordbot.db.VerifyDAO;
import de.staticred.discordbot.files.BlockedServerFileManager;
import de.staticred.discordbot.files.ConfigFileManager;
import de.staticred.discordbot.files.DiscordMessageFileManager;
import de.staticred.discordbot.util.MemberManager;
import de.staticred.discordbot.util.manager.RewardManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.md_5.bungee.api.CommandSender;
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

            RewardManager.executeVerifyRewardProcess(p);

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

            Member m;
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

            RewardManager.executeVerifyUnlinkProcess(p);


            p.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("UnlinkedYourSelf",true)));
            return;
        }
        p.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("VerifyPrefix",true)));
    }
}
