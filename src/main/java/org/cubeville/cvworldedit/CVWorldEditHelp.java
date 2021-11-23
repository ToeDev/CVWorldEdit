package org.cubeville.cvworldedit;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.cubeville.commons.commands.Command;
import org.cubeville.commons.commands.CommandResponse;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class CVWorldEditHelp extends Command {

    final private String cvwand;
    final private String cvpos;
    final private String cvexpand;
    final private String cvcontract;
    final private String cvsel;
    final private String cvsize;
    final private String cvcopy;
    final private String cvpaste;
    final private String cvrotate;
    final private String cvset;
    final private String cvwalls;
    final private String cvfaces;
    final private String cvreplace;
    final private String cvundo;
    final private String cvredo;
    final private String cvclearhistory;
    final private String cvclearclipboard;
    final private String cvclearplot;

    public CVWorldEditHelp() {
        super("");

        cvwand = ChatColor.GOLD + "/cvwand" + ChatColor.LIGHT_PURPLE + " - Give yourself a selection wand";
        cvpos = ChatColor.GOLD + "/cvpos1 + /cvpos2" + ChatColor.LIGHT_PURPLE + " - Set points of a selection at your location";
        cvexpand = ChatColor.GOLD + "/cvexpand <number> <direction>" + ChatColor.LIGHT_PURPLE + " - Expand your selection in a direction";
        cvcontract = ChatColor.GOLD + "/cvcontract <number> <direction>" + ChatColor.LIGHT_PURPLE + " - Contract your selection in a direction";
        cvsel = ChatColor.GOLD + "/cvsel" + ChatColor.LIGHT_PURPLE + " - Clear your current selection";
        cvsize = ChatColor.GOLD + "/cvsize" + ChatColor.LIGHT_PURPLE + " - List your current selection's size and dimensions";
        cvcopy = ChatColor.GOLD + "/cvcopy" + ChatColor.LIGHT_PURPLE + " - Copy your current selection to the clipboard";
        cvpaste = ChatColor.GOLD + "/cvpaste" + ChatColor.LIGHT_PURPLE + " - Pastes your current clipboard where you're standing";
        cvrotate = ChatColor.GOLD + "/cvrotate <degrees>" + ChatColor.LIGHT_PURPLE + " - Rotates your clipboard on the Y axis";
        cvset = ChatColor.GOLD + "/cvset <targetBlock>" + ChatColor.LIGHT_PURPLE + " - Sets your current selection to the target block";
        cvreplace = ChatColor.GOLD + "/cvreplace <sourceBlock> <targetBlock>" + ChatColor.LIGHT_PURPLE + " - Replaces the source blocks in your selection with the target block";
        cvwalls = ChatColor.GOLD + "/cvwalls <targetBlock>" + ChatColor.LIGHT_PURPLE + " - Sets the sides/walls of your current selection to the target block";
        cvfaces = ChatColor.GOLD + "/cvfaces <targetBlock>" + ChatColor.LIGHT_PURPLE + " - Sets the walls, floor, and ceiling of your current selection to the target block";
        cvundo = ChatColor.GOLD + "/cvundo" + ChatColor.LIGHT_PURPLE + " - Undoes your last block-changing command";
        cvredo = ChatColor.GOLD + "/cvredo" + ChatColor.LIGHT_PURPLE + " - Redoes your last undone block-changing command";
        cvclearhistory = ChatColor.GOLD + "/cvclearhistory" + ChatColor.LIGHT_PURPLE + " - Clears your block-changing command from history";
        cvclearclipboard = ChatColor.GOLD + "/cvclearclipboard" + ChatColor.LIGHT_PURPLE + " - Clears your current clipboard";
        cvclearplot = ChatColor.GOLD + "/cvclearplot" + ChatColor.LIGHT_PURPLE + " - Clears your entire plot";
    }

    @Override
    public CommandResponse execute(Player sender, Set<String> set, Map<String, Object> map, List<Object> baseParameters) {

        if (baseParameters.size() > 0) {
            return new CommandResponse(ChatColor.RED + "Invalid Command!" + ChatColor.LIGHT_PURPLE + " Proper Usage: /cvhelp");
        }

        //Send Command info to player
        return new CommandResponse(cvwand, cvpos, cvexpand, cvcontract, cvsel, cvsize, cvcopy, cvpaste, cvrotate, cvset, cvreplace, cvwalls, cvfaces, cvundo, cvredo, cvclearhistory, cvclearclipboard, cvclearplot);
    }
}
