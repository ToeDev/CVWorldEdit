package org.cubeville.cvworldedit.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.cubeville.commons.commands.Command;
import org.cubeville.commons.commands.CommandResponse;
import org.cubeville.cvworldedit.CVWorldEdit;
import org.cubeville.cvworldedit.PlayerClipboard;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClearClipboard extends Command {

    final private PlayerClipboard pluginPlayerClipboard;

    final private String prefix;

    public ClearClipboard(CVWorldEdit plugin, PlayerClipboard pluginPlayerClipboard) {
        super("");

        this.pluginPlayerClipboard = pluginPlayerClipboard;

        prefix = plugin.getPrefix();
    }

    @Override
    public CommandResponse execute(Player sender, Set<String> set, Map<String, Object> map, List<Object> baseParameters) {

        if (baseParameters.size() > 0) {
            return new CommandResponse(prefix + ChatColor.RED + "Invalid Command!" + ChatColor.LIGHT_PURPLE + " Proper Usage: /weclearclipboard");
        }

        //Clear a players clipboard (can't paste after this)
        pluginPlayerClipboard.clearClipboard(sender.getUniqueId());
        pluginPlayerClipboard.clearBlocksCopied(sender.getUniqueId());

        return new CommandResponse(prefix + ChatColor.LIGHT_PURPLE + "Clipboard cleared!");
    }













}
