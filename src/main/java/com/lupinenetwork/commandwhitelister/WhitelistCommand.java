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
package com.lupinenetwork.commandwhitelister;

import java.util.ArrayList;
import java.util.List;
import com.lupinenetwork.commandwhitelister.database.WhitelistDatabase;
import com.lupinenetwork.commandwhitelister.database.WhitelistDatabaseException;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

/**
 * The logic for the /commandwhitelister command.
 * 
 * @author Moses Miller <pre><Majora320@gmail.com></pre>
 */
public class WhitelistCommand extends Command {
    private final WhitelistDatabase database;
    
    public WhitelistCommand(WhitelistDatabase database) {
        super("commandwhitelister");
        this.database = database;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage();
            return;
        }
        
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
        
        switch (onOffText) {
            case "on":
                on = true;
                break;
            case "off":
                on = false;
                break;
            default:
                return;
        }
        
        try {
            database.set(on, world, group, command, subs);
        } catch (WhitelistDatabaseException ex) {
            sender.sendMessage(new TextComponent(ChatColor.RED + "Error modifing the database! Check the logs for details."));
            throw new RuntimeException(ex);
        }
        
        sender.sendMessage(new TextComponent(ChatColor.GREEN + "Successfully modified the database!"));
    }
}
