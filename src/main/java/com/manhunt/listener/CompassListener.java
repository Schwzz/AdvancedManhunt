package com.manhunt.listener;

import com.manhunt.ManhuntPlugin;
import com.manhunt.util.MessageUtil;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class CompassListener implements Listener {
    
    private final ManhuntPlugin plugin;
    
    public CompassListener(ManhuntPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onCompassUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null || XMaterial.matchXMaterial(item.getType()) != XMaterial.COMPASS) {
            return;
        }
        
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        if (!plugin.getGameManager().isGameActive() || plugin.getGameManager().isGamePaused()) {
            return;
        }
        
        if (!plugin.getRoleManager().isHunter(player)) {
            return;
        }
        
        event.setCancelled(true);
        
        if (player.isSneaking()) {
            handleRunnerCycle(player);
            return;
        }
        
        if (plugin.getTrackerManager().isOnCooldown(player)) {
            long remaining = plugin.getTrackerManager().getRemainingCooldown(player);
            String message = plugin.getConfigManager().getCooldownMessage()
                .replace("{time}", String.valueOf(remaining));
            MessageUtil.send(player, message);
            return;
        }
        
        List<Player> runners = plugin.getRoleManager().getOnlineRunners();
        if (runners.isEmpty()) {
            MessageUtil.send(player, "&cNo runners online");
            return;
        }
        
        Player targetRunner = plugin.getTrackerManager().getCurrentTrackedRunner(player);
        if (targetRunner == null || !runners.contains(targetRunner)) {
            targetRunner = findClosestRunner(player, runners);
            plugin.getTrackerManager().setCurrentTrackedRunner(player, targetRunner);
        }
        
        plugin.getTrackerManager().trackRunner(player, targetRunner);
    }
    
    private void handleRunnerCycle(Player hunter) {
        List<Player> runners = plugin.getRoleManager().getOnlineRunners();
        if (runners.isEmpty()) {
            MessageUtil.send(hunter, "&cNo runners online");
            return;
        }
        
        if (runners.size() == 1) {
            MessageUtil.send(hunter, "&eOnly one runner available");
            return;
        }
        
        Player currentRunner = plugin.getTrackerManager().getCurrentTrackedRunner(hunter);
        int currentIndex = runners.indexOf(currentRunner);
        int nextIndex = (currentIndex + 1) % runners.size();
        Player nextRunner = runners.get(nextIndex);
        
        plugin.getTrackerManager().setCurrentTrackedRunner(hunter, nextRunner);
        MessageUtil.send(hunter, "&aTarget switched to: &e" + nextRunner.getName());
    }
    
    @EventHandler
    public void onPortalUse(PlayerPortalEvent event) {
        Player player = event.getPlayer();
        
        if (!plugin.getGameManager().isGameActive()) {
            return;
        }
        
        if (plugin.getRoleManager().isRunner(player)) {
            plugin.getTrackerManager().updatePortalLocation(player, event.getFrom());
        }
    }
    
    private Player findClosestRunner(Player hunter, List<Player> runners) {
        Player closest = null;
        double minDistance = Double.MAX_VALUE;
        
        for (Player runner : runners) {
            if (hunter.getWorld().equals(runner.getWorld())) {
                double distance = hunter.getLocation().distance(runner.getLocation());
                if (distance < minDistance) {
                    minDistance = distance;
                    closest = runner;
                }
            }
        }
        
        return closest != null ? closest : runners.get(0);
    }
}