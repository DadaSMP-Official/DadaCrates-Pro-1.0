/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.block.Block
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.Firework
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.block.Action
 *  org.bukkit.event.entity.EntityDamageByEntityEvent
 *  org.bukkit.event.player.PlayerInteractEvent
 *  org.bukkit.inventory.EquipmentSlot
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.persistence.PersistentDataType
 */
package it.dadasmp.dadacrates;

import it.dadasmp.dadacrates.Crate;
import it.dadasmp.dadacrates.DadaCratesPlugin;
import it.dadasmp.dadacrates.KeyMode;
import it.dadasmp.dadacrates.Reward;
import it.dadasmp.dadacrates.Text;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

final class CrateListener
implements Listener {
    private final DadaCratesPlugin plugin;

    CrateListener(DadaCratesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND || event.getClickedBlock() == null) {
            return;
        }
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (this.plugin.command().takeWaitingRemoveBlock(player) && player.hasPermission("dadacrates.admin")) {
            if (this.plugin.store().removeLocation(block.getLocation())) {
                player.sendMessage(this.plugin.store().prefix() + Text.color("&aCrate posizionata rimossa."));
            } else {
                player.sendMessage(this.plugin.store().prefix() + Text.color("&cQuesto blocco non e una crate registrata."));
            }
            event.setCancelled(true);
            return;
        }
        String pending = this.plugin.command().takeWaitingSetBlock(player);
        if (pending != null && player.hasPermission("dadacrates.admin")) {
            this.plugin.store().byId(pending).ifPresent(crate -> {
                this.plugin.store().addLocation((Crate)crate, block.getLocation());
                block.setType(crate.blockMaterial);
                this.plugin.holograms().refresh();
                player.sendMessage(this.plugin.store().prefix() + Text.color("&aBlocco impostato per &f" + crate.id));
            });
            event.setCancelled(true);
            return;
        }
        Crate crate2 = this.plugin.store().byLocation(block.getLocation()).orElse(null);
        if (crate2 == null) {
            return;
        }
        event.setCancelled(true);
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            this.plugin.gui().openPreview(player, crate2);
            return;
        }
        if (!player.hasPermission("dadacrates.use")) {
            player.sendMessage(this.plugin.store().prefix() + this.plugin.store().msg("no-permission"));
            return;
        }
        if (!crate2.permission.isBlank() && !player.hasPermission(crate2.permission)) {
            player.sendMessage(this.plugin.store().prefix() + this.plugin.store().msg("need-crate-permission").replace("%permission%", crate2.permission));
            return;
        }
        if (!this.plugin.keys().isKey(player.getInventory().getItemInMainHand(), crate2)) {
            player.sendMessage(this.plugin.store().prefix() + Text.color("&cTi serve una &e%key%&c.").replace("%key%", Text.plain(crate2.keyName)));
            return;
        }
        ItemStack keyItem = player.getInventory().getItemInMainHand();
        KeyMode keyMode = this.plugin.keys().mode(keyItem);
        int keyPicks = this.plugin.keys().picks(keyItem, crate2);
        Reward reward = this.plugin.store().roll(crate2);
        if (reward == null) {
            player.sendMessage(this.plugin.store().prefix() + Text.color("&cQuesta crate non ha reward validi."));
            return;
        }
        player.sendMessage(this.plugin.store().prefix() + Text.color(crate2.openMessage).replace("%crate%", Text.plain(crate2.displayName)));
        if (keyMode != KeyMode.NORMAL && crate2.pickModeEnabled && keyPicks > 0) {
            this.plugin.gui().openPrizePicker(player, block.getLocation().add(0.5, 1.1, 0.5), crate2, keyPicks);
        } else {
            this.plugin.keys().consumeOne(player.getInventory().getItemInMainHand());
            this.plugin.animations().play(player, block.getLocation().add(0.5, 1.1, 0.5), crate2, reward, () -> this.plugin.award(player, crate2, reward));
        }
    }

    @EventHandler
    public void onFireworkDamage(EntityDamageByEntityEvent event) {
        Firework firework;
        Entity entity = event.getDamager();
        if (entity instanceof Firework && (firework = (Firework)entity).getPersistentDataContainer().has(this.plugin.cosmeticFireworkKey(), PersistentDataType.BYTE)) {
            event.setCancelled(true);
            event.setDamage(0.0);
        }
    }
}

