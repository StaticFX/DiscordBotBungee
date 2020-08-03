package de.staticred.discordbot.discordcommands;

import de.staticred.discordbot.DBVerifier;
import de.staticred.discordbot.files.ConfigFileManager;
import de.staticred.discordbot.files.DiscordMessageFileManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class HelpCommandExecutor {

    public HelpCommandExecutor(Member m, TextChannel tc, Message command, String args[]) {
        execute(m,tc,command,args);
    }

    private void execute(Member m, TextChannel tc, Message command, String[] args) {

        EmbedBuilder embedBuilder = new EmbedBuilder();

        if(args.length != 1) {
            embedBuilder.setDescription("Please use '!help'");
            embedBuilder.setColor(Color.RED);

            int time = ConfigFileManager.INSTANCE.getTime();

            if(time != -1) {
                tc.sendMessage(embedBuilder.build()).queue(msg -> msg.delete().queueAfter(time, TimeUnit.SECONDS));
                command.delete().queueAfter(10,TimeUnit.SECONDS);
            }else {
                tc.sendMessage(embedBuilder.build()).queue();
            }

            return;
        }

        String prefix = ConfigFileManager.INSTANCE.getCommandPrefix();

        embedBuilder.setDescription( prefix + "help -> list of all commands\n" + prefix + "verify -> synchronise your discord rank with your minecraft rank\n" + prefix +"unlink -> unlink from your mc account\n" + prefix + "update -> update your discord rank\n" + prefix + "info -> gives information about a linked player/member");
        embedBuilder.setFooter("DBVerifier " + DBVerifier.pluginVersion + " by StaticRed.");
        embedBuilder.setColor(Color.GREEN);

        int time = ConfigFileManager.INSTANCE.getTime();

        if(time != -1) {
            tc.sendMessage(embedBuilder.build()).queue(msg -> msg.delete().queueAfter(time, TimeUnit.SECONDS));
            command.delete().queueAfter(10,TimeUnit.SECONDS);
        }else {
            tc.sendMessage(embedBuilder.build()).queue();
        }

    }

}
