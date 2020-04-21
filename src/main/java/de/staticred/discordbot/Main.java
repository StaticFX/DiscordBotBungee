package de.staticred.discordbot;

import de.staticred.discordbot.bungeecommands.DBCommandExecutor;
import de.staticred.discordbot.bungeecommands.MCVerifyCommandExecutor;
import de.staticred.discordbot.bungeecommands.SetupCommandExecutor;
import de.staticred.discordbot.bungeeevents.JoinEvent;
import de.staticred.discordbot.bungeeevents.LeaveEvent;
import de.staticred.discordbot.db.DataBaseConnection;
import de.staticred.discordbot.db.MetricsLite;
import de.staticred.discordbot.db.VerifyDAO;
import de.staticred.discordbot.discordevents.GuildJoinEvent;
import de.staticred.discordbot.discordevents.GuildLeftEvent;
import de.staticred.discordbot.discordevents.MessageEvent;
import de.staticred.discordbot.files.ConfigFileManager;
import de.staticred.discordbot.files.DiscordFileManager;
import de.staticred.discordbot.files.MessagesFileManager;
import de.staticred.discordbot.files.VerifyFileManager;
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
    public static String configVersion = "1.1.0";

    @Override
    public void onEnable() {

        INSTANCE = this;

        ConfigFileManager.INSTANCE.loadFile();
        VerifyFileManager.INSTANCE.loadFile();
        MessagesFileManager.INSTANCE.loadFile();
        DiscordFileManager.INSTANCE.loadFile();
        setuped = ConfigFileManager.INSTANCE.isSetuped();

        if(ConfigFileManager.INSTANCE.useSQL() && setuped) {
            loadDataBase();
        }


        useSRV = ConfigFileManager.INSTANCE.useSRV();
        useSQL = ConfigFileManager.INSTANCE.useSQL();
        token = ConfigFileManager.INSTANCE.getString("bot-token");
        loadBungeeEvents();

        String activity = ConfigFileManager.INSTANCE.getString("discordBotActivityType");
        String type = ConfigFileManager.INSTANCE.getString("discordBotActivity");
        String link = ConfigFileManager.INSTANCE.getString("streamingLink");
        MetricsLite metrics = new MetricsLite(this);
        System.out.println(metrics.isEnabled());

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
        getLogger().info("Success");
    }



    void loadMetrcis() {
        MetricsLite metrcis = new MetricsLite(this);
        System.out.println(metrcis.isEnabled());
    }


    @Override
    public void onDisable() {
        if(ConfigFileManager.INSTANCE.isSetuped()) {
            DataBaseConnection.INSTANCE.closeConnection();
        }
        ConfigFileManager.INSTANCE.saveFile();
        VerifyFileManager.INSTANCE.saveFile();
    }

    public void loadBungeeCommands(String command) {
        getProxy().getPluginManager().registerCommand(this,new MCVerifyCommandExecutor(command));
        getProxy().getPluginManager().registerCommand(this,new DBCommandExecutor("dbreload"));
        getProxy().getPluginManager().registerCommand(this,new SetupCommandExecutor());
    }

    public void loadBungeeEvents() {
        getProxy().getPluginManager().registerListener(this,new JoinEvent());
        getProxy().getPluginManager().registerListener(this,new LeaveEvent());
    }

    public void loadDataBase() {
        DataBaseConnection con = DataBaseConnection.INSTANCE;
        con.connect();
        System.out.println("[DiscordVerify] Connect test success!");
        try {
            con.executeUpdate("CREATE TABLE IF NOT EXISTS verify(UUID VARCHAR(36) PRIMARY KEY, Name VARCHAR(16), Rank VARCHAR(20), Verified BOOLEAN, DiscordID VARCHAR(100))");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DiscordVerify] Connect test failed!");
        }
        con.closeConnection();
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
        System.out.println("[DiscordVerify] Bot Started!");
    }

    public static Main getInstance() {
        return INSTANCE;
    }

    public void updateRoles(Member m, ProxiedPlayer p) {

        List<Member> addedNonDynamicGroups = new ArrayList<>();

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
                }

                addedNonDynamicGroups.add(m);

            }else{
                if(p.hasPermission(DiscordFileManager.INSTANCE.getPermissionsForGroup(group))) {
                    if(ConfigFileManager.INSTANCE.useTokens()) {
                        m.getGuild().addRoleToMember(m,m.getGuild().getRoleById(DiscordFileManager.INSTANCE.getDiscordGroupIDForGroup(group))).queue();
                    }else{
                        m.getGuild().addRoleToMember(m,m.getGuild().getRolesByName(DiscordFileManager.INSTANCE.getDiscordGroupNameForGroup(group),true).get(0)).queue();
                    }
                }
            }
        }
    }

    public String getRank(ProxiedPlayer p) {
        return "none";
    }

    public void removeAllRolesFromMember(Member m) {
        for(Role role : m.getRoles()) {
            m.getGuild().removeRoleFromMember(m,role).queue();
        }
    }

    public Member getMemberFromPlayer(ProxiedPlayer p) throws SQLException {
        User u;
        u = Main.jda.getUserById(Long.parseLong(VerifyDAO.INSTANCE.getDiscordID(p)));
        Member m = null;
        if (!Main.jda.getGuilds().isEmpty()) {
            for (Guild guild : Main.jda.getGuilds()) {
                if (u != null)
                    m = guild.getMember(u);
            }
        } else {
            throw new SQLException("There was an internal error! But may not found") ;
        }
        return m;
    }
}
