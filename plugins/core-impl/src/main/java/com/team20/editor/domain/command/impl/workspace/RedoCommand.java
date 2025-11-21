package com.team20.editor.domain.command.impl.workspace;

import com.team20.editor.domain.command.Command;
import com.team20.editor.domain.workspace.Workspace;
import com.team20.editor.extension.registry.DefaultCommandRegistry;
import com.team20.editor.bootstrap.ApplicationContext;

/**
 * redo - redo last undone operation.
 *
 * Similar strategy to UndoCommand: attempt redo(Workspace) then redo().
 */
public class RedoCommand implements Command {

    @Override
    public void execute(Workspace workspace) {
        ApplicationContext ctx = DefaultCommandRegistry.getApplicationContext();
        if (ctx == null) {
            System.out.println("无法执行 redo：ApplicationContext 未就绪");
            return;
        }
        var invoker = ctx.commandInvoker();
        if (invoker == null) {
            System.out.println("无法执行 redo：CommandInvoker 未就绪");
            return;
        }

        try {
            invoker.redo(workspace);
            System.out.println("重做成功");
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
        } catch (Throwable t) {
            System.out.println("重做失败: " + t.getMessage());
        }
    }

    @Override
    public String toString() {
        return "redo";
    }
}