package de.staticred.discordbot.db;

import de.staticred.discordbot.files.ConfigFileManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SRVDBConnection {

    private Connection connection;
    private String user, password,url, host, port;

    public final static SRVDBConnection INSTANCE = new SRVDBConnection();

    private SRVDBConnection() {
        user = ConfigFileManager.INSTANCE.getString("SRV_USER");
        password = ConfigFileManager.INSTANCE.getString("SRV_PASSWORD");
        url = ConfigFileManager.INSTANCE.getString("SRV_DATABASE");
        host = ConfigFileManager.INSTANCE.getString("SRV_HOST");
        port = ConfigFileManager.INSTANCE.getString("SRV_PORT");
    }

    public void connect() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + url, user,password);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void closeConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnectionOpened() {
        return (connection != null);
    }

    public Connection getConnection() {
        return connection;
    }

    public void executeUpdate(String string, Object... obj) throws SQLException {

        PreparedStatement ps = getConnection().prepareStatement(string);
        for(int i = 0; i < obj.length; i++) {
            ps.setObject(i + 1, obj[i]);
        }
        ps.executeUpdate();
        ps.close();
    }

}
