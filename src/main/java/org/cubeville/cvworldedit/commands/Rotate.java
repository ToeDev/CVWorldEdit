package org.cubeville.cvworldedit.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.cubeville.commons.commands.Command;
import org.cubeville.commons.commands.CommandParameterString;
import org.cubeville.commons.commands.CommandResponse;
import org.cubeville.cvworldedit.CVWorldEdit;

import java.util.*;

public class Rotate extends Command {

    final private HashMap<UUID, Integer> rotationYList;

    final private String prefix;

    public Rotate(CVWorldEdit plugin) {
        super("");
        addBaseParameter(new CommandParameterString()); // Y-axis rotation degrees

        prefix = plugin.getPrefix();

        this.rotationYList = plugin.getRotationYList();
    }

    @Override
    public CommandResponse execute(Player sender, Set<String> set, Map<String, Object> map, List<Object> baseParameters) {

        if (baseParameters.size() != 1) {
            return new CommandResponse(prefix + ChatColor.RED + "Invalid Command!" + ChatColor.LIGHT_PURPLE + " Proper Usage: Use /werotate <degrees> after copying an area with /wecopy");
        }

        //Check if the provided rotation parameter is an integer
        int rotateY;
        try {
            rotateY = Integer.parseInt(baseParameters.get(0).toString());
        } catch(NumberFormatException e) {
            return new CommandResponse(prefix + ChatColor.RED + "Invalid rotation parameter!" + ChatColor.LIGHT_PURPLE + " Proper Usage: Use /werotate <degrees(0-360)> after copying an area with /wecopy");
        }

        //store the rotation integer for usage in paste class
        setRotation(sender, rotateY);

        return new CommandResponse(prefix + ChatColor.LIGHT_PURPLE + "Clipboard rotated " + rotateY + " degrees.");
    }

    public Integer getRotation(Player sender) {
        if(rotationYList.get(sender.getUniqueId()) == null) {
            return 0;
        } else {
            return rotationYList.get(sender.getUniqueId());
        }
    }

    public void setRotation(Player sender, Integer rotation) {
        UUID uuid = sender.getUniqueId();
        if(rotationYList.get(uuid) == null) {
            rotationYList.put(uuid, rotation);
        } else {
            rotationYList.put(uuid, rotationYList.get(uuid) + rotation);
        }
    }

    public void clearRotation(Player sender) {
        rotationYList.remove(sender.getUniqueId());
    }
}
