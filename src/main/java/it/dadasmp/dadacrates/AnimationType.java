/*
 * Decompiled with CFR 0.152.
 */
package it.dadasmp.dadacrates;

enum AnimationType {
    CSGO,
    RANDOM_GLASS,
    SLOT_MACHINE,
    WHEEL,
    SHRINK_REVEAL,
    PRISM,
    BLACK_HOLE,
    METEOR,
    COSMIC_PORTAL,
    FIREWORK,
    INSTANT;


    static AnimationType parse(String value) {
        try {
            return AnimationType.valueOf(String.valueOf(value).toUpperCase());
        }
        catch (IllegalArgumentException ex) {
            return CSGO;
        }
    }
}

