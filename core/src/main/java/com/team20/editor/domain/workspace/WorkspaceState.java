package com.team20.editor.domain.workspace;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * WorkspaceState - lightweight DTO for persisting workspace metadata.
 * Does NOT attempt to instantiate concrete Editor implementations.
 */
public class WorkspaceState {
    private int editorCount;
    private String activeEditorName;
    private List<String> editorNames;

    // 每文件：是否启用日志
    private Map<String, Boolean> loggingEnabledMap;

    // 新增：每文件：被排除的命令集合（小写命令名）
    private Map<String, Set<String>> loggingExclusionsMap;

    // 持久化会话累计时长（当前实现不恢复）
    private Map<String, Long> editorDurations;

    public WorkspaceState() {
    }

    public int getEditorCount() {
        return editorCount;
    }

    public void setEditorCount(int editorCount) {
        this.editorCount = editorCount;
    }

    public String getActiveEditorName() {
        return activeEditorName;
    }

    public void setActiveEditorName(String activeEditorName) {
        this.activeEditorName = activeEditorName;
    }

    public List<String> getEditorNames() {
        return editorNames;
    }

    public void setEditorNames(List<String> editorNames) {
        this.editorNames = editorNames;
    }

    public Map<String, Boolean> getLoggingEnabledMap() {
        return loggingEnabledMap;
    }

    public void setLoggingEnabledMap(Map<String, Boolean> loggingEnabledMap) {
        this.loggingEnabledMap = loggingEnabledMap;
    }

    public Map<String, Set<String>> getLoggingExclusionsMap() {
        return loggingExclusionsMap;
    }

    public void setLoggingExclusionsMap(Map<String, Set<String>> loggingExclusionsMap) {
        this.loggingExclusionsMap = loggingExclusionsMap;
    }

    public Map<String, Long> getEditorDurations() {
        return editorDurations;
    }

    public void setEditorDurations(Map<String, Long> editorDurations) {
        this.editorDurations = editorDurations;
    }

    @Override
    public String toString() {
        return "WorkspaceState{" +
                "editorCount=" + editorCount +
                ", activeEditorName='" + activeEditorName + '\'' +
                ", editorNames=" + editorNames +
                ", loggingEnabledMap=" + loggingEnabledMap +
                ", loggingExclusionsMap=" + loggingExclusionsMap +
                ", editorDurations=" + editorDurations +
                '}';
    }
}