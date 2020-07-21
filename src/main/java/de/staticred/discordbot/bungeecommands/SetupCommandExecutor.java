package de.staticred.discordbot.bungeecommands;

import de.staticred.discordbot.Main;
import de.staticred.discordbot.db.DataBaseConnection;
import de.staticred.discordbot.db.VerifyDAO;
import de.staticred.discordbot.files.ConfigFileManager;
import de.staticred.discordbot.files.DiscordFileManager;
import net.dv8tion.jda.api.entities.Role;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import sun.security.provider.ConfigFile;

import javax.security.auth.login.LoginException;
import java.sql.SQLException;

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

        if(!p.hasPermission("db.cmd.setup")) {
            p.sendMessage(new TextComponent(Main.getInstance().getStringFromConfig("NoPermission", true)));
            return;
        }

        if(!Main.settingUp.isEmpty()) {
            p.sendMessage(new TextComponent("§cAnother player is already setting up the plugin."));
            return;
        }

        Main.settingUp.add(p);
        p.sendMessage(new TextComponent("§aFirst we will check if your bot is reachable."));
        p.sendMessage(new TextComponent("§aTesting Connection:"));
        p.sendMessage(new TextComponent(""));

        try{
            Main.getInstance().initBot(Main.getInstance().token,Main.getInstance().activity);
        }catch (LoginException e) {
            p.sendMessage(new TextComponent("§cTest Failed ✖"));
            p.sendMessage(new TextComponent("§cBe sure to setup the correct token!"));
            p.sendMessage(new TextComponent(""));
            Main.settingUp.clear();
            return;
        }
        p.sendMessage(new TextComponent("§aTest succeed ✔"));
        p.sendMessage(new TextComponent(""));

        if(Main.getInstance().useSQL) {
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
                Main.settingUp.clear();
                return;
            }
            if(Main.useSRV) {
                p.sendMessage(new TextComponent("§aNow let´s test the SRV connection"));
                p.sendMessage(new TextComponent("§aTesting Connection:"));
                p.sendMessage(new TextComponent(""));
                if(DataBaseConnection.INSTANCE.connectTest()) {
                    p.sendMessage(new TextComponent("§aTest succeed ✔"));
                    p.sendMessage(new TextComponent(""));
                }else{
                    p.sendMessage(new TextComponent("§cTest Failed ✖"));
                    p.sendMessage(new TextComponent("§cBe sure to setup the correct connection details!"));
                    p.sendMessage(new TextComponent(""));
                    Main.settingUp.clear();
                    return;
                }
            }
        }

        p.sendMessage(new TextComponent("§aNow let´s check the discord Groups."));
        int groups = DiscordFileManager.INSTANCE.getAllGroups().size();
        if(groups == 0) {
            p.sendMessage(new TextComponent("§cThere were 0 groups found, please recheck your discord.yml!"));
            return;
        }

        p.sendMessage(new TextComponent("§aThere were §c" + groups + " §afound in the config."));
        p.sendMessage(new TextComponent("§aThe plugin will now generate the default values for each §agroup."));

        DiscordFileManager.INSTANCE.generateGroupConfig();

        p.sendMessage(new TextComponent("§aNow go in the discord.yml file and edit the §agroups."));
        p.sendMessage(new TextComponent("§aThe setup is now finished, after you edited, be sure to §arestart §ayour proxy."));

        Main.settingUp.clear();
        ConfigFileManager.INSTANCE.setSetuped(true);
    }
}
