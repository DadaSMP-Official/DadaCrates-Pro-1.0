/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Color
 *  org.bukkit.FireworkEffect
 *  org.bukkit.FireworkEffect$Type
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.Particle
 *  org.bukkit.Sound
 *  org.bukkit.entity.Firework
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.Inventory
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.FireworkMeta
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.persistence.PersistentDataType
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitRunnable
 */
package it.dadasmp.dadacrates;

import it.dadasmp.dadacrates.Crate;
import it.dadasmp.dadacrates.DadaCratesPlugin;
import it.dadasmp.dadacrates.Reward;
import it.dadasmp.dadacrates.Text;
import java.util.List;
import java.util.Random;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

final class AnimationRunner {
    private final DadaCratesPlugin plugin;
    private final Random random = new Random();

    AnimationRunner(DadaCratesPlugin plugin) {
        this.plugin = plugin;
    }

    void play(Player player, Location location, Crate crate, Reward reward, Runnable finish) {
        player.getWorld().spawnParticle(crate.particle, location, 35, 0.45, 0.35, 0.45, 0.02);
        switch (crate.animation) {
            case INSTANT: {
                player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.4f);
                finish.run();
                break;
            }
            case CSGO: {
                this.csgo(player, crate, reward, finish);
                break;
            }
            case RANDOM_GLASS: {
                this.randomGlass(player, crate, reward, finish);
                break;
            }
            case SLOT_MACHINE: {
                this.slotMachine(player, crate, reward, finish);
                break;
            }
            case WHEEL: {
                this.wheel(player, crate, reward, finish);
                break;
            }
            case SHRINK_REVEAL: {
                this.shrinkReveal(player, crate, reward, finish);
                break;
            }
            case PRISM: {
                this.prism(player, crate, reward, finish);
                break;
            }
            case BLACK_HOLE: {
                this.blackHole(player, crate, reward, finish);
                break;
            }
            case METEOR: {
                this.meteor(player, location, crate, reward, finish);
                break;
            }
            case COSMIC_PORTAL: {
                this.exoticSpiral(player, location, crate, reward, finish, Particle.PORTAL);
                break;
            }
            case FIREWORK: {
                this.guiRoll(player, crate, reward, finish, Math.max(10, crate.animationDuration - 8), true);
                break;
            }
            default: {
                this.guiRoll(player, crate, reward, finish, crate.animationDuration, crate.animationFirework);
            }
        }
    }

    private void guiRoll(final Player player, final Crate crate, final Reward reward, final Runnable finish, final int ticks, final boolean firework) {
        final Inventory inv = this.plugin.getServer().createInventory(null, crate.animationGuiSize, this.animationTitle(crate));
        player.openInventory(inv);
        this.plugin.gui().lock(player, inv);
        final List<Reward> rewards = crate.rewards.isEmpty() ? List.of(reward) : crate.rewards;
        new BukkitRunnable(){
            int step = 0;

            public void run() {
                for (int i = 0; i < inv.getSize(); ++i) {
                    Reward shown = (Reward)rewards.get(AnimationRunner.this.random.nextInt(rewards.size()));
                    inv.setItem(i, AnimationRunner.this.playerIcon(shown));
                }
                inv.setItem(AnimationRunner.this.center(inv), this.step >= ticks - 3 ? AnimationRunner.this.playerIcon(reward) : AnimationRunner.this.playerIcon((Reward)rewards.get(AnimationRunner.this.random.nextInt(rewards.size()))));
                AnimationRunner.this.tickSound(player, crate, 0.8f + (float)this.step * 0.03f);
                if (this.step++ >= ticks) {
                    this.cancel();
                    inv.setItem(AnimationRunner.this.center(inv), AnimationRunner.this.playerIcon(reward));
                    if (firework && crate.animationFirework) {
                        AnimationRunner.this.spawnFirework(player.getLocation());
                    }
                    AnimationRunner.this.plugin.getServer().getScheduler().runTaskLater((Plugin)AnimationRunner.this.plugin, () -> {
                        AnimationRunner.this.plugin.gui().unlock(player);
                        player.closeInventory();
                        finish.run();
                    }, (long)Math.max(1, crate.animationCloseDelay));
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, (long)Math.max(1, crate.animationSpeed));
    }

    private void csgo(final Player player, final Crate crate, final Reward reward, final Runnable finish) {
        final Inventory inv = this.plugin.getServer().createInventory(null, 27, this.animationTitle(crate));
        player.openInventory(inv);
        this.plugin.gui().lock(player, inv);
        final List<Reward> rewards = crate.rewards.isEmpty() ? List.of(reward) : crate.rewards;
        new BukkitRunnable(){
            int step = 0;

            public void run() {
                for (int slot = 9; slot <= 17; ++slot) {
                    inv.setItem(slot, slot == 13 && this.step > crate.animationDuration - 4 ? AnimationRunner.this.playerIcon(reward) : AnimationRunner.this.playerIcon((Reward)rewards.get(AnimationRunner.this.random.nextInt(rewards.size()))));
                }
                inv.setItem(4, AnimationRunner.this.glass(Material.LIME_STAINED_GLASS_PANE, "&av"));
                inv.setItem(22, AnimationRunner.this.glass(Material.LIME_STAINED_GLASS_PANE, "&a^"));
                AnimationRunner.this.tickSound(player, crate, 0.7f + (float)this.step * 0.025f);
                if (this.step++ >= crate.animationDuration) {
                    this.cancel();
                    inv.setItem(13, AnimationRunner.this.playerIcon(reward));
                    AnimationRunner.this.finishLater(player, crate, finish);
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, (long)Math.max(1, crate.animationSpeed));
    }

    private void randomGlass(final Player player, final Crate crate, final Reward reward, final Runnable finish) {
        final Inventory inv = this.plugin.getServer().createInventory(null, crate.animationGuiSize, this.animationTitle(crate));
        player.openInventory(inv);
        this.plugin.gui().lock(player, inv);
        final Material[] glass = new Material[]{Material.RED_STAINED_GLASS_PANE, Material.ORANGE_STAINED_GLASS_PANE, Material.YELLOW_STAINED_GLASS_PANE, Material.LIME_STAINED_GLASS_PANE, Material.LIGHT_BLUE_STAINED_GLASS_PANE, Material.PURPLE_STAINED_GLASS_PANE};
        new BukkitRunnable(){
            int step = 0;

            public void run() {
                for (int i = 0; i < inv.getSize(); ++i) {
                    inv.setItem(i, AnimationRunner.this.glass(glass[AnimationRunner.this.random.nextInt(glass.length)], " "));
                }
                inv.setItem(AnimationRunner.this.center(inv), this.step > crate.animationDuration - 5 ? AnimationRunner.this.playerIcon(reward) : AnimationRunner.this.glass(Material.WHITE_STAINED_GLASS_PANE, "&f?"));
                AnimationRunner.this.tickSound(player, crate, 1.0f);
                if (this.step++ >= crate.animationDuration) {
                    this.cancel();
                    inv.setItem(AnimationRunner.this.center(inv), AnimationRunner.this.playerIcon(reward));
                    AnimationRunner.this.finishLater(player, crate, finish);
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, (long)Math.max(1, crate.animationSpeed));
    }

    private void removingItems(final Player player, final Crate crate, final Reward reward, final Runnable finish) {
        final Inventory inv = this.plugin.getServer().createInventory(null, crate.animationGuiSize, this.animationTitle(crate));
        player.openInventory(inv);
        this.plugin.gui().lock(player, inv);
        List<Reward> rewards = crate.rewards.isEmpty() ? List.of(reward) : crate.rewards;
        for (int i = 0; i < inv.getSize(); ++i) {
            inv.setItem(i, this.playerIcon(rewards.get(this.random.nextInt(rewards.size()))));
        }
        new BukkitRunnable(){
            int step = 0;

            public void run() {
                int slot = AnimationRunner.this.random.nextInt(inv.getSize());
                if (slot != AnimationRunner.this.center(inv)) {
                    inv.setItem(slot, null);
                }
                inv.setItem(AnimationRunner.this.center(inv), this.step > crate.animationDuration - 6 ? AnimationRunner.this.playerIcon(reward) : inv.getItem(AnimationRunner.this.center(inv)));
                AnimationRunner.this.tickSound(player, crate, 1.1f);
                if (this.step++ >= crate.animationDuration) {
                    this.cancel();
                    for (int i = 0; i < inv.getSize(); ++i) {
                        inv.setItem(i, null);
                    }
                    inv.setItem(AnimationRunner.this.center(inv), AnimationRunner.this.playerIcon(reward));
                    AnimationRunner.this.finishLater(player, crate, finish);
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, (long)Math.max(1, crate.animationSpeed));
    }

    private void slotMachine(final Player player, final Crate crate, final Reward reward, final Runnable finish) {
        final Inventory inv = this.plugin.getServer().createInventory(null, 27, this.animationTitle(crate));
        player.openInventory(inv);
        this.plugin.gui().lock(player, inv);
        final List<Reward> rewards = crate.rewards.isEmpty() ? List.of(reward) : crate.rewards;
        new BukkitRunnable(){
            int step = 0;

            public void run() {
                for (int slot : new int[]{11, 13, 15}) {
                    inv.setItem(slot, AnimationRunner.this.playerIcon((Reward)rewards.get(AnimationRunner.this.random.nextInt(rewards.size()))));
                }
                if (this.step > crate.animationDuration - 6) {
                    inv.setItem(11, AnimationRunner.this.playerIcon(reward));
                    inv.setItem(13, AnimationRunner.this.playerIcon(reward));
                    inv.setItem(15, AnimationRunner.this.playerIcon(reward));
                }
                AnimationRunner.this.tickSound(player, crate, 0.9f + (float)this.step * 0.02f);
                if (this.step++ >= crate.animationDuration) {
                    this.cancel();
                    AnimationRunner.this.finishLater(player, crate, finish);
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, (long)Math.max(1, crate.animationSpeed));
    }

    private void wheel(final Player player, final Crate crate, final Reward reward, final Runnable finish) {
        final Inventory inv = this.plugin.getServer().createInventory(null, 45, this.animationTitle(crate));
        player.openInventory(inv);
        this.plugin.gui().lock(player, inv);
        final int[] ring = new int[]{10, 11, 12, 13, 14, 15, 16, 25, 34, 33, 32, 31, 30, 29, 28, 19};
        final List<Reward> rewards = crate.rewards.isEmpty() ? List.of(reward) : crate.rewards;
        new BukkitRunnable(){
            int step = 0;

            public void run() {
                for (int i = 0; i < ring.length; ++i) {
                    inv.setItem(ring[i], AnimationRunner.this.playerIcon((Reward)rewards.get(AnimationRunner.this.random.nextInt(rewards.size()))));
                }
                inv.setItem(ring[this.step % ring.length], AnimationRunner.this.glass(Material.LIME_STAINED_GLASS_PANE, "&a*"));
                inv.setItem(22, this.step > crate.animationDuration - 4 ? AnimationRunner.this.playerIcon(reward) : AnimationRunner.this.glass(Material.WHITE_STAINED_GLASS_PANE, "&f?"));
                AnimationRunner.this.tickSound(player, crate, 0.8f);
                if (this.step++ >= crate.animationDuration) {
                    this.cancel();
                    inv.setItem(22, AnimationRunner.this.playerIcon(reward));
                    AnimationRunner.this.finishLater(player, crate, finish);
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, (long)Math.max(1, crate.animationSpeed));
    }

    private void mysteryBox(final Player player, final Crate crate, final Reward reward, final Runnable finish) {
        final Inventory inv = this.plugin.getServer().createInventory(null, 27, this.animationTitle(crate));
        player.openInventory(inv);
        this.plugin.gui().lock(player, inv);
        new BukkitRunnable(){
            int step = 0;

            public void run() {
                Material material = this.step % 2 == 0 ? Material.CHEST : Material.ENDER_CHEST;
                inv.setItem(13, AnimationRunner.this.glass(material, this.step > crate.animationDuration - 4 ? "&a!" : "&e?"));
                AnimationRunner.this.tickSound(player, crate, 0.7f + (float)this.step * 0.03f);
                if (this.step++ >= crate.animationDuration) {
                    this.cancel();
                    inv.setItem(13, AnimationRunner.this.playerIcon(reward));
                    AnimationRunner.this.finishLater(player, crate, finish);
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, (long)Math.max(1, crate.animationSpeed));
    }

    private void shrinkReveal(final Player player, final Crate crate, final Reward reward, final Runnable finish) {
        final Inventory inv = this.plugin.getServer().createInventory(null, 45, this.animationTitle(crate));
        player.openInventory(inv);
        this.plugin.gui().lock(player, inv);
        final int[][] rings = new int[][]{{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44}, {10, 11, 12, 13, 14, 15, 16, 19, 25, 28, 29, 30, 31, 32, 33, 34}, {20, 21, 23, 24}, {22}};
        new BukkitRunnable(){
            int step = 0;

            public void run() {
                Material glass = switch (this.step % 4) {
                    case 0 -> Material.LIGHT_BLUE_STAINED_GLASS_PANE;
                    case 1 -> Material.PURPLE_STAINED_GLASS_PANE;
                    case 2 -> Material.MAGENTA_STAINED_GLASS_PANE;
                    default -> Material.WHITE_STAINED_GLASS_PANE;
                };
                for (int ring = 0; ring < rings.length; ++ring) {
                    for (int slot : rings[ring]) {
                        inv.setItem(slot, ring <= this.step / 8 ? AnimationRunner.this.glass(glass, " ") : null);
                    }
                }
                if (this.step > crate.animationDuration - 8) {
                    inv.setItem(22, AnimationRunner.this.playerIcon(reward));
                }
                AnimationRunner.this.tickSound(player, crate, 0.8f + (float)this.step * 0.03f);
                if (this.step++ >= crate.animationDuration) {
                    this.cancel();
                    inv.setItem(22, AnimationRunner.this.playerIcon(reward));
                    AnimationRunner.this.finishLater(player, crate, finish);
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, (long)Math.max(1, crate.animationSpeed));
    }

    private void prism(final Player player, final Crate crate, final Reward reward, final Runnable finish) {
        final Inventory inv = this.plugin.getServer().createInventory(null, 45, this.animationTitle(crate));
        player.openInventory(inv);
        this.plugin.gui().lock(player, inv);
        final Material[] colors = new Material[]{Material.RED_STAINED_GLASS_PANE, Material.ORANGE_STAINED_GLASS_PANE, Material.YELLOW_STAINED_GLASS_PANE, Material.LIME_STAINED_GLASS_PANE, Material.LIGHT_BLUE_STAINED_GLASS_PANE, Material.PURPLE_STAINED_GLASS_PANE};
        new BukkitRunnable(){
            int step = 0;

            public void run() {
                for (int i = 0; i < inv.getSize(); ++i) {
                    inv.setItem(i, AnimationRunner.this.glass(colors[(i + this.step) % colors.length], " "));
                }
                inv.setItem(22, this.step > crate.animationDuration - 6 ? AnimationRunner.this.playerIcon(reward) : AnimationRunner.this.glass(Material.WHITE_STAINED_GLASS_PANE, "&f?"));
                AnimationRunner.this.tickSound(player, crate, 1.1f);
                if (this.step++ >= crate.animationDuration) {
                    this.cancel();
                    inv.setItem(22, AnimationRunner.this.playerIcon(reward));
                    AnimationRunner.this.finishLater(player, crate, finish);
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, (long)Math.max(1, crate.animationSpeed));
    }

    private void blackHole(final Player player, final Crate crate, final Reward reward, final Runnable finish) {
        final Inventory inv = this.plugin.getServer().createInventory(null, 45, this.animationTitle(crate));
        player.openInventory(inv);
        this.plugin.gui().lock(player, inv);
        final int[] pull = new int[]{0, 8, 36, 44, 1, 7, 9, 17, 27, 35, 37, 43, 10, 16, 28, 34, 20, 24, 21, 23, 22};
        new BukkitRunnable(){
            int step = 0;

            public void run() {
                if (this.step < pull.length) {
                    inv.setItem(pull[this.step], AnimationRunner.this.glass(Material.BLACK_STAINED_GLASS_PANE, "&8*"));
                }
                inv.setItem(22, this.step > crate.animationDuration - 5 ? AnimationRunner.this.playerIcon(reward) : AnimationRunner.this.glass(Material.ENDER_PEARL, "&5"));
                AnimationRunner.this.tickSound(player, crate, 0.6f + (float)this.step * 0.04f);
                if (this.step++ >= crate.animationDuration) {
                    this.cancel();
                    inv.clear();
                    inv.setItem(22, AnimationRunner.this.playerIcon(reward));
                    AnimationRunner.this.finishLater(player, crate, finish);
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, (long)Math.max(1, crate.animationSpeed));
    }

    private void lightning(Player player, Location location, Crate crate, Reward reward, Runnable finish) {
        player.getWorld().strikeLightningEffect(location);
        player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.6f, 1.4f);
        this.plugin.getServer().getScheduler().runTaskLater((Plugin)this.plugin, finish, 25L);
    }

    private void vortex(final Player player, final Location location, final Crate crate, Reward reward, final Runnable finish) {
        new BukkitRunnable(){
            int step = 0;

            public void run() {
                for (int i = 0; i < 18; ++i) {
                    double angle = (double)this.step * 0.35 + (double)i;
                    Location point = location.clone().add(Math.cos(angle) * 0.8, (double)i * 0.04, Math.sin(angle) * 0.8);
                    player.getWorld().spawnParticle(crate.particle, point, 1, 0.0, 0.0, 0.0, 0.0);
                }
                if (this.step++ > 30) {
                    this.cancel();
                    finish.run();
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, 1L);
    }

    private void explosion(Player player, Location location, Crate crate, Reward reward, Runnable finish) {
        player.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, location, 3, 0.2, 0.2, 0.2, 0.0);
        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.7f, 1.3f);
        if (crate.animationFirework) {
            this.spawnFirework(player.getLocation());
        }
        this.plugin.getServer().getScheduler().runTaskLater((Plugin)this.plugin, finish, (long)Math.max(10, crate.animationCloseDelay));
    }

    private void beacon(final Player player, final Location location, final Crate crate, Reward reward, final Runnable finish) {
        new BukkitRunnable(){
            int step = 0;

            public void run() {
                for (double y = 0.0; y < 3.0; y += 0.25) {
                    player.getWorld().spawnParticle(Particle.END_ROD, location.clone().add(0.0, y, 0.0), 1, 0.0, 0.0, 0.0, 0.0);
                }
                AnimationRunner.this.tickSound(player, crate, 1.5f);
                if (this.step++ >= crate.animationDuration) {
                    this.cancel();
                    finish.run();
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, (long)Math.max(1, crate.animationSpeed));
    }

    private void confetti(final Player player, final Location location, final Crate crate, Reward reward, final Runnable finish) {
        new BukkitRunnable(){
            int step = 0;

            public void run() {
                player.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, location, 30, 0.8, 0.8, 0.8, 0.08);
                player.getWorld().spawnParticle(crate.particle, location, 15, 0.7, 0.7, 0.7, 0.02);
                if (this.step++ >= Math.max(10, crate.animationDuration / 2)) {
                    this.cancel();
                    finish.run();
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, (long)Math.max(1, crate.animationSpeed));
    }

    private void matrix(final Player player, final Crate crate, final Reward reward, final Runnable finish) {
        final Inventory inv = this.plugin.getServer().createInventory(null, crate.animationGuiSize, this.animationTitle(crate));
        player.openInventory(inv);
        this.plugin.gui().lock(player, inv);
        final Material[] green = new Material[]{Material.LIME_STAINED_GLASS_PANE, Material.GREEN_STAINED_GLASS_PANE, Material.BLACK_STAINED_GLASS_PANE};
        new BukkitRunnable(){
            int step = 0;

            public void run() {
                for (int i = 0; i < inv.getSize(); ++i) {
                    inv.setItem(i, AnimationRunner.this.glass(green[AnimationRunner.this.random.nextInt(green.length)], "&a" + AnimationRunner.this.random.nextInt(10)));
                }
                if (this.step > crate.animationDuration - 5) {
                    inv.setItem(AnimationRunner.this.center(inv), AnimationRunner.this.playerIcon(reward));
                }
                AnimationRunner.this.tickSound(player, crate, 1.2f);
                if (this.step++ >= crate.animationDuration) {
                    this.cancel();
                    inv.setItem(AnimationRunner.this.center(inv), AnimationRunner.this.playerIcon(reward));
                    AnimationRunner.this.finishLater(player, crate, finish);
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, (long)Math.max(1, crate.animationSpeed));
    }

    private void countdown(final Player player, final Crate crate, final Reward reward, final Runnable finish) {
        final Inventory inv = this.plugin.getServer().createInventory(null, 27, this.animationTitle(crate));
        player.openInventory(inv);
        this.plugin.gui().lock(player, inv);
        new BukkitRunnable(){
            int step = 3;

            public void run() {
                inv.setItem(13, AnimationRunner.this.glass(Material.CLOCK, (String)(this.step > 0 ? "&e" + this.step : "&aGO")));
                player.playSound(player.getLocation(), this.step > 0 ? Sound.BLOCK_NOTE_BLOCK_PLING : Sound.ENTITY_PLAYER_LEVELUP, 1.0f, this.step > 0 ? 1.0f : 1.6f);
                if (this.step-- <= 0) {
                    this.cancel();
                    inv.setItem(13, AnimationRunner.this.playerIcon(reward));
                    AnimationRunner.this.finishLater(player, crate, finish);
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, 20L);
    }

    private void orbit(final Player player, final Location location, final Crate crate, Reward reward, final Runnable finish) {
        new BukkitRunnable(){
            int step = 0;

            public void run() {
                for (int i = 0; i < 4; ++i) {
                    double angle = (double)this.step * 0.25 + (double)i * Math.PI / 2.0;
                    player.getWorld().spawnParticle(crate.particle, location.clone().add(Math.cos(angle), 0.2, Math.sin(angle)), 3, 0.0, 0.0, 0.0, 0.0);
                }
                if (this.step++ >= crate.animationDuration) {
                    this.cancel();
                    finish.run();
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, (long)Math.max(1, crate.animationSpeed));
    }

    private void exoticSpiral(final Player player, final Location location, final Crate crate, Reward reward, final Runnable finish, final Particle particle) {
        new BukkitRunnable(){
            int step = 0;

            public void run() {
                for (int i = 0; i < 24; ++i) {
                    double angle = (double)this.step * 0.22 + (double)i * 0.35;
                    double radius = 0.25 + (double)i * 0.035;
                    Location point = location.clone().add(Math.cos(angle) * radius, (double)i * 0.045, Math.sin(angle) * radius);
                    player.getWorld().spawnParticle(particle, point, 1, 0.0, 0.0, 0.0, 0.0);
                }
                AnimationRunner.this.tickSound(player, crate, 0.8f + (float)this.step * 0.02f);
                if (this.step++ >= crate.animationDuration) {
                    this.cancel();
                    if (crate.animationFirework) {
                        AnimationRunner.this.spawnFirework(player.getLocation());
                    }
                    finish.run();
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, (long)Math.max(1, crate.animationSpeed));
    }

    private void meteor(final Player player, final Location location, final Crate crate, Reward reward, final Runnable finish) {
        new BukkitRunnable(){
            int step = 0;

            public void run() {
                Location point = location.clone().add((double)(crate.animationDuration - this.step) * 0.04, 3.0 - (double)this.step * 0.08, 0.0);
                player.getWorld().spawnParticle(Particle.FLAME, point, 12, 0.08, 0.08, 0.08, 0.02);
                player.getWorld().spawnParticle(Particle.SMOKE_LARGE, point, 4, 0.1, 0.1, 0.1, 0.01);
                if (this.step++ >= crate.animationDuration) {
                    this.cancel();
                    player.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, location, 2, 0.1, 0.1, 0.1, 0.0);
                    finish.run();
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, (long)Math.max(1, crate.animationSpeed));
    }

    private void finishLater(Player player, Crate crate, Runnable finish) {
        if (crate.animationFirework) {
            this.spawnFirework(player.getLocation());
        }
        this.plugin.getServer().getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            this.plugin.gui().unlock(player);
            player.closeInventory();
            finish.run();
        }, (long)Math.max(1, crate.animationCloseDelay));
    }

    private void tickSound(Player player, Crate crate, float pitch) {
        try {
            player.playSound(player.getLocation(), Sound.valueOf((String)crate.animationSound), 0.8f, Math.max(0.5f, Math.min(2.0f, pitch)));
        }
        catch (IllegalArgumentException ex) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, Math.max(0.5f, Math.min(2.0f, pitch)));
        }
    }

    private int center(Inventory inv) {
        return inv.getSize() / 2;
    }

    private String animationTitle(Crate crate) {
        String name = Text.plain(crate.displayName).isBlank() ? crate.id : Text.plain(crate.displayName);
        return Text.color("DadaCrates - PickRoll " + name);
    }

    private ItemStack glass(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Text.color(name));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack playerIcon(Reward reward) {
        ItemStack item = reward.itemReward();
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (!meta.hasDisplayName()) {
                meta.setDisplayName(Text.color(reward.name()));
            }
            if (!meta.hasLore()) {
                meta.setLore(Text.color(reward.lore()));
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private void spawnFirework(Location location) {
        Firework firework = (Firework)location.getWorld().spawn(location, Firework.class);
        firework.getPersistentDataContainer().set(this.plugin.cosmeticFireworkKey(), PersistentDataType.BYTE, (byte)1);
        FireworkMeta meta = firework.getFireworkMeta();
        meta.addEffect(FireworkEffect.builder().withColor(new Color[]{Color.AQUA, Color.YELLOW}).with(FireworkEffect.Type.BALL_LARGE).build());
        meta.setPower(0);
        firework.setFireworkMeta(meta);
        firework.detonate();
    }
}
