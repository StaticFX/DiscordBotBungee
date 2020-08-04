package de.staticred.discordbot.files;

import de.staticred.discordbot.DBVerifier;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.json.JSONObject;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;

public class DiscordMessageFileManager {

    private File file = new File(DBVerifier.getInstance().getDataFolder().getAbsolutePath() + "/messages", "DiscordMessages.yml");
    private Configuration conf;
    public static DiscordMessageFileManager INSTANCE = new DiscordMessageFileManager();

    public void loadFile() {
        if(!file.exists()) {
            file.getParentFile().mkdirs();
            try(InputStream in = getClass().getClassLoader().getResourceAsStream("DiscordMessages.yml")) {
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


    public MessageEmbed getEmbed(String path, String name) {
        JSONObject object = new JSONObject(getString(path).replaceAll("%name%",name));
        String title = object.getString("title");
        String description = object.getString("description");
        String color = object.getString("color");

        Color colorObj;
        try {
            Field field = Class.forName("java.awt.Color").getField(color);
            colorObj = (Color) field.get(null);
        } catch (Exception e) {
            colorObj = null; // Not defined
        }

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(title);
        builder.setDescription(description);
        builder.setColor(colorObj);

        return builder.build();
    }

    public MessageEmbed getEmbed(String path) {
        JSONObject object = new JSONObject(getString(path));
        String title = object.getString("title");
        String description = object.getString("description");
        String color = object.getString("color");

        Color colorObj;
        try {
            Field field = Class.forName("java.awt.Color").getField(color);
            colorObj = (Color) field.get(null);
        } catch (Exception e) {
            colorObj = null; // Not defined
        }

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(title);
        builder.setDescription(description);
        builder.setColor(colorObj);

        return builder.build();
    }

    public MessageEmbed getEmbedInformationMember(String path, String name, String id, String uuid, String memberName, String memberTag) {
        JSONObject object = new JSONObject(getString(path).replaceAll("%name%",name).replaceAll("%id%", id).replaceAll("%uuid%",uuid).replaceAll("%memberName%",memberName).replaceAll("%memberAsTag%",memberTag));
        String title = object.getString("title");
        String description = object.getString("description");
        String color = object.getString("color");
        String footer = object.getString("footer");

        Color colorObj;
        try {
            Field field = Class.forName("java.awt.Color").getField(color);
            colorObj = (Color) field.get(null);
        } catch (Exception e) {
            colorObj = null; // Not defined
        }

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(title);
        builder.setDescription(description);
        builder.setColor(colorObj);
        builder.setFooter(footer);

        return builder.build();
    }

    public MessageEmbed getEmbedInformationPlayer(String path, String name, String id, String uuid, String tagline, String member) {
        JSONObject object = new JSONObject(getString(path).replaceAll("%name%",name).replaceAll("%id%", id).replaceAll("%uuid%",uuid).replaceAll("%tag%",tagline).replaceAll("%memberAsTag%",member));
        String title = object.getString("title");
        String description = object.getString("description");
        String color = object.getString("color");
        String footer = object.getString("footer");


        Color colorObj;
        try {
            Field field = Class.forName("java.awt.Color").getField(color);
            colorObj = (Color) field.get(null);
        } catch (Exception e) {
            colorObj = null; // Not defined
        }

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(title);
        builder.setDescription(description);
        builder.setColor(colorObj);
        builder.setFooter(footer);

        return builder.build();
    }


    public String getString(String path) {
        return conf.getString(path);
    }

    public String getVersion() {
        return conf.getString("version");
    }

}
