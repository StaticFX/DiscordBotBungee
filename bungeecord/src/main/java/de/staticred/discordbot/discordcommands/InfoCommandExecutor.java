package de.staticred.discordbot.discordcommands;

import de.staticred.discordbot.DBVerifier;
import de.staticred.discordbot.db.VerifyDAO;
import de.staticred.discordbot.files.ConfigFileManager;
import de.staticred.discordbot.files.DiscordMessageFileManager;
import de.staticred.discordbot.util.Debugger;
import de.staticred.discordbot.util.MemberManager;
import de.staticred.discordbot.util.UUIDFetcher;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class InfoCommandExecutor {

    public InfoCommandExecutor(Member m, TextChannel tc, Message command, String args[]) {
        try {
            execute(m,tc,command,args);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void execute(Member m, TextChannel tc, Message command, String[] args) throws SQLException {

        int time = ConfigFileManager.INSTANCE.getTime();

        if(time != -1)
            command.delete().queueAfter(time,TimeUnit.SECONDS);


        if(!m.getRoles().contains(ConfigFileManager.INSTANCE.getPermissionRole())) {
            if(time != -1) {
                tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbed("NoPermissions", m)).queue(msg -> msg.delete().queueAfter(time, TimeUnit.SECONDS));
            } else {
                tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbed("NoPermissions", m)).queue();
            }
            return;
        }

        if(args.length != 2) {
            if(time != -1) {
                tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbed("InfoSyntax", m)).queue(msg -> msg.delete().queueAfter(time, TimeUnit.SECONDS));
            } else {
                tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbed("InfoSyntax", m)).queue();
            }
            return;
        }

        String player = args[1];

        if(player.startsWith("<") && player.endsWith(">")) {

            String id;
            if(player.contains("!")) {
                id = player.substring(3, player.length() -1);
            }else {
                id = player.substring(2, player.length() -1);
            }


            if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("ID fount for account: " + id);


            if(!VerifyDAO.INSTANCE.isDiscordIDInUse(id)) {
                if(time != -1) {
                    tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbed("NotVerifed", m)).queue(msg -> msg.delete().queueAfter(time, TimeUnit.SECONDS));
                } else {
                    tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbed("NotVerifed", m)).queue();
                }
                return;
            }

            UUID uuid = UUID.fromString(VerifyDAO.INSTANCE.getUUIDByDiscordID(id));
            String name = VerifyDAO.INSTANCE.getName(id);
            Member target = MemberManager.getMemberFromPlayer(uuid);

            if(time != -1) {
                tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbedInformationMember("InformationAboutAsMember",name,id,uuid.toString(),target.getEffectiveName(),target.getAsMention())).queue(msg -> msg.delete().queueAfter(time, TimeUnit.SECONDS));
            } else {
                tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbedInformationMember("InformationAboutAsMember",name,id,uuid.toString(),target.getEffectiveName(),target.getAsMention())).queue();
            }
            return;
        } else if (player.length() > 16) {

            long id;

            try {
                id = Long.parseLong(player);
            }catch (Exception e) {
                if(time != -1) {
                    tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbed("MustBeOver16Chars", m)).queue(msg -> msg.delete().queueAfter(time, TimeUnit.SECONDS));
                } else {
                    tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbed("MustBeOver16Chars", m)).queue();
                }
                return;
            }

            User givenDiscordUser;

            try {
                givenDiscordUser = tc.getJDA().retrieveUserById(id).complete();
            }catch (Exception e) {
                if(time != -1) {
                    tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbed("CantFindMember", m)).queue(msg -> msg.delete().queueAfter(time, TimeUnit.SECONDS));
                } else {
                    tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbed("CantFindMember", m)).queue();
                }
                return;
            }


            if(givenDiscordUser == null) {
                if(time != -1) {
                    tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbed("CantFindMember", m)).queue(msg -> msg.delete().queueAfter(time, TimeUnit.SECONDS));
                } else {
                    tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbed("CantFindMember", m)).queue();
                }
                return;
            }

            Member member;

            try{
                 member = tc.getJDA().getGuilds().get(0).retrieveMember(givenDiscordUser).complete();
            }catch (Exception e) {
                if(time != -1) {
                    tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbed("CantFindMemberOnDiscord", m)).queue(msg -> msg.delete().queueAfter(time, TimeUnit.SECONDS));
                } else {
                    tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbed("CantFindMemberOnDiscord", m)).queue();
                }
                return;
            }


            if(member == null) {
                if(time != -1) {
                    tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbed("CantFindMemberOnDiscord", m)).queue(msg -> msg.delete().queueAfter(time, TimeUnit.SECONDS));
                } else {
                    tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbed("CantFindMemberOnDiscord", m)).queue();
                }
                return;
            }

            if(!VerifyDAO.INSTANCE.isDiscordIDInUse(member.getId())) {
                if(time != -1) {
                    tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbed("NotVerifed", m)).queue(msg -> msg.delete().queueAfter(time, TimeUnit.SECONDS));
                } else {
                    tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbed("NotVerifed", m)).queue();
                }
                return;
            }

            UUID uuid = UUID.fromString(VerifyDAO.INSTANCE.getUUIDByDiscordID(member.getId()));
            String name = VerifyDAO.INSTANCE.getName(member.getId());
            Member target = MemberManager.getMemberFromPlayer(uuid);

            if(time != -1) {
                tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbedInformationMemberOverID("InformationAboutAsMemberID",name,member.getId(),uuid.toString(),target.getEffectiveName(),target.getAsMention())).queue(msg -> msg.delete().queueAfter(time, TimeUnit.SECONDS));
            } else {
                tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbedInformationMemberOverID("InformationAboutAsMemberID",name,member.getId(),uuid.toString(),target.getEffectiveName(),target.getAsMention())).queue();
            }

        } else {

            UUID uuid = VerifyDAO.INSTANCE.getUUIDByName(player);

            if(uuid == null) {
                if(time != -1) {
                    tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbed("PlayerNotRegisteredOnMojang", m)).queue(msg -> msg.delete().queueAfter(time, TimeUnit.SECONDS));
                } else {
                    tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbed("PlayerNotRegisteredOnMojang", m)).queue();
                }
                return;
            }

            if(!VerifyDAO.INSTANCE.isPlayerVerified(uuid)) {
                if(time != -1) {
                    tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbed("NotVerifiedPlayer", m)).queue(msg -> msg.delete().queueAfter(time, TimeUnit.SECONDS));
                } else {
                    tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbed("NotVerifiedPlayer", m)).queue();
                }
                return;
            }

            String id = VerifyDAO.INSTANCE.getDiscordID(uuid);
            String name = VerifyDAO.INSTANCE.getName(id);
            Member target = MemberManager.getMemberFromPlayer(uuid);
            String tagLine = target.getUser().getAsTag();

            if(time != -1) {
                tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbedInformationPlayer("InformationAboutAsPlayer",name,id,uuid.toString(),tagLine,target.getAsMention())).queue(msg -> msg.delete().queueAfter(time, TimeUnit.SECONDS));
            } else {
                tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbedInformationPlayer("InformationAboutAsPlayer",name,id,uuid.toString(),tagLine,target.getAsMention())).queue();
            }

        }

    }

}
