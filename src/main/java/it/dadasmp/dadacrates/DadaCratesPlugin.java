/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.GameMode
 *  org.bukkit.NamespacedKey
 *  org.bukkit.Sound
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.command.TabCompleter
 *  org.bukkit.entity.Player
 *  org.bukkit.event.Listener
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.plugin.java.JavaPlugin
 */
package it.dadasmp.dadacrates;

import it.dadasmp.dadacrates.AnimationRunner;
import it.dadasmp.dadacrates.Crate;
import it.dadasmp.dadacrates.CrateCommand;
import it.dadasmp.dadacrates.CrateListener;
import it.dadasmp.dadacrates.CrateStore;
import it.dadasmp.dadacrates.GuiManager;
import it.dadasmp.dadacrates.HologramManager;
import it.dadasmp.dadacrates.IdleParticleManager;
import it.dadasmp.dadacrates.KeyFactory;
import it.dadasmp.dadacrates.Reward;
import it.dadasmp.dadacrates.RewardType;
import it.dadasmp.dadacrates.Text;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class DadaCratesPlugin
extends JavaPlugin {
    private CrateStore store;
    private NamespacedKey cosmeticFireworkKey;
    private KeyFactory keyFactory;
    private AnimationRunner animationRunner;
    private GuiManager guiManager;
    private HologramManager hologramManager;
    private IdleParticleManager idleParticleManager;
    private CrateCommand command;

    public void onEnable() {
        this.saveDefaultConfig();
        this.saveResource("locations.yml", false);
        this.saveResource("holograms.yml", false);
        this.cosmeticFireworkKey = new NamespacedKey((Plugin)this, "cosmetic_firework");
        this.store = new CrateStore(this);
        this.keyFactory = new KeyFactory(this);
        this.animationRunner = new AnimationRunner(this);
        this.hologramManager = new HologramManager(this);
        this.idleParticleManager = new IdleParticleManager(this);
        this.guiManager = new GuiManager(this);
        this.command = new CrateCommand(this);
        this.store.reload();
        this.hologramManager.startKeepAlive();
        this.idleParticleManager.start();
        this.getServer().getPluginManager().registerEvents((Listener)new CrateListener(this), (Plugin)this);
        this.getServer().getPluginManager().registerEvents((Listener)this.guiManager, (Plugin)this);
        this.getServer().getPluginCommand("dadacrates").setExecutor((CommandExecutor)this.command);
        this.getServer().getPluginCommand("dadacrates").setTabCompleter((TabCompleter)this.command);
    }

    public void onDisable() {
        if (this.hologramManager != null) {
            this.hologramManager.stopKeepAlive();
            this.hologramManager.clear();
        }
        if (this.idleParticleManager != null) {
            this.idleParticleManager.stop();
        }
    }

    CrateStore store() {
        return this.store;
    }

    NamespacedKey cosmeticFireworkKey() {
        return this.cosmeticFireworkKey;
    }

    KeyFactory keys() {
        return this.keyFactory;
    }

    AnimationRunner animations() {
        return this.animationRunner;
    }

    GuiManager gui() {
        return this.guiManager;
    }

    HologramManager holograms() {
        return this.hologramManager;
    }

    IdleParticleManager idleParticles() {
        return this.idleParticleManager;
    }

    CrateCommand command() {
        return this.command;
    }

    void award(Player player, Crate crate, Reward reward) {
        if (reward.type() == RewardType.ITEM && player.getGameMode() != GameMode.SPECTATOR) {
            player.getInventory().addItem(new ItemStack[]{reward.itemReward()});
        } else {
            for (String command : reward.commands()) {
                Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), (String)command.replace("%player%", player.getName()));
            }
        }
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
        player.sendMessage(this.store().prefix() + this.store().msg("win").replace("%reward%", Text.plain(reward.name())));
        if (reward.broadcast()) {
            Bukkit.broadcastMessage((String)(this.store().prefix() + Text.color(reward.broadcastMessage()).replace("%player%", player.getName()).replace("%reward%", Text.plain(reward.name())).replace("%crate%", Text.plain(crate.displayName))));
        }
    }
}

