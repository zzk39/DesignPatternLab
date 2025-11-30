package com.team20.editor.domain.command.impl.workspace;

import com.team20.editor.domain.command.Command;
import com.team20.editor.domain.editor.Editor;
import com.team20.editor.domain.workspace.Workspace;
import com.team20.editor.infrastructure.persistence.PersistenceManager;
import com.team20.editor.bootstrap.ApplicationContext;
import com.team20.editor.extension.registry.DefaultCommandRegistry;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * init <file> [with-log]
 * 兼容扩展：
 * - init <text|xml> [with-log] -> 仅创建缓冲区（不落盘），并初始化内容
 *
 * 行为（落盘模式）：
 * - 文件已存在则拒绝
 * - 成功后总是创建/截断 .<filename>.log
 * - with-log: 目标文件首行写 "# log"；并在 .<filename>.log 记录一次 session start；启用
 * workspace logging
 *
 * 行为（缓冲区模式）：
 * - 仅在内存创建一个未保存的新缓冲（untitled.txt/xml）
 * - xml: 若 with-log 则首行 "# log"，其后为合法 XML 空结构；否则直接使用 XML 空结构
 * - text: 若 with-log 则首行 "# log"，否则空
 * - 标记为 modified，并设为活动文件
 */
public class InitCommand implements Command {

    private final String filepathOrType;
    private final boolean withLog;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss");

    public InitCommand(String filepathOrType, boolean withLog) {
        this.filepathOrType = (filepathOrType == null) ? null : filepathOrType.trim();
        this.withLog = withLog;
    }

    @Override
    public void execute(Workspace workspace) {
        if (filepathOrType == null || filepathOrType.isBlank()) {
            System.out.println("用法: init <file|text|xml> [with-log]");
            return;
        }

        String lowered = filepathOrType.toLowerCase();
        if ("text".equals(lowered) || "xml".equals(lowered)) {
            createBuffer(workspace, lowered, withLog);
            return;
        }

        // 兼容原有：init <file> [with-log] 落盘模式
        createFileOnDisk(workspace, filepathOrType, withLog);
    }

    private void createBuffer(Workspace workspace, String type, boolean withLog) {
        try {
            ApplicationContext ctx = DefaultCommandRegistry.getApplicationContext();
            if (ctx == null) {
                System.out.println("初始化失败：ApplicationContext 未就绪");
                return;
            }

            // 生成一个缓冲名（未保存）。优先避免与已打开同名冲突。
            String ext = "xml".equals(type) ? "xml" : "txt";
            String name = suggestUntitledName(workspace, ext);

            // 使用编辑器工厂基于扩展名创建相应编辑器
            Editor editor = ctx.editorFactory().createEditor(name);

            // 组装初始内容
            String content;
            if ("xml".equals(type)) {
                String xmlSkeleton = """
                        <?xml version="1.0" encoding="UTF-8"?>
                        <root id="root">
                        </root>
                        """;
                if (withLog) {
                    content = "# log\n" + xmlSkeleton;
                } else {
                    // 直接给出骨架可避免装载时空内容再自动生成
                    content = xmlSkeleton;
                }
            } else {
                // text
                content = withLog ? "# log" : "";
            }

            editor.loadContent(content);
            // 缓冲区模式：标记为已修改（需 save 指定路径）
            try {
                editor.setModified(true);
            } catch (Throwable ignored) {
            }

            workspace.addEditor(editor);
            workspace.setActiveEditor(editor);

            // 发布编辑器激活事件（用于统计时长）
            try {
                workspace.publishCommandEvent("init", name);
            } catch (Throwable ignored) {
            }

            System.out.println("已创建缓冲区: " + name + (withLog ? " (logging header enabled)" : ""));
            if ("xml".equals(type)) {
                System.out.println(
                        "提示：使用 XML 命令（append-child / insert-before / edit-id / edit-text / delete / xml-tree）进行编辑");
            } else {
                System.out.println("提示：使用 'append \"text\"' 等文本命令进行编辑");
            }
        } catch (Throwable t) {
            System.out.println("init 发生异常（缓冲区模式）: " + t.getMessage());
        }
    }

    private void createFileOnDisk(Workspace workspace, String filepath, boolean withLog) {
        try {
            File f = new File(filepath);
            if (f.exists()) {
                System.out.println("文件已存在: " + filepath);
                return;
            }

            ApplicationContext ctx = DefaultCommandRegistry.getApplicationContext();
            if (ctx == null) {
                System.out.println("初始化失败：ApplicationContext 未就绪");
                return;
            }
            PersistenceManager pm = ctx.persistenceManager();

            // with-log -> 仅将 "# log" 写入文件首行（不额外空行）
            String content = withLog ? "# log" : "";

            try {
                pm.save(filepath, content);
            } catch (Exception ex) {
                System.out.println("创建文件失败: " + ex.getMessage());
                return;
            }

            // 打开编辑器并加载内容
            try {
                var factory = ctx.editorFactory();
                Editor editor = factory.createEditor(filepath);
                editor.loadContent(content);
                workspace.addEditor(editor);
                workspace.setActiveEditor(editor);

                // 发布编辑器激活事件（用于统计时长）
                try {
                    workspace.publishCommandEvent("init", filepath);
                } catch (Throwable ignored) {
                }

                System.out.println("已创建文件: " + filepath);
                System.out.println("提示：使用 'append \"text\"' 添加内容 或 XML 命令操作 .xml 文件");
            } catch (Exception ex) {
                System.out.println("创建编辑器失败，但文件已创建: " + filepath + " (" + ex.getMessage() + ")");
            }

            // 每个文件对应一个 .<filename>.log（覆盖创建）
            String safeName = new File(filepath).getName();
            File logFile = new File("." + safeName + ".log");
            try {
                try (FileWriter fw = new FileWriter(logFile, false)) {
                    // 截断/创建
                }
            } catch (Exception le) {
                System.err.println("Warning: 无法创建/清空日志文件: " + le.getMessage());
            }

            // with-log: 追加 session 行、启用 workspace logging 并持久化
            if (withLog) {
                try (PrintWriter pw = new PrintWriter(new FileWriter(logFile, true))) {
                    String session = LocalDateTime.now().format(FORMATTER);
                    pw.println("session start at " + session);
                } catch (Exception le) {
                    System.err.println("Warning: 无法写入 session start 到日志文件: " + le.getMessage());
                }

                try {
                    workspace.setLoggingEnabled(filepath, true);
                } catch (Throwable ignored) {
                }

                try {
                    if (ctx.persistenceManager() != null) {
                        ctx.persistenceManager().saveWorkspaceState(".workspace.state", workspace.getState());
                    }
                } catch (Throwable le) {
                    System.err.println("Warning: 无法持久化工作区状态: " + le.getMessage());
                }

                System.out.println("日志已启用: " + logFile.getName());
            }
        } catch (Throwable t) {
            System.out.println("init 发生异常: " + t.getMessage());
        }
    }

    private static String suggestUntitledName(Workspace ws, String ext) {
        // 尽量避免与已打开名字冲突（若 Workspace 无此 API，将简单返回默认名）
        String base = "untitled";
        String name = base + "." + ext;
        try {
            int i = 2;
            // 假设 Workspace 有 getEditor(name)，否则捕获异常并返回默认名
            while (ws.getEditor(name) != null) {
                name = base + "(" + i + ")." + ext;
                i++;
            }
        } catch (Throwable ignored) {
        }
        return name;
    }

    @Override
    public String toString() {
        return "init " + (filepathOrType == null ? "" : filepathOrType) + (withLog ? " with-log" : "");
    }
}