package com.manhunt.listener;

import com.manhunt.ManhuntPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {
    
    private final ManhuntPlugin plugin;
    
    public PlayerMoveListener(ManhuntPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        if (!plugin.getFreezeManager().isFrozen(player)) {
            if (plugin.getGameManager().isCountdownActive() && 
                plugin.getRoleManager().isRunner(player) &&
                !plugin.getGameManager().isGameStarted()) {
                
                Location from = event.getFrom();
                Location to = event.getTo();
                
                if (to != null && (from.getX() != to.getX() || from.getZ() != to.getZ())) {
                    plugin.getGameManager().onRunnerMove();
                }
            }
            return;
        }
        
        Location from = event.getFrom();
        Location to = event.getTo();
        
        if (to == null) {
            return;
        }
        
        if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
            Location frozenLoc = plugin.getFreezeManager().getFrozenLocation(player);
            if (frozenLoc != null) {
                event.setTo(frozenLoc);
            }
        }
    }
}