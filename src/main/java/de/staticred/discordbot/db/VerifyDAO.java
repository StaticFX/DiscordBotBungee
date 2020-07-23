package de.staticred.discordbot.db;

import de.staticred.discordbot.DBVerifier;
import de.staticred.discordbot.files.ConfigFileManager;
import de.staticred.discordbot.files.VerifyFileManager;
import de.staticred.discordbot.util.Debugger;
import net.dv8tion.jda.api.entities.Member;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.UUID;

public class VerifyDAO {

    public static VerifyDAO INSTANCE = new VerifyDAO();

    private boolean sql = ConfigFileManager.INSTANCE.useSQL();


    public void loadDataBase() {
        DataBaseConnection con = DataBaseConnection.INSTANCE;
        try {

            con.connect();
            con.executeUpdate("CREATE TABLE IF NOT EXISTS verify(UUID VARCHAR(36) PRIMARY KEY, PlayerName VARCHAR(16), Verified BOOLEAN, DiscordID VARCHAR(100), Version VARCHAR(10))");
            con.closeConnection();

            //update database to newest version
            if(getDataBaseVersion() == null || !doesColumnExist("Version")) {
                Debugger.debugMessage("DataBaseVersion outdated, trying to auto update the Database");

                if(!doesColumnExist("Version")) {
                    con.connect();
                    con.executeUpdate("ALTER TABLE verify ADD Version VARCHAR(10)");
                    con.closeConnection();
                }

                if(hasUsersInDataBase()) {
                    con.connect();
                    con.executeUpdate("UPDATE verify SET Version = ?", DBVerifier.DATABASE_VERSION);
                    con.closeConnection();
                }

                if(doesColumnExist("rank")) {
                    con.connect();
                    con.executeUpdate("ALTER TABLE verify DROP COLUMN rank");
                    con.closeConnection();
                }


                if(doesColumnExist("Name")) {
                    con.connect();
                    con.executeUpdate("ALTER TABLE verify CHANGE Name PlayerName VARCHAR(16)");
                    con.closeConnection();
                }

            }

            if(getDataBaseVersion() != null && !getDataBaseVersion().equals(DBVerifier.DATABASE_VERSION)) {
                //update for the future
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DiscordVerify] SQL Connect test failed! Please check your SQL Connection settings.");
        }

        Debugger.debugMessage("SQL Connect test success!");

        con.closeConnection();
    }

    public boolean doesColumnExist(String column) throws SQLException {
        DataBaseConnection con = DataBaseConnection.INSTANCE;
        con.connect();
        PreparedStatement ps = con.getConnection().prepareStatement("SELECT * FROM verify");

        ResultSet rs = ps.executeQuery();
        ResultSetMetaData rsmd = rs.getMetaData();
        int columns = rsmd.getColumnCount();

        for (int x = 1; x <= columns; x++) {
            if (column.equals(rsmd.getColumnName(x))) {
                rs.close();
                ps.close();
                con.closeConnection();
                return true;
            }
        }
        con.closeConnection();
        ps.close();
        rs.close();
        return false;
    }

    public boolean hasUsersInDataBase() throws SQLException {
        DataBaseConnection con = DataBaseConnection.INSTANCE;
        con.connect();
        PreparedStatement ps = con.getConnection().prepareStatement("SELECT * FROM verify");

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

    public String getDataBaseVersion() {
        DataBaseConnection con = DataBaseConnection.INSTANCE;
        con.connect();

        try {
            PreparedStatement ps = con.getConnection().prepareStatement("SELECT * FROM verify");

            ResultSet rs = ps.executeQuery();

            if(rs.next()) {
                String version = rs.getString("Version");
                rs.close();
                ps.close();
                con.closeConnection();
                return version;
            }
            con.closeConnection();
            ps.close();
            rs.close();
            return null;
        }catch (Exception e) {
            return null;
        }
    }

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
        ps.close();
        rs.close();
        con.closeConnection();
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

    public void removePlayerData(UUID uuid) throws SQLException {
        if(!sql) {
            VerifyFileManager.INSTANCE.removePlayerData(uuid);
            return;
        }

        DataBaseConnection con = DataBaseConnection.INSTANCE;
        con.connect();
        con.executeUpdate("DELETE FROM verify WHERE UUID = ?",uuid.toString());
        con.closeConnection();
    }

    public void addPlayerAsUnverified(ProxiedPlayer player) throws SQLException {

        if(!sql) {
            VerifyFileManager.INSTANCE.addPlayerAsUnverified(player);
            return;
        }

        DataBaseConnection con = DataBaseConnection.INSTANCE;
        con.connect();
        con.executeUpdate("INSERT INTO verify(UUID,PlayerName,Verified,DiscordID,Version) VALUES(?,?,?,?,?)", player.getUniqueId().toString(), player.getName() ,false,null, DBVerifier.DATABASE_VERSION);

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
        con.executeUpdate("UPDATE verify SET PlayerName = ? WHERE UUID = ?", player.getName(), player.getUniqueId().toString());
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
