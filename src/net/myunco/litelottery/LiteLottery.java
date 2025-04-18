package net.myunco.litelottery;

import net.myunco.folia.FoliaCompatibleAPI;
import net.myunco.folia.scheduler.CompatibleScheduler;
import net.myunco.litelottery.config.Config;
import net.myunco.litelottery.config.Messages;
import net.myunco.litelottery.core.Lottery;
import net.myunco.litelottery.economy.Currency;
import net.myunco.litelottery.economy.Money;
import net.myunco.litelottery.economy.Points;
import net.myunco.litelottery.metrics.Metrics;
import net.myunco.litelottery.papi.LiteLotteryExpansion;
import net.myunco.litelottery.update.UpdateChecker;
import net.myunco.litelottery.update.UpdateNotification;
import net.myunco.litelottery.util.TabComplete;
import net.myunco.litelottery.util.Version;
import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPoints;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
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
    public Lottery lottery;
    private CompatibleScheduler scheduler;
    private ConsoleCommandSender console;
    private boolean ready = false;

    @Override
    public void onEnable() {
        plugin = this;
        mcVersion = new Version(getServer().getBukkitVersion());
        console = getServer().getConsoleSender();
        initFoliaCompatibleAPI();
        getLogger().info("Minecraft version = " + mcVersion);
        init();
        UpdateChecker.start(this);
        getServer().getPluginManager().registerEvents(new UpdateNotification(), this);
        new Metrics(this, 13291).addCustomChart(new Metrics.SimplePie("economy_plugin", () -> currency.getName()));
        setupPAPI();
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

    public void setupPAPI() {
        Plugin papi = getServer().getPluginManager().getPlugin("PlaceholderAPI");
        if (papi != null && papi.isEnabled()) {
            logMessage("Found PlaceHolderAPI: §3v" + papi.getDescription().getVersion());
            new LiteLotteryExpansion().register();
        }
    }

    public void initFoliaCompatibleAPI() {
        Plugin api = getServer().getPluginManager().getPlugin("FoliaCompatibleAPI");
        if (api == null) {
            getLogger().warning("FoliaCompatibleAPI not found!");
            File file = new File(getDataFolder().getParentFile(), "FoliaCompatibleAPI-1.2.0.jar");
            InputStream in = getResource("lib/FoliaCompatibleAPI-1.2.0.jar");
            try {
                saveResource(file, in);
                api = getServer().getPluginManager().loadPlugin(file);
                if (api == null) {
                    throw new Exception("FoliaCompatibleAPI load failed!");
                }
                getServer().getPluginManager().enablePlugin(api);
                api.onLoad();
            } catch (Exception e) {
                e.printStackTrace();
                getLogger().severe("未安装 FoliaCompatibleAPI ，本插件无法运行！");
                return;
            }
        }
        scheduler = ((FoliaCompatibleAPI) api).getScheduler(this);
        logMessage("Found FoliaCompatibleAPI: §3v" + api.getDescription().getVersion());
    }

    private void saveResource(File target, InputStream source) throws Exception {
        if (source != null) {
            //noinspection IOStreamConstructor
            OutputStream out = new FileOutputStream(target);
            byte[] buf = new byte[8192];
            int len;
            while ((len = source.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            out.close();
            source.close();
        }
    }

    @Override
    public void onDisable() {
        UpdateChecker.stop();
        scheduler.cancelTasks();
    }

    public void logMessage(String msg) {
        console.sendMessage("[LiteLottery] " + msg);
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

    @SuppressWarnings("NullableProblems")
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equals("LiteLottery")) {
            commandLiteLottery(sender, args);
        } else {
            if (!ready) {
                sendMessage(sender, "§c初始化失败, 插件无法使用, 请检查控制台报错.");
            } else {
                commandLottery(sender, args);
            }
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
        if (args.length < 1) {
            sendMessage(sender, "§7/LiteLottery §3<§7version§3|§7reload§3|§7run§3|§7force§3|§7reset§3|§7betting§3|§7bettingWithPlayer§3>");
            return;
        }
        String cmd = args[0].toLowerCase();
        if (!ready && !cmd.equals("version") && !cmd.equals("reload")) {
            sendMessage(sender, "§c初始化失败, 插件无法使用, 请检查控制台报错.");
            return;
        }
        switch (cmd) {
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
            case "betting":
                if (args.length < 4) {
                    sendMessage(sender, "§6用法: §7/lot betting <玩家名> <投注数量> <投注号码>");
                } else {
                    Player player = getServer().getPlayerExact(args[1]);
                    if (player == null) {
                        sendMessage(sender, "§c错误: 指定的玩家不存在或未在线");
                        return;
                    }
                    lottery.consoleBetting(sender, player, args);
                }
                break;
            case "bettingwithplayer":
                if (args.length < 4) {
                    sendMessage(sender, "§6用法: §7/lot bettingWithPlayer <玩家名> <投注数量> <投注号码>");
                } else {
                    Player player = getServer().getPlayerExact(args[1]);
                    if (player == null) {
                        sendMessage(sender, "§c错误: 指定的玩家不存在或未在线");
                        return;
                    }
                    lottery.placeBet(player, Arrays.copyOfRange(args, 2, args.length));
                }
                break;
            default:
                sendMessage(sender, Messages.unknownCommandArgs);
        }
    }

    public void sendMessage(CommandSender sender, String msg) {
        sender.sendMessage(Messages.messagePrefix + msg);
    }

    @SuppressWarnings("NullableProblems")
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
        return TabComplete.getCompleteList(args, TabComplete.getTabList(args, command.getName()), true);
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

    public CompatibleScheduler getScheduler() {
        return scheduler;
    }

    public static LiteLottery getPlugin() {
        return plugin;
    }
}
