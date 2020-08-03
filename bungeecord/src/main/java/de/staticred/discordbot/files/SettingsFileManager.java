package de.staticred.discordbot.files;

import de.staticred.discordbot.DBVerifier;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class SettingsFileManager {


    private File file = new File(DBVerifier.getInstance().getDataFolder().getAbsolutePath(), "internSettings.yml");
    private Configuration conf;
    public static SettingsFileManager INSTANCE = new SettingsFileManager();

    public void loadFile() {
        if(!file.exists()) {
            file.getParentFile().mkdirs();
            try(InputStream in = getClass().getClassLoader().getResourceAsStream("internSettings.yml")) {
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
    }

    public void saveFile() throws IOException {
        ConfigurationProvider.getProvider(YamlConfiguration.class).save(conf,file);
    }

    public void setSetup(boolean state) {
        conf.set("setup",state);
        try {
            saveFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void toggleDebug() {
        conf.set("debugmode",!conf.getBoolean("debugmode"));
        try {
            saveFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isDebug() {
        return conf.getBoolean("debugmode");
    }

    public boolean isSetup() {
        return conf.getBoolean("setup");
    }

}
