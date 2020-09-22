package de.staticred.discordbot.files;

import de.staticred.discordbot.DBVerifier;
import de.staticred.discordbot.util.GroupInfo;
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
    private File file = new File(DBVerifier.getInstance().getDataFolder().getAbsolutePath(), "discord.yml");
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

    public void deleteGroup(String group) {
        List<String> groups = getAllGroups();
        groups.remove(group);
        conf.set("Groups",groups);
        conf.set(group,null);
        saveFile();
    }

    public String getPrefix(String group) {
        return conf.getString(group + ".prefix");
    }

    public @Nullable List<String> getAllGroups()  {
        return conf.getStringList("Groups");
    }

    public List<String> getGroupInfo(String group) {
        return conf.getStringList(group);
    }

    public boolean doesGroupExist(String group) {
        return conf.contains(group);
    }

    public void generateGroupConfig() {
        for(String string : getAllGroups()) {
            conf.set(string + ".groupName", string);
            conf.set(string + ".permission", "perm." + string);
            conf.set(string + ".dynamicGroup",false);
            conf.set(string + ".prefix",string + " | %player%");
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

    public void createGroup(GroupInfo info) {
        List<String> groups = getAllGroups();
        groups.add(info.getName());
        conf.set("Groups",groups);
        String name = info.getName();

        if(ConfigFileManager.INSTANCE.useTokens()) {
            conf.set(name + ".groupName",Long.parseLong(info.getDiscordGroup()));
        }else{
            conf.set(name + ".groupName",(info.getDiscordGroup()));
        }

        conf.set(name + ".permission", info.getPermission());
        conf.set(name + ".dynamicGroup",info.isDynamic());
        conf.set(name + ".prefix",info.getPrefix());
        saveFile();
    }

    public String getPermissionsForGroup(String group) {
        return conf.getString(group + ".permission");
    }

    public String getDiscordGroupNameForGroup(String group) {
        return conf.getString(group + ".groupName");
    }

    public String getDiscordGroupIDForGroup(String group) {
        return conf.getString(group + ".groupName");
    }

    public boolean isDynamicGroup(String group) {
        return conf.getBoolean(group + ".dynamicGroup");
    }

    public @Nullable String discordGroupNameToConfigGroupName(String name) {
        for(String group : getAllGroups()) {
            if(getDiscordGroupNameForGroup(group).equals(name)) return group;
        }
        return null;
    }

    public @Nullable String getConfigGroupForPermission(String permission) {

        Map<String, String> map = getAllGroupPermissions();

        for(String groups : map.keySet()) {
            if(DiscordFileManager.INSTANCE.getPermissionsForGroup(groups).equalsIgnoreCase(permission)) return (groups);
        }

        return null;

    }

    public void update() {
        for(String group : getAllGroups()) {
            if(!conf.contains(group + ".prefix")) {
                conf.set(group + ".prefix", group + " | %name%");
            }
        }
    }

    public List<String> getAllDiscordGroups() {

        List<String> string = new ArrayList<>();

        for(String group : getAllGroups()) {
            if(ConfigFileManager.INSTANCE.useTokens()) {
                string.add((getDiscordGroupIDForGroup(group)));
            }else{
                string.add(getDiscordGroupNameForGroup(group));
            }
        }
        return string;
    }

    public List<String> getDynamicGroups() {
        List<String> list = new ArrayList<>();
        for(String group : getAllGroups()) {
            if(isDynamicGroup(group)) list.add(group);
        }
        return list;
    }


}
