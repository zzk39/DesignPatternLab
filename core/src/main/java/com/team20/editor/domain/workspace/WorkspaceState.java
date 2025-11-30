package com.team20.editor.domain.workspace;

import java.util.List;
import java.util.Map;

/**
 * WorkspaceState - lightweight DTO for persisting workspace metadata.
 * Does NOT attempt to instantiate concrete Editor implementations.
 */
public class WorkspaceState {
    private int editorCount;
    private String activeEditorName;
    private List<String> editorNames;

    // Persist per-editor flags (e.g. logging enabled). key = editor name/path,
    // value = true/false
    private Map<String, Boolean> loggingEnabledMap;

    // Persist per-editor accumulated durations (seconds)
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
                ", editorDurations=" + editorDurations +
                '}';
    }
}
