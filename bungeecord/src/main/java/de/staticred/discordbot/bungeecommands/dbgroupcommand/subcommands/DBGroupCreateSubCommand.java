package de.staticred.discordbot.bungeecommands.dbgroupcommand.subcommands;

import de.staticred.discordbot.DBVerifier;
import de.staticred.discordbot.files.DiscordFileManager;
import de.staticred.discordbot.util.GroupInfo;
import de.staticred.discordbot.util.SubCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class DBGroupCreateSubCommand extends SubCommand {


    public DBGroupCreateSubCommand(String name, CommandSender sender, String[] args) {
        super(name, sender, args);
    }

    @Override
    public void execute(String name, CommandSender sender, String[] args) {

        if(!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("NeedToBeAPlayer", true)));
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;

        if (!sender.hasPermission("db.cmd.dbgroup.create") && !sender.hasPermission("db.*")) {
            sender.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("NoPermissions", true)));
            return;
        }

        if (args.length != 2) {
            sender.sendMessage(new TextComponent("§cUse: /dbgroup create <name>"));
            return;
        }

        String groupName = args[1];

        if(DiscordFileManager.INSTANCE.doesGroupExist(groupName)) {
            sender.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("GroupAlreadyExists", true)));
            return;
        }


        if(DBVerifier.getInstance().playerCreatingGroupStateHashMap.containsKey(player.getUniqueId())) {
            sender.sendMessage(new TextComponent(DBVerifier.getInstance().getStringFromConfig("AlreadyCreatingGroup",true)));
            return;
        }

        sender.sendMessage(new TextComponent("§aYou are now in the process to create a new group. \n§aFor this you have to write the values into the chat.\n§a You can always cancel the process by typing §cCANCEL §ain the §achat.\n\n§a§lPlease write first the permissions of the group."));

        DBVerifier.getInstance().playerCreatingGroupStateHashMap.put(player.getUniqueId(),0);
        DBVerifier.getInstance().playerGroupInfoHashMap.put(player.getUniqueId(), new GroupInfo(false, groupName, null, null,null));

    }
}
