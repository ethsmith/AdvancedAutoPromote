package io.ethansmith.advancedautopromote.economy;

import io.ethansmith.advancedautopromote.AdvancedAutoPromote;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class Economy {

    private static AdvancedAutoPromote plugin = AdvancedAutoPromote.getInstance();
    private static net.milkbowl.vault.economy.Economy econ = null;

    public static void init() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().severe("Vault not found! Disabling this plugin.");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }

        RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> rsp = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (rsp == null) {
            plugin.getLogger().severe("Economy provider not found! Disabling this plugin.");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }

        econ = rsp.getProvider();
        plugin.getLogger().info("Economy manager initialized!");
    }

    public static boolean hasEnough(Player player, double amount) {
        return econ.getBalance(player) >= amount;
    }

    public static void withdraw(Player player, double amount) {
        econ.withdrawPlayer(player, amount);
        player.sendMessage(ChatColor.GREEN + "You have been charged $" + amount + " to rank up.");
    }

    public static void deposit(Player player, double amount) {
        econ.depositPlayer(player, amount);
    }
}
