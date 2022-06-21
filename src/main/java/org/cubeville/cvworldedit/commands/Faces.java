package org.cubeville.cvworldedit.commands;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.cubeville.commons.commands.Command;
import org.cubeville.commons.commands.CommandParameterString;
import org.cubeville.commons.commands.CommandResponse;
import org.cubeville.cvworldedit.CVWorldEdit;
import org.cubeville.cvworldedit.CheckBlacklist;
import org.cubeville.cvworldedit.CheckRegion;
import org.cubeville.cvworldedit.CommandCooldown;

import java.text.DecimalFormat;
import java.util.*;

public class Faces extends Command {

    final private CVWorldEdit plugin;
    final private CheckBlacklist pluginBlacklist;
    final private CheckRegion pluginCheckRegion;
    final private CommandCooldown pluginCommandCooldown;

    final private String prefix;

    public Faces(CVWorldEdit plugin, CheckBlacklist pluginBlacklist, CheckRegion pluginCheckRegion, CommandCooldown pluginCommandCooldown) {
        super("");
        addBaseParameter(new CommandParameterString()); //target block

        prefix = plugin.getPrefix();

        this.plugin = plugin;
        this.pluginBlacklist = pluginBlacklist;
        this.pluginCheckRegion = pluginCheckRegion;
        this.pluginCommandCooldown = pluginCommandCooldown;
    }

    @Override
    public CommandResponse execute(Player sender, Set<String> set, Map<String, Object> map, List<Object> baseParameters) {

        if (baseParameters.size() != 1) {
            return new CommandResponse(prefix + ChatColor.RED + "Invalid Command!" + ChatColor.LIGHT_PURPLE + " Proper Usage: /wewalls <targetblock>");
        }

        //Check if block(s) are valid or on the blacklist
        String[] tempTargetBlocks;
        Map<String, Integer> targetBlocks = new HashMap<>();
        String target = baseParameters.get(0).toString().toLowerCase();
        if(target.contains(",")) {
            tempTargetBlocks = target.split(",");
        } else {
            tempTargetBlocks = new String[] {target};
        }
        for(String s : tempTargetBlocks) {
            String i;
            if(s.contains("%")) {
                i = s.substring(0, s.indexOf("%"));
                s = s.replaceAll(".+%", "");
                try {
                    Integer.parseInt(i);
                } catch(NumberFormatException e) {
                    return new CommandResponse(prefix + ChatColor.RED + i + "% is not a valid percentage!");
                }
            } else {
                i = String.valueOf(1);
            }
            targetBlocks.put(s, Integer.valueOf(i));
        }
        for(String targetBlock : targetBlocks.keySet()) {
            if (BlockTypes.get(targetBlock) == null) {
                return new CommandResponse(prefix + ChatColor.RED + targetBlock.toUpperCase() + " is not a valid block!");
            }
            if(pluginBlacklist.checkBlockBanned(targetBlock)) {
                return new CommandResponse(prefix + ChatColor.RED + "You cannot WorldEdit the following block! " + ChatColor.GOLD + targetBlock);
            }
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

        //Check if the player's selection is larger than the max block volume limit
        int length = playerSelection.getLength();
        int width = playerSelection.getWidth();
        int height = playerSelection.getHeight();
        int walls = ((((length - 1) * 2) + ((width - 1) * 2)) * height);
        int floorCeiling = (((length - 2) * (width - 2)) * 2);
        if(plugin.getBlockVolumeLimit() < walls + floorCeiling) {
            return new CommandResponse(prefix + ChatColor.RED + "Your requested edit is too large! (" + ChatColor.GOLD + (walls + floorCeiling) + ChatColor.RED + ")" +  " The maximum block count per command is " + ChatColor.GOLD + plugin.getBlockVolumeLimit());
        }

        //Check if the player's selection is in a region they are owner of
        if(!pluginCheckRegion.isOwner(bPlayer, playerSelection)) {
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

        //Get the block pattern
        RandomPattern pattern = new RandomPattern();
        for(String block : targetBlocks.keySet()) {
            pattern.add(Objects.requireNonNull(BlockTypes.get(block)).getDefaultState(), targetBlocks.get(block));
        }

        //Set the blocks
        LocalSession localSession = WorldEdit.getInstance().getSessionManager().get(bPlayer);
        int blocksChanged;
        try (EditSession editSession = localSession.createEditSession(bPlayer)) {
            blocksChanged = editSession.makeFaces(playerSelection, pattern);
            localSession.remember(editSession);
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.YELLOW + "Unable to replace blocks in selection! (did volume exceed allowed amount?)");
            return new CommandResponse(prefix + ChatColor.RED + "You cannot WE that many of the following block type at once! " + ChatColor.GOLD + targetBlocks.keySet().toString().replace("[", "").replace("]", ""));
        }
        return new CommandResponse(prefix + ChatColor.LIGHT_PURPLE + "Setting " + blocksChanged + " " + targetBlocks.keySet().toString().replace("[", "").replace("]", ""));
    }
}
