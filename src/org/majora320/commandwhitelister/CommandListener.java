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
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.majora320.commandwhitelister.database.WhitelistDatabase;
import org.majora320.commandwhitelister.database.WhitelistDatabaseException;

/**
 * The class which handles the command request.
 * 
 * @author Moses Miller <pre><Majora320@gmail.com></pre>
 */
public class CommandListener implements Listener {
    private final WhitelistDatabase database;
    private final FileConfiguration config;
    
    public CommandListener(WhitelistDatabase database, FileConfiguration config) {
        this.database = database;
        this.config = config;
    }
    
    // Complicated magic :)
    // We need raw strings in java!
    private static final Pattern COMMAND_PATTERN = Pattern.compile("(([^\" ]|\\\\\\\\\")+|\"(\\\\\\\\.|[^\"\\\\\\\\])*\")\\S*");
    
    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent evt) throws WhitelistDatabaseException {
        String cmd = evt.getMessage().substring(1); // Remove leading slash
        Matcher matt = COMMAND_PATTERN.matcher(cmd);
        
        String label;
        List<String> args = new ArrayList<>();
        
        if (!matt.find())
            return; // Pattern did not match; will cause a syntax error
        
        label = matt.group(1); // Group one is the contents without the \S*
        
        while (matt.find()) {
            args.add(matt.group(1));
        }
        
        Map<String, Boolean> allows = database.get(evt.getPlayer().getWorld().getName(), label, args);
        
        boolean allow = allows.keySet().stream()
                .filter(key -> (key.equals("*") || evt.getPlayer().hasPermission("group." + key))
                        && allows.get(key).equals(true)).toArray().length != 0;
        
        if (!allow) {
            evt.setCancelled(true);
            evt.getPlayer().sendMessage(config.getString("blacklisted-error-message", "§cYou do not have permission to execute this command!"));
        }
    }
}