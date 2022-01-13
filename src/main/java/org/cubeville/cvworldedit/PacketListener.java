package org.cubeville.cvworldedit;


import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

public class PacketListener {

    private final ProtocolManager protocolManager;

    public PacketListener(ProtocolManager protocolManager) {
        this.protocolManager = protocolManager;

        // Disable all sound effects
        protocolManager.addPacketListener(
                new PacketAdapter(this, ListenerPriority.NORMAL,
                        PacketType.Play.Server.NAMED_SOUND_EFFECT) {
                    @Override
                    public void onPacketSending(PacketEvent event) {
                        // Item packets (id: 0x29)
                        if (event.getPacketType() ==
                                PacketType.Play.Server.NAMED_SOUND_EFFECT) {
                            event.setCancelled(true);
                        }
                    }
                });
    }
}
