package io.ethansmith.advancedautopromote;

import io.ethansmith.advancedautopromote.command.BuyCommand;
import io.ethansmith.advancedautopromote.command.RankupCommand;
import io.ethansmith.advancedautopromote.economy.Economy;
import io.ethansmith.advancedautopromote.permissions.Permissions;
import io.ethansmith.advancedautopromote.signs.SignListener;
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

        if (getConfig().getBoolean("rankup-enabled"))
            getCommand("rankup").setExecutor(new RankupCommand());

        if (getConfig().getBoolean("buy-rank-enabled")) {
            getCommand("buy-rank").setExecutor(new BuyCommand());
            getCommand("buy-rank").setTabCompleter(new BuyCommand());
        }

        if (getConfig().getBoolean("sign-rankup-enabled"))
            getServer().getPluginManager().registerEvents(new SignListener(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
