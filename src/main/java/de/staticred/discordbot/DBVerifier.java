package de.staticred.discordbot;

import de.staticred.discordbot.api.EventManager;
import de.staticred.discordbot.api.VerifyAPI;
import de.staticred.discordbot.bungeecommands.dbcommand.DBCommandExecutor;
import de.staticred.discordbot.bungeecommands.MCVerifyCommandExecutor;
import de.staticred.discordbot.bungeecommands.SetupCommandExecutor;
import de.staticred.discordbot.bungeeevents.JoinEvent;
import de.staticred.discordbot.bungeeevents.LeaveEvent;
import de.staticred.discordbot.db.DataBaseConnection;
import de.staticred.discordbot.db.MetricsLite;
import de.staticred.discordbot.db.RewardsDAO;
import de.staticred.discordbot.db.VerifyDAO;
import de.staticred.discordbot.discordevents.GuildJoinEvent;
import de.staticred.discordbot.discordevents.GuildLeftEvent;
import de.staticred.discordbot.discordevents.MessageEvent;
import de.staticred.discordbot.event.UserUpdatedRolesEvent;
import de.staticred.discordbot.files.*;
import de.staticred.discordbot.test.TestUserVerifiedEvent;
import de.staticred.discordbot.util.Debugger;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import javax.security.auth.login.LoginException;
import java.io.File;
import java.nio.file.attribute.DosFileAttributes;
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
    public ArrayList<ProxiedPlayer> settingUp = new ArrayList<>();

    //the token of the bot
    public String token;

    //the activity of the bot
    public Activity activity;

    //the version of the internal file system
    public final static String configVersion = "1.1.1";
    public final static String msgVersion = "1.0.2";

    //the version of the database
    public final static String DATABASE_VERSION = "1.0.0";

    public boolean debugMode = false;

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

        setuped = ConfigFileManager.INSTANCE.isSetuped();

        if(ConfigFileManager.INSTANCE.useSQL() && setuped) {
            if(!DataBaseConnection.INSTANCE.connectTest())  {
                Debugger.debugMessage("Can't connect to database.");
                return;
            }
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
        MetricsLite metrics = new MetricsLite(this);


        System.out.println(ConfigFileManager.INSTANCE.hasVerifyRole());

        Debugger.debugMessage("Using metrics: " + metrics.isEnabled());
        loadMetrcis();

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


        if(getAmountOfGroupsTokens() != 0 && !ConfigFileManager.INSTANCE.useTokens()) {
            Debugger.debugMessage("[WARNING] " + getAmountOfGroupsTokens() + " groups are using tokens (IDS), but you set the 'useTokes' option to false in your config.");
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



    void loadMetrcis() {
        MetricsLite metrcis = new MetricsLite(this);
    }


    @Override
    public void onDisable() {

        if(jda != null)
            jda.shutdownNow();
        if(ConfigFileManager.INSTANCE.isSetuped()) {
            DataBaseConnection.INSTANCE.closeConnection();
        }

    }

    public void loadBungeeCommands(String command) {
        getProxy().getPluginManager().registerCommand(this,new MCVerifyCommandExecutor(command));
        getProxy().getPluginManager().registerCommand(this,new DBCommandExecutor("db"));
        getProxy().getPluginManager().registerCommand(this,new SetupCommandExecutor());
    }

    public void loadBungeeEvents() {
        getProxy().getPluginManager().registerListener(this,new JoinEvent());
        getProxy().getPluginManager().registerListener(this,new LeaveEvent());
    }



    public String getStringFromConfig(String string, boolean prefix) {
        if(prefix)
            return ConfigFileManager.INSTANCE.getString("prefix").replaceAll("&","§") + MessagesFileManager.INSTANCE.getString(string).replaceAll("&","§");
        return MessagesFileManager.INSTANCE.getString(string).replaceAll("&","§");
    }

    public void initBot(String token, Activity activity) throws LoginException {
        Debugger.debugMessage("Trying to init bot");
        jda = new JDABuilder(token).build();
        jda.getPresence().setPresence(activity,true);
        jda.addEventListener(new MessageEvent());
        jda.addEventListener(new GuildJoinEvent());
        jda.addEventListener(new GuildLeftEvent());
        Debugger.debugMessage("Bot Started!");
    }

    public static DBVerifier getInstance() {
        return INSTANCE;
    }


    //method used to give the players the role he needs
    public void updateRoles(Member m, ProxiedPlayer p) {
        List<Member> addedNonDynamicGroups = new ArrayList<>();
        List<String> roles = new ArrayList<>();


        if(debugMode) Debugger.debugMessage("Updating groups for player: " + p.getName() + " Member: " + m.getEffectiveName());

        //when the admin sets a verify role the member will get the role
        if(debugMode) Debugger.debugMessage("Checking if verify role is empty");

        if(ConfigFileManager.INSTANCE.hasVerifyRole()) {
            if(debugMode) Debugger.debugMessage("Role is not empty");

            try {
                if(debugMode) Debugger.debugMessage("Checking if tokens are used");

                if(ConfigFileManager.INSTANCE.useTokens()) {
                    if(debugMode) Debugger.debugMessage("Tokens are used");
                    if(debugMode) Debugger.debugMessage("Token found for group: verify - " + ConfigFileManager.INSTANCE.getVerifyRoleAsLong());
                    if(debugMode) Debugger.debugMessage("Trying to give role to the player");
                    if(debugMode) Debugger.debugMessage("Is role null? " + (m.getGuild().getRoleById((ConfigFileManager.INSTANCE.getVerifyRoleAsLong())) == null));

                    m.getGuild().addRoleToMember(m,m.getGuild().getRoleById((ConfigFileManager.INSTANCE.getVerifyRoleAsLong()))).queue();
                }else{
                    if(debugMode) Debugger.debugMessage("Tokens are not used");
                    if(debugMode) Debugger.debugMessage("Name found for group: verify - " + ConfigFileManager.INSTANCE.getVerifyRole());
                    if(debugMode) Debugger.debugMessage("Trying to give role to the player");
                    if(debugMode) Debugger.debugMessage("Is role null? " + (m.getGuild().getRolesByName(ConfigFileManager.INSTANCE.getVerifyRole(),true).get(0) == null));

                    m.getGuild().addRoleToMember(m,m.getGuild().getRolesByName(ConfigFileManager.INSTANCE.getVerifyRole(),true).get(0)).queue();
                }
            }catch (NullPointerException | IndexOutOfBoundsException exception) {
                Debugger.debugMessage("The Bot can't find the given Role. The Role the problem has occured: verify");
                Debugger.debugMessage("UseIDs: " + ConfigFileManager.INSTANCE.useTokens());
            }

        }

        if(debugMode) Debugger.debugMessage("Starting group loop");
        for(String group : DiscordFileManager.INSTANCE.getAllGroups()) {
            if(debugMode) Debugger.debugMessage("Checking if group " + group + " is a dynamic group.");
            if(!DiscordFileManager.INSTANCE.isDynamicGroup(group)) {
                if(debugMode) Debugger.debugMessage("Group is not dynamic.");
                if(debugMode) Debugger.debugMessage("Checking if the player already has a dynamic group.");
                if(addedNonDynamicGroups.contains(m)) {
                    if(debugMode) Debugger.debugMessage("Player already has a dynamic group. Skipping to next group.");
                    continue;
                }

                if(debugMode) Debugger.debugMessage("Player does not have dynamic group.");
                if(debugMode) Debugger.debugMessage("Checking if players has permission: " + DiscordFileManager.INSTANCE.getPermissionsForGroup(group));
                if(p.hasPermission(DiscordFileManager.INSTANCE.getPermissionsForGroup(group))) {
                    if(debugMode) Debugger.debugMessage("Player has permission.");

                    try {
                        if(debugMode) Debugger.debugMessage("Checking if tokens are used");
                        if(ConfigFileManager.INSTANCE.useTokens()) {
                            if(debugMode) Debugger.debugMessage("Tokens are used");
                            if(debugMode) Debugger.debugMessage("Token found for group: " + group + "-" +DiscordFileManager.INSTANCE.getDiscordGroupIDForGroup(group));
                            if(debugMode) Debugger.debugMessage("Trying to give role to the player");
                            m.getGuild().addRoleToMember(m,m.getGuild().getRoleById(DiscordFileManager.INSTANCE.getDiscordGroupIDForGroup(group))).queue();
                        }else{
                            if(debugMode) Debugger.debugMessage("Tokens are not used");
                            if(debugMode) Debugger.debugMessage("Token found for group: " + group + "-" + DiscordFileManager.INSTANCE.getDiscordGroupNameForGroup(group));
                            if(debugMode) Debugger.debugMessage("Trying to give role to the player");
                            m.getGuild().addRoleToMember(m,m.getGuild().getRolesByName(DiscordFileManager.INSTANCE.getDiscordGroupNameForGroup(group),true).get(0)).queue();
                        }
                        if(debugMode) Debugger.debugMessage("Adding nondynamic role to player");
                        roles.add(group);
                        addedNonDynamicGroups.add(m);
                    }catch (NullPointerException | IndexOutOfBoundsException exception) {
                        Debugger.debugMessage("The Bot can't find the given Role. The Role the problem has occured: " + group);
                        Debugger.debugMessage("UseIDs: " + ConfigFileManager.INSTANCE.useTokens());
                        return;
                    }


                }

            }else{

                if(debugMode) Debugger.debugMessage("Group is dynamic");
                if(debugMode) Debugger.debugMessage("Checking if players has permission: " + DiscordFileManager.INSTANCE.getPermissionsForGroup(group));

                if(p.hasPermission(DiscordFileManager.INSTANCE.getPermissionsForGroup(group))) {
                    if(debugMode) Debugger.debugMessage("Player has permission.");

                    if(debugMode) Debugger.debugMessage("Checking if tokens are used");
                    try {

                        if(ConfigFileManager.INSTANCE.useTokens()) {
                            if(debugMode) Debugger.debugMessage("Tokens are used");
                            if(debugMode) Debugger.debugMessage("Token found for group: " + group + "-" + DiscordFileManager.INSTANCE.getDiscordGroupIDForGroup(group));
                            if(debugMode) Debugger.debugMessage("Trying to give role to the player");
                            m.getGuild().addRoleToMember(m,m.getGuild().getRoleById(DiscordFileManager.INSTANCE.getDiscordGroupIDForGroup(group))).queue();
                        }else{
                            if(debugMode) Debugger.debugMessage("Tokens are not used");
                            if(debugMode) Debugger.debugMessage("Token found for group: " + group + "-" + DiscordFileManager.INSTANCE.getDiscordGroupIDForGroup(group));
                            if(debugMode) Debugger.debugMessage("Trying to give role to the player");
                            m.getGuild().addRoleToMember(m,m.getGuild().getRolesByName(DiscordFileManager.INSTANCE.getDiscordGroupNameForGroup(group),true).get(0)).queue();
                        }
                        roles.add(group);
                    }catch (NullPointerException | IndexOutOfBoundsException exception) {
                        Debugger.debugMessage("The Bot can't find the given Role. The Role the problem has occured: " + group);
                        Debugger.debugMessage("UseIDs: " + ConfigFileManager.INSTANCE.useTokens());
                        return;
                    }
                }
            }
        }
        EventManager.instance.fireEvent(new UserUpdatedRolesEvent(m,p,roles));
    }

    public List<String> getTheoreticalRoles(Member m, ProxiedPlayer p) {
        List<Member> addedNonDynamicGroups = new ArrayList<>();
        List<String> roles = new ArrayList<>();

        for(String group : DiscordFileManager.INSTANCE.getAllGroups()) {
            if(!DiscordFileManager.INSTANCE.isDynamicGroup(group)) {
                if(addedNonDynamicGroups.contains(m)) {
                    continue;
                }

                if(p.hasPermission(DiscordFileManager.INSTANCE.getPermissionsForGroup(group))) {
                    roles.add(group);
                    addedNonDynamicGroups.add(m);
                }

            }else{
                if(p.hasPermission(DiscordFileManager.INSTANCE.getPermissionsForGroup(group))) {
                    roles.add(group);
                }
            }
        }
        EventManager.instance.fireEvent(new UserUpdatedRolesEvent(m,p,roles));
        return roles;
    }

    public String getRank(ProxiedPlayer p) {
        return "none";
    }

    public void removeAllRolesFromMember(Member m) {
        for(Role role : m.getRoles()) {
                if(ConfigFileManager.INSTANCE.useTokens()) {
                    if(DiscordFileManager.INSTANCE.getAllDiscordGroups().contains(role.getId())) {
                        m.getGuild().removeRoleFromMember(m,role).queue();
                    }
                }else{
                    if(DiscordFileManager.INSTANCE.getAllDiscordGroups().contains(role.getName())) {
                        m.getGuild().removeRoleFromMember(m,role).queue();
                }
            }
        }
    }


}
