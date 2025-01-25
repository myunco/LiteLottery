package net.myunco.litelottery.economy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class Money implements Currency {
    private final Economy economy;

    public Money(Economy economy) {
        this.economy = economy;
    }

    @Override
    public String getName() {
        return economy.getName();
    }

    @Override
    public boolean has(Player player, double amount) {
        return economy.has(player, amount);
    }

    @Override
    public boolean depositPlayer(OfflinePlayer player, double amount) {
        return economy.depositPlayer(player, amount).transactionSuccess();
    }

    @Override
    public boolean withdrawPlayer(Player player, double amount) {
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }
}
