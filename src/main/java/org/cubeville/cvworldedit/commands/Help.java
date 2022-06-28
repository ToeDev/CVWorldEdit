package org.cubeville.cvworldedit.commands;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.cubeville.commons.commands.Command;
import org.cubeville.commons.commands.CommandParameterInteger;
import org.cubeville.commons.commands.CommandResponse;

import java.util.ArrayList;
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
    final private String cvstack;
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
        addOptionalBaseParameter(new CommandParameterInteger());

        cvwand = ChatColor.GOLD + "/wewand" + ChatColor.LIGHT_PURPLE + " - Give yourself a selection wand";
        cvpos = ChatColor.GOLD + "/wepos1 + /wepos2" + ChatColor.LIGHT_PURPLE + " - Set points of a selection at your location";
        cvhpos = ChatColor.GOLD + "/wehpos1 + /wehpos2" + ChatColor.LIGHT_PURPLE + " - Set points of a selection to the block you're looking at";
        cvexpand = ChatColor.GOLD + "/weexpand <number> [direction]" + ChatColor.LIGHT_PURPLE + " - Expand your selection in the specified direction or in the direction you're looking if not specified";
        cvcontract = ChatColor.GOLD + "/wecontract <number> [direction]" + ChatColor.LIGHT_PURPLE + " - Contract your selection in the specified direction or in the direction you're looking if not specified";
        cvshift = ChatColor.GOLD + "/weshift <number> [direction]" + ChatColor.LIGHT_PURPLE + " - Move your selected area in the specified direction or in the direction you're looking if not specified";
        cvsel = ChatColor.GOLD + "/wesel" + ChatColor.LIGHT_PURPLE + " - Clear your current selection";
        cvsize = ChatColor.GOLD + "/wesize" + ChatColor.LIGHT_PURPLE + " - List your current selection's size and dimensions";

        cvcut = ChatColor.GOLD + "/wecut" + ChatColor.LIGHT_PURPLE + " - Copy your current selection to the clipboard then delete it";
        cvcopy = ChatColor.GOLD + "/wecopy" + ChatColor.LIGHT_PURPLE + " - Copy your current selection to the clipboard";
        cvpaste = ChatColor.GOLD + "/wepaste" + ChatColor.LIGHT_PURPLE + " - Pastes your current clipboard where you're standing";
        cvflip = ChatColor.GOLD + "/weflip [direction]" + ChatColor.LIGHT_PURPLE + " - Flips your clipboard in the specified direction or in the direction you're looking if not specified";
        cvrotate = ChatColor.GOLD + "/werotate <y> [x] [z]" + ChatColor.LIGHT_PURPLE + " - Rotates your clipboard on the y, x, and/or z axis. (y is required)";
        cvclearclipboard = ChatColor.GOLD + "/weclearclipboard" + ChatColor.LIGHT_PURPLE + " - Clears your current clipboard";

        cvstack = ChatColor.GOLD + "/westack <number> [direction]" + ChatColor.LIGHT_PURPLE + " - Replicate your selection in the specified direction or in the direction you're looking if not specified";
        cvmove = ChatColor.GOLD + "/wemove <number> [direction]" + ChatColor.LIGHT_PURPLE + " - Move your selection in the specified direction or in the direction you're looking if not specified";
        cvset = ChatColor.GOLD + "/weset <targetBlock>" + ChatColor.LIGHT_PURPLE + " - Sets your current selection to the target block";
        cvreplace = ChatColor.GOLD + "/wereplace <sourceBlock> <targetBlock>" + ChatColor.LIGHT_PURPLE + " - Replaces the source blocks in your selection with the target block";
        cvwalls = ChatColor.GOLD + "/wewalls <targetBlock>" + ChatColor.LIGHT_PURPLE + " - Sets the sides/walls of your current selection to the target block";
        cvfaces = ChatColor.GOLD + "/wefaces <targetBlock>" + ChatColor.LIGHT_PURPLE + " - Sets the walls, floor, and ceiling of your current selection to the target block";
        cvundo = ChatColor.GOLD + "/weundo" + ChatColor.LIGHT_PURPLE + " - Undoes your last block-changing command";
        cvredo = ChatColor.GOLD + "/weredo" + ChatColor.LIGHT_PURPLE + " - Redoes your last undone block-changing command";
        cvclearhistory = ChatColor.GOLD + "/weclearhistory" + ChatColor.LIGHT_PURPLE + " - Clears your block-changing commands from history";
        cvclearplot = ChatColor.GOLD + "/weclearplot" + ChatColor.LIGHT_PURPLE + " - Clears your entire plot";
    }

    @Override
    public CommandResponse execute(Player sender, Set<String> set, Map<String, Object> map, List<Object> baseParameters) {

        if (baseParameters.size() > 1) {
            return new CommandResponse(ChatColor.RED + "Invalid Command!" + ChatColor.LIGHT_PURPLE + " Proper Usage: /wehelp [page]");
        }

        //Build the text component and send to player
        int i = baseParameters.size() == 1 ? (int) baseParameters.get(0) : 1;
        if(i == 1) {
            List<TextComponent> out = new ArrayList<>();
            out.add(new TextComponent(ChatColor.BLUE + "               Selection commands               "));
            out.add(new TextComponent(cvwand));
            out.add(new TextComponent(cvpos));
            out.add(new TextComponent(cvhpos));
            out.add(new TextComponent(cvexpand));
            out.add(new TextComponent(cvcontract));
            out.add(new TextComponent(cvshift));
            out.add(new TextComponent(cvsel));
            out.add(new TextComponent(cvsize));
            TextComponent bottom = new TextComponent(ChatColor.BLUE + "Page 1/3 | ");
            TextComponent next = new TextComponent(ChatColor.AQUA + "Next ->");
            next.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Next Page")));
            next.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wehelp 2"));
            bottom.addExtra(next);
            out.add(new TextComponent(bottom));
            for(TextComponent o : out) {
                sender.spigot().sendMessage(o);
            }
            return new CommandResponse("");
        } else if(i == 2) {
            List<TextComponent> out = new ArrayList<>();
            out.add(new TextComponent(ChatColor.BLUE + "               Clipboard commands               "));
            out.add(new TextComponent(cvcut));
            out.add(new TextComponent(cvcopy));
            out.add(new TextComponent(cvpaste));
            out.add(new TextComponent(cvflip));
            out.add(new TextComponent(cvrotate));
            out.add(new TextComponent(cvclearclipboard));
            TextComponent previous = new TextComponent(ChatColor.AQUA + "<- Previous");
            previous.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Previous Page")));
            previous.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wehelp 1"));
            previous.addExtra(new TextComponent(ChatColor.BLUE + " | Page 2/3 | "));
            TextComponent next = new TextComponent(ChatColor.AQUA + "Next ->");
            next.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Next Page")));
            next.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wehelp 3"));
            previous.addExtra(next);
            out.add(new TextComponent(previous));
            for(TextComponent o : out) {
                sender.spigot().sendMessage(o);
            }
            return new CommandResponse("");
        } else if(i == 3) {
            List<TextComponent> out = new ArrayList<>();
            out.add(new TextComponent(ChatColor.BLUE + "               Other commands               "));
            out.add(new TextComponent(cvstack));
            out.add(new TextComponent(cvmove));
            out.add(new TextComponent(cvset));
            out.add(new TextComponent(cvreplace));
            out.add(new TextComponent(cvwalls));
            out.add(new TextComponent(cvfaces));
            out.add(new TextComponent(cvundo));
            out.add(new TextComponent(cvredo));
            out.add(new TextComponent(cvclearhistory));
            out.add(new TextComponent(cvclearplot));
            TextComponent previous = new TextComponent(ChatColor.AQUA + "<- Previous");
            previous.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Previous Page")));
            previous.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wehelp 2"));
            previous.addExtra(new TextComponent(ChatColor.BLUE + " | Page 2/3"));
            out.add(new TextComponent(previous));
            for(TextComponent o : out) {
                sender.spigot().sendMessage(o);
            }
            return new CommandResponse("");
        } else {
            return new CommandResponse(ChatColor.RED + "Invalid Command! There are only 3 wehelp pages!");
        }
    }
}
