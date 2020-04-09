package de.staticred.discordbot.db;

import de.staticred.discordbot.files.ConfigFileManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DataBaseConnection {

    private Connection connection;
    String user, password,url, host;
    int port;

    public final static DataBaseConnection INSTANCE = new DataBaseConnection();

    private DataBaseConnection() {
        user = ConfigFileManager.INSTANCE.getUser();
        password = ConfigFileManager.INSTANCE.getPassword();
        url = ConfigFileManager.INSTANCE.getDataBase();
        host = ConfigFileManager.INSTANCE.getHost();
        port = ConfigFileManager.INSTANCE.getPort();
    }

    public void connect() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + url, user,password);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean connectTest() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + url, user,password);
            closeConnection();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
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
