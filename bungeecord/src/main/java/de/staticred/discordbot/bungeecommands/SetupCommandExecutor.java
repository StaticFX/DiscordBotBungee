package de.staticred.discordbot.bungeecommands;

import de.staticred.discordbot.DBVerifier;
import de.staticred.discordbot.db.DataBaseConnection;
import de.staticred.discordbot.files.DiscordFileManager;
import de.staticred.discordbot.files.SettingsFileManager;
import de.staticred.discordbot.util.Debugger;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import javax.security.auth.login.LoginException;
import java.util.concurrent.TimeUnit;

public class SetupCommandExecutor extends Command {

    public SetupCommandExecutor() {
        super("setup");
    }



    @Override
    public void execute(CommandSender sender, String[] args) {
        

        if(!sender.hasPermission("db.cmd.setup") && !sender.hasPermission("db.*")) {
            sender.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("NoPermissions", true)));
            return;
        }

        if(!DBVerifier.getInstance().settingUp.isEmpty()) {
            sender.sendMessage(new TextComponent("§cAnother player is already setting up the plugin."));
            return;
        }

        DBVerifier.getInstance().settingUp.add(sender);
        sender.sendMessage(new TextComponent("§aFirst we will check if your bot is reachable."));
        sender.sendMessage(new TextComponent("§aTesting Connection:"));
        sender.sendMessage(new TextComponent(""));

        try{
            DBVerifier.getInstance().initBot(DBVerifier.getInstance().token, DBVerifier.getInstance().activity);
        }catch (LoginException e) {
            sender.sendMessage(new TextComponent("§cTest Failed ✖"));
            sender.sendMessage(new TextComponent("§cBe sure to setup the correct token!"));
            sender.sendMessage(new TextComponent(""));
            DBVerifier.getInstance().settingUp.clear();
            return;
        }
        sender.sendMessage(new TextComponent("§aTest succeed ✔"));
        sender.sendMessage(new TextComponent(""));

        if(DBVerifier.getInstance().useSQL) {
            sender.sendMessage(new TextComponent("§aNow let´s test the SQL connection"));
            sender.sendMessage(new TextComponent("§aTesting Connection:"));
            sender.sendMessage(new TextComponent(""));
            if(DataBaseConnection.INSTANCE.connectTest()) {
                sender.sendMessage(new TextComponent("§aTest succeed ✔"));
                sender.sendMessage(new TextComponent(""));
            }else{
                sender.sendMessage(new TextComponent("§cTest Failed ✖"));
                sender.sendMessage(new TextComponent("§cBe sure to setup the correct connection details!"));
                sender.sendMessage(new TextComponent(""));
                DBVerifier.getInstance().settingUp.clear();
                return;
            }
            DataBaseConnection.INSTANCE.connect();
            if(DBVerifier.getInstance().useSRV) {
                sender.sendMessage(new TextComponent("§aNow let´s test the SRV connection"));
                sender.sendMessage(new TextComponent("§aTesting Connection:"));
                sender.sendMessage(new TextComponent(""));

                if(!(sender instanceof ProxiedPlayer)) {
                    sender.sendMessage(new TextComponent("§cYou must be a player to test the SRV connection!"));
                    DBVerifier.getInstance().settingUp.clear();
                    return;
                }

                ProxiedPlayer p = (ProxiedPlayer) sender;

                if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Sending test message to bukkit server");
                DBVerifier.getInstance().bukkitMessageHandler.sendConnectTestToBukkit(p);


                if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Starting async scheduler, waiting 10 seconds");

                ProxyServer.getInstance().getScheduler().schedule(DBVerifier.getInstance(),() -> {
                    if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("3 seconds are over");

                    if(!DBVerifier.getInstance().foundSRV) {
                        sender.sendMessage(new TextComponent("§cTest Failed ✖"));
                        sender.sendMessage(new TextComponent("§cTimed out"));
                        sender.sendMessage(new TextComponent("§cThe plugin can't reach out to your bukkit server or the installed srv on the bukkit!"));
                        sender.sendMessage(new TextComponent(""));
                        DBVerifier.getInstance().settingUp.clear();
                        DBVerifier.getInstance().srvFailed = true;
                        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Set setup to failed");

                    }else{
                        sender.sendMessage(new TextComponent("§aTest succeed ✔"));
                        sender.sendMessage(new TextComponent(""));
                        groupCheck(sender);

                    }
                }, 3, TimeUnit.SECONDS);

            }else{
                groupCheck(sender);

            }
        }else{
            groupCheck(sender);
        }


    }


    public void groupCheck(CommandSender sender) {
        sender.sendMessage(new TextComponent("§aNow let´s check the discord Groups."));
        int groups = DiscordFileManager.INSTANCE.getAllGroups().size();
        if(groups == 0)
            sender.sendMessage(new TextComponent("§cThere were 0 groups found, please recheck your discord.yml if this is no error"));


        sender.sendMessage(new TextComponent("§aThere were §c" + groups + " §afound in the config."));
        sender.sendMessage(new TextComponent("§aThe plugin will now generate the default values for each §agroup."));

        DiscordFileManager.INSTANCE.generateGroupConfig();

        sender.sendMessage(new TextComponent("§aNow go in the discord - discord.yml file and edit the §agroups."));
        sender.sendMessage(new TextComponent("§aThe setup is now finished, after you edited, be sure to §arestart §ayour proxy."));

        DBVerifier.getInstance().settingUp.clear();
        SettingsFileManager.INSTANCE.setSetup(true);
    }
}
