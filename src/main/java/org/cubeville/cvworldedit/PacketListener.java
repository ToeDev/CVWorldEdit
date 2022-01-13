package org.cubeville.cvworldedit;


import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;

public class PacketListener implements Listener {

    public PacketListener(CVWorldEdit cvWorldEdit, ProtocolManager protocolManager) {
        protocolManager.addPacketListener(
                new PacketAdapter(cvWorldEdit, ListenerPriority.NORMAL, PacketType.Play.Client.SET_CREATIVE_SLOT) {
                    @Override
                    public void onPacketReceiving(PacketEvent event) {
                        event.getPlayer().sendMessage("size is: " + event.getPacket().getItemModifier().readSafely(0).getItemMeta().);

                        PacketContainer fakeExplosion = new PacketContainer(PacketType.Play.Server.EXPLOSION);
                        fakeExplosion.getDoubles().
                                write(0, event.getPlayer().getLocation().getX()).
                                write(1, event.getPlayer().getLocation().getY()).
                                write(2, event.getPlayer().getLocation().getZ());
                        fakeExplosion.getFloat().write(0, 3.0F);
                        try {
                            protocolManager.sendServerPacket(event.getPlayer(), fakeExplosion);
                        } catch (InvocationTargetException e) {
                            throw new RuntimeException("Cannot send packet " + fakeExplosion, e);
                        }
                    }
                }
        );
    }
}
