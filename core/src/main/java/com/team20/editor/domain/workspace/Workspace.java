package com.team20.editor.domain.workspace;

import com.team20.editor.domain.editor.Editor;
import com.team20.editor.infrastructure.event.CommandEvent;
import com.team20.editor.infrastructure.event.EventPublisher;
import com.team20.editor.infrastructure.event.WorkspaceEvent;
import com.team20.editor.extension.spi.statistics.StatisticsService;

import java.util.*;
import java.util.stream.Collectors;

public class Workspace {

    private final Map<String, Editor> editors = new HashMap<>();
    private final List<Editor> editorList = new ArrayList<>();
    private Editor activeEditor;
    private EventPublisher eventPublisher;

    // 原有：启用日志开关
    private final Map<String, Boolean> loggingEnabled = new HashMap<>();
    // 新增：每文件排除的命令集合（小写命令名）
    private final Map<String, Set<String>> loggingExclusions = new HashMap<>();

    // 会话级时长统计
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

            if (statisticsService != null) {
                statisticsService.resetDuration(filepath);
            }
        }

        loggingEnabled.putIfAbsent(filepath, Boolean.FALSE);
        loggingExclusions.putIfAbsent(filepath, new HashSet<>());

        if (activeEditor == null) {
            setActiveEditor(editor);
        }
    }

    public void removeEditor(Editor editor) {
        if (editor == null)
            return;

        String filepath = editor.getName();

        if (statisticsService != null) {
            statisticsService.stopTracking(filepath);
        }

        editors.remove(filepath);
        editorList.remove(editor);
        loggingEnabled.remove(filepath);
        loggingExclusions.remove(filepath);

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
                statisticsService.stopTracking(activeEditor.getName());
            }
            activeEditor = editor;
            if (statisticsService != null) {
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
        loggingExclusions.putIfAbsent(filepath, new HashSet<>());
    }

    // 新增：设置/获取/判定 排除命令
    public void setLogExclusions(String filepath, Set<String> exclusions) {
        if (filepath == null)
            return;
        if (exclusions == null)
            exclusions = new HashSet<>();
        // 统一为小写命令名
        Set<String> lower = new HashSet<>();
        for (String s : exclusions)
            if (s != null)
                lower.add(s.toLowerCase(Locale.ROOT));
        loggingExclusions.put(filepath, lower);
    }

    public Set<String> getLogExclusions(String filepath) {
        Set<String> s = loggingExclusions.get(filepath);
        return (s == null) ? Collections.emptySet() : Collections.unmodifiableSet(s);
    }

    public boolean isCommandExcluded(String filepath, String commandName) {
        if (filepath == null || commandName == null)
            return false;
        Set<String> s = loggingExclusions.get(filepath);
        return s != null && s.contains(commandName.toLowerCase(Locale.ROOT));
    }

    public Map<String, Boolean> getLoggingEnabledMap() {
        return Collections.unmodifiableMap(new HashMap<>(loggingEnabled));
    }

    public Map<String, Set<String>> getLoggingExclusionsMap() {
        // 深复制
        Map<String, Set<String>> copy = new HashMap<>();
        for (var e : loggingExclusions.entrySet()) {
            copy.put(e.getKey(), new HashSet<>(e.getValue()));
        }
        return Collections.unmodifiableMap(copy);
    }

    public WorkspaceState getState() {
        WorkspaceState state = new WorkspaceState();
        state.setEditorCount(editorList.size());
        state.setActiveEditorName(activeEditor != null ? activeEditor.getName() : null);
        state.setEditorNames(editorList.stream().map(Editor::getName).collect(Collectors.toList()));
        state.setLoggingEnabledMap(new HashMap<>(loggingEnabled));
        state.setLoggingExclusionsMap(getLoggingExclusionsMap());
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

        Map<String, Set<String>> ex = state.getLoggingExclusionsMap();
        if (ex != null) {
            loggingExclusions.clear();
            // 统一小写
            for (var e : ex.entrySet()) {
                setLogExclusions(e.getKey(), e.getValue());
            }
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