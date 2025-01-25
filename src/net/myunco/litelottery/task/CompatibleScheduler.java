package net.myunco.litelottery.task;

public interface CompatibleScheduler {
    void runTask(Runnable task);

    void runTaskLater(Runnable task, long delay);

    void runTaskTimer(Runnable task, long delay, long period);

    void runTaskAsynchronously(Runnable task);

    void runTaskLaterAsynchronously(Runnable task, long delay);

    void runTaskTimerAsynchronously(Runnable task, long delay, long period);

    void cancelTasks();
}
