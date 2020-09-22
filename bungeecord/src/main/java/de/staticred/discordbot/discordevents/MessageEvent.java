package de.staticred.discordbot.discordevents;

import de.staticred.discordbot.DBVerifier;
import de.staticred.discordbot.discordcommands.*;
import de.staticred.discordbot.files.AliasesFileManager;
import de.staticred.discordbot.files.ConfigFileManager;
import de.staticred.discordbot.util.Debugger;
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

        if(!args[0].startsWith(ConfigFileManager.INSTANCE.getCommandPrefix())) return;


        if(args[0].equalsIgnoreCase( ConfigFileManager.INSTANCE.getCommandPrefix() + "verify") || AliasesFileManager.INSTANCE.getVerifyAliases().contains(args[0].substring(ConfigFileManager.INSTANCE.getCommandPrefix().length()))) {
            new VerifyCommandExecutor(m,e.getChannel(),message,args);
            return;
        }else if(args[0].equalsIgnoreCase(ConfigFileManager.INSTANCE.getCommandPrefix() + "unlink") || AliasesFileManager.INSTANCE.getUnlinkAliases().contains(args[0].substring(ConfigFileManager.INSTANCE.getCommandPrefix().length()))) {
            new UnlinkCommandExecutor(m,e.getChannel(),message,args);
            return;
        }else if(args[0].equalsIgnoreCase(ConfigFileManager.INSTANCE.getCommandPrefix() + "help") || AliasesFileManager.INSTANCE.getHelpAliases().contains(args[0].substring(ConfigFileManager.INSTANCE.getCommandPrefix().length()))) {
            new HelpCommandExecutor(m,e.getChannel(),message,args);
            return;
        } else if (args[0].equalsIgnoreCase(ConfigFileManager.INSTANCE.getCommandPrefix() + "update") || AliasesFileManager.INSTANCE.getUpdateAliases().contains(args[0].substring(ConfigFileManager.INSTANCE.getCommandPrefix().length()))) {
            new UpdateCommandExecutor(m, e.getChannel(), message, args);
            return;
        } else if  (args[0].equalsIgnoreCase(ConfigFileManager.INSTANCE.getCommandPrefix() + "info") || AliasesFileManager.INSTANCE.getInfoAliases().contains(args[0].substring(ConfigFileManager.INSTANCE.getCommandPrefix().length()))) {
            new InfoCommandExecutor(m,e.getChannel(),message,args);
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
