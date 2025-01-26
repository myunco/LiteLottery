package net.myunco.litelottery.core;

import net.myunco.litelottery.LiteLottery;
import net.myunco.litelottery.config.Config;
import net.myunco.litelottery.config.Messages;
import net.myunco.litelottery.economy.Currency;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class Lottery {
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static final DecimalFormat decimalFormat = new DecimalFormat(",##0.00");
    private YamlConfiguration lotteryData;
    private final LiteLottery plugin;
    private final Currency currency;
    private File dataFile;
    public boolean isInitialized;
    private double prizePoolBalance = -1;
    public boolean drawing;
    public boolean drawCompleted;
    private boolean drawNotice;
    public List<String> availableNumbers = new ArrayList<>();
    private String formattedNumbers;
    private String gameIntroduction;
    private final File dataFolder;

    public Lottery(LiteLottery plugin, Currency currency) {
        this.plugin = plugin;
        this.currency = currency;
        this.dataFolder = new File(plugin.getDataFolder(), "data/");
        this.dataFile = new File(dataFolder, getFileName());
        init();
        initNumber();
    }

    public void initNumber() {
        if (!availableNumbers.isEmpty()) {
            availableNumbers.clear();
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i <= Config.maxNumber; i++) {
            String num = getNum(i);
            builder.append(num);
            availableNumbers.add(num);
            if (i < Config.maxNumber) {
                builder.append(' ');
            }
        }
        formattedNumbers = builder.toString();
    }

    public static String getNum(int n) {
        return n < 10 ? "0" + n : String.valueOf(n);
    }

    public void init() {
        String lastFileName = getLastFileName();
        if (!dataFile.exists()) {
            try {
                if (!dataFile.createNewFile()) {
                    plugin.getServer().getConsoleSender().sendMessage(Messages.messagePrefix + Messages.fileCreationFailed);
                    isInitialized = false;
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
                isInitialized = false;
                return;
            }
        }
        lotteryData = Config.loadConfiguration(dataFile, plugin);
        if (!lotteryData.contains("prizePoolBalance")) {
            if (prizePoolBalance == -1) { // 如果奖池余额为-1，则表示是第一次运行init，需要初始化奖池余额。
                if (lastFileName.isEmpty()) {
                    prizePoolBalance = Config.initialPrizePool; // 新一期奖池余额 = 初始奖池
                } else {
                    YamlConfiguration lastLotInfo = Config.loadConfiguration(new File(dataFolder, lastFileName), plugin);
                    prizePoolBalance = lastLotInfo.getDouble("prizePoolBalance"); // 奖池余额 = 上一期奖池余额
                    addPrizePool(Config.initialPrizePool); // 新一期奖池余额 = 上期奖池余额 + 初始奖池
                }
            } else {
                addPrizePool(Config.initialPrizePool); // 新一期奖池余额 = 上期剩余奖池 + 初始奖池
            }
            save();
        } else {
            prizePoolBalance = lotteryData.getDouble("prizePoolBalance"); // 如果奖池余额存在，则直接读取。
        }
        drawing = lotteryData.getBoolean("drawCompleted");
        drawCompleted = drawing;
        drawNotice = false;
        gameIntroduction = String.format(Messages.gameIntroduction,
                formatDecimal(Config.moneyPerBet),  getLotteryTime(),
                formatDecimal(Config.fifthPrize), formatDecimal(Config.fourthPrize),
                formatDecimal(Config.thirdPrize), formatDecimal(Config.secondPrize),
                formatDecimal(Config.firstPrize), formatDecimal(Config.specialPrize));
        isInitialized = true;
    }

    private void save() {
        try {
            lotteryData.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getFileName() {
        return simpleDateFormat.format(new Date()) + ".yml";
    }

    private String getLastFileName() {
        // 文件名的格式是yyyy-MM-dd.yml 例如2025-01-21.yml，但上一次的文件名是不确定的，因为不能保证插件每天都在运行，如果有一天没有运行，那么上一次的文件名就不是昨天了。
        // 本方法的目的是获取离今天最近的一个存在的文件名，例如今天是2025-01-21，那么上一次的文件名可能是2025-01-15，因为2025-01-16到2025-01-20这几天插件没有运行。
        // 本方法的实现思路是遍历dataFolder目录下的所有名为yyyy-MM-dd格式的.yml文件，找到离今天最近的一个文件名，如果没有找到就返回空字符串。
        File[] files = dataFolder.listFiles((dir, name) -> name.matches("\\d{4}-\\d{2}-\\d{2}\\.yml"));
        if (files == null || files.length == 0) {
            return "";
        }
        String latestFileName = "";
        Date latestDate = null;
        for (File file : files) {
            String fileName = file.getName();
            try {
                Date fileDate = simpleDateFormat.parse(fileName.substring(0, 10));
                if (latestDate == null || fileDate.after(latestDate)) {
                    latestDate = fileDate;
                    latestFileName = fileName;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return latestFileName;
    }

    private void addPrizePool(double value) {
        prizePoolBalance += value;
        if (prizePoolBalance > Config.maxPrizePool) {
            prizePoolBalance = Config.maxPrizePool;
        }
        setPrizePoolBalance(prizePoolBalance);
    }

    private void subPrizePool(double value) {
        prizePoolBalance -= value;
        if (prizePoolBalance < 0) {
            prizePoolBalance = 0;
        }
        setPrizePoolBalance(prizePoolBalance);
    }

    private void setPrizePoolBalance(double value) {
        prizePoolBalance = value;
        setValue("prizePoolBalance", prizePoolBalance);
    }

    private void setValue(String path, Object value) {
        lotteryData.set(path, value);
    }

    public void showLotteryInfo(Player player) {
        sendMessage(player, gameIntroduction);
        sendMessage(player, Messages.bettingInstructions);
        sendMessage(player, Messages.availableNumbers + formattedNumbers);
        sendMessage(player, Messages.currentPrizePool + formatDecimal(prizePoolBalance));
        showBets(player);
    }

    private void showBets(Player player) {
        ConfigurationSection cs = lotteryData.getConfigurationSection("bets." + player.getName());
        if (cs == null) {
            return;
        }
        Set<String> bets = cs.getKeys(false);
        if (bets.isEmpty()) {
            return;
        }
        StringBuilder builder = new StringBuilder(Messages.currentBets + "\n");
        int amount = 0;
        for (String bet : bets) {
            int i = cs.getInt(bet + ".amount");
            amount += i;
            builder.append("§7 >> §b").append(bet).append(" §a共§6").append(i).append("§a注 ").append(cs.getString(bet + ".state")).append('\n');
        }
        sendMessage(player, builder.substring(0, builder.length() - 1));
        sendMessage(player, Messages.getMessage(Messages.betStatistics, String.valueOf(bets.size()), String.valueOf(amount)));
    }

    private String getLotteryTime() {
        return getNum(Config.lotteryHour) + ":" + getNum(Config.lotteryMinute);
    }

    private static String formatDecimal(double d) {
        return decimalFormat.format(d);
    }

    private void sendMessage(Player player, String message) {
        player.sendMessage(Messages.messagePrefix + message);
    }

    public void placeBet(Player player, String[] args) {
        if (drawing) {
            sendMessage(player, Messages.drawCompleted);
        } else if (args.length < 2) {
            sendMessage(player, Messages.usageInstructions);
        } else if (args.length > 6) {
            sendMessage(player, Messages.tooManyNumbers);
        } else {
            int amount = parseInt(args[0]);
            if (amount < 1) {
                sendMessage(player, Messages.invalidBetAmount + args[0]);
                return;
            }
            if (args.length == 2 && args[1].equals("random")) {
                String playerName = player.getName();
                if (getRandomCount(playerName) >= Config.randomMax && !player.hasPermission("LiteLottery.bypass")) {
                    sendMessage(player, Messages.randomLimitReached);
                } else if (player.hasPermission("LiteLottery.bypass") || checkRandomInterval(playerName)) {
                    if (buyLottery(player, String.join(" ", randomNumber()), amount)) {
                        addRandomCount(playerName);
                        setRandomTime(playerName, System.currentTimeMillis());
                        save();
                    }
                } else {
                    sendMessage(player, Messages.randomCooldown);
                }
                return;
            }
            String selectedNumbers = mergeArgs(args);
            if (checkNum(player, selectedNumbers)) {
                buyLottery(player, selectedNumbers, amount);
            }
        }
    }

    private boolean checkNum(Player player, String s) {
        String[] selectedNumbers = s.split(" ");
        ArrayList<String> temp = new ArrayList<>();
        for (String num : selectedNumbers) {
            if (temp.contains(num)) {
                sendMessage(player, Messages.duplicateNumber + num);
                return false;
            } else if (!availableNumbers.contains(num)) {
                sendMessage(player, Messages.invalidNumber + num);
                return false;
            }
            temp.add(num);
        }
        return true;
    }

    private static String mergeArgs(String[] args) {
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i != 1) {
                builder.append(' ');
            }
            builder.append(args[i]);
        }
        return builder.toString();
    }

    private boolean buyLottery(Player player, String selectedNumbers, int amount) {
        if (Config.maxNumbers != 0 && getBetNumbers(player.getName()) == Config.maxNumbers && getBetAmount(player.getName(), selectedNumbers) == 0) {
            sendMessage(player, Messages.maxBetsReached);
        } else if (Config.maxBets != 0 && getBetsAmount(player.getName()) + amount > Config.maxBets) {
            sendMessage(player, Messages.betLimitExceeded);
        } else {
            double money = amount * Config.moneyPerBet;
            if (currency.has(player, money)) {
                currency.withdrawPlayer(player, money);
                addPrizePool(money);
                //有些服主用命令控制一天多次开奖 所以要防止玩家钻空子把已开奖的号码再投一注变为等待开奖
                if (!getBetState(player.getName(), selectedNumbers).equals(Messages.awaitingDraw)) {
                    setBetAmount(player.getName(), selectedNumbers, amount);
                } else {
                    addBetAmount(player.getName(), selectedNumbers, amount);
                }
                setBetState(player.getName(), selectedNumbers, Messages.awaitingDraw);
                save();
                sendMessage(player, Messages.getMessage(Messages.betPlaced, String.valueOf(amount), selectedNumbers) + formatDecimal(money));
                playSound(player);
                return true;
            } else {
                sendMessage(player, Messages.insufficientFunds);
                sendMessage(player, Messages.getMessage(Messages.totalCost, formatDecimal(money), formatDecimal(Config.moneyPerBet)));
            }
        }
        return false;
    }

    private void playSound(Player player) {
        if (plugin.mcVersion.getMinor() < 9) {
            playSound(player, Sound.valueOf("ORB_PICKUP"));
        } else {
            playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
        }
    }

    private void playSound(Player player, String sound) {
        if (plugin.mcVersion.getMinor() < 9) {
            switch (sound) {
                case "ENTITY_EXPERIENCE_ORB_PICKUP":
                    sound = "ORB_PICKUP";
                    break;
                case "ENTITY_PLAYER_LEVELUP":
                    sound = "LEVEL_UP";
                    break;
                case "ENTITY_ENDER_DRAGON_DEATH":
                    sound = "ENDERDRAGON_DEATH";
            }
        } else if (plugin.isFolia) {
            switch (sound) {
                case "ENTITY_EXPERIENCE_ORB_PICKUP":
                    playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
                    return;
                case "ENTITY_PLAYER_LEVELUP":
                    playSound(player, Sound.ENTITY_PLAYER_LEVELUP);
                    return;
                case "ENTITY_ENDER_DRAGON_DEATH":
                    playSound(player, Sound.ENTITY_ENDER_DRAGON_DEATH);
                    return;
            }
            return;
        }
        playSound(player, Sound.valueOf(sound));
    }

    private void playSound(Player player, Sound sound) {
        player.playSound(player.getLocation(), sound, 1.0F, 1.0F);
    }

    private void playSoundAll(String sound) {
        for (Player player : plugin.getOnlinePlayers()) {
            playSound(player, sound);
        }
    }

    private String getBetState(String player, String bet) {
        return lotteryData.getString("bets." + player + "." + bet + ".state", Messages.awaitingDraw);
    }

    private void setBetState(String player, String bet, String state) {
        setValue("bets." + player + "." + bet + ".state", state);
    }

    private void addBetAmount(String player, String bet, int amount) {
        setBetAmount(player, bet, getBetAmount(player, bet) + amount);
    }

    private void setBetAmount(String player, String bet, int amount) {
        setValue("bets." + player + "." + bet + ".amount", amount);
    }

    private int getBetAmount(String player, String bet) {
        return lotteryData.getInt("bets." + player + "." + bet + ".amount");
    }

    private int getBetsAmount(String player) {
        ConfigurationSection cs = lotteryData.getConfigurationSection("bets." + player);
        if (cs == null) {
            return 0;
        }
        Set<String> bets = cs.getKeys(false);
        if (bets.isEmpty()) {
            return 0;
        }
        int amount = 0;
        for (String bet : bets) {
            int i = cs.getInt(bet + ".amount");
            amount += i;
        }
        return amount;
    }

    private int getBetNumbers(String player) {
        ConfigurationSection cs = lotteryData.getConfigurationSection("bets." + player);
        return cs == null ? 0 : cs.getKeys(false).size();
    }

    private static List<String> randomNumber() {
        Random random = new Random();
        ArrayList<String> list = new ArrayList<>();
        while (list.size() < 5) {
            String num = getNum(random.nextInt(Config.maxNumber) + 1);
            if (list.contains(num)) {
                continue;
            }
            list.add(num);
        }
        return list;
    }

    private static void randomNumber(ArrayList<String> list) {
        Random random = new Random();
        while (list.size() < 5) {
            String num = getNum(random.nextInt(Config.maxNumber) + 1);
            if (!list.contains(num)) {
                list.add(num);
                return;
            }
        }
    }

    private boolean checkRandomInterval(String player) {
        long diff = System.currentTimeMillis() - getRandomTime(player);
        return diff >= Config.randomInterval * 1000L;
    }

    private void setRandomTime(String player, long time) {
        setValue("random." + player + ".time", time);
    }

    private long getRandomTime(String player) {
        return lotteryData.getLong("random." + player + ".time");
    }

    private void addRandomCount(String player) {
        setRandomCount(player, getRandomCount(player) + 1);
    }

    private void setRandomCount(String player, int count) {
        setValue("random." + player + ".count", count);
    }

    private int getRandomCount(String player) {
        return lotteryData.getInt("random." + player + ".count");
    }

    /**
     * 将字符串解析为10进制整数
     * @param num 十进制整数字符串
     * @return 如果结果小于1 返回-1
     */
    private static int parseInt(String num) {
        try {
            int i = Integer.parseInt(num);
            return i < 1 ? -1 : i;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public boolean checkTime(int hour, int minute) {
        if (hour == Config.lotteryHour && minute == Config.lotteryMinute) {
            return true;
        } else if (Config.notice && !drawNotice) {
            minute += 10;
            if (minute > 59) {
                hour++; //hour超过23也无所谓 设计就是如果开奖时间在00:10之前则不提示开奖
                minute = minute - 60; // % 60;
            }
            if (hour == Config.lotteryHour && minute == Config.lotteryMinute) {
                broadcastMessage(Messages.drawNotice);
                drawNotice = true;
            }
        }
        return false;
    }

    private void broadcastMessage(String message) {
        plugin.getServer().broadcastMessage(Messages.messagePrefix + message);
    }

    public void tryRunLottery() {
        if (!drawing) {
            drawing = true;
            setValue("drawCompleted", true);
            runLottery();
        }
    }

    private void runLottery() {
        plugin.getScheduler().runTaskAsynchronously(() -> {
            ArrayList<String> winningNumbers = new ArrayList<>();
            broadcastMessage(Messages.drawing);
            sendTitleAll(Messages.drawingInProgressTitle, "§k00 00 00 00 00", 5);
            playSoundAll("ENTITY_EXPERIENCE_ORB_PICKUP");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            StringBuilder builder = new StringBuilder();
            for (int i = 1; i < 5; i++) {
                randomNumber(winningNumbers);
                broadcastMessage(Messages.getMessage(Messages.winningNumber, String.valueOf(i)) + winningNumbers.get(i - 1));
                if (i != 1) {
                    builder.append(' ');
                }
                builder.append(winningNumbers.get(i - 1));
                sendTitleAll(Messages.drawingInProgressTitle, "§c" + builder + " §r§k" + repeat00(5 - i), 5);
                playSoundAll("ENTITY_EXPERIENCE_ORB_PICKUP");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            randomNumber(winningNumbers);
            broadcastMessage(Messages.getMessage(Messages.winningNumber, "5") + winningNumbers.get(4));
            builder.append(' ').append(winningNumbers.get(4));
            sendTitleAll(Messages.currentWinningNumbers, "§a" + builder, 7);
            broadcastMessage(Messages.winningNumbers + builder);
            setValue("winningNumbers", builder.toString());
            HashMap<String, Integer> firstPrize = new HashMap<>();
            HashMap<String, Integer> secondPrize = new HashMap<>();
            HashMap<String, Integer> thirdPrize = new HashMap<>();
            HashMap<String, Integer> fifthPrize = new HashMap<>();
            HashMap<String, Integer> fourthPrize = new HashMap<>();
            ArrayList<String> players = getPlayers();
            for (String player : players) {
                ArrayList<String> bets = getBets(player);
                for (String bet : bets) {
                    if (!getBetState(player, bet).equals(Messages.awaitingDraw)) {
                        continue; //已开过奖的号码不再开奖
                    }
                    if (bet.contentEquals(builder)) {
                        setBetState(player, bet, Messages.specialPrize);
                        int amount = getBetAmount(player, bet);
                        String msg = Messages.getMessage(Messages.specialPrizeWinner, player, String.valueOf(amount));
                        broadcastMessage(msg);
                        broadcastMessage(msg);
                        broadcastMessage(msg);
                        playSoundAll("ENTITY_ENDER_DRAGON_DEATH");
                        Player p = plugin.getServer().getPlayerExact(player);
                        if (p == null) {
                            broadcastMessage(Messages.specialPrizeOffline);
                            currency.depositPlayer(getOfflinePlayer(player), Math.min(Config.specialPrize * amount * 0.2, Config.specialPrizeMax));
                        } else {
                            double money = Math.min(Config.specialPrize * amount, Config.specialPrizeMax);
                            currency.depositPlayer(p, money);

                            sendMessage(p, Messages.specialPrizeAmount + formatDecimal(money));
                        }
                    } else {
                        String[] numbers = bet.split(" ");
                        int count = 0;
                        for (String num : numbers) {
                            if (winningNumbers.contains(num)) {
                                count++;
                            }
                        }
                        if (count != 0) {
                            int amount = getBetAmount(player, bet);
                            if (count == 5) {
                                setBetState(player, bet, Messages.firstPrize);
                                firstPrize.merge(player, amount, Integer::sum);
                            } else if (count == 4) {
                                setBetState(player, bet, Messages.secondPrize);
                                secondPrize.merge(player, amount, Integer::sum);
                            } else if (count == 3) {
                                setBetState(player, bet, Messages.thirdPrize);
                                thirdPrize.merge(player, amount, Integer::sum);
                            } else if (count == 2) {
                                setBetState(player, bet, Messages.fourthPrize);
                                fifthPrize.merge(player, amount, Integer::sum);
                            } else if (count == 1) {
                                setBetState(player, bet, Messages.fifthPrize);
                                fourthPrize.merge(player, amount, Integer::sum);
                            }
                        } else {
                            setBetState(player, bet, Messages.noPrize);
                        }
                    }
                }
            }
            String firstPrizeWinners;
            String secondPrizeWinners;
            String thirdPrizeWinners;
            String fifthPrizeWinners;
            String fourthPrizeWinners;
            if (fourthPrize.isEmpty()) {
                fourthPrizeWinners = Messages.noFifthPrize;
            } else {
                fourthPrizeWinners = Messages.fifthPrizeWinners + getNamesAndGiveMoney(fourthPrize, Config.fifthPrize, Config.fifthPrizeMax, Messages.fifthPrizeLabel);
            }
            if (fifthPrize.isEmpty()) {
                fifthPrizeWinners = Messages.noFourthPrize;
            } else {
                fifthPrizeWinners = Messages.fourthPrizeWinners + getNamesAndGiveMoney(fifthPrize, Config.fourthPrize, Config.fourthPrizeMax, Messages.fourthPrizeLabel);
            }
            if (thirdPrize.isEmpty()) {
                thirdPrizeWinners = Messages.noThirdPrize;
            } else {
                thirdPrizeWinners = Messages.thirdPrizeWinners + getNamesAndGiveMoney(thirdPrize, Config.thirdPrize, Config.thirdPrizeMax, Messages.thirdPrizeLabel);
            }
            double prizePoolTemp = prizePoolBalance;
            if (secondPrize.isEmpty()) {
                secondPrizeWinners = Messages.noSecondPrize;
            } else {
                secondPrizeWinners = Messages.secondPrizeWinners + getNamesAndBalance(secondPrize, prizePoolTemp * 0.25, Config.secondPrize, Messages.secondPrizeMessage);
                subPrizePool(prizePoolTemp * 0.25);
            }
            if (firstPrize.isEmpty()) {
                firstPrizeWinners = Messages.noFirstPrize;
            } else {
                firstPrizeWinners = Messages.firstPrizeWinners + getNamesAndBalance(firstPrize, prizePoolTemp * 0.75, Config.firstPrize, Messages.firstPrizeMessage);
                subPrizePool(prizePoolTemp * 0.75);
            }
            broadcastMessage(Messages.drawResults);
            broadcastMessage(firstPrizeWinners);
            broadcastMessage(secondPrizeWinners);
            broadcastMessage(thirdPrizeWinners);
            broadcastMessage(fifthPrizeWinners);
            broadcastMessage(fourthPrizeWinners);
            drawCompleted = true;
            save();
        });
    }

    private String getNamesAndBalance(HashMap<String, Integer> list, double pool, double prize, String msg) {
        StringBuilder names = new StringBuilder();
        int total = 0;
        Set<Map.Entry<String, Integer>> entrySet = list.entrySet();
        for (Map.Entry<String, Integer> entry : entrySet) {
            names.append("§2").append(entry.getKey()).append(" §7x§6").append(entry.getValue()).append(' ');
            total += entry.getValue();
        }
        double moneyPerBets = (pool + prize) / total;
        for (Map.Entry<String, Integer> entry : entrySet) {
            Player p = plugin.getServer().getPlayerExact(entry.getKey());
            OfflinePlayer player = p == null ? getOfflinePlayer(entry.getKey()) : p;
            double money = moneyPerBets * entry.getValue();
            currency.depositPlayer(player, money);
            if (p != null) {
                sendMessage(p, String.format(msg, entry.getValue()) + formatDecimal(money));
            }
        }
        playSoundAll("ENTITY_PLAYER_LEVELUP");
        return names.toString();
    }

    private String getNamesAndGiveMoney(HashMap<String, Integer> list, double moneyPerBets, double maxMoney, String prize) {
        StringBuilder names = new StringBuilder();
        for (Map.Entry<String, Integer> entry : list.entrySet()) {
            names.append("§2").append(entry.getKey()).append(" §7x§6").append(entry.getValue()).append(' ');
            Player p = plugin.getServer().getPlayerExact(entry.getKey());
            OfflinePlayer player = p == null ? getOfflinePlayer(entry.getKey()) : p;
            double money = Math.min(moneyPerBets * entry.getValue(), maxMoney);
            if (Config.ignorePrizePool || prizePoolBalance >= money) { //忽略奖池余额或奖池余额大于等于奖金
                subPrizePool(money); //从奖池里扣钱
                currency.depositPlayer(player, money);
                if (p != null) {
                    playSound(p, "ENTITY_PLAYER_LEVELUP");
                    sendMessage(p, Messages.getMessage(Messages.prizeWon, String.valueOf(entry.getValue()), prize) + formatDecimal(money));
                }
            } else if (prizePoolBalance != 0) { //奖池余额小于奖金且奖池余额不为0
                money = prizePoolBalance;
                subPrizePool(money);
                currency.depositPlayer(player, money);
                if (p != null) {
                    playSound(p, "ENTITY_PLAYER_LEVELUP");
                    sendMessage(p, Messages.getMessage(Messages.prizeReduced, String.valueOf(entry.getValue()), prize) + formatDecimal(money));
                }
            } else {
                if (p != null) {
                    sendMessage(p, Messages.getMessage(Messages.noPrizeFunds, String.valueOf(entry.getValue()), prize));
                }
            }
        }
        return names.toString();
    }

    private OfflinePlayer getOfflinePlayer(String player) {
        return plugin.getServer().getOfflinePlayer(player);
    }

    private ArrayList<String> getBets(String player) {
        ConfigurationSection cs = lotteryData.getConfigurationSection("bets." + player);
        ArrayList<String> bets = new ArrayList<>();
        if (cs == null) {
            return bets;
        }
        bets.addAll(cs.getKeys(false));
        return bets;
    }

    private ArrayList<String> getPlayers() {
        ConfigurationSection cs = lotteryData.getConfigurationSection("bets");
        ArrayList<String> players = new ArrayList<>();
        if (cs == null) {
            return players;
        }
        players.addAll(cs.getKeys(false));
        return players;
    }

    private static String repeat00(int n) {
        if (n == 1) {
            return "00";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < n; i++) {
            if (i != 0) {
                builder.append(' ');
            }
            builder.append("00");
        }
        return builder.toString();
    }

    private void sendTitleAll(String title, String subtitle, int stay) {
        if (plugin.mcVersion.getMinor() > 10) {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                player.sendTitle(title, subtitle, 10, stay * 20, 10);
            }
        }
    }

    public boolean tryUpdate() {
        if (drawing && !drawCompleted) {
            return false;
        }
        dataFile = new File(dataFolder, getFileName());
        init();
        return true;
    }

    public void resetDrawState() {
        if (drawing) {
            drawing = false;
            setValue("drawCompleted", false);
            save();
        }
    }

}
