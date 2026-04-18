package com.manhunt.listener;

import com.manhunt.ManhuntPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
    private final ManhuntPlugin plugin;
    
    public PlayerQuitListener(ManhuntPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        if (!plugin.getGameManager().isGameActive()) {
            return;
        }
        
        if (plugin.getRoleManager().isHunter(player)) {
            plugin.getReconnectManager().registerDisconnect(
                player.getUniqueId(),
                "hunter",
                player.getGameMode()
            );
        } else if (plugin.getRoleManager().isRunner(player)) {
            plugin.getReconnectManager().registerDisconnect(
                player.getUniqueId(),
                "runner",
                player.getGameMode()
            );
        }
    }
}