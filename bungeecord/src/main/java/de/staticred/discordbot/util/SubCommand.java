package de.staticred.discordbot.util;

import net.md_5.bungee.api.CommandSender;

public abstract class SubCommand {

    private String name;
    private CommandSender sender;
    private String[] args;

    public SubCommand(String name, CommandSender sender, String[] args) {
        this.name = name;
        this.sender = sender;
        this.args = args;
    }


    public abstract void execute(String name, CommandSender sender, String[] args);

}
