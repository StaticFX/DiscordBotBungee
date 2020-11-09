package de.staticred.discordbot.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.staticred.discordbot.DBVerifier;
import de.staticred.discordbot.files.ConfigFileManager;
import de.staticred.discordbot.util.Debugger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DataBaseConnection {

    private Connection connection;
    String url, host, port;

    public final static DataBaseConnection INSTANCE = new DataBaseConnection();
    public HikariDataSource dataSource;
    private HikariConfig config = new HikariConfig();

    private DataBaseConnection() {
        config.setUsername(ConfigFileManager.INSTANCE.getUser());
        config.setPassword(ConfigFileManager.INSTANCE.getPassword());
        config.addDataSourceProperty( "cachePrepStmts" , "true" );
        config.addDataSourceProperty( "prepStmtCacheSize" , "250" );
        config.addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" );
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(3);
        url = ConfigFileManager.INSTANCE.getDataBase();
        host = ConfigFileManager.INSTANCE.getHost();
        port = ConfigFileManager.INSTANCE.getPort();
        if(DBVerifier.getInstance().debugMode) Debugger.debugMessage("jdbc:mysql://" + host + ":" + port + "/" + url);
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + url);
        dataSource = new HikariDataSource(config);
    }

    public boolean isConnectionOpened() {
        return (connection != null);
    }

    public Connection getConnection() {
        return connection;
    }

    public void executeUpdate(String string, Object... obj) throws SQLException {

        Connection connection = dataSource.getConnection();

        PreparedStatement ps = connection.prepareStatement(string);
        for(int i = 0; i < obj.length; i++) {
            ps.setObject(i + 1, obj[i]);
        }
        ps.executeUpdate();
        ps.close();
        connection.close();
    }

    public boolean connectTest() {
        try {
            Connection connection = dataSource.getConnection();
        } catch (SQLException throwables) {
            return false;
        }
        return true;
    }
}
