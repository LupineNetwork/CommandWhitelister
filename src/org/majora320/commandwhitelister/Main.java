package org.majora320.commandwhitelister;

import org.majora320.commandwhitelister.database.MySQLWhitelistDatabase;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.logging.Level;
import org.bukkit.plugin.java.JavaPlugin;
import org.majora320.commandwhitelister.database.WhitelistDatabase;
import org.majora320.commandwhitelister.database.WhitelistDatabaseException;

/**
 * The main plugin class.
 * 
 * @author Moses Miller <pre><Majora320@gmail.com></pre>
 */
public class Main extends JavaPlugin {
    private WhitelistDatabase database;
    
    @Override
    public void onEnable() {
        saveDefaultConfig();
        
        /*---------- DRIVER HANDLING ----------*/
        String driverName = getConfig().getString("mysql.driver", Constants.DEFAULT_DRIVER_NAME);
        Driver driver;
        
        try {
            try {
                driver = (Driver)Class.forName(driverName).newInstance();
            } catch (ClassNotFoundException ex) {
                // No need to report exception; we know what it is
                getServer().getLogger().log(Level.WARNING, "Could not find driver {0}, using default driver {1} instead.", new String[]{ driverName, Constants.DEFAULT_DRIVER_NAME });
                driver = Constants.getDefaultDriver();
            } catch (InstantiationException | IllegalAccessException ex) {
                getServer().getLogger().log(Level.WARNING, "Error initializing driver " + driverName + ", using default driver " + Constants.DEFAULT_DRIVER_NAME + " instead.", ex);
                driver = Constants.getDefaultDriver();
            }
        } catch (SQLException ex) { // We coulden't initialize the default driver
            throw new RuntimeException("Failed to initialize the default driver, bailing out!", ex);
        }
        /*---------- END DRIVER HANDLING ----------*/
        
        String url = getConfig().getString("mysql.url", Constants.DEFAULT_URL);
        String username = getConfig().getString("mysql.username");
        String password = getConfig().getString("mysql.password");
        String primaryTableName = getConfig().getString("mysql.primary-table-name", Constants.DEFAULT_PRIMARY_TABLE_NAME);
        String argumentTableName = getConfig().getString("argument-table-name", Constants.DEFAULT_ARGUMENT_TABLE_NAME);
        
        try {
            database = new MySQLWhitelistDatabase(url, username, password, primaryTableName, argumentTableName, driver);
        } catch (WhitelistDatabaseException ex) {
            throw new RuntimeException("Failed to initialize the connection to the database, bailing out!", ex);
        }
        
        getServer().getPluginManager().registerEvents(new CommandListener(database, getConfig()), this);
        getCommand("commandwhitelist").setExecutor(new WhitelistCommand(database));
    }
    
    @Override
    public void onDisable() {
        try {
            database.close();
        } catch (Exception ex) {
            getServer().getLogger().log(Level.SEVERE, "Failed to close the connection to the database!", ex);
        }
    }
}
