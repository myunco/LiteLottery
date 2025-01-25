package ml.mcos.litelottery.config;

import ml.mcos.litelottery.LiteLottery;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class Messages {
    public static String messagePrefix;
    public static String playerOnly;
    public static String versionInfo;
    public static String configReloaded;
    public static String alreadyDrawn;
    public static String drawingInProgress;
    public static String settingDrawState;
    public static String drawStateReset;
    public static String unknownCommandArgs;
    public static String fileCreationFailed;
    public static String gameIntroduction;
    public static String bettingInstructions;
    public static String availableNumbers;
    public static String currentPrizePool;
    public static String currentBets;
    public static String betStatistics;
    public static String drawCompleted;
    public static String usageInstructions;
    public static String tooManyNumbers;
    public static String invalidBetAmount;
    public static String randomLimitReached;
    public static String randomCooldown;
    public static String duplicateNumber;
    public static String invalidNumber;
    public static String maxBetsReached;
    public static String betLimitExceeded;
    public static String awaitingDraw;
    public static String betPlaced;
    public static String insufficientFunds;
    public static String totalCost;
    public static String drawNotice;
    public static String drawing;
    public static String drawingInProgressTitle;
    public static String winningNumber;
    public static String currentWinningNumbers;
    public static String winningNumbers;
    public static String specialPrize;
    public static String specialPrizeWinner;
    public static String specialPrizeOffline;
    public static String specialPrizeAmount;
    public static String firstPrize;
    public static String secondPrize;
    public static String thirdPrize;
    public static String fourthPrize;
    public static String fifthPrize;
    public static String noPrize;
    public static String noFifthPrize;
    public static String fifthPrizeWinners;
    public static String noFourthPrize;
    public static String fourthPrizeWinners;
    public static String noThirdPrize;
    public static String thirdPrizeWinners;
    public static String noSecondPrize;
    public static String secondPrizeWinners;
    public static String noFirstPrize;
    public static String firstPrizeWinners;
    public static String fifthPrizeLabel;
    public static String fourthPrizeLabel;
    public static String thirdPrizeLabel;
    public static String secondPrizeMessage;
    public static String firstPrizeMessage;
    public static String drawResults;
    public static String prizeWon;
    public static String prizeReduced;
    public static String noPrizeFunds;

    public static void init(LiteLottery plugin) {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", true);
        }
        YamlConfiguration messages = Config.loadConfiguration(file, plugin);
        messagePrefix = messages.getString("messagePrefix", "§7§l[§c§lLottery§7§l] ");
        playerOnly = messages.getString("playerOnly", "§c本命令只能玩家使用。");
        versionInfo = messages.getString("versionInfo", "§a当前版本: §b");
        configReloaded = messages.getString("configReloaded", "§a配置文件重载完成。");
        alreadyDrawn = messages.getString("alreadyDrawn", "§c今天已经开过奖了!");
        drawingInProgress = messages.getString("drawingInProgress", "§c当前正在开奖, 你必须等待开奖结束才能再次开奖!");
        settingDrawState = messages.getString("settingDrawState", "§c当前正在开奖, 你必须等待开奖结束才能设置开奖状态!");
        drawStateReset = messages.getString("drawStateReset", "§a已将开奖状态设为§b未开奖§a。");
        unknownCommandArgs = messages.getString("unknownCommandArgs", "§6错误: 未知的命令参数");
        fileCreationFailed = messages.getString("fileCreationFailed", "§c无法创建新文件, 初始化失败!");
        StringBuilder builder = new StringBuilder();
        for (String s : messages.getStringList("gameIntroduction")) {
            if (builder.length() != 0) {
                builder.append('\n');
            }
            builder.append(s);
        }
        if (builder.length() == 0) {
            builder.append("§7玩法介绍: 每天开奖前从可选号码中任选1-5个号码为一组进行投注, 每注价格: $%s")
                    .append("\n可以投注多组号码, 但每一组号码中的号码不能重复。")
                    .append("\n开奖规则: 每天%s系统随机生成5个中奖号码。所选号码与中奖号码进行比较, 包含全部中奖号码即为一等奖, 包含4个中奖号码即为二等奖, 包含3个中奖号码即为三等奖, 包含2个中奖号码即为四等奖, 包含1个中奖号码即为五等奖。(不要求顺序一致)")
                    .append("\n五等奖每注奖金: $%s, 四等奖每注奖金: $%s, 三等奖每注奖金: $%s")
                    .append("\n二等奖每注奖金: (奖池资金 * 0.25 + $%s) / 中奖注数")
                    .append("\n一等奖每注奖金: (奖池资金 * 0.75 + $%s) / 中奖注数")
                    .append("\n除此之外, 还设有特等奖, 投注号码与中奖号码完全一致即为特等奖。")
                    .append("\n特等奖每注奖金: $%s");
        }
        gameIntroduction = builder.toString();
        builder = new StringBuilder();
        for (String s : messages.getStringList("bettingInstructions")) {
            if (builder.length() != 0) {
                builder.append('\n');
            }
            builder.append(s);
        }
        if (builder.length() == 0) {
            builder.append("§3投注方法: /lt <投注数量> <所选号码>")
                    .append("\n例如: /lt 2 07 12 02 10 05 表示购买2注(07 12 02 10 05)");
        }
        bettingInstructions = builder.toString();
        availableNumbers = messages.getString("availableNumbers", "§6可选号码: §2");
        currentPrizePool = messages.getString("currentPrizePool", "§a当前奖池资金: §e$");
        currentBets = messages.getString("currentBets", "§d本期投注 §3(您):");
        betStatistics = messages.getString("betStatistics", "§3本期投注统计: §a{0}§3组号码 §6{1}§3注");
        drawCompleted = messages.getString("drawCompleted", "§c今天已经开奖, 明天再来投注吧。");
        usageInstructions = messages.getString("usageInstructions", "§6用法: §7/lt <投注数量> <所选号码>");
        tooManyNumbers = messages.getString("tooManyNumbers", "§6你选的号码太多了, 最多只能选5个号码。");
        invalidBetAmount = messages.getString("invalidBetAmount", "§6无效的投注数量: §7");
        randomLimitReached = messages.getString("randomLimitReached", "§c你今天使用随机选号的次数已达上限。");
        randomCooldown = messages.getString("randomCooldown", "§b你使用的太快了，喝口茶休息一会再来吧。");
        duplicateNumber = messages.getString("duplicateNumber", "§6错误, 选择的号码中含有重复号码: §7");
        invalidNumber = messages.getString("invalidNumber", "§6错误, 选择的号码不在可选号码内: §7");
        maxBetsReached = messages.getString("maxBetsReached", "§c你本期投注的号码数量已达上限。");
        betLimitExceeded = messages.getString("betLimitExceeded", "§c错误, 投注数量超过了本期投注上限。");
        awaitingDraw = messages.getString("awaitingDraw", "§7(等待开奖)");
        betPlaced = messages.getString("betPlaced", "§a§l购买§6{0}§a§l注§3({1}) §a§l共花费: §b$");
        insufficientFunds = messages.getString("insufficientFunds", "§c错误: §7你没有足够的金钱。");
        totalCost = messages.getString("totalCost", "§7共计需要: ${0} (每注价格: ${1})");
        drawNotice = messages.getString("drawNotice", "§b开奖将在十分钟后进行, 不要走开, 也许你就是本期的特等奖得主!");
        drawing = messages.getString("drawing", "§6§l正在开奖§6···");
        drawingInProgressTitle = messages.getString("drawingInProgressTitle", "§6正在开奖···");
        winningNumber = messages.getString("winningNumber", "§b第{0}个中奖号码是: §c");
        currentWinningNumbers = messages.getString("currentWinningNumbers", "§6本期中奖号码");
        winningNumbers = messages.getString("winningNumbers", "§6本期中奖号码: §a");
        specialPrize = messages.getString("specialPrize", "§d§l(特等奖)");
        specialPrizeWinner = messages.getString("specialPrizeWinner", "§4§l难以置信！ §a{0}§c§l抽中§d§l特等奖§6§l{1}§c§l注！");
        specialPrizeOffline = messages.getString("specialPrizeOffline", "§6很遗憾··· 由于特等奖得主当前并未在线, 只能获得20%的奖金。");
        specialPrizeAmount = messages.getString("specialPrizeAmount", "§6你抽中§d§l特等奖§c, 获得了: §b$");
        firstPrize = messages.getString("firstPrize", "§c(一等奖)");
        secondPrize = messages.getString("secondPrize", "§b(二等奖)");
        thirdPrize = messages.getString("thirdPrize", "§a(三等奖)");
        fourthPrize = messages.getString("fourthPrize", "§3(四等奖)");
        fifthPrize = messages.getString("fifthPrize", "§9(五等奖)");
        noPrize = messages.getString("noPrize", "§7(未中奖)");
        noFifthPrize = messages.getString("noFifthPrize", "§9五等奖: §7无");
        fifthPrizeWinners = messages.getString("fifthPrizeWinners", "§9五等奖: ");
        noFourthPrize = messages.getString("noFourthPrize", "§3四等奖: §7无");
        fourthPrizeWinners = messages.getString("fourthPrizeWinners", "§3四等奖: ");
        noThirdPrize = messages.getString("noThirdPrize", "§a三等奖: §7无");
        thirdPrizeWinners = messages.getString("thirdPrizeWinners", "§a三等奖: ");
        noSecondPrize = messages.getString("noSecondPrize", "§b二等奖: §7无");
        secondPrizeWinners = messages.getString("secondPrizeWinners", "§b二等奖: ");
        noFirstPrize = messages.getString("noFirstPrize", "§c一等奖: §7无");
        firstPrizeWinners = messages.getString("firstPrizeWinners", "§c一等奖: ");
        fifthPrizeLabel = messages.getString("fifthPrizeLabel", "§9五等奖");
        fourthPrizeLabel = messages.getString("fourthPrizeLabel", "§3四等奖");
        thirdPrizeLabel = messages.getString("thirdPrizeLabel", "§d三等奖");
        secondPrizeMessage = messages.getString("secondPrizeMessage", "§a你抽中§6%d§a注§b二等奖§a, 获得了: §b$");
        firstPrizeMessage = messages.getString("firstPrizeMessage", "§c你抽中§6%d§c注§c§l一等奖§c, 获得了: §b$");
        drawResults = messages.getString("drawResults", "§6§l开奖结果:");
        prizeWon = messages.getString("prizeWon", "§a你抽中§6{0}§a注{1}§a, 获得了: §b$");
        prizeReduced = messages.getString("prizeReduced", "§a你抽中§6{0}§a注{1}§a, 但由于奖池资金不足, 只获得了: §b$");
        noPrizeFunds = messages.getString("noPrizeFunds", "§c很遗憾! 虽然你抽中§6{0}§c注{1}§c, 但由于奖池资金已经为0, 未能获得奖金。");
    }

    public static String getMessage(String msg, String... args) {
        for (int i = 0; i < args.length; i++) {
            //msg = msg.replace("{" + i + "}", args[i]);
            msg = msg.replace("{0}".replace('0', (char) (i + 0x30)), args[i]);
        }
        return msg;
    }

}
