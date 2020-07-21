package de.staticred.discordbot.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class RewardsDAO {

    public static RewardsDAO INSTANCE = new RewardsDAO();

    public void loadTable() throws SQLException {
        DataBaseConnection con = DataBaseConnection.INSTANCE;
        con.connect();
        con.executeUpdate("CREATE TABLE IF NOT EXISTS rewards(UUID VARCHAR(36) PRIMARY KEY, playerRewarded BOOLEAN)");
        con.closeConnection();
    }

    public void addPlayerAsUnRewarded(UUID player) throws SQLException {
        DataBaseConnection con = DataBaseConnection.INSTANCE;
        con.connect();
        con.executeUpdate("INSERT INTO rewards(UUID, playerRewarded) VALUES(?,?)", player.toString(), 0);
        con.closeConnection();
    }

    public void setPlayerRewardState(UUID player, boolean state) throws SQLException {
        DataBaseConnection con = DataBaseConnection.INSTANCE;
        con.connect();
        con.executeUpdate("UPDATE rewards SET playerRewarded = ? WHERE UUID = ?", state, player.toString());
        con.closeConnection();
    }

    public boolean isPlayerInTable(UUID player) throws SQLException {
        DataBaseConnection con = DataBaseConnection.INSTANCE;
        con.connect();
        PreparedStatement ps = con.getConnection().prepareStatement("SELECT * FROM rewards WHERE UUID = ?");
        ps.setString(1, player.toString());

        ResultSet rs = ps.executeQuery();

        if(rs.next()) {
            ps.close();
            rs.close();
            con.closeConnection();
            return true;
        }
        ps.close();
        rs.close();
        con.closeConnection();
        return false;
    }

    public boolean hasPlayerBeenRewarded(UUID player) throws SQLException {
        DataBaseConnection con = DataBaseConnection.INSTANCE;
        con.connect();
        PreparedStatement ps = con.getConnection().prepareStatement("SELECT * FROM rewards WHERE UUID = ? AND playerRewarded = 1");
        ps.setString(1, player.toString());

        ResultSet rs = ps.executeQuery();

        if(rs.next()) {
            ps.close();
            rs.close();
            con.closeConnection();
            return true;
        }
        ps.close();
        rs.close();
        con.closeConnection();
        return false;
    }
}
