package de.staticred.discordbot.discordevents;

import de.staticred.discordbot.discordcommands.HelpCommandExecutor;
import de.staticred.discordbot.discordcommands.UnlinkCommandExecutor;
import de.staticred.discordbot.discordcommands.UpdateCommandExecutor;
import de.staticred.discordbot.discordcommands.VerifyCommandExecutor;
import de.staticred.discordbot.files.ConfigFileManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageEvent extends ListenerAdapter {

    public void onGuildMessageReceived(GuildMessageReceivedEvent e) {

        Message message = e.getMessage();
        String[] args = e.getMessage().getContentRaw().split(" ");
        Member m = e.getMember();





        if(args[0].equalsIgnoreCase("!verify") || args[0].equalsIgnoreCase("!sync")) {
            if(!ConfigFileManager.INSTANCE.getVerifyChannel().isEmpty()) {
                if(!e.getChannel().getId().equals(ConfigFileManager.INSTANCE.getVerifyChannel())) {
                    e.getMessage().delete().queue();
                    return;
                }
            }
            new VerifyCommandExecutor(m,e.getChannel(),message,args);
            return;
        }else if(args[0].equalsIgnoreCase("!unlink")) {
            if(!ConfigFileManager.INSTANCE.getVerifyChannel().isEmpty()) {
                if(!e.getChannel().getId().equals(ConfigFileManager.INSTANCE.getVerifyChannel())) {
                    e.getMessage().delete().queue();
                    return;
                }
            }
            new UnlinkCommandExecutor(m,e.getChannel(),message,args);
            return;
        }else if(args[0].equalsIgnoreCase("!help")) {
            if(!ConfigFileManager.INSTANCE.getVerifyChannel().isEmpty()) {
                if(!e.getChannel().getId().equals(ConfigFileManager.INSTANCE.getVerifyChannel())) {
                    e.getMessage().delete().queue();
                    return;
                }
            }
            new HelpCommandExecutor(m,e.getChannel(),message,args);
            return;
        } else if (args[0].equalsIgnoreCase("!update")) {
            if(!ConfigFileManager.INSTANCE.getVerifyChannel().isEmpty()) {
                if(!e.getChannel().getId().equals(ConfigFileManager.INSTANCE.getVerifyChannel())) {
                    e.getMessage().delete().queue();
                    return;
                }
            }
            new UpdateCommandExecutor(m,e.getChannel(),message,args);
            return;
        }

    }


}
