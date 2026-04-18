package com.manhunt.tracker;

import com.manhunt.ManhuntPlugin;
import com.manhunt.util.MessageUtil;
import com.cryptomorin.xseries.XSound;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public class TrackerManager {
    
    private final ManhuntPlugin plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<UUID, Location> lastPortalLocations = new HashMap<>();
    private final Map<UUID, Player> currentTrackedRunner = new HashMap<>();
    private final Map<UUID, BukkitRunnable> activeTrackingTasks = new HashMap<>();
    
    public TrackerManager(ManhuntPlugin plugin) {
        this.plugin = plugin;
    }
    
    public boolean isOnCooldown(Player player) {
        if (!cooldowns.containsKey(player.getUniqueId())) {
            return false;
        }
        
        long lastUse = cooldowns.get(player.getUniqueId());
        long cooldownMs = plugin.getConfigManager().getTrackerCooldown() * 1000L;
        return System.currentTimeMillis() - lastUse < cooldownMs;
    }
    
    public long getRemainingCooldown(Player player) {
        if (!cooldowns.containsKey(player.getUniqueId())) {
            return 0;
        }
        
        long lastUse = cooldowns.get(player.getUniqueId());
        long cooldownMs = plugin.getConfigManager().getTrackerCooldown() * 1000L;
        long elapsed = System.currentTimeMillis() - lastUse;
        return Math.max(0, (cooldownMs - elapsed) / 1000);
    }
    
    public void setCooldown(Player player) {
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }
    
    public void clearCooldowns() {
        cooldowns.clear();
        lastPortalLocations.clear();
        currentTrackedRunner.clear();
        
        for (BukkitRunnable task : activeTrackingTasks.values()) {
            task.cancel();
        }
        activeTrackingTasks.clear();
    }
    
    public void updatePortalLocation(Player runner, Location location) {
        lastPortalLocations.put(runner.getUniqueId(), location.clone());
    }
    
    public Location getLastPortalLocation(Player runner) {
        return lastPortalLocations.get(runner.getUniqueId());
    }
    
    public Player getCurrentTrackedRunner(Player hunter) {
        return currentTrackedRunner.get(hunter.getUniqueId());
    }
    
    public void setCurrentTrackedRunner(Player hunter, Player runner) {
        currentTrackedRunner.put(hunter.getUniqueId(), runner);
    }
    
    public void trackRunner(Player hunter, Player runner) {
        Location targetLocation;
        boolean differentDimension = false;
        
        if (!hunter.getWorld().equals(runner.getWorld())) {
            differentDimension = true;
            Location portalLoc = getLastPortalLocation(runner);
            if (portalLoc != null && portalLoc.getWorld().equals(hunter.getWorld())) {
                targetLocation = portalLoc;
            } else {
                targetLocation = runner.getLocation();
            }
        } else {
            targetLocation = runner.getLocation();
        }
        
        hunter.setCompassTarget(targetLocation);
        
        String message = plugin.getConfigManager().getTrackingMessage()
            .replace("{runner}", runner.getName());
        MessageUtil.send(hunter, message);
        
        if (differentDimension) {
            String dimMessage = plugin.getConfigManager().getMessage("dimension-tracking")
                .replace("{dimension}", getDimensionName(runner.getWorld().getEnvironment()));
            MessageUtil.send(hunter, dimMessage);
        }
        
        XSound.matchXSound(plugin.getConfigManager().getSound("tracking"))
            .ifPresent(sound -> sound.play(hunter));
        
        if (plugin.getConfigManager().isAlertRunner()) {
            alertRunner(runner);
        }
        
        setCooldown(hunter);
        
        int duration = plugin.getConfigManager().getTrackerDuration();
        if (duration > 0) {
            startTracking(hunter, runner, duration);
        }
    }
    
    private void startTracking(Player hunter, Player runner, int durationSeconds) {
        BukkitRunnable existingTask = activeTrackingTasks.get(hunter.getUniqueId());
        if (existingTask != null) {
            existingTask.cancel();
        }
        
        final Player finalRunner = runner;
        BukkitRunnable trackingTask = new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = durationSeconds * 20;
            
            @Override
            public void run() {
                if (!plugin.getGameManager().isGameActive() || !hunter.isOnline()) {
                    cancel();
                    activeTrackingTasks.remove(hunter.getUniqueId());
                    return;
                }
                
                if (ticks >= maxTicks) {
                    cancel();
                    activeTrackingTasks.remove(hunter.getUniqueId());
                    return;
                }
                
                if (ticks % 10 == 0) {
                    Location targetLoc;
                    if (!hunter.getWorld().equals(finalRunner.getWorld())) {
                        Location portalLoc = getLastPortalLocation(finalRunner);
                        targetLoc = (portalLoc != null && portalLoc.getWorld().equals(hunter.getWorld())) 
                            ? portalLoc : finalRunner.getLocation();
                    } else {
                        targetLoc = finalRunner.getLocation();
                    }
                    hunter.setCompassTarget(targetLoc);
                }
                
                ticks++;
            }
        };
        
        trackingTask.runTaskTimer(plugin, 0L, 1L);
        activeTrackingTasks.put(hunter.getUniqueId(), trackingTask);
    }
    
    private void alertRunner(Player runner) {
        String alert = plugin.getConfigManager().getRunnerAlertMessage();
        MessageUtil.sendActionBar(runner, alert);
        
        XSound.matchXSound(plugin.getConfigManager().getSound("tracked"))
            .ifPresent(sound -> sound.play(runner));
    }
    
    private String getDimensionName(org.bukkit.World.Environment environment) {
        switch (environment) {
            case NETHER:
                return "Nether";
            case THE_END:
                return "The End";
            default:
                return "Overworld";
        }
    }
}