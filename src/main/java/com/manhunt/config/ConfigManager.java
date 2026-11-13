package com.manhunt.config;

import com.manhunt.ManhuntPlugin;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

@Getter
public class ConfigManager {
    
    private final ManhuntPlugin plugin;
    private final FileConfiguration config;
    
    private final String runnerAlertMessage;
    private final String cooldownMessage;
    private final String trackingMessage;
    private final boolean autoGiveCompass;
    private final boolean announceStart;
    private final boolean announceEnd;
    private final boolean freezeOnEnd;
    private final String prefix;
    
    public ConfigManager(ManhuntPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        
        this.runnerAlertMessage = config.getString("tracker.runner-alert-message", "&c&lTRACKED");
        this.cooldownMessage = config.getString("tracker.cooldown-message", "&cTracker recharging...");
        this.trackingMessage = config.getString("tracker.tracking-message", "&aTracking...");
        this.autoGiveCompass = config.getBoolean("game.auto-give-compass", true);
        this.announceStart = config.getBoolean("game.announce-start", true);
        this.announceEnd = config.getBoolean("game.announce-end", true);
        this.freezeOnEnd = config.getBoolean("game.freeze-on-end", true);
        this.prefix = config.getString("messages.prefix", "&8[&6Manhunt&8] &7");
    }
    
    public String getMessage(String path) {
        return config.getString("messages." + path, "");
    }

    public int getTrackerCooldown() {
        return config.getInt("tracker.cooldown-seconds", 7);
    }

    public int getTrackerDuration() {
        return config.getInt("tracker.duration-seconds", 0);
    }

    public boolean isAlertRunner() {
        return config.getBoolean("tracker.alert-runner", true);
    }

    public boolean isHunterCanDamageDragon() {
        return config.getBoolean("game.hunter-can-damage-dragon", false);
    }
    
    public String getSound(String path) {
        return config.getString("sounds." + path, "");
    }
    
    public int getRunnerGracePeriodSeconds() {
        return Math.max(0, config.getInt("game.runner-grace-period-seconds", 5));
    }

    public int getHunterLives() {
        return Math.max(1, config.getInt("game.hunter-lives", 3));
    }

    public int getRunnerLives() {
        return Math.max(1, config.getInt("game.runner-lives", 3));
    }
}