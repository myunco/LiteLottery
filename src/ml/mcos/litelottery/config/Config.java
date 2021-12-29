package ml.mcos.litelottery.config;

import ml.mcos.litelottery.LiteLottery;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Config {
    public static int lotteryHour;
    public static int lotteryMinute;
    public static boolean notice;
    public static int maxNumber;
    public static double initialPrizePool;
    public static double maxPrizePool;
    public static double moneyPerBets;
    public static int maxNums;
    public static int maxBets;
    public static double fifthPrize;
    public static double fifthPrizeMax;
    public static double fourthPrize;
    public static double fourthPrizeMax;
    public static double thirdPrize;
    public static double thirdPrizeMax;
    public static double secondPrize;
    public static double firstPrize;
    public static double specialPrize;
    public static double specialPrizeMax;
    public static boolean ignorePrizePool;
    public static int randomInterval;
    public static int randomMax;

    public static void init(LiteLottery plugin) {
        plugin.saveDefaultConfig();
        YamlConfiguration config = loadConfiguration(new File(plugin.getDataFolder(), "config.yml"), plugin);
        String drawTime = config.getString("lotteryTime");
        if (drawTime != null && drawTime.matches("[0-9]+:[0-9]+")) {
            String[] time = drawTime.split(":");
            lotteryHour = Integer.parseInt(time[0]);
            lotteryMinute = Integer.parseInt(time[1]);
            if (lotteryHour > 23) {
                plugin.getLogger().info("错误的开奖时: " + lotteryHour);
                lotteryHour = 23;
                plugin.getLogger().info("已将开奖时修正为: 23");
            }
            if (lotteryMinute > 59) {
                plugin.getLogger().info("错误的开奖分: " + lotteryMinute);
                lotteryMinute = 59;
                plugin.getLogger().info("已将开奖分修正为: 59");
            }
        } else {
            plugin.getLogger().info("错误,开奖时间不存在或格式不正确: " + drawTime);
            lotteryHour = 20;
            lotteryMinute = 30;
            plugin.getLogger().info("已将开奖时间设置为: 20:30");
        }
        notice = config.getBoolean("notice", true);
        maxNumber = config.getInt("maxNumber");
        if (maxNumber < 5) {
            maxNumber = 5;
        } else if (maxNumber > 99) {
            maxNumber = 99;
        }
        initialPrizePool = config.getDouble("initialPrizePool");
        if (initialPrizePool < 0) {
            initialPrizePool = 0;
        }
        maxPrizePool = config.getDouble("maxPrizePool");
        if (maxPrizePool < initialPrizePool) {
            maxPrizePool = initialPrizePool;
        }
        moneyPerBets = config.getDouble("moneyPerBets");
        if (moneyPerBets < 0) {
            moneyPerBets = 0;
        }
        maxNums = config.getInt("maxNums");
        if (maxNums < 0) {
            maxNums = 0;
        }
        maxBets = config.getInt("maxBets");
        if (maxBets < 0) {
            maxBets = 0;
        }
        fifthPrize = config.getDouble("fifthPrize");
        fifthPrizeMax = config.getDouble("fifthPrizeMax");
        if (fifthPrizeMax < fifthPrize) {
            fifthPrizeMax = fifthPrize;
        }
        fourthPrize = config.getDouble("fourthPrize");
        fourthPrizeMax = config.getDouble("fourthPrizeMax");
        if (fourthPrizeMax < fourthPrize) {
            fourthPrizeMax = fourthPrize;
        }
        thirdPrize = config.getDouble("thirdPrize");
        thirdPrizeMax = config.getDouble("thirdPrizeMax");
        if (thirdPrizeMax < thirdPrize) {
            thirdPrizeMax = thirdPrize;
        }
        secondPrize = config.getDouble("secondPrize");
        firstPrize = config.getDouble("firstPrize");
        specialPrize = config.getDouble("specialPrize");
        specialPrizeMax = config.getDouble("specialPrizeMax");
        if (specialPrizeMax < specialPrize) {
            specialPrizeMax = specialPrize;
        }
        ignorePrizePool = config.getBoolean("ignorePrizePool", true);
        randomInterval = config.getInt("randomInterval");
        if (randomInterval < 0) {
            randomInterval = 0;
        }
        randomMax = config.getInt("randomMax");
        if (randomMax < 0) {
            randomMax = 0;
        }
    }

    public static YamlConfiguration loadConfiguration(File file, LiteLottery plugin) {
        YamlConfiguration config = new YamlConfiguration();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            String line;
            try {
                while((line = reader.readLine()) != null) {
                    builder.append(line).append('\n');
                }
            } finally {
                reader.close();
            }
            config.loadFromString(builder.toString());
        } catch (Exception e) {
            plugin.getLogger().info(e.getMessage());
            plugin.getLogger().severe("错误, 无法加载配置文件: " + file.getAbsolutePath());
        }
        return config;
    }

}
