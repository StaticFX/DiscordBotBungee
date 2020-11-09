package de.staticred.dbverifier;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import github.scarsz.discordsrv.DiscordSRV;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.UUID;

public class DBVerifier extends JavaPlugin implements PluginMessageListener {

    DiscordSRV srv;
    YamlConfiguration conf;

    //the name of the channel which is used to communicate with the bukkit subserver for a discordsrv connection
    public static final String PLUGIN_CHANNEL_NAME = "dbv:bungeecord";

    @Override
    public void onEnable() {

        loadConfig();

        if(conf.getBoolean("useSRV")) {
            srv = DiscordSRV.getPlugin();
        }



        getServer().getMessenger().registerOutgoingPluginChannel(this, PLUGIN_CHANNEL_NAME);
        getServer().getMessenger().registerIncomingPluginChannel(this, PLUGIN_CHANNEL_NAME, this);

    }


    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] message) {

        if(!channel.equals(PLUGIN_CHANNEL_NAME)) return;

        ByteArrayDataInput in = ByteStreams.newDataInput(message);

        String subchannel = in.readUTF();
        String data = in.readUTF();

        System.out.println("Received Data by BungeeCord: " + data + " SubChannel: " + subchannel);

        if (subchannel.equals("verified")) {
            JSONObject jsonObject = new JSONObject(data);

            String name = jsonObject.getString("name");
            String uuid = jsonObject.getString("uuid");
            String discordID = jsonObject.getString("discordID");

            if(srv.getAccountLinkManager().getDiscordId(UUID.fromString(uuid)) != null && srv.getAccountLinkManager().getDiscordId(UUID.fromString(uuid)).equals(discordID)){
                return;
            }

            srv.getAccountLinkManager().link(discordID, UUID.fromString(uuid));
            System.out.println("Linked " + name + " in srv");
        }
        if(subchannel.equals("unlink")) {
            JSONObject jsonObject = new JSONObject(data);

            String name = jsonObject.getString("name");
            String uuid = jsonObject.getString("uuid");

            srv.getAccountLinkManager().unlink(UUID.fromString(uuid));
            System.out.println("unlinked " + name + " in srv");
        }

        if(subchannel.equals("request")) {
            JSONObject jsonObject = new JSONObject(data);
            String uuid = jsonObject.getString("uuid");

            boolean verified = srv.getAccountLinkManager().getDiscordId(UUID.fromString(uuid)) == null;
            sendToBungee(player, "requestAnswer", "{\"uuid\": \"" + uuid + "\", \"verified\": " + verified + "}");


        }

        if(subchannel.equals("command")) {
            JSONObject jsonObject = new JSONObject(data);
            String uuid = jsonObject.getString("uuid");
            String command = jsonObject.getString("command");
            String name = jsonObject.getString("name");
            System.out.println("Received command from bungeecord: " + command);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),command.replaceAll("%player%",name));
        }

        if (subchannel.equals("test")) {
            sendToBungee(player,"test","{\"test\":true}");
        }
    }


    public void sendToBungee(Player player, String subServer, String data) {

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(subServer);
        out.writeUTF(data);

        // If you don't care about the player
        // Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
        // Else, specify them
        player.sendPluginMessage(this, PLUGIN_CHANNEL_NAME, out.toByteArray());

    }

    public void loadConfig() {
        File file = new File(getDataFolder().getAbsolutePath(), "config.yml");
        if(!file.exists()) {
            file.getParentFile().mkdirs();
            try(InputStream in = getClass().getClassLoader().getResourceAsStream("config.yml")) {
                Files.copy(in,file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        conf = YamlConfiguration.loadConfiguration(file);
    }

}
