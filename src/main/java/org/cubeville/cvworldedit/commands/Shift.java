package org.cubeville.cvworldedit.commands;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionOperationException;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.util.Direction;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.cubeville.commons.commands.*;
import org.cubeville.cvworldedit.CVWorldEdit;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class Shift extends Command {

    final private CVWorldEdit plugin;
    final private String prefix;

    public Shift(CVWorldEdit plugin) {
        super("");
        addBaseParameter(new CommandParameterInteger()); //number of blocks to move
        addOptionalBaseParameter(new CommandParameterString()); //direction to move

        this.plugin = plugin;
        prefix = this.plugin.getPrefix();
    }

    @Override
    public CommandResponse execute(Player sender, Set<String> set, Map<String, Object> map, List<Object> baseParameters) {

        if (baseParameters.size() > 2) {
            return new CommandResponse(prefix + ChatColor.RED + "Invalid Command!" + ChatColor.LIGHT_PURPLE + " Proper Usage: /weshift <number> [direction]", ChatColor.LIGHT_PURPLE + "Example: /wemove 5 north");
        }

        //Change players args into variables
        final int amount = Integer.parseInt(baseParameters.get(0).toString());
        final String direction;
        if(baseParameters.size() > 1) {
            direction = (String) baseParameters.get(1);
        } else {
            direction = plugin.getPlayerFacing(sender);
        }

        //Get the players session, selector, region, etc
        BukkitPlayer bPlayer = BukkitAdapter.adapt(sender);
        LocalSession localSession = WorldEdit.getInstance().getSessionManager().get(bPlayer);
        RegionSelector selector = localSession.getRegionSelector(bPlayer.getWorld());
        Region playerSelection;
        try {
            playerSelection = localSession.getSelection(bPlayer.getWorld());
        } catch (IncompleteRegionException e) {
            Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.YELLOW + "Players selection returned null! (did they make a selection?)");
            return new CommandResponse(prefix + ChatColor.RED + "You haven't made a selection yet!");
        }

        //determine direction of region expansion
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
            case "left":
            case "l":
                Direction dirL = bPlayer.getCardinalDirection();
                if(dirL.equals(Direction.NORTH)) {
                    x = amount;
                    isPositive = false;
                    break;
                }
                if(dirL.equals(Direction.SOUTH)) {
                    x = amount;
                    break;
                }
                if(dirL.equals(Direction.WEST)) {
                    z = amount;
                    break;
                }
                if(dirL.equals(Direction.EAST)) {
                    z = amount;
                    isPositive = false;
                    break;
                }
                return new CommandResponse(prefix + ChatColor.RED + "Not looking in a specific direction!" + ChatColor.LIGHT_PURPLE + " Ensure you are looking directly North, South, East, or West to use the left or right parameter.");
            case "right":
            case "r":
                Direction dirR = bPlayer.getCardinalDirection();
                if(dirR.equals(Direction.NORTH)) {
                    x = amount;
                    break;
                }
                if(dirR.equals(Direction.SOUTH)) {
                    x = amount;
                    isPositive = false;
                    break;
                }
                if(dirR.equals(Direction.WEST)) {
                    z = amount;
                    isPositive = false;
                    break;
                }
                if(dirR.equals(Direction.EAST)) {
                    z = amount;
                    break;
                }
                return new CommandResponse(prefix + ChatColor.RED + "Not looking in a specific direction!" + ChatColor.LIGHT_PURPLE + " Ensure you are looking directly North, South, East, or West to use the left or right parameter.");
            default:
                return new CommandResponse(prefix + ChatColor.RED + "Invalid Command!" + ChatColor.LIGHT_PURPLE + " Proper Usage: /weshift <number> [direction]", ChatColor.LIGHT_PURPLE + "Example: /wemove 5 north");
        }

        //apply region move
        try {
            playerSelection.shift(BlockVector3.at(
                    ((isPositive) ? x : -x),
                    ((isPositive) ? y : -y),
                    ((isPositive) ? z : -z)));
        } catch (RegionOperationException e) {
            Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.RED + "Unable to shift region/selection!");
            Bukkit.getConsoleSender().sendMessage(prefix + e);
            return new CommandResponse(prefix + ChatColor.RED + "Unable to shift region/selection! Contact Administrator!");
        }
        selector.learnChanges();
        selector.explainRegionAdjust(bPlayer, localSession);

        return new CommandResponse(prefix + ChatColor.LIGHT_PURPLE + "Selection shifted " + amount + " blocks to " + direction);
    }
}
