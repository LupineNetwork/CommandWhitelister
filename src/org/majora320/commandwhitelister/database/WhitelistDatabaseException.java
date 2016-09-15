package org.majora320.commandwhitelister.database;

/**
 * A wrapper for handling exceptions.
 * 
 * @author Moses Miller <pre><Majora320@gmail.com></pre>
 */
public class WhitelistDatabaseException extends Exception {
    public WhitelistDatabaseException(String message) {
        super(message);
    }
    
    public WhitelistDatabaseException(Throwable cause) {
        super(cause);
    }
    
    public WhitelistDatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
