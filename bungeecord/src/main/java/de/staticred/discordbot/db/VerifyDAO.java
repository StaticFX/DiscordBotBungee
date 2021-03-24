package de.staticred.discordbot.db;

import de.staticred.discordbot.DBVerifier;
import de.staticred.discordbot.files.ConfigFileManager;
import de.staticred.discordbot.files.VerifyFileManager;
import de.staticred.discordbot.util.Debugger;
import net.dv8tion.jda.api.entities.Member;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VerifyDAO {

    public static VerifyDAO INSTANCE = new VerifyDAO();

    private boolean sql = ConfigFileManager.INSTANCE.useSQL();


    public void loadDataBase() {
        DataBaseConnection con = DataBaseConnection.INSTANCE;
        try {

            if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: VerifyDAO.loadDataBase 1");
            con.executeUpdate("CREATE TABLE IF NOT EXISTS verify(UUID VARCHAR(36) PRIMARY KEY, PlayerName VARCHAR(16), Verified BOOLEAN, DiscordID VARCHAR(100), Version VARCHAR(10))");

            String version = getDataBaseVersion();

            //update database to newest version
            if(version == null || !doesColumnExist("Version")) {
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

            if(version != null && !version.equals(DBVerifier.DATABASE_VERSION)) {
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

        Connection con = DataBaseConnection.INSTANCE.dataSource.getConnection();
        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: VerifyDAO.getAmoundOfVerifedPlayers");
        PreparedStatement ps = con.prepareStatement("SELECT * FROM verify WHERE Verified = 1");
        ResultSet rs = ps.executeQuery();

        int amount = 0;

        while (rs.next()){
            amount += 1;
        }

        ps.close();
        rs.close();
        con.close();
        return amount;
    }

    public boolean doesColumnExist(String column) throws SQLException {
        Connection con = DataBaseConnection.INSTANCE.dataSource.getConnection();
        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: VerifyDAO.doesColumnExist");
        PreparedStatement ps = con.prepareStatement("SELECT * FROM verify");

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
        con.close();

        return false;
    }

    public boolean hasUsersInDataBase() throws SQLException {
        Connection con = DataBaseConnection.INSTANCE.dataSource.getConnection();
        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: VerifyDAO.hasUserInDataBase");
        PreparedStatement ps = con.prepareStatement("SELECT * FROM verify");

        ResultSet rs = ps.executeQuery();

        if(rs.next()) {
            rs.close();
            ps.close();
            return true;
        }
        ps.close();
        rs.close();
        con.close();
        return false;
    }

    public String getDataBaseVersion() {
        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: VerifyDAO.getDataBaseVersion");

        try {
            Connection con = DataBaseConnection.INSTANCE.dataSource.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM verify");

            ResultSet rs = ps.executeQuery();

            if(rs.next()) {
                String version = rs.getString("Version");
                rs.close();
                ps.close();
                con.close();
                return version;
            }
            ps.close();
            rs.close();
            con.close();
            return null;
        }catch (Exception e) {
            return null;
        }
    }

    public boolean isPlayerInDataBase(ProxiedPlayer p) throws SQLException {

        if(!sql) return VerifyFileManager.INSTANCE.isPlayerInFile(p);

        Connection con = DataBaseConnection.INSTANCE.dataSource.getConnection();
        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: VerifyDAO.isPlayerInDatabase");
        PreparedStatement ps = con.prepareStatement("SELECT * FROM verify WHERE UUID = ?");
        ps.setString(1,p.getUniqueId().toString());

        ResultSet rs = ps.executeQuery();

        if(rs.next()) {
            rs.close();
            ps.close();
            con.close();
            return true;
        }
        ps.close();
        rs.close();
        con.close();
        return false;
    }

    public boolean isPlayerInDataBase(UUID uuid) throws SQLException {

        if(!sql) return VerifyFileManager.INSTANCE.isPlayerInFile(uuid);

        Connection con = DataBaseConnection.INSTANCE.dataSource.getConnection();
        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: VerifyDAO.isPlayerInDatabase");
        PreparedStatement ps = con.prepareStatement("SELECT * FROM verify WHERE UUID = ?");
        ps.setString(1,uuid.toString());

        ResultSet rs = ps.executeQuery();

        if(rs.next()) {
            rs.close();
            ps.close();
            con.close();
            return true;
        }
        ps.close();
        rs.close();
        con.close();
        return false;
    }


    public String getName(String discordID) throws SQLException {
        if(!sql) return VerifyFileManager.INSTANCE.getName(discordID);

        Connection con = DataBaseConnection.INSTANCE.dataSource.getConnection();
        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: VerifyDAO.getName");
        PreparedStatement ps = con.prepareStatement("SELECT * FROM verify WHERE DiscordID = ?");
        ps.setString(1,discordID);

        ResultSet rs = ps.executeQuery();

        if(rs.next()) {
            String name = rs.getString("PlayerName");
            rs.close();
            ps.close();
            con.close();
            return name;
        }
        ps.close();
        rs.close();
        con.close();
        return null;
    }

    public void removePlayerData(UUID uuid) throws SQLException {
        if(!sql) {
            VerifyFileManager.INSTANCE.removePlayerData(uuid);
            return;
        }

        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: VerifyDAO.removePlayerData");
        DataBaseConnection.INSTANCE.executeUpdate("DELETE FROM verify WHERE UUID = ?",uuid.toString());
    }

    public void addPlayerAsUnverified(ProxiedPlayer player) throws SQLException {

        if(!sql) {
            VerifyFileManager.INSTANCE.addPlayerAsUnverified(player);
            return;
        }

        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: VerifyDAO.addPlayerAsUnverified");
        DataBaseConnection.INSTANCE.executeUpdate("INSERT INTO verify(UUID,PlayerName,Verified,DiscordID,Version) VALUES(?,?,?,?,?)", player.getUniqueId().toString(), player.getName() ,false,null, DBVerifier.DATABASE_VERSION);
    }

    public void addPlayerAsUnverified(UUID uuid) throws SQLException {

        if(!sql) {
            VerifyFileManager.INSTANCE.addPlayerAsUnverified(uuid);
            return;
        }

        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: VerifyDAO.addPlayerAsUnverified");
        DataBaseConnection.INSTANCE.executeUpdate("INSERT INTO verify(UUID,PlayerName,Verified,DiscordID,Version) VALUES(?,?,?,?,?)", uuid.toString(), "unknown" ,false,null, DBVerifier.DATABASE_VERSION);
    }

    public String getDiscordID(UUID uuid) throws SQLException {

        if(!sql) {
            return  VerifyFileManager.INSTANCE.getDiscordID(uuid);
        }

        Connection con = DataBaseConnection.INSTANCE.dataSource.getConnection();
        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: VerifyDAO.getDiscordID");
        PreparedStatement ps = con.prepareStatement("SELECT * FROM verify WHERE UUID = ?");
        ps.setString(1,uuid.toString());

        ResultSet rs = ps.executeQuery();

        if(rs.next()) {
            String discordID = rs.getString("DiscordID");
            rs.close();
            ps.close();
            con.close();
            return discordID;
        }
        ps.close();
        rs.close();
        con.close();
        return null;
    }

    public boolean hasDiscordID(ProxiedPlayer p) throws SQLException {

        if(!sql) {
            return VerifyFileManager.INSTANCE.hasDiscordID(p);
        }

        Connection con = DataBaseConnection.INSTANCE.dataSource.getConnection();
        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: VerifyDAO.hasDiscordID");
        PreparedStatement ps = con.prepareStatement("SELECT * FROM verify WHERE UUID = ?");
        ps.setString(1,p.getUniqueId().toString());

        ResultSet rs = ps.executeQuery();

        if(rs.next()) {
            String discordID = rs.getString("DiscordID");
            rs.close();
            ps.close();
            con.close();
            return (discordID != null);
        }
        ps.close();
        rs.close();
        con.close();
        return false;
    }

    public void addDiscordID(ProxiedPlayer player, Member member) throws SQLException {

        if(!sql) {
            VerifyFileManager.INSTANCE.addDiscordID(player, member.getId());
            return;
        }

        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: VerifyDAO.addDiscordID");
        DataBaseConnection.INSTANCE.executeUpdate("UPDATE verify SET DiscordID = ? WHERE UUID = ?", member.getUser().getId(), player.getUniqueId().toString());
    }

    public void addDiscordID(UUID player, String id) throws SQLException {

        if(!sql) {
            VerifyFileManager.INSTANCE.addDiscordID(player, id);
            return;
        }

        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: VerifyDAO.addDiscordID");
        DataBaseConnection.INSTANCE.executeUpdate("UPDATE verify SET DiscordID = ? WHERE UUID = ?", id, player.toString());
    }

    public void removeDiscordID(ProxiedPlayer player) throws SQLException {

        if(!sql) {
            VerifyFileManager.INSTANCE.removeDiscordID(player);
            return;
        }

        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: VerifyDAO.removeDiscordID");
        DataBaseConnection.INSTANCE.executeUpdate("UPDATE verify SET DiscordID = ? WHERE UUID = ?",null, player.getUniqueId().toString());
    }

    public void removeDiscordID(UUID player) throws SQLException {

        if(!sql) {
            VerifyFileManager.INSTANCE.removeDiscordID(player);
            return;
        }

        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: VerifyDAO.removeDiscordID");
        DataBaseConnection.INSTANCE.executeUpdate("UPDATE verify SET DiscordID = ? WHERE UUID = ?",null, player.toString());
    }

    public void setPlayerAsVerified(UUID uuid) throws SQLException {

        if(!sql) {
            VerifyFileManager.INSTANCE.setVerifiedState(uuid,true);
            return;
        }

        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: VerifyDAO.setPlayerAsVerified");
        DataBaseConnection.INSTANCE.executeUpdate("UPDATE verify SET Verified = true WHERE UUID = ?", uuid.toString());
    }

    public void setPlayerAsUnVerified(UUID uuid) throws SQLException {

        if(!sql) {
            VerifyFileManager.INSTANCE.setVerifiedState(uuid,false);
            return;
        }

        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: VerifyDAO.setPlayerAsUnVerified");
        DataBaseConnection.INSTANCE.executeUpdate("UPDATE verify SET Verified = false WHERE UUID = ?", uuid.toString());
    }

    public void setPlayerAsUnverified(String discordID) throws SQLException {

        if(!sql) {
            VerifyFileManager.INSTANCE.setVerifiedState(discordID,false);
            return;
        }

        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: VerifyDAO.setPlayerAsUnverifiedUUID");
        DataBaseConnection.INSTANCE.executeUpdate("UPDATE verify SET Verified = false WHERE DiscordID = ?", discordID);
    }

    public void removeDiscordIDByDiscordID(Member m) throws SQLException {

        if(!sql) {
            VerifyFileManager.INSTANCE.removeDiscordID(m.getId());
            return;
        }

        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: VerifyDAO.removeDiscordIDByDiscordID");
        DataBaseConnection.INSTANCE.executeUpdate("UPDATE verify SET DiscordID = ? WHERE DiscordID = ?", null,m.getId());
    }



    public void updateUserName(ProxiedPlayer player) throws SQLException {

        if(!sql) {
            VerifyFileManager.INSTANCE.updateUserName(player);
            return;
        }

        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: VerifyDAO.updateUserName");
        DataBaseConnection.INSTANCE.executeUpdate("UPDATE verify SET PlayerName = ? WHERE UUID = ?", player.getName(), player.getUniqueId().toString());
    }

    public void updateUserName(UUID uuid, String player) throws SQLException {

        if(!sql) {
            VerifyFileManager.INSTANCE.updateUserName(uuid, player);
            return;
        }

        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: VerifyDAO.updateUserName");
        DataBaseConnection.INSTANCE.executeUpdate("UPDATE verify SET PlayerName = ? WHERE UUID = ?", player, uuid.toString());
    }



    public boolean isPlayerVerified(UUID uuid) throws SQLException {

        if(!sql) {
            return VerifyFileManager.INSTANCE.isPlayerVerified(uuid);
        }

        Connection con = DataBaseConnection.INSTANCE.dataSource.getConnection();
        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: VerifyDAO.isPlayerVerified");
        PreparedStatement ps = con.prepareStatement("SELECT * FROM verify WHERE UUID = ?");
        ps.setString(1,uuid.toString());

        ResultSet rs = ps.executeQuery();

        if(rs.next()) {
            boolean online = rs.getBoolean("Verified");
            rs.close();
            ps.close();
            con.close();
            return online;
        }
        ps.close();
        rs.close();
        con.close();
        return false;
    }

    public UUID getUUIDByName(String name) throws SQLException {
        if(!sql) {
            return VerifyFileManager.INSTANCE.getUUIDByPlayerName(name);
        }

        Connection con = DataBaseConnection.INSTANCE.dataSource.getConnection();
        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: VerifyDAO.getUUIDByName");
        PreparedStatement ps = con.prepareStatement("SELECT * FROM verify WHERE PlayerName = ?");
        ps.setString(1,name);

        ResultSet rs = ps.executeQuery();

        if(rs.next()) {
            UUID online = UUID.fromString(rs.getString("UUID"));
            rs.close();
            ps.close();
            con.close();
            return online;
        }
        ps.close();
        rs.close();
        con.close();
        return null;
    }

    public boolean isDiscordIDInUse(String discordID) throws SQLException {


        if(!sql) {
            return VerifyFileManager.INSTANCE.isIsDiscordIDInUse(discordID);
        }

        Connection con = DataBaseConnection.INSTANCE.dataSource.getConnection();
        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: VerifyDAO.isDiscordIDInUse");
        PreparedStatement ps = con.prepareStatement("SELECT * FROM verify WHERE DiscordID = ?");
        ps.setString(1,discordID);

        ResultSet rs = ps.executeQuery();

        if(rs.next()) {
            rs.close();
            ps.close();
            con.close();
            return true;
        }
        ps.close();
        rs.close();
        con.close();
        return false;
    }

    public String getUUIDByDiscordID(String discordID) throws SQLException {

        if(!sql) {
            return VerifyFileManager.INSTANCE.getUUIDFromDiscordID(discordID);
        }

        Connection con = DataBaseConnection.INSTANCE.dataSource.getConnection();
        PreparedStatement ps = con.prepareStatement("SELECT * FROM verify WHERE DiscordID = ?");
        ps.setString(1,discordID);

        ResultSet rs = ps.executeQuery();

        if(rs.next()) {
            String uuid = rs.getString("UUID");
            rs.close();
            ps.close();
            con.close();
            return uuid;
        }
        ps.close();
        rs.close();
        con.close();
        return null;

    }

    public Set<UUID> getAllUsers() throws SQLException {

        if(!sql) {
            return VerifyFileManager.INSTANCE.getAllUsers();
        }

        Connection con = DataBaseConnection.INSTANCE.dataSource.getConnection();
        PreparedStatement ps = con.prepareStatement("SELECT * FROM verify");

        ResultSet rs = ps.executeQuery();

        Set<UUID> ids = new HashSet<>();

        while(rs.next()) {
            ids.add(UUID.fromString(rs.getString("UUID")));
        }
        ps.close();
        rs.close();
        con.close();
        return ids;
    }
}
