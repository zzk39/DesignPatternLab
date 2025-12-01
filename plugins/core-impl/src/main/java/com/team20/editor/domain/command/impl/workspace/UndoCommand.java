package com.team20.editor.domain.command.impl.workspace;

import com.team20.editor.domain.command.Command;
import com.team20.editor.domain.workspace.Workspace;
import com.team20.editor.extension.registry.DefaultCommandRegistry;
import com.team20.editor.bootstrap.ApplicationContext;

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
            if (!invoker.canUndo()) {
                System.out.println("没有可撤销的命令");
                return;
            }
            invoker.undo(workspace);
            System.out.println("撤销成功");
        } catch (Throwable t) {
            System.out.println("撤销失败: " + t.getMessage());
        }
    }

    @Override
    public String toString() {
        return "undo";
    }
}