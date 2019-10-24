package de.staticred.discordbot.discordevents;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildJoinEvent extends ListenerAdapter {

    public void onGuildMemberJoin(GuildMemberJoinEvent e) {
        Member m = e.getMember();
        sendPrivateMessage(m.getUser(),"Hey. Welcome to the velony.net network. Please use !verify [name] to get your discord rank. Be sure to be online while executing the command.");
    }


    public void sendPrivateMessage(User user, String content) {
        user.openPrivateChannel().queue((channel) ->
        {
            channel.sendMessage(content).queue();
        });
    }

}
