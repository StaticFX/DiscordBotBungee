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

        if(!m.getRoles().contains(ConfigFileManager.INSTANCE.getPermissionRole())) {
            if(time != -1) {
                tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbed("NoPermissions")).queue(msg -> msg.delete().queueAfter(time, TimeUnit.SECONDS));
            } else {
                tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbed("NoPermissions")).queue();
            }
            return;
        }

        if(args.length != 2) {
            if(time != -1) {
                tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbed("InfoSyntax")).queue(msg -> msg.delete().queueAfter(time, TimeUnit.SECONDS));
            } else {
                tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbed("InfoSyntax")).queue();
            }
            return;
        }

        String player = args[1];

        System.out.println(player);

        if(player.startsWith("<") && player.endsWith(">")) {

            String id = player.substring(2, player.length() -1);

            if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("ID fount for account: " + id);


            if(!VerifyDAO.INSTANCE.isDiscordIDInUse(id)) {
                if(time != -1) {
                    tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbed("NotVerifed")).queue(msg -> msg.delete().queueAfter(time, TimeUnit.SECONDS));
                } else {
                    tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbed("NotVerifed")).queue();
                }
                return;
            }

            UUID uuid = UUID.fromString(VerifyDAO.INSTANCE.getUUIDByDiscordID(id));
            System.out.println(uuid.toString());

            if(time != -1) {
                tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbedInformationMember("InformationAboutAsMember",m.getUser().getName(),id,uuid.toString())).queue(msg -> msg.delete().queueAfter(time, TimeUnit.SECONDS));
            } else {
                tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbedInformationMember("InformationAboutAsMember",m.getAsMention(),id,uuid.toString())).queue();
            }
            return;
        } else {

            UUID uuid = VerifyDAO.INSTANCE.getUUIDByName(player);

            if(uuid == null) {
                if(time != -1) {
                    tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbed("PlayerNotRegisteredOnMojang")).queue(msg -> msg.delete().queueAfter(time, TimeUnit.SECONDS));
                } else {
                    tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbed("PlayerNotRegisteredOnMojang")).queue();
                }
                return;
            }

            if(!VerifyDAO.INSTANCE.isPlayerVerified(uuid)) {
                if(time != -1) {
                    tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbed("NotVerifiedPlayer")).queue(msg -> msg.delete().queueAfter(time, TimeUnit.SECONDS));
                } else {
                    tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbed("NotVerifiedPlayer")).queue();
                }
                return;
            }

            String name = player;
            String id = VerifyDAO.INSTANCE.getDiscordID(uuid);
            String tagLine = MemberManager.getMemberFromPlayer(uuid).getUser().getAsTag();

            if(time != -1) {
                tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbedInformationPlayer("InformationAboutAsPlayer",name,id,uuid.toString(),tagLine,m.getAsMention())).queue(msg -> msg.delete().queueAfter(time, TimeUnit.SECONDS));
            } else {
                tc.sendMessage(DiscordMessageFileManager.INSTANCE.getEmbedInformationPlayer("InformationAboutAsPlayer",name,id,uuid.toString(),tagLine,m.getAsMention())).queue();
            }



        }

    }

}
