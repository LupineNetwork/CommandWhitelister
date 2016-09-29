/*
 * Copyright (C) 2016 Lupine Network <bedev@twpclan.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.lupinenetwork.commandwhitelister;

import com.lupinenetwork.commandwhitelister.database.MySQLWhitelistDatabase;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.logging.Level;
import com.lupinenetwork.commandwhitelister.database.WhitelistDatabase;
import com.lupinenetwork.commandwhitelister.database.WhitelistDatabaseException;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

/**
 * The main plugin class.
 * 
 * @author Moses Miller <pre><Majora320@gmail.com></pre>
 */
public class Main extends Plugin {
    private WhitelistDatabase database;
    
    @Override
    public void onEnable() {
        Configuration config;
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
        } catch (IOException ex) {
            getProxy().getLogger().log(Level.WARNING, "Failed to load config file", ex);
            config = new Configuration();
        }
        
        /*---------- DRIVER HANDLING ----------*/
        String driverName = config.getString("mysql.driver", Constants.DEFAULT_DRIVER_NAME);
        Driver driver;
        
        try {
            try {
                driver = (Driver)Class.forName(driverName).newInstance();
            } catch (ClassNotFoundException ex) {
                // No need to report exception; we know what it is
                getProxy().getLogger().log(Level.WARNING, "Could not find driver {0}, using default driver {1} instead.", new String[]{ driverName, Constants.DEFAULT_DRIVER_NAME });
                driver = Constants.getDefaultDriver();
            } catch (InstantiationException | IllegalAccessException ex) {
                getProxy().getLogger().log(Level.WARNING, "Error initializing driver " + driverName + ", using default driver " + Constants.DEFAULT_DRIVER_NAME + " instead.", ex);
                driver = Constants.getDefaultDriver();
            }
        } catch (SQLException ex) { // We coulden't initialize the default driver
            throw new RuntimeException("Failed to initialize the default driver, bailing out!", ex);
        }
        /*---------- END DRIVER HANDLING ----------*/
        
        String url = config.getString("mysql.url", Constants.DEFAULT_URL);
        String username = config.getString("mysql.username");
        String password = config.getString("mysql.password");
        String primaryTableName = config.getString("mysql.primary-table-name", Constants.DEFAULT_PRIMARY_TABLE_NAME);
        String argumentTableName = config.getString("mysql.argument-table-name", Constants.DEFAULT_ARGUMENT_TABLE_NAME);
        
        try {
            database = new MySQLWhitelistDatabase(url, username, password, primaryTableName, argumentTableName, driver);
        } catch (WhitelistDatabaseException ex) {
            throw new RuntimeException("Failed to initialize the connection to the database, bailing out!", ex);
        }
        
        getProxy().getPluginManager().registerListener(this, new CommandListener(database, config));
        getProxy().getPluginManager().registerCommand(this, new WhitelistCommand(database));
    }
    
    @Override
    public void onDisable() {
        try {
            database.close();
        } catch (Exception ex) {
            getProxy().getLogger().log(Level.SEVERE, "Failed to close the connection to the database!", ex);
        }
    }
}
