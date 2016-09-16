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
package org.majora320.commandwhitelister;

import java.sql.Driver;
import java.sql.SQLException;

/**
 * Program-wide constants.
 * 
 * @author Moses Miller <pre><Majora320@gmail.com></pre>
 */
public final class Constants {
    public static final String DEFAULT_URL = "jdbc:mysql:localhost/test";
    public static final String DEFAULT_DRIVER_NAME = "com.mysql.jdbc.Driver";
    public static final String DEFAULT_PRIMARY_TABLE_NAME = "command_whitelister";
    public static final String DEFAULT_ARGUMENT_TABLE_NAME = "command_whitelister_arguments";
    
    // Workaround to make up for the fact that constants can't throw
    public static final Driver getDefaultDriver() throws SQLException {
        return new com.mysql.jdbc.Driver();
    }
}