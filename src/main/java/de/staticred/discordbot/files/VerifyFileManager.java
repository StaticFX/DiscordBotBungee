package de.staticred.discordbot.files;

import de.staticred.discordbot.Main;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class VerifyFileManager {

    public static VerifyFileManager INSTANCE = new VerifyFileManager();

    private File file = new File(Main.getInstance().getDataFolder().getAbsolutePath(), "verify.yml");
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

        saveFile();

    }


    public void saveFile() {
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(conf,file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public boolean isPlayerInFile(ProxiedPlayer p) {
        return conf.getKeys().contains(p.getUniqueId().toString());
    }

    public void addPlayerAsUnverified(ProxiedPlayer p) {
        String uuid = p.getUniqueId().toString();
        conf.set(uuid + ".uuid",uuid);
        conf.set(uuid + ".name",p.getName());
        conf.set(uuid + ".discordid","nothing");
        conf.set(uuid + ".verified",false);
        updateRank(p);
        saveFile();
    }

    public String getDiscordID(ProxiedPlayer p) {
        return conf.getString(p.getUniqueId().toString() + ".discordid");
    }

    public boolean hasDiscordID(ProxiedPlayer p) {
        return !conf.getString(p.getUniqueId().toString() + ".discordid").equals("nothing");
    }

    public boolean isIsDiscordIDInUse(String discordID) {
        return hasDiscordID(discordID);
    }

    public void addDiscordID(ProxiedPlayer p, String discordID) {
        addDiscordIDToUUID(discordID,p.getUniqueId());
        conf.set(p.getUniqueId().toString() + ".discordid",discordID);
        saveFile();
    }

    public void removeDiscordID(ProxiedPlayer p) {
        removeDiscordIDUUIDLink(getDiscordID(p));
        conf.set(p.getUniqueId().toString() + ".discordid","nothing");
        saveFile();
    }


    public void setVerifiedState(ProxiedPlayer p, boolean verified) {
        conf.set(p.getUniqueId().toString() + ".verified",verified);
        saveFile();
    }

    public void setVerifiedState(String discordID, boolean verified) {
        String uuid = getUUIDFromDiscordID(discordID);
        conf.set(uuid + ".verified",verified);
        saveFile();
    }

    public void removeDiscordID(String discordID) {
        String uuid = getUUIDFromDiscordID(discordID);
        conf.set(uuid + ".discordid","nothing");
        removeDiscordIDUUIDLink(discordID);
        saveFile();
    }

    public void updateUserName(ProxiedPlayer p) {
        conf.set(p.getUniqueId().toString() + ".name",p.getName());
        saveFile();
    }

    public void updateRank(ProxiedPlayer p) {
        conf.set(p.getUniqueId().toString() + ".rank",Main.getInstance().getRank(p));
        saveFile();
    }

    public boolean isPlayerVerified(ProxiedPlayer p) {
        return conf.getBoolean(p.getUniqueId().toString() + ".verified");
    }

    public void addDiscordIDToUUID(String discordID, UUID uuid) {
        conf.set(discordID,uuid.toString());
        saveFile();
    }

    public String getUUIDFromDiscordID(String discordID) {
        return conf.getString(discordID);
    }

    public void removeDiscordIDUUIDLink(String discordID) {
        conf.set(discordID,null);
        saveFile();
    }

    public boolean hasDiscordID(String discordID) {
        return conf.getKeys().contains(discordID);
    }





}
