package org.majora320.commandwhitelister;

import org.majora320.commandwhitelister.database.WhitelistDatabase;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * The logic for the /commandwhitelist command.
 * 
 * @author Moses Miller <pre><Majora320@gmail.com></pre>
 */
public class WhitelistCommand implements CommandExecutor {
    private final WhitelistDatabase database;
    
    public WhitelistCommand(WhitelistDatabase database) {
        this.database = database;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        return true;
    } 
}
