package com.team20.editor.plugin.stats;

import com.team20.editor.infrastructure.event.Event;
import com.team20.editor.infrastructure.event.EventListener;
import com.team20.editor.infrastructure.event.CommandEvent;
import com.team20.editor.extension.spi.statistics.StatisticsService;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 会话内统计编辑时长的 SPI 插件实现
 */
public class SessionStatisticsListener implements EventListener, StatisticsService {

    private volatile String activeFile = null;
    private volatile long activeStartEpoch = 0L;
    private final Map<String, Long> seconds = new ConcurrentHashMap<>();

    public SessionStatisticsListener() {
        // 构造函数轻量，不输出调试信息
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof CommandEvent ce) {
            String cmd = ce.getCommandName();
            String targetFile = ce.getFilepath();

            switch (cmd) {
                case "load":
                case "edit":
                case "init":
                    startTracking(targetFile);
                    break;
                case "close":
                    stopTracking(targetFile);
                    break;
                case "exit":
                    stopAccum();
                    activeFile = null;
                    break;
                default:
                    // 其它命令无需处理
            }
        }
    }

    // ==== StatisticsService 抽象方法实现 ====

    @Override
    public void startTracking(String filename) {
        if (filename == null || filename.isBlank())
            return;
        if (filename.equals(activeFile))
            return;

        stopAccum(); // 停止上一个活跃文件的计时
        activeFile = filename;
        activeStartEpoch = Instant.now().getEpochSecond();
    }

    @Override
    public void stopTracking(String filename) {
        if (filename == null || filename.isBlank())
            return;

        if (filename.equals(activeFile)) {
            stopAccum();
            activeFile = null;
        }
    }

    @Override
    public void resetDuration(String filename) {
        if (filename != null) {
            seconds.remove(filename);
        }
    }

    @Override
    public long getDurationSeconds(String filename) {
        if (filename == null)
            return 0;

        return seconds.getOrDefault(filename, 0L) + liveSecondsIfActive(filename);
    }

    private long liveSecondsIfActive(String filename) {
        if (!filename.equals(activeFile) || activeStartEpoch == 0)
            return 0;
        long now = Instant.now().getEpochSecond();
        return Math.max(0, now - activeStartEpoch);
    }

    private void stopAccum() {
        if (activeFile == null || activeStartEpoch == 0)
            return;
        long now = Instant.now().getEpochSecond();
        long delta = Math.max(0, now - activeStartEpoch);
        seconds.merge(activeFile, delta, Long::sum);
        activeStartEpoch = 0;
    }

    /**
     * 提供 formatDuration 的统一实现
     */
    @Override
    public String formatDuration(long seconds) {
        if (seconds < 60)
            return seconds + "秒";
        long minutes = seconds / 60;
        if (minutes < 60)
            return minutes + "分钟";
        long hours = minutes / 60;
        minutes = minutes % 60;
        if (hours < 24)
            return hours + "小时" + (minutes > 0 ? minutes + "分钟" : "");
        long days = hours / 24;
        hours = hours % 24;
        return days + "天" + (hours > 0 ? hours + "小时" : "");
    }
}
