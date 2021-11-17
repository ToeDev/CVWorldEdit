package org.cubeville.cvworldedit;

import java.util.List;

public class CVWorldEditCheckBlacklist {

    final private List<String> blockBlacklist;

    public CVWorldEditCheckBlacklist(CVWorldEdit plugin) {
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
