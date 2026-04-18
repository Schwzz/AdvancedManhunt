package com.manhunt.util;

import com.manhunt.ManhuntPlugin;
import com.cryptomorin.xseries.XSound;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class MessageUtil {
    
    public static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    public static void send(Player player, String message) {
        player.sendMessage(color(message));
    }
    
    public static void send(ManhuntPlugin plugin, Player player, String message) {
        player.sendMessage(color(plugin.getConfigManager().getPrefix() + message));
    }
    
    public static void broadcast(ManhuntPlugin plugin, String message) {
        String formatted = color(plugin.getConfigManager().getPrefix() + message);
        Bukkit.broadcastMessage(formatted);
    }
    
    public static void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(color(message)));
    }
    
    public static void playSound(ManhuntPlugin plugin, String soundName) {
        XSound.matchXSound(soundName).ifPresent(sound -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                sound.play(player);
            }
        });
    }
}