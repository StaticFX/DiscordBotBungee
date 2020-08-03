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
import sun.security.krb5.Config;

public class MessageEvent extends ListenerAdapter {

    public void onGuildMessageReceived(GuildMessageReceivedEvent e) {

        Message message = e.getMessage();
        String[] args = e.getMessage().getContentRaw().split(" ");
        Member m = e.getMember();


        if(!ConfigFileManager.INSTANCE.getVerifyChannel().isEmpty()) {
            if(!e.getChannel().getId().equals(ConfigFileManager.INSTANCE.getVerifyChannel()))
            return;
        }

        if(args[0].equalsIgnoreCase( ConfigFileManager.INSTANCE.getCommandPrefix() + "verify") || args[0].equalsIgnoreCase(ConfigFileManager.INSTANCE.getCommandPrefix() + "sync")) {
            new VerifyCommandExecutor(m,e.getChannel(),message,args);
            return;
        }else if(args[0].equalsIgnoreCase(ConfigFileManager.INSTANCE.getCommandPrefix() + "unlink")) {
            new UnlinkCommandExecutor(m,e.getChannel(),message,args);
            return;
        }else if(args[0].equalsIgnoreCase(ConfigFileManager.INSTANCE.getCommandPrefix() + "help")) {
            new HelpCommandExecutor(m,e.getChannel(),message,args);
            return;
        } else if (args[0].equalsIgnoreCase(ConfigFileManager.INSTANCE.getCommandPrefix() + "update")) {
            new UpdateCommandExecutor(m,e.getChannel(),message,args);
            return;
        }else{
            if(!ConfigFileManager.INSTANCE.getVerifyChannel().isEmpty()) {
                if(e.getChannel().getId().equals(ConfigFileManager.INSTANCE.getVerifyChannel())) {
                    if(!e.getMember().getUser().isBot()) {
                        if(ConfigFileManager.INSTANCE.forceCleanMode())
                            e.getMessage().delete().queue();
                    }
                }
            }
        }



    }


}
