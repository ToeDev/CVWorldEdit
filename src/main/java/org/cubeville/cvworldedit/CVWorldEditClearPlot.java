package org.cubeville.cvworldedit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.cubeville.commons.commands.*;
import org.cubeville.commons.utils.BlockUtils;

import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CVWorldEditClearPlot extends Command {

    final private Logger logger;
    final private Plugin plugin;
    final private BukkitScheduler scheduler;

    final private String prefix;

    final private HashMap<UUID, String> clearPlotRunningList;
    final private int clearPlotRunningLimit;
    final private List<String> clearPlotEntityList;
    final private int clearPlotDelay;
    final private int plotVolume;
    final private int plotYLevel;
    final private HashMap<UUID, Integer> taskIDCheckList;
    final private HashMap<UUID, Integer> taskIDClearList;

    public CVWorldEditClearPlot(CVWorldEdit plugin) {
        super("");
        addOptionalBaseParameter(new CommandParameterString()); //target plot

        prefix = plugin.getPrefix();

        this.clearPlotRunningList = plugin.getClearPlotRunningList();
        this.clearPlotRunningLimit = plugin.getClearPlotRunningLimit();
        this.clearPlotEntityList = plugin.getClearPlotEntityList();
        this.clearPlotDelay = 10;
        this.plotVolume = plugin.getClearPlotVolume();
        this.plotYLevel = plugin.getPlotYLevel();
        this.taskIDCheckList = plugin.getTaskIDCheckList();
        this.taskIDClearList = plugin.getTaskIDClearList();

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
            if(targetRegion.volume() != plotVolume) {
                return new CommandResponse(prefix + ChatColor.RED + "You cannot run a plotclear on this region! If you believe this to be in error, please contact an administrator. If you are an administrator, check the Plot-Clear-Volume config option");
            }
            return adminClearPlot(sender, targetRegion);
        } else {
            //check if there are too many plotclears running at once defined by "clearPlotRunningLimit"
            if(clearPlotRunningList.size() >= clearPlotRunningLimit) {
                return new CommandResponse(prefix + ChatColor.RED + "The max amount of plotclears are currently running on the server! Please wait a few minutes and try again!");
            }

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
                clearPlotRunningList.put(sender.getUniqueId(), playerRegion.getId());
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
        taskIDCheck = scheduler.runTaskLater(this.plugin, () -> clearPlotCheck(sender), 200).getTaskId();
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
        World world = bPlayer.getWorld();
        BlockVector3 maxPoint = playerRegion.getMaximumPoint();
        BlockVector3 minPoint = playerRegion.getMinimumPoint();
        int xMax = maxPoint.getBlockX();
        int zMax = maxPoint.getBlockZ();
        int xMin = minPoint.getBlockX();
        int zMin = minPoint.getBlockZ();
        int lowest = playerRegion.getMinimumPoint().getBlockY();
        long startTime = System.currentTimeMillis();
        int taskIDClear = scheduler.runTaskAsynchronously(this.plugin, () -> {
            int i = 1;
            for(int y = maxPoint.getBlockY(); y >= lowest; y--) {
                BlockVector3 max = BlockVector3.at(xMax, y, zMax);
                BlockVector3 min = BlockVector3.at(xMin, y, zMin);
                scheduler.runTaskLaterAsynchronously(plugin, () -> {
                    try (EditSession editSession = WorldEdit.getInstance().newEditSession(world)) {
                        editSession.setBlocks(new CuboidRegion(max, min), Objects.requireNonNull(BlockTypes.AIR).getDefaultState());
                    } catch (MaxChangedBlocksException e) {
                        logger.log(Level.WARNING, "Unable to replace blocks in selection!");
                    }
                }, (long) clearPlotDelay * i);
                i++;
            }
            for(int y = lowest; y <= plotYLevel; y++) {
                BlockVector3 max = BlockVector3.at(xMax, y, zMax);
                BlockVector3 min = BlockVector3.at(xMin, y, zMin);
                final int newY = y;
                scheduler.runTaskLaterAsynchronously(plugin, () -> {
                    try (EditSession editSession = WorldEdit.getInstance().newEditSession(world)) {
                        BlockState block = (newY != plotYLevel ? Objects.requireNonNull(BlockTypes.DIRT).getDefaultState() : Objects.requireNonNull(BlockTypes.GRASS_BLOCK).getDefaultState());
                        editSession.setBlocks(new CuboidRegion(max, min), block);
                    } catch (MaxChangedBlocksException e) {
                        logger.log(Level.WARNING, "Unable to replace blocks in selection!");
                    }
                }, (long) clearPlotDelay * i);
                i++;
            }
            scheduler.runTaskLaterAsynchronously(this.plugin, () -> {
                taskIDClearList.put(sender.getUniqueId(), 0);
                long finishTime = System.currentTimeMillis() - startTime;
                double time = ((double) finishTime) / 1000.0D;
                int timeMinutes = (int) time / 60;
                double timeSeconds = time % 60;
                DecimalFormat format = new DecimalFormat("0.000");
                clearPlotRunningList.remove(sender.getUniqueId());
                sender.sendMessage(prefix + ChatColor.LIGHT_PURPLE + "Plotclear completed in " + ChatColor.GOLD + timeMinutes + "m " + format.format(timeSeconds) + "s");
            }, (long) clearPlotDelay * (i - 1));
        }).getTaskId();
        taskIDClearList.put(sender.getUniqueId(), taskIDClear);
        for(String entity : clearPlotEntityList) {
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "cvtools killentities type:" + entity + " wg:" + playerRegion.getId() + " world:" + world.getId());
        }
        double estimated = ((double) clearPlotDelay / 20.0D) * ((maxPoint.getBlockY() - lowest) + (plotYLevel - lowest));
        int estMinutes = (int) estimated / 60;
        double estSeconds = estimated % 60;
        return new CommandResponse(prefix + ChatColor.LIGHT_PURPLE + "Plotclear started! Please wait until it is finished. Estimated time till completion: " + ChatColor.GOLD + estMinutes + "m " + estSeconds + "s");
    }

    //perform admin plotclear
    public CommandResponse adminClearPlot(Player adminSender, ProtectedRegion targetRegion) {
        BukkitPlayer bAdmin = BukkitAdapter.adapt(adminSender);
        World world = bAdmin.getWorld();
        BlockVector3 maxPoint = targetRegion.getMaximumPoint();
        BlockVector3 minPoint = targetRegion.getMinimumPoint();
        int xMax = maxPoint.getBlockX();
        int zMax = maxPoint.getBlockZ();
        int xMin = minPoint.getBlockX();
        int zMin = minPoint.getBlockZ();
        int lowest = targetRegion.getMinimumPoint().getBlockY();
        long startTime = System.currentTimeMillis();
        scheduler.runTaskAsynchronously(this.plugin, () -> {
            int i = 1;
            for(int y = maxPoint.getBlockY(); y >= lowest; y--) {
                BlockVector3 max = BlockVector3.at(xMax, y, zMax);
                BlockVector3 min = BlockVector3.at(xMin, y, zMin);
                scheduler.runTaskLaterAsynchronously(plugin, () -> {
                    try (EditSession editSession = WorldEdit.getInstance().newEditSession(world)) {
                        editSession.setBlocks(new CuboidRegion(max, min), Objects.requireNonNull(BlockTypes.AIR).getDefaultState());
                    } catch (MaxChangedBlocksException e) {
                        logger.log(Level.WARNING, "Unable to replace blocks in selection!");
                    }
                }, (long) clearPlotDelay * i);
                i++;
            }
            for(int y = lowest; y <= plotYLevel; y++) {
                BlockVector3 max = BlockVector3.at(xMax, y, zMax);
                BlockVector3 min = BlockVector3.at(xMin, y, zMin);
                final int newY = y;
                scheduler.runTaskLaterAsynchronously(plugin, () -> {
                    try (EditSession editSession = WorldEdit.getInstance().newEditSession(world)) {
                        BlockState block = (newY != plotYLevel ? Objects.requireNonNull(BlockTypes.DIRT).getDefaultState() : Objects.requireNonNull(BlockTypes.GRASS_BLOCK).getDefaultState());
                        editSession.setBlocks(new CuboidRegion(max, min), block);
                    } catch (MaxChangedBlocksException e) {
                        logger.log(Level.WARNING, "Unable to replace blocks in selection!");
                    }
                }, (long) clearPlotDelay * i);
                i++;
            }
            scheduler.runTaskLaterAsynchronously(this.plugin, () -> {

                long finishTime = System.currentTimeMillis() - startTime;
                double time = ((double) finishTime) / 1000.0D;
                int timeMinutes = (int) time / 60;
                double timeSeconds = time % 60;
                DecimalFormat format = new DecimalFormat("0.000");
                adminSender.sendMessage(prefix + ChatColor.LIGHT_PURPLE + "Plotclear completed in " + ChatColor.GOLD + timeMinutes + "m " + format.format(timeSeconds) + "s");
            }, (long) clearPlotDelay * (i - 1));
        });
        for(String entity : clearPlotEntityList) {
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "cvtools killentities type:" + entity + " wg:" + targetRegion.getId() + " world:" + world.getId());
        }
        double estimated = ((double) clearPlotDelay / 20.0D) * ((maxPoint.getBlockY() - lowest) + (plotYLevel - lowest));
        int estMinutes = (int) estimated / 60;
        double estSeconds = estimated % 60;
        return new CommandResponse(prefix + ChatColor.LIGHT_PURPLE + "Plotclear started for plot: " + ChatColor.GOLD + targetRegion.getId().toLowerCase() + ChatColor.LIGHT_PURPLE + " Please wait until it is finished. Estimated time till completion: " + ChatColor.GOLD + estMinutes + "m " + estSeconds + "s");
    }
}
