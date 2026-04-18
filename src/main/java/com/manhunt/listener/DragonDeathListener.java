package com.manhunt.listener;

import com.manhunt.ManhuntPlugin;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class DragonDeathListener implements Listener {
    
    private final ManhuntPlugin plugin;
    
    public DragonDeathListener(ManhuntPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onDragonDeath(EntityDeathEvent event) {
        if (event.getEntityType() != EntityType.ENDER_DRAGON) {
            return;
        }
        
        if (!plugin.getGameManager().isGameActive()) {
            return;
        }
        
        Player killer = event.getEntity().getKiller();
        plugin.getGameManager().handleDragonDeath(killer);
    }
}