package org.cubeville.cvworldedit.commands;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.cubeville.commons.commands.Command;
import org.cubeville.commons.commands.CommandResponse;
import org.cubeville.cvworldedit.CVWorldEdit;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClearClipboard extends Command {

    final private String prefix;

    public ClearClipboard(CVWorldEdit plugin) {
        super("");

        prefix = plugin.getPrefix();
    }

    @Override
    public CommandResponse execute(Player sender, Set<String> set, Map<String, Object> map, List<Object> baseParameters) {

        if (baseParameters.size() > 0) {
            return new CommandResponse(prefix + ChatColor.RED + "Invalid Command!" + ChatColor.LIGHT_PURPLE + " Proper Usage: /weclearclipboard");
        }

        //Clear a players clipboard (can't paste after this)
        LocalSession localSession = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(sender));
        localSession.setClipboard(null);

        return new CommandResponse(prefix + ChatColor.LIGHT_PURPLE + "Clipboard cleared!");
    }













}
