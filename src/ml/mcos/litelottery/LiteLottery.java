package ml.mcos.litelottery;

import ml.mcos.litelottery.config.Config;
import ml.mcos.litelottery.config.Messages;
import ml.mcos.litelottery.core.Lottery;
import ml.mcos.litelottery.economy.Currency;
import ml.mcos.litelottery.economy.Money;
import ml.mcos.litelottery.economy.Points;
import ml.mcos.litelottery.metrics.Metrics;
import ml.mcos.litelottery.util.Version;
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
    private int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    public Version mcVersion;
    private Currency currency;
    private Lottery lottery;

    @Override
    public void onEnable() {
        mcVersion = new Version(getServer().getBukkitVersion());
        getLogger().info("Minecraft version = " + mcVersion);
        init();
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
        getServer().getConsoleSender().sendMessage("[LiteLottery] Found EconomyProvider: §3v" + rsp.getPlugin().getDescription().getVersion());
        currency = new Money(rsp.getProvider());
    }

    public void setupPoints() {
        PlayerPoints playerPoints = (PlayerPoints) getServer().getPluginManager().getPlugin("PlayerPoints");
        if (playerPoints == null || !playerPoints.isEnabled()) {
            getLogger().severe("未找到点券插件，请检查是否正确安装PlayerPoints插件！");
            return;
        }
        currency = new Points(playerPoints.getAPI());
        getServer().getConsoleSender().sendMessage("[LiteLottery] Found PlayerPoints: §3v" + playerPoints.getDescription().getVersion());
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
            getServer().getConsoleSender().sendMessage("[LiteLottery] using currency system: §3" + currency.getName());
            lottery = new Lottery(this, currency);
            if (lottery.isOK) {
                getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
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
                return;
            }
        }
        getServer().getConsoleSender().sendMessage("[LiteLottery] §c初始化失败, 插件无法继续加载.");
        getServer().getPluginManager().disablePlugin(this);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equals("LiteLottery")) {
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

    @SuppressWarnings("SpellCheckingInspection")
    private void commandLiteLottery(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sendMessage(sender, "§7/LiteLottery §3<§7version§3|§7reload§3|§7run§3|§7force§3|§7forcefalse§3>");
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
                if (lottery.isRunLottery) {
                    sendMessage(sender, Messages.alreadyDrawn);
                } else {
                    lottery.tryRunLottery();
                }
                break;
            case "force":
                if (lottery.isRunLottery && !lottery.runLotteryFinish) {
                    sendMessage(sender, Messages.drawingInProgress);
                } else {
                    lottery.isRunLottery = false;
                    lottery.tryRunLottery();
                }
                break;
            case "forcefalse":
                if (lottery.isRunLottery && !lottery.runLotteryFinish) {
                    sendMessage(sender, Messages.settingDrawState);
                } else {
                    lottery.forceFalse();
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

    @SuppressWarnings("NullableProblems")
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equals("Lottery") && args.length > 1 && sender instanceof Player) {
            if (args.length < 7 && !args[1].equalsIgnoreCase("random")) {
                ArrayList<String> list = new ArrayList<>(lottery.numList);
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
}
