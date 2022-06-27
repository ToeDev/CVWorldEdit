package org.cubeville.cvworldedit;

import com.sk89q.worldedit.registry.state.BooleanProperty;
import com.sk89q.worldedit.registry.state.DirectionalProperty;
import com.sk89q.worldedit.registry.state.IntegerProperty;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {

    final private String prefix;

    public Utils(CVWorldEdit plugin) {
        prefix = plugin.getPrefix();
    }
    public String[] splitNicely(String string) {
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

    public BlockState getBlockState(BlockType type, String[] states, Player player) {
        for(String checkState : states) {
            String property = checkState.substring(0, checkState.indexOf("="));
            if(type.getProperty(property) == null) {
                player.sendMessage(prefix + ChatColor.RED + checkState.substring(0, checkState.indexOf("=")) + " is not a valid BlockState for " + type.getId().substring(type.getId().indexOf(":") + 1));
                return null;
            }
            List<String> values = new ArrayList<>();
            for(Object o : type.getProperty(property).getValues()) {
                values.add(o.toString().toLowerCase());
            }
            if(!values.contains(checkState.substring(checkState.indexOf("=") + 1).toLowerCase())) {
                player.sendMessage(prefix + ChatColor.RED + checkState.substring(checkState.indexOf("=") + 1) + " is not a valid value for BlockState " + checkState.substring(0, checkState.indexOf("=")));
                return null;
            }
        }
        Map<Property<?>, Object> newState = new HashMap<>(type.getDefaultState().getStates());
        for(Property<?> property : type.getDefaultState().getStates().keySet()) {
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
                    newState.put(property, object);
                    break;
                }
            }
        }
        for(BlockState state : type.getAllStates()) {
            if(state.getStates().equals(newState)) {
                return state;
            }
        }
        return null;
    }
}
