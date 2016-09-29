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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.plugin.Listener;
import com.lupinenetwork.commandwhitelister.database.WhitelistDatabase;
import com.lupinenetwork.commandwhitelister.database.WhitelistDatabaseException;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import ru.tehkode.permissions.bukkit.PermissionsEx;

/**
 * The class which handles the command request.
 *
 * @author Moses Miller <pre><Majora320@gmail.com></pre>
 */
public class CommandListener implements Listener {
    private final WhitelistDatabase database;
    private final Configuration config;

    public CommandListener(WhitelistDatabase database, Configuration config) {
        this.database = database;
        this.config = config;
    }

    // Complicated magic :)
    // We need raw strings in java!
    private static final Pattern COMMAND_PATTERN = Pattern.compile("([^ ]+)\\S+");

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(ChatEvent evt) {
        Connection sender = evt.getSender();
        
        if (!(sender instanceof ProxiedPlayer))
            return;
        
        ProxiedPlayer player = (ProxiedPlayer)sender;
        
        if (player.hasPermission("commandwhitelister.bypass"))
            return;
        
        String cmd = evt.getMessage().trim().substring(1); // Remove leading slash
        Matcher matt = COMMAND_PATTERN.matcher(cmd);

        String label;
        List<String> args = new ArrayList<>();

        if (!matt.find()) {
            return; // Pattern did not match; will cause a syntax error
        }
        label = matt.group(1); // Group 1 is the contents without the \S*

        while (matt.find()) {
            args.add(matt.group(1));
        }
        
        List<String> allows;
        try {
            allows = database.get(player.getServer().getInfo().getName(), label, args);
        } catch (WhitelistDatabaseException ex) {
            throw new RuntimeException(ex);
        }
        
        boolean allow = label.equals("commandwhitelister")
                || allows.stream()
                .filter(group -> group.equals("*") || PermissionsEx.getUser(player.getName()).inGroup(group))
                        .toArray().length != 0;

        if (!allow) {
            evt.setCancelled(true);
            player.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', 
                    config.getString("messages.blacklisted-error-message", "&cYou do not have permission to execute this command!"))));
        }
    }
}
