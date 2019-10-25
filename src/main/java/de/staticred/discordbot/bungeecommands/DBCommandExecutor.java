package de.staticred.discordbot.bungeecommands;

import de.staticred.discordbot.db.VerifyDAO;
import de.staticred.discordbot.files.ConfigFileManager;
import de.staticred.discordbot.files.VerifyFileManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class DBCommandExecutor extends Command {

    public DBCommandExecutor(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {


        if(commandSender instanceof ProxiedPlayer) {
            ProxiedPlayer p = (ProxiedPlayer) commandSender;
            if(!p.hasPermission("db.reload")) {
                return;
            }
        }



        ConfigFileManager.INSTANCE.saveFile();
        ConfigFileManager.INSTANCE.loadFile();
        VerifyFileManager.INSTANCE.saveFile();
        VerifyFileManager.INSTANCE.loadFile();

        commandSender.sendMessage(new TextComponent("§aReloaded!"));


    }
}
