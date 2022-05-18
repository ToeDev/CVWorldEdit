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
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.cubeville.commons.commands.*;
import org.cubeville.commons.utils.BlockUtils;
import org.cubeville.cvworldedit.CVWorldEdit;
import org.cubeville.cvworldedit.CheckBlacklist;
import org.cubeville.cvworldedit.CheckRegion;


import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Copy extends Command {

    final private Logger logger;

    final private CVWorldEdit plugin;
    final private CheckBlacklist pluginBlacklist;
    final private CheckRegion pluginCheckRegion;
    final private Rotate pluginRotate;

    final private HashMap<UUID, BlockArrayClipboard> clipboardList;
    final private HashMap<UUID, Integer> blocksCopiedList;

    final private String prefix;

    public Copy(CVWorldEdit plugin, Rotate pluginRotate, CheckBlacklist pluginBlacklist, CheckRegion pluginCheckRegion) {
        super("");

        prefix = plugin.getPrefix();

        this.pluginRotate = pluginRotate;
        this.pluginBlacklist = pluginBlacklist;
        this.pluginCheckRegion = pluginCheckRegion;
        this.plugin = plugin;
        this.clipboardList = plugin.getClipboardList();
        this.blocksCopiedList = plugin.getBlocksCopiedList();

        this.logger = plugin.getLogger();
    }

    @Override
    public CommandResponse execute(Player sender, Set<String> set, Map<String, Object> map, List<Object> baseParameters) {
        if (baseParameters.size() > 0) {
            return new CommandResponse(prefix + ChatColor.RED + "Invalid Command!" + ChatColor.LIGHT_PURPLE + " Proper Usage: Select an area then use /cvcopy");
        }

        //Check if player has a cuboid selection made
        Region region;
        BukkitPlayer bPlayer = BukkitAdapter.adapt(sender);
        try {
            region = BlockUtils.getWESelection(sender);
        }
        catch (IllegalArgumentException e) {
            this.logger.log(Level.WARNING, "Players selection returned null", e);
            return new CommandResponse(prefix + ChatColor.RED + "You haven't made a selection yet!");
        }
        if(!(region instanceof CuboidRegion)) {
            return new CommandResponse(prefix + ChatColor.RED + "Only cuboid selection are allowed!");
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

        //Check if the player's selection is larger than the max block volume limit
        if(plugin.getBlockVolumeLimit() < region.getVolume()) {
            return new CommandResponse(prefix + ChatColor.RED + "Your selection is too large! (" + ChatColor.GOLD + region.getVolume() + ChatColor.RED + ")" +  " The maximum block count per command is " + ChatColor.GOLD + plugin.getBlockVolumeLimit());
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
            int blocksCopied = forwardExtentCopy.getAffected();
            blocksCopiedList.put(sender.getUniqueId(), blocksCopied);
            clipboardList.put(sender.getUniqueId(), clipboard);
            if(pluginRotate.getRotation(sender) != 0) {
                pluginRotate.clearRotation(sender);
            }
        }
        catch (WorldEditException e) {
            this.logger.log(Level.WARNING, "Unable to copy players selection to clipboard!");
            return new CommandResponse(prefix + ChatColor.RED + "Unable to copy selection to clipboard! Contact administrator!");
        }

        return new CommandResponse(prefix + ChatColor.LIGHT_PURPLE + "Copied " + blocksCopiedList.get(sender.getUniqueId()) + " blocks.");
    }

    public void putClipboard(Player sender, BlockArrayClipboard clipboard) {
        clipboardList.put(sender.getUniqueId(), clipboard);
    }

    public BlockArrayClipboard getClipboard(Player sender) {
        if(clipboardList.containsKey(sender.getUniqueId())) {
            return clipboardList.get(sender.getUniqueId());
        }
        return null;
    }

    public void clearClipboard(Player sender) {
        if(clipboardList.containsKey(sender.getUniqueId())) {
            clipboardList.remove(sender.getUniqueId());
        }
    }

    public int getBlocksCopied(Player sender) {
        return blocksCopiedList.get(sender.getUniqueId());
    }
}
