/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.Particle
 *  org.bukkit.Sound
 *  org.bukkit.entity.HumanEntity
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.inventory.ClickType
 *  org.bukkit.event.inventory.InventoryClickEvent
 *  org.bukkit.event.inventory.InventoryCloseEvent
 *  org.bukkit.event.inventory.InventoryDragEvent
 *  org.bukkit.event.player.AsyncPlayerChatEvent
 *  org.bukkit.inventory.Inventory
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitRunnable
 */
package it.dadasmp.dadacrates;

import it.dadasmp.dadacrates.AnimationType;
import it.dadasmp.dadacrates.Crate;
import it.dadasmp.dadacrates.DadaCratesPlugin;
import it.dadasmp.dadacrates.KeyMode;
import it.dadasmp.dadacrates.ParticleShape;
import it.dadasmp.dadacrates.Reward;
import it.dadasmp.dadacrates.Text;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

final class GuiManager
implements Listener {
    private static final String MAIN = "DadaCrates - Admin";
    private static final String LIST = "DadaCrates - Crates";
    private static final String EDIT = "DadaCrates - Edit ";
    private static final String REWARDS = "DadaCrates - Rewards ";
    private static final String PREVIEW = "DadaCrates - Preview ";
    private static final String REWARD_EDIT = "DadaCrates - RewardEdit ";
    private static final String PICKER = "DadaCrates - Pick ";
    private static final String ANIMS = "DadaCrates - Anim ";
    private static final String ANIM_SETTINGS = "DadaCrates - AnimCfg ";
    private static final String BLOCKS = "DadaCrates - Block ";
    private static final String EFFECTS = "DadaCrates - Effect ";
    private static final String IDLE_PARTICLES = "DadaCrates - Particles ";
    private static final String KEYS_LIST = "DadaCrates - Keys";
    private static final String KEY = "DadaCrates - Key ";
    private static final String MESSAGES = "DadaCrates - Msg ";
    private static final String PERMS = "DadaCrates - Perm ";
    private final DadaCratesPlugin plugin;
    private final Map<UUID, PickSession> pickSessions = new HashMap<UUID, PickSession>();
    private final Map<UUID, ItemStack> pendingKeyItems = new HashMap<UUID, ItemStack>();
    private final Map<UUID, Inventory> lockedInventories = new HashMap<UUID, Inventory>();
    private final Map<UUID, CommandInputSession> commandInputs = new HashMap<UUID, CommandInputSession>();
    private final Map<UUID, String> previewTitles = new HashMap<UUID, String>();

    GuiManager(DadaCratesPlugin plugin) {
        this.plugin = plugin;
    }

    void lock(Player player, Inventory inventory) {
        this.lockedInventories.put(player.getUniqueId(), inventory);
    }

    void unlock(Player player) {
        this.lockedInventories.remove(player.getUniqueId());
    }

    void openMain(Player player) {
        Inventory inv = this.plugin.getServer().createInventory(null, 27, MAIN);
        inv.setItem(10, this.item(Material.CHEST, "&6Crates", "&7Crea, modifica, seleziona"));
        inv.setItem(12, this.item(Material.TRIPWIRE_HOOK, "&eKeys", "&7Key personalizzate da GUI"));
        inv.setItem(14, this.item(Material.FIREWORK_ROCKET, "&dAnimazioni", "&722 animazioni e settaggi"));
        inv.setItem(16, this.item(Material.COMPARATOR, "&bReload", "&7Ricarica config"));
        this.fill(inv);
        player.openInventory(inv);
    }

    void openCrates(Player player) {
        Inventory inv = this.plugin.getServer().createInventory(null, 54, LIST);
        int slot = 0;
        for (Crate crate : this.plugin.store().all()) {
            inv.setItem(slot++, this.item(crate.blockMaterial, crate.displayName, "&7ID: &f" + crate.id, "&7Permesso: &e" + crate.permission, "&7Animazione: &d" + String.valueOf((Object)crate.animation), "&7Click per modificare"));
        }
        inv.setItem(53, this.item(Material.EMERALD, "&aNuova crate", "&7Comando: /dcrates create <id>"));
        player.openInventory(inv);
    }

    void openKeys(Player player) {
        Inventory inv = this.plugin.getServer().createInventory(null, 54, KEYS_LIST);
        int slot = 0;
        for (Crate crate : this.plugin.store().all()) {
            if (slot >= 45) break;
            ItemStack key = this.plugin.keys().create(crate, 1, KeyMode.NORMAL);
            ItemMeta meta = key.getItemMeta();
            if (meta != null) {
                meta.setLore(Text.color(List.of(
                        "&7Click per aprire la gestione key.",
                        "&7Crate: &f" + Text.plain(crate.displayName),
                        "&7Utilizzi default: &f" + crate.keyUses,
                        "&8ID: &f" + crate.id
                )));
                key.setItemMeta(meta);
            }
            inv.setItem(slot++, key);
        }
        inv.setItem(53, this.item(Material.BARRIER, "&cIndietro", "&7Torna al menu principale"));
        this.fill(inv);
        player.openInventory(inv);
    }

    void openEdit(Player player, Crate crate) {
        Inventory inv = this.plugin.getServer().createInventory(null, 45, EDIT + crate.id);
        inv.setItem(10, this.item(Material.CHEST, "&6Tipo baule/blocco", "&7Cassa, barrel, ender chest o blocco"));
        inv.setItem(11, this.item(Material.TRIPWIRE_HOOK, "&eKey", "&7Metti una key nella GUI e salva"));
        inv.setItem(12, this.item(Material.NETHER_STAR, "&dReward e probabilita", "&7Aggiungi, togli e cambia chance"));
        inv.setItem(13, this.item(Material.FIREWORK_ROCKET, "&bAnimazione", "&7Attuale: &f" + String.valueOf((Object)crate.animation)));
        inv.setItem(14, this.item(Material.BLAZE_POWDER, "&aEffetto intorno", "&7Attuale: &f" + String.valueOf(crate.particle)));
        inv.setItem(15, this.item(Material.OAK_SIGN, "&fMessaggi", "&7No key, apertura, vincita"));
        inv.setItem(16, this.item(Material.REDSTONE_TORCH, "&cPermesso crate", "&7" + crate.permission));
        inv.setItem(25, this.item(Material.AMETHYST_SHARD, "&dParticelle crate", "&7Forma: &f" + String.valueOf((Object)crate.idleParticleShape), "&7RGB: &c" + crate.idleParticleRed + " &a" + crate.idleParticleGreen + " &b" + crate.idleParticleBlue, "&7Click per modificare"));
        inv.setItem(28, this.item(Material.CLOCK, "&bImpostazioni animazione", "&7Durata: &f" + crate.animationDuration, "&7Velocita: &f" + crate.animationSpeed, "&7GUI: &f" + crate.animationGuiSize, "&7Firework: &f" + crate.animationFirework));
        inv.setItem(31, this.item(Material.MAP, "&aImposta blocco fisico", "&7Usa: /dcrates setblock " + crate.id));
        this.fill(inv);
        player.openInventory(inv);
    }

    void openRewards(Player player, Crate crate) {
        Inventory inv = this.plugin.getServer().createInventory(null, 54, REWARDS + crate.id);
        int slot = 0;
        for (Reward reward : crate.rewards) {
            if (slot >= 45) break;
            inv.setItem(slot++, reward.itemReward());
        }
        inv.setItem(45, this.item(Material.LIME_DYE, "&aSalvataggio automatico", "&7Metti qui dentro gli oggetti", "&7Chiudi la GUI per salvare"));
        inv.setItem(49, this.item(Material.PAPER, "&eModifica reward", "&7Click destro su un premio", "&7per probabilita, comando e broadcast"));
        inv.setItem(53, this.item(Material.BARRIER, "&cIndietro", new String[0]));
        player.openInventory(inv);
    }

    void openPreview(Player player, Crate crate) {
        String title = this.centeredPreviewTitle(crate);
        this.previewTitles.put(player.getUniqueId(), title);
        Inventory inv = this.plugin.getServer().createInventory(null, 54, title);
        int slot = 0;
        for (Reward reward : crate.rewards) {
            if (slot >= 54) break;
            inv.setItem(slot++, this.previewIcon(reward));
        }
        player.openInventory(inv);
    }

    private void openAnimations(Player player, Crate crate) {
        Inventory inv = this.plugin.getServer().createInventory(null, 54, ANIMS + crate.id);
        int slot = 0;
        for (AnimationType type : AnimationType.values()) {
            inv.setItem(slot++, this.item(Material.FIREWORK_STAR, (crate.animation == type ? "&a" : "&d") + type.name(), "&7Click per selezionare"));
        }
        player.openInventory(inv);
    }

    private void openAnimationSettings(Player player, Crate crate) {
        Inventory inv = this.plugin.getServer().createInventory(null, 36, ANIM_SETTINGS + crate.id);
        inv.setItem(10, this.item(Material.CLOCK, "&eDurata", "&7Attuale: &f" + crate.animationDuration, "&7Sinistro +5, destro -5"));
        inv.setItem(12, this.item(Material.SUGAR, "&bVelocita tick", "&7Attuale: &f" + crate.animationSpeed, "&7Sinistro +1, destro -1"));
        inv.setItem(14, this.item(Material.CHEST, "&dGrandezza GUI", "&7Attuale: &f" + crate.animationGuiSize, "&7Click cambia 9/18/27/36/45/54"));
        inv.setItem(16, this.item(crate.animationFirework ? Material.LIME_DYE : Material.GRAY_DYE, "&aFirework finale", "&7Attuale: &f" + crate.animationFirework));
        inv.setItem(31, this.item(Material.BARRIER, "&cIndietro", new String[0]));
        this.fill(inv);
        player.openInventory(inv);
    }

    private void openBlocks(Player player, Crate crate) {
        Inventory inv = this.plugin.getServer().createInventory(null, 54, BLOCKS + crate.id);
        List<Material> materials = List.of(Material.CHEST, Material.TRAPPED_CHEST, Material.BARREL, Material.ENDER_CHEST, Material.PLAYER_HEAD, Material.GOLD_BLOCK, Material.DIAMOND_BLOCK, Material.EMERALD_BLOCK, Material.NETHERITE_BLOCK);
        for (int i = 0; i < materials.size(); ++i) {
            Material material = materials.get(i);
            inv.setItem(i, this.item(material, (crate.blockMaterial == material ? "&a" : "&e") + material.name(), "&7Click per scegliere"));
        }
        inv.setItem(49, this.item(Material.ANVIL, "&aSalva blocco dal cursore", "&7Metti un blocco sul cursore", "&7e clicca qui per salvarlo"));
        inv.setItem(53, this.item(Material.BARRIER, "&cIndietro", new String[0]));
        player.openInventory(inv);
    }

    private void openEffects(Player player, Crate crate) {
        Inventory inv = this.plugin.getServer().createInventory(null, 54, EFFECTS + crate.id);
        List<Particle> particles = List.of(Particle.VILLAGER_HAPPY, Particle.FLAME, Particle.SOUL_FIRE_FLAME, Particle.ENCHANTMENT_TABLE, Particle.CRIT_MAGIC, Particle.END_ROD, Particle.HEART, Particle.TOTEM, Particle.PORTAL, Particle.CLOUD, Particle.FIREWORKS_SPARK, Particle.CRIT);
        for (int i = 0; i < particles.size(); ++i) {
            Particle particle = particles.get(i);
            inv.setItem(i, this.item(Material.BLAZE_POWDER, (crate.particle == particle ? "&a" : "&b") + particle.name(), "&7Click per scegliere"));
        }
        player.openInventory(inv);
    }

    private void openIdleParticles(Player player, Crate crate) {
        Inventory inv = this.plugin.getServer().createInventory(null, 54, IDLE_PARTICLES + crate.id);
        int slot = 0;
        for (ParticleShape shape : ParticleShape.values()) {
            inv.setItem(slot++, this.item(shape == crate.idleParticleShape ? Material.LIME_DYE : Material.AMETHYST_SHARD, (shape == crate.idleParticleShape ? "&a" : "&d") + shape.name(), "&7Click per scegliere la forma"));
        }
        inv.setItem(27, this.item(Material.RED_DYE, "&cRosso", "&7Attuale: &f" + crate.idleParticleRed, "&7Sinistro +15, destro -15"));
        inv.setItem(28, this.item(Material.LIME_DYE, "&aVerde", "&7Attuale: &f" + crate.idleParticleGreen, "&7Sinistro +15, destro -15"));
        inv.setItem(29, this.item(Material.LIGHT_BLUE_DYE, "&bBlu", "&7Attuale: &f" + crate.idleParticleBlue, "&7Sinistro +15, destro -15"));
        inv.setItem(31, this.item(Material.SLIME_BALL, "&eGrandezza", "&7Attuale: &f" + crate.idleParticleSize, "&7Sinistro +0.1, destro -0.1"));
        inv.setItem(32, this.item(Material.ENDER_PEARL, "&dRaggio", "&7Attuale: &f" + crate.idleParticleRadius, "&7Sinistro +0.1, destro -0.1"));
        inv.setItem(33, this.item(Material.GLOWSTONE_DUST, "&6Quantita", "&7Attuale: &f" + crate.idleParticlePoints, "&7Sinistro +2, destro -2"));
        inv.setItem(34, this.item(Material.FEATHER, "&fAltezza", "&7Attuale: &f" + crate.idleParticleHeight, "&7Sinistro +0.1, destro -0.1", "&7Valori bassi = mezzo/parte bassa crate"));
        inv.setItem(36, this.item(Material.REDSTONE, "&cParticella RGB", "&7REDSTONE colorabile"));
        inv.setItem(37, this.item(Material.FIREWORK_STAR, "&fScintille", "&7FIREWORKS_SPARK"));
        inv.setItem(38, this.item(Material.END_ROD, "&fEnd Rod", "&7END_ROD"));
        inv.setItem(39, this.item(Material.AMETHYST_SHARD, "&dMagia viola", "&7CRIT_MAGIC"));
        inv.setItem(40, this.item(Material.ENDER_EYE, "&5Portale", "&7PORTAL"));
        inv.setItem(41, this.item(Material.TOTEM_OF_UNDYING, "&eTotem", "&7TOTEM"));
        inv.setItem(42, this.item(Material.SOUL_TORCH, "&bSoul Flame", "&7SOUL_FIRE_FLAME"));
        inv.setItem(43, this.item(Material.EMERALD, "&aHappy", "&7VILLAGER_HAPPY"));
        inv.setItem(49, this.item(Material.BARRIER, "&cIndietro", new String[0]));
        this.fill(inv);
        player.openInventory(inv);
    }

    private void openKey(Player player, Crate crate) {
        Inventory inv = this.plugin.getServer().createInventory(null, 27, KEY + crate.id);
        ItemStack pending = this.pendingKeyItems.get(player.getUniqueId());
        inv.setItem(4, this.plugin.keys().create(crate, 1, KeyMode.NORMAL));
        inv.setItem(10, this.item(Material.TRIPWIRE_HOOK, "&aPrendi key normale", "&7Click: ricevi 1 key", "&7Utilizzi: &f" + crate.keyUses));
        inv.setItem(11, this.item(Material.CHEST_MINECART, "&aPrendi 16 key normali", "&7Click: ricevi 16 key", "&7Utilizzi: &f" + crate.keyUses));
        inv.setItem(12, this.item(Material.EXPERIENCE_BOTTLE, "&cUtilizzi -1", "&7Attuale: &f" + crate.keyUses));
        inv.setItem(13, this.item(Material.EXPERIENCE_BOTTLE, "&aUtilizzi +1", "&7Attuale: &f" + crate.keyUses));
        inv.setItem(14, this.item(Material.REDSTONE_BLOCK, "&4Utilizzi -10", "&7Attuale: &f" + crate.keyUses));
        inv.setItem(15, this.item(Material.EMERALD_BLOCK, "&2Utilizzi +10", "&7Attuale: &f" + crate.keyUses));
        inv.setItem(16, this.item(crate.pickModeEnabled ? Material.CHEST_MINECART : Material.HOPPER_MINECART, "&dSelezione riquadri", "&7Attuale: &f" + (crate.pickModeEnabled ? "ON" : "OFF"), "&7OFF = apertura normale"));
        inv.setItem(19, this.item(Material.ENDER_EYE, "&bKey scegli 1", "&7Scegli un riquadro poi roulette"));
        inv.setItem(20, this.item(Material.NETHER_STAR, "&dKey scegli 2", "&7Scegli due premi poi roulette"));
        inv.setItem(22, this.item(Material.BOOK, "&eEditor item key", "&7Prendi un item dal tuo inventario", "&7clicca lo slot input, poi conferma"));
        inv.setItem(23, pending == null ? this.item(Material.HOPPER, "&bSlot input key", "&7Clicca qui con un item sul cursore") : pending);
        inv.setItem(24, this.item(Material.LIME_DYE, "&aConferma item key", "&7Applica l'item nello slot input"));
        inv.setItem(26, this.item(Material.BARRIER, "&cIndietro", "&7Torna alla lista key"));
        this.fill(inv);
        player.openInventory(inv);
    }

    void openPrizePicker(Player player, Location location, Crate crate, int picks) {
        Inventory inv = this.plugin.getServer().createInventory(null, 27, PICKER + crate.id);
        this.pickSessions.put(player.getUniqueId(), new PickSession(crate, location, Math.max(1, Math.min(26, picks))));
        for (int i = 0; i < 27; ++i) {
            inv.setItem(i, this.item(Material.MINECART, "&7Riquadro vuoto", "&eClicca per selezionare"));
        }
        inv.setItem(4, this.item(Material.CHEST, "&bScegli dei riquadri", "&7Poi rollano solo quelli scelti"));
        player.openInventory(inv);
    }

    private void openMessages(Player player, Crate crate) {
        Inventory inv = this.plugin.getServer().createInventory(null, 27, MESSAGES + crate.id);
        inv.setItem(11, this.item(Material.OAK_SIGN, "&cMessaggio senza key", crate.noKeyMessage));
        inv.setItem(13, this.item(Material.WRITABLE_BOOK, "&aMessaggio apertura", crate.openMessage));
        inv.setItem(15, this.item(Material.BOOK, "&eAltri messaggi", "&7Nel config.yml sezione messages"));
        this.fill(inv);
        player.openInventory(inv);
    }

    private void openPermissions(Player player, Crate crate) {
        Inventory inv = this.plugin.getServer().createInventory(null, 27, PERMS + crate.id);
        inv.setItem(11, this.item(Material.REDSTONE_TORCH, "&cPermesso apertura", "&e" + crate.permission));
        inv.setItem(13, this.item(Material.LIME_DYE, "&aEsempio LuckPerms", "&7/lp group vip permission set " + crate.permission + " true"));
        inv.setItem(15, this.item(Material.PAPER, "&fPermesso base", "&edadacrates.use"));
        this.fill(inv);
        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        HumanEntity humanEntity = event.getWhoClicked();
        if (!(humanEntity instanceof Player)) {
            return;
        }
        Player player = (Player)humanEntity;
        String title = event.getView().getTitle();
        String previewTitle = this.previewTitles.get(player.getUniqueId());
        if (previewTitle != null && previewTitle.equals(title)) {
            event.setCancelled(true);
            return;
        }
        if (!title.startsWith("DadaCrates")) {
            return;
        }
        if (!player.hasPermission("dadacrates.admin") && !title.startsWith(PICKER)) {
            event.setCancelled(true);
            player.closeInventory();
            return;
        }
        int slot = event.getRawSlot();
        int topSize = event.getView().getTopInventory().getSize();
        if (title.startsWith(REWARDS)) {
            Crate crate = this.crateFromTitle(title);
            if (crate == null) {
                return;
            }
            this.handleRewards(player, crate, slot, event);
            return;
        }
        if (slot >= topSize) {
            if (event.getClick().isShiftClick()) {
                event.setCancelled(true);
            }
            return;
        }
        event.setCancelled(true);
        if (title.equals(MAIN)) {
            if (slot == 10 || slot == 14) {
                this.openCrates(player);
            }
            if (slot == 12) {
                this.openKeys(player);
            }
            if (slot == 16) {
                this.plugin.store().reload();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            }
            return;
        }
        if (title.equals(LIST)) {
            ArrayList<Crate> crates = new ArrayList<Crate>(this.plugin.store().all());
            if (slot >= 0 && slot < crates.size()) {
                this.openEdit(player, (Crate)crates.get(slot));
            }
            return;
        }
        if (title.equals(KEYS_LIST)) {
            if (slot == 53) {
                this.openMain(player);
                return;
            }
            ArrayList<Crate> crates = new ArrayList<Crate>(this.plugin.store().all());
            if (slot >= 0 && slot < crates.size()) {
                this.openKey(player, (Crate)crates.get(slot));
            }
            return;
        }
        Crate crate = this.crateFromTitle(title);
        if (crate == null) {
            return;
        }
        if (title.startsWith(EDIT)) {
            if (slot == 10) {
                this.openBlocks(player, crate);
            }
            if (slot == 11) {
                this.openKey(player, crate);
            }
            if (slot == 12) {
                this.openRewards(player, crate);
            }
            if (slot == 13) {
                this.openAnimations(player, crate);
            }
            if (slot == 14) {
                this.openEffects(player, crate);
            }
            if (slot == 15) {
                this.openMessages(player, crate);
            }
            if (slot == 16) {
                this.openPermissions(player, crate);
            }
            if (slot == 25) {
                this.openIdleParticles(player, crate);
            }
            if (slot == 28) {
                this.openAnimationSettings(player, crate);
            }
            return;
        }
        if (title.startsWith(ANIMS) && slot >= 0 && slot < AnimationType.values().length) {
            crate.animation = AnimationType.values()[slot];
            this.set(crate, "animation", crate.animation.name());
            this.openEdit(player, crate);
            return;
        }
        if (title.startsWith(ANIM_SETTINGS)) {
            this.handleAnimationSettings(player, crate, slot, event);
            return;
        }
        if (title.startsWith(BLOCKS)) {
            this.handleBlocks(player, crate, slot, event);
            return;
        }
        if (title.startsWith(EFFECTS) && event.getCurrentItem() != null && event.getCurrentItem().hasItemMeta()) {
            String name = Text.plain(event.getCurrentItem().getItemMeta().getDisplayName());
            try {
                crate.particle = Particle.valueOf((String)name);
                this.set(crate, "particle", crate.particle.name());
                this.openEdit(player, crate);
            }
            catch (IllegalArgumentException illegalArgumentException) {
                // empty catch block
            }
            return;
        }
        if (title.startsWith(IDLE_PARTICLES)) {
            this.handleIdleParticles(player, crate, slot, event);
            return;
        }
        if (title.startsWith(KEY)) {
            this.handleKey(player, crate, slot, event);
            return;
        }
        if (title.startsWith(REWARD_EDIT)) {
            this.handleRewardEdit(player, crate, slot, event);
            return;
        }
        if (title.startsWith(PICKER)) {
            this.handlePicker(player, slot, event);
        }
    }

    private void handleBlocks(Player player, Crate crate, int slot, InventoryClickEvent event) {
        if (slot == 49) {
            ItemStack cursor = event.getCursor();
            if (cursor != null && !cursor.getType().isAir() && cursor.getType().isBlock()) {
                this.plugin.store().setBlock(crate, cursor.getType());
                player.sendMessage(this.plugin.store().prefix() + Text.color("&aBlocco crate salvato: &f" + String.valueOf(cursor.getType())));
                this.openEdit(player, crate);
            } else {
                player.sendMessage(this.plugin.store().prefix() + Text.color("&cMetti un blocco sul cursore e clicca salva."));
            }
            return;
        }
        ItemStack cursor = event.getCursor();
        if (cursor != null && !cursor.getType().isAir() && cursor.getType().isBlock() && slot >= 0 && slot < 45) {
            this.plugin.store().setBlock(crate, cursor.getType());
            player.sendMessage(this.plugin.store().prefix() + Text.color("&aBlocco crate salvato: &f" + String.valueOf(cursor.getType())));
            this.openEdit(player, crate);
            return;
        }
        if (slot == 53) {
            this.openEdit(player, crate);
            return;
        }
        ItemStack clicked = event.getCurrentItem();
        if (clicked != null && clicked.getType().isBlock()) {
            this.plugin.store().setBlock(crate, clicked.getType());
            this.openEdit(player, crate);
        }
    }

    private void handleKey(Player player, Crate crate, int slot, InventoryClickEvent event) {
        if (slot == 23) {
            ItemStack cursor = event.getCursor();
            if (cursor != null && !cursor.getType().isAir()) {
                ItemStack copy = cursor.clone();
                copy.setAmount(1);
                this.pendingKeyItems.put(player.getUniqueId(), copy);
                this.openKey(player, crate);
            } else {
                player.sendMessage(this.plugin.store().prefix() + Text.color("&cPrendi un item dal tuo inventario e clicca lo slot input."));
            }
            return;
        }
        if (slot == 24) {
            ItemStack pending = this.pendingKeyItems.remove(player.getUniqueId());
            if (pending != null && !pending.getType().isAir()) {
                this.plugin.store().setKeyFromItem(crate, pending);
                player.sendMessage(this.plugin.store().prefix() + Text.color("&aKey applicata e salvata."));
                this.openKey(player, crate);
            } else {
                player.sendMessage(this.plugin.store().prefix() + Text.color("&cPrima metti una key nello slot input."));
            }
            return;
        }
        if (slot == 12) {
            this.plugin.store().setKeyUses(crate, crate.keyUses - 1);
            this.openKey(player, crate);
            return;
        }
        if (slot == 13) {
            this.plugin.store().setKeyUses(crate, crate.keyUses + 1);
            this.openKey(player, crate);
            return;
        }
        if (slot == 14) {
            this.plugin.store().setKeyUses(crate, crate.keyUses - 10);
            this.openKey(player, crate);
            return;
        }
        if (slot == 15) {
            this.plugin.store().setKeyUses(crate, crate.keyUses + 10);
            this.openKey(player, crate);
            return;
        }
        if (slot == 16) {
            this.plugin.store().setPickModeEnabled(crate, !crate.pickModeEnabled);
            this.openKey(player, crate);
            return;
        }
        if (slot == 10) {
            player.getInventory().addItem(new ItemStack[]{this.plugin.keys().create(crate, 1, KeyMode.NORMAL)});
        }
        if (slot == 11) {
            player.getInventory().addItem(new ItemStack[]{this.plugin.keys().create(crate, 16, KeyMode.NORMAL)});
        }
        if (slot == 19) {
            player.getInventory().addItem(new ItemStack[]{this.plugin.keys().create(crate, 1, KeyMode.PICK_ONE)});
        }
        if (slot == 20) {
            player.getInventory().addItem(new ItemStack[]{this.plugin.keys().create(crate, 1, KeyMode.PICK_TWO)});
        }
        if (slot == 26) {
            this.openKeys(player);
        }
    }

    private void handleRewards(Player player, Crate crate, int slot, InventoryClickEvent event) {
        if (!player.hasPermission("dadacrates.admin")) {
            event.setCancelled(true);
            return;
        }
        int topSize = event.getView().getTopInventory().getSize();
        if (slot >= topSize) {
            return;
        }
        if (slot >= 0 && slot < 45) {
            if (event.getClick() == ClickType.RIGHT && (event.getCursor() == null || event.getCursor().getType().isAir()) && event.getCurrentItem() != null && !event.getCurrentItem().getType().isAir()) {
                Reward reward;
                event.setCancelled(true);
                this.saveRewardInventory(crate, event.getView().getTopInventory());
                Reward reward2 = reward = slot < crate.rewards.size() ? crate.rewards.get(slot) : null;
                if (reward != null) {
                    this.openRewardEdit(player, crate, reward);
                }
                return;
            }
            event.setCancelled(false);
            return;
        }
        if (slot == 53) {
            event.setCancelled(true);
            this.saveRewardInventory(crate, event.getView().getTopInventory());
            this.openEdit(player, crate);
            return;
        }
        event.setCancelled(true);
    }

    private void openRewardEdit(Player player, Crate crate, Reward reward) {
        Inventory inv = this.plugin.getServer().createInventory(null, 36, REWARD_EDIT + crate.id + ":" + reward.id());
        inv.setItem(4, reward.icon());
        inv.setItem(10, this.item(Material.LIME_DYE, "&aChance +1", "&7Attuale: &e" + reward.chance() + "%"));
        inv.setItem(11, this.item(Material.EMERALD_BLOCK, "&aChance +10", "&7Attuale: &e" + reward.chance() + "%"));
        inv.setItem(12, this.item(Material.RED_DYE, "&cChance -1", "&7Attuale: &e" + reward.chance() + "%"));
        inv.setItem(13, this.item(Material.REDSTONE_BLOCK, "&cChance -10", "&7Attuale: &e" + reward.chance() + "%"));
        inv.setItem(15, this.item(Material.HOPPER, "&bTipo", "&7Attuale: &f" + String.valueOf((Object)reward.type()), "&7Click alterna ITEM/COMMAND"));
        inv.setItem(16, this.item(Material.ANVIL, "&eQuantita +1", "&7Attuale: &f" + reward.amount()));
        inv.setItem(19, this.item(Material.DAMAGED_ANVIL, "&eQuantita -1", "&7Attuale: &f" + reward.amount()));
        inv.setItem(25, this.item(Material.COMMAND_BLOCK, "&dImposta comando", "&7Chiude la GUI e ti fa scrivere il comando in chat", "&7Usa %player% per il nome player", "&7Comandi: &f" + reward.commands().size()));
        inv.setItem(26, this.item(Material.BARRIER, "&cTogli comandi", "&7Rimuove i comandi", "&7e torna a dare l'oggetto"));
        inv.setItem(27, this.item(reward.broadcast() ? Material.BELL : Material.GRAY_DYE, "&6Messaggio globale", "&7Attuale: &f" + (reward.broadcast() ? "ON" : "OFF"), "&7Annuncia questo premio in chat"));
        inv.setItem(31, this.item(Material.BARRIER, "&cIndietro", new String[0]));
        inv.setItem(35, this.item(Material.TNT, "&4Elimina reward", new String[0]));
        this.fill(inv);
        player.openInventory(inv);
    }

    private void handleRewardEdit(Player player, Crate crate, int slot, InventoryClickEvent event) {
        Reward reward = this.rewardFromTitle(event.getView().getTitle(), crate);
        if (reward == null) {
            this.openRewards(player, crate);
            return;
        }
        if (slot == 10) {
            this.plugin.store().updateRewardChance(crate, reward, reward.chance() + 1.0);
        }
        if (slot == 11) {
            this.plugin.store().updateRewardChance(crate, reward, reward.chance() + 10.0);
        }
        if (slot == 12) {
            this.plugin.store().updateRewardChance(crate, reward, reward.chance() - 1.0);
        }
        if (slot == 13) {
            this.plugin.store().updateRewardChance(crate, reward, reward.chance() - 10.0);
        }
        if (slot == 15) {
            this.plugin.store().toggleRewardType(crate, reward);
        }
        if (slot == 16) {
            this.plugin.store().updateRewardAmount(crate, reward, reward.amount() + 1);
        }
        if (slot == 19) {
            this.plugin.store().updateRewardAmount(crate, reward, reward.amount() - 1);
        }
        if (slot == 25) {
            this.commandInputs.put(player.getUniqueId(), new CommandInputSession(crate.id, reward.id()));
            player.closeInventory();
            player.sendMessage(this.plugin.store().prefix() + Text.color("&eScrivi in chat il comando senza / oppure &ccancel&e."));
            player.sendMessage(this.plugin.store().prefix() + Text.color("&7Esempio: eco give %player% 1000"));
            return;
        }
        if (slot == 26) {
            this.plugin.store().clearRewardCommands(crate, reward);
            player.sendMessage(this.plugin.store().prefix() + Text.color("&aComandi rimossi. Ora questo premio da l'oggetto."));
            Reward updated = this.rewardFromId(crate, reward.id());
            this.openRewardEdit(player, crate, updated == null ? reward : updated);
            return;
        }
        if (slot == 27) {
            this.plugin.store().toggleRewardBroadcast(crate, reward);
            Reward updated = this.rewardFromId(crate, reward.id());
            this.openRewardEdit(player, crate, updated == null ? reward : updated);
            return;
        }
        if (slot == 35) {
            this.plugin.store().removeReward(crate, reward);
            this.openRewards(player, crate);
            return;
        }
        if (slot == 31) {
            this.openRewards(player, crate);
            return;
        }
        Reward updated = this.rewardFromId(crate, reward.id());
        this.openRewardEdit(player, crate, updated == null ? reward : updated);
    }

    private void handlePicker(Player player, int slot, InventoryClickEvent event) {
        PickSession session = this.pickSessions.get(player.getUniqueId());
        if (session == null || slot < 0 || slot >= event.getView().getTopInventory().getSize()) {
            return;
        }
        if (slot == 4 || session.rolling) {
            return;
        }
        if (session.claimReady) {
            Reward reward = session.rewards.remove(slot);
            if (reward != null) {
                this.plugin.award(player, session.crate, reward);
                event.getView().getTopInventory().setItem(slot, this.item(Material.MINECART, "&7Ritirato", new String[0]));
                if (session.rewards.isEmpty()) {
                    this.pickSessions.remove(player.getUniqueId());
                    this.unlock(player);
                    this.plugin.getServer().getScheduler().runTaskLater((Plugin)this.plugin, () -> player.closeInventory(), 10L);
                }
            }
            return;
        }
        if (session.selectedSlots.contains(slot)) {
            return;
        }
        session.selectedSlots.add(slot);
        event.getView().getTopInventory().setItem(slot, this.item(Material.CHEST_MINECART, "&aSelezionato", "&7Questo riquadro rollera"));
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.4f);
        if (session.selectedSlots.size() >= session.required) {
            if (!this.consumeOneKey(player, session.crate)) {
                this.pickSessions.remove(player.getUniqueId());
                player.closeInventory();
                player.sendMessage(this.plugin.store().prefix() + Text.color("&cKey non trovata nel tuo inventario. Apertura annullata."));
                return;
            }
            this.startPickerRoll(player, event.getView().getTopInventory(), session);
        }
    }

    private boolean consumeOneKey(Player player, Crate crate) {
        ItemStack[] contents;
        ItemStack main = player.getInventory().getItemInMainHand();
        if (this.plugin.keys().isKey(main, crate)) {
            this.plugin.keys().consumeOne(main);
            return true;
        }
        for (ItemStack item : contents = player.getInventory().getContents()) {
            if (!this.plugin.keys().isKey(item, crate)) continue;
            this.plugin.keys().consumeOne(item);
            return true;
        }
        return false;
    }

    private void startPickerRoll(final Player player, final Inventory inv, final PickSession session) {
        session.rolling = true;
        this.lock(player, inv);
        for (int i = 0; i < inv.getSize(); ++i) {
            if (i == 4 || session.selectedSlots.contains(i)) continue;
            inv.setItem(i, this.item(Material.HOPPER_MINECART, "&8Non selezionato", new String[0]));
        }
        final List rewards = session.crate.rewards.isEmpty() ? List.of() : session.crate.rewards;
        new BukkitRunnable(){
            int step = 0;

            public void run() {
                for (int slot : session.selectedSlots) {
                    if (rewards.isEmpty()) continue;
                    inv.setItem(slot, GuiManager.this.playerRewardIcon((Reward)rewards.get((this.step + slot) % rewards.size()), null));
                }
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 0.8f + (float)this.step * 0.03f);
                if (this.step++ >= session.crate.animationDuration) {
                    this.cancel();
                    session.rolling = false;
                    session.claimReady = true;
                    for (int slot : session.selectedSlots) {
                        Reward reward = GuiManager.this.plugin.store().roll(session.crate);
                        if (reward == null) continue;
                        session.rewards.put(slot, reward);
                        inv.setItem(slot, GuiManager.this.playerRewardIcon(reward, "&aClicca per reclamare"));
                    }
                    inv.setItem(4, GuiManager.this.item(Material.EMERALD, "&aClicca i premi per reclamarli", "&7Ritira tutti gli oggetti trovati"));
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, (long)Math.max(1, session.crate.animationSpeed));
    }

    private void handleAnimationSettings(Player player, Crate crate, int slot, InventoryClickEvent event) {
        if (slot == 10) {
            crate.animationDuration = Math.max(5, crate.animationDuration + (event.isRightClick() ? -5 : 5));
            this.set(crate, "animation-settings.duration-steps", crate.animationDuration);
        }
        if (slot == 12) {
            crate.animationSpeed = Math.max(1, crate.animationSpeed + (event.isRightClick() ? -1 : 1));
            this.set(crate, "animation-settings.speed-ticks", crate.animationSpeed);
        }
        if (slot == 14) {
            crate.animationGuiSize = this.nextGuiSize(crate.animationGuiSize);
            this.set(crate, "animation-settings.gui-size", crate.animationGuiSize);
        }
        if (slot == 16) {
            crate.animationFirework = !crate.animationFirework;
            this.set(crate, "animation-settings.finish-firework", crate.animationFirework);
        }
        if (slot == 31) {
            this.openEdit(player, crate);
            return;
        }
        this.openAnimationSettings(player, crate);
    }

    private void handleIdleParticles(Player player, Crate crate, int slot, InventoryClickEvent event) {
        int deltaColor;
        if (slot >= 0 && slot < ParticleShape.values().length) {
            this.plugin.store().setIdleParticleShape(crate, ParticleShape.values()[slot]);
            this.plugin.idleParticles().start();
            this.openIdleParticles(player, crate);
            return;
        }
        int n = deltaColor = event.isRightClick() ? -15 : 15;
        if (slot == 27) {
            this.plugin.store().setIdleParticleColor(crate, crate.idleParticleRed + deltaColor, crate.idleParticleGreen, crate.idleParticleBlue);
        }
        if (slot == 28) {
            this.plugin.store().setIdleParticleColor(crate, crate.idleParticleRed, crate.idleParticleGreen + deltaColor, crate.idleParticleBlue);
        }
        if (slot == 29) {
            this.plugin.store().setIdleParticleColor(crate, crate.idleParticleRed, crate.idleParticleGreen, crate.idleParticleBlue + deltaColor);
        }
        if (slot == 31) {
            this.plugin.store().setIdleParticleSize(crate, crate.idleParticleSize + (event.isRightClick() ? -0.1f : 0.1f));
        }
        if (slot == 32) {
            this.plugin.store().setIdleParticleRadius(crate, crate.idleParticleRadius + (event.isRightClick() ? -0.1 : 0.1));
        }
        if (slot == 33) {
            this.plugin.store().setIdleParticlePoints(crate, crate.idleParticlePoints + (event.isRightClick() ? -2 : 2));
        }
        if (slot == 34) {
            this.plugin.store().setIdleParticleHeight(crate, crate.idleParticleHeight + (event.isRightClick() ? -0.1 : 0.1));
        }
        if (slot == 36) {
            this.plugin.store().setIdleParticle(crate, Particle.REDSTONE);
        }
        if (slot == 37) {
            this.plugin.store().setIdleParticle(crate, Particle.FIREWORKS_SPARK);
        }
        if (slot == 38) {
            this.plugin.store().setIdleParticle(crate, Particle.END_ROD);
        }
        if (slot == 39) {
            this.plugin.store().setIdleParticle(crate, Particle.CRIT_MAGIC);
        }
        if (slot == 40) {
            this.plugin.store().setIdleParticle(crate, Particle.PORTAL);
        }
        if (slot == 41) {
            this.plugin.store().setIdleParticle(crate, Particle.TOTEM);
        }
        if (slot == 42) {
            this.plugin.store().setIdleParticle(crate, Particle.SOUL_FIRE_FLAME);
        }
        if (slot == 43) {
            this.plugin.store().setIdleParticle(crate, Particle.VILLAGER_HAPPY);
        }
        if (slot == 49) {
            this.openEdit(player, crate);
            return;
        }
        this.plugin.idleParticles().start();
        this.openIdleParticles(player, crate);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        HumanEntity humanEntity = event.getWhoClicked();
        if (!(humanEntity instanceof Player)) {
            return;
        }
        Player player = (Player)humanEntity;
        String title = event.getView().getTitle();
        if (!title.startsWith("DadaCrates") || !player.hasPermission("dadacrates.admin")) {
            return;
        }
        Crate crate = this.crateFromTitle(title);
        if (crate == null) {
            return;
        }
        ItemStack dragged = event.getOldCursor();
        if (dragged == null || dragged.getType().isAir()) {
            return;
        }
        boolean topInventory = event.getRawSlots().stream().anyMatch(slot -> slot < event.getView().getTopInventory().getSize());
        if (!topInventory) {
            return;
        }
        event.setCancelled(true);
        if (title.startsWith(KEY)) {
            this.plugin.store().setKeyFromItem(crate, dragged.clone());
            player.sendMessage(this.plugin.store().prefix() + Text.color("&aKey salvata trascinandola nella GUI."));
            this.openKey(player, crate);
        } else if (title.startsWith(REWARDS)) {
            this.plugin.store().addRewardFromItem(crate, dragged.clone());
            player.sendMessage(this.plugin.store().prefix() + Text.color("&aReward aggiunto trascinandolo nella GUI."));
            this.openRewards(player, crate);
        } else if (title.startsWith(BLOCKS) && dragged.getType().isBlock()) {
            this.plugin.store().setBlock(crate, dragged.getType());
            player.sendMessage(this.plugin.store().prefix() + Text.color("&aBlocco crate salvato trascinandolo nella GUI."));
            this.openEdit(player, crate);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        Inventory locked;
        Crate crate;
        HumanEntity humanEntity = event.getPlayer();
        if (!(humanEntity instanceof Player)) {
            return;
        }
        Player player = (Player)humanEntity;
        String title = event.getView().getTitle();
        String previewTitle = this.previewTitles.get(player.getUniqueId());
        if (previewTitle != null && previewTitle.equals(title)) {
            this.previewTitles.remove(player.getUniqueId());
        }
        if (title.startsWith(REWARDS) && player.hasPermission("dadacrates.admin") && (crate = this.crateFromTitle(title)) != null) {
            this.saveRewardInventory(crate, event.getInventory());
            player.sendMessage(this.plugin.store().prefix() + Text.color("&aReward salvati."));
        }
        if ((locked = this.lockedInventories.get(player.getUniqueId())) == null) {
            return;
        }
        this.plugin.getServer().getScheduler().runTask((Plugin)this.plugin, () -> {
            if (this.lockedInventories.containsKey(player.getUniqueId()) && player.isOnline()) {
                player.openInventory(locked);
            }
        });
    }

    private void saveRewardInventory(Crate crate, Inventory inventory) {
        ArrayList<ItemStack> items = new ArrayList<ItemStack>();
        for (int i = 0; i < Math.min(45, inventory.getSize()); ++i) {
            ItemStack item = inventory.getItem(i);
            if (item == null || item.getType().isAir()) {
                items.add(null);
                continue;
            }
            items.add(item.clone());
        }
        this.plugin.store().replaceRewardsFromItems(crate, items);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        CommandInputSession input = this.commandInputs.remove(event.getPlayer().getUniqueId());
        if (input == null) {
            return;
        }
        event.setCancelled(true);
        this.plugin.getServer().getScheduler().runTask((Plugin)this.plugin, () -> {
            Reward updated;
            Player player = event.getPlayer();
            Crate crate = this.plugin.store().byId(input.crateId).orElse(null);
            if (crate == null) {
                return;
            }
            Reward reward = this.rewardFromId(crate, input.rewardId);
            if (reward == null) {
                this.openRewards(player, crate);
                return;
            }
            if (!event.getMessage().equalsIgnoreCase("cancel")) {
                this.plugin.store().updateRewardCommands(crate, reward, List.of(event.getMessage()));
                player.sendMessage(this.plugin.store().prefix() + Text.color("&aComando salvato."));
            }
            this.openRewardEdit(player, crate, (updated = this.rewardFromId(crate, input.rewardId)) == null ? reward : updated);
        });
    }

    private void set(Crate crate, String field, Object value) {
        this.plugin.getConfig().set("crates." + crate.id + "." + field, value);
        this.plugin.store().save();
    }

    private Crate crateFromTitle(String title) {
        if (title.startsWith(REWARD_EDIT) && title.contains(":")) {
            String rest = title.substring(REWARD_EDIT.length());
            return this.plugin.store().byId(rest.substring(0, rest.indexOf(58))).orElse(null);
        }
        String id = title.substring(title.lastIndexOf(32) + 1);
        return this.plugin.store().byId(id).orElse(null);
    }

    private Reward rewardFromTitle(String title, Crate crate) {
        if (!title.contains(":")) {
            return null;
        }
        return this.rewardFromId(crate, title.substring(title.indexOf(58) + 1));
    }

    private Reward rewardFromId(Crate crate, String id) {
        return crate.rewards.stream().filter(reward -> reward.id().equals(id)).findFirst().orElse(null);
    }

    private ItemStack item(Material material, String name, String ... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Text.color(name));
            meta.setLore(Arrays.stream(lore).map(Text::color).toList());
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack rewardIcon(Reward reward) {
        return this.rewardIcon(reward, "&7Click per modificare");
    }

    private ItemStack previewIcon(Reward reward) {
        ItemStack icon = reward.itemReward();
        ItemMeta meta = icon.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Text.color(reward.name()));
            ArrayList<String> lore = new ArrayList<String>(Text.color(reward.lore()));
            lore.add(Text.color("&7Percentuale: &e" + reward.chance() + "%"));
            meta.setLore(lore);
            icon.setItemMeta(meta);
        }
        return icon;
    }

    private String centeredPreviewTitle(Crate crate) {
        String name = Text.plain(crate.displayName).isBlank() ? crate.id : Text.plain(crate.displayName);
        return Text.color("&f" + name);
    }

    private ItemStack playerRewardIcon(Reward reward, String lastLine) {
        ItemStack icon = reward.itemReward();
        ItemMeta meta = icon.getItemMeta();
        if (meta != null) {
            ArrayList<String> lore;
            if (!meta.hasDisplayName()) {
                meta.setDisplayName(Text.color(reward.name()));
            }
            ArrayList<String> arrayList = lore = meta.hasLore() ? new ArrayList<String>(meta.getLore()) : new ArrayList<String>(Text.color(reward.lore()));
            if (lastLine != null && !lastLine.isBlank()) {
                lore.add(Text.color(lastLine));
            }
            meta.setLore(lore);
            icon.setItemMeta(meta);
        }
        return icon;
    }

    private ItemStack rewardIcon(Reward reward, String actionLine) {
        ItemStack icon = reward.icon();
        ItemMeta meta = icon.getItemMeta();
        if (meta != null) {
            meta.setLore(List.of(Text.color("&7Chance: &e" + reward.chance() + "%"), Text.color("&7Tipo: &f" + String.valueOf((Object)reward.type())), Text.color(actionLine), Text.color("&7Sinistro: &a+1%"), Text.color("&7Destro: &c-1%"), Text.color("&7Shift destro: &cElimina")));
            icon.setItemMeta(meta);
        }
        return icon;
    }

    private int nextGuiSize(int current) {
        return switch (current) {
            case 9 -> 18;
            case 18 -> 27;
            case 27 -> 36;
            case 36 -> 45;
            case 45 -> 54;
            default -> 9;
        };
    }

    private void fill(Inventory inv) {
        ItemStack filler = this.item(Material.GRAY_STAINED_GLASS_PANE, " ", new String[0]);
        for (int i = 0; i < inv.getSize(); ++i) {
            if (inv.getItem(i) != null) continue;
            inv.setItem(i, filler);
        }
    }

    private static final class PickSession {
        final Crate crate;
        final Location location;
        final int required;
        final List<Integer> selectedSlots = new ArrayList<Integer>();
        final Map<Integer, Reward> rewards = new HashMap<Integer, Reward>();
        boolean rolling;
        boolean claimReady;

        PickSession(Crate crate, Location location, int required) {
            this.crate = crate;
            this.location = location;
            this.required = required;
        }
    }

    private record CommandInputSession(String crateId, String rewardId) {
    }
}
