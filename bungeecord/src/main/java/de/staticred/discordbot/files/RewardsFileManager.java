package de.staticred.discordbot.files;

import de.staticred.discordbot.DBVerifier;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

public class RewardsFileManager {

    public static RewardsFileManager INSTANCE = new RewardsFileManager();

    private File file = new File(DBVerifier.getInstance().getDataFolder().getAbsolutePath(), "rewards.yml");
    private Configuration conf;

    public void loadFile() {

        if(!file.exists()) {
            file.getParentFile().mkdirs();
            try(InputStream in = getClass().getClassLoader().getResourceAsStream("rewards.yml")) {
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

    public List<String> getCommandsOnVerified() {
        return conf.getStringList("verified.commands");
    }

    public List<String> getCommandsOnUnVerified() {
        return conf.getStringList("unverified.commands");
    }

}
