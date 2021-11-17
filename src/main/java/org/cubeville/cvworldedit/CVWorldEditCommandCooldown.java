package org.cubeville.cvworldedit;

import java.util.HashMap;
import java.util.UUID;

public class CVWorldEditCommandCooldown {

    final private double cooldown; //the command cooldown
    final private HashMap<UUID, Double> commandCooldownList;

    public CVWorldEditCommandCooldown(CVWorldEdit plugin) {
        this.commandCooldownList = plugin.getCommandCooldownList();
        this.cooldown = plugin.getCommandCooldown();
    }

    //Used to get the players current command cooldown (this cooldown is for the following commands /cvset, /cvpaste, /cvreplace
    public double getCommandCooldown(UUID uuid) {
        if(!commandCooldownList.containsKey(uuid)) {
            return 0.0D;
        }
        else if((System.currentTimeMillis() / 1000.0D) < commandCooldownList.get(uuid)) {
            return commandCooldownList.get(uuid) - (System.currentTimeMillis() / 1000.0D);
        } else {
            return 0.0D;
        }
    }

    public void startCommandCooldown(UUID uuid) {
        commandCooldownList.put(uuid, cooldown + (System.currentTimeMillis() / 1000.0D));
    }
}
