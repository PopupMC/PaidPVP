package com.popupmc.paidpvp;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class PaidPVP extends JavaPlugin {

    // A custom WorldGuard flag needs to be done before the plugin is enabled
    @Override
    public void onLoad() {
        setupWGFlag();
    }

    @Override
    public void onEnable() {
        plugin = this;

        // Setup Vault
        if (!setupEconomy() ) {
            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Register join and death events
        Bukkit.getPluginManager().registerEvents(new OnPlayerJoinEvent(), this);
        Bukkit.getPluginManager().registerEvents(new OnPlayerDeathEvent(), this);

        getLogger().info("PaidPVP is enabled.");
    }

    // This is how Vault reccomends to setup economy
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);

        if (rsp == null) {
            return false;
        }

        econ = rsp.getProvider();
        return true;
    }

    // This is how WorldGuard reccomends to setup a custom flag
    private void setupWGFlag() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();

        try {
            StateFlag flag = new StateFlag("pvp-arena", false);
            registry.register(flag);
            pvpArenaFlag = flag;
        } catch (FlagConflictException e) {
            getLogger().warning("double-pvp can't be registered, conflicts with another flag");
        } catch (Exception ex){
            getLogger().warning("A WG error occured and flag can't be set");
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("PaidPVP is disabled");
    }

    static JavaPlugin plugin;

    // How many kills per emerald
    final static int killsPerEmerald = 5;

    // What does an emerald cost?
    final static float exchangeRate = 0.02f;

    public static Economy econ = null;
    public static StateFlag pvpArenaFlag = null;
}
