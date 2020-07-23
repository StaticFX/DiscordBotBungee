package de.staticred.discordbot.db;

import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SRVDAO {

    public static SRVDAO INSTANCE = new SRVDAO();


    public void link(ProxiedPlayer p, String discordID) throws SQLException {

        SRVDBConnection srv = SRVDBConnection.INSTANCE;

        srv.connect();
        srv.executeUpdate("INSERT INTO discordsrv_accounts (discord, uuid) VALUES (?, ?)", discordID,p.getUniqueId().toString());
        srv.closeConnection();
    }

    public void unlink(ProxiedPlayer p) throws SQLException {
        SRVDBConnection srv = SRVDBConnection.INSTANCE;
        srv.connect();
        srv.executeUpdate("DELETE FROM discordsrv_accounts WHERE uuid = ?",p.getUniqueId().toString());
        srv.closeConnection();
    }

    public void unlink(String discordID) throws SQLException {
        SRVDBConnection srv = SRVDBConnection.INSTANCE;
        srv.connect();
        srv.executeUpdate("DELETE FROM discordsrv_accounts WHERE discord = ?",discordID);
        srv.closeConnection();
    }

    public boolean isLinked(ProxiedPlayer p) throws SQLException {
        SRVDBConnection srv = SRVDBConnection.INSTANCE;
        srv.connect();

        PreparedStatement ps = srv.getConnection().prepareStatement("SELECT * FROM discordsrv_accounts WHERE uuid = ?");
        ps.setString(1, p.getUniqueId().toString());
        ResultSet rs = ps.executeQuery();

        if(rs.next()) {
            rs.close();
            ps.close();
            srv.closeConnection();
            return true;
        }
        rs.close();
        ps.close();
        srv.closeConnection();
        return false;
    }

    public boolean isLinked(String discordID) throws SQLException {
        SRVDBConnection srv = SRVDBConnection.INSTANCE;
        srv.connect();

        PreparedStatement ps = srv.getConnection().prepareStatement("SELECT * FROM discordsrv_accounts WHERE discord = ?");
        ps.setString(1, discordID);
        ResultSet rs = ps.executeQuery();

        if(rs.next()) {
            rs.close();
            ps.close();
            srv.closeConnection();
            return true;
        }
        rs.close();
        ps.close();
        srv.closeConnection();
        return false;
    }

}
