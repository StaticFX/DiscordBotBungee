package de.staticred.discordbot.files;

import de.staticred.discordbot.DBVerifier;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class VerifyFileManager {

    public static VerifyFileManager INSTANCE = new VerifyFileManager();

    private File file = new File(DBVerifier.getInstance().getDataFolder().getAbsolutePath(), "verify.yml");
    private Configuration conf;

    public String getName(String discordID) {
        String uuid = getUUIDFromDiscordID(discordID);
        return conf.getString(uuid + ".name");
    }

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

    public UUID getUUIDByPlayerName(String name) {

        for(String group : conf.getKeys()) {
            if(UUID.fromString(group) != null) {
                if(getName(getDiscordID(UUID.fromString(group))).equals(name)) return UUID.fromString(group);
            }
        }
        return null;

    }

    public void removePlayerData(UUID uuid) {
        conf.set(uuid.toString(), null);
        saveFile();
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
        conf.set(uuid + ".rewarded",false);
        updateRank(p);
        saveFile();
    }

    public String getDiscordID(UUID uuid) {
        return conf.getString(uuid.toString() + ".discordid");
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
        removeDiscordIDUUIDLink(getDiscordID(p.getUniqueId()));
        conf.set(p.getUniqueId().toString() + ".discordid","nothing");
        saveFile();
    }

    public void setRewardState(UUID uuid, boolean state) {
        conf.set(uuid.toString() + ".rewarded",state);
        saveFile();
    }


    public void setVerifiedState(UUID uuid, boolean verified) {
        conf.set(uuid.toString() + ".verified",verified);
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

        if(!conf.contains(p.getUniqueId().toString() + ".rewarded")) {
            conf.set(p.getUniqueId().toString() + ".rewarded", false);
        }

        saveFile();
    }

    public void updateRank(ProxiedPlayer p) {
        conf.set(p.getUniqueId().toString() + ".rank", DBVerifier.getInstance().getRank(p));
        saveFile();
    }

    public boolean isPlayerVerified(UUID uuid) {
        return conf.getBoolean(uuid.toString() + ".verified");
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


    public boolean getRewardState(UUID uuid) {
        return conf.getBoolean(uuid.toString() + ".rewarded");
    }

    public int getAmountOfVerifiedPlayers() {
        int amount = 0;
        for(String key : conf.getKeys()) {
            if(isPlayerVerified(UUID.fromString(key))) {
                amount += 0;
            }
        }

        return amount;
    }
}
