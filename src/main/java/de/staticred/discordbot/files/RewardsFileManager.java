package de.staticred.discordbot.files;

import de.staticred.discordbot.Main;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class RewardsFileManager {

    public static RewardsFileManager INSTANCE = new RewardsFileManager();

    private File file = new File(Main.getInstance().getDataFolder().getAbsolutePath(), "rewards.yml");
    private Configuration conf;

    public void loadFile() {

        if(!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
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
