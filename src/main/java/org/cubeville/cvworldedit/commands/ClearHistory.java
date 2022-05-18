package org.cubeville.cvworldedit.commands;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.cubeville.commons.commands.Command;
import org.cubeville.commons.commands.CommandResponse;
import org.cubeville.cvworldedit.CVWorldEdit;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClearHistory extends Command {

    final private String prefix;

    public ClearHistory(CVWorldEdit plugin) {
        super("");

        prefix = plugin.getPrefix();
    }

    @Override
    public CommandResponse execute(Player sender, Set<String> set, Map<String, Object> map, List<Object> baseParameters) {

        if (baseParameters.size() > 0) {
            return new CommandResponse(prefix + ChatColor.RED + "Invalid Command!" + ChatColor.LIGHT_PURPLE + " Proper Usage: /cvclearhistory");
        }

        //Clear a players localsession history (cant use undo or redo after this)
        BukkitPlayer bPlayer = BukkitAdapter.adapt(sender);
        WorldEdit.getInstance().getSessionManager().get(bPlayer).clearHistory();

        return new CommandResponse(prefix + ChatColor.LIGHT_PURPLE + "History cleared!");
    }













}
