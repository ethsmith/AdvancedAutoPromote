package io.ethansmith.advancedautopromote;

import io.ethansmith.advancedautopromote.command.RankupCommand;
import io.ethansmith.advancedautopromote.economy.Economy;
import io.ethansmith.advancedautopromote.permissions.Permissions;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public final class AdvancedAutoPromote extends JavaPlugin {

    @Getter
    private static AdvancedAutoPromote instance;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        saveDefaultConfig();

        getLogger().info("Initializing economy manager...");
        Economy.init();

        getLogger().info("Initializing permissions manager...");
        Permissions.init();

        getCommand("rankup").setExecutor(new RankupCommand());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
