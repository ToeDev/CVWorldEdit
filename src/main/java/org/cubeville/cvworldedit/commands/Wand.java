package org.cubeville.cvworldedit.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.cubeville.commons.commands.*;
import org.cubeville.cvworldedit.CVWorldEdit;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class Wand extends Command {

    final private String prefix;

    public Wand(CVWorldEdit plugin) {
        super("");

        prefix = plugin.getPrefix();
    }

    @Override
    public CommandResponse execute(Player sender, Set<String> set, Map<String, Object> map, List<Object> baseParameters) {

        if (baseParameters.size() > 0) {
            return new CommandResponse(prefix + ChatColor.RED + "Invalid Command!" + ChatColor.LIGHT_PURPLE + " Proper Usage: /cvwand");
        }

        //give the player a netherite hoe for region selection
        PlayerInventory playerInventory = sender.getInventory();
        playerInventory.addItem(new ItemStack(Material.NETHERITE_HOE));
        return new CommandResponse(prefix + ChatColor.LIGHT_PURPLE + "Selection wand given!");
    }
}
