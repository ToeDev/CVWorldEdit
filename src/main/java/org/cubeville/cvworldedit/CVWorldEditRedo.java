package org.cubeville.cvworldedit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.cubeville.commons.commands.Command;
import org.cubeville.commons.commands.CommandResponse;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CVWorldEditRedo extends Command {

    final private CVWorldEditCommandCooldown pluginCommandCooldown;

    final private String prefix;

    public CVWorldEditRedo(CVWorldEditCommandCooldown pluginCommandCooldown) {
        super("");

        this.pluginCommandCooldown =  pluginCommandCooldown;

        prefix = ChatColor.GRAY + "[" + ChatColor.DARK_RED + "CVWorldEdit" + ChatColor.GRAY + "]" + " ";
    }

    @Override
    public CommandResponse execute(Player sender, Set<String> set, Map<String, Object> map, List<Object> baseParameters) {

        if (baseParameters.size() > 0) {
            return new CommandResponse(prefix + ChatColor.RED + "Invalid Command!" + ChatColor.LIGHT_PURPLE + " Proper Usage: /cvredo");
        }

        //Check if the player is on command cooldown check the CVWorldEditCommandCooldown class
        DecimalFormat format = new DecimalFormat("0.000");
        BukkitPlayer bPlayer = BukkitAdapter.adapt(sender);
        double cooldown = pluginCommandCooldown.getCommandCooldown(bPlayer.getUniqueId());
        if(cooldown != 0.0D) {
            return new CommandResponse(prefix + ChatColor.RED + "You cannot use that command yet! Please wait " + ChatColor.GOLD + format.format(cooldown) + " seconds" + ChatColor.RED + " before entering it again!");
        }

        //Attempt redoing an undone action from the players localsession history
        LocalSession localSession = WorldEdit.getInstance().getSessionManager().get(bPlayer);
        EditSession redone = localSession.redo(localSession.getBlockBag(bPlayer), bPlayer);

        //Check if the redo was successful
        if (redone != null) {
            //Start the command cooldown
            pluginCommandCooldown.startCommandCooldown(bPlayer.getUniqueId());
            return new CommandResponse(prefix + ChatColor.LIGHT_PURPLE + "Action redone successfully!");

        }
        return new CommandResponse(prefix + ChatColor.RED + "No actions to redo!");
    }
}
