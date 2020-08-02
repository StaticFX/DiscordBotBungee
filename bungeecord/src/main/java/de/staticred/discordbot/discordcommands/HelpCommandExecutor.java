package de.staticred.discordbot.discordcommands;

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
            }else{
                tc.sendMessage(embedBuilder.build()).queue();
            }

            return;
        }

        embedBuilder.setDescription("!help -> list of all commands\n!verify -> synchronise your discord rank with your minecraft rank\n!unlink -> unlink from your mc account\n!update -> update your discord rank");
        embedBuilder.setColor(Color.GREEN);

        int time = ConfigFileManager.INSTANCE.getTime();

        if(time != -1) {
            tc.sendMessage(embedBuilder.build()).queue(msg -> msg.delete().queueAfter(time, TimeUnit.SECONDS));
            command.delete().queueAfter(10,TimeUnit.SECONDS);
        }else{
            tc.sendMessage(embedBuilder.build()).queue();
        }

    }

}
