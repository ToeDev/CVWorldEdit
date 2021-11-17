package org.cubeville.cvworldedit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.cubeville.commons.commands.*;
import org.cubeville.commons.utils.BlockUtils;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CVWorldEditClearPlot extends Command {

    final private Logger logger;
    final private Plugin plugin;
    final private BukkitScheduler scheduler;

    final private String prefix;

    final private int plotVolume;
    final private HashMap<UUID, Integer> taskIDCheckList;
    final private HashMap<UUID, Integer> taskIDClearList;
    final private HashMap<String, Integer> taskIDAdminClearList;

    public CVWorldEditClearPlot(CVWorldEdit plugin) {
        super("");
        addOptionalBaseParameter(new CommandParameterString()); //target plot

        prefix = ChatColor.GRAY + "[" + ChatColor.DARK_RED + "CVWorldEdit" + ChatColor.GRAY + "]" + " ";

        plotVolume = 820080; //22500000 (cvsize)
        this.taskIDCheckList = plugin.getTaskIDCheckList();
        this.taskIDClearList = plugin.getTaskIDClearList();
        this.taskIDAdminClearList = plugin.getTaskIDAdminClearList();

        this.scheduler = plugin.getServer().getScheduler();
        this.logger = plugin.getLogger();
        this.plugin = plugin;
    }

    @Override
    public CommandResponse execute(Player sender, Set<String> set, Map<String, Object> map, List<Object> baseParameters) {

        if(baseParameters.size() > 1) {
            return new CommandResponse(prefix + ChatColor.RED + "Invalid Command!" + ChatColor.LIGHT_PURPLE + " Proper Usage: /cvclearplot");
        }
        if(baseParameters.size() == 1 && !sender.hasPermission("CVWorldEdit.admin")) {
            return new CommandResponse(prefix + ChatColor.RED + "Invalid Command!" + ChatColor.LIGHT_PURPLE + " Proper Usage: /cvclearplot");
        }
        if(baseParameters.size() == 1 && sender.hasPermission("CVWorldEdit.admin")) {
            ProtectedRegion targetRegion;
            try {
                targetRegion = BlockUtils.getWGRegion(sender.getWorld(), baseParameters.get(0).toString());
            } catch (IllegalArgumentException e) {
                this.logger.log(Level.WARNING, "No region found by the name " + baseParameters.get(0).toString());
                return new CommandResponse(prefix + ChatColor.RED + "No region found by the name " + baseParameters.get(0).toString());
            }
            return adminClearPlot(sender, targetRegion);
        } else {
            //check if a plotclear is already running
            if (taskIDClearList.containsKey(sender.getUniqueId()) && taskIDClearList.get(sender.getUniqueId()) != 0) {
                return new CommandResponse(prefix + ChatColor.RED + "Plotclear in-progress! Please wait!");
            }

            //check if the player has already entered /cvclearplot once and it entering again to confirm
            if (!taskIDCheckList.containsKey(sender.getUniqueId()) || taskIDCheckList.get(sender.getUniqueId()).equals(0)) {
                ProtectedRegion playerRegion;
                LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(sender);
                RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
                RegionQuery regionQuery = regionContainer.createQuery();
                ApplicableRegionSet applicableRegionSet = regionQuery.getApplicableRegions(localPlayer.getLocation());
                playerRegion = regionCheck(localPlayer, applicableRegionSet);
                if (playerRegion != null) {
                    return clearPlotConfirm(sender, playerRegion);
                } else {
                    return new CommandResponse(prefix + ChatColor.RED + "Stand in your plot to perform the /cvclearplot command!");
                }
            } else {
                ProtectedRegion playerRegion;
                LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(sender);
                RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
                RegionQuery regionQuery = regionContainer.createQuery();
                ApplicableRegionSet applicableRegionSet = regionQuery.getApplicableRegions(localPlayer.getLocation());
                playerRegion = regionCheck(localPlayer, applicableRegionSet);
                scheduler.cancelTask(taskIDCheckList.get(sender.getUniqueId()));
                taskIDCheckList.put(sender.getUniqueId(), 0);
                return clearPlot(sender, playerRegion);
            }
        }
    }

    //check if they are owner on the region they are standing in. - also checks the volume of the region to ensure it is a normal plot and not a random region that an A+ might be owner on
    public ProtectedRegion regionCheck(LocalPlayer localPlayer, ApplicableRegionSet applicableRegionSet) {
        for (ProtectedRegion region : applicableRegionSet) {
            if (region.isOwner(localPlayer) && region.volume() == plotVolume) {
                return region;
            }
        }
        return null;
    }

    //wait 10 seconds for the user to enter /cvclearplot again to confirm
    public CommandResponse clearPlotConfirm(Player sender, ProtectedRegion region) {
        int taskIDCheck;
        taskIDCheck = scheduler.runTaskLater(this.plugin, () -> {
            clearPlotCheck(sender);
        }, 200).getTaskId();
        taskIDCheckList.put(sender.getUniqueId(), taskIDCheck);
        return new CommandResponse(prefix + ChatColor.GOLD + "Are you sure you want to completely clear your plot " + ChatColor.LIGHT_PURPLE + region.getId().toLowerCase() + ChatColor.GOLD + "? Type /cvclearplot again to confirm. " + ChatColor.RED + "You can't /cvundo this!");
    }

    //check if the user confirmed plotclear
    public void clearPlotCheck(Player sender) {
        if(!taskIDClearList.containsKey(sender.getUniqueId()) || taskIDClearList.get(sender.getUniqueId()) != 0) {
            taskIDCheckList.put(sender.getUniqueId(), 0);
            sender.sendMessage(prefix + ChatColor.RED + "Plotclear not confirmed! Please try again if you wish to clear your plot.");
        }
    }

    //perform the plotclear
    public CommandResponse clearPlot(Player sender, ProtectedRegion playerRegion) {
        BukkitPlayer bPlayer = BukkitAdapter.adapt(sender);
        BlockVector3 max = playerRegion.getMaximumPoint();
        BlockVector3 min = BlockVector3.at(playerRegion.getMinimumPoint().getBlockX(), max.getBlockY(), playerRegion.getMinimumPoint().getBlockZ());
        CuboidRegion region = new CuboidRegion(max, min);
        int lowest = playerRegion.getMinimumPoint().getBlockY() - 1;
        long startTime = System.currentTimeMillis();
        int taskIDClear;
        taskIDClear = scheduler.runTaskTimer(this.plugin, new Runnable() {
            int i;
            public void run() {
                i++;
                try (EditSession editSession = WorldEdit.getInstance().newEditSession(bPlayer.getWorld())) {
                    editSession.setBlocks(region, BlockTypes.AIR.getDefaultState());
                } catch (MaxChangedBlocksException e) {
                    logger.log(Level.WARNING, "Unable to replace blocks in selection!");
                }
                region.setPos1(max.subtract(0, i, 0));
                region.setPos2(min.subtract(0, i, 0));
                if (max.subtract(0, i, 0).getBlockY() == lowest) {
                    scheduler.cancelTask(taskIDClearList.get(sender.getUniqueId()));
                    taskIDClearList.put(sender.getUniqueId(), 0);
                    long finishTime = System.currentTimeMillis() - startTime;
                    double time = ((double) finishTime) / 1000.0D;
                    sender.sendMessage(prefix + ChatColor.LIGHT_PURPLE + "Plotclear completed in " + time + " seconds!");
                }
            }
        }, 0, 20).getTaskId();
        taskIDClearList.put(sender.getUniqueId(), taskIDClear);
        double estimated = ((double) (max.getBlockY() - lowest)) / 1.0D;
        return new CommandResponse(prefix + ChatColor.LIGHT_PURPLE + "Plotclear started! Please wait until it is finished. Estimated time till completion: " + estimated + " seconds");
    }

    //perform admin plotclear
    public CommandResponse adminClearPlot(Player adminSender, ProtectedRegion targetRegion) {
        BukkitPlayer bAdmin = BukkitAdapter.adapt(adminSender);
        BlockVector3 max = targetRegion.getMaximumPoint();
        BlockVector3 min = BlockVector3.at(targetRegion.getMinimumPoint().getBlockX(), max.getBlockY(), targetRegion.getMinimumPoint().getBlockZ());
        CuboidRegion region = new CuboidRegion(max, min);
        int lowest = targetRegion.getMinimumPoint().getBlockY() - 1;
        long startTime = System.currentTimeMillis();
        int taskIDAdminClear;
        taskIDAdminClear = scheduler.runTaskTimer(this.plugin, new Runnable() {
            int i;
            public void run() {
                i++;
                try (EditSession editSession = WorldEdit.getInstance().newEditSession(bAdmin.getWorld())) {
                    editSession.setBlocks(region, BlockTypes.AIR.getDefaultState());
                } catch (MaxChangedBlocksException e) {
                    logger.log(Level.WARNING, "Unable to replace blocks in selection!");
                }
                region.setPos1(max.subtract(0, i, 0));
                region.setPos2(min.subtract(0, i, 0));
                if (max.subtract(0, i, 0).getBlockY() == lowest) {
                    scheduler.cancelTask(taskIDAdminClearList.get(targetRegion.getId()));
                    long finishTime = System.currentTimeMillis() - startTime;
                    double time = ((double) finishTime) / 1000.0D;
                    adminSender.sendMessage(prefix + ChatColor.LIGHT_PURPLE + "Plotclear completed in " + time + " seconds!");
                }
            }
        }, 0, 20).getTaskId();
        taskIDAdminClearList.put(targetRegion.getId(), taskIDAdminClear);
        double estimated = ((double) (max.getBlockY() - lowest)) / 1.0D;
        return new CommandResponse(prefix + ChatColor.LIGHT_PURPLE + "Plotclear started for plot: " + ChatColor.GOLD + targetRegion.getId().toLowerCase() + ChatColor.LIGHT_PURPLE + " Please wait until it is finished. Estimated time till completion: " + estimated + " seconds");
    }
}
