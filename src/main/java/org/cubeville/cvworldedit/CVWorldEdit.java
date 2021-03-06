package org.cubeville.cvworldedit;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.extension.platform.permission.ActorSelectorLimits;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.cubeville.commons.commands.CommandParser;
import org.cubeville.cvworldedit.commands.*;
import org.cubeville.cvworldedit.commands.Stack;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class CVWorldEdit extends JavaPlugin implements Listener {


    private String prefix;
    private List<String> blockBlacklist;
    private Double commandCooldown;
    private int blockVolumeLimit;
    private int clearPlotVolume;
    private int plotYLevel;
    private List<String> clearPlotEntityList;
    private HashMap<UUID, String> clearPlotRunningList;
    private int clearPlotRunningLimit;

    private HashMap<UUID, Double> commandCooldownList;
    private HashMap<UUID, Integer> taskIDCheckList;
    private HashMap<UUID, Integer> taskIDClearList;
    private HashMap<UUID, Integer> rotationYList;

    private CommandParser helpParser;
    private CommandParser clearPlotParser;
    private CommandParser setParser;
    private CommandParser wallsParser;
    private CommandParser facesParser;
    private CommandParser replaceParser;
    private CommandParser stackParser;
    private CommandParser moveParser;
    private CommandParser cutParser;
    private CommandParser copyParser;
    private CommandParser pasteParser;
    private CommandParser rotateParser;
    private CommandParser flipParser;
    private CommandParser undoParser;
    private CommandParser redoParser;
    private CommandParser clearHistoryParser;
    private CommandParser clearClipboardParser;
    private CommandParser pos1Parser;
    private CommandParser pos2Parser;
    private CommandParser hPos1Parser;
    private CommandParser hPos2Parser;
    private CommandParser wandParser;
    private CommandParser expandParser;
    private CommandParser contractParser;
    private CommandParser shiftParser;
    private CommandParser sizeParser;
    private CommandParser selectionParser;

    @Override
    public void onEnable() {
        this.prefix = ChatColor.GRAY + "[" + ChatColor.DARK_RED + "CVWorldEdit" + ChatColor.GRAY + "]" + " ";
        ConsoleCommandSender console = Bukkit.getConsoleSender();

        final File dataDir = getDataFolder();
        if(!dataDir.exists()) {
            dataDir.mkdirs();
        }
        File configFile = new File(dataDir, "config.yml");
        if(!configFile.exists()) {
            try {
                configFile.createNewFile();
                final InputStream inputStream = this.getResource(configFile.getName());
                final FileOutputStream fileOutputStream = new FileOutputStream(configFile);
                final byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = Objects.requireNonNull(inputStream).read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                }
                fileOutputStream.flush();
                fileOutputStream.close();
            } catch(IOException e) {
                console.sendMessage(this.prefix + ChatColor.RED + "Unable to generate config file");
                throw new RuntimeException(ChatColor.LIGHT_PURPLE + "Unable to generate config file", e);
            }
        }

        this.rotationYList = new HashMap<>();
        this.commandCooldownList = new HashMap<>();
        this.taskIDCheckList = new HashMap<>();
        this.taskIDClearList = new HashMap<>();
        this.blockBlacklist = new ArrayList<>();
        this.clearPlotEntityList = new ArrayList<>();
        this.clearPlotRunningList = new HashMap<>();
        YamlConfiguration mainConfig = new YamlConfiguration();
        try {
            mainConfig.load(configFile);
            for(String block : mainConfig.getStringList("Block-Blacklist")) {
                blockBlacklist.add(block);
                console.sendMessage(this.prefix + ChatColor.GOLD + block + ChatColor.LIGHT_PURPLE + " loaded from blacklist");
            }
            for(String entity : mainConfig.getStringList("Plot-Clear-Entity")) {
                clearPlotEntityList.add(entity);
                console.sendMessage(this.prefix + ChatColor.GOLD + entity + ChatColor.LIGHT_PURPLE + " loaded from plotclear entity list");
            }
            commandCooldown = mainConfig.getDouble("Command-Cooldown");
            console.sendMessage(this.prefix + ChatColor.LIGHT_PURPLE + "Command Cooldown loaded from config: " + ChatColor.GOLD + commandCooldown);
            blockVolumeLimit = mainConfig.getInt("Block-Volume-Limit");
            console.sendMessage(this.prefix + ChatColor.LIGHT_PURPLE + "Block Volume Limit loaded from config: " + ChatColor.GOLD + blockVolumeLimit);
            clearPlotVolume = mainConfig.getInt("Plot-Clear-Volume");
            console.sendMessage(this.prefix + ChatColor.LIGHT_PURPLE + "Plot Block Volume loaded from config: " + ChatColor.GOLD + clearPlotVolume);
            plotYLevel = mainConfig.getInt("Plot-Y-Level");
            console.sendMessage(this.prefix + ChatColor.LIGHT_PURPLE + "Plot Y-Level loaded from config: " + ChatColor.GOLD + plotYLevel);
            clearPlotRunningLimit = mainConfig.getInt("Plot-Clears-Running-At-A-Time");
            console.sendMessage(this.prefix + ChatColor.LIGHT_PURPLE + "Plot-Clears-Running-At-A-Time loaded from config: " + ChatColor.GOLD + clearPlotRunningLimit);
        } catch(IOException | InvalidConfigurationException e) {
            console.sendMessage(this.prefix + ChatColor.RED + "Unable to load config file");
            console.sendMessage(this.prefix + e);
        }

        helpParser = new CommandParser();
        helpParser.addCommand(new Help());
        clearPlotParser = new CommandParser();
        clearPlotParser.addCommand(new ClearPlot(this));
        Utils pluginUtils = new Utils(this);
        CheckBlacklist pluginBlacklist = new CheckBlacklist(this);
        CheckRegion pluginCheckRegion = new CheckRegion();
        CommandCooldown pluginCommandCooldown = new CommandCooldown(this);
        setParser = new CommandParser();
        setParser.addCommand(new SetCommand(this, pluginBlacklist, pluginCheckRegion, pluginCommandCooldown, pluginUtils));
        wallsParser = new CommandParser();
        wallsParser.addCommand(new Walls(this, pluginBlacklist, pluginCheckRegion, pluginCommandCooldown, pluginUtils));
        facesParser = new CommandParser();
        facesParser.addCommand(new Faces(this, pluginBlacklist, pluginCheckRegion, pluginCommandCooldown, pluginUtils));
        replaceParser = new CommandParser();
        replaceParser.addCommand(new Replace(this, pluginBlacklist, pluginCheckRegion, pluginCommandCooldown, pluginUtils));
        stackParser = new CommandParser();
        stackParser.addCommand(new Stack(this, pluginCheckRegion, pluginCommandCooldown));
        moveParser = new CommandParser();
        moveParser.addCommand(new Move(this, pluginCheckRegion, pluginCommandCooldown));
        cutParser = new CommandParser();
        Cut cutCommand = new Cut(this, pluginBlacklist, pluginCheckRegion);
        cutParser.addCommand(cutCommand);
        copyParser = new CommandParser();
        Copy copyCommand = new Copy(this, pluginBlacklist, pluginCheckRegion);
        copyParser.addCommand(copyCommand);
        flipParser = new CommandParser();
        flipParser.addCommand(new Flip(this));
        rotateParser = new CommandParser();
        rotateParser.addCommand(new Rotate(this));
        pasteParser = new CommandParser();
        pasteParser.addCommand(new Paste(this, pluginCheckRegion, pluginCommandCooldown));
        undoParser = new CommandParser();
        undoParser.addCommand(new Undo(this, pluginCommandCooldown));
        redoParser = new CommandParser();
        redoParser.addCommand(new Redo(this, pluginCommandCooldown));
        clearHistoryParser = new CommandParser();
        clearHistoryParser.addCommand(new ClearHistory(this));
        clearClipboardParser = new CommandParser();
        clearClipboardParser.addCommand(new ClearClipboard(this));
        pos1Parser = new CommandParser();
        pos1Parser.addCommand(new Pos1(this));
        pos2Parser = new CommandParser();
        pos2Parser.addCommand(new Pos2(this));
        hPos1Parser = new CommandParser();
        hPos1Parser.addCommand(new HPos1(this));
        hPos2Parser = new CommandParser();
        hPos2Parser.addCommand(new HPos2(this));
        wandParser = new CommandParser();
        wandParser.addCommand(new Wand(this));
        expandParser = new CommandParser();
        expandParser.addCommand(new Expand(this));
        contractParser = new CommandParser();
        contractParser.addCommand(new Contract(this));
        shiftParser = new CommandParser();
        shiftParser.addCommand(new Shift(this));
        sizeParser = new CommandParser();
        sizeParser.addCommand(new Size(this));
        selectionParser = new CommandParser();
        selectionParser.addCommand(new Selection(this));
        Bukkit.getPluginManager().registerEvents(this, this);

        console.sendMessage(this.prefix + ChatColor.LIGHT_PURPLE + "Plugin Enabled Successfully");
    }

    public String getPrefix() {
        return this.prefix;
    }

    public HashMap<UUID, Integer> getRotationYList() {
        return this.rotationYList;
    }

    public List<String> getBlockBlacklist() {
        return this.blockBlacklist;
    }

    public List<String> getClearPlotEntityList() {
        return this.clearPlotEntityList;
    }

    public HashMap<UUID, String> getClearPlotRunningList() {
        return this.clearPlotRunningList;
    }

    public int getClearPlotRunningLimit() {
        return this.clearPlotRunningLimit;
    }

    public Double getCommandCooldown() {
        return this.commandCooldown;
    }

    public int getBlockVolumeLimit() {
        return this.blockVolumeLimit;
    }

    public int getClearPlotVolume() {
        return this.clearPlotVolume;
    }

    public int getPlotYLevel() {
        return this.plotYLevel;
    }

    public HashMap<UUID, Double> getCommandCooldownList() {
        return this.commandCooldownList;
    }

    public HashMap<UUID, Integer> getTaskIDCheckList() {
        return this.taskIDCheckList;
    }

    public HashMap<UUID, Integer> getTaskIDClearList() {
        return this.taskIDClearList;
    }

    public String getPlayerFacing(Player player) {
        float yaw = player.getLocation().getYaw();
        float pitch = player.getLocation().getPitch();
        if(pitch >= 67) return "down";
        if(pitch <= -67) return "up";
        if(yaw < 45 && yaw >= -45) return "south";
        if(yaw < -45 && yaw >= -135) return "east";
        if((yaw < -135 && yaw >= -180) || (yaw < 180 && yaw >= 135)) return "north";
        if(yaw < 135 && yaw >= 45) return "west";
        return null;
    }

    public String getPlayerFacingSpecific(Player player) {
        float yaw = player.getLocation().getYaw();
        float pitch = player.getLocation().getPitch();
        if(pitch >= 67) return "down";
        if(pitch <= -67) return "up";
        if(yaw < 22.5 && yaw >= -22.5) return "south";
        if(yaw < -22.5 && yaw >= -67.5) return "southeast";
        if(yaw < -67.5 && yaw >= -112.5) return "east";
        if(yaw < -112.5 && yaw >= -157.5) return "northeast";
        if((yaw < -157.5 && yaw >= -180) || (yaw < 180) && yaw >= 157.5) return "north";
        if(yaw < 157.5 && yaw >= 112.5) return "northwest";
        if(yaw < 112.5 && yaw >= 67.5) return "west";
        if(yaw < 67.5 && yaw >= 22.5) return "southwest";
        return null;
    }

    @EventHandler
    public void onNetheriteHoeClick(final PlayerInteractEvent event) {
        if(Objects.equals(event.getHand(), EquipmentSlot.OFF_HAND)) return;
        if(!event.getPlayer().getInventory().getItemInMainHand().equals(new ItemStack(Material.NETHERITE_HOE))) {
            return;
        }
        if(event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
            return;
        }
        if(!event.getPlayer().hasPermission("CVWorldEdit.selection.commands")) {
            return;
        }
        event.setCancelled(true);
        Player player = event.getPlayer();
        org.bukkit.Location pos = Objects.requireNonNull(event.getClickedBlock()).getLocation();
        BlockVector3 vec = BlockVector3.at(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
        BukkitPlayer bPlayer = BukkitAdapter.adapt(player);
        LocalSession localSession = WorldEdit.getInstance().getSessionManager().get(bPlayer);
        RegionSelector selector = localSession.getRegionSelector(bPlayer.getWorld());
        if(event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            selector.selectPrimary(vec, ActorSelectorLimits.forActor(bPlayer));
            if(selector.isDefined()) {
                Region playerSelection;
                try {
                    playerSelection = selector.getRegion();
                    player.sendMessage(prefix + ChatColor.LIGHT_PURPLE + "Position 1 set at " + pos.getBlockX() + ", " + pos.getBlockY() + ", " + pos.getBlockZ() + " Block Count: " + playerSelection.getVolume());
                } catch (IncompleteRegionException e) {
                    player.sendMessage(prefix + ChatColor.RED + "Selection not retrieved! Contact Administrator!");
                }
            } else {
                player.sendMessage(prefix + ChatColor.LIGHT_PURPLE + "Position 1 set at " + vec.getBlockX() + ", " + vec.getBlockY() + ", " + vec.getBlockZ());
            }
        } else if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            selector.selectSecondary(vec, ActorSelectorLimits.forActor(bPlayer));
            if(selector.isDefined()) {
                Region playerSelection;
                try {
                    playerSelection = selector.getRegion();
                    player.sendMessage(prefix + ChatColor.LIGHT_PURPLE + "Position 2 set at " + pos.getBlockX() + ", " + pos.getBlockY() + ", " + pos.getBlockZ() + " Block Count: " + playerSelection.getVolume());
                } catch (IncompleteRegionException e) {
                    player.sendMessage(prefix + ChatColor.RED + "Selection not retrieved! Contact Administrator!");
                }
            } else {
                player.sendMessage(prefix + ChatColor.LIGHT_PURPLE + "Position 2 set at " + vec.getBlockX() + ", " + vec.getBlockY() + ", " + vec.getBlockZ());
            }
        }
        selector.explainRegionAdjust(bPlayer, localSession);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        switch(command.getName().toLowerCase()) {
            case "cvhelp":
                return helpParser.execute(sender, args);
            case "cvclearplot":
                return clearPlotParser.execute(sender, args);
            case "cvpos1":
                return pos1Parser.execute(sender, args);
            case "cvpos2":
                return pos2Parser.execute(sender, args);
            case "cvhpos1":
                return hPos1Parser.execute(sender, args);
            case "cvhpos2":
                return hPos2Parser.execute(sender, args);
            case "cvwand":
                return wandParser.execute(sender, args);
            case "cvexpand":
                return expandParser.execute(sender, args);
            case "cvcontract":
                return contractParser.execute(sender, args);
            case "cvshift":
                return shiftParser.execute(sender, args);
            case "cvsize":
                return sizeParser.execute(sender, args);
            case "cvselection":
            case "cvsel":
                return selectionParser.execute(sender, args);
            case "cvset":
                return setParser.execute(sender, args);
            case "cvwalls":
                return wallsParser.execute(sender, args);
            case "cvfaces":
                return facesParser.execute(sender, args);
            case "cvreplace":
            case "cvrep":
                return replaceParser.execute(sender, args);
            case "cvstack":
                return stackParser.execute(sender, args);
            case "cvmove":
                return moveParser.execute(sender, args);
            case "cvcut":
                return cutParser.execute(sender, args);
            case "cvcopy":
                return copyParser.execute(sender, args);
            case "cvpaste":
                return pasteParser.execute(sender, args);
            case "cvflip":
                return flipParser.execute(sender, args);
            case "cvrotate":
                return rotateParser.execute(sender, args);
            case "cvundo":
                return undoParser.execute(sender, args);
            case "cvredo":
                return redoParser.execute(sender, args);
            case "cvclearhistory":
                return clearHistoryParser.execute(sender, args);
            case "cvclearclipboard":
                return clearClipboardParser.execute(sender, args);
            default:
                return false;
        }
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage(this.prefix + ChatColor.LIGHT_PURPLE + "Plugin Disabled Successfully");
    }
}
