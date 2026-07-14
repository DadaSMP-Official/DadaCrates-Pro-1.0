/*
 * Decompiled with CFR 0.152.
 */
package it.dadasmp.dadacrates;

enum ParticleShape {
    NONE,
    RING,
    DOUBLE_RING,
    SPIRAL,
    HELIX,
    HEART,
    STAR,
    ORBIT,
    AURA,
    CROWN,
    FOUNTAIN,
    SIDE_WAVES,
    COLOR_SWIRL,
    BURST,
    MAGIC_PLATFORM,
    CORNER_SPARKS,
    RUNE_SQUARE,
    SPLASH_AURA,
    RISING_STARS,
    LOW_MIST,
    DIAGONAL_CROSS,
    BUTTERFLY,
    CUBE_FRAME,
    FLAME_CIRCLE;


    static ParticleShape parse(String value) {
        try {
            return ParticleShape.valueOf(String.valueOf(value).toUpperCase());
        }
        catch (IllegalArgumentException ex) {
            return RING;
        }
    }
}

