package com.team20.editor.domain.workspace;

import com.team20.editor.domain.editor.Editor;
import com.team20.editor.infrastructure.event.CommandEvent;
import com.team20.editor.infrastructure.event.EventPublisher;
import com.team20.editor.infrastructure.event.WorkspaceEvent;
import com.team20.editor.extension.spi.statistics.StatisticsService;
import com.team20.editor.bootstrap.ApplicationContext;

import java.util.*;
import java.util.stream.Collectors;

public class Workspace {

    private final Map<String, Editor> editors = new HashMap<>();
    private final List<Editor> editorList = new ArrayList<>();
    private Editor activeEditor;
    private EventPublisher eventPublisher;

    private final Map<String, Boolean> loggingEnabled = new HashMap<>();

    // 新增：StatisticsService 引用，用于会话级别编辑时长
    private final StatisticsService statisticsService;

    public Workspace() {
        this.statisticsService = null;
    }

    public Workspace(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    public void setEventPublisher(EventPublisher publisher) {
        this.eventPublisher = publisher;
    }

    public void addEditor(Editor editor) {
        if (editor == null)
            return;

        String filepath = editor.getName();
        if (!editors.containsKey(filepath)) {
            editors.put(filepath, editor);
            editorList.add(editor);

            // 新增：打开文件即重置会话时长
            if (statisticsService != null) {
                statisticsService.resetDuration(filepath);
            }
        }

        loggingEnabled.putIfAbsent(filepath, Boolean.FALSE);

        if (activeEditor == null) {
            setActiveEditor(editor); // 使用 setActiveEditor 来触发统计
        }
    }

    public void removeEditor(Editor editor) {
        if (editor == null)
            return;

        String filepath = editor.getName();

        // 新增：关闭文件停止统计
        if (statisticsService != null) {
            statisticsService.stopTracking(filepath);
        }

        editors.remove(filepath);
        editorList.remove(editor);
        loggingEnabled.remove(filepath);

        if (activeEditor == editor) {
            Editor newActive = editorList.isEmpty() ? null : editorList.get(editorList.size() - 1);
            setActiveEditor(newActive);
        }
    }

    public List<Editor> getEditors() {
        return Collections.unmodifiableList(editorList);
    }

    public Editor getEditor(String filepath) {
        return editors.get(filepath);
    }

    public Editor getActiveEditor() {
        return activeEditor;
    }

    public void setActiveEditor(Editor editor) {
        if (editor != null && editorList.contains(editor)) {
            if (activeEditor != null && statisticsService != null) {
                // 停止前一个文件的计时
                statisticsService.stopTracking(activeEditor.getName());
            }
            activeEditor = editor;
            if (statisticsService != null) {
                // 开始当前文件计时
                statisticsService.startTracking(activeEditor.getName());
            }
        }
    }

    public boolean hasEditors() {
        return !editorList.isEmpty();
    }

    public int getEditorCount() {
        return editorList.size();
    }

    public void publishCommandEvent(String commandName, String arguments) {
        if (eventPublisher != null) {
            String filepath = activeEditor != null ? activeEditor.getName() : "";
            CommandEvent event = new CommandEvent(commandName, arguments, filepath);
            eventPublisher.publish(event);
        }
    }

    public void publishWorkspaceEvent(String eventType, Object data) {
        if (eventPublisher != null) {
            WorkspaceEvent event = new WorkspaceEvent(eventType, data);
            eventPublisher.publish(event);
        }
    }

    public boolean isLoggingEnabled(String filepath) {
        if (filepath == null)
            return false;
        Boolean v = loggingEnabled.get(filepath);
        return v != null && v;
    }

    public void setLoggingEnabled(String filepath, boolean enabled) {
        if (filepath == null)
            return;
        loggingEnabled.put(filepath, enabled);
    }

    public Map<String, Boolean> getLoggingEnabledMap() {
        return Collections.unmodifiableMap(new HashMap<>(loggingEnabled));
    }

    public WorkspaceState getState() {
        WorkspaceState state = new WorkspaceState();
        state.setEditorCount(editorList.size());
        state.setActiveEditorName(activeEditor != null ? activeEditor.getName() : null);
        state.setEditorNames(editorList.stream().map(Editor::getName).collect(Collectors.toList()));
        state.setLoggingEnabledMap(new HashMap<>(loggingEnabled));
        // editorDurations 不再恢复
        state.setEditorDurations(Collections.emptyMap());
        return state;
    }

    public void restoreState(WorkspaceState state) {
        if (state == null)
            return;

        Map<String, Boolean> map = state.getLoggingEnabledMap();
        if (map != null) {
            loggingEnabled.clear();
            loggingEnabled.putAll(map);
        }

        String activeName = state.getActiveEditorName();
        if (activeName != null) {
            Editor editor = editors.get(activeName);
            if (editor != null) {
                setActiveEditor(editor);
            } else if (!editorList.isEmpty()) {
                setActiveEditor(editorList.get(0));
            }
        }
    }

    @Override
    public String toString() {
        return String.format("Workspace[editors=%d, active=%s]",
                editorList.size(),
                activeEditor != null ? activeEditor.getName() : "none");
    }

    public StatisticsService getStatisticsService() {
        return this.statisticsService;
    }

}
