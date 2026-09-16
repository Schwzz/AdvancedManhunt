package com.manhunt.listener;

import com.manhunt.ManhuntPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerRespawnListener implements Listener {

    private final ManhuntPlugin plugin;

    public PlayerRespawnListener(ManhuntPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        plugin.getGameManager().handlePlayerRespawn(event.getPlayer());
    }
}