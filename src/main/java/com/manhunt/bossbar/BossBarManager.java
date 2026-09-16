package com.manhunt.bossbar;

import com.manhunt.ManhuntPlugin;
import com.manhunt.util.MessageUtil;
import lombok.Getter;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class BossBarManager {

    private final ManhuntPlugin plugin;
    @Getter
    private BossBar bossBar;
    private BukkitRunnable updateTask;

    public BossBarManager(ManhuntPlugin plugin) {
        this.plugin = plugin;
    }

    public void show() {
        remove();
        bossBar = plugin.getServer().createBossBar("", BarColor.YELLOW, BarStyle.SOLID);
        update();
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!plugin.getGameManager().isGameActive()) {
                    cancel();
                    return;
                }
                update();
            }
        };
        updateTask.runTaskTimer(plugin, 20L, 20L);
    }

    public void remove() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
        if (bossBar != null) {
            bossBar.removeAll();
            bossBar = null;
        }
    }

    private void update() {
        if (bossBar == null) {
            return;
        }

        for (Player player : plugin.getRoleManager().getOnlineHunters()) {
            bossBar.addPlayer(player);
        }
        for (Player player : plugin.getRoleManager().getOnlineRunners()) {
            bossBar.addPlayer(player);
        }

        int hunters = plugin.getRoleManager().getOnlineHunters().size();
        int runners = plugin.getRoleManager().getOnlineRunners().size();
        String title;
        BarColor color;

        if (plugin.getGameManager().isGamePaused()) {
            title = "&e&lMANHUNT PAUSED &7• &fTime: &e" + plugin.getGameTimer().getFormattedTime();
            color = BarColor.YELLOW;
        } else if (plugin.getGameManager().isCountdownActive()) {
            title = "&6&lMANHUNT STARTING &7• &fHunters: &c" + hunters + " &7| Runners: &a" + runners;
            color = BarColor.BLUE;
        } else {
            title = "&6&lMANHUNT &7• &fTime: &e" + plugin.getGameTimer().getFormattedTime()
                + " &7| Hunters: &c" + hunters + " &7| Runners: &a" + runners;
            color = BarColor.RED;
        }

        bossBar.setTitle(MessageUtil.color(title));
        bossBar.setColor(color);
        bossBar.setProgress(1.0D);
    }
}