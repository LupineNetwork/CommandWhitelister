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
package org.majora320.commandwhitelister.util;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Misc. util for sql.
 * 
 * @author Moses Miller <pre><Majora320@gmail.com></pre>
 */
public class SQLUtil {
    /**
     * Check if a table exists.
     * This one I owe to stackoverflow: https://stackoverflow.com/questions/2942788/check-if-table-exists
     * 
     * @param conn the connection to query
     * @param tableName the name of the table to test
     * @return true if the table was found, otherwise false
     * @throws SQLException if there is an error with the database
     */
    public static boolean doesTableExist(Connection conn, String tableName) throws SQLException {
        return conn.getMetaData().getTables(null, null, tableName, new String[]{ "TABLE" }).next();
    }
}
