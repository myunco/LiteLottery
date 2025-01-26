package net.myunco.litelottery.update;

import net.myunco.litelottery.LiteLottery;
import net.myunco.litelottery.config.Messages;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.UUID;

public class UpdateNotification implements Listener {
    private int day = LocalDate.now().getDayOfMonth();
    private final ArrayList<UUID> notifiedPlayers = new ArrayList<>();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (UpdateChecker.isUpdateAvailable && event.getPlayer().hasPermission("LiteLottery.admin")) {
            if (day != LocalDate.now().getDayOfMonth()) {
                day = LocalDate.now().getDayOfMonth();
                if (!notifiedPlayers.isEmpty()) {
                    notifiedPlayers.clear();
                }
            }
            if (!notifiedPlayers.contains(event.getPlayer().getUniqueId())) {
                notifiedPlayers.add(event.getPlayer().getUniqueId());
                LiteLottery.getPlugin().getScheduler().runTaskLaterAsynchronously(() -> {
                    event.getPlayer().sendMessage(Messages.messagePrefix + UpdateChecker.newVersion);
                    event.getPlayer().sendMessage(Messages.messagePrefix + UpdateChecker.downloadLink);
                }, 60);
            }
        }
    }

}
