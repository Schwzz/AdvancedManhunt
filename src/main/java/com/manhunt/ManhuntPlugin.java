package com.manhunt;

import com.manhunt.command.ManhuntCommand;
import com.manhunt.command.TeamChatCommand;
import com.manhunt.config.ConfigManager;
import com.manhunt.freeze.FreezeManager;
import com.manhunt.game.GameManager;
import com.manhunt.inventory.gui.GUIListener;
import com.manhunt.inventory.gui.GUIManager;
import com.manhunt.listener.CompassListener;
import com.manhunt.listener.DragonDeathListener;
import com.manhunt.listener.EntityDamageListener;
import com.manhunt.listener.PlayerDeathListener;
import com.manhunt.listener.PlayerJoinListener;
import com.manhunt.listener.PlayerMoveListener;
import com.manhunt.listener.PlayerQuitListener;
import com.manhunt.reconnect.ReconnectManager;
import com.manhunt.role.RoleManager;
import com.manhunt.timer.GameTimer;
import com.manhunt.tracker.TrackerManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class ManhuntPlugin extends JavaPlugin {
    
    private ConfigManager configManager;
    private GameManager gameManager;
    private RoleManager roleManager;
    private TrackerManager trackerManager;
    private FreezeManager freezeManager;
    private GameTimer gameTimer;
    private GUIManager guiManager;
    private ReconnectManager reconnectManager;
    
    @Override
    public void onEnable() {
        saveDefaultConfig();
        
        this.configManager = new ConfigManager(this);
        this.roleManager = new RoleManager();
        this.trackerManager = new TrackerManager(this);
        this.freezeManager = new FreezeManager();
        this.gameTimer = new GameTimer();
        this.guiManager = new GUIManager();
        this.reconnectManager = new ReconnectManager();
        this.gameManager = new GameManager(this);
        
        getCommand("manhunt").setExecutor(new ManhuntCommand(this));
        getCommand("msgh").setExecutor(new TeamChatCommand(this, true));
        getCommand("msgr").setExecutor(new TeamChatCommand(this, false));
        
        Bukkit.getPluginManager().registerEvents(new CompassListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        Bukkit.getPluginManager().registerEvents(new DragonDeathListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerMoveListener(this), this);
        Bukkit.getPluginManager().registerEvents(new EntityDamageListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        Bukkit.getPluginManager().registerEvents(new GUIListener(guiManager), this);
        
        getLogger().info("ManhuntPlugin enabled!");
    }
    
    @Override
    public void onDisable() {
        if (gameManager.isGameActive()) {
            gameManager.stopGame();
        }
        getLogger().info("ManhuntPlugin disabled!");
    }
}