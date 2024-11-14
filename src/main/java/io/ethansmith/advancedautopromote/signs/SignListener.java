package io.ethansmith.advancedautopromote.signs;

import io.ethansmith.advancedautopromote.economy.Economy;
import io.ethansmith.advancedautopromote.permissions.Permissions;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class SignListener implements Listener {

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();

        if (!player.hasPermission("aap.sign.create")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to create signs!");
            return;
        }

        String line1 = event.getLine(0) != null ? event.getLine(0) : "";
        String line2 = event.getLine(1) != null ? event.getLine(1).toLowerCase() : "";
        String line3 = event.getLine(2) != null ? event.getLine(2) : "";

        if (!line1.equalsIgnoreCase("[Promote]")) return;

        if (line2.isEmpty()) {
            player.sendMessage(ChatColor.RED + "You must specify a rank to promote to on line two!");
            return;
        }

        if (line3.isEmpty()) {
            player.sendMessage(ChatColor.RED + "You must specify a cost to promote on line three!");
            return;
        }

        if (!Permissions.getConfiguredGroups().containsKey(line2)) {
            player.sendMessage(ChatColor.RED + "Invalid rank specified!");
            return;
        }

        double cost;

        try { cost = Double.parseDouble(line3); }

        catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid cost specified!");
            return;
        }

        event.setLine(0, ChatColor.GREEN + "[Promote]");
        event.setLine(1, ChatColor.AQUA + line2);
        event.setLine(2, ChatColor.GOLD + "$" + cost);

        player.sendMessage(ChatColor.GREEN + "Promotion sign created!");
    }

    @EventHandler
    public void onSignClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        if (!block.getType().name().contains("SIGN")) return;

        Sign sign = (Sign) block.getState();
        String line1 = sign.getSide(Side.FRONT).getLine(0);
        String line2 = sign.getSide(Side.FRONT).getLine(1);
        String line3 = sign.getSide(Side.FRONT).getLine(2);

        if (!line1.equalsIgnoreCase(ChatColor.GREEN + "[Promote]")) return;

        if (!player.hasPermission("aap.sign.use")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use promote signs!");
            return;
        }

        String group = ChatColor.stripColor(line2).toLowerCase();
        double cost;

        try { cost = Double.parseDouble(ChatColor.stripColor(line3).replace("$", "")); }

        catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid cost specified on sign!");
            return;
        }

        if (!Permissions.getConfiguredGroups().containsKey(group)) {
            player.sendMessage(ChatColor.RED + "Invalid rank specified on sign!");
            return;
        }

        if (!player.hasPermission("aap.buy." + group)) {
            player.sendMessage(ChatColor.RED + "You do not have permission to buy this rank!");
            return;
        }

        String currentGroup = Permissions.getCurrentGroup(player);

        if (Permissions.getGroupIndex(currentGroup) > Permissions.getGroupIndex(group)) {
            player.sendMessage(ChatColor.RED + "You cannot buy a rank that is the lower than your current rank!");
            return;
        } else if (Permissions.getGroupIndex(currentGroup) == Permissions.getGroupIndex(group)) {
            player.sendMessage(ChatColor.RED + "You are already at this rank!");
            return;
        }

        if (!Economy.hasEnough(player, cost)) {
            player.sendMessage(ChatColor.RED + "You do not have enough money to buy this rank!");
            return;
        }

        boolean success = Permissions.promotePlayer(player, group);

        if (!success && Permissions.getNextGroup(player) != null) {
            player.sendMessage(ChatColor.RED + "You may not skip ranks when promoting!");
            return;
        } else if (!success) {
            player.sendMessage(ChatColor.RED + "An error occurred while promoting you to " + group + "!");
            return;
        }

        Economy.withdraw(player, cost);
        player.sendMessage(ChatColor.GREEN + "You have been promoted to " + group + "!");
    }
}
