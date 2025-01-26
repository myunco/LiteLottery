package net.myunco.litelottery.task;

import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;

public class FoliaScheduler implements CompatibleScheduler {
    private final Plugin plugin;
    private final GlobalRegionScheduler scheduler;
    private final AsyncScheduler asyncScheduler;

    public FoliaScheduler(Plugin plugin) {
        this.plugin = plugin;
        this.scheduler = plugin.getServer().getGlobalRegionScheduler();
        this.asyncScheduler = plugin.getServer().getAsyncScheduler();
    }

    @Override
    public void runTask(Runnable task) {
        scheduler.run(plugin, scheduledTask -> task.run());
    }

    @Override
    public void runTaskLater(Runnable task, long delay) {
        scheduler.runDelayed(plugin, scheduledTask -> task.run(), delay);
    }

    @Override
    public void runTaskTimer(Runnable task, long delay, long period) {
        scheduler.runAtFixedRate(plugin, scheduledTask -> task.run(), delay, period);
    }

    @Override
    public void runTaskAsynchronously(Runnable task) {
        asyncScheduler.runNow(plugin, tscheduledTask -> task.run());
    }

    @Override
    public void runTaskLaterAsynchronously(Runnable task, long delay) {
        asyncScheduler.runDelayed(plugin, tscheduledTask -> task.run(), delay * 50, TimeUnit.MILLISECONDS);
    }

    @Override
    public void runTaskTimerAsynchronously(Runnable task, long delay, long period) {
        asyncScheduler.runAtFixedRate(plugin, tscheduledTask -> task.run(), delay * 50, period * 50, TimeUnit.MILLISECONDS);
    }

    @Override
    public void cancelTasks() {
        scheduler.cancelTasks(plugin);
        asyncScheduler.cancelTasks(plugin);
    }
}
