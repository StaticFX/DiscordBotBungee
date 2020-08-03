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
        if(!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage("You must be a player to setup the plugin!");
            return;
        }
        ProxiedPlayer p = (ProxiedPlayer) sender;

        if(!p.hasPermission("db.cmd.setup") && !sender.hasPermission("db.*")) {
            p.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("NoPermissions", true)));
            return;
        }

        if(!DBVerifier.getInstance().settingUp.isEmpty()) {
            p.sendMessage(new TextComponent("§cAnother player is already setting up the plugin."));
            return;
        }

        DBVerifier.getInstance().settingUp.add(p);
        p.sendMessage(new TextComponent("§aFirst we will check if your bot is reachable."));
        p.sendMessage(new TextComponent("§aTesting Connection:"));
        p.sendMessage(new TextComponent(""));

        try{
            DBVerifier.getInstance().initBot(DBVerifier.getInstance().token, DBVerifier.getInstance().activity);
        }catch (LoginException e) {
            p.sendMessage(new TextComponent("§cTest Failed ✖"));
            p.sendMessage(new TextComponent("§cBe sure to setup the correct token!"));
            p.sendMessage(new TextComponent(""));
            DBVerifier.getInstance().settingUp.clear();
            return;
        }
        p.sendMessage(new TextComponent("§aTest succeed ✔"));
        p.sendMessage(new TextComponent(""));

        if(DBVerifier.getInstance().useSQL) {
            p.sendMessage(new TextComponent("§aNow let´s test the SQL connection"));
            p.sendMessage(new TextComponent("§aTesting Connection:"));
            p.sendMessage(new TextComponent(""));
            if(DataBaseConnection.INSTANCE.connectTest()) {
                p.sendMessage(new TextComponent("§aTest succeed ✔"));
                p.sendMessage(new TextComponent(""));
            }else{
                p.sendMessage(new TextComponent("§cTest Failed ✖"));
                p.sendMessage(new TextComponent("§cBe sure to setup the correct connection details!"));
                p.sendMessage(new TextComponent(""));
                DBVerifier.getInstance().settingUp.clear();
                return;
            }
            if(DBVerifier.getInstance().useSRV) {
                p.sendMessage(new TextComponent("§aNow let´s test the SRV connection"));
                p.sendMessage(new TextComponent("§aTesting Connection:"));
                p.sendMessage(new TextComponent(""));


                if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Sending test message to bukkit server");
                DBVerifier.getInstance().bukkitMessageHandler.sendConnectTestToBukkit(p);


                if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Starting async scheduler, waiting 10 seconds");

                ProxyServer.getInstance().getScheduler().schedule(DBVerifier.getInstance(),() -> {
                    if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("3 seconds are over");

                    if(!DBVerifier.getInstance().foundSRV) {
                        p.sendMessage(new TextComponent("§cTest Failed ✖"));
                        p.sendMessage(new TextComponent("§cTimed out"));
                        p.sendMessage(new TextComponent("§cThe plugin can't reach out to your bukkit server or the installed srv on the bukkit!"));
                        p.sendMessage(new TextComponent(""));
                        DBVerifier.getInstance().settingUp.clear();
                        DBVerifier.getInstance().srvFailed = true;
                        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Set setup to failed");

                    }else{
                        p.sendMessage(new TextComponent("§aTest succeed ✔"));
                        p.sendMessage(new TextComponent(""));
                        groupCheck(p);

                    }
                }, 3, TimeUnit.SECONDS);

            }else{
                groupCheck(p);

            }
        }


    }


    public void groupCheck(ProxiedPlayer p) {
        p.sendMessage(new TextComponent("§aNow let´s check the discord Groups."));
        int groups = DiscordFileManager.INSTANCE.getAllGroups().size();
        if(groups == 0)
            p.sendMessage(new TextComponent("§cThere were 0 groups found, please recheck your discord.yml! If this is no error"));


        p.sendMessage(new TextComponent("§aThere were §c" + groups + " §afound in the config."));
        p.sendMessage(new TextComponent("§aThe plugin will now generate the default values for each §agroup."));

        DiscordFileManager.INSTANCE.generateGroupConfig();

        p.sendMessage(new TextComponent("§aNow go in the discord - names.yml file and edit the §agroups."));
        p.sendMessage(new TextComponent("§aThe setup is now finished, after you edited, be sure to §arestart §ayour proxy."));

        DBVerifier.getInstance().settingUp.clear();
        SettingsFileManager.INSTANCE.setSetup(true);
    }
}
