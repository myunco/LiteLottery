package net.myunco.litelottery.economy;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public interface Currency {
    String getName();

    boolean has(Player player, double amount);

    boolean depositPlayer(OfflinePlayer player, double amount);

    boolean withdrawPlayer(Player player, double amount);
}
