package net.myunco.litelottery.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.myunco.litelottery.LiteLottery;
import net.myunco.litelottery.config.Config;
import net.myunco.litelottery.config.Messages;
import net.myunco.litelottery.core.Lottery;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LiteLotteryExpansion extends PlaceholderExpansion {
    private static final LiteLottery plugin = LiteLottery.plugin;

    @Override
    public String getIdentifier() {
        return "LiteLottery";
    }

    @Override
    public  String getAuthor() {
        return "myunco";
    }

    @Override
    public  String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        switch (params) {
            case "current_pool": //当前奖池余额
                return plugin.lottery.getPrizePool();
            case "current_pool_msg": //当前奖池余额消息
                return Messages.currentPrizePool + plugin.lottery.getPrizePool();
            case "player_bet_stats": //当前玩家投注统计
                return player == null ? noStats : getPlayerBetStats(player);
            case "player_bet_detail": //当前玩家投注详情
                return player == null ? "" : getPlayerBetDetail(player);
            case "draw_time": //开奖时间
                return plugin.lottery.getLotteryTime();
            case "draw_state": //是否已开奖
                return plugin.lottery.drawing ? "已开奖" : "未开奖";
            case "first_prize": //一等奖最高奖金 通过 75%奖池资金 + 基础奖金 实时计算得出
                return Lottery.formatDecimal(plugin.lottery.getPrizePoolBalance() * 0.75 + Config.firstPrize);
            case "second_prize": //二等奖最高奖金 通过 25%奖池资金 + 基础奖金 实时计算得出
                return Lottery.formatDecimal(plugin.lottery.getPrizePoolBalance() * 0.25 + Config.secondPrize);
            case "third_prize": //三等奖每注奖金
                return Lottery.formatDecimal(Config.thirdPrize);
            case "fourth_prize": //四等奖每注奖金
                return Lottery.formatDecimal(Config.fourthPrize);
            case "fifth_prize": //五等奖每注奖金
                return Lottery.formatDecimal(Config.fifthPrize);
            case "money_per_bet": //投注每注需要的金额
                return Lottery.formatDecimal(Config.moneyPerBet);
            default:
                return null;
        }
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        return onRequest(player, params);
    }

    // 添加缓存字段
    private final Map<String, CachedStats> statsCache = new ConcurrentHashMap<>();
    private final Map<String, CachedDetail> detailCache = new ConcurrentHashMap<>();
    private final String noStats = Messages.getMessage(Messages.betStatistics, "0", "0");

    // 缓存对象定义
    private static class CachedStats {
        long expireTime;
        String value;

        public CachedStats(long expireTime, String value) {
            this.expireTime = expireTime;
            this.value = value;
        }
    }

    private static class CachedDetail {
        long expireTime;
        String value;

        public CachedDetail(long expireTime, String value) {
            this.expireTime = expireTime;
            this.value = value;
        }
    }

    private String getPlayerBetStats(OfflinePlayer player) {
        CachedStats cachedStats = statsCache.get(player.getName());
        if (cachedStats != null && cachedStats.expireTime > System.currentTimeMillis()) {
            return cachedStats.value;
        }
        ConfigurationSection cs = plugin.lottery.getBetSection(player.getName());
        if (cs != null) {
            Set<String> bets = cs.getKeys(false);
            if (!bets.isEmpty()) {
                int betAmount = 0;
                for (String bet : bets) {
                    int i = cs.getInt(bet + ".amount");
                    betAmount += i;
                }
                String result = Messages.getMessage(Messages.betStatistics, String.valueOf(bets.size()), String.valueOf(betAmount));
                statsCache.put(player.getName(), new CachedStats(System.currentTimeMillis() + 3000, result));
                return result;
            }
        }
        statsCache.put(player.getName(), new CachedStats(System.currentTimeMillis() + 3000, noStats));
        return noStats;
    }

    private String getPlayerBetDetail(OfflinePlayer player) {
        CachedDetail cachedDetail = detailCache.get(player.getName());
        if (cachedDetail != null && cachedDetail.expireTime > System.currentTimeMillis()) {
            return cachedDetail.value;
        }
        ConfigurationSection cs = plugin.lottery.getBetSection(player.getName());
        if (cs != null) {
            Set<String> bets = cs.getKeys(false);
            if (!bets.isEmpty()) {
                StringBuilder builder = new StringBuilder(Messages.currentBets + "\n");
                for (String bet : bets) {
                    int i = cs.getInt(bet + ".amount");
                    builder.append("§7 >> §b").append(bet).append(" §a共§6").append(i).append("§a注 ").append(cs.getString(bet + ".state")).append('\n');
                }
                String result = builder.substring(0, builder.length() - 1);
                detailCache.put(player.getName(), new CachedDetail(System.currentTimeMillis() + 3000, result));
                return result;
            }
        }
        detailCache.put(player.getName(), new CachedDetail(System.currentTimeMillis() + 3000, ""));
        return "";
    }
}
