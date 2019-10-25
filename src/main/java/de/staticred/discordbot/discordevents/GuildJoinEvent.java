package de.staticred.discordbot.discordevents;

import de.staticred.discordbot.files.ConfigFileManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildJoinEvent extends ListenerAdapter {

    public void onGuildMemberJoin(GuildMemberJoinEvent e) {
        Member m = e.getMember();

        String message = ConfigFileManager.INSTANCE.getString("JoinMessage");


        if(!message.isEmpty() || !message.equals("")) {
            sendPrivateMessage(m.getUser(),message);
        }

    }


    public void sendPrivateMessage(User user, String content) {
        user.openPrivateChannel().queue((channel) ->
        {
            channel.sendMessage(content).queue();
        });
    }

}
