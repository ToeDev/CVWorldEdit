package org.cubeville.cvworldedit.commands;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.cubeville.commons.commands.*;
import org.cubeville.commons.utils.BlockUtils;
import org.cubeville.cvworldedit.CVWorldEdit;
import org.cubeville.cvworldedit.CheckBlacklist;
import org.cubeville.cvworldedit.CheckRegion;


import java.util.*;

public class Copy extends Command {

    final private CVWorldEdit plugin;
    final private CheckBlacklist pluginBlacklist;
    final private CheckRegion pluginCheckRegion;

    final private String prefix;

    public Copy(CVWorldEdit plugin, CheckBlacklist pluginBlacklist, CheckRegion pluginCheckRegion) {
        super("");

        prefix = plugin.getPrefix();

        this.pluginBlacklist = pluginBlacklist;
        this.pluginCheckRegion = pluginCheckRegion;
        this.plugin = plugin;
    }

    @Override
    public CommandResponse execute(Player sender, Set<String> set, Map<String, Object> map, List<Object> baseParameters) {
        if (baseParameters.size() > 0) {
            return new CommandResponse(prefix + ChatColor.RED + "Invalid Command!" + ChatColor.LIGHT_PURPLE + " Proper Usage: Select an area then use /wecopy");
        }

        //Check if player has a cuboid selection made
        Region region;
        BukkitPlayer bPlayer = BukkitAdapter.adapt(sender);
        try {
            region = BlockUtils.getWESelection(sender);
        }
        catch (IllegalArgumentException e) {
            Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.YELLOW + "Players selection returned null! (did they make a selection?)");
            return new CommandResponse(prefix + ChatColor.RED + "You haven't made a selection yet!");
        }
        if(!(region instanceof CuboidRegion)) {
            return new CommandResponse(prefix + ChatColor.RED + "Only cuboid selection are allowed!");
        }

        //Check if the player's selection is larger than the max block volume limit
        if(plugin.getBlockVolumeLimit() < region.getVolume()) {
            return new CommandResponse(prefix + ChatColor.RED + "Your selection is too large! (" + ChatColor.GOLD + region.getVolume() + ChatColor.RED + ")" +  " The maximum block count per command is " + ChatColor.GOLD + plugin.getBlockVolumeLimit());
        }

        //Check if any blocks in the targetted selection are on the Blacklist
        for(BlockVector3 vec : region) {
            String block = bPlayer.getExtent().getBlock(vec).getBlockType().toString().toLowerCase().substring(10);
            if(pluginBlacklist.checkBlockBanned(block)) {
                return new CommandResponse(prefix + ChatColor.RED + "You cannot copy the following block with WorldEdit! " + ChatColor.GOLD + block);
            }
        }

        //Check if the player's selection is in a region they are owner of
        if(!pluginCheckRegion.isOwner(bPlayer, region)) {
            return new CommandResponse(prefix + ChatColor.RED + "You cannot use WorldEdit to copy builds outside your plot! Please alter your selection!");
        }

        //Copy the players selection and offset to the players clipboard
        CuboidRegion playerSelection = (CuboidRegion) region;
        BlockArrayClipboard clipboard = new BlockArrayClipboard(playerSelection);
        LocalSession localSession = WorldEdit.getInstance().getSessionManager().get(bPlayer);
        try (EditSession editSession = localSession.createEditSession(bPlayer)) {
            ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(editSession, playerSelection, clipboard, playerSelection.getMinimumPoint());
            forwardExtentCopy.setCopyingEntities(false);
            forwardExtentCopy.setCopyingBiomes(false);
            clipboard.setOrigin(localSession.getPlacementPosition(bPlayer));
            Operations.complete(forwardExtentCopy);
            localSession.setClipboard(new ClipboardHolder(clipboard));
        }
        catch (WorldEditException e) {
            Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.RED + "Unable to copy players selection to clipboard!");
            Bukkit.getConsoleSender().sendMessage(prefix + e);
            return new CommandResponse(prefix + ChatColor.RED + "Unable to copy selection to clipboard! Contact administrator!");
        }

        return new CommandResponse(prefix + ChatColor.LIGHT_PURPLE + "Copied " + clipboard.getRegion().getVolume() + " blocks.");
    }
}
