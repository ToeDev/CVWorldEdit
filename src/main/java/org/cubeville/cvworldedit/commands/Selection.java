package org.cubeville.cvworldedit.commands;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.regions.RegionSelector;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.cubeville.commons.commands.Command;
import org.cubeville.commons.commands.CommandResponse;
import org.cubeville.cvworldedit.CVWorldEdit;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class Selection extends Command {

    final private String prefix;

    public Selection(CVWorldEdit plugin) {
        super("");

        prefix = plugin.getPrefix();
    }

    @Override
    public CommandResponse execute(Player sender, Set<String> set, Map<String, Object> map, List<Object> baseParameters) {

        if (baseParameters.size() > 0) {
            return new CommandResponse(prefix + ChatColor.RED + "Invalid Command!" + ChatColor.LIGHT_PURPLE + " Proper Usage: /cvsel or /cvselection");
        }

        //Obtain volume, positions, and dimensions of players current selection
        BukkitPlayer bPlayer = BukkitAdapter.adapt(sender);
        LocalSession localSession = WorldEdit.getInstance().getSessionManager().get(bPlayer);
        RegionSelector selector = localSession.getRegionSelector(bPlayer.getWorld());
        selector.clear();
        selector.explainRegionAdjust(bPlayer, localSession);
        return new CommandResponse(prefix + ChatColor.LIGHT_PURPLE + "Selection cleared!");
    }
}