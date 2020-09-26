package de.staticred.discordbot;

import de.staticred.discordbot.api.EventManager;
import de.staticred.discordbot.api.VerifyAPI;
import de.staticred.discordbot.bukkitconnectionhandler.BukkitMessageHandler;
import de.staticred.discordbot.bungeecommands.dbcommand.DBCommandExecutor;
import de.staticred.discordbot.bungeecommands.MCVerifyCommandExecutor;
import de.staticred.discordbot.bungeecommands.SetupCommandExecutor;
import de.staticred.discordbot.bungeecommands.dbgroupcommand.DBGroupCommandExecutor;
import de.staticred.discordbot.bungeeevents.*;
import de.staticred.discordbot.db.*;
import de.staticred.discordbot.discordevents.GuildJoinEvent;
import de.staticred.discordbot.discordevents.GuildLeftEvent;
import de.staticred.discordbot.discordevents.MessageEvent;
import de.staticred.discordbot.event.UserUpdatedRolesEvent;
import de.staticred.discordbot.files.*;
import de.staticred.discordbot.test.TestUserVerifiedEvent;
import de.staticred.discordbot.util.Debugger;
import de.staticred.discordbot.util.GroupInfo;
import io.netty.util.internal.logging.Log4J2LoggerFactory;
import io.netty.util.internal.logging.Log4JLoggerFactory;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import org.apache.log4j.lf5.Log4JLogRecord;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    //the version of the internal file system
    public final static String configVersion = "1.6.0";
    public final static String msgVersion = "1.6.0";
    public final static String dcMSGVersion  = "1.6.0";

    public static String pluginVersion;

    //the version of the database
    public final static String DATABASE_VERSION = "1.0.0";

    //debug mode of the plugin
    public boolean debugMode;

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

    @Override
    public void onEnable() {

        INSTANCE = this;

        if(!fileUpdater()) {
            Debugger.debugMessage("Error new FileSystem already exists. Please move your Files manually from 'DiscordBotBungee' to 'DBVerifier'");
        }

        VerifyAPI.instance = new VerifyAPI();

        ConfigFileManager.INSTANCE.loadFile();

        VerifyFileManager.INSTANCE.loadFile();

        MessagesFileManager.INSTANCE.loadFile();

        DiscordFileManager.INSTANCE.loadFile();

        RewardsFileManager.INSTANCE.loadFile();

        BlockedServerFileManager.INSTANCE.loadFile();

        DiscordMessageFileManager.INSTANCE.loadFile();

        SettingsFileManager.INSTANCE.loadFile();

        DiscordFileManager.INSTANCE.update();

        AliasesFileManager.INSTANCE.loadFile();

        debugMode = SettingsFileManager.INSTANCE.isDebug();

        setuped = SettingsFileManager.INSTANCE.isSetup();

        bukkitMessageHandler = new BukkitMessageHandler();

        getProxy().registerChannel(PLUGIN_CHANNEL_NAME);
        getProxy().getPluginManager().registerListener(this, bukkitMessageHandler);

        pluginVersion = getDescription().getVersion();

        if(ConfigFileManager.INSTANCE.useSQL() && setuped) {
            if(!DataBaseConnection.INSTANCE.connectTest())  {
                Debugger.debugMessage("Can't connect to database.");
                return;
            }
            DataBaseConnection.INSTANCE.connect();
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

        Debugger.debugMessage("Plugin loaded.");
    }


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

    private int getAmountOfGroupsTokens() {
        int amount = 0;
        for(String group : DiscordFileManager.INSTANCE.getAllGroups()) {
            try {
                DiscordFileManager.INSTANCE.getDiscordGroupIDForGroup(group);
                amount++;
            }catch (Exception ignore) {
            }
        }
        return amount;
    }

    @Override
    public void onDisable() {
        if(jda != null)
            jda.shutdownNow();
        if(SettingsFileManager.INSTANCE.isSetup()) {
            if(useSQL) {
                DataBaseConnection.INSTANCE.closeConnection();
            }
        }

    }

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



    public String getStringFromConfig(String string, boolean prefix) {
        if(prefix)
            return ConfigFileManager.INSTANCE.getString("prefix").replaceAll("&","§") + MessagesFileManager.INSTANCE.getString(string).replaceAll("&","§");
        return MessagesFileManager.INSTANCE.getString(string).replaceAll("&","§");
    }

    public void initBot(String token, Activity activity) throws LoginException {
        Debugger.debugMessage("Trying to init bot");
        jda = JDABuilder.createDefault(token).build();
        jda.getPresence().setPresence(activity,true);
        jda.addEventListener(new MessageEvent());
        jda.addEventListener(new GuildJoinEvent());
        jda.addEventListener(new GuildLeftEvent());
        Debugger.debugMessage("Bot Started!");
    }

    public static DBVerifier getInstance() {
        return INSTANCE;
    }


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
}
