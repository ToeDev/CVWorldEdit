package org.cubeville.cvworldedit;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionOperationException;
import com.sk89q.worldedit.regions.RegionSelector;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.cubeville.commons.commands.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CVWorldEditContract extends Command {

    final private Logger logger;

    final private String prefix;

    public CVWorldEditContract(CVWorldEdit plugin) {
        super("");
        addBaseParameter(new CommandParameterString()); //number of blocks to expand
        addBaseParameter(new CommandParameterString()); //direction to expand

        prefix = plugin.getPrefix();

        this.logger = plugin.getLogger();
    }

    @Override
    public CommandResponse execute(Player sender, Set<String> set, Map<String, Object> map, List<Object> baseParameters) {

        if (baseParameters.size() != 2 || !checkInt(baseParameters.get(0).toString()) || checkInt(baseParameters.get(1).toString())) {
            return new CommandResponse(prefix + ChatColor.RED + "Invalid Command!" + ChatColor.LIGHT_PURPLE + " Proper Usage: /cvcontract <number> <direction>", ChatColor.LIGHT_PURPLE + "Example: /cvcontract 5 north");
        }

        //Change players args into variables
        final int amount = Integer.parseInt(baseParameters.get(0).toString());
        final String direction = baseParameters.get(1).toString();

        //Get the players session, selector, region, etc
        BukkitPlayer bPlayer = BukkitAdapter.adapt(sender);
        LocalSession localSession = WorldEdit.getInstance().getSessionManager().get(bPlayer);
        RegionSelector selector = localSession.getRegionSelector(bPlayer.getWorld());
        Region playerSelection;
        try {
            playerSelection = localSession.getSelection(bPlayer.getWorld());
        } catch (IncompleteRegionException e) {
            this.logger.log(Level.WARNING, "Unable to get players WE selection");
            return new CommandResponse(prefix + ChatColor.RED + "You haven't made a selection yet!");
        }

        //determine direction of region contraction
        int x = 0;
        int y = 0;
        int z = 0;
        boolean isPositive = true;
        switch(direction.toLowerCase()) {
            case "up":
            case "u":
                y = amount;
                break;
            case "down":
            case "d":
                y = amount;
                isPositive = false;
                break;
            case "north":
            case "n":
                z = amount;
                isPositive = false;
                break;
            case "south":
            case "s":
                z = amount;
                break;
            case "west":
            case "w":
                x = amount;
                isPositive = false;
                break;
            case "east":
            case "e":
                x = amount;
                break;
            default:
                return new CommandResponse(prefix + ChatColor.RED + "Invalid Command!" + ChatColor.LIGHT_PURPLE + " Proper Usage: /cvcontract <number> <direction>", ChatColor.LIGHT_PURPLE + "Example: /cvcontract 5 north");
        }

        //apply region expansion
        long oldSize = playerSelection.getVolume();
        try {
            playerSelection.contract(BlockVector3.at(
                    ((isPositive) ? x : -x),
                    ((isPositive) ? y : -y),
                    ((isPositive) ? z : -z)));

        } catch (RegionOperationException e) {
            this.logger.log(Level.WARNING, "Unable to contract region/selection");
            return new CommandResponse(prefix + ChatColor.RED + "Unable to contract region/selection! Contact Administrator!");
        }
        selector.learnChanges();
        selector.explainRegionAdjust(bPlayer, localSession);
        long newSize = playerSelection.getVolume();

        return new CommandResponse(prefix + ChatColor.LIGHT_PURPLE + "Selection contracted " + (oldSize - newSize) + " blocks.");
    }

    public boolean checkInt(String arg) {
        try {
            Integer.parseInt(arg);
            return true;
        }
        catch (NumberFormatException e) {
            return false;
        }
    }
}
