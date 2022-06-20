package org.cubeville.cvworldedit.commands;

import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.Direction;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.cubeville.commons.commands.Command;
import org.cubeville.commons.commands.CommandParameterString;
import org.cubeville.commons.commands.CommandResponse;
import org.cubeville.cvworldedit.CVWorldEdit;

import java.util.*;

public class Flip extends Command {

    final private CVWorldEdit plugin;
    final private String prefix;

    public Flip(CVWorldEdit plugin) {
        super("");
        addOptionalBaseParameter(new CommandParameterString()); // direction to flip

        this.plugin = plugin;
        prefix = this.plugin.getPrefix();
    }

    @Override
    public CommandResponse execute(Player sender, Set<String> set, Map<String, Object> map, List<Object> baseParameters) {

        if (baseParameters.size() > 1) {
            return new CommandResponse(prefix + ChatColor.RED + "Invalid Command!" + ChatColor.LIGHT_PURPLE + " Proper Usage: Use /weflip <direction> after copying an area with /wecopy or cutting an area with /wecut");
        }

        //Check if the direction is valid
        BlockVector3 dir;
        String direction;
        if(baseParameters.size() == 0 || ((String) baseParameters.get(0)).equalsIgnoreCase("me")) {
            direction = plugin.getPlayerFacing(sender);
        } else {
            direction = (String) baseParameters.get(0);
        }
        if(direction.equalsIgnoreCase("north") || direction.equalsIgnoreCase("n")) {
            dir = Direction.NORTH.toBlockVector();
        } else if(direction.equalsIgnoreCase("south") || direction.equalsIgnoreCase("s")) {
            dir = Direction.SOUTH.toBlockVector();
        } else if(direction.equalsIgnoreCase("west") || direction.equalsIgnoreCase("w")) {
            dir = Direction.WEST.toBlockVector();
        } else if(direction.equalsIgnoreCase("east") || direction.equalsIgnoreCase("e")) {
            dir = Direction.EAST.toBlockVector();
        } else if(direction.equalsIgnoreCase("up") || direction.equalsIgnoreCase("u")) {
            dir = Direction.UP.toBlockVector();
        } else if(direction.equalsIgnoreCase("down") || direction.equalsIgnoreCase("d")) {
            dir = Direction.DOWN.toBlockVector();
        } else {
            return new CommandResponse(prefix + ChatColor.RED + "Invalid Command!" + ChatColor.LIGHT_PURPLE + " Proper Directions: north, south, west, east, up, or down");
        }

        //Check if the player currently has a clipboard stored
        BukkitPlayer bPlayer = BukkitAdapter.adapt(sender);
        LocalSession localSession = WorldEdit.getInstance().getSessionManager().get(bPlayer);
        ClipboardHolder holder;
        try {
            holder = localSession.getClipboard();
        } catch(EmptyClipboardException e) {
            Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.YELLOW + "Players clipboard returned null! (should they have one?)");
            return new CommandResponse(prefix + ChatColor.RED + "No clipboard to flip!" + ChatColor.LIGHT_PURPLE + " Proper Usage: Use /weflip after copying an area with /wecopy or cutting an area with /wecut");
        }

        //apply the transform flip to the holder
        AffineTransform transform = new AffineTransform();
        transform = transform.scale(dir.abs().multiply(-2).add(1, 1, 1).toVector3());
        holder.setTransform(holder.getTransform().combine(transform));

        return new CommandResponse(prefix + ChatColor.LIGHT_PURPLE + "Clipboard flipped " + direction);
    }
}
