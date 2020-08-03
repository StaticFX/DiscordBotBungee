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

            if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: VerifyDAO.loadDataBase 1");
            con.executeUpdate("CREATE TABLE IF NOT EXISTS verify(UUID VARCHAR(36) PRIMARY KEY, PlayerName VARCHAR(16), Verified BOOLEAN, DiscordID VARCHAR(100), Version VARCHAR(10))");

            //update database to newest version
            if(getDataBaseVersion() == null || !doesColumnExist("Version")) {
                Debugger.debugMessage("DataBaseVersion outdated, trying to auto update the Database");

                if(!doesColumnExist("Version")) {
                    if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: VerifyDAO.loadDataBase 2");
                    con.executeUpdate("ALTER TABLE verify ADD Version VARCHAR(10)");
                }

                if(hasUsersInDataBase()) {
                    if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: VerifyDAO.loadDataBase 3");
                    con.executeUpdate("UPDATE verify SET Version = ?", DBVerifier.DATABASE_VERSION);
                }

                if(doesColumnExist("rank")) {
                    if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: VerifyDAO.loadDataBase 4");
                    con.executeUpdate("ALTER TABLE verify DROP COLUMN rank");
                }


                if(doesColumnExist("Name")) {
                    if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: VerifyDAO.loadDataBase 5");
                    con.executeUpdate("ALTER TABLE verify CHANGE Name PlayerName VARCHAR(16)");
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

    }

    public int getAmountOfVerifiedPlayers() throws SQLException {

        if(!sql) return VerifyFileManager.INSTANCE.getAmountOfVerifiedPlayers();

        DataBaseConnection con = DataBaseConnection.INSTANCE;
        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: VerifyDAO.getAmoundOfVerifedPlayers");
        PreparedStatement ps = con.getConnection().prepareStatement("SELECT * FROM verify WHERE Verified = 1");
        ResultSet rs = ps.executeQuery();

        int amount = 0;

        while (rs.next()){
            amount += 1;
        }

        ps.close();
        rs.close();
        return amount;
    }

    public boolean doesColumnExist(String column) throws SQLException {
        DataBaseConnection con = DataBaseConnection.INSTANCE;
        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: VerifyDAO.doesColumnExist");
        PreparedStatement ps = con.getConnection().prepareStatement("SELECT * FROM verify");

        ResultSet rs = ps.executeQuery();
        ResultSetMetaData rsmd = rs.getMetaData();
        int columns = rsmd.getColumnCount();

        for (int x = 1; x <= columns; x++) {
            if (column.equals(rsmd.getColumnName(x))) {
                rs.close();
                ps.close();
                return true;
            }
        }
        ps.close();
        rs.close();
        return false;
    }

    public boolean hasUsersInDataBase() throws SQLException {
        DataBaseConnection con = DataBaseConnection.INSTANCE;
        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: VerifyDAO.hasUserInDataBase");
        PreparedStatement ps = con.getConnection().prepareStatement("SELECT * FROM verify");

        ResultSet rs = ps.executeQuery();

        if(rs.next()) {
            rs.close();
            ps.close();
            return true;
        }
        ps.close();
        rs.close();
        return false;
    }

    public String getDataBaseVersion() {
        DataBaseConnection con = DataBaseConnection.INSTANCE;
        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: VerifyDAO.getDataBaseVersion");

        try {
            PreparedStatement ps = con.getConnection().prepareStatement("SELECT * FROM verify");

            ResultSet rs = ps.executeQuery();

            if(rs.next()) {
                String version = rs.getString("Version");
                rs.close();
                ps.close();
                return version;
            }
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
        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: VerifyDAO.isPlayerInDatabase");
        PreparedStatement ps = con.getConnection().prepareStatement("SELECT * FROM verify WHERE UUID = ?");
        ps.setString(1,p.getUniqueId().toString());

        ResultSet rs = ps.executeQuery();

        if(rs.next()) {
            rs.close();
            ps.close();
            return true;
        }
        ps.close();
        rs.close();
        return false;
    }


    public String getName(String discordID) throws SQLException {
        if(!sql) return VerifyFileManager.INSTANCE.getName(discordID);

        DataBaseConnection con = DataBaseConnection.INSTANCE;
        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: VerifyDAO.getName");
        PreparedStatement ps = con.getConnection().prepareStatement("SELECT * FROM verify WHERE DiscordID = ?");
        ps.setString(1,discordID);

        ResultSet rs = ps.executeQuery();

        if(rs.next()) {
            String name = rs.getString("PlayerName");
            rs.close();
            ps.close();
            return name;
        }
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
        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: VerifyDAO.removePlayerData");
        con.executeUpdate("DELETE FROM verify WHERE UUID = ?",uuid.toString());
    }

    public void addPlayerAsUnverified(ProxiedPlayer player) throws SQLException {

        if(!sql) {
            VerifyFileManager.INSTANCE.addPlayerAsUnverified(player);
            return;
        }

        DataBaseConnection con = DataBaseConnection.INSTANCE;
        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: VerifyDAO.addPlayerAsUnverified");
        con.executeUpdate("INSERT INTO verify(UUID,PlayerName,Verified,DiscordID,Version) VALUES(?,?,?,?,?)", player.getUniqueId().toString(), player.getName() ,false,null, DBVerifier.DATABASE_VERSION);
    }

    public String getDiscordID(UUID uuid) throws SQLException {

        if(!sql) {
            return  VerifyFileManager.INSTANCE.getDiscordID(uuid);
        }

        DataBaseConnection con = DataBaseConnection.INSTANCE;
        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: VerifyDAO.getDiscordID");
        PreparedStatement ps = con.getConnection().prepareStatement("SELECT * FROM verify WHERE UUID = ?");
        ps.setString(1,uuid.toString());

        ResultSet rs = ps.executeQuery();

        if(rs.next()) {
            String discordID = rs.getString("DiscordID");
            rs.close();
            ps.close();
            return discordID;
        }
        ps.close();
        rs.close();
        return null;
    }

    public boolean hasDiscordID(ProxiedPlayer p) throws SQLException {

        if(!sql) {
            return VerifyFileManager.INSTANCE.hasDiscordID(p);
        }

        DataBaseConnection con = DataBaseConnection.INSTANCE;
        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: VerifyDAO.hasDiscordID");
        PreparedStatement ps = con.getConnection().prepareStatement("SELECT * FROM verify WHERE UUID = ?");
        ps.setString(1,p.getUniqueId().toString());

        ResultSet rs = ps.executeQuery();

        if(rs.next()) {
            String discordID = rs.getString("DiscordID");
            rs.close();
            ps.close();
            return (discordID != null);
        }
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
        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: VerifyDAO.addDiscordID");
        con.executeUpdate("UPDATE verify SET DiscordID = ? WHERE UUID = ?", member.getUser().getId(), player.getUniqueId().toString());
    }

    public void removeDiscordID(ProxiedPlayer player) throws SQLException {

        if(!sql) {
            VerifyFileManager.INSTANCE.removeDiscordID(player);
            return;
        }

        DataBaseConnection con = DataBaseConnection.INSTANCE;
        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: VerifyDAO.removeDiscordID");
        con.executeUpdate("UPDATE verify SET DiscordID = ? WHERE UUID = ?",null, player.getUniqueId().toString());
    }

    public void setPlayerAsVerified(UUID uuid) throws SQLException {

        if(!sql) {
            VerifyFileManager.INSTANCE.setVerifiedState(uuid,true);
            return;
        }

        DataBaseConnection con = DataBaseConnection.INSTANCE;
        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: VerifyDAO.setPlayerAsVerified");
        con.executeUpdate("UPDATE verify SET Verified = true WHERE UUID = ?", uuid.toString());
    }

    public void setPlayerAsUnVerified(UUID uuid) throws SQLException {

        if(!sql) {
            VerifyFileManager.INSTANCE.setVerifiedState(uuid,false);
            return;
        }

        DataBaseConnection con = DataBaseConnection.INSTANCE;
        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: VerifyDAO.setPlayerAsUnVerified");
        con.executeUpdate("UPDATE verify SET Verified = false WHERE UUID = ?", uuid.toString());
    }

    public void setPlayerAsUnverified(String discordID) throws SQLException {

        if(!sql) {
            VerifyFileManager.INSTANCE.setVerifiedState(discordID,false);
            return;
        }

        DataBaseConnection con = DataBaseConnection.INSTANCE;
        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: VerifyDAO.setPlayerAsUnverifiedUUID");
        con.executeUpdate("UPDATE verify SET Verified = false WHERE DiscordID = ?", discordID);
    }

    public void removeDiscordIDByDiscordID(Member m) throws SQLException {

        if(!sql) {
            VerifyFileManager.INSTANCE.removeDiscordID(m.getId());
            return;
        }

        DataBaseConnection con = DataBaseConnection.INSTANCE;
        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: VerifyDAO.removeDiscordIDByDiscordID");
        con.executeUpdate("UPDATE verify SET Verified = false, DiscordID = ? WHERE DiscordID = ?", null,m.getId());
    }



    public void updateUserName(ProxiedPlayer player) throws SQLException {

        if(!sql) {
            VerifyFileManager.INSTANCE.updateUserName(player);
            return;
        }

        DataBaseConnection con = DataBaseConnection.INSTANCE;
        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: VerifyDAO.updateUserName");
        con.executeUpdate("UPDATE verify SET PlayerName = ? WHERE UUID = ?", player.getName(), player.getUniqueId().toString());
    }



    public boolean isPlayerVerified(UUID uuid) throws SQLException {

        if(!sql) {
            return VerifyFileManager.INSTANCE.isPlayerVerified(uuid);
        }

        DataBaseConnection con = DataBaseConnection.INSTANCE;
        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: VerifyDAO.isPlayerVerified");
        PreparedStatement ps = con.getConnection().prepareStatement("SELECT * FROM verify WHERE UUID = ?");
        ps.setString(1,uuid.toString());

        ResultSet rs = ps.executeQuery();

        if(rs.next()) {
            boolean online = rs.getBoolean("Verified");
            System.out.println(online);
            rs.close();
            ps.close();
            return online;
        }
        ps.close();
        rs.close();
        return false;
    }

    public UUID getUUIDByName(String name) throws SQLException {
        if(!sql) {
            return VerifyFileManager.INSTANCE.getUUIDByPlayerName(name);
        }

        DataBaseConnection con = DataBaseConnection.INSTANCE;
        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: VerifyDAO.getUUIDByName");
        PreparedStatement ps = con.getConnection().prepareStatement("SELECT * FROM verify WHERE PlayerName = ?");
        ps.setString(1,name);

        ResultSet rs = ps.executeQuery();

        if(rs.next()) {
            UUID online = UUID.fromString(rs.getString("UUID"));
            System.out.println(online);
            rs.close();
            ps.close();
            return online;
        }
        ps.close();
        rs.close();
        return null;
    }

    public boolean isDiscordIDInUse(String discordID) throws SQLException {


        if(!sql) {
            return VerifyFileManager.INSTANCE.isIsDiscordIDInUse(discordID);
        }

        DataBaseConnection con = DataBaseConnection.INSTANCE;
        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: VerifyDAO.isDiscordIDInUse");
        PreparedStatement ps = con.getConnection().prepareStatement("SELECT * FROM verify WHERE DiscordID = ?");
        ps.setString(1,discordID);

        ResultSet rs = ps.executeQuery();

        if(rs.next()) {
            rs.close();
            ps.close();
            return true;
        }
        ps.close();
        rs.close();
        return false;
    }

    public String getUUIDByDiscordID(String discordID) throws SQLException {

        if(!sql) {
            return VerifyFileManager.INSTANCE.getUUIDFromDiscordID(discordID);
        }

        DataBaseConnection con = DataBaseConnection.INSTANCE;
        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: VerifyDAO.getUUIDByDiscordID");
        PreparedStatement ps = con.getConnection().prepareStatement("SELECT * FROM verify WHERE DiscordID = ?");
        ps.setString(1,discordID);

        ResultSet rs = ps.executeQuery();

        if(rs.next()) {
            String uuid = rs.getString("UUID");
            rs.close();
            ps.close();
            return uuid;
        }
        ps.close();
        rs.close();
        return null;

    }

}
