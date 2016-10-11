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
package com.lupinenetwork.commandwhitelister.database;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import com.lupinenetwork.commandwhitelister.Constants;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;

/**
 * A mysql implementation of the database.
 *
 * @author Moses Miller &lt;Majora320@gmail.com&gt;
 */
public class MySQLWhitelistDatabase implements WhitelistDatabase {
    private Connection conn;
    private String primaryTableName;

    public MySQLWhitelistDatabase(Connection conn) {
        this.conn = conn;
    }

    public MySQLWhitelistDatabase(String url, String username, String password, String primaryTableName, Driver driver) throws WhitelistDatabaseException {
        this.primaryTableName = primaryTableName;

        try {
            DriverManager.registerDriver(driver);
            conn = DriverManager.getConnection(url, username, password);
            initializeDatabase();
        } catch (SQLException ex) {
            throw new WhitelistDatabaseException(ex);
        }
    }

    public MySQLWhitelistDatabase(String url, String username, String primaryTableName, String password) throws WhitelistDatabaseException {
        this.primaryTableName = primaryTableName;

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
     *
     * @throws SQLException if there is an error with the database
     */
    protected final void initializeDatabase() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS " + primaryTableName + "(id BIGINT NOT NULL AUTO_INCREMENT, server VARCHAR(255), group_name VARCHAR(255), command VARCHAR(255), args VARCHAR(255), on BIT, PRIMARY KEY(id))");
        }
    }

    /**
     * Inserts a list of arguments into the database.
     *
     * @param args the arguments to serialize
     * @return the encoded object
     * @throws SQLException if there is an error with the database
     */
    @SuppressWarnings("unchecked")
    protected String JSONEncode(List<String> args) throws SQLException {
        JSONArray array = new JSONArray();
        args.forEach(s -> array.put(s));
        
        return array.toString();
    }

    /**
     * Retrieves a list of arguments from the database.
     * Requires the column "args" to be requested.
     *
     * @param rs the {@code ResultSet} to read from
     * @return the retrieved records
     * @throws SQLException if there is an error with the database
     */
    @SuppressWarnings("unchecked")
    protected List<String> getArguments(ResultSet rs) throws SQLException {
        String json = rs.getString("args");
        
        JSONArray parsed = new JSONArray(json);
        List<String> ret = new ArrayList<>();
        
        parsed.iterator().forEachRemaining(o -> ret.add((String)o));
        
        return ret;
    }

    @Override
    public Map<String, Boolean> get(String server, String command, List<String> args) throws WhitelistDatabaseException {
        Map<String, Boolean> ret = new HashMap<>();

        try (PreparedStatement stmt = conn.prepareStatement("SELECT group_name, args, on FROM " + primaryTableName + " WHERE "
                        + "(server = ?)"
                        + "AND (? REGEXP command)")) {
            stmt.setString(1, server);
            stmt.setString(2, command);
            try (ResultSet rs = stmt.executeQuery()) {
                // Check if each row's arguments match the args parameter
                // If they do, add it to ret
                while (rs.next()) {
                    if (!rs.getBoolean("on"))
                        ret.put(rs.getString("group_name"), false);
                    
                    List<String> rowArgs = getArguments(rs);

                    // If args starts with rowArgs
                    if (args.size() >= rowArgs.size()) {
                        boolean allow = true;
                        
                        for (int i = 0; i < rowArgs.size(); i++) {
                            if (!args.get(i).matches("^" + (rowArgs.get(i).equals("*") ? ".*" : rowArgs.get(i)) + "$")) {
                                allow = false;
                                break;
                            }
                        }
                        
                        if (allow)
                            // Add it
                            ret.put(rs.getString("group_name"), true);
                    }
                }
            }
        } catch (SQLException ex) {
            throw new WhitelistDatabaseException(ex);
        }

        return ret;
    }

    @Override
    public void set(boolean on, String server, String group, String command, List<String> args) throws WhitelistDatabaseException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT args FROM " + primaryTableName + " WHERE (server = ?) AND (group_name = ?) AND (command = ?)")) {
            stmt.setString(1, server);
            stmt.setString(2, group);
            stmt.setString(3, command);
            
            try (ResultSet similar = stmt.executeQuery()) {
                if (on) {
                    while (similar.next())
                        if (getArguments(similar).equals(args))
                            return;

                    similar.beforeFirst();

                    // Insert everything
                    try (PreparedStatement insert = conn.prepareStatement("INSERT INTO " + primaryTableName + "(server, group_name, command, args, on) VALUES (?, ?, ?, ?, ?)")) {
                        insert.setString(1, server);
                        insert.setString(2, group);
                        insert.setString(3, command);
                        insert.setString(4, JSONEncode(args));
                        insert.setBoolean(5, on);

                        insert.execute();
                    }
                } else {
                    try (PreparedStatement delete = conn.prepareStatement("DELETE FROM " + primaryTableName + " WHERE (server = ?) AND (group_name = ?) AND (command = ?) AND (args = ?) AND (on = ?)")) {
                        delete.setString(1, server);
                        delete.setString(2, group);
                        delete.setString(3, command);
                        delete.setString(4, JSONEncode(args));
                        delete.setBoolean(5, true);
                        delete.execute();
                    }
                    
                    try (PreparedStatement insert = conn.prepareStatement("INSERT INTO " + primaryTableName + "VALUES (?, ?, ?, ?, ?)")) {
                       insert.setString(1, server);
                       insert.setString(2, group);
                       insert.setString(3, command);
                       insert.setString(4, JSONEncode(args));
                       insert.setBoolean(5, false);
                       insert.execute();
                    }
                }
            }
        } catch (SQLException ex) {
            throw new WhitelistDatabaseException(ex);
        }
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
