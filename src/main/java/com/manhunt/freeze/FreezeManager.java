package com.manhunt.freeze;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public class FreezeManager {
    
    private final Map<UUID, Location> frozenPlayers = new HashMap<>();
    
    public void freezePlayer(Player player) {
        frozenPlayers.put(player.getUniqueId(), player.getLocation().clone());
    }
    
    public void unfreezePlayer(Player player) {
        frozenPlayers.remove(player.getUniqueId());
    }
    
    public boolean isFrozen(Player player) {
        return frozenPlayers.containsKey(player.getUniqueId());
    }
    
    public void unfreezeAll() {
        frozenPlayers.clear();
    }
    
    public Location getFrozenLocation(Player player) {
        return frozenPlayers.get(player.getUniqueId());
    }
}