package org.cubeville.cvworldedit;

import org.cubeville.cvworldedit.CVWorldEdit;

import java.util.List;

public class CheckBlacklist {

    final private List<String> blockBlacklist;

    public CheckBlacklist(CVWorldEdit plugin) {
        blockBlacklist = plugin.getBlockBlacklist();
    }

    public boolean checkBlockBanned(String targetBlock) {
        for(String block : blockBlacklist) {
            if(targetBlock.equalsIgnoreCase(block)) {
                return true;
            }
        }
        return false;
    }
}
