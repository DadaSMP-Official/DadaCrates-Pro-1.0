/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Color
 *  org.bukkit.Location
 *  org.bukkit.Particle
 *  org.bukkit.Particle$DustOptions
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitTask
 */
package it.dadasmp.dadacrates;

import it.dadasmp.dadacrates.Crate;
import it.dadasmp.dadacrates.DadaCratesPlugin;
import it.dadasmp.dadacrates.ParticleShape;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

final class IdleParticleManager {
    private final DadaCratesPlugin plugin;
    private BukkitTask task;
    private int tick;

    IdleParticleManager(DadaCratesPlugin plugin) {
        this.plugin = plugin;
    }

    void start() {
        this.stop();
        this.task = this.plugin.getServer().getScheduler().runTaskTimer((Plugin)this.plugin, this::render, 20L, 4L);
    }

    void stop() {
        if (this.task != null) {
            this.task.cancel();
            this.task = null;
        }
    }

    private void render() {
        ++this.tick;
        for (Crate crate : this.plugin.store().all()) {
            if (crate.idleParticleShape == ParticleShape.NONE) continue;
            for (String serialized : crate.locations) {
                Location base = this.plugin.store().deserialize(serialized);
                if (base == null || base.getWorld() == null) continue;
                this.draw(crate, base.clone().add(0.5, 0.8 + crate.idleParticleHeight, 0.5));
            }
        }
    }

    private void draw(Crate crate, Location center) {
        switch (crate.idleParticleShape) {
            case RING: {
                this.ring(crate, center, 0.0);
                break;
            }
            case DOUBLE_RING: {
                this.ring(crate, center.clone().add(0.0, -0.2, 0.0), 0.0);
                this.ring(crate, center.clone().add(0.0, 0.35, 0.0), Math.PI / (double)crate.idleParticlePoints);
                break;
            }
            case SPIRAL: {
                this.spiral(crate, center, false);
                break;
            }
            case HELIX: {
                this.spiral(crate, center, true);
                break;
            }
            case HEART: {
                this.heart(crate, center);
                break;
            }
            case STAR: {
                this.star(crate, center);
                break;
            }
            case ORBIT: {
                this.orbit(crate, center);
                break;
            }
            case AURA: {
                this.aura(crate, center);
                break;
            }
            case CROWN: {
                this.crown(crate, center);
                break;
            }
            case FOUNTAIN: {
                this.fountain(crate, center);
                break;
            }
            case SIDE_WAVES: {
                this.sideWaves(crate, center);
                break;
            }
            case COLOR_SWIRL: {
                this.colorSwirl(crate, center);
                break;
            }
            case BURST: {
                this.burst(crate, center);
                break;
            }
            case MAGIC_PLATFORM: {
                this.magicPlatform(crate, center);
                break;
            }
            case CORNER_SPARKS: {
                this.cornerSparks(crate, center);
                break;
            }
            case RUNE_SQUARE: {
                this.runeSquare(crate, center);
                break;
            }
            case SPLASH_AURA: {
                this.splashAura(crate, center);
                break;
            }
            case RISING_STARS: {
                this.risingStars(crate, center);
                break;
            }
            case LOW_MIST: {
                this.lowMist(crate, center);
                break;
            }
            case DIAGONAL_CROSS: {
                this.diagonalCross(crate, center);
                break;
            }
            case BUTTERFLY: {
                this.butterfly(crate, center);
                break;
            }
            case CUBE_FRAME: {
                this.cubeFrame(crate, center);
                break;
            }
            case FLAME_CIRCLE: {
                this.flameCircle(crate, center);
                break;
            }
        }
    }

    private void ring(Crate crate, Location center, double offset) {
        for (int i = 0; i < crate.idleParticlePoints; ++i) {
            double angle = Math.PI * 2 * (double)i / (double)crate.idleParticlePoints + (double)this.tick * 0.08 + offset;
            this.spawn(crate, center.clone().add(Math.cos(angle) * crate.idleParticleRadius, 0.0, Math.sin(angle) * crate.idleParticleRadius));
        }
    }

    private void spiral(Crate crate, Location center, boolean doubleHelix) {
        for (int i = 0; i < crate.idleParticlePoints; ++i) {
            double progress = (double)i / (double)crate.idleParticlePoints;
            double angle = progress * Math.PI * 2.0 + (double)this.tick * 0.12;
            double y = progress * 1.4 - 0.35;
            this.spawn(crate, center.clone().add(Math.cos(angle) * crate.idleParticleRadius, y, Math.sin(angle) * crate.idleParticleRadius));
            if (!doubleHelix) continue;
            this.spawn(crate, center.clone().add(Math.cos(angle + Math.PI) * crate.idleParticleRadius, y, Math.sin(angle + Math.PI) * crate.idleParticleRadius));
        }
    }

    private void heart(Crate crate, Location center) {
        for (int i = 0; i < crate.idleParticlePoints; ++i) {
            double t = Math.PI * 2 * (double)i / (double)crate.idleParticlePoints;
            double x = 16.0 * Math.pow(Math.sin(t), 3.0) / 18.0;
            double y = (13.0 * Math.cos(t) - 5.0 * Math.cos(2.0 * t) - 2.0 * Math.cos(3.0 * t) - Math.cos(4.0 * t)) / 18.0;
            this.spawn(crate, center.clone().add(x * crate.idleParticleRadius, y + 0.7, 0.0));
        }
    }

    private void star(Crate crate, Location center) {
        for (int i = 0; i < crate.idleParticlePoints; ++i) {
            double angle = Math.PI * 2 * (double)i / (double)crate.idleParticlePoints + (double)this.tick * 0.06;
            double radius = i % 2 == 0 ? crate.idleParticleRadius : crate.idleParticleRadius * 0.45;
            this.spawn(crate, center.clone().add(Math.cos(angle) * radius, 0.25, Math.sin(angle) * radius));
        }
    }

    private void orbit(Crate crate, Location center) {
        for (int i = 0; i < 3; ++i) {
            double angle = (double)this.tick * 0.18 + (double)i * Math.PI * 2.0 / 3.0;
            this.spawn(crate, center.clone().add(Math.cos(angle) * crate.idleParticleRadius, 0.3 + Math.sin(angle * 2.0) * 0.25, Math.sin(angle) * crate.idleParticleRadius));
        }
    }

    private void aura(Crate crate, Location center) {
        double angle;
        int i;
        int points = Math.max(16, crate.idleParticlePoints);
        for (i = 0; i < points; ++i) {
            angle = Math.PI * 2 * (double)i / (double)points + (double)this.tick * 0.11;
            double wave = Math.sin((double)this.tick * 0.25 + (double)i * 0.8) * 0.18;
            double radius = crate.idleParticleRadius + wave;
            this.spawn(crate, center.clone().add(Math.cos(angle) * radius, 0.05 + Math.sin(angle * 2.0 + (double)this.tick * 0.1) * 0.22, Math.sin(angle) * radius));
        }
        for (i = 0; i < points / 3; ++i) {
            angle = Math.PI * 2 * (double)i / (double)(points / 3) - (double)this.tick * 0.16;
            this.spawn(crate, center.clone().add(Math.cos(angle) * crate.idleParticleRadius * 0.55, 0.75, Math.sin(angle) * crate.idleParticleRadius * 0.55));
        }
    }

    private void crown(Crate crate, Location center) {
        this.ring(crate, center.clone().add(0.0, 0.85, 0.0), 0.0);
        for (int i = 0; i < 6; ++i) {
            double angle = Math.PI * 2 * (double)i / 6.0 + (double)this.tick * 0.05;
            Location base = center.clone().add(Math.cos(angle) * crate.idleParticleRadius, 0.85, Math.sin(angle) * crate.idleParticleRadius);
            for (int y = 0; y < 4; ++y) {
                this.spawn(crate, base.clone().add(0.0, (double)y * 0.12, 0.0));
            }
        }
    }

    private void fountain(Crate crate, Location center) {
        for (int i = 0; i < crate.idleParticlePoints; ++i) {
            double angle = Math.PI * 2 * (double)i / (double)crate.idleParticlePoints + (double)this.tick * 0.08;
            double progress = (double)(i % 6) / 6.0;
            double radius = crate.idleParticleRadius * progress;
            double y = 0.15 + Math.sin(progress * Math.PI) * 0.9;
            this.spawn(crate, center.clone().add(Math.cos(angle) * radius, y, Math.sin(angle) * radius));
        }
    }

    private void sideWaves(Crate crate, Location center) {
        for (int side = -1; side <= 1; side += 2) {
            for (int i = 0; i < crate.idleParticlePoints / 2; ++i) {
                double progress = (double)i / (double)(crate.idleParticlePoints / 2);
                double z = (progress - 0.5) * 1.8;
                double y = 0.15 + Math.sin(progress * Math.PI * 2.0 + (double)this.tick * 0.2) * 0.25;
                this.spawn(crate, center.clone().add((double)side * crate.idleParticleRadius, y, z));
            }
        }
    }

    private void colorSwirl(Crate crate, Location center) {
        for (int i = 0; i < crate.idleParticlePoints; ++i) {
            double progress = (double)i / (double)crate.idleParticlePoints;
            double angle = progress * Math.PI * 2.0 + (double)this.tick * 0.18;
            int red = (int)(128.0 + 127.0 * Math.sin(angle));
            int green = (int)(128.0 + 127.0 * Math.sin(angle + 2.1));
            int blue = (int)(128.0 + 127.0 * Math.sin(angle + 4.2));
            Location point = center.clone().add(Math.cos(angle) * crate.idleParticleRadius, progress * 1.2 - 0.2, Math.sin(angle) * crate.idleParticleRadius);
            this.spawnDust(point, red, green, blue, crate.idleParticleSize);
        }
    }

    private void burst(Crate crate, Location center) {
        int points = Math.max(12, crate.idleParticlePoints);
        for (int i = 0; i < points; ++i) {
            double angle = Math.PI * 2 * (double)i / (double)points;
            double pulse = 0.35 + (Math.sin((double)this.tick * 0.25) + 1.0) * 0.35;
            this.spawn(crate, center.clone().add(Math.cos(angle) * pulse, 0.25 + Math.sin((double)i + (double)this.tick * 0.2) * 0.2, Math.sin(angle) * pulse));
        }
    }

    private void magicPlatform(Crate crate, Location center) {
        double y = -0.55;
        for (double x = -1.05; x <= 1.05; x += 0.22) {
            this.spawn(crate, center.clone().add(x, y, -1.05));
            this.spawn(crate, center.clone().add(x, y, 1.05));
        }
        for (double z = -1.05; z <= 1.05; z += 0.22) {
            this.spawn(crate, center.clone().add(-1.05, y, z));
            this.spawn(crate, center.clone().add(1.05, y, z));
        }
        this.ring(crate, center.clone().add(0.0, -0.55, 0.0), (double)this.tick * 0.05);
    }

    private void cornerSparks(Crate crate, Location center) {
        double[][] corners;
        for (double[] corner : corners = new double[][]{{-0.85, -0.85}, {0.85, -0.85}, {-0.85, 0.85}, {0.85, 0.85}}) {
            for (int i = 0; i < 5; ++i) {
                double lift = 0.05 + (double)((this.tick + i) % 8) * 0.11;
                this.spawn(crate, center.clone().add(corner[0] + Math.sin((double)this.tick * 0.2 + (double)i) * 0.08, lift, corner[1] + Math.cos((double)this.tick * 0.2 + (double)i) * 0.08));
            }
        }
    }

    private void runeSquare(Crate crate, Location center) {
        this.magicPlatform(crate, center);
        for (int i = 0; i < 4; ++i) {
            double angle = (double)this.tick * 0.08 + (double)i * Math.PI / 2.0;
            this.spawn(crate, center.clone().add(Math.cos(angle) * 0.55, -0.15 + Math.sin((double)this.tick * 0.12) * 0.08, Math.sin(angle) * 0.55));
        }
    }

    private void splashAura(Crate crate, Location center) {
        for (int i = 0; i < crate.idleParticlePoints; ++i) {
            double angle = Math.PI * 2 * (double)i / (double)crate.idleParticlePoints + (double)this.tick * 0.12;
            double radius = 0.25 + (double)(i % 5) * 0.16;
            double y = -0.35 + Math.sin((double)this.tick * 0.18 + (double)i) * 0.35;
            this.spawn(crate, center.clone().add(Math.cos(angle) * radius, y, Math.sin(angle) * radius));
        }
        if (this.tick % 3 == 0) {
            this.spawn(crate, center.clone().add(0.0, 0.85, 0.0));
        }
    }

    private void risingStars(Crate crate, Location center) {
        for (int i = 0; i < crate.idleParticlePoints / 2; ++i) {
            double angle = Math.PI * 2 * (double)i / (double)(crate.idleParticlePoints / 2) + (double)this.tick * 0.09;
            double y = (double)((this.tick + i * 3) % 20) / 20.0 * 1.6 - 0.45;
            double radius = crate.idleParticleRadius * (0.45 + (double)(i % 3) * 0.22);
            this.spawn(crate, center.clone().add(Math.cos(angle) * radius, y, Math.sin(angle) * radius));
        }
    }

    private void lowMist(Crate crate, Location center) {
        for (int i = 0; i < crate.idleParticlePoints; ++i) {
            double angle = Math.PI * 2 * (double)i / (double)crate.idleParticlePoints + (double)this.tick * 0.04;
            double radius = crate.idleParticleRadius * (0.35 + (double)(i % 4) * 0.18);
            this.spawn(crate, center.clone().add(Math.cos(angle) * radius, -0.55 + Math.sin((double)this.tick * 0.1 + (double)i) * 0.08, Math.sin(angle) * radius));
        }
    }

    private void diagonalCross(Crate crate, Location center) {
        for (double t = -1.0; t <= 1.0; t += 0.18) {
            this.spawn(crate, center.clone().add(t * crate.idleParticleRadius, 0.0, t * crate.idleParticleRadius));
            this.spawn(crate, center.clone().add(t * crate.idleParticleRadius, 0.0, -t * crate.idleParticleRadius));
        }
    }

    private void butterfly(Crate crate, Location center) {
        for (int i = 0; i < crate.idleParticlePoints; ++i) {
            double t = Math.PI * 2 * (double)i / (double)crate.idleParticlePoints;
            double wing = Math.sin(t) * Math.cos(t);
            double x = Math.sin(t) * crate.idleParticleRadius;
            double z = wing * 1.4 * crate.idleParticleRadius;
            double y = Math.abs(Math.cos(t)) * 0.55;
            this.spawn(crate, center.clone().add(x, y, z));
            this.spawn(crate, center.clone().add(-x, y, z));
        }
    }

    private void cubeFrame(Crate crate, Location center) {
        double r = crate.idleParticleRadius;
        double y1 = -0.45;
        double y2 = 0.75;
        for (double d = -r; d <= r; d += 0.25) {
            this.spawn(crate, center.clone().add(d, y1, -r));
            this.spawn(crate, center.clone().add(d, y1, r));
            this.spawn(crate, center.clone().add(-r, y1, d));
            this.spawn(crate, center.clone().add(r, y1, d));
            this.spawn(crate, center.clone().add(d, y2, -r));
            this.spawn(crate, center.clone().add(d, y2, r));
            this.spawn(crate, center.clone().add(-r, y2, d));
            this.spawn(crate, center.clone().add(r, y2, d));
        }
    }

    private void flameCircle(Crate crate, Location center) {
        for (int i = 0; i < crate.idleParticlePoints; ++i) {
            double angle = Math.PI * 2 * (double)i / (double)crate.idleParticlePoints + (double)this.tick * 0.12;
            Location point = center.clone().add(Math.cos(angle) * crate.idleParticleRadius, Math.sin((double)this.tick * 0.2 + (double)i) * 0.12, Math.sin(angle) * crate.idleParticleRadius);
            point.getWorld().spawnParticle(Particle.FLAME, point, 1, 0.0, 0.0, 0.0, 0.01);
        }
    }

    private void spawn(Crate crate, Location location) {
        if (crate.idleParticle == Particle.REDSTONE) {
            Particle.DustOptions dust = new Particle.DustOptions(Color.fromRGB((int)crate.idleParticleRed, (int)crate.idleParticleGreen, (int)crate.idleParticleBlue), crate.idleParticleSize);
            location.getWorld().spawnParticle(Particle.REDSTONE, location, 1, 0.0, 0.0, 0.0, 0.0, (Object)dust);
            return;
        }
        location.getWorld().spawnParticle(crate.idleParticle, location, 1, 0.0, 0.0, 0.0, 0.0);
    }

    private void spawnDust(Location location, int red, int green, int blue, float size) {
        Particle.DustOptions dust = new Particle.DustOptions(Color.fromRGB((int)this.clamp(red), (int)this.clamp(green), (int)this.clamp(blue)), size);
        location.getWorld().spawnParticle(Particle.REDSTONE, location, 1, 0.0, 0.0, 0.0, 0.0, (Object)dust);
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }
}

