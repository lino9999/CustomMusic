package com.lino.custommusic.hook;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyHook {
    private Economy economy;

    public void init() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return;
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            economy = rsp.getProvider();
        }
    }

    public boolean isAvailable() {
        return economy != null;
    }

    public double getBalance(Player player) {
        if (economy == null || player == null) return 0.0;
        return economy.getBalance(player);
    }

    public boolean has(Player player, double amount) {
        if (economy == null || player == null) return true;
        return economy.has(player, amount);
    }

    public boolean withdraw(Player player, double amount) {
        if (economy == null || player == null) return true;
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }
}
