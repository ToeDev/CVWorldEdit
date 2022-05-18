package org.cubeville.cvworldedit.commands;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.extension.platform.permission.ActorSelectorLimits;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.util.Location;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.cubeville.commons.commands.*;
import org.cubeville.cvworldedit.CVWorldEdit;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class Pos2 extends Command {

    final private String prefix;

    public Pos2(CVWorldEdit plugin) {
        super("");

        prefix = plugin.getPrefix();
    }

    @Override
    public CommandResponse execute(Player sender, Set<String> set, Map<String, Object> map, List<Object> baseParameters) {

        if (baseParameters.size() > 0) {
            return new CommandResponse(prefix + ChatColor.RED + "Invalid Command!" + ChatColor.LIGHT_PURPLE + " Proper Usage: /cvpos2");
        }

        //Set player selection at position 2 and update their client
        BukkitPlayer bPlayer = BukkitAdapter.adapt(sender);
        LocalSession localSession = WorldEdit.getInstance().getSessionManager().get(bPlayer);
        RegionSelector selector = localSession.getRegionSelector(bPlayer.getWorld());
        Location pos = bPlayer.getBlockLocation();
        BlockVector3 posVec = BlockVector3.at(pos.getX(), pos.getY(), pos.getZ());
        selector.selectSecondary(posVec, ActorSelectorLimits.forActor(bPlayer));
        selector.explainRegionAdjust(bPlayer, localSession);
        if(selector.isDefined()) {
            Region playerSelection;
            try {
                playerSelection = selector.getRegion();
            } catch (IncompleteRegionException e) {
                return new CommandResponse(prefix + ChatColor.RED + "Selection not retrieved! Contact Administrator!");
            }
            return new CommandResponse(prefix + ChatColor.LIGHT_PURPLE + "Position 2 set at " + pos.getBlockX() + ", " + pos.getBlockY() + ", " + pos.getBlockZ() + " Block Count: " + playerSelection.getVolume());
        }

        return new CommandResponse(prefix + ChatColor.LIGHT_PURPLE + "Position 2 set at " + pos.getBlockX() + ", " + pos.getBlockY() + ", " + pos.getBlockZ());
    }
}
