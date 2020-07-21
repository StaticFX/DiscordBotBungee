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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Main extends Plugin {

    public static  Main INSTANCE;
    public static HashMap<ProxiedPlayer, Member> playerMemberHashMap = new HashMap<>();
    public static HashMap<ProxiedPlayer, TextChannel> playerChannelHashMap = new HashMap<>();
    public boolean useSQL;
    public boolean syncNickname;
    public static JDA jda;
    public static boolean setuped;
    public static boolean useSRV;
    public static ArrayList<ProxiedPlayer> settingUp = new ArrayList<>();
    public String token;
    public Activity activity;
    public static String configVersion = "1.1.1";
    public static String msgVersion = "1.0.1";
    public static String DATABASE_VERSION = "1.0.0";
    public static int timer = 0;
    public boolean debugMode = false;

    @Override
    public void onEnable() {

        INSTANCE = this;
        VerifyAPI.instance = new VerifyAPI();
        ConfigFileManager.INSTANCE.loadFile();
        VerifyFileManager.INSTANCE.loadFile();
        MessagesFileManager.INSTANCE.loadFile();
        DiscordFileManager.INSTANCE.loadFile();
        RewardsFileManager.INSTANCE.loadFile();

        setuped = ConfigFileManager.INSTANCE.isSetuped();

        if(ConfigFileManager.INSTANCE.useSQL() && setuped) {
            try {
                RewardsDAO.INSTANCE.loadTable();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            if(!DataBaseConnection.INSTANCE.connectTest())  {
                Debugger.debugMessage("Can't connect to database.");
                return;
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

        String command = ConfigFileManager.INSTANCE.getString("verifycommand");

        syncNickname = ConfigFileManager.INSTANCE.getSyncName();
        loadBungeeCommands(command);

        Debugger.debugMessage("Plugin loaded.");
    }



    void loadMetrcis() {
        MetricsLite metrcis = new MetricsLite(this);
    }


    @Override
    public void onDisable() {
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
        jda = new JDABuilder(token).build();
        jda.getPresence().setPresence(activity,true);
        jda.addEventListener(new MessageEvent());
        jda.addEventListener(new GuildJoinEvent());
        jda.addEventListener(new GuildLeftEvent());
        Debugger.debugMessage("Bot Started!");
    }

    public static Main getInstance() {
        return INSTANCE;
    }

    public void updateRoles(Member m, ProxiedPlayer p) {
        List<Member> addedNonDynamicGroups = new ArrayList<>();
        List<String> roles = new ArrayList<>();

        if(!ConfigFileManager.INSTANCE.getVerifyRole().isEmpty()) {
            if(ConfigFileManager.INSTANCE.useTokens()) {
                m.getGuild().addRoleToMember(m,m.getGuild().getRoleById((ConfigFileManager.INSTANCE.getVerifyRoleAsLong()))).queue();
            }else{
                m.getGuild().addRoleToMember(m,m.getGuild().getRolesByName(ConfigFileManager.INSTANCE.getVerifyRole(),true).get(0)).queue();
            }
        }

        for(String group : DiscordFileManager.INSTANCE.getAllGroups()) {
            if(!DiscordFileManager.INSTANCE.isDynamicGroup(group)) {
                if(addedNonDynamicGroups.contains(m)) {
                    continue;
                }

                if(p.hasPermission(DiscordFileManager.INSTANCE.getPermissionsForGroup(group))) {
                    if(ConfigFileManager.INSTANCE.useTokens()) {
                        m.getGuild().addRoleToMember(m,m.getGuild().getRoleById(DiscordFileManager.INSTANCE.getDiscordGroupIDForGroup(group))).queue();
                    }else{
                        m.getGuild().addRoleToMember(m,m.getGuild().getRolesByName(DiscordFileManager.INSTANCE.getDiscordGroupNameForGroup(group),true).get(0)).queue();
                    }
                    roles.add(group);
                    addedNonDynamicGroups.add(m);
                }

            }else{
                if(p.hasPermission(DiscordFileManager.INSTANCE.getPermissionsForGroup(group))) {
                    if(ConfigFileManager.INSTANCE.useTokens()) {
                        m.getGuild().addRoleToMember(m,m.getGuild().getRoleById(DiscordFileManager.INSTANCE.getDiscordGroupIDForGroup(group))).queue();
                    }else{
                        m.getGuild().addRoleToMember(m,m.getGuild().getRolesByName(DiscordFileManager.INSTANCE.getDiscordGroupNameForGroup(group),true).get(0)).queue();
                    }
                    roles.add(group);
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

    public Member getMemberFromPlayer(UUID uuid) throws SQLException {
        User u;
        u = Main.jda.getUserById((VerifyDAO.INSTANCE.getDiscordID(uuid)));
        Member m = null;
        if (!Main.jda.getGuilds().isEmpty()) {
            for (Guild guild : Main.jda.getGuilds()) {
                if (u != null)
                    m = guild.getMember(u);
            }
        } else {
            throw new SQLException("There was an internal error! The member of the player can´t be found. Please contact the developer of this plugin.") ;
        }
        return m;
    }
}
