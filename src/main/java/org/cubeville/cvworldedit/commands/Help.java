package org.cubeville.cvworldedit.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.cubeville.commons.commands.Command;
import org.cubeville.commons.commands.CommandResponse;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class Help extends Command {

    final private String cvwand;
    final private String cvpos;
    final private String cvhpos;
    final private String cvexpand;
    final private String cvcontract;
    final private String cvshift;
    final private String cvsel;
    final private String cvsize;
    final private String cvcut;
    final private String cvcopy;
    final private String cvpaste;
    final private String cvflip;
    final private String cvrotate;
    final private String cvmove;
    final private String cvset;
    final private String cvwalls;
    final private String cvfaces;
    final private String cvreplace;
    final private String cvundo;
    final private String cvredo;
    final private String cvclearhistory;
    final private String cvclearclipboard;
    final private String cvclearplot;

    public Help() {
        super("");

        cvwand = ChatColor.GOLD + "/wewand" + ChatColor.LIGHT_PURPLE + " - Give yourself a selection wand";
        cvpos = ChatColor.GOLD + "/wepos1 + /wepos2" + ChatColor.LIGHT_PURPLE + " - Set points of a selection at your location";
        cvhpos = ChatColor.GOLD + "/wehpos1 + /wehpos2" + ChatColor.LIGHT_PURPLE + " - Set points of a selection to the block you're looking at";
        cvexpand = ChatColor.GOLD + "/weexpand <number> [direction]" + ChatColor.LIGHT_PURPLE + " - Expand your selection in the specified direction or in the direction you're if not specified";
        cvcontract = ChatColor.GOLD + "/wecontract <number> [direction]" + ChatColor.LIGHT_PURPLE + " - Contract your selection in the specified direction or in the direction you're if not specified";
        cvshift = ChatColor.GOLD + "/weshift <number> [direction]" + ChatColor.LIGHT_PURPLE + " - Move your selected area in the specified direction or in the direction you're if not specified";
        cvsel = ChatColor.GOLD + "/wesel" + ChatColor.LIGHT_PURPLE + " - Clear your current selection";
        cvsize = ChatColor.GOLD + "/wesize" + ChatColor.LIGHT_PURPLE + " - List your current selection's size and dimensions";
        cvcut = ChatColor.GOLD + "/wecut" + ChatColor.LIGHT_PURPLE + " - Copy your current selection to the clipboard then delete it";
        cvcopy = ChatColor.GOLD + "/wecopy" + ChatColor.LIGHT_PURPLE + " - Copy your current selection to the clipboard";
        cvpaste = ChatColor.GOLD + "/wepaste" + ChatColor.LIGHT_PURPLE + " - Pastes your current clipboard where you're standing";
        cvflip = ChatColor.GOLD + "/weflip [direction]" + ChatColor.LIGHT_PURPLE + " - Flips your clipboard in the specified direction or in the direction you're if not specified";
        cvrotate = ChatColor.GOLD + "/werotate <y> [x] [z]" + ChatColor.LIGHT_PURPLE + " - Rotates your clipboard on the y, x, and/or z axis. (y is required)";
        cvmove = ChatColor.GOLD + "/wemove <number> [direction]" + ChatColor.LIGHT_PURPLE + " - Move your selection in the specified direction or in the direction you're if not specified";
        cvset = ChatColor.GOLD + "/weset <targetBlock>" + ChatColor.LIGHT_PURPLE + " - Sets your current selection to the target block";
        cvreplace = ChatColor.GOLD + "/wereplace <sourceBlock> <targetBlock>" + ChatColor.LIGHT_PURPLE + " - Replaces the source blocks in your selection with the target block";
        cvwalls = ChatColor.GOLD + "/wewalls <targetBlock>" + ChatColor.LIGHT_PURPLE + " - Sets the sides/walls of your current selection to the target block";
        cvfaces = ChatColor.GOLD + "/wefaces <targetBlock>" + ChatColor.LIGHT_PURPLE + " - Sets the walls, floor, and ceiling of your current selection to the target block";
        cvundo = ChatColor.GOLD + "/weundo" + ChatColor.LIGHT_PURPLE + " - Undoes your last block-changing command";
        cvredo = ChatColor.GOLD + "/weredo" + ChatColor.LIGHT_PURPLE + " - Redoes your last undone block-changing command";
        cvclearhistory = ChatColor.GOLD + "/weclearhistory" + ChatColor.LIGHT_PURPLE + " - Clears your block-changing command from history";
        cvclearclipboard = ChatColor.GOLD + "/weclearclipboard" + ChatColor.LIGHT_PURPLE + " - Clears your current clipboard";
        cvclearplot = ChatColor.GOLD + "/weclearplot" + ChatColor.LIGHT_PURPLE + " - Clears your entire plot";
    }

    @Override
    public CommandResponse execute(Player sender, Set<String> set, Map<String, Object> map, List<Object> baseParameters) {

        if (baseParameters.size() > 0) {
            return new CommandResponse(ChatColor.RED + "Invalid Command!" + ChatColor.LIGHT_PURPLE + " Proper Usage: /wehelp");
        }

        //Send Command info to player
        return new CommandResponse(cvwand, cvpos, cvhpos, cvexpand, cvcontract, cvshift, cvsel, cvsize,
                cvcut, cvcopy, cvpaste, cvflip, cvrotate, cvmove, cvset, cvreplace, cvwalls, cvfaces,
                cvundo, cvredo, cvclearhistory, cvclearclipboard, cvclearplot);
    }
}
