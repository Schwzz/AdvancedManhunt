package com.manhunt.config;

import com.manhunt.ManhuntPlugin;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

@Getter
public class ConfigManager {
    
    private final ManhuntPlugin plugin;
    private final FileConfiguration config;
    
    private final int trackerCooldown;
    private final int trackerDuration;
    private final boolean alertRunner;
    private final String runnerAlertMessage;
    private final String cooldownMessage;
    private final String trackingMessage;
    private final boolean autoGiveCompass;
    private final boolean announceStart;
    private final boolean announceEnd;
    private final boolean freezeOnEnd;
    private final String prefix;
    private final boolean hunterCanDamageDragon;
    private final String hunterFreezeDelay;
    
    public ConfigManager(ManhuntPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        
        this.trackerCooldown = config.getInt("tracker.cooldown-seconds", 7);
        this.trackerDuration = config.getInt("tracker.duration-seconds", 0);
        this.alertRunner = config.getBoolean("tracker.alert-runner", true);
        this.runnerAlertMessage = config.getString("tracker.runner-alert-message", "&c&lTRACKED");
        this.cooldownMessage = config.getString("tracker.cooldown-message", "&cTracker recharging...");
        this.trackingMessage = config.getString("tracker.tracking-message", "&aTracking...");
        this.autoGiveCompass = config.getBoolean("game.auto-give-compass", true);
        this.announceStart = config.getBoolean("game.announce-start", true);
        this.announceEnd = config.getBoolean("game.announce-end", true);
        this.freezeOnEnd = config.getBoolean("game.freeze-on-end", true);
        this.hunterCanDamageDragon = config.getBoolean("game.hunter-can-damage-dragon", false);
        this.hunterFreezeDelay = config.getString("game.hunter-freeze-delay", "wait-for-move");
        this.prefix = config.getString("messages.prefix", "&8[&6Manhunt&8] &7");
    }
    
    public String getMessage(String path) {
        return config.getString("messages." + path, "");
    }
    
    public String getSound(String path) {
        return config.getString("sounds." + path, "");
    }
    
    public String getHunterFreezeDelayDisplay() {
        switch (hunterFreezeDelay.toLowerCase()) {
            case "none":
                return "None";
            case "wait-for-move":
                return "Wait for Move";
            case "5":
                return "5 seconds";
            case "10":
                return "10 seconds";
            default:
                return "Wait for Move";
        }
    }
}