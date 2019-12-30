package de.staticred.discordbot.files;

import de.staticred.discordbot.Main;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class DiscordFileManager {
    private File file = new File(Main.getInstance().getDataFolder().getAbsolutePath(), "discord.yml");
    private Configuration conf;
    public static DiscordFileManager INSTANCE = new DiscordFileManager();


    public void loadFile() {
        if(!file.exists()) {
            file.getParentFile().mkdirs();
            try(InputStream in = getClass().getClassLoader().getResourceAsStream("discord.yml")) {
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


}
