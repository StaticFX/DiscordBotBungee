package de.staticred.discordbot.db;

import de.staticred.discordbot.DBVerifier;
import de.staticred.discordbot.files.ConfigFileManager;
import de.staticred.discordbot.files.VerifyFileManager;
import de.staticred.discordbot.util.Debugger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class RewardsDAO {

    public static RewardsDAO INSTANCE = new RewardsDAO();

    public void loadTable() throws SQLException {

        if(!ConfigFileManager.INSTANCE.useSQL()) {
            return;
        }


        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: RewardsDAO.loadTable");
        DataBaseConnection.INSTANCE.executeUpdate("CREATE TABLE IF NOT EXISTS rewards(UUID VARCHAR(36) PRIMARY KEY, playerRewarded BOOLEAN)");
    }

    public void addPlayerAsUnRewarded(UUID player) throws SQLException {
        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: RewardsDAO.addPlayerAsUnRewarded ");
        DataBaseConnection.INSTANCE.executeUpdate("INSERT INTO rewards(UUID, playerRewarded) VALUES(?,?)", player.toString(), 0);
    }

    public void setPlayerRewardState(UUID player, boolean state) throws SQLException {

        if(!ConfigFileManager.INSTANCE.useSQL()) {
            VerifyFileManager.INSTANCE.setRewardState(player,state);
            return;
        }

        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: RewardsDAO.setPlayerRewardState");
        DataBaseConnection.INSTANCE.executeUpdate("UPDATE rewards SET playerRewarded = ? WHERE UUID = ?", state, player.toString());
    }

    public boolean isPlayerInTable(UUID player) throws SQLException {

        if(!ConfigFileManager.INSTANCE.useSQL()) {
            return true;
        }

        Connection con = DataBaseConnection.INSTANCE.dataSource.getConnection();
        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: RewardsDAO.isPlayerInTable");
        PreparedStatement ps = con.prepareStatement("SELECT * FROM rewards WHERE UUID = ?");
        ps.setString(1, player.toString());

        ResultSet rs = ps.executeQuery();

        if(rs.next()) {
            ps.close();
            rs.close();
            con.close();
            return true;
        }
        ps.close();
        rs.close();
        con.close();
        return false;
    }

    public boolean hasPlayerBeenRewarded(UUID player) throws SQLException {


        if(ConfigFileManager.INSTANCE.igrnoreRewardState()) return false;


        if(!ConfigFileManager.INSTANCE.useSQL()) {
            return VerifyFileManager.INSTANCE.getRewardState(player);
        }

        Connection con = DataBaseConnection.INSTANCE.dataSource.getConnection();
        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection from: RewardsDAO.hasPlayerBeenRewarded");
        PreparedStatement ps = con.prepareStatement("SELECT * FROM rewards WHERE UUID = ? AND playerRewarded = 1");
        ps.setString(1, player.toString());

        ResultSet rs = ps.executeQuery();

        if(rs.next()) {
            ps.close();
            rs.close();
            con.close();
            return true;
        }
        ps.close();
        rs.close();
        con.close();
        return false;
    }
}
