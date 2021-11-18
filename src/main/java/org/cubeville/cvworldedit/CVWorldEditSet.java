package org.cubeville.cvworldedit;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.cubeville.commons.commands.*;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CVWorldEditSet extends Command {

    final private Logger logger;

    final private CVWorldEdit plugin;
    final private CVWorldEditCheckBlacklist pluginBlacklist;
    final private CVWorldEditCheckRegion pluginCheckRegion;
    final private CVWorldEditCommandCooldown pluginCommandCooldown;

    final private String prefix;

    public CVWorldEditSet(CVWorldEdit plugin, CVWorldEditCheckBlacklist pluginBlacklist, CVWorldEditCheckRegion pluginCheckRegion, CVWorldEditCommandCooldown pluginCommandCooldown) {
        super("");
        addBaseParameter(new CommandParameterString()); //target block

        prefix = plugin.getPrefix();

        this.plugin = plugin;
        this.pluginBlacklist = pluginBlacklist;
        this.pluginCheckRegion = pluginCheckRegion;
        this.pluginCommandCooldown = pluginCommandCooldown;

        this.logger = plugin.getLogger();
    }

    @Override
    public CommandResponse execute(Player sender, Set<String> set, Map<String, Object> map, List<Object> baseParameters) {

        if (baseParameters.size() != 1) {
            return new CommandResponse(prefix + ChatColor.RED + "Invalid Command!" + ChatColor.LIGHT_PURPLE + " Proper Usage: /cvset <targetblock>");
        }
        String targetBlock = baseParameters.get(0).toString().toLowerCase();
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
            this.logger.log(Level.WARNING, "Unable to get players WE selection");
            return new CommandResponse(prefix + ChatColor.RED + "You haven't made a selection yet!");
        }

        //Check if the player's selection is in a region they are owner of
        if(!pluginCheckRegion.isOwner(bPlayer, playerSelection)) {
            return new CommandResponse(prefix + ChatColor.RED + "You cannot WorldEdit outside your plot! Please alter your selection!");
        }

        //Check if the player's selection is larger than the max block volume limit
        if(plugin.getBlockVolumeLimit() < playerSelection.getVolume()) {
            return new CommandResponse(prefix + ChatColor.RED + "Your selection is too large! (" + ChatColor.GOLD + playerSelection.getVolume() + ChatColor.RED + ")" +  " The maximum block count per command is " + ChatColor.GOLD + plugin.getBlockVolumeLimit());
        }

        //Check if the player is on command cooldown check the CVWorldEditCommandCooldown class
        DecimalFormat format = new DecimalFormat("0.000");
        double cooldown = pluginCommandCooldown.getCommandCooldown(bPlayer.getUniqueId());
        if(cooldown != 0.0D) {
            return new CommandResponse(prefix + ChatColor.RED + "You cannot use that command yet! Please wait " + ChatColor.GOLD + format.format(cooldown) + " seconds" + ChatColor.RED + " before entering it again!");
        }

        //Start the command cooldown
        pluginCommandCooldown.startCommandCooldown(bPlayer.getUniqueId());

        //Set the blocks
        LocalSession localSession = WorldEdit.getInstance().getSessionManager().get(bPlayer);
        int blocksChanged;
        try (EditSession editSession = localSession.createEditSession(bPlayer)) {
            blocksChanged = editSession.setBlocks(playerSelection, Objects.requireNonNull(BlockTypes.get(targetBlock)).getDefaultState());
            localSession.remember(editSession);
        } catch (MaxChangedBlocksException e) {
            this.logger.log(Level.WARNING, "Unable to replace blocks in selection!", e);
            return new CommandResponse(prefix + ChatColor.RED + "Unable to replace blocks in selection! Contact administrator!");
        }
        return new CommandResponse(prefix + ChatColor.LIGHT_PURPLE + "Setting " + blocksChanged + " " + targetBlock.toUpperCase());
    }
}
