package com.manhunt.listener;

import com.manhunt.ManhuntPlugin;
import com.manhunt.util.MessageUtil;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EntityDamageListener implements Listener {
    
    private final ManhuntPlugin plugin;
    
    public EntityDamageListener(ManhuntPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!plugin.getGameManager().isGameActive()) {
            return;
        }
        
        if (event.getEntityType() != EntityType.ENDER_DRAGON) {
            return;
        }
        
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        
        Player damager = (Player) event.getDamager();
        
        if (plugin.getRoleManager().isHunter(damager) && !plugin.getConfigManager().isHunterCanDamageDragon()) {
            event.setCancelled(true);
            MessageUtil.send(damager, "&cYou cannot damage the Ender Dragon! Only runners can kill it.");
        }
    }
}