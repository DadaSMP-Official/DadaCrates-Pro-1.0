/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.Particle
 *  org.bukkit.inventory.ItemStack
 */
package it.dadasmp.dadacrates;

import it.dadasmp.dadacrates.AnimationType;
import it.dadasmp.dadacrates.ParticleShape;
import it.dadasmp.dadacrates.Reward;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.inventory.ItemStack;

final class Crate {
    final String id;
    String displayName;
    String keyId;
    AnimationType animation;
    Material blockMaterial;
    Particle particle;
    ParticleShape idleParticleShape;
    Particle idleParticle;
    int idleParticleRed;
    int idleParticleGreen;
    int idleParticleBlue;
    float idleParticleSize;
    double idleParticleRadius;
    double idleParticleHeight;
    int idleParticlePoints;
    String permission;
    String noKeyMessage;
    String openMessage;
    boolean hologramEnabled;
    double hologramHeight;
    double hologramOffsetX;
    double hologramOffsetY;
    double hologramOffsetZ;
    List<String> hologramLines;
    int animationDuration;
    int animationSpeed;
    int animationGuiSize;
    int animationCloseDelay;
    boolean animationFirework;
    String animationTitle;
    String animationSound;
    final List<String> locations;
    Material keyMaterial;
    String keyName;
    List<String> keyLore;
    ItemStack keyItem;
    int keyUses;
    boolean pickModeEnabled;
    final List<Reward> rewards;

    Crate(String id) {
        this.id = id;
        this.displayName = "&a" + id;
        this.keyId = id;
        this.animation = AnimationType.CSGO;
        this.blockMaterial = Material.CHEST;
        this.particle = Particle.VILLAGER_HAPPY;
        this.idleParticleShape = ParticleShape.AURA;
        this.idleParticle = Particle.REDSTONE;
        this.idleParticleRed = 85;
        this.idleParticleGreen = 255;
        this.idleParticleBlue = 255;
        this.idleParticleSize = 1.1f;
        this.idleParticleRadius = 0.85;
        this.idleParticleHeight = 0.25;
        this.idleParticlePoints = 18;
        this.permission = "dadacrates.open." + id.toLowerCase();
        this.noKeyMessage = "&cTi serve la key giusta.";
        this.openMessage = "&aApertura crate...";
        this.hologramEnabled = true;
        this.hologramHeight = 1.7;
        this.hologramOffsetX = 0.5;
        this.hologramOffsetY = 1.7;
        this.hologramOffsetZ = 0.5;
        this.hologramLines = List.of("&f %crate%", "", "&f\u27a4 Clic &aDESTRO &fper aprire la cassa", "&f\u27a4 Clic &bSINISTRO &fper visualizzare i premi", "", "&d&l&m\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501&e&l&m\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501&a&l&m\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501&b&l&m\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501&5&l&m\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501", "", "&fPer ottenere le key visita:", "&ewww.dadasmp.it");
        this.animationDuration = 28;
        this.animationSpeed = 2;
        this.animationGuiSize = 27;
        this.animationCloseDelay = 18;
        this.animationFirework = true;
        this.animationTitle = "&6Apertura %crate%";
        this.animationSound = "UI_BUTTON_CLICK";
        this.locations = new ArrayList<String>();
        this.keyMaterial = Material.TRIPWIRE_HOOK;
        this.keyName = "&aKey " + id;
        this.keyLore = List.of("&7Key per aprire questa crate");
        this.keyItem = null;
        this.keyUses = 1;
        this.pickModeEnabled = true;
        this.rewards = new ArrayList<Reward>();
    }
}

