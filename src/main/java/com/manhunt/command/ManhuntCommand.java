package com.manhunt.command;

import com.manhunt.ManhuntPlugin;
import com.manhunt.inventory.impl.SetupGUI;
import com.manhunt.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ManhuntCommand implements CommandExecutor, TabCompleter {

    private final ManhuntPlugin plugin;

    public ManhuntCommand(ManhuntPlugin plugin) {
        this.plugin = plugin;
        plugin.getCommand("manhunt").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(MessageUtil.color("&cYou do not have permission to use this command."));
            return true;
        }
        
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "start":
                handleStart(sender);
                break;
            case "stop":
                handleStop(sender);
                break;
            case "restart":
                handleRestart(sender);
                break;
            case "pause":
                handlePause(sender);
                break;
            case "resume":
                handleResume(sender);
                break;
            case "setup":
                handleSetup(sender);
                break;
            case "addhunter":
                if (args.length < 2) {
                    sender.sendMessage(MessageUtil.color("&cUsage: /manhunt addhunter <player>"));
                    return true;
                }
                handleAddHunter(sender, args[1]);
                break;
            case "addrunner":
                if (args.length < 2) {
                    sender.sendMessage(MessageUtil.color("&cUsage: /manhunt addrunner <player>"));
                    return true;
                }
                handleAddRunner(sender, args[1]);
                break;
            case "remove":
                if (args.length < 2) {
                    sender.sendMessage(MessageUtil.color("&cUsage: /manhunt remove <player>"));
                    return true;
                }
                handleRemove(sender, args[1]);
                break;
            case "status":
                handleStatus(sender);
                break;
            case "configure":
            case "config":
                handleConfigure(sender);
                break;
            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void handleStart(CommandSender sender) {
        if (plugin.getGameManager().isGameActive()) {
            sender.sendMessage(MessageUtil.color(plugin.getConfigManager().getMessage("already-in-game")));
            return;
        }

        if (!plugin.getRoleManager().hasEnoughPlayers()) {
            sender.sendMessage(MessageUtil.color(plugin.getConfigManager().getMessage("not-enough-players")));
            return;
        }

        plugin.getGameManager().startGame();
    }

    private void handleStop(CommandSender sender) {
        if (!plugin.getGameManager().isGameActive()) {
            sender.sendMessage(MessageUtil.color(plugin.getConfigManager().getMessage("no-active-game")));
            return;
        }

        plugin.getGameManager().stopGame();
    }

    private void handleRestart(CommandSender sender) {
        MessageUtil.broadcast(plugin, "&eRestarting manhunt game...");
        plugin.getGameManager().restartGame();
    }

    private void handlePause(CommandSender sender) {
        if (!plugin.getGameManager().isGameActive()) {
            sender.sendMessage(MessageUtil.color(plugin.getConfigManager().getMessage("no-active-game")));
            return;
        }

        plugin.getGameManager().pauseGame();
    }

    private void handleResume(CommandSender sender) {
        if (!plugin.getGameManager().isGameActive()) {
            sender.sendMessage(MessageUtil.color(plugin.getConfigManager().getMessage("no-active-game")));
            return;
        }

        plugin.getGameManager().resumeGame();
    }
    
    private void handleSetup(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtil.color("&cOnly players can use this command"));
            return;
        }
        
        Player player = (Player) sender;
        SetupGUI setupGUI = new SetupGUI(plugin);
        plugin.getGuiManager().openGUI(setupGUI, player);
    }

    private void handleAddHunter(CommandSender sender, String playerName) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(MessageUtil.color("&cPlayer not found"));
            return;
        }

        plugin.getRoleManager().addHunter(target);
        String message = plugin.getConfigManager().getMessage("player-added-hunter")
            .replace("{player}", target.getName());
        sender.sendMessage(MessageUtil.color(message));
    }

    private void handleAddRunner(CommandSender sender, String playerName) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(MessageUtil.color("&cPlayer not found"));
            return;
        }

        plugin.getRoleManager().addRunner(target);
        String message = plugin.getConfigManager().getMessage("player-added-runner")
            .replace("{player}", target.getName());
        sender.sendMessage(MessageUtil.color(message));
    }

    private void handleRemove(CommandSender sender, String playerName) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(MessageUtil.color("&cPlayer not found"));
            return;
        }

        plugin.getRoleManager().removePlayer(target);
        String message = plugin.getConfigManager().getMessage("player-removed")
            .replace("{player}", target.getName());
        sender.sendMessage(MessageUtil.color(message));
    }

    private void handleStatus(CommandSender sender) {
        List<Player> hunters = plugin.getRoleManager().getOnlineHunters();
        List<Player> runners = plugin.getRoleManager().getOnlineRunners();
        
        String hunterNames = hunters.isEmpty() ? "&7None" : "&c" + hunters.stream()
            .map(Player::getName)
            .collect(Collectors.joining("&7, &c"));
        
        String runnerNames = runners.isEmpty() ? "&7None" : "&a" + runners.stream()
            .map(Player::getName)
            .collect(Collectors.joining("&7, &a"));
        
        sender.sendMessage(MessageUtil.color("&8&m-------------------"));
        sender.sendMessage(MessageUtil.color("&6&lManhunt Status"));
        sender.sendMessage(MessageUtil.color("&7Game Active: " + (plugin.getGameManager().isGameActive() ? "&aYes" : "&cNo")));
        sender.sendMessage(MessageUtil.color("&7Game Paused: " + (plugin.getGameManager().isGamePaused() ? "&eYes" : "&aNo")));
        sender.sendMessage(MessageUtil.color("&7Hunters (&c" + hunters.size() + "&7): " + hunterNames));
        sender.sendMessage(MessageUtil.color("&7Runners (&a" + runners.size() + "&7): " + runnerNames));
        sender.sendMessage(MessageUtil.color("&8&m-------------------"));
    }

    private void handleConfigure(CommandSender sender) {
        sender.sendMessage(MessageUtil.color("&8&m-------------------"));
        sender.sendMessage(MessageUtil.color("&6&lManhunt Configuration"));
        sender.sendMessage(MessageUtil.color("&7Tracker Cooldown: &e" + plugin.getConfigManager().getTrackerCooldown() + "s"));
        sender.sendMessage(MessageUtil.color("&7Tracker Duration: &e" + plugin.getConfigManager().getTrackerDuration() + "s"));
        sender.sendMessage(MessageUtil.color("&7Alert Runner: " + (plugin.getConfigManager().isAlertRunner() ? "&aYes" : "&cNo")));
        sender.sendMessage(MessageUtil.color("&7Auto Give Compass: " + (plugin.getConfigManager().isAutoGiveCompass() ? "&aYes" : "&cNo")));
        sender.sendMessage(MessageUtil.color("&7Hunter Dragon Damage: " + (plugin.getConfigManager().isHunterCanDamageDragon() ? "&aEnabled" : "&cDisabled")));
        sender.sendMessage(MessageUtil.color("&7Hunter Freeze Delay: &e" + plugin.getConfigManager().getHunterFreezeDelayDisplay()));
        sender.sendMessage(MessageUtil.color("&7Use &e/manhunt setup &7for GUI configuration"));
        sender.sendMessage(MessageUtil.color("&8&m-------------------"));
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(MessageUtil.color("&8&m-------------------"));
        sender.sendMessage(MessageUtil.color("&6&lManhunt Commands"));
        sender.sendMessage(MessageUtil.color("&e/manhunt start &7- Start the game"));
        sender.sendMessage(MessageUtil.color("&e/manhunt stop &7- Stop the game"));
        sender.sendMessage(MessageUtil.color("&e/manhunt restart &7- Restart the game"));
        sender.sendMessage(MessageUtil.color("&e/manhunt pause &7- Pause the game"));
        sender.sendMessage(MessageUtil.color("&e/manhunt resume &7- Resume the game"));
        sender.sendMessage(MessageUtil.color("&e/manhunt setup &7- Open setup GUI"));
        sender.sendMessage(MessageUtil.color("&e/manhunt addhunter <player> &7- Add hunter"));
        sender.sendMessage(MessageUtil.color("&e/manhunt addrunner <player> &7- Add runner"));
        sender.sendMessage(MessageUtil.color("&e/manhunt remove <player> &7- Remove player"));
        sender.sendMessage(MessageUtil.color("&e/manhunt status &7- View game status"));
        sender.sendMessage(MessageUtil.color("&e/manhunt configure &7- View config"));
        sender.sendMessage(MessageUtil.color("&e/msgh <message> &7- Hunter team chat"));
        sender.sendMessage(MessageUtil.color("&e/msgr <message> &7- Runner team chat"));
        sender.sendMessage(MessageUtil.color("&8&m-------------------"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("start", "stop", "restart", "pause", "resume", "setup", "addhunter", "addrunner", "remove", "status", "configure"));
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("addhunter") || args[0].equalsIgnoreCase("addrunner") || args[0].equalsIgnoreCase("remove"))) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                completions.add(player.getName());
            }
        }

        return completions;
    }
}