package ml.mcos.litelottery;

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
        plugin.reloadConfig();
        String drawTime = plugin.getConfig().getString("lotteryTime");
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
        notice = plugin.getConfig().getBoolean("notice", true);
        maxNumber = plugin.getConfig().getInt("maxNumber");
        if (maxNumber < 5) {
            maxNumber = 5;
        } else if (maxNumber > 99) {
            maxNumber = 99;
        }
        initialPrizePool = plugin.getConfig().getDouble("initialPrizePool");
        if (initialPrizePool < 0) {
            initialPrizePool = 0;
        }
        maxPrizePool = plugin.getConfig().getDouble("maxPrizePool");
        if (maxPrizePool < initialPrizePool) {
            maxPrizePool = initialPrizePool;
        }
        moneyPerBets = plugin.getConfig().getDouble("moneyPerBets");
        if (moneyPerBets < 0) {
            moneyPerBets = 0;
        }
        maxNums = plugin.getConfig().getInt("maxNums");
        if (maxNums < 0) {
            maxNums = 0;
        }
        maxBets = plugin.getConfig().getInt("maxBets");
        if (maxBets < 0) {
            maxBets = 0;
        }
        fifthPrize = plugin.getConfig().getDouble("fifthPrize");
        fifthPrizeMax = plugin.getConfig().getDouble("fifthPrizeMax");
        if (fifthPrizeMax < fifthPrize) {
            fifthPrizeMax = fifthPrize;
        }
        fourthPrize = plugin.getConfig().getDouble("fourthPrize");
        fourthPrizeMax = plugin.getConfig().getDouble("fourthPrizeMax");
        if (fourthPrizeMax < fourthPrize) {
            fourthPrizeMax = fourthPrize;
        }
        thirdPrize = plugin.getConfig().getDouble("thirdPrize");
        thirdPrizeMax = plugin.getConfig().getDouble("thirdPrizeMax");
        if (thirdPrizeMax < thirdPrize) {
            thirdPrizeMax = thirdPrize;
        }
        secondPrize = plugin.getConfig().getDouble("secondPrize");
        firstPrize = plugin.getConfig().getDouble("firstPrize");
        specialPrize = plugin.getConfig().getDouble("specialPrize");
        specialPrizeMax = plugin.getConfig().getDouble("specialPrizeMax");
        if (specialPrizeMax < specialPrize) {
            specialPrizeMax = specialPrize;
        }
        ignorePrizePool = plugin.getConfig().getBoolean("ignorePrizePool", true);
        randomInterval = plugin.getConfig().getInt("randomInterval");
        if (randomInterval < 0) {
            randomInterval = 0;
        }
        randomMax = plugin.getConfig().getInt("randomMax");
        if (randomMax < 0) {
            randomMax = 0;
        }
    }

}
