package org.cubeville.cvworldedit;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.cubeville.commons.commands.Command;
import org.cubeville.commons.commands.CommandResponse;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CVWorldEditSize extends Command {

    final private Logger logger;

    final private String prefix;

    public CVWorldEditSize(CVWorldEdit plugin) {
        super("");

        prefix = plugin.getPrefix();

        this.logger = plugin.getLogger();
    }

    @Override
    public CommandResponse execute(Player sender, Set<String> set, Map<String, Object> map, List<Object> baseParameters) {

        if (baseParameters.size() > 0) {
            return new CommandResponse(prefix + ChatColor.RED + "Invalid Command!" + ChatColor.LIGHT_PURPLE + " Proper Usage: /cvsize");
        }

        //Obtain volume, positions, and dimensions of players current selection
        BukkitPlayer bPlayer = BukkitAdapter.adapt(sender);
        LocalSession localSession = WorldEdit.getInstance().getSessionManager().get(bPlayer);
        Region playerSelection;
        try {
            playerSelection = localSession.getSelection(bPlayer.getWorld());
        } catch (IncompleteRegionException e) {
            this.logger.log(Level.WARNING, "Players selection returned null", e);
            return new CommandResponse(prefix + ChatColor.RED + "You haven't made a selection yet!");
        }
        long selVol = playerSelection.getVolume();
        BlockVector3 selDim = playerSelection.getMaximumPoint().subtract(playerSelection.getMinimumPoint()).add(1,1,1);
        BlockVector3 selMin = playerSelection.getMinimumPoint();
        BlockVector3 selMax = playerSelection.getMaximumPoint();

        return new CommandResponse(prefix + ChatColor.LIGHT_PURPLE + "Corner 1: " + selMin, prefix + ChatColor.LIGHT_PURPLE + "Corner 2: " + selMax, prefix + ChatColor.LIGHT_PURPLE + "Dimensions: " + selDim, prefix + ChatColor.LIGHT_PURPLE + "Block Count: " +selVol);
    }
}
