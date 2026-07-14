/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Location
 *  org.bukkit.NamespacedKey
 *  org.bukkit.World
 *  org.bukkit.entity.ArmorStand
 *  org.bukkit.entity.Display$Billboard
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.TextDisplay
 *  org.bukkit.entity.TextDisplay$TextAlignment
 *  org.bukkit.persistence.PersistentDataType
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitTask
 */
package it.dadasmp.dadacrates;

import it.dadasmp.dadacrates.Crate;
import it.dadasmp.dadacrates.DadaCratesPlugin;
import it.dadasmp.dadacrates.Text;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

final class HologramManager {
    private static final double LINE_SPACING = 0.25;
    private static final double BLANK_LINE_SPACING = 0.25;
    private final DadaCratesPlugin plugin;
    private final NamespacedKey tag;
    private BukkitTask keepAliveTask;

    HologramManager(DadaCratesPlugin plugin) {
        this.plugin = plugin;
        this.tag = new NamespacedKey((Plugin)plugin, "crate_hologram");
    }

    void refresh() {
        this.clear();
        for (Crate crate : this.plugin.store().all()) {
            if (!crate.hologramEnabled) continue;
            for (String serialized : crate.locations) {
                Location base = this.plugin.store().deserialize(serialized);
                if (base == null || base.getWorld() == null) continue;
                base.getChunk().load();
                this.spawn(crate, base);
            }
        }
    }

    void startKeepAlive() {
        this.stopKeepAlive();
        this.plugin.getServer().getScheduler().runTaskLater((Plugin)this.plugin, this::refresh, 40L);
        this.keepAliveTask = this.plugin.getServer().getScheduler().runTaskTimer((Plugin)this.plugin, this::refresh, 600L, 600L);
    }

    void stopKeepAlive() {
        if (this.keepAliveTask != null) {
            this.keepAliveTask.cancel();
            this.keepAliveTask = null;
        }
    }

    void clear() {
        for (World world : this.plugin.getServer().getWorlds()) {
            for (Entity entity : world.getEntitiesByClass(ArmorStand.class)) {
                if (!entity.getPersistentDataContainer().has(this.tag, PersistentDataType.STRING)) continue;
                entity.remove();
            }
            for (Entity entity : world.getEntitiesByClass(TextDisplay.class)) {
                if (!entity.getPersistentDataContainer().has(this.tag, PersistentDataType.STRING)) continue;
                entity.remove();
            }
        }
    }

    private void spawn(Crate crate, Location blockLocation) {
        double y = blockLocation.getY() + crate.hologramOffsetY;
        for (String line : crate.hologramLines) {
            if (line == null || line.isBlank()) {
                y -= 0.25;
                continue;
            }
            Location lineLocation = new Location(blockLocation.getWorld(), (double)blockLocation.getBlockX() + crate.hologramOffsetX, y, (double)blockLocation.getBlockZ() + crate.hologramOffsetZ);
            blockLocation.getWorld().spawn(lineLocation, TextDisplay.class, textDisplay -> {
                textDisplay.setText(this.format(crate, line));
                textDisplay.setLineWidth(300);
                textDisplay.setBillboard(Display.Billboard.CENTER);
                textDisplay.setAlignment(TextDisplay.TextAlignment.CENTER);
                textDisplay.setGravity(false);
                textDisplay.setTextOpacity((byte)-1);
                textDisplay.setSeeThrough(false);
                textDisplay.setShadowed(false);
                textDisplay.setDefaultBackground(false);
                textDisplay.setPersistent(false);
                textDisplay.getPersistentDataContainer().set(this.tag, PersistentDataType.STRING, crate.id);
            });
            y -= 0.25;
        }
    }

    private String format(Crate crate, String line) {
        return Text.color(this.readableSpacing(line).replace("%crate%", Text.plain(crate.displayName)).replace("%id%", crate.id).replace("%key%", Text.plain(crate.keyName)).replace("%animation%", crate.animation.name()));
    }

    private String readableSpacing(String line) {
        return line.replace("Clic &aDESTRO &fper", "Clic  &aDESTRO  &fper").replace("Clic &bSINISTRO &fper", "Clic  &bSINISTRO  &fper");
    }
}
