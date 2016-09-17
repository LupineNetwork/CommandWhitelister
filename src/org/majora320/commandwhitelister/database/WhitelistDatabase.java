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
     * @return a list of groups for which the result is valid for
     * @throws WhitelistDatabaseException if there is an error with the database
     */
    public List<String> get(String world, String command, List<String> args) throws WhitelistDatabaseException;
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
