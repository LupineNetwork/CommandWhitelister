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
package com.lupinenetwork.commandwhitelister.util;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Misc. util for sql.
 * 
 * @author Moses Miller <pre><Majora320@gmail.com></pre>
 */
public class SQLUtil {
    /**
     * Get the number of rows in table.
     * @param table the table to grab the number of rows from
     * @return the number of rows
     * @throws java.sql.SQLException if there is an error with the database
     */
    public static int getRows(ResultSet table) throws SQLException {
        if (table == null)
            return 0;
        
        int rows;
        try {
            table.last();
            rows = table.getRow();
        } finally {
            table.beforeFirst();
        }
        
        return rows;
    }
}
