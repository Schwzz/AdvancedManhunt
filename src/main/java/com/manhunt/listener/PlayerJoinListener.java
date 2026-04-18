package com.manhunt.listener;

import com.manhunt.ManhuntPlugin;
import com.manhunt.reconnect.ReconnectManager;
import com.manhunt.util.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    
    private final ManhuntPlugin plugin;
    
    public PlayerJoinListener(ManhuntPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        if (!plugin.getGameManager().isGameActive()) {
            return;
        }
        
        if (plugin.getReconnectManager().hasReconnectData(player.getUniqueId())) {
            ReconnectManager.ReconnectData data = plugin.getReconnectManager().getReconnectData(player.getUniqueId());
            
            if (data.getRole().equals("hunter")) {
                plugin.getRoleManager().addHunter(player);
            } else if (data.getRole().equals("runner")) {
                plugin.getRoleManager().addRunner(player);
            }
            
            player.setGameMode(data.getGameMode());
            plugin.getReconnectManager().removeReconnectData(player.getUniqueId());
            
            String role = data.getRole().equals("hunter") ? "&cHunter" : "&aRunner";
            MessageUtil.send(plugin, player, "&7You have reconnected as " + role + "&7!");
            return;
        }
        
        if (plugin.getRoleManager().isInGame(player)) {
            String role = plugin.getRoleManager().isHunter(player) ? "&cHunter" : "&aRunner";
            MessageUtil.send(plugin, player, "&7You are a " + role + " &7in the active manhunt!");
        }
    }
}