package ml.mcos.litelottery.economy;

import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class Points implements Currency {
    private final PlayerPointsAPI pointsAPI;

    public Points(PlayerPointsAPI pointsAPI) {
        this.pointsAPI = pointsAPI;
    }

    @Override
    public String getName() {
        return "PlayerPoints";
    }

    @Override
    public boolean has(Player player, double amount) {
        return pointsAPI.look(player.getUniqueId()) >= amount;
    }

    @Override
    public boolean depositPlayer(OfflinePlayer player, double amount) {
        return pointsAPI.give(player.getUniqueId(), (int) amount);
    }

    @Override
    public boolean withdrawPlayer(Player player, double amount) {
        return pointsAPI.take(player.getUniqueId(), (int) amount);
    }
}
