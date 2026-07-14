/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 */
package it.dadasmp.dadacrates;

import it.dadasmp.dadacrates.RewardType;
import it.dadasmp.dadacrates.Text;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

record Reward(String id, String name, List<String> lore, double chance, RewardType type, Material material, int amount, ItemStack item, List<String> commands, boolean broadcast, String broadcastMessage) {
    ItemStack icon() {
        ItemStack icon = this.baseItem();
        ItemMeta meta = icon.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Text.color(this.name));
            meta.setLore(List.of(Text.color("&7Chance: &e" + this.chance + "%"), Text.color("&7Tipo: &f" + String.valueOf((Object)this.type))));
            icon.setItemMeta(meta);
        }
        return icon;
    }

    ItemStack itemReward() {
        return this.baseItem();
    }

    private ItemStack baseItem() {
        if (this.item != null && !this.item.getType().isAir()) {
            ItemStack clone = this.item.clone();
            clone.setAmount(Math.max(1, Math.min(64, this.amount)));
            return clone;
        }
        ItemStack fallback = new ItemStack(this.material == null ? Material.STONE : this.material, Math.max(1, Math.min(64, this.amount)));
        ItemMeta meta = fallback.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Text.color(this.name));
            meta.setLore(Text.color(this.lore));
            fallback.setItemMeta(meta);
        }
        return fallback;
    }
}

