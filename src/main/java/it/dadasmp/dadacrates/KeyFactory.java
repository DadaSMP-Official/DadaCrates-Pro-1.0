/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.NamespacedKey
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.persistence.PersistentDataType
 *  org.bukkit.plugin.Plugin
 */
package it.dadasmp.dadacrates;

import it.dadasmp.dadacrates.Crate;
import it.dadasmp.dadacrates.DadaCratesPlugin;
import it.dadasmp.dadacrates.KeyMode;
import it.dadasmp.dadacrates.Text;
import java.util.ArrayList;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

final class KeyFactory {
    private final NamespacedKey keyTag;
    private final NamespacedKey modeTag;
    private final NamespacedKey picksTag;

    KeyFactory(DadaCratesPlugin plugin) {
        this.keyTag = new NamespacedKey((Plugin)plugin, "crate_key");
        this.modeTag = new NamespacedKey((Plugin)plugin, "crate_key_mode");
        this.picksTag = new NamespacedKey((Plugin)plugin, "crate_key_picks");
    }

    ItemStack create(Crate crate, int amount) {
        return this.create(crate, amount, KeyMode.NORMAL);
    }

    ItemStack create(Crate crate, int amount, KeyMode mode) {
        return this.create(crate, amount, mode, crate.keyUses);
    }

    ItemStack create(Crate crate, int amount, KeyMode mode, int picks) {
        ItemStack item = crate.keyItem == null ? new ItemStack(crate.keyMaterial == null ? Material.TRIPWIRE_HOOK : crate.keyMaterial, Math.max(1, amount)) : crate.keyItem.clone();
        item.setAmount(Math.max(1, amount));
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            ArrayList<String> lore;
            if (!meta.hasDisplayName()) {
                meta.setDisplayName(Text.color(crate.keyName));
            }
            ArrayList<String> arrayList = lore = meta.hasLore() ? new ArrayList<String>(meta.getLore()) : new ArrayList();
            if (mode != KeyMode.NORMAL) {
                lore.add(Text.color("&7Premi selezionabili: &e" + Math.max(1, picks)));
            }
            meta.setLore(lore.isEmpty() ? null : lore);
            meta.getPersistentDataContainer().set(this.keyTag, PersistentDataType.STRING, crate.keyId);
            meta.getPersistentDataContainer().set(this.modeTag, PersistentDataType.STRING, mode.name());
            meta.getPersistentDataContainer().set(this.picksTag, PersistentDataType.INTEGER, Math.max(1, picks));
            item.setItemMeta(meta);
        }
        return item;
    }

    boolean isKey(ItemStack item, Crate crate) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) {
            return false;
        }
        String value = (String)item.getItemMeta().getPersistentDataContainer().get(this.keyTag, PersistentDataType.STRING);
        if (crate.keyId.equalsIgnoreCase(String.valueOf(value))) {
            return true;
        }
        ItemMeta meta = item.getItemMeta();
        String display = meta.hasDisplayName() ? meta.getDisplayName() : "";
        return item.getType() == crate.keyMaterial && Text.plain(display).equalsIgnoreCase(Text.plain(crate.keyName));
    }

    KeyMode mode(ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) {
            return KeyMode.NORMAL;
        }
        String value = (String)item.getItemMeta().getPersistentDataContainer().get(this.modeTag, PersistentDataType.STRING);
        return KeyMode.parse(value);
    }

    int picks(ItemStack item, Crate crate) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) {
            return crate.keyUses;
        }
        Integer value = (Integer)item.getItemMeta().getPersistentDataContainer().get(this.picksTag, PersistentDataType.INTEGER);
        return value == null ? crate.keyUses : Math.max(1, value);
    }

    void consumeOne(ItemStack item) {
        if (item.getAmount() <= 1) {
            item.setAmount(0);
            return;
        }
        item.setAmount(item.getAmount() - 1);
    }
}
