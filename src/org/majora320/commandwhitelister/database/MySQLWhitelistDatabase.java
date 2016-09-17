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
package org.majora320.commandwhitelister.database;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
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
     *
     * @throws SQLException if there is an error with the database
     */
    protected final void initializeDatabase() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            if (!SQLUtil.doesTableExist(conn, primaryTableName)) {
                stmt.execute("CREATE TABLE " + primaryTableName + "(id BIGINT NOT NULL AUTO_INCREMENT, world VARCHAR(255), group_name VARCHAR(255), command VARCHAR(255), args_list_id BLOB(20), PRIMARY KEY(id))");
            }

            if (!SQLUtil.doesTableExist(conn, argumentTableName)) {
                stmt.execute("CREATE TABLE " + argumentTableName + "(value VARCHAR(255), args_list_id BLOB(20))");
            }
        }
    }

    /**
     * Inserts a list of arguments into the database.
     *
     * @param id the id of the arguments to insert
     * @param args the arguments to insert
     * @throws SQLException if there is an error with the database
     */
    protected void insertArguments(byte[] id, List<String> args) throws SQLException {
        Blob idBlob = conn.createBlob();
        idBlob.setBytes(0, id);

        try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO " + argumentTableName + " VALUES(?, ?)")) {
            stmt.setBlob(2, idBlob);
            for (String arg : args) {
                stmt.setString(1, arg);
                stmt.execute();
            }
        }
    }

    /**
     * Retrieves a list of arguments from the database.
     *
     * @param id the id of the arguments to inset
     * @return the retrieved records
     * @throws SQLException if there is an error with the database
     */
    protected List<String> getArguments(byte[] id) throws SQLException {
        List<String> ret = new ArrayList<>();

        Blob idBlob = conn.createBlob();
        idBlob.setBytes(0, id);

        try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM " + argumentTableName + " WHERE (args_list_id = ?)")) {
            stmt.setBlob(1, idBlob);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ret.add(rs.getString("value"));
                }
            }
        }

        return ret;
    }
    
    protected void removeArguments(byte[] id) throws SQLException {
        Blob idBlob = conn.createBlob();
        idBlob.setBytes(0, id);
        
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM " + argumentTableName + " WHERE (args_list_id = ?)")) {
            stmt.setBlob(1, idBlob);
            stmt.execute();
        }
    }

    @Override
    public List<String> get(String world, String command, List<String> args) throws WhitelistDatabaseException {
        List<String> ret = new ArrayList<>();

        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM " + primaryTableName
                        + (world.equals("*") ? "" : " WHERE (world = " + world + ") AND") // Handle wildcards
                        + "(command = " + command + ")")) {

            // Check if each row's arguments match the args parameter
            // If they do, add it to ret
            while (rs.next()) {
                Blob idBlob = rs.getBlob("args_list_id");
                byte[] id = idBlob.getBytes(1, (int) idBlob.length());

                List<String> rowArgs = getArguments(id);

                // If args starts with rowArgs
                if (rowArgs.equals(args.subList(0, rowArgs.size() - 1))) {
                    // Add it
                    ret.add(rs.getString("group_name"));
                }
            }

        } catch (SQLException ex) {
            throw new WhitelistDatabaseException(ex);
        }

        return ret;
    }

    @Override
    public void set(boolean on, String world, String group, String command, List<String> args) throws WhitelistDatabaseException {
        boolean isOn = get(world, command, args).stream().filter(group2 -> group2.equals(group)).toArray().length != 0;

        // Nothing to do if we are in that state already
        if (isOn == on) {
            return;
        }

        try {
            if (on) {
                byte[] argsId;
                Blob idBlob = conn.createBlob();

                try {
                    List<Blob> allArgsIds = new ArrayList<>();

                    // Collect every args_list_id field to make sure it dosen't conflict with ours
                    try (Statement stmt = conn.createStatement();
                            ResultSet rs = stmt.executeQuery("SELECT args_list_id FROM " + primaryTableName)) {

                        while (rs.next()) {
                            Blob id = rs.getBlob("args_list_id");
                            allArgsIds.add(id);
                        }
                    }

                    do {
                        // Accumulate into a single string, plus some randomness
                        // Then hash that
                        // 3random5you?
                        Random rand = new Random();
                        argsId = MessageDigest.getInstance("SHA-1").digest(args.stream().reduce(new String(), (result, element) -> {
                            byte[] randomBytes = new byte[4];
                            rand.nextBytes(randomBytes);
                            return result + new String(randomBytes) + element;
                        }).getBytes());

                        idBlob.setBytes(0, argsId);
                    } while (allArgsIds.contains(idBlob));
                } catch (NoSuchAlgorithmException ex) {
                    throw new RuntimeException("MessageDigest does not contain the required implementation for SHA-1 algorithm", ex);
                }

                // Insert everything
                try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO " + argumentTableName + " VALUES(?, ?, ?, ?)")) {
                    stmt.setString(1, world);
                    stmt.setString(2, group);
                    stmt.setString(3, command);
                    stmt.setBlob(4, idBlob);

                    stmt.execute();

                    insertArguments(argsId, args);
                }
            } else {
                try (Statement stmt = conn.createStatement();
                        ResultSet rs = stmt.executeQuery("SELECT * FROM " + primaryTableName + " WHERE (world = " + world + ") AND (group_name = " + group + ") AND (command = " + command + ")")) {
                    ResultSetMetaData rsmd = rs.getMetaData();
                    
                    if (SQLUtil.getRows(rs) == 1) {
                        
                    } else {
                        
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
