package org.cubeville.cvworldedit;

import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;

public class CheckRegion {

    public boolean isOwner(BukkitPlayer bPlayer, Region playerSelection) {
        World world = bPlayer.getWorld();
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(bPlayer.getPlayer());
        RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery regionQuery = regionContainer.createQuery();
        for(BlockVector3 block : playerSelection) {
            Location loc = new Location(world, block.getBlockX(), block.getBlockY(), block.getBlockZ());
            ApplicableRegionSet applicableRegionSet = regionQuery.getApplicableRegions(loc);
            if(applicableRegionSet.size() < 1) {
                return false;
            }
            for(ProtectedRegion region : applicableRegionSet) {
                if(!region.isOwner(localPlayer)) {
                    return false;
                }
            }
        }
        return true;
    }
}
