/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.ChatColor
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.Particle
 *  org.bukkit.configuration.ConfigurationSection
 *  org.bukkit.configuration.file.FileConfiguration
 *  org.bukkit.configuration.file.YamlConfiguration
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 */
package it.dadasmp.dadacrates;

import it.dadasmp.dadacrates.AnimationType;
import it.dadasmp.dadacrates.Crate;
import it.dadasmp.dadacrates.DadaCratesPlugin;
import it.dadasmp.dadacrates.ParticleShape;
import it.dadasmp.dadacrates.Reward;
import it.dadasmp.dadacrates.RewardType;
import it.dadasmp.dadacrates.Text;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

final class CrateStore {
    private final DadaCratesPlugin plugin;
    private final Random random = new Random();
    private final Map<String, Crate> crates = new LinkedHashMap<String, Crate>();
    private File locationsFile;
    private File hologramsFile;
    private FileConfiguration locationsConfig;
    private FileConfiguration hologramsConfig;

    CrateStore(DadaCratesPlugin plugin) {
        this.plugin = plugin;
    }

    void reload() {
        this.plugin.reloadConfig();
        this.loadExtraFiles();
        this.crates.clear();
        ConfigurationSection root = this.plugin.getConfig().getConfigurationSection("crates");
        if (root == null) {
            return;
        }
        for (String id : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(id);
            if (section == null) continue;
            this.crates.put(id.toLowerCase(Locale.ROOT), this.loadCrate(id, section));
        }
        this.ensurePresetCrates();
        this.saveExtraFiles();
    }

    void save() {
        this.plugin.saveConfig();
        this.saveExtraFiles();
    }

    private void loadExtraFiles() {
        this.locationsFile = new File(this.plugin.getDataFolder(), "locations.yml");
        this.hologramsFile = new File(this.plugin.getDataFolder(), "holograms.yml");
        this.locationsConfig = YamlConfiguration.loadConfiguration((File)this.locationsFile);
        this.hologramsConfig = YamlConfiguration.loadConfiguration((File)this.hologramsFile);
    }

    private void saveExtraFiles() {
        try {
            if (this.locationsConfig != null) {
                this.locationsConfig.save(this.locationsFile);
            }
            if (this.hologramsConfig != null) {
                this.hologramsConfig.save(this.hologramsFile);
            }
        }
        catch (IOException ex) {
            this.plugin.getLogger().warning("Impossibile salvare locations.yml/holograms.yml: " + ex.getMessage());
        }
    }

    Collection<Crate> all() {
        return this.crates.values();
    }

    Optional<Crate> byId(String id) {
        return Optional.ofNullable(this.crates.get(String.valueOf(id).toLowerCase(Locale.ROOT)));
    }

    Optional<Crate> byLocation(Location location) {
        String serialized = this.serialize(location);
        return this.crates.values().stream().filter(crate -> crate.locations.contains(serialized)).findFirst();
    }

    Crate create(String id) {
        Crate crate = new Crate(id.toLowerCase(Locale.ROOT));
        this.crates.put(crate.id, crate);
        this.writeCrate(crate);
        this.save();
        return crate;
    }

    boolean delete(String id) {
        Crate removed = this.crates.remove(String.valueOf(id).toLowerCase(Locale.ROOT));
        if (removed == null) {
            return false;
        }
        this.plugin.getConfig().set("crates." + removed.id, null);
        if (this.locationsConfig != null) {
            this.locationsConfig.set("crates." + removed.id, null);
        }
        if (this.hologramsConfig != null) {
            this.hologramsConfig.set("crates." + removed.id, null);
        }
        this.save();
        this.plugin.holograms().refresh();
        return true;
    }

    void addLocation(Crate crate, Location location) {
        String value = this.serialize(location);
        if (!crate.locations.contains(value)) {
            crate.locations.add(value);
            this.writeCrate(crate);
            this.save();
            this.plugin.holograms().refresh();
        }
    }

    boolean removeLocation(Location location) {
        String value = this.serialize(location);
        for (Crate crate : this.crates.values()) {
            if (!crate.locations.remove(value)) continue;
            this.writeCrate(crate);
            this.save();
            this.plugin.holograms().refresh();
            return true;
        }
        return false;
    }

    void setBlock(Crate crate, Material material) {
        crate.blockMaterial = material;
        this.writeCrate(crate);
        this.save();
    }

    void setKeyFromItem(Crate crate, ItemStack item) {
        crate.keyMaterial = item.getType();
        crate.keyItem = item.clone();
        crate.keyItem.setAmount(1);
        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            crate.keyName = meta.hasDisplayName() ? meta.getDisplayName().replace('\u00a7', '&') : "&eKey " + crate.id;
            crate.keyLore = meta.hasLore() ? meta.getLore().stream().map(line -> line.replace('\u00a7', '&')).toList() : List.of("&7Key per aprire " + crate.id);
        } else {
            crate.keyName = "&eKey " + crate.id;
            crate.keyLore = List.of("&7Key per aprire " + crate.id);
        }
        this.writeCrate(crate);
        this.save();
        this.plugin.holograms().refresh();
    }

    Reward addRewardFromItem(Crate crate, ItemStack item) {
        String id = "reward_" + System.currentTimeMillis();
        Reward reward = this.rewardFromItem(id, item, 10.0);
        crate.rewards.add(reward);
        this.writeCrate(crate);
        this.save();
        return reward;
    }

    void replaceRewardsFromItems(Crate crate, List<ItemStack> items) {
        ArrayList<Reward> oldRewards = new ArrayList<Reward>(crate.rewards);
        crate.rewards.clear();
        int index = 0;
        for (ItemStack item : items) {
            if (item == null || item.getType().isAir()) {
                ++index;
                continue;
            }
            Reward old = index < oldRewards.size() ? (Reward)oldRewards.get(index) : null;
            Reward reward = this.rewardFromItem((String)(old == null ? "reward_" + System.currentTimeMillis() + "_" + index : old.id()), item, old == null ? 10.0 : old.chance());
            if (old != null) {
                reward = new Reward(reward.id(), reward.name(), reward.lore(), old.chance(), old.type(), reward.material(), reward.amount(), reward.item(), old.commands(), old.broadcast(), old.broadcastMessage());
            }
            crate.rewards.add(reward);
            ++index;
        }
        this.writeCrate(crate);
        this.save();
    }

    private Reward rewardFromItem(String id, ItemStack item, double chance) {
        String name = item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName().replace('\u00a7', '&') : "&f" + item.getType().name();
        List<String> lore = item.hasItemMeta() && item.getItemMeta().hasLore() ? item.getItemMeta().getLore().stream().map(line -> line.replace('\u00a7', '&')).toList() : List.of();
        return new Reward(id, name, lore, chance, RewardType.ITEM, item.getType(), item.getAmount(), item.clone(), List.of(), false, "&b%player% &7ha trovato &f%reward% &7da &b%crate%");
    }

    void removeReward(Crate crate, Reward reward) {
        crate.rewards.remove(reward);
        this.plugin.getConfig().set("crates." + crate.id + ".rewards." + reward.id(), null);
        this.writeCrate(crate);
        this.save();
    }

    void updateRewardChance(Crate crate, Reward oldReward, double chance) {
        int index = crate.rewards.indexOf(oldReward);
        if (index < 0) {
            return;
        }
        Reward updated = this.copy(oldReward, Math.max(0.1, chance), oldReward.type(), oldReward.amount(), oldReward.commands(), oldReward.broadcast(), oldReward.broadcastMessage());
        crate.rewards.set(index, updated);
        this.writeCrate(crate);
        this.save();
    }

    void updateRewardAmount(Crate crate, Reward oldReward, int amount) {
        int index = crate.rewards.indexOf(oldReward);
        if (index < 0) {
            return;
        }
        Reward updated = this.copy(oldReward, oldReward.chance(), oldReward.type(), Math.max(1, Math.min(64, amount)), oldReward.commands(), oldReward.broadcast(), oldReward.broadcastMessage());
        crate.rewards.set(index, updated);
        this.writeCrate(crate);
        this.save();
    }

    void toggleRewardType(Crate crate, Reward oldReward) {
        int index = crate.rewards.indexOf(oldReward);
        if (index < 0) {
            return;
        }
        RewardType type = oldReward.type() == RewardType.ITEM ? RewardType.COMMAND : RewardType.ITEM;
        Reward updated = this.copy(oldReward, oldReward.chance(), type, oldReward.amount(), oldReward.commands(), oldReward.broadcast(), oldReward.broadcastMessage());
        crate.rewards.set(index, updated);
        this.writeCrate(crate);
        this.save();
    }

    void updateRewardCommands(Crate crate, Reward oldReward, List<String> commands) {
        int index = crate.rewards.indexOf(oldReward);
        if (index < 0) {
            return;
        }
        Reward updated = this.copy(oldReward, oldReward.chance(), RewardType.COMMAND, oldReward.amount(), commands, oldReward.broadcast(), oldReward.broadcastMessage());
        crate.rewards.set(index, updated);
        this.writeCrate(crate);
        this.save();
    }

    void clearRewardCommands(Crate crate, Reward oldReward) {
        int index = crate.rewards.indexOf(oldReward);
        if (index < 0) {
            return;
        }
        Reward updated = this.copy(oldReward, oldReward.chance(), RewardType.ITEM, oldReward.amount(), List.of(), oldReward.broadcast(), oldReward.broadcastMessage());
        crate.rewards.set(index, updated);
        this.writeCrate(crate);
        this.save();
    }

    void toggleRewardBroadcast(Crate crate, Reward oldReward) {
        int index = crate.rewards.indexOf(oldReward);
        if (index < 0) {
            return;
        }
        Reward updated = this.copy(oldReward, oldReward.chance(), oldReward.type(), oldReward.amount(), oldReward.commands(), !oldReward.broadcast(), oldReward.broadcastMessage());
        crate.rewards.set(index, updated);
        this.writeCrate(crate);
        this.save();
    }

    private Reward copy(Reward reward, double chance, RewardType type, int amount, List<String> commands, boolean broadcast, String broadcastMessage) {
        ItemStack item;
        ItemStack itemStack = item = reward.item() == null ? null : reward.item().clone();
        if (item != null && !item.getType().isAir()) {
            item.setAmount(Math.max(1, Math.min(64, amount)));
        }
        return new Reward(reward.id(), reward.name(), reward.lore(), chance, type, reward.material(), amount, item, commands, broadcast, broadcastMessage);
    }

    void setKeyUses(Crate crate, int uses) {
        crate.keyUses = Math.max(1, Math.min(999, uses));
        this.writeCrate(crate);
        this.save();
    }

    void setPickModeEnabled(Crate crate, boolean enabled) {
        crate.pickModeEnabled = enabled;
        this.writeCrate(crate);
        this.save();
    }

    void setIdleParticleShape(Crate crate, ParticleShape shape) {
        crate.idleParticleShape = shape;
        this.writeCrate(crate);
        this.save();
    }

    void setIdleParticleColor(Crate crate, int red, int green, int blue) {
        crate.idleParticle = Particle.REDSTONE;
        crate.idleParticleRed = this.clampColor(red);
        crate.idleParticleGreen = this.clampColor(green);
        crate.idleParticleBlue = this.clampColor(blue);
        this.writeCrate(crate);
        this.save();
    }

    void setIdleParticle(Crate crate, Particle particle) {
        crate.idleParticle = particle;
        this.writeCrate(crate);
        this.save();
    }

    void setIdleParticleSize(Crate crate, float size) {
        crate.idleParticleSize = Math.max(0.2f, Math.min(4.0f, size));
        this.writeCrate(crate);
        this.save();
    }

    void setIdleParticleRadius(Crate crate, double radius) {
        crate.idleParticleRadius = Math.max(0.1, Math.min(3.0, radius));
        this.writeCrate(crate);
        this.save();
    }

    void setIdleParticleHeight(Crate crate, double height) {
        crate.idleParticleHeight = Math.max(-64.0, Math.min(64.0, height));
        this.writeCrate(crate);
        this.save();
    }

    void setIdleParticlePoints(Crate crate, int points) {
        crate.idleParticlePoints = Math.max(6, Math.min(48, points));
        this.writeCrate(crate);
        this.save();
    }

    Reward roll(Crate crate) {
        double total = crate.rewards.stream().mapToDouble(Reward::chance).sum();
        if (total <= 0.0 || crate.rewards.isEmpty()) {
            return null;
        }
        double pick = this.random.nextDouble() * total;
        double current = 0.0;
        for (Reward reward : crate.rewards) {
            if (!(pick <= (current += reward.chance()))) continue;
            return reward;
        }
        return crate.rewards.get(crate.rewards.size() - 1);
    }

    String msg(String path) {
        return Text.color(this.plugin.getConfig().getString("messages." + path, "&cMessaggio mancante: " + path));
    }

    String prefix() {
        return Text.color(this.plugin.getConfig().getString("settings.prefix", "&f\ue00a &8\u00bb "));
    }

    private Crate loadCrate(String id, ConfigurationSection section) {
        ConfigurationSection rewards;
        ConfigurationSection animationSettings;
        ConfigurationSection externalHologram;
        Crate crate = new Crate(id);
        crate.displayName = section.getString("display-name", crate.displayName);
        crate.keyId = section.getString("key-id", crate.keyId);
        crate.animation = AnimationType.parse(section.getString("animation", crate.animation.name()));
        crate.blockMaterial = this.material(section.getString("block-material"), crate.blockMaterial);
        crate.particle = this.particle(section.getString("particle"), crate.particle);
        ConfigurationSection idleParticles = section.getConfigurationSection("idle-particles");
        if (idleParticles != null) {
            crate.idleParticleShape = ParticleShape.parse(idleParticles.getString("shape", crate.idleParticleShape.name()));
            crate.idleParticle = this.particle(idleParticles.getString("particle"), crate.idleParticle);
            crate.idleParticleRed = this.clampColor(idleParticles.getInt("color.red", crate.idleParticleRed));
            crate.idleParticleGreen = this.clampColor(idleParticles.getInt("color.green", crate.idleParticleGreen));
            crate.idleParticleBlue = this.clampColor(idleParticles.getInt("color.blue", crate.idleParticleBlue));
            crate.idleParticleSize = (float)idleParticles.getDouble("color.size", (double)crate.idleParticleSize);
            crate.idleParticleRadius = idleParticles.getDouble("radius", crate.idleParticleRadius);
            crate.idleParticleHeight = idleParticles.getDouble("height", crate.idleParticleHeight);
            crate.idleParticlePoints = Math.max(6, Math.min(48, idleParticles.getInt("points", crate.idleParticlePoints)));
        }
        crate.permission = section.getString("permission", "dadacrates.open." + id.toLowerCase(Locale.ROOT));
        crate.noKeyMessage = section.getString("no-key-message", crate.noKeyMessage);
        crate.openMessage = section.getString("open-message", crate.openMessage);
        ConfigurationSection hologram = section.getConfigurationSection("hologram");
        if (hologram != null) {
            crate.hologramEnabled = hologram.getBoolean("enabled", crate.hologramEnabled);
            crate.hologramHeight = hologram.getDouble("height", crate.hologramHeight);
            crate.hologramOffsetX = hologram.getDouble("offset-x", crate.hologramOffsetX);
            crate.hologramOffsetY = hologram.getDouble("offset-y", crate.hologramOffsetY);
            crate.hologramOffsetZ = hologram.getDouble("offset-z", crate.hologramOffsetZ);
            List lines = hologram.getStringList("lines");
            if (!lines.isEmpty()) {
                crate.hologramLines = lines;
            }
        }
        ConfigurationSection configurationSection = externalHologram = this.hologramsConfig == null ? null : this.hologramsConfig.getConfigurationSection("crates." + id);
        if (externalHologram != null) {
            crate.hologramEnabled = externalHologram.getBoolean("enabled", crate.hologramEnabled);
            crate.hologramHeight = externalHologram.getDouble("height", crate.hologramHeight);
            crate.hologramOffsetX = externalHologram.getDouble("offset-x", crate.hologramOffsetX);
            crate.hologramOffsetY = externalHologram.getDouble("offset-y", crate.hologramOffsetY);
            crate.hologramOffsetZ = externalHologram.getDouble("offset-z", crate.hologramOffsetZ);
            List lines = externalHologram.getStringList("lines");
            if (!lines.isEmpty()) {
                crate.hologramLines = lines;
            }
        }
        if ((animationSettings = section.getConfigurationSection("animation-settings")) != null) {
            crate.animationDuration = animationSettings.getInt("duration-steps", crate.animationDuration);
            crate.animationSpeed = animationSettings.getInt("speed-ticks", crate.animationSpeed);
            crate.animationGuiSize = this.normalizeGuiSize(animationSettings.getInt("gui-size", crate.animationGuiSize));
            crate.animationCloseDelay = animationSettings.getInt("close-delay-ticks", crate.animationCloseDelay);
            crate.animationFirework = animationSettings.getBoolean("finish-firework", crate.animationFirework);
            crate.animationTitle = animationSettings.getString("title", crate.animationTitle);
            crate.animationSound = animationSettings.getString("tick-sound", crate.animationSound);
        }
        List externalLocations = this.locationsConfig == null ? List.of() : this.locationsConfig.getStringList("crates." + id);
        crate.locations.addAll(externalLocations.isEmpty() ? section.getStringList("locations") : externalLocations);
        this.writeExternalCrateFiles(crate);
        ConfigurationSection key = section.getConfigurationSection("key");
        if (key != null) {
            crate.keyItem = key.getItemStack("item");
            crate.keyMaterial = this.material(key.getString("material"), crate.keyMaterial);
            crate.keyName = key.getString("name", crate.keyName);
            crate.keyLore = key.getStringList("lore");
            crate.keyUses = Math.max(1, key.getInt("uses", crate.keyUses));
            crate.pickModeEnabled = key.getBoolean("pick-mode-enabled", crate.pickModeEnabled);
        }
        if ((rewards = section.getConfigurationSection("rewards")) != null) {
            for (String rewardId : rewards.getKeys(false)) {
                ConfigurationSection reward = rewards.getConfigurationSection(rewardId);
                if (reward == null) continue;
                RewardType type = RewardType.valueOf(reward.getString("type", "ITEM").toUpperCase(Locale.ROOT));
                ItemStack rewardItem = reward.getItemStack("item");
                rewardItem = this.cleanStoredRewardItem(rewardItem);
                Material rewardMaterial = this.material(reward.getString("material"), rewardItem == null ? Material.CHEST : rewardItem.getType());
                int rewardAmount = reward.contains("amount") ? reward.getInt("amount", rewardItem == null ? 1 : rewardItem.getAmount()) : (rewardItem == null ? 1 : rewardItem.getAmount());
                crate.rewards.add(new Reward(rewardId, reward.getString("name", rewardId), reward.getStringList("lore"), reward.getDouble("chance", 1.0), type, rewardMaterial, rewardAmount, rewardItem, reward.getStringList("commands"), reward.getBoolean("broadcast.enabled", false), reward.getString("broadcast.message", "&b%player% &7ha trovato &f%reward% &7da &b%crate%")));
            }
        }
        if (!crate.rewards.isEmpty()) {
            this.writeCrate(crate);
        }
        return crate;
    }

    private ItemStack cleanStoredRewardItem(ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) {
            return item;
        }
        ItemStack clean = item.clone();
        ItemMeta meta = clean.getItemMeta();
        if (meta == null || !meta.hasLore()) {
            return clean;
        }
        List<String> lore = meta.getLore().stream().filter(line -> !this.isPluginRewardLore((String)line)).toList();
        meta.setLore(lore.isEmpty() ? null : lore);
        clean.setItemMeta(meta);
        return clean;
    }

    private boolean isPluginRewardLore(String line) {
        String plain = ChatColor.stripColor((String)(line == null ? "" : line)).toLowerCase(Locale.ROOT);
        return plain.startsWith("chance:") || plain.startsWith("tipo:") || plain.startsWith("click per modificare") || plain.startsWith("sinistro:") || plain.startsWith("destro:") || plain.startsWith("shift destro:") || plain.startsWith("shift sinistro:") || plain.contains("clicca per reclamare");
    }

    private void writeCrate(Crate crate) {
        String path = "crates." + crate.id + ".";
        this.plugin.getConfig().set(path + "display-name", (Object)crate.displayName);
        this.plugin.getConfig().set(path + "key-id", (Object)crate.keyId);
        this.plugin.getConfig().set(path + "animation", (Object)crate.animation.name());
        this.plugin.getConfig().set(path + "block-material", (Object)crate.blockMaterial.name());
        this.plugin.getConfig().set(path + "particle", (Object)crate.particle.name());
        this.plugin.getConfig().set(path + "idle-particles.shape", (Object)crate.idleParticleShape.name());
        this.plugin.getConfig().set(path + "idle-particles.particle", (Object)crate.idleParticle.name());
        this.plugin.getConfig().set(path + "idle-particles.color.red", (Object)crate.idleParticleRed);
        this.plugin.getConfig().set(path + "idle-particles.color.green", (Object)crate.idleParticleGreen);
        this.plugin.getConfig().set(path + "idle-particles.color.blue", (Object)crate.idleParticleBlue);
        this.plugin.getConfig().set(path + "idle-particles.color.size", (Object)Float.valueOf(crate.idleParticleSize));
        this.plugin.getConfig().set(path + "idle-particles.radius", (Object)crate.idleParticleRadius);
        this.plugin.getConfig().set(path + "idle-particles.height", (Object)crate.idleParticleHeight);
        this.plugin.getConfig().set(path + "idle-particles.points", (Object)crate.idleParticlePoints);
        this.plugin.getConfig().set(path + "permission", (Object)crate.permission);
        this.plugin.getConfig().set(path + "no-key-message", (Object)crate.noKeyMessage);
        this.plugin.getConfig().set(path + "open-message", (Object)crate.openMessage);
        this.plugin.getConfig().set(path + "hologram.enabled", (Object)crate.hologramEnabled);
        this.plugin.getConfig().set(path + "hologram.height", (Object)crate.hologramHeight);
        this.plugin.getConfig().set(path + "hologram.offset-x", (Object)crate.hologramOffsetX);
        this.plugin.getConfig().set(path + "hologram.offset-y", (Object)crate.hologramOffsetY);
        this.plugin.getConfig().set(path + "hologram.offset-z", (Object)crate.hologramOffsetZ);
        this.plugin.getConfig().set(path + "hologram.lines", crate.hologramLines);
        this.writeExternalCrateFiles(crate);
        this.saveExtraFiles();
        this.plugin.getConfig().set(path + "animation-settings.duration-steps", (Object)crate.animationDuration);
        this.plugin.getConfig().set(path + "animation-settings.speed-ticks", (Object)crate.animationSpeed);
        this.plugin.getConfig().set(path + "animation-settings.gui-size", (Object)crate.animationGuiSize);
        this.plugin.getConfig().set(path + "animation-settings.close-delay-ticks", (Object)crate.animationCloseDelay);
        this.plugin.getConfig().set(path + "animation-settings.finish-firework", (Object)crate.animationFirework);
        this.plugin.getConfig().set(path + "animation-settings.title", (Object)crate.animationTitle);
        this.plugin.getConfig().set(path + "animation-settings.tick-sound", (Object)crate.animationSound);
        this.plugin.getConfig().set(path + "locations", crate.locations);
        this.plugin.getConfig().set(path + "key.material", (Object)crate.keyMaterial.name());
        this.plugin.getConfig().set(path + "key.name", (Object)crate.keyName);
        this.plugin.getConfig().set(path + "key.lore", crate.keyLore);
        this.plugin.getConfig().set(path + "key.item", (Object)crate.keyItem);
        this.plugin.getConfig().set(path + "key.uses", (Object)crate.keyUses);
        this.plugin.getConfig().set(path + "key.pick-mode-enabled", (Object)crate.pickModeEnabled);
        this.plugin.getConfig().set(path + "rewards", null);
        for (Reward reward : crate.rewards) {
            String rewardPath = path + "rewards." + reward.id() + ".";
            this.plugin.getConfig().set(rewardPath + "name", (Object)reward.name());
            this.plugin.getConfig().set(rewardPath + "lore", reward.lore());
            this.plugin.getConfig().set(rewardPath + "chance", (Object)reward.chance());
            this.plugin.getConfig().set(rewardPath + "type", (Object)reward.type().name());
            this.plugin.getConfig().set(rewardPath + "material", (Object)reward.material().name());
            this.plugin.getConfig().set(rewardPath + "amount", (Object)reward.amount());
            this.plugin.getConfig().set(rewardPath + "item", (Object)reward.item());
            this.plugin.getConfig().set(rewardPath + "commands", reward.commands());
            this.plugin.getConfig().set(rewardPath + "broadcast.enabled", (Object)reward.broadcast());
            this.plugin.getConfig().set(rewardPath + "broadcast.message", (Object)reward.broadcastMessage());
        }
    }

    private String serialize(Location location) {
        return location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
    }

    private void writeExternalCrateFiles(Crate crate) {
        if (this.locationsConfig != null) {
            this.locationsConfig.set("crates." + crate.id, crate.locations);
        }
        if (this.hologramsConfig != null) {
            String path = "crates." + crate.id + ".";
            if (!this.hologramsConfig.contains(path + "enabled")) {
                this.hologramsConfig.set(path + "enabled", (Object)crate.hologramEnabled);
            }
            if (!this.hologramsConfig.contains(path + "height")) {
                this.hologramsConfig.set(path + "height", (Object)crate.hologramHeight);
            }
            if (!this.hologramsConfig.contains(path + "offset-x")) {
                this.hologramsConfig.set(path + "offset-x", (Object)crate.hologramOffsetX);
            }
            if (!this.hologramsConfig.contains(path + "offset-y")) {
                this.hologramsConfig.set(path + "offset-y", (Object)crate.hologramOffsetY);
            }
            if (!this.hologramsConfig.contains(path + "offset-z")) {
                this.hologramsConfig.set(path + "offset-z", (Object)crate.hologramOffsetZ);
            }
            if (!this.hologramsConfig.contains(path + "lines")) {
                this.hologramsConfig.set(path + "lines", crate.hologramLines);
            }
        }
    }

    private void ensurePresetCrates() {
        this.addPreset("comune", "&aCrate Comune", Material.CHEST, AnimationType.CSGO, ParticleShape.AURA, 85, 255, 85, this.presetLines("&aCrate Comune"), 3.0);
        this.addPreset("rara", "&bCrate Rara", Material.CHEST, AnimationType.SHRINK_REVEAL, ParticleShape.COLOR_SWIRL, 85, 200, 255, this.presetLines("&bCrate Rara"), 3.0);
        this.addPreset("epica", "&dCrate Epica", Material.CHEST, AnimationType.WHEEL, ParticleShape.HELIX, 190, 80, 255, this.presetLines("&dCrate Epica"), 3.0);
        this.addPreset("leggendaria", "&6Crate Leggendaria", Material.CHEST, AnimationType.FIREWORK, ParticleShape.CROWN, 255, 220, 45, this.presetLines("&6Crate Leggendaria"), 3.0);
        this.addPreset("mitica", "&cCrate Mitica", Material.CHEST, AnimationType.METEOR, ParticleShape.FLAME_CIRCLE, 255, 45, 45, this.presetLines("&cCrate Mitica"), 3.0);
        this.addPreset("voto", "&fCrate Voto", Material.CHEST, AnimationType.RANDOM_GLASS, ParticleShape.CUBE_FRAME, 150, 150, 150, this.presetLines("&fCrate Voto"), 3.0);
        this.addPreset("oscura", "&8Crate Oscura", Material.CHEST, AnimationType.BLACK_HOLE, ParticleShape.LOW_MIST, 35, 35, 35, this.presetLines("&8Crate Oscura"), 3.0);
        this.plugin.saveConfig();
    }

    private void addPreset(String id, String displayName, Material material, AnimationType animation, ParticleShape particleShape, int red, int green, int blue, List<String> hologramLines, double hologramOffsetY) {
        String key = id.toLowerCase(Locale.ROOT);
        if (this.crates.containsKey(key)) {
            return;
        }
        Crate crate = new Crate(id);
        crate.displayName = displayName;
        crate.keyId = id;
        crate.keyName = displayName.replace("Crate", "Key");
        crate.keyLore = List.of("&7Apre " + displayName, "&7Utilizzi: &f%uses%");
        crate.blockMaterial = material;
        crate.animation = animation;
        crate.idleParticleShape = particleShape;
        crate.idleParticle = Particle.REDSTONE;
        crate.idleParticleRed = red;
        crate.idleParticleGreen = green;
        crate.idleParticleBlue = blue;
        crate.idleParticleHeight = 0.15;
        crate.hologramOffsetY = hologramOffsetY;
        crate.hologramLines = hologramLines;
        crate.permission = "dadacrates.open." + key;
        crate.noKeyMessage = "&cTi serve una &f" + displayName + "&c in mano.";
        crate.openMessage = "&aHai aperto &f" + displayName + "&a!";
        this.crates.put(key, crate);
        this.writeCrate(crate);
    }

    private List<String> presetLines(String separator) {
        return List.of(separator, "", "&fClick &adestro &fper aprire", "&fClick &bsinistro &fper vedere i premi", "", "&7Key vanilla, nessuna resource pack richiesta");
    }

    Location deserialize(String value) {
        String[] parts = value.split(",");
        if (parts.length != 4 || Bukkit.getWorld((String)parts[0]) == null) {
            return null;
        }
        try {
            return new Location(Bukkit.getWorld((String)parts[0]), (double)Integer.parseInt(parts[1]), (double)Integer.parseInt(parts[2]), (double)Integer.parseInt(parts[3]));
        }
        catch (NumberFormatException ex) {
            return null;
        }
    }

    private Material material(String value, Material fallback) {
        Material material = Material.matchMaterial((String)String.valueOf(value));
        return material == null ? fallback : material;
    }

    private Particle particle(String value, Particle fallback) {
        try {
            return Particle.valueOf((String)String.valueOf(value).toUpperCase(Locale.ROOT));
        }
        catch (IllegalArgumentException ex) {
            return fallback;
        }
    }

    private int clampColor(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private int normalizeGuiSize(int size) {
        if (size <= 9) {
            return 9;
        }
        if (size <= 18) {
            return 18;
        }
        if (size <= 27) {
            return 27;
        }
        if (size <= 36) {
            return 36;
        }
        if (size <= 45) {
            return 45;
        }
        return 54;
    }
}
