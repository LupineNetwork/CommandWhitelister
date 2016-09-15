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
