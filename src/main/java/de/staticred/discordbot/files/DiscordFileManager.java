package de.staticred.discordbot.files;

import de.staticred.discordbot.Main;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;

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

    public @Nullable List<String> getAllGroups()  {
        return conf.getStringList("Groups");
    }

    public List<String> getGroupInfo(String group) {
        return conf.getStringList(group);
    }

    public void generateGroupConfig() {
        for(String string : getAllGroups()) {
            conf.set(string, Arrays.asList(string,"defaultPermission"));
        }
        saveFile();
    }

    public Map<String, String> getAllGroupPermissions() {
        Map<String,String> permissions = new HashMap<>();

        for(String string : getAllGroups()) {
            permissions.put(string,getPermissionsForGroup(string));
        }

        return permissions;
    }

    public String getPermissionsForGroup(String group) {
        return conf.getStringList(group).get(1);
    }

    public String getDiscordGrouNameForGroup(String group) {
        return conf.getStringList(group).get(0);
    }

    public Long getDiscordGroupIDForGroup(String group) {
        return Long.parseLong(conf.getStringList(group).get(1));
    }


}
