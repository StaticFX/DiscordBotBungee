package de.staticred.discordbot.discordevents;

import de.staticred.discordbot.files.ConfigFileManager;
import de.staticred.discordbot.files.DiscordMessageFileManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildJoinEvent extends ListenerAdapter {

    public void onGuildMemberJoin(GuildMemberJoinEvent e) {
        Member m = e.getMember();



        if(!DiscordMessageFileManager.INSTANCE.getString("JoinMessage").isEmpty()) {
            MessageEmbed message = DiscordMessageFileManager.INSTANCE.getEmbed("JoinMessage");

            if(!message.isEmpty() || !message.equals("")) {
                sendPrivateMessage(m.getUser(),message);
            }
        }
    }


    public void sendPrivateMessage(User user, MessageEmbed content) {
        user.openPrivateChannel().queue((channel) ->
        {
            channel.sendMessage(content).queue();
        });
    }

}
