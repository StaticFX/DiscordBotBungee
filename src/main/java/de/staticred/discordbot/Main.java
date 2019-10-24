package de.staticred.discordbot;

import de.staticred.discordbot.bungeecommands.MCVerifyCommandExecutor;
import de.staticred.discordbot.bungeeevents.JoinEvent;
import de.staticred.discordbot.bungeeevents.LeaveEvent;
import de.staticred.discordbot.db.DataBaseConnection;
import de.staticred.discordbot.discordevents.GuildJoinEvent;
import de.staticred.discordbot.discordevents.GuildLeftEvent;
import de.staticred.discordbot.discordevents.MessageEvent;
import de.staticred.discordbot.files.ConfigFileManager;
import de.staticred.discordbot.files.VerifyFileManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

import javax.security.auth.login.LoginException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

public class Main extends Plugin {

    public static  Main INSTANCE;
    public static HashMap<ProxiedPlayer, Member> playerMemberHashMap = new HashMap<>();
    public static HashMap<ProxiedPlayer, TextChannel> playerChannelHashMap = new HashMap<>();
    public boolean useSQL;


    public  static JDA jda;

    @Override
    public void onEnable() {

        INSTANCE = this;


        ConfigFileManager.INSTANCE.loadFile();
        VerifyFileManager.INSTANCE.loadFile();

        if(ConfigFileManager.INSTANCE.useSQL()) {
            loadDataBase();
        }

        useSQL = ConfigFileManager.INSTANCE.useSQL();
        String token = ConfigFileManager.INSTANCE.getString("bot-token");
        loadBungeeEvents();




        String activity = ConfigFileManager.INSTANCE.getString("discordBotActivityType");
        String type = ConfigFileManager.INSTANCE.getString("discordBotActivity");
        String link = ConfigFileManager.INSTANCE.getString("streamingLink");
        Activity activity1;



        if(activity.equalsIgnoreCase("listening")) {
            activity1 = Activity.listening(type);
        }else if(activity.equalsIgnoreCase("playing")) {
            activity1 = Activity.playing(type);
        }else if(activity.equalsIgnoreCase("streaming")) {
            activity1 = Activity.streaming(type,link);
        }else if(activity.equalsIgnoreCase("watching")) {
            activity1 = Activity.watching(type);
        }else {
            activity1 = Activity.playing(type);
        }

        String command = ConfigFileManager.INSTANCE.getString("verifycommand");

        loadBungeeCommands(command);


        try {
            initBot(token,activity1);
        } catch (LoginException e) {
            e.printStackTrace();
            System.out.println("[DiscordVerify] Bot can´t connect!");
            return;
        }




    }

    public void loadBungeeCommands(String command) {
        getProxy().getPluginManager().registerCommand(this,new MCVerifyCommandExecutor(command));
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
            return ConfigFileManager.INSTANCE.getString("prefix").replaceAll("&","§") + ConfigFileManager.INSTANCE.getString(string).replaceAll("&","§");
        return ConfigFileManager.INSTANCE.getString(string).replaceAll("&","§");
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

        boolean ids = ConfigFileManager.INSTANCE.useTokens();

        if (p.hasPermission("db.verified")) {
            if (ids) {
                m.getGuild().addRoleToMember(m, ConfigFileManager.INSTANCE.getRoleById("verifiedName")).queue();
            } else {
                m.getGuild().addRoleToMember(m, ConfigFileManager.INSTANCE.getRoleByName("verifiedName")).queue();
            }
        }


        if (p.hasPermission("db.admin")) {
            if (ids) {
                m.getGuild().addRoleToMember(m, ConfigFileManager.INSTANCE.getRoleById("adminName")).queue();
            } else {
                m.getGuild().addRoleToMember(m, ConfigFileManager.INSTANCE.getRoleByName("adminName")).queue();
            }
            return;
        }

        if (p.hasPermission("db.discordstaff")) {
            if (ids) {
                m.getGuild().addRoleToMember(m, ConfigFileManager.INSTANCE.getRoleById("discord-staffName")).queue();
            } else {
                m.getGuild().addRoleToMember(m, ConfigFileManager.INSTANCE.getRoleByName("discord-staffName")).queue();
            }
            return;
        }

        if (p.hasPermission("db.staff")) {
            if (ids) {
                m.getGuild().addRoleToMember(m, ConfigFileManager.INSTANCE.getRoleById("friendName")).queue();
            } else {
                m.getGuild().addRoleToMember(m, ConfigFileManager.INSTANCE.getRoleByName("friendName")).queue();
            }
            return;
        }

        if (p.hasPermission("db.friend")) {
            if (ids) {
                m.getGuild().addRoleToMember(m, ConfigFileManager.INSTANCE.getRoleById("friendName")).queue();
            } else {
                m.getGuild().addRoleToMember(m, ConfigFileManager.INSTANCE.getRoleByName("friendName")).queue();
            }
            return;
        }


        if (p.hasPermission("db.youtuber")) {
            if (ids) {
                m.getGuild().addRoleToMember(m, ConfigFileManager.INSTANCE.getRoleById("youtuberName")).queue();
            } else {
                m.getGuild().addRoleToMember(m, ConfigFileManager.INSTANCE.getRoleByName("youtuberName")).queue();
            }
            return;
        }


        if (p.hasPermission("db.vip")) {
            if (ids) {
                m.getGuild().addRoleToMember(m, ConfigFileManager.INSTANCE.getRoleById("vipName")).queue();
            } else {
                m.getGuild().addRoleToMember(m, ConfigFileManager.INSTANCE.getRoleByName("vipName")).queue();
            }
        }

    }



    public String getRank(ProxiedPlayer p) {

        String rank = "";

        if (p.hasPermission("db.admin")) {
            rank = "admin";
        } else

        if (p.hasPermission("db.discordstaff")) {
            rank = "dcstaff";
        } else

        if (p.hasPermission("db.staff")) {
            rank = "staff";
        } else

        if (p.hasPermission("db.friend")) {
            rank = "friend";
        } else


        if (p.hasPermission("db.youtuber")) {
            rank = "youtuber";
        }else


        if (p.hasPermission("db.vip")) {
            rank = "vip";
        }else {
            rank = "none";
        }

        return rank;

    }




    public void removeAllRolesFromMember(Member m) {
        for(Role role : m.getRoles()) {
            m.getGuild().removeRoleFromMember(m,role).queue();
        }
    }
}
