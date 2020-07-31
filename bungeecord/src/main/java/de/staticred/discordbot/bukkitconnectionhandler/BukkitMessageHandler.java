package de.staticred.discordbot.bukkitconnectionhandler;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.staticred.discordbot.DBVerifier;
import de.staticred.discordbot.util.Debugger;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;

public class BukkitMessageHandler implements Listener {


    public void sendPlayerVerified(ProxiedPlayer player) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Sending string: '" + "{\"name\":\"" + player.getName() +"\",\"uuid\":\"" + player.getUniqueId().toString() + "\"}" +  "' to bukkitsubserver: " + player.getServer().getInfo().getName());
        out.writeUTF( "verified");
        out.writeUTF("{\"name\":\"" + player.getName() +"\",\"uuid\":\"" + player.getUniqueId().toString() + "\"}");
        player.getServer().getInfo().sendData(DBVerifier.PLUGIN_CHANNEL_NAME, out.toByteArray());
    }

    public void sendPlayerUnlinked(ProxiedPlayer player) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF( "unlink");
        out.writeUTF("{\"name\":\"" + player.getName() +"\",\"uuid\":\"" + player.getUniqueId().toString() + "\"}");
        player.getServer().getInfo().sendData(DBVerifier.PLUGIN_CHANNEL_NAME, out.toByteArray());
    }

    @EventHandler
    public void onMessageReceived(PluginMessageEvent e) {

        String channel = e.getTag();

        if(!e.getTag().equals(DBVerifier.PLUGIN_CHANNEL_NAME)) return;

        ByteArrayDataInput in = ByteStreams.newDataInput(e.getData());
        String subChannel = in.readUTF();

        if(subChannel.equals("success")) {
            if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Received message from Bukkit-Subserver: " + e.getSender().getSocketAddress());
            String data = in.readUTF();
            if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Input: " + data);

            JSONObject object = new JSONObject(data);

            String name = object.getString("name");
            String uuid = object.getString("uuid");

            if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("name: " + name + "; uuid: " + uuid);


        }






    }

}
