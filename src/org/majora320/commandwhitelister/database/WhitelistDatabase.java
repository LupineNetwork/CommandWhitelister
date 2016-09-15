package org.majora320.commandwhitelister.database;

import java.util.List;
import java.util.Map;

/**
 * A contract for a database class.
 * The idea is that you develop subclasses for mysql, sqlite, yaml, etc.
 * 
 * @author Moses Miller <pre><Majora320@gmail.com></pre>
 */
public interface WhitelistDatabase extends AutoCloseable {
    /**
     * Grabs a result from the database.
     * 
     * @param world the world in which the result is valid in
     * @param command the command to be validated
     * @param args any arguments to the command
     * @return a map from the groups this entry is valid for to a boolean if they are valid
     * @throws WhitelistDatabaseException if there is an error with the database
     */
    public Map<String, Boolean> get(String world, String command, List<String> args) throws WhitelistDatabaseException;
    /**
     * Sets a value in the database.
     * 
     * @param on the value to set
     * @param world the world in which the result is valid in
     * @param group the group for which the result is valid
     * @param command the command to be validated
     * @param args any arguments to the command
     * @throws WhitelistDatabaseException if there is an error with the database
     */
    public void set(boolean on, String world, String group, String command, List<String> args) throws WhitelistDatabaseException;
}
