/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.ChatColor
 */
package it.dadasmp.dadacrates;

import java.util.List;
import org.bukkit.ChatColor;

final class Text {
    private Text() {
    }

    static String color(String value) {
        return ChatColor.translateAlternateColorCodes((char)'&', (String)(value == null ? "" : value));
    }

    static List<String> color(List<String> values) {
        return values.stream().map(Text::color).toList();
    }

    static String plain(String value) {
        return ChatColor.stripColor((String)Text.color(value));
    }
}

