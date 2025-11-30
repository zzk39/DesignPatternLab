package com.team20.editor.extension.spi.statistics;

/**
 * SPI: 会话内的编辑时长服务。
 * 插件实现负责订阅事件并累积时长；调用方只读。
 */
public interface StatisticsService {

    /**
     * 返回文件在当前会话的累计编辑时长（秒）。不存在返回 0。
     */
    long getDurationSeconds(String filename);

    /**
     * 将秒数格式化为可读字符串，如“45秒”、“2小时15分钟”、“1天3小时”。
     * 调用方也可自行格式化，此方法仅提供统一实现。
     */
    default String formatDuration(long seconds) {
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

    /**
     * 开始追踪指定文件的编辑时长
     */
    void startTracking(String filename);

    /**
     * 停止追踪指定文件的编辑时长
     */
    void stopTracking(String filename);

    /**
     * 重置指定文件的累计时长为 0
     */
    void resetDuration(String filename);
}