package org.cubeville.cvworldedit.commands;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extension.platform.Locatable;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.mask.BlockTypeMask;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.ChatColor;

import org.bukkit.entity.Player;
import org.cubeville.commons.commands.*;
import org.cubeville.cvworldedit.CVWorldEdit;
import org.cubeville.cvworldedit.CheckBlacklist;
import org.cubeville.cvworldedit.CheckRegion;
import org.cubeville.cvworldedit.CommandCooldown;

import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Replace extends Command {

    final private Logger logger;

    final private CVWorldEdit plugin;
    final private CommandCooldown pluginCommandCooldown;
    final private CheckBlacklist pluginBlacklist;
    final private CheckRegion pluginCheckRegion;

    final private String prefix;

    public Replace(CVWorldEdit plugin, CheckBlacklist pluginBlacklist, CheckRegion pluginCheckRegion, CommandCooldown pluginCommandCooldown) {
        super("");
        addBaseParameter(new CommandParameterString()); //source block
        addBaseParameter(new CommandParameterString()); //target block

        this.prefix = plugin.getPrefix();

        this.plugin = plugin;
        this.pluginCommandCooldown = pluginCommandCooldown;
        this.pluginBlacklist = pluginBlacklist;
        this.pluginCheckRegion = pluginCheckRegion;

        this.logger = plugin.getLogger();
    }

    @Override
    public CommandResponse execute(Player sender, Set<String> set, Map<String, Object> map, List<Object> baseParameters) {

        if (baseParameters.size() != 2) {
            return new CommandResponse(prefix + ChatColor.RED + "Invalid Command!" + ChatColor.LIGHT_PURPLE + " Proper Usage: /wereplace <sourceblock> <targetblock>");
        }
        String sourceBlock = baseParameters.get(0).toString().toLowerCase();
        String targetBlock = baseParameters.get(1).toString().toLowerCase();
        if (BlockTypes.get(sourceBlock) == null) {
            return new CommandResponse(prefix + ChatColor.RED + sourceBlock.toUpperCase() + " is not a valid block!");
        }
        if (BlockTypes.get(targetBlock) == null) {
            return new CommandResponse(prefix + ChatColor.RED + targetBlock.toUpperCase() + " is not a valid block!");
        }

        //Check if target block is on the Block Blacklist
        if(pluginBlacklist.checkBlockBanned(targetBlock)) {
            return new CommandResponse(prefix + ChatColor.RED + "You cannot WorldEdit the following block! " + ChatColor.GOLD + targetBlock);
        }

        //Check if player has a selection made
        Region playerSelection;
        BukkitPlayer bPlayer = BukkitAdapter.adapt(sender);
        try {
            playerSelection = WorldEdit.getInstance().getSessionManager().get(bPlayer).getSelection(bPlayer.getWorld());
        } catch (IncompleteRegionException e) {
            this.logger.log(Level.WARNING, "Unable to get players WE selection", e);
            return new CommandResponse(prefix + ChatColor.RED + "You haven't made a selection yet!");
        }

        //Check if the player's selection is in a region they are owner of
        if(!pluginCheckRegion.isOwner(bPlayer, playerSelection)) {
            return new CommandResponse(prefix + ChatColor.RED + "You cannot WorldEdit outside your plot! Please alter your selection!");
        }

        //Get a parsercontext and extent for the Mask and Target
        LocalSession session = WorldEdit.getInstance().getSessionManager().get(bPlayer);
        ParserContext parserContext = new ParserContext();
        parserContext.setActor(bPlayer);
        Extent extent = ((Locatable) bPlayer).getExtent();
        parserContext.setWorld((World) extent);
        parserContext.setSession(session);
        parserContext.setRestricted(true);

        //Get a Mask(for source) and Pattern(for target)
        BlockTypeMask from = new BlockTypeMask(extent, BlockTypes.get(sourceBlock));
        Pattern to;
        try {
            to = WorldEdit.getInstance().getPatternFactory().parseFromInput(targetBlock, parserContext);
        } catch (InputParseException e) {
            this.logger.log(Level.WARNING, "Unable to get pattern from targetBlock", e);
            return new CommandResponse(prefix + ChatColor.RED + "Unable to use target block! Contact administrator!");
        }

        //Check if the player's selection is larger than the max block volume limit
        LocalSession localSession = WorldEdit.getInstance().getSessionManager().get(bPlayer);
        int blocksChanging;
        try (EditSession editSession = localSession.createEditSession(bPlayer)) {
            blocksChanging = editSession.countBlocks(playerSelection, from);
        }
        if(plugin.getBlockVolumeLimit() < blocksChanging) {
            return new CommandResponse(prefix + ChatColor.RED + "Your selection is too large! (" + ChatColor.GOLD + blocksChanging + ChatColor.RED + ")" +  " The maximum block count per command is " + ChatColor.GOLD + plugin.getBlockVolumeLimit());
        }

        //Check if the player is on command cooldown check the CVWorldEditCommandCooldown class
        DecimalFormat format = new DecimalFormat("0.000");
        double cooldown = pluginCommandCooldown.getCommandCooldown(bPlayer.getUniqueId());
        if(cooldown != 0.0D) {
            return new CommandResponse(prefix + ChatColor.RED + "You cannot use that command yet! Please wait " + ChatColor.GOLD + format.format(cooldown) + " seconds" + ChatColor.RED + " before entering it again!");
        }

        //Start the command cooldown
        pluginCommandCooldown.startCommandCooldown(bPlayer.getUniqueId());

        //Replace the blocks
        int blocksChanged;
        try (EditSession editSession = localSession.createEditSession(bPlayer)) {
            blocksChanged = editSession.replaceBlocks(playerSelection, from, to);
            localSession.remember(editSession);
        } catch (Exception e) {
            this.logger.log(Level.WARNING, "Unable to replace blocks in selection!");
            return new CommandResponse(prefix + ChatColor.RED + "You cannot WE that many of the following block type at once! " + ChatColor.GOLD + targetBlock);
        }
        return new CommandResponse(prefix + ChatColor.LIGHT_PURPLE + "Replacing " + blocksChanged + " " + sourceBlock.toUpperCase() + " with " + targetBlock.toUpperCase());
    }

















}
