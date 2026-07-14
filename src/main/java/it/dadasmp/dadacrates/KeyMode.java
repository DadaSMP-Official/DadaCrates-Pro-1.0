/*
 * Decompiled with CFR 0.152.
 */
package it.dadasmp.dadacrates;

enum KeyMode {
    NORMAL(0, "&aKey Normale"),
    PICK_ONE(1, "&bKey Pick 1"),
    PICK_TWO(2, "&dKey Pick 2");

    final int picks;
    final String display;

    private KeyMode(int picks, String display) {
        this.picks = picks;
        this.display = display;
    }

    static KeyMode parse(String value) {
        try {
            return KeyMode.valueOf(String.valueOf(value).toUpperCase());
        }
        catch (IllegalArgumentException ex) {
            return NORMAL;
        }
    }
}

