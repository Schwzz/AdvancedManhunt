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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Getter
public class GameManager {
    
    private final ManhuntPlugin plugin;
    private boolean gameActive;
    private boolean gamePaused;
    private boolean countdownActive;
    private boolean gameStarted;
    private boolean lobbyActive;
    private final Set<UUID> readyPlayers = new HashSet<>();
    private final Map<UUID, Integer> remainingLives = new HashMap<>();
    private final Set<UUID> eliminatedPlayers = new HashSet<>();
    
    public GameManager(ManhuntPlugin plugin) {
        this.plugin = plugin;
        this.gameActive = false;
        this.gamePaused = false;
        this.countdownActive = false;
        this.gameStarted = false;
        this.lobbyActive = false;
    }
    
    public boolean openLobby() {
        if (gameActive || lobbyActive) {
            return false;
        }
        
        if (!plugin.getRoleManager().hasEnoughPlayers()) {
            return false;
        }

        lobbyActive = true;
        readyPlayers.clear();
        MessageUtil.broadcast(plugin, plugin.getConfigManager().getMessage("lobby-opened"));
        return true;
    }

    public boolean toggleReady(Player player) {
        if (!lobbyActive || !plugin.getRoleManager().isInGame(player)) {
            return false;
        }

        UUID playerId = player.getUniqueId();
        if (readyPlayers.remove(playerId)) {
            MessageUtil.broadcast(plugin, plugin.getConfigManager().getMessage("player-unready")
                .replace("{player}", player.getName()));
            return true;
        }

        readyPlayers.add(playerId);
        MessageUtil.broadcast(plugin, plugin.getConfigManager().getMessage("player-ready")
            .replace("{player}", player.getName())
            .replace("{ready}", String.valueOf(readyPlayers.size()))
            .replace("{total}", String.valueOf(getParticipantCount())));

        if (allPlayersReady()) {
            beginGame();
        }
        return true;
    }

    public void clearReady(Player player) {
        readyPlayers.remove(player.getUniqueId());
    }

    public boolean isReady(Player player) {
        return readyPlayers.contains(player.getUniqueId());
    }

    public boolean allPlayersReady() {
        return plugin.getRoleManager().hasEnoughPlayers()
            && readyPlayers.containsAll(plugin.getRoleManager().getHunters())
            && readyPlayers.containsAll(plugin.getRoleManager().getRunners());
    }

    public int getParticipantCount() {
        return plugin.getRoleManager().getHunters().size() + plugin.getRoleManager().getRunners().size();
    }

    private void beginGame() {
        if (!lobbyActive || !allPlayersReady()) {
            return;
        }
        
        gameActive = true;
        gamePaused = false;
        countdownActive = true;
        gameStarted = false;
        lobbyActive = false;
        readyPlayers.clear();
        initializeLives();
        
        teleportAllToSpawn();
        freezeAllPlayers();
        plugin.getBossBarManager().show();
        
        startCountdown();
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
                    long gracePeriodTicks = plugin.getConfigManager().getRunnerGracePeriodSeconds() * 20L;
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (!gameActive || gameStarted) {
                                return;
                            }
                            unfreezeHunters();
                            startGameImmediately();
                        }
                    }.runTaskLater(plugin, gracePeriodTicks);
                    
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
        // Hunter release is controlled exclusively by the configured runner grace period.
    }
    
    public void restartGame() {
        stopGame();
        
        new BukkitRunnable() {
            @Override
            public void run() {
                openLobby();
            }
        }.runTaskLater(plugin, 40L);
    }
    
    public void stopGame() {
        if (!gameActive && !lobbyActive) {
            return;
        }
        
        gameActive = false;
        gamePaused = false;
        countdownActive = false;
        gameStarted = false;
        lobbyActive = false;
        readyPlayers.clear();
        
        plugin.getTrackerManager().clearCooldowns();
        plugin.getFreezeManager().unfreezeAll();
        plugin.getGameTimer().reset();
        plugin.getReconnectManager().clear();
        plugin.getBossBarManager().remove();
        
        removeAllCompasses();
        teleportAllToSpawn();
        remainingLives.clear();
        eliminatedPlayers.clear();
        
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
        handlePlayerDeath(runner);
    }

    public void handleHunterDeath(Player hunter) {
        handlePlayerDeath(hunter);
    }

    public void handlePlayerDeath(Player player) {
        if (!gameActive || gamePaused || isEliminated(player)) {
            return;
        }

        UUID playerId = player.getUniqueId();
        int lives = Math.max(0, remainingLives.getOrDefault(playerId, 1) - 1);
        remainingLives.put(playerId, lives);

        if (lives > 0) {
            MessageUtil.broadcast(plugin, "&e" + player.getName() + " &7has &c" + lives + " &7life" + (lives == 1 ? "" : "s") + " remaining.");
            return;
        }

        eliminatedPlayers.add(playerId);
        MessageUtil.broadcast(plugin, "&c" + player.getName() + " &7has been eliminated!");
        if (plugin.getRoleManager().isRunner(player) && allRunnersEliminated()) {
            announceHunterWin();
            stopGame();
        }
    }

    public void handlePlayerRespawn(Player player) {
        if (!gameActive || !plugin.getRoleManager().isInGame(player)) {
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!gameActive || !player.isOnline()) {
                    return;
                }
                player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
                player.setGameMode(isEliminated(player) ? GameMode.SPECTATOR : GameMode.SURVIVAL);
            }
        }.runTask(plugin);
    }

    public int getRemainingLives(Player player) {
        return remainingLives.getOrDefault(player.getUniqueId(), 0);
    }

    public boolean isEliminated(Player player) {
        return eliminatedPlayers.contains(player.getUniqueId());
    }

    private void initializeLives() {
        remainingLives.clear();
        eliminatedPlayers.clear();
        for (UUID playerId : plugin.getRoleManager().getHunters()) {
            remainingLives.put(playerId, plugin.getConfigManager().getHunterLives());
        }
        for (UUID playerId : plugin.getRoleManager().getRunners()) {
            remainingLives.put(playerId, plugin.getConfigManager().getRunnerLives());
        }
    }

    private boolean allRunnersEliminated() {
        for (UUID runnerId : plugin.getRoleManager().getRunners()) {
            if (!eliminatedPlayers.contains(runnerId)) {
                return false;
            }
        }
        return true;
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