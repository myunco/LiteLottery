package net.myunco.litelottery;

import net.myunco.litelottery.config.Config;
import net.myunco.litelottery.config.Messages;
import net.myunco.litelottery.core.Lottery;
import net.myunco.litelottery.economy.Currency;
import net.myunco.litelottery.economy.Money;
import net.myunco.litelottery.economy.Points;
import net.myunco.litelottery.metrics.Metrics;
import net.myunco.litelottery.task.BukkitScheduler;
import net.myunco.litelottery.task.CompatibleScheduler;
import net.myunco.litelottery.task.FoliaScheduler;
import net.myunco.litelottery.update.UpdateChecker;
import net.myunco.litelottery.update.UpdateNotification;
import net.myunco.litelottery.util.TabComplete;
import net.myunco.litelottery.util.Version;
import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPoints;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class LiteLottery extends JavaPlugin {
    public static LiteLottery plugin;
    private int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    public Version mcVersion;
    private Currency currency;
    private Lottery lottery;
    private CompatibleScheduler scheduler;
    private boolean ready = false;
    public boolean isFolia = isFolia();

    @Override
    public void onEnable() {
        plugin = this;
        mcVersion = new Version(getServer().getBukkitVersion());
        getLogger().info("Minecraft version = " + mcVersion);
        init();
        UpdateChecker.start(this);
        getServer().getPluginManager().registerEvents(new UpdateNotification(), this);
        new Metrics(this, 13291).addCustomChart(new Metrics.SimplePie("economy_plugin", () -> currency.getName()));
    }

    private void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().severe("未找到Vault，请检查是否正确安装Vault插件！");
            return;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            getLogger().severe("未找到经济系统，请检查是否正确安装经济提供插件！(如Ess、CMI、Economy等)");
            return;
        }
        currency = new Money(rsp.getProvider());
    }

    @Override
    public void onDisable() {
        UpdateChecker.stop();
        scheduler.cancelTasks();
    }

    public void logMessage(String msg) {
        getServer().getConsoleSender().sendMessage("[LiteLottery] " + msg);
    }

    public void setupPoints() {
        PlayerPoints playerPoints = (PlayerPoints) getServer().getPluginManager().getPlugin("PlayerPoints");
        if (playerPoints == null || !playerPoints.isEnabled()) {
            getLogger().severe("未找到点券插件，请检查是否正确安装PlayerPoints插件！");
            return;
        }
        currency = new Points(playerPoints.getAPI());
        logMessage("Found PlayerPoints: §3v" + playerPoints.getDescription().getVersion());
    }

    private void init() {
        Config.init(this);
        Messages.init(this);
        if (Config.usePoints) {
            setupPoints();
        } else {
            setupEconomy();
        }
        if (currency != null) {
            logMessage("using currency system: §3" + currency.getName());
            lottery = new Lottery(this, currency);
            if (lottery.isInitialized) {
                getScheduler().runTaskTimerAsynchronously(() -> {
                    Calendar cal = Calendar.getInstance();
                    if (lottery.checkTime(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))) {
                        lottery.tryRunLottery();
                    }
                    if (cal.get(Calendar.DAY_OF_MONTH) != day) {
                        if (lottery.tryUpdate()) {
                            day = cal.get(Calendar.DAY_OF_MONTH);
                        }
                    }
                }, 100, 100);
                ready = true;
                return;
            }
        }
        logMessage("§c初始化失败, 插件无法继续加载.");
        ready = false;
        // getServer().getPluginManager().disablePlugin(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!ready) {
            sendMessage(sender, "§c初始化失败, 插件无法使用, 请检查控制台报错.");
        } else if (command.getName().equals("LiteLottery")) {
            commandLiteLottery(sender, args);
        } else {
            commandLottery(sender, args);
        }
        return true;
    }

    private void commandLottery(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length == 0) {
                lottery.showLotteryInfo(player);
            } else {
                lottery.placeBet(player, args);
            }
        } else {
            sendMessage(sender, Messages.playerOnly);
        }
    }

    private void commandLiteLottery(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sendMessage(sender, "§7/LiteLottery §3<§7version§3|§7reload§3|§7run§3|§7force§3|§7reset§3>");
            return;
        }
        switch (args[0].toLowerCase()) {
            case "version":
                sendMessage(sender, Messages.versionInfo + getDescription().getVersion());
                break;
            case "reload":
                init();
                sendMessage(sender, Messages.configReloaded);
                break;
            case "run":
                if (lottery.drawing) {
                    sendMessage(sender, Messages.alreadyDrawn);
                } else {
                    lottery.tryRunLottery();
                }
                break;
            case "force":
                if (lottery.drawing && !lottery.drawCompleted) {
                    sendMessage(sender, Messages.drawingInProgress);
                } else {
                    lottery.drawing = false;
                    lottery.tryRunLottery();
                }
                break;
            case "reset":
                if (lottery.drawing && !lottery.drawCompleted) {
                    sendMessage(sender, Messages.settingDrawState);
                } else {
                    lottery.resetDrawState();
                    sendMessage(sender, Messages.drawStateReset);
                }
                break;
            default:
                sendMessage(sender, Messages.unknownCommandArgs);
        }
    }

    private void sendMessage(CommandSender sender, String msg) {
        sender.sendMessage(Messages.messagePrefix + msg);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equals("Lottery") && args.length > 1 && sender instanceof Player) {
            if (args.length < 7 && !args[1].equalsIgnoreCase("random")) {
                ArrayList<String> list = new ArrayList<>(lottery.availableNumbers);
                if (args.length == 2) {
                    list.add("random");
                }
                list.removeAll(asList(args));
                return TabComplete.getCompleteList(args, list);
            }
        }
        return TabComplete.getCompleteList(args, TabComplete.getTabList(args, command.getName()));
    }

    private static ArrayList<String> asList(String[] args) {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 1; i < args.length; i++) {
            if (args[i].isEmpty()) {
                continue;
            }
            list.add(args[i]);
        }
        return list;
    }

    private Method getOnlinePlayers;
    public Collection<? extends Player> getOnlinePlayers() {
        if (mcVersion.getMinor() > 7 || (mcVersion.getMinor() == 7 && mcVersion.getPatch() == 10)) {
            return getServer().getOnlinePlayers();
        }
        try {
            if (getOnlinePlayers == null) {
                getOnlinePlayers = Class.forName("org.bukkit.Server").getMethod("getOnlinePlayers");
            }
            return Arrays.asList((Player[]) getOnlinePlayers.invoke(getServer()));
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public CompatibleScheduler getScheduler() {
        if (scheduler == null) {
            if (isFolia) {
                scheduler = new FoliaScheduler(this);
            } else {
                scheduler = new BukkitScheduler(this);
            }
        }
        return scheduler;
    }

    public static LiteLottery getPlugin() {
        return plugin;
    }
}
