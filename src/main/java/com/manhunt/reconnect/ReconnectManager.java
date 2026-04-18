package com.manhunt.reconnect;

import lombok.Getter;
import org.bukkit.GameMode;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public class ReconnectManager {
    private final Map<UUID, ReconnectData> disconnectedPlayers = new HashMap<>();
    
    public void registerDisconnect(UUID playerId, String role, GameMode gameMode) {
        disconnectedPlayers.put(playerId, new ReconnectData(role, gameMode, System.currentTimeMillis()));
    }
    
    public ReconnectData getReconnectData(UUID playerId) {
        return disconnectedPlayers.get(playerId);
    }
    
    public void removeReconnectData(UUID playerId) {
        disconnectedPlayers.remove(playerId);
    }
    
    public boolean hasReconnectData(UUID playerId) {
        return disconnectedPlayers.containsKey(playerId);
    }
    
    public void clear() {
        disconnectedPlayers.clear();
    }
    
    @Getter
    public static class ReconnectData {
        private final String role;
        private final GameMode gameMode;
        private final long disconnectTime;
        
        public ReconnectData(String role, GameMode gameMode, long disconnectTime) {
            this.role = role;
            this.gameMode = gameMode;
            this.disconnectTime = disconnectTime;
        }
    }
}