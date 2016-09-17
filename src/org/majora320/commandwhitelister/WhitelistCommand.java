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

import java.util.ArrayList;
import java.util.List;
import org.majora320.commandwhitelister.database.WhitelistDatabase;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.majora320.commandwhitelister.database.WhitelistDatabaseException;

/**
 * The logic for the /commandwhitelister command.
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
        if (args.length < 4)
            return false;
        
        String world = args[0];
        String group = args[1];
        String command = args[2];
        List<String> subs = new ArrayList<>();
        
        // Grab arguments
        for (int i = 3; i < args.length - 1; i++) {
            subs.add(args[i]);
        }
        
        String onOffText = args[args.length - 1];
        boolean on;
        
        if (onOffText.equals("on"))
            on = true;
        else if (onOffText.equals("off"))
            on = false;
        else
            return false;
        
        try {
            database.set(on, world, group, command, subs);
        } catch (WhitelistDatabaseException ex) {
            throw new RuntimeException(ex);
        }
        
        return true;
    } 
}
