package com.manhunt.listener;

import com.manhunt.ManhuntPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {
    
    private final ManhuntPlugin plugin;
    
    public PlayerDeathListener(ManhuntPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        
        if (!plugin.getGameManager().isGameActive()) {
            return;
        }
        
        if (plugin.getRoleManager().isRunner(player)) {
            plugin.getGameManager().handleRunnerDeath(player);
        }
    }
}