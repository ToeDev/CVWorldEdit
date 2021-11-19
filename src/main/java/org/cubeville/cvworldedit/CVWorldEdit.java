package org.cubeville.cvworldedit;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.extension.platform.permission.ActorSelectorLimits;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.cubeville.commons.commands.CommandParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CVWorldEdit extends JavaPlugin implements Listener {

    private Logger logger;

    private String prefix;
    private List<String> blockBlacklist;
    private Double commandCooldown;
    private int blockVolumeLimit;
    private int clearPlotVolume;
    private int plotYLevel;
    private List<String> clearPlotEntityList;

    private HashMap<UUID, Double> commandCooldownList;
    private HashMap<UUID, Integer> taskIDCheckList;
    private HashMap<UUID, Integer> taskIDClearList;
    private HashMap<UUID, BlockArrayClipboard> clipboardList;
    private HashMap<UUID, Integer> blocksCopiedList;
    private HashMap<UUID, Integer> rotationYList;

    private CommandParser clearPlotParser;
    private CommandParser setParser;
    private CommandParser wallsParser;
    private CommandParser facesParser;
    private CommandParser replaceParser;
    private CommandParser copyParser;
    private CommandParser pasteParser;
    private CommandParser rotateParser;
    private CommandParser undoParser;
    private CommandParser redoParser;
    private CommandParser clearHistoryParser;
    private CommandParser pos1Parser;
    private CommandParser pos2Parser;
    private CommandParser wandParser;
    private CommandParser expandParser;
    private CommandParser contractParser;
    private CommandParser sizeParser;
    private CommandParser selectionParser;

    @Override
    public void onEnable() {
        this.logger = getLogger();

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
                logger.log(Level.WARNING, ChatColor.LIGHT_PURPLE + "Unable to generate config file", e);
                throw new RuntimeException(ChatColor.LIGHT_PURPLE + "Unable to generate config file", e);
            }
        }

        this.prefix = ChatColor.GRAY + "[" + ChatColor.DARK_RED + "CVWorldEdit" + ChatColor.GRAY + "]" + " ";
        this.rotationYList = new HashMap<>();
        this.commandCooldownList = new HashMap<>();
        this.taskIDCheckList = new HashMap<>();
        this.taskIDClearList = new HashMap<>();
        this.clipboardList = new HashMap<>();
        this.blocksCopiedList = new HashMap<>();
        this.blockBlacklist = new ArrayList<>();
        this.clearPlotEntityList = new ArrayList<>();
        YamlConfiguration mainConfig = new YamlConfiguration();
        try {
            mainConfig.load(configFile);
            for(String block : mainConfig.getStringList("Block-Blacklist")) {
                blockBlacklist.add(block);
                logger.log(Level.INFO, ChatColor.GOLD + block + ChatColor.LIGHT_PURPLE + " loaded from blacklist");
            }
            for(String entity : mainConfig.getStringList("Plot-Clear-Entity")) {
                clearPlotEntityList.add(entity);
                logger.log(Level.INFO, ChatColor.GOLD + entity + ChatColor.LIGHT_PURPLE + " loaded from plotclear entity list");
            }
            commandCooldown = mainConfig.getDouble("Command-Cooldown");
            logger.log(Level.INFO, ChatColor.LIGHT_PURPLE + "Command Cooldown loaded from config: " + ChatColor.GOLD + commandCooldown);
            blockVolumeLimit = mainConfig.getInt("Block-Volume-Limit");
            logger.log(Level.INFO, ChatColor.LIGHT_PURPLE + "Block Volume Limit loaded from config: " + ChatColor.GOLD + blockVolumeLimit);
            clearPlotVolume = mainConfig.getInt("Plot-Clear-Volume");
            logger.log(Level.INFO, ChatColor.LIGHT_PURPLE + "Plot Block Volume loaded from config: " + ChatColor.GOLD + clearPlotVolume);
            plotYLevel = mainConfig.getInt("Plot-Y-Level");
            logger.log(Level.INFO, ChatColor.LIGHT_PURPLE + "Plot Y-Level loaded from config: " + ChatColor.GOLD + plotYLevel);
        } catch(IOException | InvalidConfigurationException e) {
            logger.log(Level.WARNING, ChatColor.LIGHT_PURPLE + "Unable to load config file", e);
        }

        clearPlotParser = new CommandParser();
        clearPlotParser.addCommand(new CVWorldEditClearPlot(this));
        CVWorldEditCheckBlacklist pluginBlacklist = new CVWorldEditCheckBlacklist(this);
        CVWorldEditCheckRegion pluginCheckRegion = new CVWorldEditCheckRegion();
        CVWorldEditCommandCooldown pluginCommandCooldown = new CVWorldEditCommandCooldown(this);
        CVWorldEditRotate pluginRotate = new CVWorldEditRotate(this);
        setParser = new CommandParser();
        setParser.addCommand(new CVWorldEditSet(this, pluginBlacklist, pluginCheckRegion, pluginCommandCooldown));
        wallsParser = new CommandParser();
        wallsParser.addCommand(new CVWorldEditWalls(this, pluginBlacklist, pluginCheckRegion, pluginCommandCooldown));
        facesParser = new CommandParser();
        facesParser.addCommand(new CVWorldEditFaces(this, pluginBlacklist, pluginCheckRegion, pluginCommandCooldown));
        replaceParser = new CommandParser();
        replaceParser.addCommand(new CVWorldEditReplace(this, pluginBlacklist, pluginCheckRegion, pluginCommandCooldown));
        copyParser = new CommandParser();
        CVWorldEditCopy copyCommand = new CVWorldEditCopy(this, pluginRotate, pluginBlacklist, pluginCheckRegion);
        copyParser.addCommand(copyCommand);
        rotateParser = new CommandParser();
        rotateParser.addCommand(new CVWorldEditRotate(this));
        pasteParser = new CommandParser();
        pasteParser.addCommand(new CVWorldEditPaste(this, copyCommand, pluginRotate, pluginCheckRegion, pluginCommandCooldown));
        undoParser = new CommandParser();
        undoParser.addCommand(new CVWorldEditUndo(this, pluginCommandCooldown));
        redoParser = new CommandParser();
        redoParser.addCommand(new CVWorldEditRedo(this, pluginCommandCooldown));
        clearHistoryParser = new CommandParser();
        clearHistoryParser.addCommand(new CVWorldEditClearHistory(this));
        pos1Parser = new CommandParser();
        pos1Parser.addCommand(new CVWorldEditPos1(this));
        pos2Parser = new CommandParser();
        pos2Parser.addCommand(new CVWorldEditPos2(this));
        wandParser = new CommandParser();
        wandParser.addCommand(new CVWorldEditWand(this));
        expandParser = new CommandParser();
        expandParser.addCommand(new CVWorldEditExpand(this));
        contractParser = new CommandParser();
        contractParser.addCommand(new CVWorldEditContract(this));
        sizeParser = new CommandParser();
        sizeParser.addCommand(new CVWorldEditSize(this));
        selectionParser = new CommandParser();
        selectionParser.addCommand(new CVWorldEditSelection(this));
        Bukkit.getPluginManager().registerEvents(this, this);

        logger.info(ChatColor.LIGHT_PURPLE + "Plugin Enabled Successfully");
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

    public HashMap<UUID, BlockArrayClipboard> getClipboardList() {
        return this.clipboardList;
    }

    public HashMap<UUID, Integer> getBlocksCopiedList() {
        return this.blocksCopiedList;
    }

    @EventHandler
    public void onNetheriteHoeClick(final PlayerInteractEvent event) {
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
            case "cvclearplot":
                return clearPlotParser.execute(sender, args);
            case "cvpos1":
                return pos1Parser.execute(sender, args);
            case "cvpos2":
                return pos2Parser.execute(sender, args);
            case "cvwand":
                return wandParser.execute(sender, args);
            case "cvexpand":
                return expandParser.execute(sender, args);
            case "cvcontract":
                return contractParser.execute(sender, args);
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
                return replaceParser.execute(sender, args);
            case "cvcopy":
                return copyParser.execute(sender, args);
            case "cvpaste":
                return pasteParser.execute(sender, args);
            case "cvrotate":
                return rotateParser.execute(sender, args);
            case "cvundo":
                return undoParser.execute(sender, args);
            case "cvredo":
                return redoParser.execute(sender, args);
            case "cvclearhistory":
                return clearHistoryParser.execute(sender, args);
            default:
                return false;
        }
    }

    @Override
    public void onDisable() {
        logger.info(ChatColor.LIGHT_PURPLE + "Plugin Disabled Successfully");
    }
}
