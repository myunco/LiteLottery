package ml.mcos.litelottery.config;

import ml.mcos.litelottery.LiteLottery;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class Messages {
    public static String messagePrefix;
    public static String playerOnly;
    public static String version;
    public static String reload;
    public static String alreadyLottery;
    public static String running1;
    public static String running2;
    public static String forceFalse;
    public static String unknownArgs;
    public static String cannotCreateNewFile;
    public static String introduction;
    public static String placeBetMethod;
    public static String optionalNumbers;
    public static String currentPrizePool;
    public static String s1;
    public static String s2;
    public static String s3;
    public static String s4;
    public static String s5;
    public static String s6;
    public static String s7;
    public static String s8;
    public static String s9;
    public static String s10;
    public static String s11;
    public static String s12;
    public static String s13;
    public static String s14;
    public static String s15;
    public static String s16;
    public static String s17;
    public static String s18;
    public static String s19;
    public static String s20;
    public static String s21;
    public static String s22;
    public static String s23;
    public static String s24;
    public static String s25;
    public static String s26;
    public static String s27;
    public static String s28;
    public static String s29;
    public static String s30;
    public static String s31;
    public static String s32;
    public static String s33;
    public static String s34;
    public static String s35;
    public static String s36;
    public static String s37;
    public static String s38;
    public static String s39;
    public static String s40;
    public static String s41;
    public static String s42;
    public static String s43;
    public static String s44;
    public static String s45;
    public static String s46;
    public static String s47;
    public static String s48;
    public static String s49;
    public static String s50;
    public static String s51;

    public static void init(LiteLottery plugin) {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", true);
        }
        YamlConfiguration messages = Config.loadConfiguration(file, plugin);
        messagePrefix = messages.getString("messagePrefix", "§7§l[§c§lLottery§7§l] ");
        playerOnly = messages.getString("playerOnly", "§c本命令只能玩家使用。");
        version = messages.getString("version", "§a当前版本: §b");
        reload = messages.getString("reload", "§a配置文件重载完成。");
        alreadyLottery = messages.getString("alreadyLottery", "§c今天已经开过奖了!");
        running1 = messages.getString("running1", "§c当前正在开奖, 你必须等待开奖结束才能再次开奖!");
        running2 = messages.getString("running2", "§c当前正在开奖, 你必须等待开奖结束才能设置开奖状态!");
        forceFalse = messages.getString("forceFalse", "§a已将开奖状态设为§b未开奖§a。");
        unknownArgs = messages.getString("unknownArgs", "§6错误: 未知的命令参数");
        cannotCreateNewFile = messages.getString("cannotCreateNewFile", "§c无法创建新文件, 初始化失败!");
        StringBuilder builder = new StringBuilder();
        for (String s : messages.getStringList("introduction")) {
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
        introduction = builder.toString();
        builder = new StringBuilder();
        for (String s : messages.getStringList("placeBetMethod")) {
            if (builder.length() != 0) {
                builder.append('\n');
            }
            builder.append(s);
        }
        if (builder.length() == 0) {
            builder.append("§3投注方法: /lt <投注数量> <所选号码>")
                    .append("\n例如: /lt 2 07 12 02 10 05 表示购买2注(07 12 02 10 05)");
        }
        placeBetMethod = builder.toString();
        optionalNumbers = messages.getString("optionalNumbers", "§6可选号码: §2");
        currentPrizePool = messages.getString("currentPrizePool", "§a当前奖池资金: §e$");
        s1 = messages.getString("s1", "§d本期投注 §3(您):");
        s2 = messages.getString("s2", "§3本期投注统计: §a{0}§3组号码 §6{1}§3注");
        s3 = messages.getString("s3", "§c今天已经开奖, 明天再来投注吧。");
        s4 = messages.getString("s4", "§6用法: §7/lt <投注数量> <所选号码>");
        s5 = messages.getString("s5", "§6你选的号码太多了, 最多只能选5个号码。");
        s6 = messages.getString("s6", "§6无效的投注数量: §7");
        s7 = messages.getString("s7", "§c你今天使用随机选号的次数已达上限。");
        s8 = messages.getString("s8", "§b你使用的太快了，喝口茶休息一会再来吧。");
        s9 = messages.getString("s9", "§6错误, 选择的号码中含有重复号码: §7");
        s10 = messages.getString("s10", "§6错误, 选择的号码不在可选号码内: §7");
        s11 = messages.getString("s11", "§c你本期投注的号码数量已达上限。");
        s12 = messages.getString("s12", "§c错误, 投注数量超过了本期投注上限。");
        s13 = messages.getString("s13", "§7(等待开奖)");
        s14 = messages.getString("s14", "§a§l购买§6{0}§a§l注§3({1}) §a§l共花费: §b$");
        s15 = messages.getString("s15", "§c错误: §7你没有足够的金钱。");
        s16 = messages.getString("s16", "§7共计需要: ${0} (每注价格: ${1})");
        s17 = messages.getString("s17", "§b开奖将在十分钟后进行, 不要走开, 也许你就是本期的特等奖得主!");
        s18 = messages.getString("s18", "§6§l正在开奖§6···");
        s19 = messages.getString("s19", "§6正在开奖···");
        s20 = messages.getString("s20", "§b第{0}个中奖号码是: §c");
        s21 = messages.getString("s21", "§6本期中奖号码");
        s22 = messages.getString("s22", "§6本期中奖号码: §a");
        s23 = messages.getString("s23", "§d§l(特等奖)");
        s24 = messages.getString("s24", "§4§l难以置信！ §a{0}§c§l抽中§d§l特等奖§6§l{1}§c§l注！");
        s25 = messages.getString("s25", "§6很遗憾··· 由于特等奖得主当前并未在线, 只能获得20%的奖金。");
        s26 = messages.getString("s26", "§6你抽中§d§l特等奖§c, 获得了: §b$");
        s27 = messages.getString("s27", "§c(一等奖)");
        s28 = messages.getString("s28", "§b(二等奖)");
        s29 = messages.getString("s29", "§a(三等奖)");
        s30 = messages.getString("s30", "§3(四等奖)");
        s31 = messages.getString("s31", "§9(五等奖)");
        s32 = messages.getString("s32", "§7(未中奖)");
        s33 = messages.getString("s33", "§9五等奖: §7无");
        s34 = messages.getString("s34", "§9五等奖: ");
        s35 = messages.getString("s35", "§3四等奖: §7无");
        s36 = messages.getString("s36", "§3四等奖: ");
        s37 = messages.getString("s37", "§a三等奖: §7无");
        s38 = messages.getString("s38", "§a三等奖: ");
        s39 = messages.getString("s39", "§b二等奖: §7无");
        s40 = messages.getString("s40", "§b二等奖: ");
        s41 = messages.getString("s41", "§c一等奖: §7无");
        s42 = messages.getString("s42", "§c一等奖: ");
        s43 = messages.getString("s43", "§9五等奖");
        s44 = messages.getString("s44", "§3四等奖");
        s45 = messages.getString("s45", "§d三等奖");
        s46 = messages.getString("s46", "§a你抽中§6%d§a注§b二等奖§a, 获得了: §b$");
        s47 = messages.getString("s47", "§c你抽中§6%d§c注§c§l一等奖§c, 获得了: §b$");
        s48 = messages.getString("s48", "§6§l开奖结果:");
        s49 = messages.getString("s49", "§a你抽中§6{0}§a注{1}§a, 获得了: §b$");
        s50 = messages.getString("s50", "§a你抽中§6{0}§a注{1}§a, 但由于奖池资金不足, 只获得了: §b$");
        s51 = messages.getString("s51", "§c很遗憾! 虽然你抽中§6{0}§c注{1}§c, 但由于奖池资金已经为0, 未能获得奖金。");
    }

    public static String getMessage(String msg, String... args) {
        for (int i = 0; i < args.length; i++) {
            //msg = msg.replace("{" + i + "}", args[i]);
            msg = msg.replace("{0}".replace('0', (char) (i + 0x30)), args[i]);
        }
        return msg;
    }

}
