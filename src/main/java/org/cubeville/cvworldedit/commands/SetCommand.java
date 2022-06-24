package org.cubeville.cvworldedit.commands;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.registry.state.*;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.cubeville.commons.commands.*;
import org.cubeville.cvworldedit.CVWorldEdit;
import org.cubeville.cvworldedit.CheckBlacklist;
import org.cubeville.cvworldedit.CheckRegion;
import org.cubeville.cvworldedit.CommandCooldown;

import java.text.DecimalFormat;
import java.util.*;

public class SetCommand extends Command {

    final private CVWorldEdit plugin;
    final private CheckBlacklist pluginBlacklist;
    final private CheckRegion pluginCheckRegion;
    final private CommandCooldown pluginCommandCooldown;

    final private String prefix;

    public SetCommand(CVWorldEdit plugin, CheckBlacklist pluginBlacklist, CheckRegion pluginCheckRegion, CommandCooldown pluginCommandCooldown) {
        super("");
        addBaseParameter(new CommandParameterString()); //target block

        prefix = plugin.getPrefix();

        this.plugin = plugin;
        this.pluginBlacklist = pluginBlacklist;
        this.pluginCheckRegion = pluginCheckRegion;
        this.pluginCommandCooldown = pluginCommandCooldown;
    }

    @Override
    public CommandResponse execute(Player sender, Set<String> set, Map<String, Object> map, List<Object> baseParameters) {

        if (baseParameters.size() != 1) {
            return new CommandResponse(prefix + ChatColor.RED + "Invalid Command!" + ChatColor.LIGHT_PURPLE + " Proper Usage: /weset <targetblock>");
        }

        //Check if block(s) are valid or on the blacklist
        String[] tempTargetBlocks;
        Map<BlockState, Integer> targetBlocks = new HashMap<>();
        String target = baseParameters.get(0).toString().toLowerCase();
        if(target.contains(",")) {
            tempTargetBlocks = splitNicely(target);
        } else {
            tempTargetBlocks = new String[] {target};
        }
        for(String s : tempTargetBlocks) {
            String i;
            if(s.contains("%")) {
                i = s.substring(0, s.indexOf("%"));
                s = s.replaceAll(".+%", "");
                try {
                    Integer.parseInt(i);
                } catch(NumberFormatException e) {
                    return new CommandResponse(prefix + ChatColor.RED + i + "% is not a valid percentage!");
                }
            } else {
                i = String.valueOf(1);
            }
            String[] states = new String[0];
            //String state = null;
            if(s.contains("[") && s.contains("]")) {
                //state = s.substring(s.indexOf("[") + 1, s.indexOf("]"));
                String tempStates = s.substring(s.indexOf("[") + 1, s.indexOf("]"));
                states = tempStates.split(",");
                s = s.substring(0, s.indexOf("["));
            }
            if (BlockTypes.get(s) == null) {
                return new CommandResponse(prefix + ChatColor.RED + s.toUpperCase() + " is not a valid block!");
            }
            if(pluginBlacklist.checkBlockBanned(s)) {
                return new CommandResponse(prefix + ChatColor.RED + "You cannot WorldEdit the following block! " + ChatColor.GOLD + s);
            }
            BlockType type = Objects.requireNonNull(BlockTypes.get(s));
            if(states.length < 1) {
                targetBlocks.put(type.getDefaultState(), Integer.valueOf(i));
            } else {
                for(String checkState : states) {
                    if(type.getProperty(checkState.substring(0, checkState.indexOf("="))) == null) {
                        return new CommandResponse(prefix + ChatColor.RED + checkState.substring(0, checkState.indexOf("=")) + " is not a valid BlockState for " + s);
                    }
                    if(!type.getProperty(checkState.substring(0, checkState.indexOf("="))).getValues().contains(checkState.substring(checkState.indexOf("=") + 1))) {
                        return new CommandResponse(prefix + ChatColor.RED + checkState.substring(checkState.indexOf("=") + 1) + " is not a valid value for BlockState " + checkState.substring(0, checkState.indexOf("=")));
                    }
                }
                Map<Property<?>, Object> p = new HashMap<>();
                for(Property<?> property : type.getProperties()) {
                    int found = 0;
                    for(String state : states) {
                        if(property.getName().equalsIgnoreCase(state.substring(0, state.indexOf("=")))) {
                            String v = state.substring(state.indexOf("=") + 1);
                            Object object = null;
                            if(property instanceof BooleanProperty) {
                                object = Boolean.valueOf(v);
                            } else if(property instanceof DirectionalProperty) {
                                for(Direction d : Direction.values()) {
                                    if(d.toString().equalsIgnoreCase(v)) {
                                        object = d;
                                        break;
                                    }
                                }
                            } else if(property instanceof IntegerProperty) {
                                object = Integer.valueOf(v);
                            } else {
                                object = v;
                            }
                            p.put(property, object);
                            found++;
                            break;
                        }
                    }
                    if(found == 0) {
                        p.put(property, property.getValues().get(0));
                    }
                }

                targetBlocks.put(type.getState(p), Integer.valueOf(i));

                /*//TODO THIS WORKS FOR SINGLE STATES!
                if(type.getProperty(state.substring(0, state.indexOf("="))) != null) {
                    if(type.getProperty(state.substring(0, state.indexOf("="))).getValues().contains(state.substring(state.indexOf("=") + 1))) {
                        Map<Property<?>, Object> p = new HashMap<>();
                        p.put(type.getProperty(state.substring(0, state.indexOf("="))), state.substring(state.indexOf("=") + 1));
                        targetBlocks.put(type.getState(p), Integer.valueOf(i));
                    } else {
                        return new CommandResponse(prefix + ChatColor.RED + state.substring(state.indexOf("=") + 1) + " is not a valid value for BlockState " + state.substring(0, state.indexOf("=")));
                    }
                } else {
                    return new CommandResponse(prefix + ChatColor.RED + state.substring(0, state.indexOf("=")) + " is not a valid BlockState for " + s);
                }*/
            }
        }

        //Check if player has a selection made
        Region playerSelection;
        BukkitPlayer bPlayer = BukkitAdapter.adapt(sender);
        try {
            playerSelection = WorldEdit.getInstance().getSessionManager().get(bPlayer).getSelection(bPlayer.getWorld());
        } catch (IncompleteRegionException e) {
            Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.YELLOW + "Players selection returned null! (did they make a selection?)");
            return new CommandResponse(prefix + ChatColor.RED + "You haven't made a selection yet!");
        }

        //Check if the player's selection is larger than the max block volume limit
        if(plugin.getBlockVolumeLimit() < playerSelection.getVolume()) {
            return new CommandResponse(prefix + ChatColor.RED + "Your selection is too large! (" + ChatColor.GOLD + playerSelection.getVolume() + ChatColor.RED + ")" +  " The maximum block count per command is " + ChatColor.GOLD + plugin.getBlockVolumeLimit());
        }

        //Check if the player's selection is in a region they are owner of
        if(!pluginCheckRegion.isOwner(bPlayer, playerSelection)) {
            return new CommandResponse(prefix + ChatColor.RED + "You cannot WorldEdit outside your plot! Please alter your selection!");
        }

        //Check if the player is on command cooldown check the CVWorldEditCommandCooldown class
        DecimalFormat format = new DecimalFormat("0.000");
        double cooldown = pluginCommandCooldown.getCommandCooldown(bPlayer.getUniqueId());
        if(cooldown != 0.0D) {
            return new CommandResponse(prefix + ChatColor.RED + "You cannot use that command yet! Please wait " + ChatColor.GOLD + format.format(cooldown) + " seconds" + ChatColor.RED + " before entering it again!");
        }

        //Start the command cooldown
        pluginCommandCooldown.startCommandCooldown(bPlayer.getUniqueId());

        //Get the block pattern
        RandomPattern pattern = new RandomPattern();
        for(BlockState block : targetBlocks.keySet()) {
            pattern.add(block, targetBlocks.get(block));
        }

        //Set the blocks
        LocalSession localSession = WorldEdit.getInstance().getSessionManager().get(bPlayer);
        int blocksChanged;
        try (EditSession editSession = localSession.createEditSession(bPlayer)) {
            blocksChanged = editSession.setBlocks(playerSelection, pattern);
            localSession.remember(editSession);
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.YELLOW + "Unable to replace blocks in selection! (did volume exceed allowed amount?)");
            return new CommandResponse(prefix + ChatColor.RED + "You cannot WE that many of the following block type at once! " + ChatColor.GOLD + Arrays.toString(tempTargetBlocks).substring(1, Arrays.toString(tempTargetBlocks).length() - 1).replaceAll("\\[.+]", ""));
        }
        return new CommandResponse(prefix + ChatColor.LIGHT_PURPLE + "Setting " + blocksChanged + " " + Arrays.toString(tempTargetBlocks).substring(1, Arrays.toString(tempTargetBlocks).length() - 1).replaceAll("\\[.+]", ""));
    }

    private String[] splitNicely(String string) {
        List<String> split = new ArrayList<>();
        int open = string.indexOf("[");
        int close = string.indexOf("]");
        int comma = string.indexOf(",");

        while (comma != -1) {
            if (close < comma && open > comma) {

                split.add(string.substring(0, comma));
                string = string.substring(comma + 1);

                open = string.indexOf("[");
                close = string.indexOf("]");
                comma = string.indexOf(",");
            } else {
                open = string.indexOf("[", comma + 1);
                close = string.indexOf("]", comma + 1);
                comma = string.indexOf(",", comma + 1);
            }
        }
        split.add(string);
        return split.toArray(new String[0]);
    }
}
