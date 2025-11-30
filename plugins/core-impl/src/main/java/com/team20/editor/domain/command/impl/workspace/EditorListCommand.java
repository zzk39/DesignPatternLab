package com.team20.editor.domain.command.impl.workspace;

import com.team20.editor.domain.command.Command;
import com.team20.editor.domain.workspace.Workspace;
import com.team20.editor.domain.editor.Editor;
import com.team20.editor.extension.spi.statistics.StatisticsService;

public class EditorListCommand implements Command {

    @Override
    public void execute(Workspace workspace) {
        System.out.println("打开的文件列表:");
        if (!workspace.hasEditors()) {
            System.out.println("  (无)");
            return;
        }

        StatisticsService statsService = workspace.getStatisticsService();

        for (Editor editor : workspace.getEditors()) {
            long seconds = statsService != null ? statsService.getDurationSeconds(editor.getName()) : 0;

            String marker = editor == workspace.getActiveEditor() ? "> " : "  ";
            String modifiedMarker = editor.isModified() ? "*" : "";

            String durationStr = formatDuration(seconds);

            System.out.println(marker + editor.getName() + modifiedMarker + " (" + durationStr + ")");
        }
    }

    private String formatDuration(long seconds) {
        if (seconds < 60)
            return seconds + "秒";
        long minutes = seconds / 60;
        if (minutes < 60)
            return minutes + "分钟";
        long hours = minutes / 60;
        minutes %= 60;
        if (hours < 24)
            return hours + "小时" + (minutes > 0 ? minutes + "分钟" : "");
        long days = hours / 24;
        hours %= 24;
        return days + "天" + (hours > 0 ? hours + "小时" : "");
    }
}
