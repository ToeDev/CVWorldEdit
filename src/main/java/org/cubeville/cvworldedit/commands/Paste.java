package org.cubeville.cvworldedit.commands;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.cubeville.commons.commands.*;
import org.cubeville.cvworldedit.CVWorldEdit;
import org.cubeville.cvworldedit.CheckRegion;
import org.cubeville.cvworldedit.CommandCooldown;
import org.cubeville.cvworldedit.PlayerClipboard;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Paste extends Command {

    final private CheckRegion pluginCheckRegion;
    final private CommandCooldown pluginCommandCooldown;
    final private Rotate pluginRotate;
    final private PlayerClipboard pluginPlayerClipboard;

    final private String prefix;

    public Paste(CVWorldEdit plugin, PlayerClipboard pluginPlayerClipboard, Rotate pluginRotate, CheckRegion pluginCheckRegion, CommandCooldown pluginCommandCooldown) {
        super("");

        prefix = plugin.getPrefix();

        this.pluginCheckRegion = pluginCheckRegion;
        this.pluginCommandCooldown = pluginCommandCooldown;
        this.pluginRotate = pluginRotate;
        this.pluginPlayerClipboard = pluginPlayerClipboard;
    }

    @Override
    public CommandResponse execute(Player sender, Set<String> set, Map<String, Object> map, List<Object> baseParameters) {

        if (baseParameters.size() > 0) {
            return new CommandResponse(prefix + ChatColor.RED + "Invalid Command!" + ChatColor.LIGHT_PURPLE + " Proper Usage: Use /wepaste after copying an area with /wecopy");
        }

        //Check if the player has copied a selection
        if(pluginPlayerClipboard.getClipboard(sender.getUniqueId()) == null) {
            return new CommandResponse(prefix + ChatColor.RED + "Nothing to paste!" + ChatColor.LIGHT_PURPLE + " Proper Usage: Use /wepaste after copying an area with /wecopy or cutting an area with /wecut");
        }

        //Get the players current position and check if the paste location is within their region
        BukkitPlayer bPlayer = BukkitAdapter.adapt(sender);
        LocalSession localSession = WorldEdit.getInstance().getSessionManager().get(bPlayer);
        int x = bPlayer.getLocation().getBlockX();
        int y = bPlayer.getLocation().getBlockY();
        int z = bPlayer.getLocation().getBlockZ();
        BlockArrayClipboard clipboard = pluginPlayerClipboard.getClipboard(sender.getUniqueId());
        ClipboardHolder holder = new ClipboardHolder(pluginPlayerClipboard.getClipboard(sender.getUniqueId()));
        int rotation = pluginRotate.getRotation(sender);
        AffineTransform transform = new AffineTransform().rotateY(-rotation);
        holder.setTransform(holder.getTransform().combine(transform));
        BlockVector3 clipboardOffset = clipboard.getRegion().getMinimumPoint().subtract(clipboard.getOrigin());
        Vector3 realTo = BlockVector3.at(x, y, z).toVector3().add(holder.getTransform().apply(clipboardOffset.toVector3()));
        Vector3 max = realTo.add(holder.getTransform().apply(clipboard.getRegion().getMaximumPoint().subtract(clipboard.getRegion().getMinimumPoint()).toVector3()));
        CuboidRegion futureRegion = new CuboidRegion(realTo.toBlockPoint(), max.toBlockPoint());
        if(!pluginCheckRegion.isOwner(bPlayer, futureRegion)) {
            return new CommandResponse(prefix + ChatColor.RED + "You cannot paste the clipboard outside your plot! Please stand so that your paste will be fully within your plot!");
        }

        //Check if the player is on command cooldown check the CVWorldEditCommandCooldown class
        DecimalFormat format = new DecimalFormat("0.000");
        double cooldown = pluginCommandCooldown.getCommandCooldown(bPlayer.getUniqueId());
        if(cooldown != 0.0D) {
            return new CommandResponse(prefix + ChatColor.RED + "You cannot use that command yet! Please wait " + ChatColor.GOLD + format.format(cooldown) + " seconds" + ChatColor.RED + " before entering it again!");
        }

        //Start the command cooldown
        pluginCommandCooldown.startCommandCooldown(bPlayer.getUniqueId());

        //perform the paste
        try (EditSession editSession = localSession.createEditSession(bPlayer)) {
            Operation operation = holder.createPaste(editSession).to(BlockVector3.at(x, y, z)).build();
            Operations.complete(operation);
            localSession.remember(editSession);
        }
        catch (WorldEditException e) {
            Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.RED + "Unable to paste players clipboard!");
            Bukkit.getConsoleSender().sendMessage(prefix + e);
            return new CommandResponse(prefix + ChatColor.RED + "Unable to paste clipboard! Contact administrator!");
        }

        return new CommandResponse(prefix + ChatColor.LIGHT_PURPLE + "Pasted " + pluginPlayerClipboard.getBlocksCopied(sender.getUniqueId()) + " blocks.");
    }
}
