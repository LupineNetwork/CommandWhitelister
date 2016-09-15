package org.majora320.commandwhitelister.database;

import java.sql.Blob;
import org.majora320.commandwhitelister.database.WhitelistDatabase;
import org.majora320.commandwhitelister.database.WhitelistDatabaseException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.majora320.commandwhitelister.Constants;
import org.majora320.commandwhitelister.util.SQLUtil;

/**
 * A mysql implementation of the database.
 * 
 * @author Moses Miller <pre><Majora320@gmail.com></pre>
 */
public class MySQLWhitelistDatabase implements WhitelistDatabase {
    private Connection conn;
    private String primaryTableName;
    private String argumentTableName;
    
    public MySQLWhitelistDatabase(Connection conn) {
        this.conn = conn;
    }
    
    public MySQLWhitelistDatabase(String url, String username, String password, String primaryTableName, String argumentTableName, Driver driver) throws WhitelistDatabaseException {
        this.primaryTableName = primaryTableName;
        this.argumentTableName = argumentTableName;
        
        try {
            DriverManager.registerDriver(driver);
            conn = DriverManager.getConnection(url, username, password);
            initializeDatabase();
        } catch (SQLException ex) {
            throw new WhitelistDatabaseException(ex);
        }
    }
    
    public MySQLWhitelistDatabase(String url, String username, String primaryTableName, String argumentTableName, String password) throws WhitelistDatabaseException {
        this.primaryTableName = primaryTableName;
        this.argumentTableName = argumentTableName;
        
        try {
            DriverManager.registerDriver(Constants.getDefaultDriver()); // Default driver
            conn = DriverManager.getConnection(url, username, password);
            initializeDatabase();
        } catch (SQLException ex) {
            throw new WhitelistDatabaseException(ex);
        } 
    }
    
    /**
     * Do the boilerplate required to setup tables, etc.
     * @throws SQLException if there is an error with the database
     */
    private void initializeDatabase() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            if (!SQLUtil.doesTableExist(conn, primaryTableName)) {
                stmt.execute("CREATE TABLE " + primaryTableName + "(world VARCHAR(255), group VARCHAR(255), command VARCHAR(255), args_list_id BLOB(32))");
            }
            
            if (!SQLUtil.doesTableExist(conn, argumentTableName)) {
                stmt.execute("CREATE TABLE " + argumentTableName + "(value VARCHAR(255), args_list_id BLOB(32))");
            }
        }
    }
    
    /**
     * Inserts a list of arguments into the database.
     * 
     * @param args the arguments to insert
     * @throws SQLException if there is an error with the database
     */
    private void insertArguments(byte[] id, List<String> args) throws SQLException {
        Blob idBlob = conn.createBlob();
        idBlob.setBytes(0, id);
        
        try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO " + argumentTableName + " VALUES(?, ?)")) {
            stmt.setBlob(1, idBlob);
            for (String arg : args) {
                stmt.setString(0, arg);
                stmt.execute();
            }
        }
    }
    
    @Override
    public Map<String, Boolean> get(String world, String command, List<String> args) throws WhitelistDatabaseException {
        Map<String, Boolean> ret = new HashMap<>();
        
        return ret;
    }

    @Override
    public void set(boolean on, String world, String group, String command, List<String> args) throws WhitelistDatabaseException {
        
    }

    @Override
    public void close() throws WhitelistDatabaseException {
        try {
            conn.close();
        } catch (SQLException ex) {
            throw new WhitelistDatabaseException(ex);
        }
    }
    
    public boolean isClosed() throws WhitelistDatabaseException {
        try {
            return conn.isClosed();
        } catch (SQLException ex) {
            throw new WhitelistDatabaseException(ex);
        }
    }
}
