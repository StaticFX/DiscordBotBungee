package de.staticred.discordbot;

import de.staticred.discordbot.api.VerifyAPI;
import de.staticred.discordbot.bukkitconnectionhandler.BukkitMessageHandler;
import de.staticred.discordbot.bungeecommands.MCVerifyCommandExecutor;
import de.staticred.discordbot.bungeecommands.SetupCommandExecutor;
import de.staticred.discordbot.bungeecommands.dbcommand.DBCommandExecutor;
import de.staticred.discordbot.bungeecommands.dbgroupcommand.DBGroupCommandExecutor;
import de.staticred.discordbot.bungeeevents.ChangedBukkitServerEvent;
import de.staticred.discordbot.bungeeevents.JoinEvent;
import de.staticred.discordbot.bungeeevents.LeaveEvent;
import de.staticred.discordbot.bungeeevents.PostLoginEvent;
import de.staticred.discordbot.db.Metrics;
import de.staticred.discordbot.db.RewardsDAO;
import de.staticred.discordbot.db.VerifyDAO;
import de.staticred.discordbot.discordevents.GuildJoinEvent;
import de.staticred.discordbot.discordevents.GuildLeftEvent;
import de.staticred.discordbot.discordevents.MessageEvent;
import de.staticred.discordbot.files.ConfigFileManager;
import de.staticred.discordbot.files.DiscordFileManager;
import de.staticred.discordbot.files.MessagesFileManager;
import de.staticred.discordbot.files.SettingsFileManager;
import de.staticred.discordbot.test.TestUserVerifiedEvent;
import de.staticred.discordbot.util.Debugger;
import de.staticred.discordbot.util.FileAndDataBaseManager;
import de.staticred.discordbot.util.GroupInfo;
import de.staticred.discordbot.util.UpdateChecker;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class DBVerifier extends Plugin {


    //the instance of this howl plugin
    public static DBVerifier INSTANCE;

    //contains the user which was given by the discord member, and the member
    public HashMap<ProxiedPlayer, Member> playerMemberHashMap = new HashMap<>();

    //contains the user which was given by the member, and the textchannel where the message was written
    public HashMap<ProxiedPlayer, TextChannel> playerChannelHashMap = new HashMap<>();

    //if the plugins should use sql or the internal file system
    public boolean useSQL;

    //if the plugin should sync the mc names to discord
    public boolean syncNickname;

    //the jda of the bot
    public JDA jda;

    //if the plugin is setuped or not
    public boolean setuped;

    //if the plugin should use discordsrv or not
    public boolean useSRV;

    //the players who are setting up the plugin
    public ArrayList<CommandSender> settingUp = new ArrayList<>();

    //the token of the bot
    public String token;

    //the activity of the bot
    public Activity activity;

    //version from the plugin on the spigot site.
    public static String spigotVersion;

    //the version of the internal file system
    public final static String configVersion = "1.6.1";
    public final static String msgVersion = "1.6.5";
    public final static String dcMSGVersion  = "1.6.1";

    public static String pluginVersion;

    //the version of the database
    public final static String DATABASE_VERSION = "1.0.0";

    //debug mode of the plugin
    public boolean debugMode;

    //if there is a update on the spigot site
    public boolean isUpdateAvailable;

    //the name of the channel which is used to communicate with the bukkit subserver for a discordsrv connection
    public static final String PLUGIN_CHANNEL_NAME = "dbv:bungeecord";

    //this object is handling the messaging
    public BukkitMessageHandler bukkitMessageHandler;

    //this will store the last message which got send from a bukkit
    public String lastMessageFromBukkit;

    public boolean foundSRV = false;

    public boolean srvFailed = false;

    public HashMap<UUID, Boolean> playerSRVVerifiedHashMap = new HashMap<>();

    //hashmap used to save the state the user is setting up a group;
    public HashMap<UUID, Integer> playerCreatingGroupStateHashMap = new HashMap<>();
    //hashmap used to save the values the player gave
    public HashMap<UUID, GroupInfo> playerGroupInfoHashMap = new HashMap<>();

    //main start method
    @Override
    public void onEnable() {

        INSTANCE = this;

        //updating files
        if(!fileUpdater()) {
            Debugger.debugMessage("Error new FileSystem already exists. Please move your Files manually from 'DiscordBotBungee' to 'DBVerifier'");
        }



        VerifyAPI.instance = new VerifyAPI();


        //loading all files
        FileAndDataBaseManager.loadAllFilesAndDatabases();

        //settings used variables so they wont change in the middle of usage
        debugMode = SettingsFileManager.INSTANCE.isDebug();

        setuped = SettingsFileManager.INSTANCE.isSetup();

        //instance for handling bukkit incoming messages.
        bukkitMessageHandler = new BukkitMessageHandler();

        //registering plugin channel
        getProxy().registerChannel(PLUGIN_CHANNEL_NAME);
        getProxy().getPluginManager().registerListener(this, bukkitMessageHandler);

        pluginVersion = getDescription().getVersion();

        //loading databases.
        if(ConfigFileManager.INSTANCE.useSQL() && setuped) {

            try {
                RewardsDAO.INSTANCE.loadTable();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            VerifyDAO.INSTANCE.loadDataBase();
        }


        useSRV = ConfigFileManager.INSTANCE.useSRV();
        useSQL = ConfigFileManager.INSTANCE.useSQL();

        token = ConfigFileManager.INSTANCE.getString("bot-token");

        loadBungeeEvents();

        VerifyAPI.getInstance().registerEvent(new TestUserVerifiedEvent());

        String activity = ConfigFileManager.INSTANCE.getString("discordBotActivityType");
        String type = ConfigFileManager.INSTANCE.getString("discordBotActivity");
        String link = ConfigFileManager.INSTANCE.getString("streamingLink");


        Metrics metrics = new Metrics(this, 	5843);
        metrics.addCustomChart(new Metrics.SingleLineChart("groups_registered", () -> DiscordFileManager.INSTANCE.getAllGroups().size()));

        if(setuped) {
            int verifed = 0;
            try {
                verifed = VerifyDAO.INSTANCE.getAmountOfVerifiedPlayers();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            int finalVerifed = verifed;
            metrics.addCustomChart(new Metrics.SingleLineChart("players_verified", () -> finalVerifed));
        }



        Debugger.debugMessage("Using metrics: " + metrics.isEnabled());

        if(activity.equalsIgnoreCase("listening")) {
            this.activity = Activity.listening(type);
        }else if(activity.equalsIgnoreCase("playing")) {
            this.activity = Activity.playing(type);
        }else if(activity.equalsIgnoreCase("streaming")) {
            this.activity = Activity.streaming(type,link);
        }else if(activity.equalsIgnoreCase("watching")) {
            this.activity = Activity.watching(type);
        }else {
            this.activity = Activity.playing(type);
        }

        if(setuped) {
            try {
                initBot(token, this.activity);
            } catch (LoginException e) {
                e.printStackTrace();
                return;
            }
        }

        String command = ConfigFileManager.INSTANCE.getString("verifycommand");

        syncNickname = ConfigFileManager.INSTANCE.getSyncName();
        loadBungeeCommands(command);

        isUpdateAvailable();

        Debugger.debugMessage("Plugin loaded.");





        if(!SettingsFileManager.INSTANCE.isSetup()) {
            System.out.println("§b----------------------------------------------------");
            System.out.println("§b ___   ___ __   __           _   __  _           ");
            System.out.println("§b|   \\ | _ )\\ \\ / / ___  _ _ (_) / _|(_) ___  _ _ ");
            System.out.println("§b| |) || _ \\ \\   / / -_)| '_|| ||  _|| |/ -_)| '_|");
            System.out.println("§b|___/ |___/  \\_/  \\___||_|  |_||_|  |_|\\___||_|  ");
            System.out.println("§cPlugin not setup yet\n§aJoin our Support-Discord: §ediscord.gg/djqVDdx\n§aInstallation guide: §ehttps://github.com/StaticFX/DiscordBotBungee/wiki");
            System.out.println("§b------------DBVerifier " + pluginVersion + " by StaticRed------------");
        }else {
            System.out.println("§b----------------------------------------------------");
            System.out.println("§b ___   ___ __   __           _   __  _           ");
            System.out.println("§b|   \\ | _ )\\ \\ / / ___  _ _ (_) / _|(_) ___  _ _ ");
            System.out.println("§b| |) || _ \\ \\   / / -_)| '_|| ||  _|| |/ -_)| '_|");
            System.out.println("§b|___/ |___/  \\_/  \\___||_|  |_||_|  |_|\\___||_|  ");
            System.out.println("§b------------DBVerifier " + pluginVersion + " by StaticRed------------");
        }



    }

    //this will update filesystem before 1.5.0
    private boolean fileUpdater() {
        File oldFile = new File(getDataFolder().getParentFile().getAbsoluteFile() + "/DiscordBotBungee");

        if(oldFile.exists()) {
            Debugger.debugMessage("Trying to update oldFile system.");
            File renameTo = new File(getDataFolder().getParentFile().getAbsoluteFile() + "/DBVerifier");

            if(renameTo.exists()) {
                Debugger.debugMessage("Error new FileSystem already exists. Please move your Files manually from 'DiscordBotBungee' to 'DBVerifier'");
                return false;
            }

            return oldFile.renameTo(renameTo);
        } else {
            Debugger.debugMessage("Old file does not exist anymore, skipping autoupdate.");
        }
        return true;
    }



    @Override
    public void onDisable() {
        //shutting down jda to prevent multiple bot instances
        if(jda != null)
            jda.shutdownNow();
    }

    //bungeecord command
    public void loadBungeeCommands(String command) {
        getProxy().getPluginManager().registerCommand(this,new MCVerifyCommandExecutor(command));
        getProxy().getPluginManager().registerCommand(this,new DBCommandExecutor("db"));
        getProxy().getPluginManager().registerCommand(this,new SetupCommandExecutor());
        getProxy().getPluginManager().registerCommand(this, new DBGroupCommandExecutor("dbgroup"));
    }


    public void loadBungeeEvents() {
        getProxy().getPluginManager().registerListener(this,new JoinEvent());
        getProxy().getPluginManager().registerListener(this,new LeaveEvent());
        getProxy().getPluginManager().registerListener(this,new ChangedBukkitServerEvent());
        getProxy().getPluginManager().registerListener(this,new PostLoginEvent());
    }


    //get a string from the minecraftmessages.yml file
    public String getStringFromConfig(String string, boolean prefix) {
        if(prefix)
            return ConfigFileManager.INSTANCE.getString("prefix").replaceAll("&","§") + MessagesFileManager.INSTANCE.getString(string).replaceAll("&","§");
        return MessagesFileManager.INSTANCE.getString(string).replaceAll("&","§");
    }

    //connecting and starting to the bot
    public void initBot(String token, Activity activity) throws LoginException {
        Debugger.debugMessage("Trying to init bot");
        jda = JDABuilder.create(token, GatewayIntent.GUILD_MEMBERS, GatewayIntent.DIRECT_MESSAGE_REACTIONS, GatewayIntent.DIRECT_MESSAGE_TYPING, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_BANS, GatewayIntent.GUILD_EMOJIS, GatewayIntent.GUILD_EMOJIS, GatewayIntent.GUILD_INVITES, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.GUILD_MESSAGE_TYPING, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_VOICE_STATES).build();
        jda.getPresence().setPresence(activity,true);
        jda.addEventListener(new MessageEvent());
        jda.addEventListener(new GuildJoinEvent());
        jda.addEventListener(new GuildLeftEvent());
        Debugger.debugMessage("Bot Started!");
    }

    public static DBVerifier getInstance() {
        return INSTANCE;
    }


    //removes all the roles from a member (only removes registered roles)
    public void removeAllRolesFromMember(Member m) {

        for(Role role : m.getRoles()) {
                if(ConfigFileManager.INSTANCE.useTokens()) {
                    if(DiscordFileManager.INSTANCE.getAllDiscordGroups().contains(role.getId())) {
                        m.getGuild().removeRoleFromMember(m,role).queue();
                    }
                    if(ConfigFileManager.INSTANCE.getVerifyRole().equals(role.getId()))
                        m.getGuild().removeRoleFromMember(m,role).queue();
                }else{
                    if(DiscordFileManager.INSTANCE.getAllDiscordGroups().contains(role.getName())) {
                        m.getGuild().removeRoleFromMember(m,role).queue();
                    if(ConfigFileManager.INSTANCE.getVerifyRole().equals(role.getName()))
                        m.getGuild().removeRoleFromMember(m,role).queue();
                }
            }
        }
    }


    public boolean isUpdateAvailable() {
        new UpdateChecker(this, 72232).getVersion(version -> {
            if (this.getDescription().getVersion().equalsIgnoreCase(version)) {
                DBVerifier.getInstance().isUpdateAvailable = false;
            } else {
                DBVerifier.getInstance().isUpdateAvailable = true;
            }
        });
        return false;
    }


}
