package io.ethansmith.advancedautopromote.command;

import io.ethansmith.advancedautopromote.economy.Economy;
import io.ethansmith.advancedautopromote.permissions.Permissions;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RankupCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("rankup")) return true;

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "You must be a player to use this command!");
            return true;
        }

        String nextGroup = Permissions.getNextGroup(player);

        if (nextGroup == null) {
            player.sendMessage(ChatColor.RED + "You are already at the highest rank!");
            return true;
        }

        double cost = Permissions.getConfiguredGroups().get(nextGroup);

        if (!Economy.hasEnough(player, cost)) {
            player.sendMessage(ChatColor.RED + "You do not have enough money to rank up!");
            return true;
        }

        Economy.withdraw(player, cost);
        Permissions.promotePlayer(player);
        player.sendMessage(ChatColor.GREEN + "You have been promoted to " + nextGroup + "!");
        return true;
    }
}
