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
import java.util.*;

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
            try {
                content = persistenceManager.load(filepath);
                if (content == null)
                    content = "";
            } catch (Exception loadEx) {
                createdNew = true;
                content = "";
            }

            Editor editor = editorFactory.createEditor(filepath);
            editor.loadContent(content);
            workspace.addEditor(editor);
            workspace.setActiveEditor(editor);

            try {
                workspace.publishCommandEvent("load", filepath);
            } catch (Throwable ignored) {
            }

            if (createdNew) {
                editor.setModified(true);
                System.out.println("已创建新文件并标记为已修改: " + filepath);
                try {
                    workspace.publishWorkspaceEvent("fileCreated", filepath);
                } catch (Throwable ignored) {
                }
            } else {
                System.out.println("已加载文件: " + filepath);
            }

            // 解析首行 "# log ..." 增强：支持 -e <cmd> 过滤
            boolean enableLog = false;
            Set<String> excludes = new HashSet<>();
            String firstNonEmpty = firstNonEmptyLine(content);

            if (firstNonEmpty != null && firstNonEmpty.startsWith("#")) {
                String trimmed = firstNonEmpty.trim();
                if (trimmed.startsWith("# log")) {
                    enableLog = true;
                    // 解析参数：# log -e cmd1 -e cmd2 ...
                    excludes = parseLogExclusions(trimmed);
                    // 校验命令存在性（不存在则忽略并告警）
                    excludes = validateExclusions(excludes);
                }
            }

            if (enableLog) {
                try {
                    workspace.setLoggingEnabled(filepath, true);
                } catch (Throwable ignored) {
                }
                try {
                    workspace.setLogExclusions(filepath, excludes);
                } catch (Throwable ignored) {
                }

                String safeName = new File(filepath).getName();
                File logFile = new File("." + safeName + ".log");
                try (PrintWriter pw = new PrintWriter(new FileWriter(logFile, true))) {
                    String session = LocalDateTime.now().format(FORMATTER);
                    pw.println("session start at " + session);
                } catch (Exception le) {
                    System.err.println("Warning: 无法写入 session start 到日志文件: " + le.getMessage());
                }

                try {
                    ApplicationContext ctx = DefaultCommandRegistry.getApplicationContext();
                    if (ctx != null && ctx.persistenceManager() != null) {
                        ctx.persistenceManager().saveWorkspaceState(".workspace.state", workspace.getState());
                    }
                } catch (Throwable le) {
                    System.err.println("Warning: 无法持久化工作区状态: " + le.getMessage());
                }

                System.out.println("日志已启用: " + "." + safeName + ".log"
                        + (excludes.isEmpty() ? "" : "，已排除命令: " + excludes));
            }

        } catch (Exception ex) {
            System.out.println("加载失败: " + ex.getMessage());
        }
    }

    private static String firstNonEmptyLine(String content) {
        if (content == null || content.isBlank())
            return null;
        try (BufferedReader br = new BufferedReader(new StringReader(content))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line != null && !line.isBlank())
                    return line;
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static Set<String> parseLogExclusions(String header) {
        // header 形如： "# log -e append -e delete"
        // 简单以空白分隔：忽略未知参数（仅提取 -e 后的一个 token）
        String[] tokens = header.split("\\s+");
        Set<String> out = new HashSet<>();
        for (int i = 0; i < tokens.length; i++) {
            if ("-e".equals(tokens[i]) && i + 1 < tokens.length) {
                String cmd = tokens[i + 1];
                if (cmd != null && !cmd.isBlank()) {
                    out.add(cmd.trim().toLowerCase(Locale.ROOT));
                }
                i++; // 跳过参数
            }
        }
        return out;
    }

    private static Set<String> validateExclusions(Set<String> excludes) {
        if (excludes == null || excludes.isEmpty())
            return Collections.emptySet();
        Set<String> ok = new HashSet<>();
        for (String cmd : excludes) {
            try {
                var reg = DefaultCommandRegistry.getInstance();
                boolean exists = reg != null && reg.hasCommand(cmd);
                if (exists)
                    ok.add(cmd);
                else
                    System.err.println("Warning: 日志过滤中包含未知命令: " + cmd);
            } catch (Throwable t) {
                // 容错：注册表不可用时，不中断，仅保留原值
                ok.add(cmd);
            }
        }
        return ok;
    }
}