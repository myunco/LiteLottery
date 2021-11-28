package ml.mcos.litelottery;

import ml.mcos.litelottery.metrics.Metrics;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class LiteLottery extends JavaPlugin {
    int mcVersion = getMinecraftVersion();
    ConsoleCommandSender consoleSender = getServer().getConsoleSender();
    BukkitScheduler bukkitScheduler = getServer().getScheduler();
    int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    Economy economy;
    Lottery lottery;

    @Override
    public void onEnable() {
        getLogger().info("Minecraft version = 1." + mcVersion);
        if (init()) {
            consoleSender.sendMessage("[LiteLottery] using economy system: §3" + economy.getName());
            bukkitScheduler.runTaskTimerAsynchronously(this, () -> {
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
            new Metrics(this, 13291).addCustomChart(new Metrics.SimplePie("economy_plugin", () -> economy.getName()));
        } else {
            consoleSender.sendMessage("[LiteLottery] §c初始化失败, 插件无法继续加载.");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().severe("未找到Vault，请检查是否正确安装Vault插件！");
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            getLogger().severe("未找到经济系统，请检查是否正确安装经济提供插件！(如Ess、CMI、Economy等)");
            return false;
        }
        economy = rsp.getProvider();
        return true;
    }

    private boolean init() {
        Config.init(this);
        if (setupEconomy()) {
            lottery = new Lottery(this, economy);
            return lottery.isOK;
        }
        return false;
    }

    public int getMinecraftVersion() {
        return Integer.parseInt(getServer().getBukkitVersion().replace('-', '.').split("\\.")[1]);
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
            sendMessage(sender, "§c本命令只能玩家使用。");
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
                sendMessage(sender, "§a当前版本: §b" + getDescription().getVersion());
                break;
            case "reload":
                Config.init(this);
                lottery.initNumber();
                if (!lottery.file.exists()) {
                    lottery.prizePool = 0.0;
                    lottery.init();
                }
                sendMessage(sender, "§a配置文件重载完成。");
                break;
            case "run":
                if (lottery.isRunLottery) {
                    sendMessage(sender, "§c今天已经开过奖了!");
                } else {
                    lottery.tryRunLottery();
                }
                break;
            case "force":
                if (lottery.isRunLottery && !lottery.runLotteryFinish) {
                    sendMessage(sender, "§c当前正在开奖, 你必须等待开奖结束才能再次开奖!");
                } else {
                    lottery.isRunLottery = false;
                    lottery.tryRunLottery();
                }
                break;
            case "forcefalse":
                if (lottery.isRunLottery && !lottery.runLotteryFinish) {
                    sendMessage(sender, "§c当前正在开奖, 你必须等待开奖结束才能设置开奖状态!");
                } else {
                    lottery.isRunLottery = false;
                    sendMessage(sender, "§a已将开奖状态设为§b未开奖§a。");
                }
                break;
            default:
                sendMessage(sender, "§6错误: 未知的命令参数");
        }
    }

    private void sendMessage(CommandSender sender, String s) {
        sender.sendMessage("§7§l[§c§lLottery§7§l] " + s);
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
                list.removeAll(mergeNums(args));
                return TabComplete.getCompleteList(args, list);
            }
        }
        return TabComplete.getCompleteList(args, TabComplete.getTabList(args, command.getName()));
    }

    private static ArrayList<String> mergeNums(String[] args) {
        ArrayList<String> nums = new ArrayList<>();
        for (int i = 1; i < args.length; i++) {
            if (args[i].isEmpty()) {
                continue;
            }
            nums.add(args[i]);
        }
        return nums;
    }

}
