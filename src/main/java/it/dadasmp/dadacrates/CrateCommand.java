/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.command.TabCompleter
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 */
package it.dadasmp.dadacrates;

import it.dadasmp.dadacrates.Crate;
import it.dadasmp.dadacrates.DadaCratesPlugin;
import it.dadasmp.dadacrates.KeyMode;
import it.dadasmp.dadacrates.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

final class CrateCommand
implements CommandExecutor,
TabCompleter {
    private final DadaCratesPlugin plugin;
    private final List<String> waitingSetBlock = new ArrayList<String>();
    private final List<String> waitingRemoveBlock = new ArrayList<String>();

    CrateCommand(DadaCratesPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(this.plugin.store().msg("only-player"));
                return true;
            }
            Player player = (Player)sender;
            if (!sender.isOp() && !sender.hasPermission("dadacrates.admin")) {
                sender.sendMessage(this.plugin.store().prefix() + this.plugin.store().msg("no-permission"));
                return true;
            }
            this.plugin.gui().openMain(player);
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        if (sub.equals("reload")) {
            if (!this.requireAdmin(sender)) {
                return true;
            }
            this.plugin.store().reload();
            this.plugin.holograms().startKeepAlive();
            this.plugin.idleParticles().start();
            sender.sendMessage(this.plugin.store().prefix() + this.plugin.store().msg("reload"));
            return true;
        }
        if (sub.equals("create") && args.length >= 2) {
            if (!this.requireAdmin(sender)) {
                return true;
            }
            Crate crate2 = this.plugin.store().create(args[1]);
            sender.sendMessage(this.plugin.store().prefix() + this.plugin.store().msg("crate-created").replace("%crate%", crate2.id));
            return true;
        }
        if ((sub.equals("delete") || sub.equals("dellette")) && args.length >= 2) {
            if (!this.requireAdmin(sender)) {
                return true;
            }
            if (this.plugin.store().delete(args[1])) {
                sender.sendMessage(this.plugin.store().prefix() + Text.color("&aCrate eliminata: &f" + args[1]));
            } else {
                sender.sendMessage(this.plugin.store().prefix() + Text.color("&cCrate non trovata."));
            }
            return true;
        }
        if (sub.equals("givekey") && args.length >= 3 || sub.equals("key") && args.length >= 4) {
            KeyMode mode;
            int picks;
            if (!this.requireAdmin(sender)) {
                return true;
            }
            boolean keySyntax = sub.equals("key");
            String targetName = args[1];
            String crateId = keySyntax ? args[2] : args[2];
            Crate crate3 = this.plugin.store().byId(crateId).orElse(null);
            if (crate3 == null) {
                sender.sendMessage("Crate non trovata.");
                return true;
            }
            int amount = args.length >= 4 ? this.parseInt(args[3], 1) : 1;
            picks = args.length >= 5 ? this.parseInt(args[4], crate3.keyUses) : crate3.keyUses;
            mode = args.length >= 6 ? KeyMode.parse(args[5]) : (picks > 1 ? KeyMode.PICK_TWO : KeyMode.NORMAL);
            if (!keySyntax && args.length >= 5) {
                mode = KeyMode.parse(args[4]);
                picks = crate3.keyUses;
            }
            int given = this.giveKey(targetName, crate3, amount, picks, mode);
            sender.sendMessage(this.plugin.store().prefix() + Text.color("&aKey date: &f" + given + " &7(" + String.valueOf((Object)mode) + ", premi: " + picks + ")"));
            return true;
        }
        if (sub.equals("setblock") && args.length >= 2 && sender instanceof Player) {
            Player player = (Player)sender;
            if (!this.requireAdmin(sender)) {
                return true;
            }
            if (this.plugin.store().byId(args[1]).isEmpty()) {
                sender.sendMessage("Crate non trovata.");
                return true;
            }
            this.waitingSetBlock.remove(String.valueOf(player.getUniqueId()) + ":");
            this.waitingSetBlock.add(String.valueOf(player.getUniqueId()) + ":" + args[1].toLowerCase(Locale.ROOT));
            player.sendMessage(this.plugin.store().prefix() + this.plugin.store().msg("click-crate-block"));
            return true;
        }
        if (sub.equals("remove") && sender instanceof Player) {
            Player player = (Player)sender;
            if (!this.requireAdmin(sender)) {
                return true;
            }
            this.waitingRemoveBlock.add(player.getUniqueId().toString());
            player.sendMessage(this.plugin.store().prefix() + Text.color("&eClicca la crate posizionata da rimuovere."));
            return true;
        }
        if (sub.equals("preview") && args.length >= 2 && sender instanceof Player) {
            Player player = (Player)sender;
            this.plugin.store().byId(args[1]).ifPresent(crate -> this.plugin.gui().openPreview(player, (Crate)crate));
            return true;
        }
        sender.sendMessage("/crate remove oppure /crate delete <crate>");
        return true;
    }

    private int giveKey(String targetName, Crate crate, int amount, int picks, KeyMode mode) {
        if (targetName.equalsIgnoreCase("all")) {
            int count = 0;
            for (Player online : Bukkit.getOnlinePlayers()) {
                online.getInventory().addItem(new ItemStack[]{this.plugin.keys().create(crate, amount, mode, picks)});
                ++count;
            }
            return count;
        }
        Player target = Bukkit.getPlayerExact((String)targetName);
        if (target == null) {
            return 0;
        }
        target.getInventory().addItem(new ItemStack[]{this.plugin.keys().create(crate, amount, mode, picks)});
        return 1;
    }

    String takeWaitingSetBlock(Player player) {
        String prefix = String.valueOf(player.getUniqueId()) + ":";
        for (String value : List.copyOf(this.waitingSetBlock)) {
            if (!value.startsWith(prefix)) continue;
            this.waitingSetBlock.remove(value);
            return value.substring(prefix.length());
        }
        return null;
    }

    boolean takeWaitingRemoveBlock(Player player) {
        return this.waitingRemoveBlock.remove(player.getUniqueId().toString());
    }

    private boolean requireAdmin(CommandSender sender) {
        if (!sender.isOp() && !sender.hasPermission("dadacrates.admin")) {
            sender.sendMessage(this.plugin.store().prefix() + this.plugin.store().msg("no-permission"));
            return false;
        }
        return true;
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException ex) {
            return fallback;
        }
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.isOp() && !sender.hasPermission("dadacrates.admin")) {
            return List.of();
        }
        if (args.length == 1) {
            return List.of("reload", "create", "delete", "remove", "givekey", "key", "setblock", "preview");
        }
        if (args.length == 3 && (args[0].equalsIgnoreCase("givekey") || args[0].equalsIgnoreCase("key")) || args.length == 2 && (args[0].equalsIgnoreCase("setblock") || args[0].equalsIgnoreCase("preview") || args[0].equalsIgnoreCase("delete"))) {
            return this.plugin.store().all().stream().map(crate -> crate.id).toList();
        }
        if (args.length == 5 && args[0].equalsIgnoreCase("givekey") || args.length == 6 && args[0].equalsIgnoreCase("key")) {
            return List.of("NORMAL", "PICK_ONE", "PICK_TWO");
        }
        return List.of();
    }
}
