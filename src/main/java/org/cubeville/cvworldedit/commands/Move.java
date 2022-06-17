package org.cubeville.cvworldedit.commands;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
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

public class Move extends Command {

    final private CVWorldEdit plugin;
    final private CheckRegion pluginCheckRegion;
    final private CommandCooldown pluginCommandCooldown;

    final private String prefix;

    public Move(CVWorldEdit plugin, CheckRegion pluginCheckRegion, CommandCooldown pluginCommandCooldown) {
        super("");
        addBaseParameter(new CommandParameterString()); //number of blocks to move
        addBaseParameter(new CommandParameterString()); //direction to move

        prefix = plugin.getPrefix();

        this.plugin = plugin;
        this.pluginCheckRegion = pluginCheckRegion;
        this.pluginCommandCooldown = pluginCommandCooldown;
    }

    @Override
    public CommandResponse execute(Player sender, Set<String> set, Map<String, Object> map, List<Object> baseParameters) {

        if (baseParameters.size() != 2 || !checkInt(baseParameters.get(0).toString()) || checkInt(baseParameters.get(1).toString())) {
            return new CommandResponse(prefix + ChatColor.RED + "Invalid Command!" + ChatColor.LIGHT_PURPLE + " Proper Usage: /wemove <number> <direction>", ChatColor.LIGHT_PURPLE + "Example: /wemove 5 north");
        }

        //Change players args into variables
        final int amount = Integer.parseInt(baseParameters.get(0).toString());
        final String direction = baseParameters.get(1).toString();

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
                return new CommandResponse(prefix + ChatColor.RED + "Invalid Command!" + ChatColor.LIGHT_PURPLE + " Proper Usage: /weexpand <number> <direction>", ChatColor.LIGHT_PURPLE + "Example: /weexpand 5 north");
        }

        //Check if the player's selection is larger than the max block volume limit
        if(plugin.getBlockVolumeLimit() < playerSelection.getVolume()) {
            return new CommandResponse(prefix + ChatColor.RED + "Your selection is too large! (" + ChatColor.GOLD + playerSelection.getVolume() + ChatColor.RED + ")" +  " The maximum block count per command is " + ChatColor.GOLD + plugin.getBlockVolumeLimit());
        }

        //Check if the player's selection plus movement is in a region they are owner of
        Region newPlayerSelection = playerSelection.clone();
        try {
            newPlayerSelection.expand(BlockVector3.at(
                    ((isPositive) ? x : -x),
                    ((isPositive) ? y : -y),
                    ((isPositive) ? z : -z)));
        } catch (RegionOperationException e) {
            Bukkit.getConsoleSender().sendMessage(prefix +  ChatColor.RED + "Unable to expand region/selection!");
            Bukkit.getConsoleSender().sendMessage(prefix + e);
            return new CommandResponse(prefix + ChatColor.RED + "Unable to expand region/selection! Contact Administrator!");
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

        //move the selection
        LocalSession localSession = WorldEdit.getInstance().getSessionManager().get(bPlayer);
        int blocksChanged;
        try (EditSession editSession = localSession.createEditSession(bPlayer)) {
            blocksChanged = editSession.moveRegion(playerSelection, BlockVector3.at(
                    ((isPositive) ? x : -x),
                    ((isPositive) ? y : -y),
                    ((isPositive) ? z : -z)),
                    1, false, null);
        } catch (MaxChangedBlocksException e) {
            Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.RED + "Unable to replace blocks in selection!");
            Bukkit.getConsoleSender().sendMessage(prefix + e);
            return new CommandResponse(prefix + ChatColor.RED + "Unable to replace blocks in selection! Contact administrator!");
        }
        return new CommandResponse(prefix + ChatColor.LIGHT_PURPLE + "Moving " + blocksChanged + " blocks.");
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
