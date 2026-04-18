package com.manhunt.role;

import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.*;

@Getter
public class RoleManager {
    
    private final Set<UUID> hunters = new HashSet<>();
    private final Set<UUID> runners = new HashSet<>();
    
    public void addHunter(Player player) {
        runners.remove(player.getUniqueId());
        hunters.add(player.getUniqueId());
    }
    
    public void addRunner(Player player) {
        hunters.remove(player.getUniqueId());
        runners.add(player.getUniqueId());
    }
    
    public void removePlayer(Player player) {
        hunters.remove(player.getUniqueId());
        runners.remove(player.getUniqueId());
    }
    
    public boolean isHunter(Player player) {
        return hunters.contains(player.getUniqueId());
    }
    
    public boolean isRunner(Player player) {
        return runners.contains(player.getUniqueId());
    }
    
    public boolean isInGame(Player player) {
        return isHunter(player) || isRunner(player);
    }
    
    public List<Player> getOnlineHunters() {
        List<Player> online = new ArrayList<>();
        for (UUID uuid : hunters) {
            Player player = org.bukkit.Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                online.add(player);
            }
        }
        return online;
    }
    
    public List<Player> getOnlineRunners() {
        List<Player> online = new ArrayList<>();
        for (UUID uuid : runners) {
            Player player = org.bukkit.Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                online.add(player);
            }
        }
        return online;
    }
    
    public void clear() {
        hunters.clear();
        runners.clear();
    }
    
    public boolean hasEnoughPlayers() {
        return !hunters.isEmpty() && !runners.isEmpty();
    }
}