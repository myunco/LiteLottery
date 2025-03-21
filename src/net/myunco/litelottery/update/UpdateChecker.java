package net.myunco.litelottery.update;

import net.myunco.litelottery.LiteLottery;
import net.myunco.litelottery.config.Messages;

import java.util.Timer;
import java.util.TimerTask;

public class UpdateChecker {
    private static Timer timer;
    static boolean isUpdateAvailable;
    static String newVersion;
    static String downloadLink;

    public static void start(LiteLottery plugin) {
        plugin.getScheduler().runTaskAsynchronously(() -> {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    CheckResult result = new CheckResult("https://myunco.sinacloud.net/47A92119/LiteLottery.txt", plugin.getDescription().getVersion());
                    if (result.getResultType() == CheckResult.ResultType.SUCCESS) {
                        if (result.hasNewVersion()) {
                            isUpdateAvailable = true;
                            String str = Messages.getMessage("§c发现新版本可用! §b当前版本: {0} §d最新版本: {1}", result.getCurrentVersion(), result.getLatestVersion());
                            newVersion = result.hasMajorUpdate() ? "§e(有大更新)" + str : str;
                            downloadLink = "§a下载地址: " + result.getDownloadLink();
                            plugin.logMessage(newVersion);
                            plugin.logMessage(downloadLink);
                            plugin.logMessage(result.getUpdateInfo());
                        } else {
                            isUpdateAvailable = false;
                        }
                    } else {
                        plugin.logMessage("§e检查更新失败: " + result.getErrorMessage());
                    }
                }
            }, 7000, 12 * 60 * 60 * 1000);
        });
    }

    public static void stop() {
        if (timer != null) {
            timer.cancel();
        }
    }

}
