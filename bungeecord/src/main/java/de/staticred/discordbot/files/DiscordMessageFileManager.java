package de.staticred.discordbot.files;

import de.staticred.discordbot.DBVerifier;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
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

        String title;
        String descirption;
        Color color;
        String footer;

        EmbedBuilder builder = new EmbedBuilder();

        if(object.has("title")) {
            title = object.getString("title");
            builder.setTitle(title);
        }
        if(object.has("description")) {
            descirption = object.getString("description");
            builder.setDescription(descirption);
        }
        if(object.has("color")) {
            String colorInput = object.getString("color");


            try {
                Field field = Class.forName("java.awt.Color").getField(colorInput);
                color = (Color) field.get(null);
            } catch (Exception e) {
                color = null; // Not defined
            }

            builder.setColor(color);
        }
        if(object.has("footer")) {
            footer = object.getString("footer");
            builder.setFooter(footer);
        }

        return builder.build();
    }

    public MessageEmbed getEmbed(String path, String name, Member meber) {
        JSONObject object = new JSONObject(getString(path).replaceAll("%name%",name).replaceAll("%member%",meber.getAsMention()));

        String title;
        String descirption;
        Color color;
        String footer;

        EmbedBuilder builder = new EmbedBuilder();

        if(object.has("title")) {
            title = object.getString("title");
            builder.setTitle(title);
        }
        if(object.has("description")) {
            descirption = object.getString("description");
            builder.setDescription(descirption);
        }
        if(object.has("color")) {
            String colorInput = object.getString("color");


            try {
                Field field = Class.forName("java.awt.Color").getField(colorInput);
                color = (Color) field.get(null);
            } catch (Exception e) {
                color = null; // Not defined
            }

            builder.setColor(color);
        }
        if(object.has("footer")) {
            footer = object.getString("footer");
            builder.setFooter(footer);
        }

        return builder.build();
    }

    public MessageEmbed getEmbed(String path) {
        JSONObject object = new JSONObject(getString(path));
        String title;
        String descirption;
        Color color;
        String footer;

        EmbedBuilder builder = new EmbedBuilder();

        if(object.has("title")) {
            title = object.getString("title");
            builder.setTitle(title);
        }
        if(object.has("description")) {
            descirption = object.getString("description");
            builder.setDescription(descirption);
        }
        if(object.has("color")) {
            String colorInput = object.getString("color");


            try {
                Field field = Class.forName("java.awt.Color").getField(colorInput);
                color = (Color) field.get(null);
            } catch (Exception e) {
                color = null; // Not defined
            }

            builder.setColor(color);
        }
        if(object.has("footer")) {
            footer = object.getString("footer");
            builder.setFooter(footer);
        }

        return builder.build();
    }

    public MessageEmbed getEmbed(String path, Member member) {
        JSONObject object = new JSONObject(getString(path).replaceAll("%member%",member.getAsMention()));
        String title;
        String descirption;
        Color color;
        String footer;

        EmbedBuilder builder = new EmbedBuilder();

        if(object.has("title")) {
            title = object.getString("title");
            builder.setTitle(title);
        }
        if(object.has("description")) {
            descirption = object.getString("description");
            builder.setDescription(descirption);
        }
        if(object.has("color")) {
            String colorInput = object.getString("color");


            try {
                Field field = Class.forName("java.awt.Color").getField(colorInput);
                color = (Color) field.get(null);
            } catch (Exception e) {
                color = null; // Not defined
            }

            builder.setColor(color);
        }
        if(object.has("footer")) {
            footer = object.getString("footer");
            builder.setFooter(footer);
        }

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
