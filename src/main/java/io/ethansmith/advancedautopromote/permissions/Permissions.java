package io.ethansmith.advancedautopromote.permissions;

import io.ethansmith.advancedautopromote.AdvancedAutoPromote;
import lombok.Getter;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.data.DataMutateResult;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.group.GroupManager;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.track.PromotionResult;
import net.luckperms.api.track.Track;
import net.luckperms.api.track.TrackManager;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

public class Permissions {

    private static AdvancedAutoPromote plugin = AdvancedAutoPromote.getInstance();

    private static LuckPerms luckPermsProvider = null;
    private static UserManager userManager = null;
    private static GroupManager groupManager = null;
    private static TrackManager trackManager = null;
    private static Track promotionTrack = null;

    @Getter
    private final static LinkedHashMap<String, Double> configuredGroups = new LinkedHashMap<>();

    public static void init() {
        if (plugin.getServer().getPluginManager().getPlugin("LuckPerms") == null) {
            plugin.getLogger().severe("LuckPerms not found! Disabling this plugin.");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }

        luckPermsProvider = LuckPermsProvider.get();
        userManager = luckPermsProvider.getUserManager();
        groupManager = luckPermsProvider.getGroupManager();
        trackManager = luckPermsProvider.getTrackManager();
        promotionTrack = trackManager.getTrack("promotion");

        getConfigGroups();
        setupPromotionTrack();

        plugin.getLogger().info("Permissions manager initialized!");
    }

    public static boolean promotePlayer(Player player) {
        User user = luckPermsProvider.getPlayerAdapter(Player.class).getUser(player);
        PromotionResult result = promotionTrack.promote(user, luckPermsProvider.getContextManager().getStaticContext());
        userManager.saveUser(user);
        return result.wasSuccessful();
    }

    public static String getCurrentGroup(Player player) {
        User user = luckPermsProvider.getPlayerAdapter(Player.class).getUser(player);
        return user.getPrimaryGroup();
    }

    public static String getNextGroup(Player player) {
        User user = luckPermsProvider.getPlayerAdapter(Player.class).getUser(player);
        Group currentGroup = groupManager.getGroup(user.getPrimaryGroup());

        if (currentGroup == null) {
            plugin.getLogger().severe("Player " + player.getName() + " has no primary group!");
            return null;
        }

        return promotionTrack.getNext(currentGroup);
    }

    private static void getConfigGroups() {
        Set<String> groups  = plugin.getConfig().getConfigurationSection("groups").getKeys(false);
        LinkedHashMap<String, Double> groupsInConfig = new LinkedHashMap<>();

        groups.forEach(group -> groupsInConfig.put(group, plugin.getConfig().getDouble("groups." + group)));
        groupsInConfig.entrySet().stream().sorted(Map.Entry.comparingByValue()).forEachOrdered(e -> configuredGroups.put(e.getKey(), e.getValue()));
    }

    private static void setupPromotionTrack() {
        if (promotionTrack == null) {
            try { promotionTrack = trackManager.createAndLoadTrack("promotion").get(); }

            catch (InterruptedException | ExecutionException e) {
                plugin.getLogger().severe("Failed to create promotion track!");
                plugin.getLogger().severe(e.getMessage());
            }

            plugin.getLogger().info("Created promotion track!");
        }

        List<String> currentTrack = promotionTrack.getGroups();
        removeInvalidGroups(currentTrack);
        addConfiguredGroups(currentTrack);
    }

    private static void removeInvalidGroups(List<String> currentTrack) {
        currentTrack.forEach(group -> {
            if (configuredGroups.containsKey(group)) return;
            plugin.getServer().getLogger().info("Removing group " + group + " from promotion track because it is not in the config.");

            DataMutateResult removeResult = promotionTrack.removeGroup(group);

            if (removeResult.wasSuccessful()) {
                trackManager.saveTrack(promotionTrack);
                return;
            }

            plugin.getLogger().severe("Failed to remove group " + group + " from promotion track!");
        });
    }

    private static void addConfiguredGroups(List<String> currentTrack) {
        AtomicInteger i = new AtomicInteger();
        configuredGroups.forEach((group, cost) -> {
            if (currentTrack.contains(group)) {
                i.getAndIncrement();
                return;
            }

            Group luckPermsGroup = groupManager.getGroup(group);

            if (luckPermsGroup == null) {
                plugin.getLogger().severe("Group " + group + " not found in LuckPerms!");
                return;
            }

            plugin.getServer().getLogger().info("Adding group " + group + " to promotion track.");
            DataMutateResult addResult = promotionTrack.insertGroup(luckPermsGroup, i.get());

            if (addResult.wasSuccessful()) {
                trackManager.saveTrack(promotionTrack);
                i.getAndIncrement();
                return;
            }

            plugin.getLogger().severe("Failed to add group " + group + " to promotion track!");
        });
    }
}
