package de.staticred.discordbot.files;

import de.staticred.discordbot.Main;
import net.dv8tion.jda.api.entities.Role;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class ConfigFileManager {

    private File file = new File(Main.getInstance().getDataFolder().getAbsolutePath(), "config.yml");
    private Configuration conf;
    public static ConfigFileManager INSTANCE = new ConfigFileManager();

    public void loadFile() {
        if(!file.exists()) {
            file.getParentFile().mkdirs();
            try(InputStream in = getClass().getClassLoader().getResourceAsStream("config.yml")) {
                Files.copy(in,file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            conf = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        } catch (IOException e) {
            e.printStackTrace();
        }



        saveFile();
    }


    public void saveFile() {
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(conf,file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String getString(String path) {
        return conf.getString(path);
    }

    public String getUser() {
        return conf.getString("SQL_USER");
    }

    public String getPassword() {
        return conf.getString("SQL_PASSWORD");
    }

    public String getDataBase() {
        return conf.getString("SQL_DATABASE");
    }


    public boolean useSQL() {
        return conf.getBoolean("UseSQL");
    }

    public boolean getSyncName() { return conf.getBoolean("syncNickName");}

    public String getHost() {
        return conf.getString("SQL_HOST");
    }

    public int getPort() {
        return conf.getInt("SQL_PORT");
    }
    
    public boolean useSRV() {
        return conf.getBoolean("USE_DISCORDSRV");
    }

    public boolean isSetuped() {
        return conf.getBoolean("Setuped");
    }

    public void setSetuped(boolean setuped) {
        conf.set("Setuped",setuped);
        saveFile();
    }

    public String getConfigVersion() {
        return conf.getString("configVersion") ;
    }

    public String getVerifyChannel() {
        return conf.getString("channelID");
    }

    public String getVerifyRole() {
        return conf.getString("verifyRole");
    }

    public long getVerifyRoleAsLong() {
        return conf.getLong("verifyRole");
    }

    public boolean useTokens() {
        return conf.getBoolean("useTokens");
    }

}
