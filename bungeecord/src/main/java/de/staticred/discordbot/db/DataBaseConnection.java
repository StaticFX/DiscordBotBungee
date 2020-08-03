package de.staticred.discordbot.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import de.staticred.discordbot.DBVerifier;
import de.staticred.discordbot.files.ConfigFileManager;
import de.staticred.discordbot.util.Debugger;
import jdk.internal.net.http.hpack.HPACK;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

public class DataBaseConnection {

    private Connection connection;
    String url, host, port;

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
        System.out.println("jdbc:mysql://" + host + ":" + port + "/" + url);
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + url);
    }

    public void connect() {
        try {
            if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Opening DB Connection");
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
        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("Closing DB Connection");
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
