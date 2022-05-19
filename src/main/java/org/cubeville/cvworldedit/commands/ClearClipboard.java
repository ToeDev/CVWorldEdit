package org.cubeville.cvworldedit.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.cubeville.commons.commands.Command;
import org.cubeville.commons.commands.CommandResponse;
import org.cubeville.cvworldedit.CVWorldEdit;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClearClipboard extends Command {

    final private Copy pluginCopy;

    final private String prefix;

    public ClearClipboard(CVWorldEdit plugin, Copy pluginCopy) {
        super("");

        this.pluginCopy = pluginCopy;

        prefix = plugin.getPrefix();
    }

    @Override
    public CommandResponse execute(Player sender, Set<String> set, Map<String, Object> map, List<Object> baseParameters) {

        if (baseParameters.size() > 0) {
            return new CommandResponse(prefix + ChatColor.RED + "Invalid Command!" + ChatColor.LIGHT_PURPLE + " Proper Usage: /weclearclipboard");
        }

        //Clear a players clipboard (can't paste after this)
        pluginCopy.clearClipboard(sender);

        return new CommandResponse(prefix + ChatColor.LIGHT_PURPLE + "Clipboard cleared!");
    }













}
