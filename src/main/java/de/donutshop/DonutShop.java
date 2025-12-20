package de.donutshop;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class DonutShop extends JavaPlugin {

    private static Economy economy = null;

    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            getLogger().severe("Vault not found or economy provider not available! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        saveDefaultConfig();
        getCommand("shop").setExecutor(new ShopCommand(this));
        getCommand("adminshop").setExecutor(new AdminShopCommand(this));
        getServer().getPluginManager().registerEvents(new ShopGUI(), this);
        getServer().getPluginManager().registerEvents(new AdminShopGUI(this), this);
        getLogger().info("DonutShop has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("DonutShop has been disabled!");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    public static Economy getEconomy() {
        return economy;
    }
}
