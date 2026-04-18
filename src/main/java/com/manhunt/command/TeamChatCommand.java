package com.manhunt.command;

import com.manhunt.ManhuntPlugin;
import com.manhunt.util.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeamChatCommand implements CommandExecutor {
    
    private final ManhuntPlugin plugin;
    private final boolean isHunterChat;
    
    public TeamChatCommand(ManhuntPlugin plugin, boolean isHunterChat) {
        this.plugin = plugin;
        this.isHunterChat = isHunterChat;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtil.color("&cOnly players can use this command"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!plugin.getGameManager().isGameActive()) {
            MessageUtil.send(plugin, player, plugin.getConfigManager().getMessage("no-active-game"));
            return true;
        }
        
        if (isHunterChat && !plugin.getRoleManager().isHunter(player)) {
            MessageUtil.send(player, "&cYou are not a hunter!");
            return true;
        }
        
        if (!isHunterChat && !plugin.getRoleManager().isRunner(player)) {
            MessageUtil.send(player, "&cYou are not a runner!");
            return true;
        }
        
        if (args.length == 0) {
            MessageUtil.send(player, "&cUsage: /" + label + " <message>");
            return true;
        }
        
        StringBuilder message = new StringBuilder();
        for (String arg : args) {
            message.append(arg).append(" ");
        }
        
        String formattedMessage = MessageUtil.color(
            (isHunterChat ? "&c[Hunter] " : "&a[Runner] ") + 
            player.getName() + "&7: &f" + message.toString().trim()
        );
        
        if (isHunterChat) {
            for (Player hunter : plugin.getRoleManager().getOnlineHunters()) {
                hunter.sendMessage(formattedMessage);
            }
        } else {
            for (Player runner : plugin.getRoleManager().getOnlineRunners()) {
                runner.sendMessage(formattedMessage);
            }
        }
        
        return true;
    }
}