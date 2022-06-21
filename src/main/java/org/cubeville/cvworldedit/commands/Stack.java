package org.cubeville.cvworldedit.commands;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.internal.annotation.Offset;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionOperationException;
import com.sk89q.worldedit.util.Direction;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.cubeville.commons.commands.*;
import org.cubeville.cvworldedit.CVWorldEdit;
import org.cubeville.cvworldedit.CheckRegion;
import org.cubeville.cvworldedit.CommandCooldown;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Stack extends Command {

    final private CVWorldEdit plugin;
    final private CheckRegion pluginCheckRegion;
    final private CommandCooldown pluginCommandCooldown;

    final private String prefix;

    public Stack(CVWorldEdit plugin, CheckRegion pluginCheckRegion, CommandCooldown pluginCommandCooldown) {
        super("");
        addBaseParameter(new CommandParameterInteger()); //number of blocks to stack
        addOptionalBaseParameter(new CommandParameterString()); //direction to stack

        prefix = plugin.getPrefix();

        this.plugin = plugin;
        this.pluginCheckRegion = pluginCheckRegion;
        this.pluginCommandCooldown = pluginCommandCooldown;
    }

    @Override
    public CommandResponse execute(Player sender, Set<String> set, Map<String, Object> map, List<Object> baseParameters) {

        if (baseParameters.size() > 2) {
            return new CommandResponse(prefix + ChatColor.RED + "Invalid Command!" + ChatColor.LIGHT_PURPLE + " Proper Usage: /westack <number> [direction]", ChatColor.LIGHT_PURPLE + "Example: /westack 5 north");
        }

        //Change players args into variables
        final int amount = Integer.parseInt(baseParameters.get(0).toString());
        final String direction;
        if(baseParameters.size() > 1) {
            direction = (String) baseParameters.get(1);
        } else {
            direction = plugin.getPlayerFacingSpecific(sender);
            sender.sendMessage(direction);
        }

        //Check if player has a selection made
        Region playerSelection;
        BukkitPlayer bPlayer = BukkitAdapter.adapt(sender);
        try {
            playerSelection = WorldEdit.getInstance().getSessionManager().get(bPlayer).getSelection(bPlayer.getWorld());
        } catch (IncompleteRegionException e) {
            Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.YELLOW + "Players selection returned null! (did they make a selection?)");
            return new CommandResponse(prefix + ChatColor.RED + "You haven't made a selection yet!");
        }

        //determine direction of region move
        int x = 0;
        int y = 0;
        int z = 0;
        boolean isPositiveY = true;
        boolean isPositiveX = true;
        boolean isPositiveZ = true;
        switch(direction.toLowerCase()) {
            case "up":
            case "u":
                y = amount;
                break;
            case "down":
            case "d":
                y = amount;
                isPositiveY = false;
                break;
            case "north":
            case "n":
                z = amount;
                isPositiveZ = false;
                break;
            case "northwest":
            case "nw":
                x = amount;
                isPositiveX = false;
                z = amount;
                isPositiveZ = false;
                break;
            case "northeast":
            case "ne":
                x = amount;
                z = amount;
                isPositiveZ = false;
                break;
            case "south":
            case "s":
                z = amount;
                break;
            case "southwest":
            case "sw":
                x = amount;
                isPositiveX = false;
                z = amount;
                break;
            case "west":
            case "w":
                x = amount;
                isPositiveX = false;
                break;
            case "southeast":
            case "se":
                x = amount;
                z = amount;
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
                    isPositiveX = false;
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
                    isPositiveZ = false;
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
                    isPositiveX = false;
                    break;
                }
                if(dirR.equals(Direction.WEST)) {
                    z = amount;
                    isPositiveZ = false;
                    break;
                }
                if(dirR.equals(Direction.EAST)) {
                    z = amount;
                    break;
                }
                return new CommandResponse(prefix + ChatColor.RED + "Not looking in a specific direction!" + ChatColor.LIGHT_PURPLE + " Ensure you are looking directly North, South, East, or West to use the left or right parameter.");
            default:
                return new CommandResponse(prefix + ChatColor.RED + "Invalid Command!" + ChatColor.LIGHT_PURPLE + " Proper Usage: /westack <number> [direction]", ChatColor.LIGHT_PURPLE + "Example: /westack 5 north");
        }

        //Check if the player's selection is larger than the max block volume limit
        if(plugin.getBlockVolumeLimit() < playerSelection.getVolume()) {
            return new CommandResponse(prefix + ChatColor.RED + "Your selection is too large! (" + ChatColor.GOLD + playerSelection.getVolume() + ChatColor.RED + ")" +  " The maximum block count per command is " + ChatColor.GOLD + plugin.getBlockVolumeLimit());
        }

        //Check if the player's selection plus movement is in a region they are owner of
        LocalSession localSession = WorldEdit.getInstance().getSessionManager().get(bPlayer);
        Region newPlayerSelection = playerSelection.clone();
        try {
            BlockVector3 size = newPlayerSelection.getMaximumPoint().subtract(newPlayerSelection.getMinimumPoint()).add(1, 1, 1);
            BlockVector3 shiftVector = BlockVector3.at(
                    ((isPositiveX) ? x : -x),
                    ((isPositiveY) ? y : -y),
                    ((isPositiveZ) ? z : -z)).multiply(size).multiply(amount);
            newPlayerSelection.shift(shiftVector);
            localSession.getRegionSelector(bPlayer.getWorld()).learnChanges();
            localSession.getRegionSelector(bPlayer.getWorld()).explainRegionAdjust(bPlayer, localSession);
        } catch (RegionOperationException e) {
            Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.RED + "Unable to shift/stack region/selection!");
            Bukkit.getConsoleSender().sendMessage(prefix + e);
            return new CommandResponse(prefix + ChatColor.RED + "Unable to shift region/selection! Contact Administrator!");
        }
        if(!pluginCheckRegion.isOwner(bPlayer, newPlayerSelection)) {
            return new CommandResponse(prefix + ChatColor.RED + "You cannot WorldEdit outside your plot! Please alter your selection!");
        }


        //Check if the player is on command cooldown check the CVWorldEditCommandCooldown class
        DecimalFormat format = new DecimalFormat("0.000");
        double cooldown = pluginCommandCooldown.getCommandCooldown(bPlayer.getUniqueId());
        if(cooldown != 0.0D) {
            return new CommandResponse(prefix + ChatColor.RED + "You cannot use that command yet! Please wait " + ChatColor.GOLD + format.format(cooldown) + " seconds" + ChatColor.RED + " before entering it again!");
        }

        //Start the command cooldown
        pluginCommandCooldown.startCommandCooldown(bPlayer.getUniqueId());

        //stack the selection
        try (EditSession editSession = localSession.createEditSession(bPlayer)) {
            editSession.stackCuboidRegion(playerSelection, BlockVector3.at(
                    ((isPositiveX) ? x : -x),
                    ((isPositiveY) ? y : -y),
                    ((isPositiveZ) ? z : -z)), amount, true);
        } catch (MaxChangedBlocksException e) {
            Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.RED + "Unable to stack region/selection!");
            Bukkit.getConsoleSender().sendMessage(prefix + e);
            return new CommandResponse(prefix + ChatColor.RED + "Unable to stack region/selection! Contact Administrator!");
        }

        return new CommandResponse(prefix + ChatColor.LIGHT_PURPLE + "Moving " + amount + " blocks to the " + direction);
    }
}
