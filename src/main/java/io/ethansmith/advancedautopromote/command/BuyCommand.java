package io.ethansmith.advancedautopromote.command;

import io.ethansmith.advancedautopromote.economy.Economy;
import io.ethansmith.advancedautopromote.permissions.Permissions;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class BuyCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("buy-rank")) return true;

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "You must be a player to use this command!");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /buy-rank <rank>");
            return true;
        }

        String group = args[0].toLowerCase();

        if (!Permissions.getConfiguredGroups().containsKey(group)) {
            player.sendMessage(ChatColor.RED + "Invalid rank specified!");
            return true;
        }

        if (!player.hasPermission("aap.buy." + group)) {
            player.sendMessage(ChatColor.RED + "You do not have permission to buy this rank!");
            return true;
        }

        String currentGroup = Permissions.getCurrentGroup(player);

        if (Permissions.getGroupIndex(currentGroup) > Permissions.getGroupIndex(group)) {
            player.sendMessage(ChatColor.RED + "You cannot buy a rank that is the lower than your current rank!");
            return true;
        } else if (Permissions.getGroupIndex(currentGroup) == Permissions.getGroupIndex(group)) {
            player.sendMessage(ChatColor.RED + "You are already at this rank!");
            return true;
        }

        double cost = Permissions.getConfiguredGroups().get(group);

        if (!Economy.hasEnough(player, cost)) {
            player.sendMessage(ChatColor.RED + "You do not have enough money to buy this rank!");
            return true;
        }

        boolean success = Permissions.promotePlayer(player, group);

        if (!success && Permissions.getNextGroup(player) != null) {
            player.sendMessage(ChatColor.RED + "You may not skip ranks when promoting!");
            return true;
        } else if (!success) {
            player.sendMessage(ChatColor.RED + "An error occurred while promoting you to " + group + "!");
            return true;
        }

        Economy.withdraw(player, cost);
        player.sendMessage(ChatColor.GREEN + "You have been promoted to " + group + "!");
        return true;
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1)
            return  getSuggestions(args[0]);

        return new ArrayList<>();
    }

    private List<String> getSuggestions(String argument) {
        List<String> suggestions = new ArrayList<>();

        for (String group : Permissions.getConfiguredGroups().keySet()) {
            if (!group.startsWith(argument)) continue;
            suggestions.add(group);
        }

        return suggestions;
    }
}
