package net.myunco.litelottery.task;

import org.bukkit.plugin.Plugin;

public class BukkitScheduler implements CompatibleScheduler {
    private final Plugin plugin;
    private final org.bukkit.scheduler.BukkitScheduler scheduler;

    public BukkitScheduler(Plugin plugin) {
        this.plugin = plugin;
        this.scheduler = plugin.getServer().getScheduler();
    }

    @Override
    public void runTask(Runnable task) {
        scheduler.runTask(plugin, task);
    }

    @Override
    public void runTaskLater(Runnable task, long delay) {
        scheduler.runTaskLater(plugin, task, delay);
    }

    @Override
    public void runTaskTimer(Runnable task, long delay, long period) {
        scheduler.runTaskTimer(plugin, task, delay, period);
    }

    @Override
    public void runTaskAsynchronously(Runnable task) {
        scheduler.runTaskAsynchronously(plugin, task);
    }

    @Override
    public void runTaskLaterAsynchronously(Runnable task, long delay) {
        scheduler.runTaskLaterAsynchronously(plugin, task, delay);
    }

    @Override
    public void runTaskTimerAsynchronously(Runnable task, long delay, long period) {
        scheduler.runTaskTimerAsynchronously(plugin, task, delay, period);
    }

    @Override
    public void cancelTasks() {
        scheduler.cancelTasks(plugin);
    }
}
