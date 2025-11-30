package com.team20.editor.domain.command.impl.workspace;

import com.team20.editor.domain.command.Command;
import com.team20.editor.domain.editor.Editor;
import com.team20.editor.domain.workspace.Workspace;
import com.team20.editor.infrastructure.persistence.PersistenceManager;
import com.team20.editor.extension.registry.EditorFactory;
import com.team20.editor.extension.registry.DefaultCommandRegistry;
import com.team20.editor.bootstrap.ApplicationContext;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * LoadCommand: loads a file from persistence and adds editor to workspace.
 *
 * Behaviour:
 * - loads file content into an Editor and makes it active
 * - if file does not exist -> create a new editor with empty content and mark
 * it as modified (unsaved)
 * - if the first non-empty line equals "# log", enable runtime logging for this
 * file
 * via workspace.setLoggingEnabled(...) (no marker files), append a
 * session-start
 * line into .<name>.log and persist workspace state (best-effort)
 */
public class LoadCommand implements Command {

    private final EditorFactory editorFactory;
    private final PersistenceManager persistenceManager;
    private final String filepath;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss");

    public LoadCommand(EditorFactory editorFactory, PersistenceManager persistenceManager, String filepath) {
        this.editorFactory = editorFactory;
        this.persistenceManager = persistenceManager;
        this.filepath = filepath;
    }

    @Override
    public void execute(Workspace workspace) {
        boolean createdNew = false;
        String content = "";
        try {
            // try to load content; if file not found or cannot be read, we'll treat as new
            // file
            try {
                content = persistenceManager.load(filepath);
                if (content == null)
                    content = "";
            } catch (Exception loadEx) {
                // Treat any load failure as "file not present / unreadable" -> create new
                // editor
                createdNew = true;
                content = "";
            }

            Editor editor = editorFactory.createEditor(filepath);
            editor.loadContent(content);
            workspace.addEditor(editor);
            workspace.setActiveEditor(editor);

            // 发布编辑器激活事件（用于统计时长）
            try {
                workspace.publishCommandEvent("load", filepath);
            } catch (Throwable ignored) {
            }

            if (createdNew) {
                // mark editor as modified (so Close/Exit will prompt to save)
                editor.setModified(true);

                System.out.println("已创建新文件并标记为已修改: " + filepath);
                try {
                    workspace.publishWorkspaceEvent("fileCreated", filepath);
                } catch (Throwable ignored) {
                }
            } else {
                System.out.println("已加载文件: " + filepath);
            }

            // Detect first non-empty line. If equals "# log", enable runtime logging.
            boolean enableLog = false;
            if (content != null && !content.isBlank()) {
                try (BufferedReader br = new BufferedReader(new StringReader(content))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line != null && !line.isBlank()) {
                            if (line.trim().equals("# log")) {
                                enableLog = true;
                            }
                            break;
                        }
                    }
                } catch (Throwable ignored) {
                }
            }

            if (enableLog) {
                // update centralized workspace flag (no marker files)
                try {
                    workspace.setLoggingEnabled(filepath, true);
                } catch (Throwable ignored) {
                }

                // append session start line to the per-file log
                String safeName = new File(filepath).getName();
                File logFile = new File("." + safeName + ".log");
                try (PrintWriter pw = new PrintWriter(new FileWriter(logFile, true))) {
                    String session = LocalDateTime.now().format(FORMATTER);
                    pw.println("session start at " + session);
                } catch (Exception le) {
                    System.err.println("Warning: 无法写入 session start 到日志文件: " + le.getMessage());
                }

                // persist workspace state (best-effort)
                try {
                    ApplicationContext ctx = DefaultCommandRegistry.getApplicationContext();
                    if (ctx != null && ctx.persistenceManager() != null) {
                        ctx.persistenceManager().saveWorkspaceState(".workspace.state", workspace.getState());
                    }
                } catch (Throwable le) {
                    System.err.println("Warning: 无法持久化工作区状态: " + le.getMessage());
                }

                System.out.println("日志已启用: " + "." + safeName + ".log");
            }

        } catch (Exception ex) {
            System.out.println("加载失败: " + ex.getMessage());
        }
    }
}