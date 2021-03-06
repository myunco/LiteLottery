package ml.mcos.litelottery;

import ml.mcos.litelottery.config.Config;
import ml.mcos.litelottery.config.Messages;
import net.milkbowl.vault.economy.Economy;
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
    private YamlConfiguration lotteryInfo;
    private final LiteLottery plugin;
    private final Economy economy;
    public File file;
    public boolean isOK;
    public double prizePool;
    public boolean isRunLottery;
    public boolean runLotteryFinish;
    public List<String> numList = new ArrayList<>();
    private String numbers;
    private boolean notice;
    private String introduction;

    public Lottery(LiteLottery plugin, Economy economy) {
        this.plugin = plugin;
        this.economy = economy;
        file = new File(plugin.getDataFolder(), getFileName());
        init();
        initNumber();
    }

    public void initNumber() {
        if (numList.size() != 0) {
            numList.clear();
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i <= Config.maxNumber; i++) {
            String num = getNum(i);
            builder.append(num);
            numList.add(num);
            if (i < Config.maxNumber) {
                builder.append(' ');
            }
        }
        numbers = builder.toString();
    }

    public static String getNum(int n) {
        return n < 10 ? "0" + n : String.valueOf(n);
    }

    public void init() {
        if (!file.exists()) {
            try {
                if (!file.createNewFile() && !file.createNewFile()) {
                    plugin.getServer().getConsoleSender().sendMessage(Messages.messagePrefix + Messages.cannotCreateNewFile);
                    isOK = false;
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
                isOK = false;
            }
        }
        lotteryInfo = YamlConfiguration.loadConfiguration(file);
        if (!lotteryInfo.contains("prizePool")) {
            addPrizePool(Config.initialPrizePool); //????????????????????? = ?????????????????? + ????????????
            save();
        } else {
            prizePool = lotteryInfo.getDouble("prizePool");
        }
        isRunLottery = lotteryInfo.getBoolean("isRunLottery");
        runLotteryFinish = isRunLottery;
        notice = false;
        introduction = String.format(Messages.introduction,
                formatDecimal(Config.moneyPerBets),  getLotteryTime(),
                formatDecimal(Config.fifthPrize), formatDecimal(Config.fourthPrize),
                formatDecimal(Config.thirdPrize), formatDecimal(Config.secondPrize),
                formatDecimal(Config.firstPrize), formatDecimal(Config.specialPrize));
        isOK = true;
    }

    private void save() {
        try {
            lotteryInfo.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getFileName() {
        return simpleDateFormat.format(new Date()) + ".yml";
    }

    private void addPrizePool(double value) {
        prizePool += value;
        if (prizePool > Config.maxPrizePool) {
            prizePool = Config.maxPrizePool;
        }
        setPrizePool(prizePool);
    }

    private void subPrizePool(double value) {
        prizePool -= value;
        if (prizePool < 0) {
            prizePool = 0;
        }
        setPrizePool(prizePool);
    }

    private void setPrizePool(double value) {
        prizePool = value;
        setValue("prizePool", prizePool);
    }

    private void setValue(String path, Object value) {
        lotteryInfo.set(path, value);
    }

    public void showLotteryInfo(Player player) {
        sendMessage(player, introduction);
        //sendMessage(player, "??3????????????: /lt <????????????> <????????????>\n??????: /lt 2 07 12 02 10 05 ????????????2???(07 12 02 10 05)");
        sendMessage(player, Messages.placeBetMethod);
        //sendMessage(player, "??6????????????: ??2" + numbers);
        sendMessage(player, Messages.optionalNumbers + numbers);
        //sendMessage(player, "??a??????????????????: ??e$" + formatDecimal(prizePool));
        sendMessage(player, Messages.currentPrizePool + formatDecimal(prizePool));
        showBets(player);
    }

    private void showBets(Player player) {
        ConfigurationSection cs = lotteryInfo.getConfigurationSection("bets." + player.getName());
        if (cs == null) {
            return;
        }
        Set<String> bets = cs.getKeys(false);
        if (bets.isEmpty()) {
            return;
        }
        //StringBuilder builder = new StringBuilder("??d???????????? ??3(???): \n");
        StringBuilder builder = new StringBuilder(Messages.s1 + "\n");
        int amount = 0;
        for (String bet : bets) {
            int i = cs.getInt(bet + ".amount");
            amount += i;
            builder.append("??7 >> ??b").append(bet).append(" ??a?????6").append(i).append("??a??? ").append(cs.getString(bet + ".state")).append('\n');
        }
        sendMessage(player, builder.substring(0, builder.length() - 1));
        //sendMessage(player, "??3??????????????????: ??a" + bets.size() + "??3????????? ??6" + amount + "??3???");
        sendMessage(player, Messages.getMessage(Messages.s2, String.valueOf(bets.size()), String.valueOf(amount)));
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
        if (isRunLottery) {
            //sendMessage(player, "??c??????????????????, ????????????????????????");
            sendMessage(player, Messages.s3);
        } else if (args.length < 2) {
            //sendMessage(player, "??6??????: ??7/lt <????????????> <????????????>");
            sendMessage(player, Messages.s4);
        } else if (args.length > 6) {
            //sendMessage(player, "??6????????????????????????, ???????????????5????????????");
            sendMessage(player, Messages.s5);
        } else {
            int amount = parseInt(args[0]);
            if (amount < 1) {
                //sendMessage(player, "??6?????????????????????: ??7" + args[0]);
                sendMessage(player, Messages.s6 + args[0]);
                return;
            }
            if (args.length == 2 && args[1].equals("random")) {
                String playerName = player.getName();
                if (getRandomCount(playerName) >= Config.randomMax) {
                    //sendMessage(player, "??c???????????????????????????????????????????????????");
                    sendMessage(player, Messages.s7);
                } else if (player.hasPermission("LiteLottery.bypass") || checkRandomInterval(playerName)) {
                    if (buyLottery(player, String.join(" ", randomNumber()), amount)) {
                        addRandomCount(playerName);
                        setRandomTime(playerName, System.currentTimeMillis());
                        save();
                    }
                } else {
                    //sendMessage(player, "??b?????????????????????????????????????????????????????????");
                    sendMessage(player, Messages.s8);
                }
                return;
            }
            String nums = mergeArgs(args);
            if (checkNum(player, nums)) {
                buyLottery(player, nums, amount);
            }
        }
    }

    private boolean checkNum(Player player, String s) {
        String[] nums = s.split(" ");
        ArrayList<String> temp = new ArrayList<>();
        for (String num : nums) {
            if (temp.contains(num)) {
                //sendMessage(player, "??6??????, ????????????????????????????????????: ??7" + num);
                sendMessage(player, Messages.s9 + num);
                return false;
            } else if (!numList.contains(num)) {
                //sendMessage(player, "??6??????, ????????????????????????????????????: ??7" + num);
                sendMessage(player, Messages.s10 + num);
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

    private boolean buyLottery(Player player, String nums, int amount) {
        if (Config.maxNums != 0 && getBetNums(player.getName()) == Config.maxNums && getBetAmount(player.getName(), nums) == 0) {
            //player.sendMessage("??c?????????????????????????????????????????????");
            sendMessage(player, Messages.s11);
        } else if (Config.maxBets != 0 && getBetsAmount(player.getName()) + amount > Config.maxBets) {
            //player.sendMessage("??c??????, ??????????????????????????????????????????");
            sendMessage(player, Messages.s12);
        } else {
            double money = amount * Config.moneyPerBets;
            if (economy.has(player, money)) {
                economy.withdrawPlayer(player, money);
                addPrizePool(money);
                //????????????????????????????????????????????? ?????????????????????????????????????????????????????????????????????????????????
                if (!getBetState(player.getName(), nums).equals(Messages.s13)) {
                    setBetAmount(player.getName(), nums, amount);
                } else {
                    addBetAmount(player.getName(), nums, amount);
                }
                //setBetState(player.getName(), nums, "??7(????????????)");
                setBetState(player.getName(), nums, Messages.s13);
                save();
                //sendMessage(player, "??a??l????????6" + amount + "??a??l?????3(" + nums + ") ??a??l?????????: ??b$" + formatDecimal(money));
                sendMessage(player, Messages.getMessage(Messages.s14, String.valueOf(amount), nums) + formatDecimal(money));
                playSound(player);
                return true;
            } else {
                //player.sendMessage("??c??????: ??7???????????????????????????");
                sendMessage(player, Messages.s15);
                //player.sendMessage("??7????????????: $" + formatDecimal(money) + " (????????????: $" + formatDecimal(Config.moneyPerBets) + ")");
                sendMessage(player, Messages.getMessage(Messages.s16, formatDecimal(money), formatDecimal(Config.moneyPerBets)));
            }
        }
        return false;
    }

    private void playSound(Player player) {
        if (plugin.mcVersion < 9) {
            playSound(player, Sound.valueOf("ORB_PICKUP"));
        } else {
            playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
        }
    }

    private void playSound(Player player, String sound) {
        if (plugin.mcVersion < 9) {
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
        }
        playSound(player, Sound.valueOf(sound));
    }

    private void playSound(Player player, Sound sound) {
        player.playSound(player.getLocation(), sound, 1.0F, 1.0F);
    }

    private void playSoundAll(String sound) {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            playSound(player, sound);
        }
    }

    private String getBetState(String player, String bet) {
        return lotteryInfo.getString("bets." + player + "." + bet + ".state", Messages.s13);
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
        return lotteryInfo.getInt("bets." + player + "." + bet + ".amount");
    }

    private int getBetsAmount(String player) {
        ConfigurationSection cs = lotteryInfo.getConfigurationSection("bets." + player);
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

    private int getBetNums(String player) {
        ConfigurationSection cs = lotteryInfo.getConfigurationSection("bets." + player);
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
        return lotteryInfo.getLong("random." + player + ".time");
    }

    private void addRandomCount(String player) {
        setRandomCount(player, getRandomCount(player) + 1);
    }

    private void setRandomCount(String player, int count) {
        setValue("random." + player + ".count", count);
    }

    private int getRandomCount(String player) {
        return lotteryInfo.getInt("random." + player + ".count");
    }

    /**
     * ?????????????????????10????????????
     * @param num ????????????????????????
     * @return ??????????????????1 ??????-1
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
        } else if (Config.notice && !notice) {
            minute += 10;
            if (minute > 59) {
                hour++; //hour??????23???????????? ?????????????????????????????????00:10????????????????????????
                minute = minute % 60;
            }
            if (hour == Config.lotteryHour && minute == Config.lotteryMinute) {
                //broadcastMessage("??b??????????????????????????????, ????????????, ???????????????????????????????????????!");
                broadcastMessage(Messages.s17);
                notice = true;
            }
        }
        return false;
    }

    private void broadcastMessage(String message) {
        plugin.getServer().broadcastMessage(Messages.messagePrefix + message);
    }

    public void tryRunLottery() {
        if (!isRunLottery) {
            isRunLottery = true;
            setValue("isRunLottery", true);
            runLottery();
        }
    }

    private void runLottery() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            ArrayList<String> prizeNum = new ArrayList<>();
            //broadcastMessage("??6??l??????????????6??????");
            broadcastMessage(Messages.s18);
            //sendTitleAll("??6??????????????????", "??k00 00 00 00 00", 5);
            sendTitleAll(Messages.s19, "??k00 00 00 00 00", 5);
            playSoundAll("ENTITY_EXPERIENCE_ORB_PICKUP");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            StringBuilder builder = new StringBuilder();
            for (int i = 1; i < 5; i++) {
                randomNumber(prizeNum);
                //broadcastMessage("??b???" + i + "??????????????????: ??c" + prizeNum.get(i - 1));
                broadcastMessage(Messages.getMessage(Messages.s20, String.valueOf(i)) + prizeNum.get(i - 1));
                if (i != 1) {
                    builder.append(' ');
                }
                builder.append(prizeNum.get(i - 1));
                //sendTitleAll("??6??????????????????", "??c" + builder + " ??r??k" + repeat00(5 - i), 5);
                sendTitleAll(Messages.s19, "??c" + builder + " ??r??k" + repeat00(5 - i), 5);
                playSoundAll("ENTITY_EXPERIENCE_ORB_PICKUP");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            randomNumber(prizeNum);
            //broadcastMessage("??b???5??????????????????: ??c" + prizeNum.get(4));
            broadcastMessage(Messages.getMessage(Messages.s20, "5") + prizeNum.get(4));
            builder.append(' ').append(prizeNum.get(4));
            //sendTitleAll("??6??????????????????", "??a" + builder, 7);
            sendTitleAll(Messages.s21, "??a" + builder, 7);
            //broadcastMessage("??6??????????????????: ??a" + builder);
            broadcastMessage(Messages.s22 + builder);
            setValue("prizeNumber", builder.toString());
            HashMap<String, Integer> firstPrize = new HashMap<>();
            HashMap<String, Integer> secondPrize = new HashMap<>();
            HashMap<String, Integer> thirdPrize = new HashMap<>();
            HashMap<String, Integer> fifthPrize = new HashMap<>();
            HashMap<String, Integer> fourthPrize = new HashMap<>();
            ArrayList<String> players = getPlayers();
            for (String player : players) {
                ArrayList<String> bets = getBets(player);
                for (String bet : bets) {
                    if (!getBetState(player, bet).equals(Messages.s13)) {
                        continue; //?????????????????????????????????
                    }
                    if (bet.equals(builder.toString())) {
                        //setBetState(player, bet, "??d??l(?????????)");
                        setBetState(player, bet, Messages.s23);
                        int amount = getBetAmount(player, bet);
                        //String msg = "??4??l??????????????? ??a" + player + "??c??l????????d??l???????????6??l" + amount + "??c??l??????";
                        String msg = Messages.getMessage(Messages.s24, player, String.valueOf(amount));
                        broadcastMessage(msg);
                        broadcastMessage(msg);
                        broadcastMessage(msg);
                        playSoundAll("ENTITY_ENDER_DRAGON_DEATH");
                        Player p = plugin.getServer().getPlayerExact(player);
                        if (p == null) {
                            //broadcastMessage("??6??????????????? ???????????????????????????????????????, ????????????20%????????????");
                            broadcastMessage(Messages.s25);
                            economy.depositPlayer(getOfflinePlayer(player), Math.min(Config.specialPrize * amount * 0.2, Config.specialPrizeMax));
                        } else {
                            double money = Math.min(Config.specialPrize * amount, Config.specialPrizeMax);
                            economy.depositPlayer(p, money);
                            //sendMessage(p, "??6???????????d??l???????????c, ?????????: ??b$" + formatDecimal(money));
                            sendMessage(p, Messages.s26 + formatDecimal(money));
                        }
                    } else {
                        String[] nums = bet.split(" ");
                        int count = 0;
                        for (String num : nums) {
                            if (prizeNum.contains(num)) {
                                count++;
                            }
                        }
                        if (count != 0) {
                            int amount = getBetAmount(player, bet);
                            if (count == 5) {
                                //setBetState(player, bet, "??c(?????????)");
                                setBetState(player, bet, Messages.s27);
                                firstPrize.merge(player, amount, Integer::sum);
                            } else if (count == 4) {
                                //setBetState(player, bet, "??b(?????????)");
                                setBetState(player, bet, Messages.s28);
                                secondPrize.merge(player, amount, Integer::sum);
                            } else if (count == 3) {
                                //setBetState(player, bet, "??a(?????????)");
                                setBetState(player, bet, Messages.s29);
                                thirdPrize.merge(player, amount, Integer::sum);
                            } else if (count == 2) {
                                //setBetState(player, bet, "??3(?????????)");
                                setBetState(player, bet, Messages.s30);
                                fifthPrize.merge(player, amount, Integer::sum);
                            } else if (count == 1) {
                                //setBetState(player, bet, "??9(?????????)");
                                setBetState(player, bet, Messages.s31);
                                fourthPrize.merge(player, amount, Integer::sum);
                            }
                        } else {
                            //setBetState(player, bet, "??7(?????????)");
                            setBetState(player, bet, Messages.s32);
                        }
                    }
                }
            }
            String firstPrizeResult;
            String secondPrizeResult;
            String thirdPrizeResult;
            String fifthPrizeResult;
            String fourthPrizeResult;
            if (fourthPrize.isEmpty()) {
                //fourthPrizeResult = "??9?????????: ??7???";
                fourthPrizeResult = Messages.s33;
            } else {
                //fourthPrizeResult = "??9?????????: " + getNamesAndGiveMoney(fourthPrize, Config.fifthPrize, Config.fifthPrizeMax, "??9?????????");
                fourthPrizeResult = Messages.s34 + getNamesAndGiveMoney(fourthPrize, Config.fifthPrize, Config.fifthPrizeMax, Messages.s43);
            }
            if (fifthPrize.isEmpty()) {
                //fifthPrizeResult = "??3?????????: ??7???";
                fifthPrizeResult = Messages.s35;
            } else {
                //fifthPrizeResult = "??3?????????: " + getNamesAndGiveMoney(fifthPrize, Config.fourthPrize, Config.fourthPrizeMax, "??3?????????");
                fifthPrizeResult = Messages.s36 + getNamesAndGiveMoney(fifthPrize, Config.fourthPrize, Config.fourthPrizeMax, Messages.s44);
            }
            if (thirdPrize.isEmpty()) {
                //thirdPrizeResult = "??a?????????: ??7???";
                thirdPrizeResult = Messages.s37;
            } else {
                //thirdPrizeResult = "??a?????????: " + getNamesAndGiveMoney(thirdPrize, Config.thirdPrize, Config.thirdPrizeMax, "??d?????????");
                thirdPrizeResult = Messages.s38 + getNamesAndGiveMoney(thirdPrize, Config.thirdPrize, Config.thirdPrizeMax, Messages.s45);
            }
            double prizePoolTemp = prizePool;
            if (secondPrize.isEmpty()) {
                //secondPrizeResult = "??b?????????: ??7???";
                secondPrizeResult = Messages.s39;
            } else {
                //secondPrizeResult = "??b?????????: " + getNamesAndBalance(secondPrize, prizePoolTemp * 0.25, Config.secondPrize, "??a???????????6%d??a?????b???????????a, ?????????: ??b$");
                secondPrizeResult = Messages.s40 + getNamesAndBalance(secondPrize, prizePoolTemp * 0.25, Config.secondPrize, Messages.s46);
                subPrizePool(prizePoolTemp * 0.25);
            }
            if (firstPrize.isEmpty()) {
                //firstPrizeResult = "??c?????????: ??7???";
                firstPrizeResult = Messages.s41;
            } else {
                //firstPrizeResult = "??c?????????: " + getNamesAndBalance(firstPrize, prizePoolTemp * 0.75, Config.firstPrize, "??c???????????6%d??c?????c??l???????????c, ?????????: ??b$");
                firstPrizeResult = Messages.s42 + getNamesAndBalance(firstPrize, prizePoolTemp * 0.75, Config.firstPrize, Messages.s47);
                subPrizePool(prizePoolTemp * 0.75);
            }
            //broadcastMessage("??6??l????????????:");
            broadcastMessage(Messages.s48);
            broadcastMessage(firstPrizeResult);
            broadcastMessage(secondPrizeResult);
            broadcastMessage(thirdPrizeResult);
            broadcastMessage(fifthPrizeResult);
            broadcastMessage(fourthPrizeResult);
            runLotteryFinish = true;
            save();
        });
    }

    private String getNamesAndBalance(HashMap<String, Integer> list, double pool, double prize, String msg) {
        StringBuilder names = new StringBuilder();
        int total = 0;
        Set<Map.Entry<String, Integer>> entrySet = list.entrySet();
        for (Map.Entry<String, Integer> entry : entrySet) {
            names.append("??2").append(entry.getKey()).append(" ??7x??6").append(entry.getValue()).append(' ');
            total += entry.getValue();
        }
        double moneyPerBets = (pool + prize) / total;
        for (Map.Entry<String, Integer> entry : entrySet) {
            Player p = plugin.getServer().getPlayerExact(entry.getKey());
            OfflinePlayer player = p == null ? getOfflinePlayer(entry.getKey()) : p;
            double money = moneyPerBets * entry.getValue();
            economy.depositPlayer(player, money);
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
            names.append("??2").append(entry.getKey()).append(" ??7x??6").append(entry.getValue()).append(' ');
            Player p = plugin.getServer().getPlayerExact(entry.getKey());
            OfflinePlayer player = p == null ? getOfflinePlayer(entry.getKey()) : p;
            double money = Math.min(moneyPerBets * entry.getValue(), maxMoney);
            if (Config.ignorePrizePool || prizePool >= money) { //???????????????????????????????????????????????????
                subPrizePool(money); //??????????????????
                economy.depositPlayer(player, money);
                if (p != null) {
                    playSound(p, "ENTITY_PLAYER_LEVELUP");
                    //sendMessage(p, "??a???????????6" + entry.getValue() + "??a???" + prize + "??a, ?????????: ??b$" + formatDecimal(money));
                    sendMessage(p, Messages.getMessage(Messages.s49, String.valueOf(entry.getValue()), prize) + formatDecimal(money));
                }
            } else if (prizePool != 0) { //?????????????????????????????????????????????0
                money = prizePool;
                subPrizePool(money);
                economy.depositPlayer(player, money);
                if (p != null) {
                    playSound(p, "ENTITY_PLAYER_LEVELUP");
                    //sendMessage(p, "??a???????????6" + entry.getValue() + "??a???" + prize + "??a, ???????????????????????????, ????????????: ??b$" + formatDecimal(money));
                    sendMessage(p, Messages.getMessage(Messages.s50, String.valueOf(entry.getValue()), prize) + formatDecimal(money));
                }
            } else {
                if (p != null) {
                    //sendMessage(p, "??c?????????! ?????????????????6" + entry.getValue() + "??c???" + prize + "??c, ??????????????????????????????0, ?????????????????????");
                    sendMessage(p, Messages.getMessage(Messages.s51, String.valueOf(entry.getValue()), prize));
                }
            }
        }
        return names.toString();
    }

    @SuppressWarnings("deprecation")
    private OfflinePlayer getOfflinePlayer(String player) {
        return plugin.getServer().getOfflinePlayer(player);
    }

    private ArrayList<String> getBets(String player) {
        ConfigurationSection cs = lotteryInfo.getConfigurationSection("bets." + player);
        ArrayList<String> bets = new ArrayList<>();
        if (cs == null) {
            return bets;
        }
        bets.addAll(cs.getKeys(false));
        return bets;
    }

    private ArrayList<String> getPlayers() {
        ConfigurationSection cs = lotteryInfo.getConfigurationSection("bets");
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
        if (plugin.mcVersion > 10) {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                player.sendTitle(title, subtitle, 10, stay * 20, 10);
            }
        }
    }

    public boolean tryUpdate() {
        if (isRunLottery && !runLotteryFinish) {
            return false;
        }
        file = new File(file.getParent(), getFileName());
        init();
        return true;
    }

    public void forceFalse() {
        if (isRunLottery) {
            isRunLottery = false;
            setValue("isRunLottery", false);
            save();
        }
    }

}
