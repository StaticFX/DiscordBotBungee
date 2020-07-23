package de.staticred.discordbot.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
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
    private HikariDataSource  source;
    private HikariConfig config = new HikariConfig();

    private DataBaseConnection() {

        config.setUsername(ConfigFileManager.INSTANCE.getUser());
        config.setPassword(ConfigFileManager.INSTANCE.getPassword());
        config.addDataSourceProperty( "cachePrepStmts" , "true" );
        config.addDataSourceProperty( "prepStmtCacheSize" , "250" );
        config.addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" );
        url = ConfigFileManager.INSTANCE.getDataBase();
        host = ConfigFileManager.INSTANCE.getHost();
        port = ConfigFileManager.INSTANCE.getPort();
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + url);
    }

    public void connect() {
        try {
            source = new HikariDataSource(config);
            connection = source.getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean connectTest() {
        try {
            connect();
            closeConnection();
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    public void closeConnection() {
        if(source.isRunning())
            source.close();
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
