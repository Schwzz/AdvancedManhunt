package com.manhunt.inventory.impl;

import com.manhunt.ManhuntPlugin;
import com.manhunt.inventory.InventoryButton;
import com.manhunt.inventory.InventoryGUI;
import com.manhunt.util.MessageUtil;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class SetupGUI extends InventoryGUI {
    private final ManhuntPlugin plugin;
    
    public SetupGUI(ManhuntPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    protected Inventory createInventory() {
        return Bukkit.createInventory(null, 45, MessageUtil.color("&6&lManhunt Setup"));
    }
    
    @Override
    public void decorate(Player player) {
        this.addButton(11, new InventoryButton()
            .creator(p -> createTrackerCooldownItem())
            .consumer(event -> {
                Player clicker = (Player) event.getWhoClicked();
                cycleTrackerCooldown();
                MessageUtil.send(plugin, clicker, "&aTracker cooldown set to &e" + plugin.getConfig().getInt("tracker.cooldown-seconds") + "s");
                XSound.matchXSound("UI_BUTTON_CLICK").ifPresent(sound -> sound.play(clicker));
                decorate(clicker);
            })
        );
        
        this.addButton(13, new InventoryButton()
            .creator(p -> createTrackerDurationItem())
            .consumer(event -> {
                Player clicker = (Player) event.getWhoClicked();
                cycleTrackerDuration();
                int duration = plugin.getConfig().getInt("tracker.duration-seconds");
                String durationText = duration == 0 ? "Disabled" : duration + "s";
                MessageUtil.send(plugin, clicker, "&aTracker duration set to &e" + durationText);
                XSound.matchXSound("UI_BUTTON_CLICK").ifPresent(sound -> sound.play(clicker));
                decorate(clicker);
            })
        );
        
        this.addButton(15, new InventoryButton()
            .creator(p -> createAlertRunnerItem())
            .consumer(event -> {
                Player clicker = (Player) event.getWhoClicked();
                toggleAlertRunner();
                boolean enabled = plugin.getConfig().getBoolean("tracker.alert-runner");
                MessageUtil.send(plugin, clicker, "&aAlert runner " + (enabled ? "&aenabled" : "&cdisabled"));
                XSound.matchXSound("UI_BUTTON_CLICK").ifPresent(sound -> sound.play(clicker));
                decorate(clicker);
            })
        );
        
        this.addButton(29, new InventoryButton()
            .creator(p -> createHunterDragonDamageItem())
            .consumer(event -> {
                Player clicker = (Player) event.getWhoClicked();
                toggleHunterDragonDamage();
                boolean enabled = plugin.getConfig().getBoolean("game.hunter-can-damage-dragon");
                MessageUtil.send(plugin, clicker, "&aHunter dragon damage " + (enabled ? "&aenabled" : "&cdisabled"));
                XSound.matchXSound("UI_BUTTON_CLICK").ifPresent(sound -> sound.play(clicker));
                decorate(clicker);
            })
        );
        
        this.addButton(33, new InventoryButton()
            .creator(p -> createHunterFreezeDelayItem())
            .consumer(event -> {
                Player clicker = (Player) event.getWhoClicked();
                cycleHunterFreezeDelay();
                String delay = plugin.getConfigManager().getHunterFreezeDelayDisplay();
                MessageUtil.send(plugin, clicker, "&aHunter freeze delay set to &e" + delay);
                XSound.matchXSound("UI_BUTTON_CLICK").ifPresent(sound -> sound.play(clicker));
                decorate(clicker);
            })
        );
        
        super.decorate(player);
    }
    
    private ItemStack createTrackerCooldownItem() {
        ItemStack item = XMaterial.matchXMaterial("COMPASS").map(XMaterial::parseItem).orElse(new ItemStack(XMaterial.COMPASS.parseMaterial()));
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(MessageUtil.color("&6Tracker Cooldown"));
            int cooldown = plugin.getConfig().getInt("tracker.cooldown-seconds", 7);
            meta.setLore(Arrays.asList(
                MessageUtil.color("&7Current: &e" + cooldown + " seconds"),
                MessageUtil.color(""),
                MessageUtil.color("&7Click to cycle:"),
                MessageUtil.color("&e1s &7→ &e3s &7→ &e5s &7→ &e7s &7→ &e10s")
            ));
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private ItemStack createTrackerDurationItem() {
        ItemStack item = XMaterial.matchXMaterial("CLOCK").map(XMaterial::parseItem).orElse(new ItemStack(XMaterial.CLOCK.parseMaterial()));
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(MessageUtil.color("&6Tracker Duration"));
            int duration = plugin.getConfig().getInt("tracker.duration-seconds", 0);
            String durationText = duration == 0 ? "Disabled" : duration + " seconds";
            meta.setLore(Arrays.asList(
                MessageUtil.color("&7Current: &e" + durationText),
                MessageUtil.color(""),
                MessageUtil.color("&7Click to cycle:"),
                MessageUtil.color("&eOff &7→ &e1s &7→ &e3s &7→ &e5s")
            ));
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private ItemStack createAlertRunnerItem() {
        boolean enabled = plugin.getConfig().getBoolean("tracker.alert-runner", true);
        ItemStack item = XMaterial.matchXMaterial(enabled ? "LIME_DYE" : "GRAY_DYE").map(XMaterial::parseItem).orElse(new ItemStack(XMaterial.LIME_DYE.parseMaterial()));
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(MessageUtil.color("&6Alert Runner When Tracked"));
            meta.setLore(Arrays.asList(
                MessageUtil.color("&7Status: " + (enabled ? "&aEnabled" : "&cDisabled")),
                MessageUtil.color(""),
                MessageUtil.color("&7Click to toggle")
            ));
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private ItemStack createHunterDragonDamageItem() {
        boolean enabled = plugin.getConfig().getBoolean("game.hunter-can-damage-dragon", false);
        ItemStack item = XMaterial.matchXMaterial(enabled ? "DIAMOND_SWORD" : "WOODEN_SWORD").map(XMaterial::parseItem).orElse(new ItemStack(XMaterial.DIAMOND_SWORD.parseMaterial()));
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(MessageUtil.color("&6Hunter Dragon Damage"));
            meta.setLore(Arrays.asList(
                MessageUtil.color("&7Status: " + (enabled ? "&aEnabled" : "&cDisabled")),
                MessageUtil.color(""),
                MessageUtil.color("&7Determines whether Hunters"),
                MessageUtil.color("&7can hurt the Ender Dragon"),
                MessageUtil.color(""),
                MessageUtil.color("&7Click to toggle")
            ));
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private ItemStack createHunterFreezeDelayItem() {
        String delay = plugin.getConfig().getString("game.hunter-freeze-delay", "wait-for-move");
        ItemStack item = XMaterial.matchXMaterial("ICE").map(XMaterial::parseItem).orElse(new ItemStack(XMaterial.ICE.parseMaterial()));
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(MessageUtil.color("&6Hunter Freeze Delay"));
            String displayText;
            switch (delay.toLowerCase()) {
                case "none":
                    displayText = "None";
                    break;
                case "5":
                    displayText = "5 seconds";
                    break;
                case "10":
                    displayText = "10 seconds";
                    break;
                default:
                    displayText = "Wait for Move";
                    break;
            }
            meta.setLore(Arrays.asList(
                MessageUtil.color("&7Current: &e" + displayText),
                MessageUtil.color(""),
                MessageUtil.color("&7Determines when Hunters"),
                MessageUtil.color("&7are released after start"),
                MessageUtil.color(""),
                MessageUtil.color("&7Click to cycle:"),
                MessageUtil.color("&eNone &7→ &eWait for Move &7→ &e5s &7→ &e10s")
            ));
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private void cycleTrackerCooldown() {
        int current = plugin.getConfig().getInt("tracker.cooldown-seconds", 7);
        int next;
        switch (current) {
            case 1:
                next = 3;
                break;
            case 3:
                next = 5;
                break;
            case 5:
                next = 7;
                break;
            case 7:
                next = 10;
                break;
            default:
                next = 1;
                break;
        }
        plugin.getConfig().set("tracker.cooldown-seconds", next);
        plugin.saveConfig();
    }
    
    private void cycleTrackerDuration() {
        int current = plugin.getConfig().getInt("tracker.duration-seconds", 0);
        int next;
        switch (current) {
            case 0:
                next = 1;
                break;
            case 1:
                next = 3;
                break;
            case 3:
                next = 5;
                break;
            default:
                next = 0;
                break;
        }
        plugin.getConfig().set("tracker.duration-seconds", next);
        plugin.saveConfig();
    }
    
    private void toggleAlertRunner() {
        boolean current = plugin.getConfig().getBoolean("tracker.alert-runner", true);
        plugin.getConfig().set("tracker.alert-runner", !current);
        plugin.saveConfig();
    }
    
    private void toggleHunterDragonDamage() {
        boolean current = plugin.getConfig().getBoolean("game.hunter-can-damage-dragon", false);
        plugin.getConfig().set("game.hunter-can-damage-dragon", !current);
        plugin.saveConfig();
    }
    
    private void cycleHunterFreezeDelay() {
        String current = plugin.getConfig().getString("game.hunter-freeze-delay", "wait-for-move").toLowerCase();
        String next;
        switch (current) {
            case "none":
                next = "wait-for-move";
                break;
            case "wait-for-move":
                next = "5";
                break;
            case "5":
                next = "10";
                break;
            default:
                next = "none";
                break;
        }
        plugin.getConfig().set("game.hunter-freeze-delay", next);
        plugin.saveConfig();
    }
}