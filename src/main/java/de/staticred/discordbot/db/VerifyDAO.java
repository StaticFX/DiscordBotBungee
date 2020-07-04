package de.staticred.discordbot.db;

import de.staticred.discordbot.Main;
import de.staticred.discordbot.files.VerifyFileManager;
import net.dv8tion.jda.api.entities.Member;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class VerifyDAO {

    public static VerifyDAO INSTANCE = new VerifyDAO();

    private boolean sql = Main.getInstance().useSQL;

    public boolean isPlayerInDataBase(ProxiedPlayer p) throws SQLException {

        if(!sql) return VerifyFileManager.INSTANCE.isPlayerInFile(p);

        DataBaseConnection con = DataBaseConnection.INSTANCE;
        con.connect();
        PreparedStatement ps = con.getConnection().prepareStatement("SELECT * FROM verify WHERE UUID = ?");
        ps.setString(1,p.getUniqueId().toString());

        ResultSet rs = ps.executeQuery();

        if(rs.next()) {
            rs.close();
            ps.close();
            con.closeConnection();
            return true;
        }
        con.closeConnection();
        ps.close();
        rs.close();
        return false;
    }


    public String getName(String discordID) throws SQLException {
        if(!sql) return VerifyFileManager.INSTANCE.getName(discordID);

        DataBaseConnection con = DataBaseConnection.INSTANCE;
        con.connect();
        PreparedStatement ps = con.getConnection().prepareStatement("SELECT * FROM verify WHERE DiscordID = ?");
        ps.setString(1,discordID);

        ResultSet rs = ps.executeQuery();

        if(rs.next()) {
            String name = rs.getString("Name");
            rs.close();
            ps.close();
            con.closeConnection();
            return name;
        }
        con.closeConnection();
        ps.close();
        rs.close();
        return null;
    }

    public void addPlayerAsUnverified(ProxiedPlayer player) throws SQLException {

        if(!sql) {
            VerifyFileManager.INSTANCE.addPlayerAsUnverified(player);
            return;
        }

        DataBaseConnection con = DataBaseConnection.INSTANCE;
        con.connect();
        con.executeUpdate("INSERT INTO verify(UUID,Name,Verified,DiscordID) VALUES(?,?,?,?)", player.getUniqueId().toString(), player.getName(), Main.getInstance().getRank(player) ,false,null);
        con.closeConnection();
    }

    public String getDiscordID(UUID uuid) throws SQLException {

        if(!sql) {
            return  VerifyFileManager.INSTANCE.getDiscordID(uuid);
        }

        DataBaseConnection con = DataBaseConnection.INSTANCE;
        con.connect();
        PreparedStatement ps = con.getConnection().prepareStatement("SELECT * FROM verify WHERE UUID = ?");
        ps.setString(1,uuid.toString());

        ResultSet rs = ps.executeQuery();

        if(rs.next()) {
            String discordID = rs.getString("DiscordID");
            rs.close();
            ps.close();
            con.closeConnection();
            return discordID;
        }
        con.closeConnection();
        ps.close();
        rs.close();
        return null;
    }

    public boolean hasDiscordID(ProxiedPlayer p) throws SQLException {

        if(!sql) {
            return VerifyFileManager.INSTANCE.hasDiscordID(p);
        }

        DataBaseConnection con = DataBaseConnection.INSTANCE;
        con.connect();
        PreparedStatement ps = con.getConnection().prepareStatement("SELECT * FROM verify WHERE UUID = ?");
        ps.setString(1,p.getUniqueId().toString());

        ResultSet rs = ps.executeQuery();

        if(rs.next()) {
            String discordID = rs.getString("DiscordID");
            rs.close();
            ps.close();
            con.closeConnection();
            return (discordID != null);
        }
        con.closeConnection();
        ps.close();
        rs.close();
        return false;
    }

    public void addDiscordID(ProxiedPlayer player, Member member) throws SQLException {

        if(!sql) {
            VerifyFileManager.INSTANCE.addDiscordID(player, member.getId());
            return;
        }

        DataBaseConnection con = DataBaseConnection.INSTANCE;
        con.connect();
        con.executeUpdate("UPDATE verify SET DiscordID = ? WHERE UUID = ?", member.getUser().getId(), player.getUniqueId().toString());
        con.closeConnection();
    }

    public void removeDiscordID(ProxiedPlayer player) throws SQLException {

        if(!sql) {
            VerifyFileManager.INSTANCE.removeDiscordID(player);
            return;
        }

        DataBaseConnection con = DataBaseConnection.INSTANCE;
        con.connect();
        con.executeUpdate("UPDATE verify SET DiscordID = ? WHERE UUID = ?",null, player.getUniqueId().toString());
        con.closeConnection();
    }

    public void setPlayerAsVerified(UUID uuid) throws SQLException {

        if(!sql) {
            VerifyFileManager.INSTANCE.setVerifiedState(uuid,true);
            return;
        }

        DataBaseConnection con = DataBaseConnection.INSTANCE;
        con.connect();
        con.executeUpdate("UPDATE verify SET Verified = true WHERE UUID = ?", uuid.toString());
        con.closeConnection();
    }

    public void setPlayerAsUnVerified(UUID uuid) throws SQLException {

        if(!sql) {
            VerifyFileManager.INSTANCE.setVerifiedState(uuid,false);
            return;
        }

        DataBaseConnection con = DataBaseConnection.INSTANCE;
        con.connect();
        con.executeUpdate("UPDATE verify SET Verified = false WHERE UUID = ?", uuid.toString());
        con.closeConnection();
    }

    public void setPlayerAsUnverified(String discordID) throws SQLException {

        if(!sql) {
            VerifyFileManager.INSTANCE.setVerifiedState(discordID,false);
            return;
        }

        DataBaseConnection con = DataBaseConnection.INSTANCE;
        con.connect();
        con.executeUpdate("UPDATE verify SET Verified = false WHERE DiscordID = ?", discordID);
        con.closeConnection();
    }

    public void removeDiscordIDByDiscordID(Member m) throws SQLException {

        if(!sql) {
            VerifyFileManager.INSTANCE.removeDiscordID(m.getId());
            return;
        }

        DataBaseConnection con = DataBaseConnection.INSTANCE;
        con.connect();
        con.executeUpdate("UPDATE verify SET Verified = false, DiscordID = ? WHERE DiscordID = ?", null,m.getId());
        con.closeConnection();
    }



    public void updateUserName(ProxiedPlayer player) throws SQLException {

        if(!sql) {
            VerifyFileManager.INSTANCE.updateUserName(player);
            return;
        }

        DataBaseConnection con = DataBaseConnection.INSTANCE;
        con.connect();
        con.executeUpdate("UPDATE verify SET Name = ? WHERE UUID = ?", player.getName(), player.getUniqueId().toString());
        con.closeConnection();
    }



    public boolean isPlayerVerified(UUID uuid) throws SQLException {

        if(!sql) {
            return VerifyFileManager.INSTANCE.isPlayerVerified(uuid);
        }

        DataBaseConnection con = DataBaseConnection.INSTANCE;
        con.connect();
        PreparedStatement ps = con.getConnection().prepareStatement("SELECT * FROM verify WHERE UUID = ?");
        ps.setString(1,uuid.toString());

        ResultSet rs = ps.executeQuery();

        if(rs.next()) {
            boolean online = rs.getBoolean("Verified");
            rs.close();
            ps.close();
            con.closeConnection();
            return online;
        }
        con.closeConnection();
        ps.close();
        rs.close();
        return false;
    }

    public boolean isDiscordIDInUse(String discordID) throws SQLException {


        if(!sql) {
            return VerifyFileManager.INSTANCE.isIsDiscordIDInUse(discordID);
        }

        DataBaseConnection con = DataBaseConnection.INSTANCE;
        con.connect();
        PreparedStatement ps = con.getConnection().prepareStatement("SELECT * FROM verify WHERE DiscordID = ?");
        ps.setString(1,discordID);

        ResultSet rs = ps.executeQuery();

        if(rs.next()) {
            rs.close();
            ps.close();
            con.closeConnection();
            return true;
        }
        con.closeConnection();
        ps.close();
        rs.close();
        return false;
    }

    public String getUUIDByDiscordID(String discordID) throws SQLException {

        if(!sql) {
            return VerifyFileManager.INSTANCE.getUUIDFromDiscordID(discordID);
        }

        DataBaseConnection con = DataBaseConnection.INSTANCE;
        con.connect();
        PreparedStatement ps = con.getConnection().prepareStatement("SELECT * FROM verify WHERE DiscordID = ?");
        ps.setString(1,discordID);

        ResultSet rs = ps.executeQuery();

        if(rs.next()) {
            String uuid = rs.getString("UUID");
            rs.close();
            ps.close();
            con.closeConnection();
            return uuid;
        }
        con.closeConnection();
        ps.close();
        rs.close();
        return null;

    }

}
