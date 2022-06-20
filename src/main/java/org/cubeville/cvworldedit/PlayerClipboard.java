package org.cubeville.cvworldedit;

import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;

import java.util.HashMap;
import java.util.UUID;

public class PlayerClipboard {

    final private HashMap<UUID, BlockArrayClipboard> clipboardList;
    final private HashMap<UUID, Integer> blocksCopiedList;

    public PlayerClipboard() {
        clipboardList = new HashMap<>();
        blocksCopiedList = new HashMap<>();
    }

    public BlockArrayClipboard getClipboard(UUID uuid) {
        if(clipboardList.containsKey(uuid)) {
            return clipboardList.get(uuid);
        }
        return null;
    }

    public void saveClipboard(UUID uuid, BlockArrayClipboard clipboard) {
        clipboardList.put(uuid, clipboard);
    }

    public void clearClipboard(UUID uuid) {
        clipboardList.remove(uuid);
    }

    public int getBlocksCopied(UUID uuid) {
        return blocksCopiedList.get(uuid);
    }

    public void saveBlocksCopied(UUID uuid, int blocksCopied) {
        blocksCopiedList.put(uuid, blocksCopied);
    }

    public void clearBlocksCopied(UUID uuid) {
        blocksCopiedList.remove(uuid);
    }
}
