package de.staticred.discordbot.bungeecommands;

import de.staticred.discordbot.Main;
import de.staticred.discordbot.db.SRVDAO;
import de.staticred.discordbot.db.VerifyDAO;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import javax.swing.plaf.metal.MetalMenuBarUI;
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
            p.sendMessage(new TextComponent(Main.getInstance().getStringFromConfig("VerifyPrefix",true)));
            return;
        }

        if (args[0].equalsIgnoreCase("accept")) {

            if (!Main.playerMemberHashMap.containsKey(p) && !Main.playerChannelHashMap.containsKey(p)) {
                p.sendMessage(new TextComponent(Main.getInstance().getStringFromConfig("NoInquiries",true)));
                return;
            }

            Member m = Main.playerMemberHashMap.get(p);
            TextChannel tc = Main.playerChannelHashMap.get(p);




            Main.INSTANCE.updateRoles(m,p);

            if(Main.INSTANCE.syncNickname) {
                if(m.isOwner()) {
                    p.sendMessage(new TextComponent(Main.getInstance().getStringFromConfig("MemberIsOwner",false)));
                }else {
                    m.getGuild().modifyNickname(m,p.getName()).queue();
                }
            }

            try {
                VerifyDAO.INSTANCE.updateRank(p);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            try {
                VerifyDAO.INSTANCE.setPlayerAsVerified(p);
                VerifyDAO.INSTANCE.addDiscordID(p, m);
                if(Main.useSRV) SRVDAO.INSTANCE.link(p,m.getId());
            } catch (SQLException e) {
                p.sendMessage(new TextComponent(Main.getInstance().getStringFromConfig("InternalError",true)));
                e.printStackTrace();
                return;
            }

            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setDescription("You have been verified " + m.getAsMention());
            embedBuilder.setColor(Color.green);
            tc.sendMessage(embedBuilder.build()).queue(msg -> msg.delete().queueAfter(10, TimeUnit.SECONDS));
            Main.playerChannelHashMap.remove(p);
            Main.playerMemberHashMap.remove(p);

            p.sendMessage(new TextComponent(Main.getInstance().getStringFromConfig("Verified",true)));
            return;

        } else if (args[0].equalsIgnoreCase("decline")) {
            if (!Main.playerMemberHashMap.containsKey(p) && !Main.playerChannelHashMap.containsKey(p)) {
                p.sendMessage(new TextComponent(Main.getInstance().getStringFromConfig("NoInquiries",true)));
                return;
            }

            TextChannel tc = Main.playerChannelHashMap.get(p);
            Member m = Main.playerMemberHashMap.get(p);

            Main.playerChannelHashMap.remove(p);
            Main.playerMemberHashMap.remove(p);
            p.sendMessage(new TextComponent(Main.getInstance().getStringFromConfig("Declined",true)));
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setDescription("The inquiry has been denied " + m.getAsMention());
            embedBuilder.setColor(Color.red);
            tc.sendMessage(embedBuilder.build()).queue(msg -> msg.delete().queueAfter(10,TimeUnit.SECONDS));

            return;
        } else if (args[0].equalsIgnoreCase("update")) {
            try {
                if (!VerifyDAO.INSTANCE.hasDiscordID(p)) {
                    p.sendMessage(new TextComponent(Main.getInstance().getStringFromConfig("NotLinkedYet",true)));
                    return;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                p.sendMessage(new TextComponent(Main.getInstance().getStringFromConfig("InternalError",true)));
                return;
            }

            Member m;

            try {
                m = Main.INSTANCE.getMemberFromPlayer(p);
            } catch (SQLException e) {
                p.sendMessage(new TextComponent(Main.getInstance().getStringFromConfig("InternalError",true)));
                e.printStackTrace();
                return;
            }



            if (m == null) {
                p.sendMessage(new TextComponent(Main.getInstance().getStringFromConfig("InternalError",true)));
                return;
            }



            Main.INSTANCE.removeAllRolesFromMember(m);
            Main.INSTANCE.updateRoles(m,p);

            if(Main.INSTANCE.syncNickname)

                if(m.isOwner()) {
                    p.sendMessage(new TextComponent(Main.getInstance().getStringFromConfig("MemberIsOwner",false)));
                }else{
                    m.getGuild().modifyNickname(m,p.getName()).queue();
                }

            try {
                VerifyDAO.INSTANCE.updateRank(p);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            p.sendMessage(new TextComponent(Main.getInstance().getStringFromConfig("UpdatedRankMC",true)));
            return;
        }else if(args[0].equalsIgnoreCase("unlink")) {

            try {
                if (!VerifyDAO.INSTANCE.hasDiscordID(p)) {
                    p.sendMessage(new TextComponent(Main.getInstance().getStringFromConfig("NotLinkedYet",true)));
                    return;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                p.sendMessage(new TextComponent(Main.getInstance().getStringFromConfig("InternalError",true)));
                return;
            }

            User u;
            try {
                u = Main.jda.getUserById(VerifyDAO.INSTANCE.getDiscordID(p));
            } catch (SQLException e) {
                e.printStackTrace();
                p.sendMessage(new TextComponent(Main.getInstance().getStringFromConfig("InternalError",true)));
                return;
            }

            Member m = null;

            if (!Main.jda.getGuilds().isEmpty()) {
                for (Guild guild : Main.jda.getGuilds()) {
                    if (u != null)
                        m = guild.getMember(u);
                }
            } else {
                return;
            }


            if (m == null) {
                p.sendMessage(new TextComponent(Main.getInstance().getStringFromConfig("InternalError",true)));
                return;
            }


            if(m.isOwner()) {
                p.sendMessage(new TextComponent(Main.getInstance().getStringFromConfig("CantUnlink",true)));
                return;
            }

            Main.INSTANCE.removeAllRolesFromMember(m);

            try {
                VerifyDAO.INSTANCE.removeDiscordID(p);
                VerifyDAO.INSTANCE.setPlayerAsUnVerified(p);
                if(Main.useSRV)
                SRVDAO.INSTANCE.unlink(p);
            } catch (SQLException e) {
                p.sendMessage(new TextComponent(Main.getInstance().getStringFromConfig("InternalError",true)));
                e.printStackTrace();
                return;
            }

            p.sendMessage(new TextComponent(Main.getInstance().getStringFromConfig("UnlinkedYourSelf",true)));
            return;
        }
        p.sendMessage(new TextComponent(Main.getInstance().getStringFromConfig("VerifyPrefix",true)));
    }


}
