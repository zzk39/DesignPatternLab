package com.team20.editor.bootstrap;

import com.team20.editor.domain.command.registry.AutoLoadingCommandRegistry;
import com.team20.editor.domain.command.CommandInvoker;
import com.team20.editor.domain.workspace.Workspace;
import com.team20.editor.domain.workspace.WorkspaceState;
import com.team20.editor.extension.registry.EditorFactory;
import com.team20.editor.extension.spi.editor.EditorProvider;
import com.team20.editor.extension.spi.serialization.SerializerProvider;
import com.team20.editor.infrastructure.event.EventBus;
import com.team20.editor.infrastructure.event.EventPublisher;
import com.team20.editor.infrastructure.event.EventListener;
import com.team20.editor.infrastructure.event.SimpleEventBus;
import com.team20.editor.infrastructure.persistence.Serializer;
import com.team20.editor.infrastructure.persistence.PersistenceManager;
import com.team20.editor.monitoring.logging.LogSink;
import com.team20.editor.monitoring.logging.LogListener;
import com.team20.editor.extension.spi.statistics.StatisticsService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

/**
 * 应用程序上下文（严格插件化）
 *
 * 说明：
 * - 仅依赖 SPI 接口，通过 ServiceLoader 加载实现（编辑器、序列化、日志、命令、监听器等）。
 */
public final class ApplicationContext {

    private final EventBus eventBus;
    private final AutoLoadingCommandRegistry commandRegistry;
    private final LogSink logSink;
    private final CommandInvoker commandInvoker;
    private final PersistenceManager persistenceManager;
    private final EditorFactory editorFactory;
    private final List<EditorProvider> editorProviders = new ArrayList<>();

    // keep a reference to the log listener so we can inject Workspace later
    private final LogListener logListener;

    // 新增：保存被加载并订阅的 StatisticsService（若存在）
    private StatisticsService statisticsService = null;

    public ApplicationContext() {
        this.eventBus = new SimpleEventBus();

        // Subscribe LogListener so it receives CommandEvent and writes per-file logs
        LogListener listener = new LogListener();
        this.logListener = listener;
        try {
            this.eventBus.subscribe(listener);
        } catch (Throwable t) {
            System.err.println("Warning: LogListener failed to subscribe: " + t.getMessage());
        }

        // 自动订阅由插件提供的 EventListener
        try {
            ServiceLoader<EventListener> evLoader = ServiceLoader.load(EventListener.class);
            for (EventListener l : evLoader) {
                this.eventBus.subscribe(l);
            }
        } catch (Throwable t) {
            System.err.println("Warning: Failed to load/subscribe EventListeners: " + t.getMessage());
        }

        this.commandRegistry = new AutoLoadingCommandRegistry();
        this.commandInvoker = new CommandInvoker();

        // 严格加载：SerializerProvider -> Serializer -> PersistenceManager
        Serializer serializer = loadSerializer();
        this.persistenceManager = new PersistenceManager(serializer);

        // 严格加载：LogSink（实现必须由插件提供）
        this.logSink = loadLogSink();

        // 加载 EditorProvider（SPI）并强制至少有一个
        loadEditorProviders();
        if (editorProviders.isEmpty()) {
            throw new IllegalStateException(
                    "没有找到任何 EditorProvider 实现。请在对应模块的 META-INF/services/com.team20.editor.extension.spi.editor.EditorProvider 中注册。");
        }
        this.editorFactory = new EditorFactory(this.editorProviders);

        // 注册 help 命令
        try {
            this.commandRegistry.registerFactory("help", rawArgs -> new com.team20.editor.domain.command.Command() {
                @Override
                public void execute(Workspace workspace) {
                    try {
                        System.out.println(ApplicationContext.this.showHelp());
                    } catch (Throwable t) {
                        System.err.println("Failed to display help: " + t.getMessage());
                    }
                }

                @Override
                public String toString() {
                    return "help";
                }
            });
        } catch (Throwable t) {
            System.err.println("Warning: failed to register help command factory: " + t.getMessage());
        }

        // 加载 StatisticsService 插件
        ServiceLoader<StatisticsService> loader = ServiceLoader.load(StatisticsService.class);
        StatisticsService foundService = null;
        for (StatisticsService service : loader) {
            foundService = service;
            break;
        }
        if (foundService == null) {
            throw new IllegalStateException(
                    "必须提供 StatisticsService 插件实现，用于统计编辑时间。");
        }
        this.statisticsService = foundService;
    }

    public StatisticsService getStatisticsService() {
        return this.statisticsService;
    }

    private Serializer loadSerializer() {
        ServiceLoader<SerializerProvider> loader = ServiceLoader.load(SerializerProvider.class);
        for (SerializerProvider sp : loader) {
            Serializer s = sp.getSerializer();
            if (s != null)
                return s;
        }
        throw new IllegalStateException("没有找到任何 SerializerProvider 实现（用于 Persistence）。请提供一个插件实现。");
    }

    private LogSink loadLogSink() {
        ServiceLoader<LogSink> loader = ServiceLoader.load(LogSink.class);
        for (LogSink ls : loader) {
            if (ls != null)
                return ls;
        }
        throw new IllegalStateException(
                "没有找到任何 LogSink 实现。请提供一个实现并在 META-INF/services/com.team20.editor.monitoring.logging.LogSink 中注册。");
    }

    private void loadEditorProviders() {
        ServiceLoader<EditorProvider> loader = ServiceLoader.load(EditorProvider.class);
        for (EditorProvider p : loader) {
            editorProviders.add(p);
        }
    }

    public Workspace createWorkspace() {
        // 使用带 StatisticsService 的 Workspace 构造
        Workspace ws = new Workspace(this.statisticsService);
        try {
            if (eventBus instanceof EventPublisher) {
                ws.setEventPublisher((EventPublisher) eventBus);
            }
        } catch (Throwable ignored) {
        }

        // 注入 Workspace 到 LogListener
        try {
            if (this.logListener != null) {
                this.logListener.setWorkspace(ws);
            }
        } catch (Throwable ignored) {
        }

        try {
            loadWorkspaceState(ws);
        } catch (Throwable t) {
            System.err.println("Warning: Failed to restore workspace state: " + t.getMessage());
        }
        try {
            migrateLegacyLogMarkers(ws);
        } catch (Throwable t) {
            System.err.println("Warning: Failed to migrate legacy log markers: " + t.getMessage());
        }

        return ws;
    }

    private void loadWorkspaceState(Workspace ws) {
        Path stateFile = Path.of(".workspace.state");
        if (!Files.exists(stateFile))
            return;
        try {
            WorkspaceState state = persistenceManager.loadWorkspaceState(".workspace.state");
            if (state != null)
                ws.restoreState(state);
        } catch (IOException e) {
            System.err.println("Warning: Could not load workspace state: " + e.getMessage());
        }
    }

    private void migrateLegacyLogMarkers(Workspace ws) {
        try {
            Path currentDir = Path.of(".");
            List<Path> markerFiles = Files.list(currentDir)
                    .filter(p -> p.getFileName().toString().startsWith(".")
                            && p.getFileName().toString().endsWith(".log.enabled"))
                    .collect(Collectors.toList());
            for (Path marker : markerFiles) {
                String fileName = marker.getFileName().toString();
                String originalFile = fileName.substring(1, fileName.length() - ".log.enabled".length());
                ws.setLoggingEnabled(originalFile, true);
                try {
                    Files.deleteIfExists(marker);
                } catch (IOException e) {
                    System.err.println("Warning: Could not delete legacy marker " + fileName + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Warning: Error during legacy marker migration: " + e.getMessage());
        }
    }

    public void saveWorkspaceState(Workspace ws) throws IOException {
        if (ws == null)
            return;
        WorkspaceState state = ws.getState();
        persistenceManager.saveWorkspaceState(".workspace.state", state);
    }

    public String showHelp() {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("Team20 Text Editor - Available Commands\n");
        sb.append("========================================\n\n");
        sb.append("Workspace Commands:\n");
        sb.append("  load <file>                   - Load file from disk\n");
        sb.append("  save [file|all]               - Save current/specified/all files\n");
        sb.append("  init <text|xml> [with-log]    - Create new buffer (text or xml)\n");
        sb.append("  close [file]                  - Close current or specified file\n");
        sb.append("  edit <file>                   - Switch active file\n");
        sb.append("  editor-list                   - List all open editors (with duration)\n");
        sb.append("  dir-tree [path]               - Show directory tree\n");
        sb.append("  undo                          - Undo last operation\n");
        sb.append("  redo                          - Redo last undone operation\n");
        sb.append("  exit                          - Exit program\n\n");
        sb.append("Text Edit Commands:\n");
        sb.append("  append \"text\"                 - Append text as new line (Undoable)\n");
        sb.append("  insert line:col \"text\"        - Insert text at position (Undoable)\n");
        sb.append("  delete line:col length        - Delete characters (Undoable)\n");
        sb.append("  replace line:col len \"text\"   - Replace text (Undoable)\n");
        sb.append("  show [start:end]              - Display content\n\n");
        sb.append("XML Commands (in xml editor):\n");
        sb.append("  insert-before <tag> <newId> <targetId> [\"text\"]\n");
        sb.append("  append-child  <tag> <newId> <parentId> [\"text\"]\n");
        sb.append("  edit-id <oldId> <newId>\n");
        sb.append("  edit-text <elementId> [\"text\"]\n");
        sb.append("  delete <elementId>\n");
        sb.append("  xml-tree [file]\n\n");
        sb.append("Spell Checking:\n");
        sb.append("  spell-check [file]            - Check spelling for text or xml\n\n");
        sb.append("Logging:\n");
        sb.append("  log-on [file]                 - Enable logging\n");
        sb.append("  log-off [file]                - Disable logging\n");
        sb.append("  log-show [file]               - Show log file\n");
        sb.append("========================================\n");
        return sb.toString();
    }

    public EventBus eventBus() {
        return eventBus;
    }

    public AutoLoadingCommandRegistry commandRegistry() {
        return commandRegistry;
    }

    public LogSink logSink() {
        return logSink;
    }

    public CommandInvoker commandInvoker() {
        return commandInvoker;
    }

    public PersistenceManager persistenceManager() {
        return persistenceManager;
    }

    public EditorFactory editorFactory() {
        return editorFactory;
    }

    public List<EditorProvider> editorProviders() {
        return List.copyOf(editorProviders);
    }

    public String dumpSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Application Context Summary:\n");
        sb.append("- Editor providers: ").append(editorProviders.size()).append("\n");
        for (EditorProvider provider : editorProviders) {
            sb.append("  * ").append(provider.getProviderName()).append("\n");
        }
        if (commandRegistry instanceof AutoLoadingCommandRegistry) {
            AutoLoadingCommandRegistry autoRegistry = (AutoLoadingCommandRegistry) commandRegistry;
            sb.append("- Command providers: ").append(autoRegistry.getProviders().size()).append("\n");
        }
        sb.append("- Available commands: ").append(commandRegistry.getCommandNames().size()).append("\n");
        return sb.toString();
    }
}
