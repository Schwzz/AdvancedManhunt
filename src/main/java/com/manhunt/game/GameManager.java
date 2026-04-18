package com.manhunt.game;

import com.manhunt.ManhuntPlugin;
import com.manhunt.util.MessageUtil;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;

@Getter
public class GameManager {
    
    private final ManhuntPlugin plugin;
    private boolean gameActive;
    private boolean gamePaused;
    private boolean countdownActive;
    private boolean gameStarted;
    
    public GameManager(ManhuntPlugin plugin) {
        this.plugin = plugin;
        this.gameActive = false;
        this.gamePaused = false;
        this.countdownActive = false;
        this.gameStarted = false;
    }
    
    public boolean startGame() {
        if (gameActive) {
            return false;
        }
        
        if (!plugin.getRoleManager().hasEnoughPlayers()) {
            return false;
        }
        
        gameActive = true;
        gamePaused = false;
        countdownActive = true;
        gameStarted = false;
        
        teleportAllToSpawn();
        freezeAllPlayers();
        
        startCountdown();
        
        return true;
    }
    
    private void startCountdown() {
        new BukkitRunnable() {
            int count = 4;
            
            @Override
            public void run() {
                if (count > 1) {
                    String displayText = count == 4 ? "&e&lGame Starting" : "&e&l" + (count - 1);
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendTitle(MessageUtil.color(displayText), "", 5, 15, 5);
                        XSound.matchXSound("BLOCK_NOTE_BLOCK_PLING").ifPresent(sound -> sound.play(p, 1.0f, 1.0f));
                    }
                    count--;
                } else if (count == 1) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendTitle(MessageUtil.color("&a&lGO!!!"), "", 5, 15, 5);
                        XSound.matchXSound("BLOCK_NOTE_BLOCK_PLING").ifPresent(sound -> sound.play(p, 1.0f, 2.0f));
                    }
                    
                    unfreezeRunners();
                    String freezeDelay = plugin.getConfigManager().getHunterFreezeDelay().toLowerCase();
                    
                    if ("none".equals(freezeDelay)) {
                        unfreezeHunters();
                        startGameImmediately();
                    } else if ("5".equals(freezeDelay)) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                unfreezeHunters();
                                startGameImmediately();
                            }
                        }.runTaskLater(plugin, 100L);
                    } else if ("10".equals(freezeDelay)) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                unfreezeHunters();
                                startGameImmediately();
                            }
                        }.runTaskLater(plugin, 200L);
                    }
                    
                    if (plugin.getConfigManager().isAutoGiveCompass()) {
                        giveCompassesToHunters();
                    }
                    
                    count--;
                } else {
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
    
    private void startGameImmediately() {
        gameStarted = true;
        countdownActive = false;
        plugin.getGameTimer().start();
    }
    
    public void onRunnerMove() {
        if (!countdownActive || gameStarted) {
            return;
        }
        
        gameStarted = true;
        countdownActive = false;
        
        for (Player hunter : plugin.getRoleManager().getOnlineHunters()) {
            hunter.sendTitle(MessageUtil.color("&a&lRunners are now Running!"), "", 5, 40, 10);
        }
        
        unfreezeHunters();
        plugin.getGameTimer().start();
    }
    
    public void restartGame() {
        stopGame();
        
        new BukkitRunnable() {
            @Override
            public void run() {
                startGame();
            }
        }.runTaskLater(plugin, 40L);
    }
    
    public void stopGame() {
        if (!gameActive) {
            return;
        }
        
        gameActive = false;
        gamePaused = false;
        countdownActive = false;
        gameStarted = false;
        
        plugin.getTrackerManager().clearCooldowns();
        plugin.getFreezeManager().unfreezeAll();
        plugin.getGameTimer().reset();
        plugin.getReconnectManager().clear();
        
        removeAllCompasses();
        teleportAllToSpawn();
        
        if (plugin.getConfigManager().isAnnounceEnd()) {
            MessageUtil.broadcast(plugin, plugin.getConfigManager().getMessage("game-stopped"));
        }
    }
    
    public void pauseGame() {
        if (!gameActive || gamePaused) {
            return;
        }
        
        gamePaused = true;
        MessageUtil.broadcast(plugin, plugin.getConfigManager().getMessage("game-paused"));
    }
    
    public void resumeGame() {
        if (!gameActive || !gamePaused) {
            return;
        }
        
        gamePaused = false;
        MessageUtil.broadcast(plugin, plugin.getConfigManager().getMessage("game-resumed"));
    }
    
    public void handleRunnerDeath(Player runner) {
        if (!gameActive || gamePaused) {
            return;
        }
        
        plugin.getGameTimer().stop();
        String timeSurvived = plugin.getGameTimer().getFormattedTime();
        MessageUtil.broadcast(plugin, "&c" + runner.getName() + " &7was eliminated! &eTime Survived: &f" + timeSurvived);
        plugin.getGameTimer().start();
        
        int remainingRunners = plugin.getRoleManager().getOnlineRunners().size() - 1;
        
        if (remainingRunners > 0) {
            runner.setGameMode(GameMode.SPECTATOR);
            MessageUtil.send(plugin, runner, "&7You are now spectating the game.");
        } else {
            plugin.getRoleManager().getRunners().remove(runner.getUniqueId());
        }
        
        if (plugin.getRoleManager().getOnlineRunners().isEmpty() || 
            (plugin.getRoleManager().getOnlineRunners().size() == 1 && 
             plugin.getRoleManager().getOnlineRunners().get(0).getGameMode() == GameMode.SPECTATOR)) {
            announceHunterWin();
            stopGame();
        }
    }
    
    public void handleDragonDeath(Player killer) {
        if (!gameActive || gamePaused) {
            return;
        }
        
        if (killer != null && plugin.getRoleManager().isHunter(killer)) {
            MessageUtil.broadcast(plugin, "&c&lHUNTER KILLED THE DRAGON!");
            MessageUtil.broadcast(plugin, "&a&lRUNNERS WIN! &7Hunters broke the rules!");
            MessageUtil.playSound(plugin, plugin.getConfigManager().getSound("game-end"));
            stopGame();
            return;
        }
        
        plugin.getGameTimer().stop();
        String ttkd = plugin.getGameTimer().getFormattedTime();
        
        announceRunnerWin(ttkd);
        stopGame();
    }
    
    private void announceRunnerWin(String ttkd) {
        String message = plugin.getConfigManager().getMessage("runner-win");
        MessageUtil.broadcast(plugin, message);
        MessageUtil.broadcast(plugin, "&e&lTime To Kill Dragon: &f" + ttkd);
        MessageUtil.playSound(plugin, plugin.getConfigManager().getSound("game-end"));
    }
    
    private void announceHunterWin() {
        String message = plugin.getConfigManager().getMessage("hunter-win");
        MessageUtil.broadcast(plugin, message);
        MessageUtil.playSound(plugin, plugin.getConfigManager().getSound("game-end"));
    }
    
    private void giveCompassesToHunters() {
        ItemStack compass = XMaterial.matchXMaterial("COMPASS").map(XMaterial::parseItem).orElse(new ItemStack(XMaterial.COMPASS.parseMaterial()));
        ItemMeta meta = compass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(MessageUtil.color("&6Tracker Compass"));
            meta.setLore(Arrays.asList(
                MessageUtil.color("&7Right-click to track runners"),
                MessageUtil.color("&7Shift + Right-click to cycle runners"),
                MessageUtil.color("&7Cooldown: &e" + plugin.getConfigManager().getTrackerCooldown() + "s")
            ));
            compass.setItemMeta(meta);
        }
        
        for (Player hunter : plugin.getRoleManager().getOnlineHunters()) {
            hunter.getInventory().addItem(compass);
        }
    }
    
    private void removeAllCompasses() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.getInventory().remove(XMaterial.COMPASS.parseMaterial());
        }
    }
    
    private void teleportAllToSpawn() {
        Location spawn = Bukkit.getWorlds().get(0).getSpawnLocation();
        List<Player> runners = plugin.getRoleManager().getOnlineRunners();
        List<Player> hunters = plugin.getRoleManager().getOnlineHunters();
        
        for (Player runner : runners) {
            runner.teleport(spawn);
            if (runner.getGameMode() == GameMode.SPECTATOR) {
                runner.setGameMode(GameMode.SURVIVAL);
            }
        }
        
        int hunterCount = hunters.size();
        if (hunterCount > 0) {
            double radius = 3.0;
            double angleStep = (2 * Math.PI) / hunterCount;
            
            for (int i = 0; i < hunterCount; i++) {
                Player hunter = hunters.get(i);
                double angle = i * angleStep;
                double x = spawn.getX() + radius * Math.cos(angle);
                double z = spawn.getZ() + radius * Math.sin(angle);
                
                Location hunterLocation = new Location(spawn.getWorld(), x, spawn.getY(), z, spawn.getYaw(), spawn.getPitch());
                hunter.teleport(hunterLocation);
                
                if (hunter.getGameMode() == GameMode.SPECTATOR) {
                    hunter.setGameMode(GameMode.SURVIVAL);
                }
            }
        }
    }
    
    private void freezeAllPlayers() {
        for (Player hunter : plugin.getRoleManager().getOnlineHunters()) {
            plugin.getFreezeManager().freezePlayer(hunter);
        }
        
        for (Player runner : plugin.getRoleManager().getOnlineRunners()) {
            plugin.getFreezeManager().freezePlayer(runner);
        }
    }
    
    private void unfreezeRunners() {
        for (Player runner : plugin.getRoleManager().getOnlineRunners()) {
            plugin.getFreezeManager().unfreezePlayer(runner);
        }
    }
    
    private void unfreezeHunters() {
        for (Player hunter : plugin.getRoleManager().getOnlineHunters()) {
            plugin.getFreezeManager().unfreezePlayer(hunter);
        }
    }
}