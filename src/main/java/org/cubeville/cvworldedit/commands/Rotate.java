package org.cubeville.cvworldedit.commands;

import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.cubeville.commons.commands.Command;
import org.cubeville.commons.commands.CommandParameterInteger;
import org.cubeville.commons.commands.CommandResponse;
import org.cubeville.cvworldedit.CVWorldEdit;

import java.util.*;

public class Rotate extends Command {

    final private String prefix;

    public Rotate(CVWorldEdit plugin) {
        super("");
        addBaseParameter(new CommandParameterInteger()); // Y-axis rotation degrees
        addOptionalBaseParameter(new CommandParameterInteger()); // X-axis rotation degrees
        addOptionalBaseParameter(new CommandParameterInteger()); // Z-axis rotation degrees

        prefix = plugin.getPrefix();
    }

    @Override
    public CommandResponse execute(Player sender, Set<String> set, Map<String, Object> map, List<Object> baseParameters) {

        if (baseParameters.size() > 3) {
            return new CommandResponse(prefix + ChatColor.RED + "Invalid Command!" + ChatColor.LIGHT_PURPLE + " Proper Usage: Use /werotate <y> [x] [z] after copying an area with /wecopy");
        }

        int rotateY = (Integer) baseParameters.get(0);
        int rotateX = baseParameters.size() > 1 ? (Integer) baseParameters.get(1) : 0;
        int rotateZ = baseParameters.size() > 2 ? (Integer) baseParameters.get(2) : 0;

        //Check if the player currently has a clipboard stored
        BukkitPlayer bPlayer = BukkitAdapter.adapt(sender);
        LocalSession localSession = WorldEdit.getInstance().getSessionManager().get(bPlayer);
        ClipboardHolder holder;
        try {
            holder = localSession.getClipboard();
        } catch(EmptyClipboardException e) {
            Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.YELLOW + "Players clipboard returned null! (should they have one?)");
            return new CommandResponse(prefix + ChatColor.RED + "No clipboard to rotate!" + ChatColor.LIGHT_PURPLE + " Proper Usage: Use /werotate after copying an area with /wecopy or cutting an area with /wecut");
        }

        //apply the transform rotation to the holder
        AffineTransform transform = new AffineTransform();
        transform = transform.rotateY(-rotateY);
        transform = transform.rotateX(-rotateX);
        transform = transform.rotateZ(-rotateZ);
        holder.setTransform(holder.getTransform().combine(transform));

        return new CommandResponse(prefix + ChatColor.LIGHT_PURPLE + "Clipboard rotated y:" + rotateY + " x:" + rotateX + " x:" + rotateZ);
    }
}
