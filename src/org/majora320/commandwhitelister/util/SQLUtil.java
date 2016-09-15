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
