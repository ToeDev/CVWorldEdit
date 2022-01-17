package org.cubeville.cvworldedit;


import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.InvocationTargetException;

public class PacketListener implements Listener {

    public PacketListener(CVWorldEdit cvWorldEdit, ProtocolManager protocolManager) {
        protocolManager.addPacketListener(
                new PacketAdapter(cvWorldEdit, ListenerPriority.HIGHEST, PacketType.Play.Server.SET_SLOT) {
                    @Override
                    public void onPacketSending(PacketEvent event) {
                        PacketContainer packet = event.getPacket();
                        if(packet.getItemModifier().readSafely(0).getType() == Material.AIR) return;

                        StructureModifier<ItemStack> items = packet.getItemModifier();
                        ItemStack stack = items.readSafely(0);
                        ItemMeta meta = stack.getItemMeta();
                        if(meta == null) return;

                        Player player = event.getPlayer();
                        if(meta.getLore() != null) {
                            for(String lore : meta.getLore()) {
                                if(lore.equalsIgnoreCase("(+NBT)")) {
                                    ItemStack newStack = new ItemStack(Material.AIR);
                                    items.write(0, newStack);
                                    player.closeInventory();
                                    player.sendMessage(ChatColor.RED + "The item '" + ChatColor.GOLD + stack.getType() + ChatColor.RED + "' has been removed due to NBT values that are not allowed on this server!");
                                    break;
                                }
                            }
                        }
                    }
                }
        );
    }

    /*public PacketListener(CVWorldEdit cvWorldEdit, ProtocolManager protocolManager) {
        protocolManager.addPacketListener(
                new PacketAdapter(cvWorldEdit, ListenerPriority.HIGHEST, PacketType.Play.Server.CLOSE_WINDOW) {
                    @Override
                    public void onPacketReceiving(PacketEvent event) {
                        PacketContainer packet = event.getPacket();
                        if(packet.getItemModifier().readSafely(0).getType() == Material.AIR) return;

                        StructureModifier<ItemStack> items = packet.getItemModifier();
                        ItemStack stack = items.readSafely(0);
                        ItemMeta meta = stack.getItemMeta();
                        if(meta == null) return;

                        Player player = event.getPlayer();
                        if(meta.getLore() != null) {
                            for(String lore : meta.getLore()) {
                                if(lore.equalsIgnoreCase("(+NBT)")) {
                                    ItemStack newStack = new ItemStack(Material.AIR);
                                    items.write(0, newStack);
                                    //player.updateInventory();
                                    player.sendMessage(ChatColor.RED + "This item contains NBT values not allowed on the server. Item Removed: " + stack.getType());
                                    break;
                                }
                            }
                        }
                    }
                }
        );
    }*/

    /*PacketContainer fakeExplosion = new PacketContainer(PacketType.Play.Server.EXPLOSION);
                        fakeExplosion.getDoubles().
                                write(0, event.getPlayer().getLocation().getX()).
                                write(1, event.getPlayer().getLocation().getY()).
                                write(2, event.getPlayer().getLocation().getZ());
                        fakeExplosion.getFloat().write(0, 3.0F);
                        try {
                            protocolManager.sendServerPacket(event.getPlayer(), fakeExplosion);
                        } catch (InvocationTargetException e) {
                            throw new RuntimeException("Cannot send packet " + fakeExplosion, e);
                        }*/
}
