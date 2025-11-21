package com.team20.editor.domain.command.impl.workspace;

import com.team20.editor.domain.command.Command;
import com.team20.editor.domain.workspace.Workspace;
import com.team20.editor.extension.registry.DefaultCommandRegistry;
import com.team20.editor.bootstrap.ApplicationContext;

/**
 * undo - undo last undoable operation.
 *
 * This command uses the ApplicationContext.commandInvoker() to perform undo.
 * It attempts to call undo(Workspace) if available; otherwise falls back to
 * undo().
 * Provides user-friendly messages when undo is not available.
 */
public class UndoCommand implements Command {

    @Override
    public void execute(Workspace workspace) {
        ApplicationContext ctx = DefaultCommandRegistry.getApplicationContext();
        if (ctx == null) {
            System.out.println("无法执行 undo：ApplicationContext 未就绪");
            return;
        }
        var invoker = ctx.commandInvoker();
        if (invoker == null) {
            System.out.println("无法执行 undo：CommandInvoker 未就绪");
            return;
        }

        try {
            invoker.undo(workspace);
            System.out.println("撤销成功");
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
        } catch (Throwable t) {
            System.out.println("撤销失败: " + t.getMessage());
        }
    }

    @Override
    public String toString() {
        return "undo";
    }
}