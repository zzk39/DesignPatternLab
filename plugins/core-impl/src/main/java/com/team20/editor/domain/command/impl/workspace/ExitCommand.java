package com.team20.editor.domain.command.impl.workspace;

import com.team20.editor.domain.command.Command;
import com.team20.editor.domain.editor.Editor;
import com.team20.editor.domain.workspace.Workspace;
import com.team20.editor.bootstrap.ApplicationContext;
import com.team20.editor.extension.registry.DefaultCommandRegistry;
import com.team20.editor.domain.workspace.WorkspaceState;

import java.util.ArrayList;
import java.util.List;

/**
 * exit - gracefully exit the application:
 * - Prompt to save each modified editor (re-using CloseCommand prompt/save
 * logic)
 * - Close all editors
 * - Persist workspace state to a file via
 * PersistenceManager.saveWorkspaceState(...)
 * - Exit JVM
 *
 * Strict behaviour:
 * - If the user chooses to save a dirty file and the save attempt fails, abort
 * the exit immediately and report the failure.
 */
public class ExitCommand implements Command {

    private final String stateFilename;

    public ExitCommand() {
        this(".workspace.state");
    }

    public ExitCommand(String stateFilename) {
        this.stateFilename = stateFilename == null || stateFilename.isBlank() ? ".workspace.state"
                : stateFilename.trim();
    }

    @Override
    public void execute(Workspace workspace) {
        // Make a copy of editors to avoid ConcurrentModification when closing
        List<Editor> editors = new ArrayList<>(workspace.getEditors());

        // Iterate and close each editor. Use CloseCommand's public helpers to ensure
        // consistent behavior.
        for (Editor e : editors) {
            String name = e.getName();
            try {
                if (CloseCommand.isEditorDirty(e)) {
                    // ask user whether to save
                    boolean save = CloseCommand.promptYesNo(String.format("文件 '%s' 已修改，是否保存? (y/n) ", name));
                    if (save) {
                        // If saving fails, abort the entire exit immediately (strict behavior)
                        boolean saved = CloseCommand.attemptSave(e, workspace);
                        if (!saved) {
                            System.err.println("错误：保存文件 '" + name + "' 失败，已中止退出操作。请修复问题后重试。");
                            // Do NOT close this editor nor any others; abort exit
                            return;
                        }
                    }
                    // if user chose not to save, continue and close without saving
                }
            } catch (Throwable t) {
                // On unexpected error while deciding/save, abort exit to be safe
                System.err.println("错误：在处理文件 '" + name + "' 时发生异常，已中止退出: " + t.getMessage());
                return;
            }
            // Close the editor
            workspace.removeEditor(e);
            System.out.println("已关闭: " + name);
        }

        // 发布退出事件（停止所有计时）
        try {
            workspace.publishCommandEvent("exit", "");
        } catch (Throwable ignored) {
        }

        // After closing editors, persist workspace state using
        // PersistenceManager.saveWorkspaceState
        try {
            ApplicationContext ctx = DefaultCommandRegistry.getApplicationContext();
            if (ctx != null && ctx.persistenceManager() != null) {
                WorkspaceState state = workspace.getState();
                try {
                    ctx.persistenceManager().saveWorkspaceState(this.stateFilename, state);
                    System.out.println("已保存工作区状态到: " + this.stateFilename);
                } catch (Exception ex) {
                    System.err.println("错误：保存工作区状态失败: " + ex.getMessage());
                    // Abort exit if workspace state cannot be persisted (strict)
                    return;
                }
            } else {
                System.err.println("错误：无法保存工作区状态：ApplicationContext 或 PersistenceManager 未就绪");
                return;
            }
        } catch (Throwable t) {
            System.err.println("错误：保存工作区状态时出错: " + t.getMessage());
            return;
        }

        // Finally exit. Use System.exit to ensure termination when invoked outside of
        // Main's flow.
        System.out.println("退出编辑器...");
        System.exit(0);
    }

    @Override
    public String toString() {
        return "exit";
    }
}