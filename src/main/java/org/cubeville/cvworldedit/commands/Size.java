package org.cubeville.cvworldedit.commands;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.cubeville.commons.commands.Command;
import org.cubeville.commons.commands.CommandResponse;
import org.cubeville.cvworldedit.CVWorldEdit;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class Size extends Command {

    final private String prefix;

    public Size(CVWorldEdit plugin) {
        super("");

        prefix = plugin.getPrefix();
    }

    @Override
    public CommandResponse execute(Player sender, Set<String> set, Map<String, Object> map, List<Object> baseParameters) {

        if (baseParameters.size() > 0) {
            return new CommandResponse(prefix + ChatColor.RED + "Invalid Command!" + ChatColor.LIGHT_PURPLE + " Proper Usage: /wesize");
        }

        //Obtain volume, positions, and dimensions of players current selection
        BukkitPlayer bPlayer = BukkitAdapter.adapt(sender);
        LocalSession localSession = WorldEdit.getInstance().getSessionManager().get(bPlayer);
        Region playerSelection;
        try {
            playerSelection = localSession.getSelection(bPlayer.getWorld());
        } catch (IncompleteRegionException e) {
            Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.YELLOW + "Players selection returned null! (did they make a selection?)");
            return new CommandResponse(prefix + ChatColor.RED + "You haven't made a selection yet!");
        }
        long selVol = playerSelection.getVolume();
        BlockVector3 selDim = playerSelection.getMaximumPoint().subtract(playerSelection.getMinimumPoint()).add(1,1,1);
        BlockVector3 selMin = playerSelection.getMinimumPoint();
        BlockVector3 selMax = playerSelection.getMaximumPoint();

        return new CommandResponse(prefix + ChatColor.LIGHT_PURPLE + "Corner 1: " + selMin, prefix + ChatColor.LIGHT_PURPLE + "Corner 2: " + selMax, prefix + ChatColor.LIGHT_PURPLE + "Dimensions: " + selDim, prefix + ChatColor.LIGHT_PURPLE + "Block Count: " + selVol);
    }
}
